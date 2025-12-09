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

/**
 * Sedan vehicle model created with Blockbench 5.0.4.
 * Exported for Minecraft version 1.17 or later with Mojang mappings.
 */
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

		PartDefinition bottom = body.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(-58, -39).addBox(-10.0F, -5.0F, -21.0F, 20.0F, 1.0F, 41.0F, new CubeDeformation(0.0F))
		.texOffs(-39, -20).addBox(-10.0F, -4.0F, -12.0F, 20.0F, 1.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left = body.addOrReplaceChild("left", CubeListBuilder.create().texOffs(-39, -40).addBox(-11.0F, -12.0F, -21.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(-15, -16).addBox(-11.0F, -5.0F, -10.0F, 1.0F, 1.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right = body.addOrReplaceChild("right", CubeListBuilder.create().texOffs(0, -1).mirror().addBox(9.0F, -12.0F, -21.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).mirror().addBox(9.0F, -12.0F, -19.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).mirror().addBox(9.0F, -12.0F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).mirror().addBox(9.0F, -12.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-12, -13).mirror().addBox(9.0F, -6.0F, -19.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-10, -11).mirror().addBox(9.0F, -7.0F, -18.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-10, -11).mirror().addBox(9.0F, -7.0F, -16.0F, 1.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-10, -11).mirror().addBox(9.0F, -8.0F, -17.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -12.0F, -14.0F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).mirror().addBox(9.0F, -12.0F, -17.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(9.0F, -12.0F, -3.0F, 1.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -6.0F, 7.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -12.0F, 7.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -12.0F, 9.0F, 2.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -7.0F, 9.0F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -6.0F, 16.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -6.0F, 17.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -7.0F, 16.0F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -12.0F, 9.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -8.0F, 10.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -8.0F, 15.0F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -12.0F, 10.0F, 2.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(10.0F, -5.0F, -10.0F, 1.0F, 1.0F, 18.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, -2).mirror().addBox(9.0F, -6.0F, 8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).addBox(-11.0F, -6.0F, 7.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -7.0F, 7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -7.0F, 9.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -8.0F, 10.0F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -6.0F, 8.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -8.0F, 7.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -12.0F, 7.0F, 2.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -12.0F, 15.0F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -6.0F, 16.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -12.0F, 17.0F, 2.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(-5, -6).addBox(-10.0F, -6.0F, -19.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -5).addBox(-10.0F, -7.0F, -18.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-5, -6).addBox(-10.0F, -7.0F, -17.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(-3, -4).addBox(-10.0F, -8.0F, -17.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -12.0F, -14.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(-1, -2).addBox(-10.0F, -12.0F, -12.0F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(1, 0).addBox(-10.0F, -12.0F, -3.0F, 1.0F, 7.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-10.0F, -12.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -6.0F, -19.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -6.0F, -19.0F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -12.0F, -19.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -12.0F, -18.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -8.0F, -17.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -8.0F, -17.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(-11.0F, -12.0F, -17.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(1, 0).addBox(-11.0F, -12.0F, 15.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition front = body.addOrReplaceChild("front", CubeListBuilder.create().texOffs(-16, 1).addBox(-8.0F, -12.0F, 23.0F, 16.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-5, 1).addBox(-7.0F, -5.0F, 22.0F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-9, 0).addBox(-10.0F, -5.0F, 20.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 0).addBox(-9.0F, -5.0F, 21.0F, 18.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(-10.0F, -12.0F, 22.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).mirror().addBox(7.0F, -12.0F, 22.0F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition engine_plane = front.addOrReplaceChild("engine_plane", CubeListBuilder.create().texOffs(-23, -14).addBox(-10.0F, -15.0F, 8.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(-21, -12).addBox(-9.0F, -15.0F, 9.0F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(-20, -11).addBox(-8.0F, -15.0F, 9.0F, 16.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(-21, -12).addBox(8.0F, -15.0F, 9.0F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F))
		.texOffs(-22, -13).addBox(9.0F, -15.0F, 8.0F, 1.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(-5, 1).addBox(-7.0F, -15.0F, 22.0F, 14.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition front_plane = front.addOrReplaceChild("front_plane", CubeListBuilder.create().texOffs(-7, 1).addBox(-8.0F, -13.0F, 8.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 1).addBox(8.0F, -20.0F, 8.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 1).addBox(-9.0F, -20.0F, 8.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 1).addBox(-8.0F, -21.0F, 8.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(-9.0F, -18.0F, 8.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).mirror().addBox(8.0F, -18.0F, 8.0F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back = body.addOrReplaceChild("back", CubeListBuilder.create().texOffs(-19, 1).mirror().addBox(-9.0F, -12.0F, -24.0F, 18.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-8, 0).addBox(-9.0F, -5.0F, -23.0F, 18.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, -1).addBox(9.0F, -12.0F, -24.0F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).mirror().addBox(-10.0F, -12.0F, -24.0F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition back_plane = back.addOrReplaceChild("back_plane", CubeListBuilder.create().texOffs(-7, 1).addBox(-9.0F, -13.0F, -16.0F, 18.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 1).addBox(8.0F, -20.0F, -16.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 1).addBox(-9.0F, -20.0F, -16.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-8, 1).addBox(-8.0F, -21.0F, -16.0F, 16.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition storage_plane = back.addOrReplaceChild("storage_plane", CubeListBuilder.create().texOffs(-15, -6).addBox(-9.0F, -13.0F, -24.0F, 18.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(-15, -6).addBox(-10.0F, -13.0F, -23.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(-15, -6).mirror().addBox(9.0F, -13.0F, -23.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition upper = body.addOrReplaceChild("upper", CubeListBuilder.create().texOffs(-38, -21).mirror().addBox(-8.0F, -22.0F, -15.0F, 16.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-20, -21).addBox(-9.0F, -21.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(-20, -21).mirror().addBox(8.0F, -21.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-21, -21).addBox(-10.0F, -19.0F, -15.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-21, -21).addBox(-10.0F, -19.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(-21, -21).addBox(-10.0F, -19.0F, 7.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-21, -21).mirror().addBox(9.0F, -19.0F, -15.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-21, -21).mirror().addBox(9.0F, -20.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-21, -21).addBox(-10.0F, -20.0F, -15.0F, 1.0F, 1.0F, 23.0F, new CubeDeformation(0.0F))
		.texOffs(-21, -21).mirror().addBox(9.0F, -19.0F, -5.0F, 1.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-21, -21).mirror().addBox(9.0F, -19.0F, 7.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wing_front_left = body.addOrReplaceChild("wing_front_left", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-13.0F, -9.0F, 11.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 1).mirror().addBox(-13.0F, -8.0F, 10.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(2, 1).mirror().addBox(-13.0F, -7.0F, 9.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 1).mirror().addBox(-13.0F, -8.0F, 16.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 1).mirror().addBox(-13.0F, -7.0F, 17.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, -1.0F));

		PartDefinition cube_r1 = wing_front_left.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-13.0F, -5.0F, 20.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r2 = wing_front_left.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(0.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-13.0F, -5.0F, 10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition wing_front_right = body.addOrReplaceChild("wing_front_right", CubeListBuilder.create().texOffs(0, 1).addBox(11.0F, -7.0F, 9.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(11.0F, -8.0F, 10.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -3).addBox(11.0F, -9.0F, 11.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(11.0F, -8.0F, 16.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(11.0F, -7.0F, 17.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -1.0F));

		PartDefinition cube_r3 = wing_front_right.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-1, 0).addBox(-3.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, 20.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r4 = wing_front_right.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, -2.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, 10.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition wing_back_left = body.addOrReplaceChild("wing_back_left", CubeListBuilder.create().texOffs(0, 1).mirror().addBox(-13.0F, -7.0F, -12.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 1).mirror().addBox(-13.0F, -8.0F, -13.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(-13.0F, -9.0F, -18.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-13.0F, -9.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 1).mirror().addBox(-13.0F, -8.0F, -19.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(2, 1).mirror().addBox(-13.0F, -7.0F, -20.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).addBox(10.0F, -9.0F, -15.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition cube_r5 = wing_back_left.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(-1, 0).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-1, 0).addBox(-2.0F, -2.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-1, 0).mirror().addBox(-25.0F, -2.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(-1, 0).mirror().addBox(-25.0F, -2.0F, -1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(12.0F, -5.0F, -9.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r6 = wing_back_left.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(0.0F, -2.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(0.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-13.0F, -5.0F, -19.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition wing_back_right = body.addOrReplaceChild("wing_back_right", CubeListBuilder.create().texOffs(0, 1).addBox(11.0F, -7.0F, -20.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(11.0F, -8.0F, -19.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -3).addBox(11.0F, -9.0F, -18.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(10.0F, -8.0F, -13.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 1).addBox(10.0F, -7.0F, -12.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 1.0F));

		PartDefinition cube_r7 = wing_back_right.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, -9.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r8 = wing_back_right.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, -2.0F, 0.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-2, 0).addBox(-3.0F, -2.0F, -1.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(13.0F, -5.0F, -19.0F, 1.5708F, 0.0F, 0.0F));

		PartDefinition axe_front = partdefinition.addOrReplaceChild("axe_front", CubeListBuilder.create().texOffs(2, 1).addBox(-10.0F, -4.0F, 13.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, -1.0F));

		PartDefinition wheel_left = axe_front.addOrReplaceChild("wheel_left", CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-12.0F, -5.0F, 10.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).mirror().addBox(-12.0F, -1.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(-12.0F, -6.0F, 11.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(-12.0F, -7.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(2, 1).mirror().addBox(-12.0F, -5.0F, 16.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wheel_right = axe_front.addOrReplaceChild("wheel_right", CubeListBuilder.create().texOffs(0, 1).addBox(10.0F, -5.0F, 10.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(10.0F, -5.0F, 16.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-2, -1).addBox(10.0F, -7.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(-2, -1).addBox(10.0F, -1.0F, 12.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -3).addBox(10.0F, -6.0F, 11.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition axe_back = partdefinition.addOrReplaceChild("axe_back", CubeListBuilder.create().texOffs(2, 1).addBox(2.0F, -2.0F, 2.0F, 20.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.0F, 22.0F, -17.0F));

		PartDefinition wheel_left2 = axe_back.addOrReplaceChild("wheel_left2", CubeListBuilder.create().texOffs(2, 1).mirror().addBox(0.0F, -3.0F, 5.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(0.0F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, -1).mirror().addBox(0.0F, 1.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(0.0F, -5.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(1, 0).mirror().addBox(0.0F, -4.0F, 0.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition wheel_right2 = axe_back.addOrReplaceChild("wheel_right2", CubeListBuilder.create().texOffs(0, 1).addBox(-2.0F, -3.0F, -1.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 1).addBox(-2.0F, -3.0F, 5.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(-2, -1).addBox(-2.0F, 1.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(-2, -1).addBox(-2.0F, -5.0F, 1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(-4, -3).addBox(-2.0F, -4.0F, 0.0F, 2.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(24.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Animation logic can be added here (e.g., rotating wheels)
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		axe_front.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		axe_back.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	/**
	 * Get wheel parts for animation purposes.
	 */
	public ModelPart getWheelLeft() {
		return wheel_left;
	}

	public ModelPart getWheelRight() {
		return wheel_right;
	}

	public ModelPart getWheelLeft2() {
		return wheel_left2;
	}

	public ModelPart getWheelRight2() {
		return wheel_right2;
	}
}
