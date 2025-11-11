package flaxbeard.immersivepetroleum.common.util;

import blusunrize.immersiveengineering.api.Lib;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class ResourceUtils{
	public static ResourceLocation ip(String str){
		return ResourceLocation.fromNamespaceAndPath(ImmersivePetroleum.MODID, str);
	}
	
	public static ModelResourceLocation ipModel(String str){
		return ModelResourceLocation.standalone(ip(str));
	}
	
	public static ResourceLocation ct(String str){
		return ResourceLocation.fromNamespaceAndPath("crafttweaker", str);
	}
	
	public static ResourceLocation ie(String str){
		return ResourceLocation.fromNamespaceAndPath(Lib.MODID, str);
	}
	
	public static ResourceLocation forge(String str){
		return ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, str);
	}
	
	public static ResourceLocation mc(String str){
		return ResourceLocation.fromNamespaceAndPath(ResourceLocation.DEFAULT_NAMESPACE, str);
	}
}
