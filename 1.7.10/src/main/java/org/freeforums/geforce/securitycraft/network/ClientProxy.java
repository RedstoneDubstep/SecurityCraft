package org.freeforums.geforce.securitycraft.network;

import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import org.freeforums.geforce.securitycraft.entity.EntityTnTCompact;
import org.freeforums.geforce.securitycraft.lookingglass.IWorldViewHelper;
import org.freeforums.geforce.securitycraft.main.mod_SecurityCraft;
import org.freeforums.geforce.securitycraft.misc.KeyBindings;
import org.freeforums.geforce.securitycraft.models.ModelClaymore;
import org.freeforums.geforce.securitycraft.models.ModelKeypadFrame;
import org.freeforums.geforce.securitycraft.models.ModelKeypadFurnaceDeactivated;
import org.freeforums.geforce.securitycraft.models.ModelSecurityCamera;
import org.freeforums.geforce.securitycraft.renderers.CustomModeledBlockRenderer;
import org.freeforums.geforce.securitycraft.renderers.ItemCameraMonitorRenderer;
import org.freeforums.geforce.securitycraft.renderers.ItemTaserRenderer;
import org.freeforums.geforce.securitycraft.renderers.RenderTnTCompact;
import org.freeforums.geforce.securitycraft.renderers.TileEntityMonitorRenderer;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityClaymore;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityClaymoreRenderer;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeypadChest;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeypadChestRenderer;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeypadFrame;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeypadFrameRenderer;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeypadFurnace;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityKeypadFurnaceRenderer;
import org.freeforums.geforce.securitycraft.tileentity.TileEntityMonitor;
import org.freeforums.geforce.securitycraft.tileentity.TileEntitySecurityCamera;
import org.freeforums.geforce.securitycraft.tileentity.TileEntitySecurityCameraRenderer;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends ServerProxy{
	
	public HashMap<String, IWorldViewHelper> worldViews = new HashMap<String, IWorldViewHelper>();
			
	@Override
	public void registerRenderThings(){
		KeyBindings.init();
				
		RenderingRegistry.registerEntityRenderingHandler(EntityTnTCompact.class, new RenderTnTCompact());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeypadChest.class, new TileEntityKeypadChestRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeypadFrame.class, new TileEntityKeypadFrameRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityKeypadFurnace.class, new TileEntityKeypadFurnaceRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityClaymore.class, new TileEntityClaymoreRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySecurityCamera.class, new TileEntitySecurityCameraRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMonitor.class, new TileEntityMonitorRenderer());

		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.keypadChest), new CustomModeledBlockRenderer(new TileEntityKeypadChest()));
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.keypadFrame), new CustomModeledBlockRenderer(new TileEntityKeypadFrame(), new ModelKeypadFrame(), 0.0D, -0.1D, 0.0D, 0.0F));
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.keypadFurnace), new CustomModeledBlockRenderer(new TileEntityKeypadFurnace(), new ModelKeypadFurnaceDeactivated(), 0.0D, -0.1D, 0.0D, 0.0F));
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.claymoreActive), new CustomModeledBlockRenderer(new TileEntityClaymore(), new ModelClaymore(), 0.0D, -0.1D, 0.0D, 0.0F));
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.securityCamera), new CustomModeledBlockRenderer(new TileEntitySecurityCamera(), new ModelSecurityCamera(), 0.0D, -0.1D, 0.0D, 0.0F));
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.securityCameraLit), new CustomModeledBlockRenderer(new TileEntitySecurityCamera(), new ModelSecurityCamera(), 0.0D, -0.1D, 0.0D, 0.0F));
		MinecraftForgeClient.registerItemRenderer(mod_SecurityCraft.cameraMonitor, new ItemCameraMonitorRenderer());
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mod_SecurityCraft.monitor), new CustomModeledBlockRenderer(new TileEntityMonitor(), new ModelKeypadFrame(), 0.0D, -0.1D, 0.0D, 0.0F));
		MinecraftForgeClient.registerItemRenderer(mod_SecurityCraft.taser, new ItemTaserRenderer());
	}
	
	public void registerClientTickHandler(){
		
	}
	

}
