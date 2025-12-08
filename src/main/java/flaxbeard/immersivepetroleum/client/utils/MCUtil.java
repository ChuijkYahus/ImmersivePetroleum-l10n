package flaxbeard.immersivepetroleum.client.utils;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Central place for Minecraft instance related stuff.<br>
 * <br>
 * Primarly so that the
 * <code>"Resource leak: 'unassigned Closeable value' is not closed at this
 * location"</code> warning is contained in here and not scattered all over the
 * place
 *
 * @author TwistedGate
 */
@OnlyIn(Dist.CLIENT)
public class MCUtil{
	
	public static void bindTexture(ResourceLocation texture){
		RenderSystem.setShaderTexture(0, texture);
	}
	
	public static void setScreen(Screen screen){
		Minecraft.getInstance().setScreen(screen);
	}
	
	public static Screen getScreen(){
		Minecraft mc = Minecraft.getInstance();
		return mc.screen;
	}
	
	public static ParticleEngine getParticleEngine(){
		Minecraft mc = Minecraft.getInstance();
		return mc.particleEngine;
	}
	
	public static TextureManager getTextureManager(){
		Minecraft mc = Minecraft.getInstance();
		return mc.getTextureManager();
	}
	
	public static BlockRenderDispatcher getBlockRenderer(){
		Minecraft mc = Minecraft.getInstance();
		return mc.getBlockRenderer();
	}
	
	public static ModelManager getModelManager(){
		Minecraft mc = Minecraft.getInstance();
		return mc.getBlockRenderer().getBlockModelShaper().getModelManager();
	}
	
	public static TextureAtlasSprite getBlockSprite(ResourceLocation resourceLocation){
		return getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(resourceLocation);
	}
	
	public static BakedModel getModel(ModelResourceLocation modelLocation){
		return getModelManager().getModel(modelLocation);
	}
	
	public static GameRenderer getGameRenderer(){
		Minecraft mc = Minecraft.getInstance();
		return mc.gameRenderer;
	}
	
	public static ClientLevel getLevel(){
		Minecraft mc = Minecraft.getInstance();
		return mc.level;
	}
	
	public static Font getFont(){
		Minecraft mc = Minecraft.getInstance();
		return mc.font;
	}
	
	public static LocalPlayer getPlayer(){
		Minecraft mc = Minecraft.getInstance();
		return mc.player;
	}
	
	public static HitResult getHitResult(){
		Minecraft mc = Minecraft.getInstance();
		return mc.hitResult;
	}
	
	public static Options getOptions(){
		Minecraft mc = Minecraft.getInstance();
		return mc.options;
	}
	
	public static Window getWindow(){
		Minecraft mc = Minecraft.getInstance();
		return mc.getWindow();
	}
	
	public static ItemRenderer getItemRenderer(){
		Minecraft mc = Minecraft.getInstance();
		return mc.getItemRenderer();
	}
	
	public static GuiSpriteManager getGuiSpriteManager(){
		Minecraft mc = Minecraft.getInstance();
		return mc.getGuiSprites();
	}
}
