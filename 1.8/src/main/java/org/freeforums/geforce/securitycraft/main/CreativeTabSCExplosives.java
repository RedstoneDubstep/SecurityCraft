package org.freeforums.geforce.securitycraft.main;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.freeforums.geforce.securitycraft.main.Utils.BlockUtils;

public class CreativeTabSCExplosives extends CreativeTabs{
	
	public CreativeTabSCExplosives(int par1, String par2Str){
		super(par1,par2Str);
	}

	@SideOnly(Side.CLIENT)
	public Item getTabIconItem(){
		return BlockUtils.getItemFromBlock(mod_SecurityCraft.Mine);
	}
	
	public String getTranslatedTabLabel(){
		return "SecurityCraft: Explosives";
		
	}

}
