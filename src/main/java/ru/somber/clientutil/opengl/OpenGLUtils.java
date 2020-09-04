package ru.somber.clientutil.opengl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.somber.clientutil.opengl.texture.Texture;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Scanner;

@SideOnly(Side.CLIENT)
public final class OpenGLUtils {
    private OpenGLUtils() {}

    public static String loadShaderCode(ResourceLocation rs) throws IOException {
        try (Scanner reader = new Scanner(Minecraft.getMinecraft().getResourceManager().getResource(rs).getInputStream())) {
            StringBuilder sb = new StringBuilder();
            while (reader.hasNextLine()) {
                sb.append(reader.nextLine()).append("\n");
            }
            return sb.toString();
        }
    }


    public static void drawColorOverFramebuffer(float r, float g, float b, float a) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glPushMatrix();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(r, g, b, a);
        tessellator.addVertex(-1, -1, 0.2F);
        tessellator.addVertex(1, -1, 0.2F);
        tessellator.addVertex(1, 1, 0.2F);
        tessellator.addVertex(-1, 1, 0.2F);
        tessellator.draw();

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
    }

    public static void drawTextureOverFramebuffer(int textureID) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        GL11.glPushAttrib(GL11.GL_LIGHTING_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glPushMatrix();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(1, 1, 1, 1);
        tessellator.addVertexWithUV(-1, -1, 0.2F, 0, 0);
        tessellator.addVertexWithUV(1, -1, 0.2F, 1, 0);
        tessellator.addVertexWithUV(1, 1, 0.2F, 1, 1);
        tessellator.addVertexWithUV(-1, 1, 0.2F, 0, 1);
        tessellator.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL11.glPopMatrix();

        GL11.glPopAttrib();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    public static void drawColorInTextureAttachSlotFramebuffer(CustomFramebuffer customFramebuffer, int attachSlot, float r, float g, float b, float a) {
        customFramebuffer.drawColorInTextureAttachSlot(attachSlot, r, g, b, a);
    }

    public static void drawTextureInTextureAttachSlotFramebuffer(CustomFramebuffer customFramebuffer, int attachSlot, Texture texture) {
        customFramebuffer.drawTextureInTextureAttachSlot(attachSlot, texture);
    }

    /**
     * 00 01
     * 10 11
     */
    public static float getElementFromMatrix22(FloatBuffer fb, int row, int column) {
        return fb.get(column * 2 + row);
    }

    /**
     * 00 01 12
     * 10 11 12
     * 20 21 22
     */
    public static float getElementFromMatrix33(FloatBuffer fb, int row, int column) {
        return fb.get(column * 3 + row);
    }

    /**
     * 00 01 02 03
     * 10 11 12 13
     * 20 21 22 23
     * 30 31 32 33
     */
    public static float getElementFromMatrix44(FloatBuffer fb, int row, int column) {
        return fb.get(column * 4 + row);
    }

    public static void printMatrix44(FloatBuffer fb) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                System.out.print(fb.get(x * 4 + y) + " ");
            }
            System.out.println();
        }
    }

}
