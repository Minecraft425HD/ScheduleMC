package de.rolandsw.schedulemc.vehicle.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class SedanModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("schedulemc", "sedan"), "main");
	private final ModelPart body;
	private final ModelPart bottom;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart front;
	private final ModelPart engine_plane;
	private final ModelPart front_plane;
	private final ModelPart back;
	private final ModelPart back_plane;
	private final ModelPart storage_plane;
	private final ModelPart upper;
	private final ModelPart wing_front_left;
	private final ModelPart wing_front_right;
	private final ModelPart wing_back_left;
	private final ModelPart wing_back_right;
	private final ModelPart axe_front;
	private final ModelPart wheel_left;
	private final ModelPart wheel_right;
	private final ModelPart axe_back;
	private final ModelPart wheel_left2;
	private final ModelPart wheel_right2;

	public SedanModel(ModelPart root) {
		this.body = root.getChild("body");
		this.bottom = this.body.getChild("bottom");
		this.left = this.body.getChild("left");
		this.right = this.body.getChild("right");
		this.front = this.body.getChild("front");
		this.engine_plane = this.front.getChild("engine_plane");
		this.front_plane = this.front.getChild("front_plane");
		this.back = this.body.getChild("back");
		this.back_plane = this.back.getChild("back_plane");
		this.storage_plane = this.back.getChild("storage_plane");
		this.upper = this.body.getChild("upper");
		this.wing_front_left = this.body.getChild("wing_front_left");
		this.wing_front_right = this.body.getChild("wing_front_right");
		this.wing_back_left = this.body.getChild("wing_back_left");
		this.wing_back_right = this.body.getChild("wing_back_right");
		this.axe_front = root.getChild("axe_front");
		this.wheel_left = this.axe_front.getChild("wheel_left");
		this.wheel_right = this.axe_front.getChild("wheel_right");
		this.axe_back = root.getChild("axe_back");
		this.wheel_left2 = this.axe_back.getChild("wheel_left2");
		this.wheel_right2 = this.axe_back.getChild("wheel_right2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bottom = body.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-10.0F, -5.0F, -21.0F, 20.0F, 1.0F, 41.0F, new CubeDeformation(0.0F))
		.texOffs(0, 42).addBox(-10.0F, -4.0F, -12.0F, 20.0F, 1.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left = body.addOrReplaceChild("left", CubeListBuilder.create().texOffs(84, 152).addBox(-11.0F, -12.0F, -21.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 113).addBox(-11.0F, -5.0F, -10.0F, 1.0F, 1.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = body.addOrReplaceChild("right", CubeListBuilder.create().texOffs(14, 149).addBox(9.0F, -12.0F, -21.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 157).addBox(9.0F, -12.0F, -19.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 159).addBox(9.0F, -12.0F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(114, 153).addBox(9.0F, -12.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(140, 159).addBox(9.0F, -6.0F, -19.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(74, 161).addBox(9.0F, -7.0F, -18.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(138, 127).addBox(9.0F, -7.0F, -16.0F, 1.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(136, 59).addBox(9.0F, -8.0F, -17.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(64, 140).addBox(9.0F, -12.0F, -14.0F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(152, 72).addBox(9.0F, -12.0F, -17.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 132).addBox(9.0F, -12.0F, -3.0F, 1.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(162, 66).addBox(9.0F, -6.0F, 7.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(76, 153).addBox(9.0F, -12.0F, 7.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(10, 159).addBox(9.0F, -12.0F, 9.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(104, 144).addBox(9.0F, -7.0F, 9.0F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(134, 146).addBox(9.0F, -6.0F, 16.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(150, 19).addBox(9.0F, -6.0F, 17.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(136, 72).addBox(9.0F, -7.0F, 16.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(134, 159).addBox(9.0F, -12.0F, 9.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 150).addBox(9.0F, -8.0F, 10.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(144, 103).addBox(9.0F, -8.0F, 15.0F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(122, 19).addBox(9.0F, -12.0F, 10.0F, 2.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(122, 0).addBox(10.0F, -5.0F, -10.0F, 1.0F, 1.0F, 18.0F, new CubeDeformation(0.0F))
		.texOffs(60, 151).addBox(9.0F, -6.0F, 8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 68).addBox(-11.0F, -6.0F, 7.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 9).addBox(-11.0F, -7.0F, 7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(154, 159).addBox(-10.0F, -7.0F, 9.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(150, 25).addBox(-10.0F, -8.0F, 10.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(38, 141).addBox(-10.0F, -6.0F, 8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(156, 51).addBox(-11.0F, -8.0F, 7.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(84, 140).addBox(-11.0F, -12.0F, 7.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(42, 156).addBox(-10.0F, -12.0F, 15.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(38, 160).addBox(-10.0F, -6.0F, 16.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(22, 144).addBox(-11.0F, -12.0F, 17.0F, 2.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(68, 161).addBox(-10.0F, -6.0F, -19.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(98, 159).addBox(-10.0F, -7.0F, -18.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 127).addBox(-10.0F, -7.0F, -17.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(48, 151).addBox(-10.0F, -8.0F, -17.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(148, 159).addBox(-10.0F, -12.0F, -14.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 132).addBox(-10.0F, -12.0F, -12.0F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(116, 127).addBox(-10.0F, -12.0F, -3.0F, 1.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(154, 42).addBox(-10.0F, -12.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(156, 102).addBox(-11.0F, -6.0F, -19.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(48, 150).addBox(-11.0F, -6.0F, -19.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 157).addBox(-11.0F, -12.0F, -19.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(92, 159).addBox(-11.0F, -12.0F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 18).addBox(-11.0F, -8.0F, -17.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(156, 99).addBox(-11.0F, -8.0F, -17.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(92, 152).addBox(-11.0F, -12.0F, -17.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(14, 158).addBox(-11.0F, -12.0F, 15.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition front = body.addOrReplaceChild("front", CubeListBuilder.create().texOffs(122, 51).addBox(-8.0F, -12.0F, 23.0F, 16.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(126, 99).addBox(-7.0F, -5.0F, 22.0F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 63).addBox(-10.0F, -5.0F, 20.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 40).addBox(-9.0F, -5.0F, 21.0F, 18.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 153).addBox(-10.0F, -12.0F, 22.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 153).addBox(7.0F, -12.0F, 22.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition engine_plane = front.addOrReplaceChild("engine_plane", CubeListBuilder.create().texOffs(90, 127).addBox(-10.0F, -15.0F, 8.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(48, 89).addBox(-9.0F, -15.0F, 9.0F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(78, 65).addBox(-8.0F, -15.0F, 9.0F, 16.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(126, 79).addBox(8.0F, -15.0F, 9.0F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(64, 127).addBox(9.0F, -15.0F, 8.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(126, 101).addBox(-7.0F, -15.0F, 22.0F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition front_plane = front.addOrReplaceChild("front_plane", CubeListBuilder.create().texOffs(126, 97).addBox(-8.0F, -13.0F, 8.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(130, 154).addBox(8.0F, -20.0F, 8.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(156, 140).addBox(-9.0F, -20.0F, 8.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(126, 95).addBox(-8.0F, -21.0F, 8.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(134, 148).addBox(-9.0F, -18.0F, 8.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(156, 58).addBox(8.0F, -18.0F, 8.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back = body.addOrReplaceChild("back", CubeListBuilder.create().texOffs(84, 51).addBox(-9.0F, -12.0F, -24.0F, 18.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(122, 35).addBox(-9.0F, -5.0F, -23.0F, 18.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(38, 113).addBox(9.0F, -12.0F, -24.0F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(56, 141).addBox(-10.0F, -12.0F, -24.0F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back_plane = back.addOrReplaceChild("back_plane", CubeListBuilder.create().texOffs(122, 38).addBox(-9.0F, -13.0F, -16.0F, 18.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(120, 158).addBox(8.0F, -20.0F, -16.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(124, 158).addBox(-9.0F, -20.0F, -16.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(126, 93).addBox(-8.0F, -21.0F, -16.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition storage_plane = back.addOrReplaceChild("storage_plane", CubeListBuilder.create().texOffs(84, 42).addBox(-9.0F, -13.0F, -24.0F, 18.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(136, 42).addBox(-10.0F, -13.0F, -23.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(138, 140).addBox(9.0F, -13.0F, -23.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition upper = body.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(0, 65).addBox(-8.0F, -22.0F, -15.0F, 16.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(78, 79).addBox(-9.0F, -21.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(0, 89).addBox(8.0F, -21.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(158, 111).addBox(-10.0F, -19.0F, -15.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(102, 153).addBox(-10.0F, -19.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(158, 155).addBox(-10.0F, -19.0F, 7.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(158, 119).addBox(9.0F, -19.0F, -15.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 103).addBox(9.0F, -20.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(96, 103).addBox(-10.0F, -20.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(108, 153).addBox(9.0F, -19.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 159).addBox(9.0F, -19.0F, 7.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wing_front_left = body.addOrReplaceChild("wing_front_left", CubeListBuilder.create().texOffs(148, 149).addBox(-13.0F, -9.0F, 11.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(162, 58).addBox(-13.0F, -8.0F, 10.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 36).addBox(-13.0F, -7.0F, 9.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 62).addBox(-13.0F, -8.0F, 16.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 44).addBox(-13.0F, -7.0F, 17.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));

		PartDefinition cube_r1 = wing_front_left.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(160, 6).addBox(0.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -5.0F, 20.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r2 = wing_front_left.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(160, 0).addBox(0.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -5.0F, 10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition wing_front_right = body.addOrReplaceChild("wing_front_right", CubeListBuilder.create().texOffs(162, 34).addBox(11.0F, -7.0F, 9.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 29).addBox(11.0F, -8.0F, 10.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(144, 121).addBox(11.0F, -9.0F, 11.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(162, 60).addBox(11.0F, -8.0F, 16.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 64).addBox(11.0F, -7.0F, 17.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));

		PartDefinition cube_r3 = wing_front_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(162, 74).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 42).addBox(-3.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, 20.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r4 = wing_front_right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(160, 3).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, 10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition wing_back_left = body.addOrReplaceChild("wing_back_left", CubeListBuilder.create().texOffs(160, 38).addBox(-13.0F, -7.0F, -12.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 32).addBox(-13.0F, -8.0F, -13.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(154, 87).addBox(-13.0F, -9.0F, -18.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(150, 32).addBox(-13.0F, -9.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 27).addBox(-13.0F, -8.0F, -19.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(86, 161).addBox(-13.0F, -7.0F, -20.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(156, 55).addBox(10.0F, -9.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition cube_r5 = wing_back_left.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(162, 72).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 161).addBox(-2.0F, -2.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 97).addBox(-25.0F, -2.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(162, 70).addBox(-25.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.0F, -5.0F, -9.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r6 = wing_back_left.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(162, 76).addBox(0.0F, -2.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(154, 91).addBox(0.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.0F, -5.0F, -19.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition wing_back_right = body.addOrReplaceChild("wing_back_right", CubeListBuilder.create().texOffs(162, 25).addBox(11.0F, -7.0F, -20.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 161).addBox(11.0F, -8.0F, -19.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(154, 83).addBox(11.0F, -9.0F, -18.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(160, 16).addBox(10.0F, -8.0F, -13.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 40).addBox(10.0F, -7.0F, -12.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition cube_r7 = wing_back_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(134, 144).addBox(-2.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, -9.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r8 = wing_back_right.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(162, 91).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(126, 63).addBox(-3.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, -19.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition axe_front = partdefinition.addOrReplaceChild("axe_front", CubeListBuilder.create().texOffs(84, 61).addBox(-10.0F, -4.0F, 13.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, -1.0F));

		PartDefinition wheel_left = axe_front.addOrReplaceChild("wheel_left", CubeListBuilder.create().texOffs(160, 140).addBox(-12.0F, -5.0F, 10.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(32, 156).addBox(-12.0F, -1.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(134, 149).addBox(-12.0F, -6.0F, 11.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(22, 156).addBox(-12.0F, -7.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(160, 144).addBox(-12.0F, -5.0F, 16.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wheel_right = axe_front.addOrReplaceChild("wheel_right", CubeListBuilder.create().texOffs(160, 93).addBox(10.0F, -5.0F, 10.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 46).addBox(10.0F, -5.0F, 16.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(148, 155).addBox(10.0F, -7.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(120, 154).addBox(10.0F, -1.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 149).addBox(10.0F, -6.0F, 11.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition axe_back = partdefinition.addOrReplaceChild("axe_back", CubeListBuilder.create().texOffs(84, 59).addBox(2.0F, -2.0F, 2.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, 22.0F, -17.0F));

		PartDefinition wheel_left2 = axe_back.addOrReplaceChild("wheel_left2", CubeListBuilder.create().texOffs(32, 160).addBox(0.0F, -3.0F, 5.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 160).addBox(0.0F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(154, 79).addBox(0.0F, 1.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(126, 59).addBox(0.0F, -5.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(120, 144).addBox(0.0F, -4.0F, 0.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wheel_right2 = axe_back.addOrReplaceChild("wheel_right2", CubeListBuilder.create().texOffs(26, 160).addBox(-2.0F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(160, 12).addBox(-2.0F, -3.0F, 5.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 123).addBox(-2.0F, 1.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(104, 140).addBox(-2.0F, -5.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(144, 111).addBox(-2.0F, -4.0F, 0.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(24.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		axe_front.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		axe_back.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
