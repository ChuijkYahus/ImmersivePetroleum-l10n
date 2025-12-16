package flaxbeard.immersivepetroleum.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import flaxbeard.immersivepetroleum.client.model.ModelMotorboat;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class EntityMotorboatRenderer extends EntityRenderer<MotorboatEntity>{
	private static final ResourceLocation texture = ResourceUtils.ip("textures/models/boat_motor.png");
	private static final ResourceLocation textureArmor = ResourceUtils.ip("textures/models/boat_motor_armor.png");
	
	/** instance of ModelBoat for rendering */
	protected final ModelMotorboat modelBoat = new ModelMotorboat();
	
	public EntityMotorboatRenderer(EntityRendererProvider.Context renderManagerIn){
		super(renderManagerIn);
		this.shadowRadius = 0.8F;
	}
	
	@Override
	public void render(@Nonnull MotorboatEntity boat, float entityYaw, float partialTicks, PoseStack matrix, @Nonnull MultiBufferSource bufferIn, int packedLight){
		matrix.pushPose();
		{
			matrix.translate(0.0D, 0.375D, 0.0D);
			this.setupRotation(boat, entityYaw, partialTicks, matrix);
			this.modelBoat.setupAnim(boat, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
			
			if(boat.isInLava()){
				matrix.translate(0, -3.9F / 16F, 0);
			}
			
			animatePropellerAssembly(boat, partialTicks);
			animatePropeller(boat, partialTicks);
			
			this.modelBoat.renderToBuffer(matrix, bufferIn.getBuffer(this.modelBoat.renderType(getEntityTexture(boat.isFireproof))), packedLight, OverlayTexture.NO_OVERLAY);
			
			if(boat.hasPaddles){
				VertexConsumer vbuilder_normal = bufferIn.getBuffer(this.modelBoat.renderType(texture));
				
				this.modelBoat.paddles[0].render(matrix, vbuilder_normal, packedLight, OverlayTexture.NO_OVERLAY);
				this.modelBoat.paddles[1].render(matrix, vbuilder_normal, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			VertexConsumer vbuilder_armored = bufferIn.getBuffer(this.modelBoat.renderType(textureArmor));
			
			if(boat.hasIcebreaker){
				this.modelBoat.icebreak.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			if(boat.hasRudders){
				this.modelBoat.ruddersBase.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
				
				animateRudders(boat, partialTicks);
				
				this.modelBoat.rudder1.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
				this.modelBoat.rudder2.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			if(boat.hasTank){
				this.modelBoat.tank.render(matrix, vbuilder_armored, packedLight, OverlayTexture.NO_OVERLAY);
			}
			
			if(!boat.isUnderWater()){
				VertexConsumer vbuilder_mask = bufferIn.getBuffer(RenderType.waterMask());
				this.modelBoat.noWaterRenderer().render(matrix, vbuilder_mask, packedLight, OverlayTexture.NO_OVERLAY);
			}
		}
		matrix.popPose();
		
		super.render(boat, entityYaw, partialTicks, matrix, bufferIn, packedLight);
	}
	
	private void animatePropeller(@Nonnull MotorboatEntity boat, float partialTicks){
		this.modelBoat.propeller.xRot = boat.propellerRotation.rotLerp(partialTicks) * Mth.DEG_TO_RAD;
	}
	
	private void animatePropellerAssembly(@Nonnull MotorboatEntity boat, float partialTicks){
		if(boat.isEmergency()){
			this.modelBoat.propellerAssembly.yRot = 0.0F;
			return;
		}
		
		this.modelBoat.propellerAssembly.yRot = 15 * boat.propellerAssemblyRotation.lerp(partialTicks) * Mth.DEG_TO_RAD;
	}
	
	private void animateRudders(@Nonnull MotorboatEntity boat, float partialTicks){
		float pr = 20 * boat.propellerAssemblyRotation.lerp(partialTicks) * Mth.DEG_TO_RAD;
		
		this.modelBoat.rudder1.yRot = pr;
		this.modelBoat.rudder2.yRot = pr;
	}
	
	@Override
	@Nonnull
	public ResourceLocation getTextureLocation(@Nonnull MotorboatEntity entity){
		return texture;
	}
	
	public ResourceLocation getEntityTexture(boolean armored){
		return armored ? textureArmor : texture;
	}
	
	public void setupRotation(MotorboatEntity boat, float entityYaw, float partialTicks, PoseStack matrix){
		matrix.mulPose(Axis.YP.rotationDegrees(180.0F - entityYaw));
		float f = (float) boat.getHurtTime() - partialTicks;
		float f1 = boat.getDamage() - partialTicks;
		
		if(f1 < 0.0F){
			f1 = 0.0F;
		}
		
		if(f > 0.0F){
			matrix.mulPose(Axis.XP.rotationDegrees(Mth.sin(f) * f * f1 / 10.0F * (float) boat.getHurtDir()));
		}
		
		if(boat.isBoosting){
			matrix.mulPose(Axis.XP.rotationDegrees(3));
		}
		
		matrix.scale(-1.0F, -1.0F, 1.0F);
		matrix.mulPose(Axis.YP.rotationDegrees(90.0F));
	}
}
