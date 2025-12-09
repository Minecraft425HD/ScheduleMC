package de.rolandsw.schedulemc.vehicle.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple OBJ model loader and renderer for vehicles
 */
public class OBJVehicleModel extends Model {

    private final List<Vector3f> vertices = new ArrayList<>();
    private final List<Vector3f> texCoords = new ArrayList<>();
    private final List<Vector3f> normals = new ArrayList<>();
    private final List<Face> faces = new ArrayList<>();

    // Texture dimensions
    private int textureWidth = 64;
    private int textureHeight = 64;

    public OBJVehicleModel(ResourceLocation objLocation) {
        super(RenderType::entityCutoutNoCull);
        loadOBJ(objLocation);
    }

    public OBJVehicleModel(ResourceLocation objLocation, int textureWidth, int textureHeight) {
        super(RenderType::entityCutoutNoCull);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        loadOBJ(objLocation);
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    private void loadOBJ(ResourceLocation location) {
        try {
            String path = "/assets/" + location.getNamespace() + "/" + location.getPath();
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Could not find OBJ file: " + path);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    // Vertex position
                    String[] parts = line.substring(2).trim().split("\\s+");
                    vertices.add(new Vector3f(
                        Float.parseFloat(parts[0]),
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2])
                    ));
                } else if (line.startsWith("vt ")) {
                    // Texture coordinate
                    String[] parts = line.substring(3).trim().split("\\s+");
                    texCoords.add(new Vector3f(
                        Float.parseFloat(parts[0]),
                        parts.length > 1 ? Float.parseFloat(parts[1]) : 0,
                        0
                    ));
                } else if (line.startsWith("vn ")) {
                    // Normal
                    String[] parts = line.substring(3).trim().split("\\s+");
                    normals.add(new Vector3f(
                        Float.parseFloat(parts[0]),
                        Float.parseFloat(parts[1]),
                        Float.parseFloat(parts[2])
                    ));
                } else if (line.startsWith("f ")) {
                    // Face
                    String[] parts = line.substring(2).trim().split("\\s+");
                    faces.add(new Face(parts));
                }
            }
            reader.close();

            System.out.println("Loaded OBJ: " + vertices.size() + " vertices, " + faces.size() + " faces");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                               int packedOverlay, float red, float green, float blue, float alpha) {

        PoseStack.Pose pose = poseStack.last();

        for (Face face : faces) {
            FaceVertex[] fv = face.indices;

            // Render each face as-is from Blockbench OBJ export
            if (fv.length >= 3) {
                // For triangles and quads, render directly without additional triangulation
                // OpenGL/Minecraft will handle the primitive assembly
                renderVertex(pose, buffer, fv[0], packedLight, packedOverlay, red, green, blue, alpha);
                renderVertex(pose, buffer, fv[1], packedLight, packedOverlay, red, green, blue, alpha);
                renderVertex(pose, buffer, fv[2], packedLight, packedOverlay, red, green, blue, alpha);

                if (fv.length == 4) {
                    // For quads, add second triangle [0,2,3]
                    renderVertex(pose, buffer, fv[0], packedLight, packedOverlay, red, green, blue, alpha);
                    renderVertex(pose, buffer, fv[2], packedLight, packedOverlay, red, green, blue, alpha);
                    renderVertex(pose, buffer, fv[3], packedLight, packedOverlay, red, green, blue, alpha);
                }
            }
        }
    }

    private void renderVertex(PoseStack.Pose pose, VertexConsumer buffer, FaceVertex fv,
                             int packedLight, int packedOverlay,
                             float red, float green, float blue, float alpha) {
        // Get vertex position
        Vector3f pos = vertices.get(fv.vertexIndex - 1);

        // Get texture coordinates (OBJ uses 1-based indexing)
        float u = 0.0f;
        float v = 0.0f;
        if (fv.texIndex > 0 && fv.texIndex <= texCoords.size()) {
            Vector3f tex = texCoords.get(fv.texIndex - 1);
            u = tex.x;
            v = 1.0f - tex.y;  // Flip V: OBJ uses bottom-left origin, OpenGL uses top-left
        }

        // Get normal
        float nx = 0.0f;
        float ny = 1.0f;
        float nz = 0.0f;
        if (fv.normalIndex > 0 && fv.normalIndex <= normals.size()) {
            Vector3f normal = normals.get(fv.normalIndex - 1);
            nx = normal.x;
            ny = normal.y;
            nz = normal.z;
        }

        // Add vertex to buffer
        buffer.vertex(pose.pose(), pos.x, pos.y, pos.z)
            .color(red, green, blue, alpha)
            .uv(u, v)
            .overlayCoords(packedOverlay)
            .uv2(packedLight)
            .normal(pose.normal(), nx, ny, nz)
            .endVertex();
    }

    private static class Face {
        FaceVertex[] indices;

        Face(String[] parts) {
            indices = new FaceVertex[parts.length];
            for (int i = 0; i < parts.length; i++) {
                indices[i] = new FaceVertex(parts[i]);
            }
        }
    }

    private static class FaceVertex {
        int vertexIndex;
        int texIndex;
        int normalIndex;

        FaceVertex(String vertexData) {
            String[] parts = vertexData.split("/");
            vertexIndex = Integer.parseInt(parts[0]);
            texIndex = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : 0;
            normalIndex = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : 0;
        }
    }
}
