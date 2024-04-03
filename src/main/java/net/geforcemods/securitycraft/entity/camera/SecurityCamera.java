package net.geforcemods.securitycraft.entity.camera;

import java.util.ArrayList;
import java.util.List;

import net.geforcemods.securitycraft.SCContent;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.blockentities.SecurityCameraBlockEntity;
import net.geforcemods.securitycraft.network.client.SetCameraView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ChunkTrackingView;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

public class SecurityCamera extends Entity {
	private static final List<Player> DISMOUNTED_PLAYERS = new ArrayList<>();
	protected float zoomAmount = 1F;
	protected boolean zooming = false;
	private ChunkTrackingView cameraChunks = null;
	private boolean hasSentChunks = false;
	private SecurityCameraBlockEntity be;

	public SecurityCamera(EntityType<SecurityCamera> type, Level level) {
		super(SCContent.SECURITY_CAMERA_ENTITY.get(), level);
		noPhysics = true;
	}

	public SecurityCamera(Level level, BlockPos pos) {
		this(SCContent.SECURITY_CAMERA_ENTITY.get(), level);

		if (!(level.getBlockEntity(pos) instanceof SecurityCameraBlockEntity cam)) {
			discard();
			return;
		}

		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.5D;
		double z = pos.getZ() + 0.5D;

		be = cam;

		if (be.isDown())
			y += 0.25D;

		setPos(x, y, z);
		setRot(be.getInitialYRotation(), be.getInitialXRotation());
	}

	@Override
	protected boolean repositionEntityAfterLoad() {
		return false;
	}

	@Override
	public void tick() {
		Level level = level();

		if (!level.isClientSide && level.getBlockState(blockPosition()).getBlock() != SCContent.SECURITY_CAMERA.get())
			discard();
	}

	public float getZoomAmount() {
		return zoomAmount;
	}

	public boolean isCameraDown() {
		return getBlockEntity() != null && !be.isRemoved() && be.isDown();
	}

	public void setRotation(float yaw, float pitch) {
		setRot(yaw, pitch);
	}

	public ChunkTrackingView getCameraChunks() {
		return cameraChunks;
	}

	public void setChunkLoadingDistance(int chunkLoadingDistance) {
		cameraChunks = ChunkTrackingView.of(chunkPosition(), chunkLoadingDistance);
	}

	public boolean hasSentChunks() {
		return hasSentChunks;
	}

	public void setHasSentChunks(boolean hasSentChunks) {
		this.hasSentChunks = hasSentChunks;
	}

	public static boolean hasRecentlyDismounted(Player player) {
		return DISMOUNTED_PLAYERS.remove(player);
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		discardCamera();
	}

	public void stopViewing(ServerPlayer player) {
		if (!level().isClientSide) {
			discard();
			player.camera = player;
			PacketDistributor.PLAYER.with(player).send(new SetCameraView(player));
			DISMOUNTED_PLAYERS.add(player);

			if (player.getEffect(MobEffects.NIGHT_VISION) instanceof CameraNightVisionEffectInstance)
				player.removeEffect(MobEffects.NIGHT_VISION);
		}
	}

	/**
	 * @deprecated Prefer calling {@link #discard()}
	 */
	@Deprecated
	public void discardCamera() {
		if (!level().isClientSide) {
			if (getBlockEntity() != null && !be.isRemoved())
				be.stopViewing();

			SectionPos chunkPos = SectionPos.of(blockPosition());
			int chunkLoadingDistance = cameraChunks instanceof ChunkTrackingView.Positioned cameraChunks ? cameraChunks.viewDistance() : level().getServer().getPlayerList().getViewDistance();

			for (int x = chunkPos.getX() - chunkLoadingDistance; x <= chunkPos.getX() + chunkLoadingDistance; x++) {
				for (int z = chunkPos.getZ() - chunkLoadingDistance; z <= chunkPos.getZ() + chunkLoadingDistance; z++) {
					SecurityCraft.CAMERA_TICKET_CONTROLLER.forceChunk((ServerLevel) level(), this, x, z, false, false);
				}
			}
		}
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return new ClientboundAddEntityPacket(this);
	}

	@Override
	public boolean isAlwaysTicking() {
		return true;
	}

	public SecurityCameraBlockEntity getBlockEntity() {
		if (be == null) {
			if (level().getBlockEntity(blockPosition()) instanceof SecurityCameraBlockEntity camera)
				be = camera;
			else
				SecurityCraft.LOGGER.warn("No security camera block entity was found at {}. Try breaking and replacing the block!", blockPosition());
		}

		return be;
	}
}
