package flaxbeard.immersivepetroleum.common;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class CommonProxy{
	
	public void setup(FMLCommonSetupEvent event){
	}
	
	public void registerContainersAndScreens(RegisterMenuScreensEvent ev){
	}
	
	public void completed(FMLLoadCompleteEvent event){
	}
	
	public void renderTile(BlockEntity te, VertexConsumer iVertexBuilder, PoseStack transform, MultiBufferSource buffer){
	}
	
	public void handleEntitySound(Holder<SoundEvent> soundEvent, Entity entity, boolean active, float volume, float pitch){
	}
	
	public void handleTileSound(Holder<SoundEvent> soundEvent, BlockEntity te, boolean active, float volume, float pitch){
	}
	
	public void drawUpperHalfSlab(PoseStack transform, ItemStack stack){
	}
	
	public Level getClientWorld(){
		return null;
	}
	
	public Player getClientPlayer(){
		return null;
	}
}
