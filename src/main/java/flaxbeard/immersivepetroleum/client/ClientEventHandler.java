package flaxbeard.immersivepetroleum.client;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.items.BuzzsawItem;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import blusunrize.immersiveengineering.common.items.DrillItem;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.items.SpeedloaderItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.client.render.RenderUtils;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.CommonEventHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.fluids.IFluidTank;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

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
			if(!((DebugItem.getMode(main) == DebugItem.Mode.SEEDBASED_RESERVOIR) || (DebugItem.getMode(off) == DebugItem.Mode.SEEDBASED_RESERVOIR))){
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
				
				matrix.pushPose();
				{
					int scaledWidth = guiGraphics.guiWidth();
					int scaledHeight = guiGraphics.guiHeight();
					
					MultiBufferSource.BufferSource buffer = event.getGuiGraphics().bufferSource();
					VertexConsumer builder = buffer.getBuffer(RenderType.guiOverlay());
					//ItemOverlayUtils.getHudElementsBuilder(buffer);
					
					final IFluidTank tank = motorboat.getTank();
					
					int rightOffset = 0;
					if(MCUtil.getOptions().showSubtitles().get())
						rightOffset += 100;
					float dx = scaledWidth - rightOffset - 16;
					float dy = scaledHeight + offset;
					matrix.pushPose();
					{
						renderFluidTankOverlay(event.getGuiGraphics(), dx, dy, tank, (guiGraphics1, fluidHandler) -> {
							
						});
					}
					matrix.popPose();
					
					buffer.endBatch();
					
					if(holdingDebugItem && MCUtil.getFont() != null){
						matrix.pushPose();
						{
							Font font = MCUtil.getFont();
							
							FluidStack fs = tank.getFluid();
							
							Vec3 vec = motorboat.getDeltaMovement();
							float speed = (float) Math.sqrt(vec.x * vec.x + vec.z * vec.z);
							
							String[] array = {
								String.format(Locale.US, "Fuel: %05d/%d mB (%s)", fs.getAmount(), tank.getCapacity(), fs.getHoverName().getString()),
								String.format(Locale.US, "Speed: %.3f", speed),
								String.format(Locale.US, "propXRot: n%.3f o%.3f d%.3f", motorboat.propellerRotation.get(), motorboat.propellerRotation.getOld(), motorboat.propellerRotation.get() - motorboat.propellerRotation.getOld()),
								String.format(Locale.US, "propYRot: n%.3f o%.3f d%.3f", motorboat.propellerAssemblyRotation.get(), motorboat.propellerAssemblyRotation.getOld(), motorboat.propellerAssemblyRotation.get() - motorboat.propellerAssemblyRotation.getOld()),
								String.format(Locale.US, "propellerXRotSpeed: %06.3f°", motorboat.propellerRotationSpeed)
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
	
	static ResourceLocation GAUGE_FULL_EMPTY = ResourceUtils.ie("hud/gauge_full_empty");
	static ResourceLocation GAUGE_POINTER = ResourceUtils.ie("hud/gauge_pointer");
	static ResourceLocation GAUGE_OVERLAY = ResourceUtils.ie("hud/gauge_no_item");
	/** Modified copy of {@link blusunrize.immersiveengineering.client.ItemOverlayUtils#renderFluidTankOverlay(GuiGraphics, int, int, Player, InteractionHand, ItemStack, boolean, BiConsumer)} */
	public static void renderFluidTankOverlay(GuiGraphics graphics, float dx, float dy, IFluidTank tank, BiConsumer<GuiGraphics, IFluidTank> additionalRender){
		var transform = graphics.pose();
		transform.pushPose();
		transform.translate((int) dx, (int) dy, 0);
		graphics.blitSprite(GAUGE_FULL_EMPTY, -24, -68, 31, 62);
		
		transform.translate(-23, -37, 0);
		int capacity = tank.getCapacity();
		if(capacity > 0){
			FluidStack fs = tank.getFluid();
			float angle = 83 - (166 * fs.getAmount() / (float) capacity);
			transform.pushPose();
			transform.mulPose(new Quaternionf().rotateZ(angle * Mth.DEG_TO_RAD));
			graphics.blitSprite(GAUGE_POINTER, 6, -2, 24, 4);
			transform.popPose();
			transform.translate(23, 37, 0);
			
			additionalRender.accept(graphics, tank);
		}
		graphics.blitSprite(GAUGE_OVERLAY, -41, -73, 53, 72);
		transform.popPose();
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
