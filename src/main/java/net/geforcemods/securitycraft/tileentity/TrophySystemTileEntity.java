package net.geforcemods.securitycraft.tileentity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.api.CustomizableTileEntity;
import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.entity.BulletEntity;
import net.geforcemods.securitycraft.misc.ModuleType;
import net.geforcemods.securitycraft.network.server.SyncTrophySystem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ExperienceBottleEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.network.PacketDistributor;

public class TrophySystemTileEntity extends CustomizableTileEntity implements ITickableTileEntity {

	/* The range (in blocks) that the trophy system will search for projectiles in */
	public static final int RANGE = 10;

	/* Number of ticks that the trophy takes to "charge" */
	public static final int COOLDOWN_TIME = 8;

	/* The number of blocks away from the trophy system you can be for
	 * the laser beam between itself and the projectile to be rendered */
	public static final int RENDER_DISTANCE = 50;

	private final Map<EntityType<?>, Boolean> projectileFilter = new LinkedHashMap<>();
	public ProjectileEntity entityBeingTargeted = null;
	public int cooldown = COOLDOWN_TIME;
	private final Random random = new Random();

	public TrophySystemTileEntity()
	{
		super(SCContent.teTypeTrophySystem);
		//when adding new types ONLY ADD TO THE END. anything else will break saved data
		projectileFilter.put(SCContent.eTypeBullet, true);
		projectileFilter.put(EntityType.SPECTRAL_ARROW, true);
		projectileFilter.put(EntityType.ARROW, true);
		projectileFilter.put(EntityType.SMALL_FIREBALL, true);
		projectileFilter.put(SCContent.eTypeImsBomb, true);
		projectileFilter.put(EntityType.FIREBALL, true);
		projectileFilter.put(EntityType.DRAGON_FIREBALL, true);
		projectileFilter.put(EntityType.WITHER_SKULL, true);
		projectileFilter.put(EntityType.SHULKER_BULLET, true);
		projectileFilter.put(EntityType.LLAMA_SPIT, true);
		projectileFilter.put(EntityType.EGG, true);
		projectileFilter.put(EntityType.ENDER_PEARL, true);
		projectileFilter.put(EntityType.SNOWBALL, true);
		projectileFilter.put(EntityType.FIREWORK_ROCKET, true);
		projectileFilter.put(EntityType.PIG, false); //modded projectiles
	}

	@Override
	public void tick() {
		// If the trophy does not have a target, try looking for one
		if(entityBeingTargeted == null) {
			ProjectileEntity target = getTarget();
			UUID shooterUUID = getShooterUUID(target);

			if(target != null && (shooterUUID == null || !shooterUUID.toString().equals(getOwner().getUUID()))) {
				entityBeingTargeted = target;
			}
		}

		// If there are no entities to target, return
		if(entityBeingTargeted == null)
			return;

		if(!entityBeingTargeted.isAlive())
		{
			resetTarget();
			return;
		}

		// If the cooldown hasn't finished yet, don't destroy any projectiles
		if(cooldown > 0) {
			cooldown--;
			return;
		}

		destroyTarget();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(getPos()).grow(RENDER_DISTANCE);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		super.write(tag);

		CompoundNBT projectilesNBT = new CompoundNBT();
		int i = 0;

		for (boolean b : projectileFilter.values()) {
			projectilesNBT.putBoolean("projectile" + i, b);
			i++;
		}

		tag.put("projectiles", projectilesNBT);
		return tag;
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		super.read(state, tag);

		CompoundNBT projectilesNBT = tag.getCompound("projectiles");
		int i = 0;

		for (EntityType<?> projectileType : projectileFilter.keySet()) {
			projectileFilter.put(projectileType, projectilesNBT.getBoolean("projectile" + i));
			i++;
		}
	}

	/**
	 * Deletes the targeted entity and creates a small explosion where it last was
	 */
	private void destroyTarget() {
		entityBeingTargeted.remove();

		if(!world.isRemote)
			world.createExplosion(null, entityBeingTargeted.getPosX(), entityBeingTargeted.getPosY(), entityBeingTargeted.getPosZ(), 0.1F, Explosion.Mode.NONE);

		resetTarget();
	}

	/**
	 * Resets the cooldown and targeted entity variables
	 */
	private void resetTarget() {
		cooldown = COOLDOWN_TIME;
		entityBeingTargeted = null;
	}

	/**
	 * Randomly returns a new Entity target from the list of all entities
	 * within range of the trophy
	 */
	private ProjectileEntity getTarget() {
		List<ProjectileEntity> potentialTargets = new ArrayList<>();
		AxisAlignedBB area = new AxisAlignedBB(pos).grow(RANGE, RANGE, RANGE);

		potentialTargets.addAll(world.getEntitiesWithinAABB(ProjectileEntity.class, area, this::isAllowedToTarget));

		//remove bullets shot by sentries of this trophy system's owner
		potentialTargets = potentialTargets.stream().filter(e -> !(e instanceof BulletEntity && ((BulletEntity)e).getOwner().equals(getOwner()))).collect(Collectors.toList());

		// If there are no projectiles, return
		if(potentialTargets.size() <= 0) return null;

		// Return a random entity to target from the list of all possible targets
		int target = random.nextInt(potentialTargets.size());

		return potentialTargets.get(target);
	}

	private boolean isAllowedToTarget(ProjectileEntity target) {
		if (target instanceof TridentEntity || target instanceof FishingBobberEntity || target instanceof PotionEntity || target instanceof ExperienceBottleEntity)
			return false;

		//try to get the target's type filter first. if not found, it's a modded projectile and the return value falls back to the modded filter (designated by the PIG entity type)
		return projectileFilter.getOrDefault(target.getType(), projectileFilter.get(EntityType.PIG));
	}

	/**
	 * Returns the UUID of the player who shot the given Entity
	 */
	public UUID getShooterUUID(Entity entity) {
		if(entity instanceof AbstractArrowEntity && ((AbstractArrowEntity) entity).func_234616_v_() != null) //getShooter
			return ((AbstractArrowEntity) entity).func_234616_v_().getUniqueID(); //getShooter
		else if(entity instanceof FireballEntity && ((FireballEntity) entity).func_234616_v_() != null) //getShooter
			return ((FireballEntity) entity).func_234616_v_().getUniqueID(); //getShooter
		else
			return null;
	}

	public void toggleFilter(EntityType<?> projectileType) {
		setFilter(projectileType, !projectileFilter.get(projectileType));
	}

	public void toggleFilter(int index)
	{
		int i = 0;

		for(EntityType<?> projectileType : projectileFilter.keySet())
		{
			if(i++ == index)
			{
				toggleFilter(projectileType);
				return;
			}
		}
	}

	public void setFilter(EntityType<?> projectileType, boolean allowed) {
		if(projectileFilter.containsKey(projectileType))
		{
			projectileFilter.put(projectileType, allowed);

			if (world.isRemote) {
				SecurityCraft.channel.send(PacketDistributor.SERVER.noArg(), new SyncTrophySystem(pos, projectileType, allowed));
			}
		}
	}

	public boolean getFilter(EntityType<?> projectileType) {
		return projectileFilter.get(projectileType);
	}

	public int getFilterSize() {
		return projectileFilter.size();
	}

	public Map<EntityType<?>,Boolean> getFilters()
	{
		return projectileFilter;
	}

	@Override
	public void onModuleRemoved(ItemStack stack, ModuleType module) {
		super.onModuleRemoved(stack, module);

		if (module == ModuleType.SMART) {
			for (EntityType<?> projectileType : projectileFilter.keySet()) {
				projectileFilter.put(projectileType, projectileType != EntityType.PIG);
			}
		}
	}

	@Override
	public ModuleType[] acceptedModules() {
		return new ModuleType[]{ModuleType.SMART};
	}

	@Override
	public Option<?>[] customOptions() {
		return null;
	}
}
