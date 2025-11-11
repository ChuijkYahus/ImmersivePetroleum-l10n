package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.ItemOverlayUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.client.render.RenderUtils;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent.OverlayType;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClientEventHandler{
	
	private static LubricatorGhostRenderer lubricatorGhostRenderer;
	
	@SubscribeEvent
	public void renderLevelStage(RenderLevelStageEvent event){
		if(event.getStage() == Stage.AFTER_TRIPWIRE_BLOCKS){
			if(lubricatorGhostRenderer == null)
				lubricatorGhostRenderer = new LubricatorGhostRenderer(Minecraft.getInstance());
			
			lubricatorGhostRenderer.render(event.getPoseStack());
		}
	}
	
	@SubscribeEvent
	public void reservoirDebuggingOverlayText(RenderGuiLayerEvent.Post event){
		if(ReservoirHandler.getGenerator() == null){
			return;
		}
		
		Player player = MCUtil.getPlayer();
		
		ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
		ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
		
		if((main != ItemStack.EMPTY && main.getItem() == IPContent.DEBUGITEM.get()) || (off != ItemStack.EMPTY && off.getItem() == IPContent.DEBUGITEM.get())){
			if(!((DebugItem.getMode(main) == DebugItem.Modes.SEEDBASED_RESERVOIR) || (DebugItem.getMode(off) == DebugItem.Modes.SEEDBASED_RESERVOIR))){
				return;
			}
			
			List<Component> debugOut = new ArrayList<>();
			
			if(!debugOut.isEmpty()){
				GuiGraphics guiGraphics = event.getGuiGraphics();
				PoseStack matrix = guiGraphics.pose();
				matrix.pushPose();
				for(int i = 0;i < debugOut.size();i++){
					int w = ClientUtils.font().width(debugOut.get(i).getString());
					int yOff = i * (ClientUtils.font().lineHeight + 2);
					
					matrix.pushPose();
					matrix.translate(0, 0, 1);
					RenderUtils.drawColouredRect(guiGraphics, 1, 1 + yOff, w + 1, 10);
					// Draw string without shadow
					guiGraphics.drawString(ClientUtils.font(), debugOut.get(i), 2, 2 + yOff, -1, false);
					matrix.popPose();
				}
				matrix.popPose();
			}
		}
	}
	
	@SubscribeEvent
	public void renderInfoOverlays(RenderGuiLayerEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getName() == VanillaGuiLayers.HOTBAR){
			Player player = MCUtil.getPlayer();
			
			HitResult result = MCUtil.getHitResult();
			if(result == null || result.getType() != HitResult.Type.ENTITY)
				return;
			
			if(result instanceof EntityHitResult eHit){
				if(eHit.getEntity() instanceof MotorboatEntity motorboat){
					String[] text = motorboat.getOverlayText(player, result);
					
					if(text != null && text.length > 0){
						Font font = ClientUtils.font();
						int col = 0xffffff;
						for(int i = 0;i < text.length;i++){
							if(text[i] != null){
								int fx = event.getGuiGraphics().guiWidth() / 2 + 8;
								int fy = event.getGuiGraphics().guiHeight() / 2 + 8 + i * font.lineHeight;
								event.getGuiGraphics().drawString(font, text[i], fx, fy, col);
							}
						}
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onRenderOverlayPost(RenderGuiLayerEvent.Post event){
		if(MCUtil.getPlayer() != null && event.getName() == VanillaGuiLayers.HOTBAR){
			Player player = MCUtil.getPlayer();
			GuiGraphics guiGraphics = event.getGuiGraphics();
			PoseStack matrix = guiGraphics.pose();
			
			if(player.getVehicle() instanceof MotorboatEntity motorboat){
				int offset = 0;
				boolean holdingDebugItem = false;
				for(InteractionHand hand:InteractionHand.values()){
					if(!player.getItemInHand(hand).isEmpty()){
						ItemStack equipped = player.getItemInHand(hand);
						if((equipped.getItem() instanceof DrillItem) || (equipped.getItem() instanceof ChemthrowerItem) || (equipped.getItem() instanceof BuzzsawItem)){
							offset -= 85;
						}else if((equipped.getItem() instanceof RevolverItem) || (equipped.getItem() instanceof SpeedloaderItem)){
							offset -= 65;
						}else if(equipped.getItem() instanceof RailgunItem){
							offset -= 50;
						}else if(equipped.getItem() instanceof IEShieldItem){
							offset -= 40;
						}
						
						if(equipped.getItem() instanceof DebugItem){
							holdingDebugItem = true;
						}
					}
				}
				
				// FIXME
				boolean enable = false;
				if(enable){
					matrix.pushPose();
					{
						int scaledWidth = guiGraphics.guiWidth();
						int scaledHeight = guiGraphics.guiWidth();
						
						MultiBufferSource.BufferSource buffer = event.getGuiGraphics().bufferSource();
						//MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
						VertexConsumer builder = null;//ItemOverlayUtils.getHudElementsBuilder(buffer);
						
						int rightOffset = 0;
						if(MCUtil.getOptions().showSubtitles().get())
							rightOffset += 100;
						float dx = scaledWidth - rightOffset - 16;
						float dy = scaledHeight + offset;
						matrix.pushPose();
						{
							matrix.translate(dx, dy, 0);
							GuiHelper.drawTexturedRect(builder, matrix, -24, -68, 31, 62, 256f, 179, 210, 9, 71);
							
							matrix.translate(-23, -37, 0);
							float capacity = motorboat.getMaxFuel();
							if(capacity > 0){
								FluidStack fuel = motorboat.getContainedFluid();
								int amount = fuel.getAmount();
								float angle = 83 - (166 * amount / capacity);
								matrix.pushPose();
								matrix.mulPose(Axis.ZP.rotationDegrees(angle));
								GuiHelper.drawTexturedRect(builder, matrix, 6, -2, 24, 4, 256f, 91, 123, 80, 87);
								matrix.popPose();
								matrix.translate(23, 37, 0);
								
								GuiHelper.drawTexturedRect(builder, matrix, -41, -73, 53, 72, 256f, 8, 61, 4, 76);
							}
						}
						matrix.popPose();
						
						buffer.endBatch();
						
						if(holdingDebugItem && MCUtil.getFont() != null){
							matrix.pushPose();
							{
								Font font = MCUtil.getFont();
								
								int capacity = motorboat.getMaxFuel();
								FluidStack fs = motorboat.getContainedFluid();
								int amount = (fs == FluidStack.EMPTY || fs.getFluid() == null) ? 0 : fs.getAmount();
								
								Vec3 vec = motorboat.getDeltaMovement();
								float speed = (float) Math.sqrt(vec.x * vec.x + vec.z * vec.z);
								
								String[] array = {
									String.format(Locale.US, "Fuel: %05d/%d mB (%s)", amount, capacity, fs.getHoverName().getString()),
									String.format(Locale.US, "Speed: %.3f", speed),
									String.format(Locale.US, "PropXRot: %07.3f° (%.3frad)", motorboat.propellerXRot, motorboat.propellerXRot * Mth.DEG_TO_RAD),
									String.format(Locale.US, "PropSpeed: %06.3f°", motorboat.propellerXRotSpeed),
									};
								int w = 3, h = 3;
								for(int i = 0;i < array.length;i++){
									guiGraphics.drawString(font, array[i], w, h + (9 * i), -1);
								}
							}
							matrix.popPose();
						}
					}
					matrix.popPose();
				}
			}
		}
	}
	
	@SubscribeEvent
	public void handleBoatImmunity(RenderBlockScreenEffectEvent event){
		Player entity = event.getPlayer();
		if(event.getOverlayType() == OverlayType.FIRE && entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity boat){
			if(boat.isFireproof){
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public void handleFireRender(RenderPlayerEvent.Pre event){
		Player entity = event.getEntity();
		if(entity.isOnFire() && entity.getVehicle() instanceof MotorboatEntity boat){
			if(boat.isFireproof){
				entity.clearFire();
			}
		}
	}
	
	@SubscribeEvent
	public void handleLubricatingMachinesClient(ClientTickEvent.Post event){
		if(MCUtil.getLevel() != null){
			CommonEventHandler.handleLubricatingMachines(MCUtil.getLevel());
		}
	}
}
