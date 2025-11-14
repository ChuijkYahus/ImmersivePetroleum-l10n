package flaxbeard.immersivepetroleum.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;

import javax.annotation.Nonnull;

public class ModelLubricantPipes{
	
	public static class Crusher extends IPModel{
		public static final String ID = "crusher_lubepipes";
		
		private ModelPart origin;
		public Crusher(){
			super(RenderType::entitySolid);
		}
		
		@Override
		public void init(){
			MeshDefinition meshDefinition = new MeshDefinition();
			
			PartDefinition origin = meshDefinition.getRoot().addOrReplaceChild("origin", singleCube(20, 8, 9, 12, 2, 2), PartPose.ZERO);
			
			origin.addOrReplaceChild("p1", singleCube(-1, -1, 0, 12, 2, 2), PartPose.offsetAndRotation(20, 9, 10, 0, (float) Math.toRadians(270), 0));
			origin.addOrReplaceChild("p2", singleCube(-1, -1, 0, 18, 2, 2), PartPose.offsetAndRotation(31, 9, -10, 0, (float) Math.toRadians(270), 0));
			origin.addOrReplaceChild("p3", singleCube(0, -1, -1, 40, 2, 2), PartPose.offsetAndRotation(30, 10, -10, 0, 0, (float) Math.toRadians(90)));
			origin.addOrReplaceChild("p5", singleCube(31, 8, 5, 1, 2, 2), PartPose.ZERO);
			origin.addOrReplaceChild("p6", singleCube(23, 48, -11, 6, 2, 2), PartPose.ZERO);
			origin.addOrReplaceChild("p7", singleCube(8, 8, 19, 10, 2, 2), PartPose.ZERO);
			origin.addOrReplaceChild("p8", singleCube(-1, -1, 0, 5, 2, 2), PartPose.offsetAndRotation(8, 9, 17, 0, (float) Math.toRadians(270), 0));
			origin.addOrReplaceChild("p9", singleCube(0, -1, -1, 14, 2, 2), PartPose.offsetAndRotation(7, 10, 17, 0, 0, (float) Math.toRadians(90)));
			
			this.origin = LayerDefinition.create(meshDefinition, 16, 16).bakeRoot().getChild("origin");
		}
		
		@Override
		public void render(@Nonnull PoseStack matrixStackIn, @Nonnull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color){
			this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
		}
	}
	
	public static class Excavator extends IPModel{
		public static final String ID_NORMAL = "excavator_lubepipes_normal";
		public static final String ID_MIRRORED = "excavator_lubepipes_mirrored";
		
		private ModelPart origin;
		private boolean mirrored;
		public Excavator(boolean mirror){
			super(RenderType::entitySolid);
			this.mirrored = mirror;
		}
		
		@Override
		public void init(){
			MeshDefinition meshDefinition = new MeshDefinition();
			
			if(this.mirrored){
				PartDefinition origin = meshDefinition.getRoot().addOrReplaceChild("origin", singleCube(51, 8, 6, 20, 2, 2), PartPose.ZERO);
				
				origin.addOrReplaceChild("p1", singleCube(-1, -1, 0, 6, 2, 2), PartPose.offsetAndRotation(71, 9, 1, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p2", singleCube(-1, -1, 0, 4, 2, 2), PartPose.offsetAndRotation(53, 9, 3, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p3", singleCube(0, -1, -1, 6, 2, 2), PartPose.offsetAndRotation(52, 10, 3, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p4", singleCube(0, -1, -1, 9, 2, 2), PartPose.offsetAndRotation(52, 32, 8, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p5", singleCube(48, 39, 7, 3, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p6", singleCube(0, -1, -1, 18, 2, 2), PartPose.offsetAndRotation(52, 16, -1, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p7", singleCube(-1, -1, 0, 4, 2, 2), PartPose.offsetAndRotation(53, 15, -1, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p8", singleCube(-1, -1, 0, 7, 2, 2), PartPose.offsetAndRotation(53, 33, 1, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p9", singleCube(-1, -1, 0, 2, 2, 2), PartPose.offsetAndRotation(75, 9, 1, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p10", singleCube(73, 8, 2, 16, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p11", singleCube(-1, -1, 0, 4, 2, 2), PartPose.offsetAndRotation(89, 9, 5, 0, (float) Math.toRadians(270), 0));
				
			}else{
				PartDefinition origin = meshDefinition.getRoot().addOrReplaceChild("origin", singleCube(51, 8, 40, 20, 2, 2), PartPose.ZERO);
				
				origin.addOrReplaceChild("p1", singleCube(-1, -1, 0, 6, 2, 2), PartPose.offsetAndRotation(71, 9, 43, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p2", singleCube(-1, -1, 0, 4, 2, 2), PartPose.offsetAndRotation(53, 9, 43, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p3", singleCube(0, -1, -1, 6, 2, 2), PartPose.offsetAndRotation(52, 10, 45, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p4", singleCube(0, -1, -1, 9, 2, 2), PartPose.offsetAndRotation(52, 32, 40, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p5", singleCube(48, 39, 39, 3, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p6", singleCube(0, -1, -1, 18, 2, 2), PartPose.offsetAndRotation(52, 16, 49, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p7", singleCube(-1, -1, 0, 4, 2, 2), PartPose.offsetAndRotation(53, 15, 47, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p8", singleCube(-1, -1, 0, 7, 2, 2), PartPose.offsetAndRotation(53, 33, 42, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p9", singleCube(-1, -1, 0, 2, 2, 2), PartPose.offsetAndRotation(75, 9, 47, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p10", singleCube(73, 8, 44, 16, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p11", singleCube(-1, -1, 0, 4, 2, 2), PartPose.offsetAndRotation(89, 9, 41, 0, (float) Math.toRadians(270), 0));
			}
			
			this.origin = LayerDefinition.create(meshDefinition, 16, 16).bakeRoot().getChild("origin");
		}
		
		@Override
		public void render(@Nonnull PoseStack matrixStackIn, @Nonnull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color){
			this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
		}
	}
	
	public static class Pumpjack extends IPModel{
		public static final String ID_NORMAL = "pumpjack_lubepipes_normal";
		public static final String ID_MIRRORED = "pumpjack_lubepipes_mirrored";
		
		private boolean mirrored = false;
		private ModelPart origin;
		public Pumpjack(boolean mirror){
			super(RenderType::entitySolid);
			this.mirrored = mirror;
		}
		
		@Override
		public void init(){
			MeshDefinition meshDefinition = new MeshDefinition();
			
			if(this.mirrored){
				PartDefinition origin = meshDefinition.getRoot().addOrReplaceChild("origin", singleCube(21, 8, 12, 15, 2, 2), PartPose.ZERO);
				
				origin.addOrReplaceChild("p1", singleCube(-1, -1, 0, 12, 2, 2), PartPose.offsetAndRotation(23, 9, 1, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p2", singleCube(-1, -1, 0, 13, 2, 2), PartPose.offsetAndRotation(38, 9, 13, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p3", singleCube(34, 8, 23, 2, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p4", singleCube(0, -1, -1, 30, 2, 2), PartPose.offsetAndRotation(33, 8, 24, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p5", singleCube(24, 36, 23, 8, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p6", singleCube(0, -1F, -1, 9, 2, 2), PartPose.offsetAndRotation(26, 9.01F, 0, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p7", singleCube(25, 8, 8.5F, 18, 2, 2), PartPose.ZERO);
				
				PartDefinition leg1_Definition = origin.addOrReplaceChild("leg1", empty(), PartPose.offsetAndRotation(56 - 13.6F, 8F, 12F, (float) Math.toRadians(9), (float) Math.toRadians(20), (float) Math.toRadians(-14)));
				leg1_Definition.addOrReplaceChild("leg2", singleCube(1F, -1F, -4F, 38, 2, 2), PartPose.rotation(0, 0, (float) Math.toRadians(90)));
				
				origin.addOrReplaceChild("p8", singleCube(0, 0, 0, 4, 2, 2), PartPose.offsetAndRotation(52.5F, 43.3F, 14.7F, 0, (float) Math.toRadians(30), 0));
				origin.addOrReplaceChild("p9", singleCube(0, -2, 0, 6, 2, 2), PartPose.offsetAndRotation(55f, 43.3f, 13f, 0, 0, (float) Math.toRadians(90)));
				
			}else{
				PartDefinition origin = meshDefinition.getRoot().addOrReplaceChild("origin", singleCube(21, 8, 48 - 12 - 2, 15, 2, 2), PartPose.ZERO);
				
				origin.addOrReplaceChild("p1", singleCube(-1, -1, 0, 12, 2, 2), PartPose.offsetAndRotation(23, 9, 37, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p2", singleCube(-1, -1, 0, 13, 2, 2), PartPose.offsetAndRotation(38, 9, 24, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p3", singleCube(34, 8, 23, 2, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p4", singleCube(0, -1, -1, 30, 2, 2), PartPose.offsetAndRotation(33, 8, 24, 0, 0, (float) Math.toRadians(90)));
				origin.addOrReplaceChild("p5", singleCube(24, 36, 23, 8, 2, 2), PartPose.ZERO);
				origin.addOrReplaceChild("p6", singleCube(39, -1F, -1, 9, 2, 2), PartPose.offsetAndRotation(26, 9.01F, 0, 0, (float) Math.toRadians(270), 0));
				origin.addOrReplaceChild("p7", singleCube(25, 8, 38.5F, 18, 2, 2), PartPose.ZERO);
				
				PartDefinition leg1_Definition = origin.addOrReplaceChild("leg1", empty(), PartPose.offsetAndRotation(56 - 13.6F, 8F, 36F, (float) Math.toRadians(-10), (float) Math.toRadians(-20), (float) Math.toRadians(-15)));
				leg1_Definition.addOrReplaceChild("leg2", singleCube(1F, -1F, 3F, 38, 2, 2), PartPose.rotation(0, 0, (float) Math.toRadians(90)));
				
				origin.addOrReplaceChild("p8", singleCube(0, 0, 0, 4, 2, 2), PartPose.offsetAndRotation(53F, 43.3F, 46 - 14.3F, 0, (float) Math.toRadians(-30), 0));
				origin.addOrReplaceChild("p9", singleCube(0, -2, 0, 6, 2, 2), PartPose.offsetAndRotation(55f, 43.3f, 33f, 0, 0, (float) Math.toRadians(90)));
			}
			
			this.origin = LayerDefinition.create(meshDefinition, 16, 16).bakeRoot().getChild("origin");
		}
		
		@Override
		public void render(@Nonnull PoseStack matrixStackIn, @Nonnull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, int color){
			this.origin.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, color);
		}
	}
}
