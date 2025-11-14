package flaxbeard.immersivepetroleum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import flaxbeard.immersivepetroleum.client.render.IPRenderTypes;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import javax.annotation.Nonnull;

public class ModelPumpjack extends IPModel{
	public static final String ID = "pumpjackarm";
	public static final ResourceLocation TEXTURE = ResourceUtils.ip("textures/models/pumpjack_armature.png");
	
	public ModelPart origin;
	public ModelPart swingy;
	public ModelPart connector;
	public ModelPart arm;
	public ModelPart wellConnector;
	public ModelPart wellConnector2;
	
	public float ticks = 0;
	
	public ModelPumpjack(){
		super(IPRenderTypes::getEntitySolid);
		
		//this.texWidth = 190;
		//this.texHeight = 58;
	}
	
	@Override
	public void init(){
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition rootDefinition = meshDefinition.getRoot();
		
		PartDefinition origin_Definition = rootDefinition.addOrReplaceChild("origin", singleCube(0, 0, 0, 1, 1, 1), PartPose.ZERO);
		PartDefinition arm_Definition = origin_Definition.addOrReplaceChild("arm", singleCube(0, 40, -24 - 16, 0, -4, 70, 10, 8), PartPose.offset(56, 48, 24));
		arm_Definition.addOrReplaceChild("head", singleCube(30, -15, -5, 12, 30, 10), PartPose.ZERO);
		arm_Definition.addOrReplaceChild("barBack", singleCube(138, 0, -35F, 3F, -11F, 4, 4, 22), PartPose.ZERO);
		PartDefinition swingy_Definition = origin_Definition.addOrReplaceChild("swingy", singleCube(44, 14, -4F, -2F, -14F, 8, 10, 4), PartPose.offset(24, 30, 30));
		swingy_Definition.addOrReplaceChild("swingy2", singleCube(44, 14, -4F, -2F, -2F, 8, 10, 4), PartPose.ZERO);
		swingy_Definition.addOrReplaceChild("counter", singleCube(44, 0, -12F, 8F, -14F, 24, 10, 4), PartPose.ZERO);
		swingy_Definition.addOrReplaceChild("counter2", singleCube(44, 0, -12F, 8F, -2F, 24, 10, 4), PartPose.ZERO);
		PartDefinition connector_Definition = origin_Definition.addOrReplaceChild("connector", singleCube(108, 0, -1F, -1F, -12F, 2, 24, 2), PartPose.ZERO);
		connector_Definition.addOrReplaceChild("connector2", singleCube(100, 0, -1F, -1F, 6F, 2, 24, 2), PartPose.ZERO);
		origin_Definition.addOrReplaceChild("wellConnector", singleCube(108, 0, -1F, 0F, -1F, 2, 30, 2), PartPose.ZERO);
		origin_Definition.addOrReplaceChild("wellConnector2", singleCube(108, 0, -1F, 0F, -1F, 2, 16, 2), PartPose.ZERO);
		
		LayerDefinition layerDefinition = LayerDefinition.create(meshDefinition, 190, 58);
		ModelPart root = layerDefinition.bakeRoot();
		
		this.origin = root.getChild("origin");
		this.arm = this.origin.getChild("arm");
		this.swingy = this.origin.getChild("swingy");
		this.connector = this.origin.getChild("connector");
		this.wellConnector = this.origin.getChild("wellConnector");
		this.wellConnector2 = this.origin.getChild("wellConnector2");
	}
	
	@Override
	public void render(@Nonnull PoseStack matrixStackIn, @Nonnull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color){
		this.arm.zRot = (float) Math.toRadians(15 * Math.sin(this.ticks / 25F));
		this.swingy.zRot = (float) (2 * (Math.PI / 4) + (this.ticks / 25F));
		
		float dist = 8.5F;
		
		float sin = Mth.sin(this.swingy.zRot);
		float cos = Mth.cos(this.swingy.zRot);
		this.connector.setPos(24 - dist * sin, 30 + dist * cos, 26);
		if(sin < 0){
			this.connector.zRot = (float) (1F * Mth.HALF_PI + Math.atan(25F / (dist * sin)));
		}else if(sin > 0){
			this.connector.zRot = (float) (3F * Mth.HALF_PI + Math.atan(25F / (dist * sin)));
		}
		
		float sin2 = Mth.sin(this.arm.zRot);
		float cos2 = Mth.cos(this.arm.zRot);
		
		float x = 24 - dist * sin;
		float y = 30 + dist * cos;
		
		float w = 33F;
		float h = 4F;
		
		float tx = 56 + w * -cos2 - h * sin2;
		float ty = 48 + w * -sin2 + h * cos2;
		
		this.connector.setPos(x, y, 26);
		this.connector.zRot = (float) (3F * Mth.HALF_PI + Math.atan2(ty - y, tx - x));
		
		this.wellConnector.setPos(88F, 16F, 24F);
		this.wellConnector2.setPos(88F, 16F, 24F);
		
		float w2 = -34F;
		float h2 = -13F;
		
		float x2 = w2 * -cos2 - h2 * sin2;
		float y2 = w2 * -sin2 + h2 * cos2;
		
		float tx2 = 32F;
		float ty2 = -32F;
		
		this.wellConnector.setPos(56 + x2, 48 + y2, 24);
		this.wellConnector2.setPos(56 + x2, 48 + y2, 24);
		
		float zRot = (float) (3F * Mth.HALF_PI + Math.atan2(ty2 - y2, tx2 - x2));
		this.wellConnector.zRot = zRot;
		this.wellConnector2.zRot = zRot;
		
		double sqrt = Math.sqrt((tx2 - x2) * (tx2 - x2) + (ty2 - y2) * (ty2 - y2));
		if(sqrt <= 16){
			this.wellConnector.visible = true;
			this.wellConnector2.visible = false;
		}else{
			this.wellConnector.visible = true;
			this.wellConnector2.visible = true;
		}
		
		this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
	}
}
