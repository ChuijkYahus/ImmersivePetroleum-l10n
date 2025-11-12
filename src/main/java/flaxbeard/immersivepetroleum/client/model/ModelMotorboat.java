package flaxbeard.immersivepetroleum.client.model;

import com.google.common.collect.ImmutableList;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ModelMotorboat extends ListModel<MotorboatEntity>{
	private final List<ModelPart> list;
	
	/**
	 * Part of the model rendered to make it seem like there's no water in the boat
	 */
	public ModelPart noWater;
	
	public ModelPart[] boatSides = new ModelPart[5];
	public ModelPart motor;
	public ModelPart propeller;
	public ModelPart propellerAssembly;
	
	public ModelPart icebreak;
	public ModelPart coreSampleBoat;
	public ModelPart coreSampleBoatDrill;
	public ModelPart tank;
	public ModelPart rudder1;
	public ModelPart rudder2;
	public ModelPart ruddersBase;
	public ModelPart[] paddles = new ModelPart[2];
	
	@SuppressWarnings("unused")
	public ModelMotorboat(){
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition rootDefinition = meshDefinition.getRoot();
		
		rootDefinition.addOrReplaceChild("boat_side0", singleCube(0, 0, -14.0F, -9.0F, -3.0F, 28, 16, 3), PartPose.offsetAndRotation(0.0F, 3.0F, 1.0F, Mth.HALF_PI, 0.0F, 0.0F));
		rootDefinition.addOrReplaceChild("boat_side1", singleCube(0, 19, -13.0F, -7.0F, -1.0F, 18, 6, 2), PartPose.offsetAndRotation(-15.0F, 4.0F, 4.0F, 0.0F, (Mth.PI * 3F / 2F), 0.0F));
		rootDefinition.addOrReplaceChild("boat_side2", singleCube(0, 27, -8.0F, -7.0F, -1.0F, 16, 6, 2), PartPose.offsetAndRotation(15.0F, 4.0F, 0.0F, 0.0F, Mth.HALF_PI, 0.0F));
		rootDefinition.addOrReplaceChild("boat_side3", singleCube(0, 35, -14.0F, -7.0F, -1.0F, 28, 6, 2), PartPose.offsetAndRotation(0.0F, 4.0F, -9.0F, 0.0F, Mth.PI, 0.0F));
		rootDefinition.addOrReplaceChild("boat_side4", singleCube(0, 43, -14.0F, -7.0F, -1.0F, 28, 6, 2), PartPose.offset(0.0F, 4.0F, 9.0F));
		
		rootDefinition.addOrReplaceChild("no_water", singleCube(0, 0, -14.0F, -9.0F, -3.0F, 28, 16, 3), PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, Mth.HALF_PI, 0.0F, 0.0F));
		
		rootDefinition.addOrReplaceChild("motor", singleCube(104, 0, -19.0F, -8.0F, -3, 6, 5, 6), PartPose.ZERO);
		
		PartDefinition propAssembly_Definition = rootDefinition.addOrReplaceChild("propeller_assembly", singleCube(96, 0, -1, -8.1F, -1, 2, 10, 2), PartPose.offset(-17F, 5F, 0));
		
		propAssembly_Definition.addOrReplaceChild("handle", singleCube(72, 0, 4F, -9.7F, -0.5F, 6, 1, 1), PartPose.offsetAndRotation(0, 0, 0, 0.0F, 0.0F, toRadians(-5)));
		PartDefinition propeller_Definition = propAssembly_Definition.addOrReplaceChild("propeller", singleCube(86, 0, -1F, -1F, -1F, 3, 2, 2), PartPose.offset(-3F, 0, 0));
		propeller_Definition.addOrReplaceChild("propeller1", singleCube(90, 4, 0F, 0F, -1F, 1, 4, 2), PartPose.offsetAndRotation(0, 0, 0, 0.0F, toRadians(15), 0.0F));
		PartDefinition propeller2B_Definition = propeller_Definition.addOrReplaceChild("propeller2B", empty(90, 4), PartPose.offsetAndRotation(0, 0, 0, toRadians(360F / 3F), 0.0F, 0.0F))
				.addOrReplaceChild("propeller2", singleCube(90, 4, 0F, 0F, -1F, 1, 4, 2), PartPose.offsetAndRotation(0, 0, 0, 0.0F, toRadians(15), 0.0F));
		PartDefinition propeller3B_Definition = propeller_Definition.addOrReplaceChild("propeller3B", empty(90, 4), PartPose.offsetAndRotation(0, 0, 0, toRadians(2 * 360F / 3F), 0.0F, 0.0F))
				.addOrReplaceChild("propeller3", singleCube(90, 4, 0F, 0F, -1F, 1, 4, 2), PartPose.offsetAndRotation(0, 0, 0, 0.0F, toRadians(15), 0.0F));
		
		PartDefinition icebreak_Definition = rootDefinition.addOrReplaceChild("icebreak", singleCube(34, 56, 16, -2, -2, 7, 4, 4), PartPose.ZERO);
		PartDefinition icebreak_iS1_Definition = icebreak_Definition.addOrReplaceChild("icebreak_iS1", singleCube(56, 52, 0.01f, -7.01F, -0.01F, 16, 10, 2), PartPose.offsetAndRotation(26.0F, 3.0F, 0.0F, 0.0F, toRadians(180 + 45), 0.0F));
		PartDefinition icebreak_iS1T_Definition = icebreak_iS1_Definition.addOrReplaceChild("icebreak_iS1T", singleCube(100, 45, 4, 0, -2F, 12, 5, 2), PartPose.offsetAndRotation(0F, -7F, 0F, toRadians(180 - 23), 0.0F, 0.0F));
		PartDefinition icebreak_iS2_Definition = icebreak_Definition.addOrReplaceChild("icebreak_iS2", singleCube(56, 52, 0, -7.0F, -2F, 16, 10, 2), PartPose.offsetAndRotation(26.0F, 3.0F, 0.0F, 0.0F, toRadians(180 - 45), 0.0F));
		PartDefinition icebreak_iS2T_Definition = icebreak_iS2_Definition.addOrReplaceChild("icebreak_iS2T", singleCube(100, 45, 4, 0, 0F, 12, 5, 2), PartPose.offsetAndRotation(0F, -7F, 0F, toRadians(180 + 23), 0.0F, 0.0F));
		
		PartDefinition tank_Definition = rootDefinition.addOrReplaceChild("tank", singleCube(86, 24, -14, -2, -8, 5, 5, 16), PartPose.ZERO);
		PartDefinition pipe1_Definition = tank_Definition.addOrReplaceChild("pipe1", singleCube(112, 38, -13, -3, 4, 1, 1, 1), PartPose.ZERO);
		PartDefinition pipe2_Definition = tank_Definition.addOrReplaceChild("pipe2", singleCube(116, 38, -15, -4, 4, 3, 1, 1), PartPose.ZERO);
		PartDefinition pipe3_Definition = tank_Definition.addOrReplaceChild("pipe3", singleCube(112, 38, -15, -4, 3, 1, 1, 1), PartPose.ZERO);
		
		PartDefinition ruddersBase_Definition = rootDefinition.addOrReplaceChild("ruddersBase", singleCube(92, 29, -18, -3, -8, 2, 6, 3), PartPose.ZERO);
		PartDefinition ruddersBase2_Definition = ruddersBase_Definition.addOrReplaceChild("ruddersBase2", singleCube(92, 29, -18, -3, 6, 2, 6, 3), PartPose.ZERO);
		
		PartDefinition rudder1_Definition = rootDefinition.addOrReplaceChild("rudder1", singleCube(112, 23, -4, 0, -.5F, 4, 6, 1), PartPose.offset(-15, 3, -6.5F));
		PartDefinition rudder2_Definition = rootDefinition.addOrReplaceChild("rudder2", singleCube(112, 23, -4, 0, -.5F, 4, 6, 1), PartPose.offset(-15, 3, 7.5F));
		
		PartDefinition coreSampleBoat_Definition = rootDefinition.addOrReplaceChild("coreSampleBoat", singleCube(10, 0, -10, -1, -13, 4, 2, 2), PartPose.ZERO);
		PartDefinition core2_Definition = coreSampleBoat_Definition.addOrReplaceChild("core2", singleCube(10, 0, -11, -2, -14, 1, 4, 4), PartPose.ZERO);
		PartDefinition core3_Definition = coreSampleBoat_Definition.addOrReplaceChild("core3", singleCube(10, 0, -6, -2, -14, 1, 4, 4), PartPose.ZERO);
		
		PartDefinition coreSampleBoatDrill_Definition = rootDefinition.addOrReplaceChild("coreSampleBoatDrill", singleCube(10, 0, -3, -8, -16, 6, 18, 6), PartPose.ZERO);
		
		PartDefinition paddle0_Definition = rootDefinition.addOrReplaceChild("paddle_left", makePaddle(rootDefinition, true), PartPose.offsetAndRotation(3.0F, -5.0F, 9.0F, 0.0F, 0.0F, 0.19634955F));
		PartDefinition paddle1_Definition = rootDefinition.addOrReplaceChild("paddle_right", makePaddle(rootDefinition, false), PartPose.offsetAndRotation(3.0F, -5.0F, -9.0F, 0.0F, Mth.PI, 0.19634955F));
		
		LayerDefinition layerDefinition = LayerDefinition.create(meshDefinition, 128, 64);
		ModelPart root = layerDefinition.bakeRoot();
		
		// ------------------------------------------------------------------------------------------------------------------------
		
		this.boatSides[0] = root.getChild("boat_side0");
		this.boatSides[1] = root.getChild("boat_side1");
		this.boatSides[2] = root.getChild("boat_side2");
		this.boatSides[3] = root.getChild("boat_side3");
		this.boatSides[4] = root.getChild("boat_side4");
		
		this.noWater = root.getChild("no_water");
		
		this.motor = root.getChild("motor");
		this.propellerAssembly = root.getChild("propeller_assembly");
		this.propeller = this.propellerAssembly.getChild("propeller");
		
		this.icebreak = root.getChild("icebreak");
		this.tank = root.getChild("tank");
		
		this.ruddersBase = root.getChild("ruddersBase");
		this.paddles[0] = root.getChild("paddle_left");
		this.paddles[1] = root.getChild("paddle_right");
		
		this.rudder1 = root.getChild("rudder1");
		this.rudder2 = root.getChild("rudder2");
		
		this.coreSampleBoat = root.getChild("coreSampleBoat");
		this.coreSampleBoatDrill = root.getChild("coreSampleBoatDrill");
		
		// ------------------------------------------------------------------------------------------------------------------------
		
		ImmutableList.Builder<ModelPart> builder = ImmutableList.builder();
		builder.addAll(Arrays.asList(this.boatSides));
		builder.addAll(Arrays.asList(this.motor, this.propellerAssembly));
		this.list = builder.build();
	}
	
	private CubeListBuilder makePaddle(PartDefinition rootDefinition, boolean left){
		CubeListBuilder builder = CubeListBuilder.create()
			.texOffs(62, left ? 2 : 22)
			.addBox(-1.0F, 0.0F, -5.0F, 2, 2, 18)
			.addBox(left ? -1.001F : 0.001F, -3.0F, 8.0F, 1, 6, 7);
		return builder;
	}
	
	@Override
	public void setupAnim(@Nonnull MotorboatEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		this.setPaddleRotationAngles(entityIn, 0, limbSwing, entityIn.isEmergency());
		this.setPaddleRotationAngles(entityIn, 1, limbSwing, entityIn.isEmergency());
	}
	
	public void setPaddleRotationAngles(Boat boat, int paddle, float limbSwing, boolean isEmergency){
		if(isEmergency){
			float f = boat.getRowingTime(paddle, limbSwing);
			ModelPart model = this.paddles[paddle];
			model.xRot = (float) Mth.clampedLerp(-(float) Math.PI / 3F, -0.2617994F, (double) ((Mth.sin(-f) + 1.0F) / 2.0F));
			model.yRot = (float) Mth.clampedLerp(-(float) Math.PI / 4F, (float) Math.PI / 4F, (double) ((Mth.sin(-f + 1.0F) + 1.0F) / 2.0F));
			
			model.setPos(3.0F, -5.0F, 9.0F);
			
			if(paddle == 1){
				model.setPos(3.0F, -5.0F, -9.0F);
				model.yRot = (float) Math.PI - model.yRot;
			}
		}else{
			ModelPart model = this.paddles[paddle];
			model.xRot = (float) Math.toRadians(-25);
			model.yRot = (float) Math.toRadians(-90);
			
			model.setPos(3.0F, -2.0F, 11.0F);
			
			if(paddle == 1){
				model.setPos(3.0F, -2.0F, -11.0F);
				model.yRot = (float) Math.PI - model.yRot;
			}
		}
	}
	
	/**
	 * Only contains the base shape
	 */
	@Override
	@Nonnull
	public Iterable<ModelPart> parts(){
		return this.list;
	}
	
	public ModelPart noWaterRenderer(){
		return this.noWater;
	}
	
	/** Creates a single cube */
	protected final CubeListBuilder singleCube(float pOriginX, float pOriginY, float pOriginZ, float pDimensionX, float pDimensionY, float pDimensionZ){
		return singleCube(0, 0, pOriginX, pOriginY, pOriginZ, pDimensionX, pDimensionY, pDimensionZ);
	}
	
	/** Creates a single cube with texture offset */
	protected final CubeListBuilder singleCube(int pXTexOffs, int pYTexOffs, float pOriginX, float pOriginY, float pOriginZ, float pDimensionX, float pDimensionY, float pDimensionZ){
		return CubeListBuilder.create().texOffs(pXTexOffs, pYTexOffs).addBox(pOriginX, pOriginY, pOriginZ, pDimensionX, pDimensionY, pDimensionZ);
	}
	
	/**
	 * Creates an empty CubeListBuilder with Texture Offset.<br>
	 * Same as doing <code>CubeListBuilder.create().texOffs(x, y);</code>
	 */
	protected final CubeListBuilder empty(int pXTexOffs, int pYTexOffs){
		return empty().texOffs(pXTexOffs, pYTexOffs);
	}
	
	/**
	 * Creates an empty CubeListBuilder.<br>
	 * Same as doing <code>CubeListBuilder.create();</code>
	 */
	protected final CubeListBuilder empty(){
		return CubeListBuilder.create();
	}
	
	private float toRadians(float degrees){
		return (float) Math.toRadians(degrees);
	}
}
