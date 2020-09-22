package ru.somber.clientutil.opengl.texture.atlas;

import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.IntBuffer;

/**
 * Утилитный класс для применения метода glTexSubImage2D.
 * По сути это вырезка из майкрафтовского TextureUtil.
 */
public class UtilTextureSubDataUpload {

    private final IntBuffer dataBuffer;


    public UtilTextureSubDataUpload() {
        dataBuffer = GLAllocation.createDirectIntBuffer(4_194_304);
    }


    public void uploadTextureSub(int[] texelData, int width, int height, int originX, int originY, boolean useLinearFilter, boolean useClampWrap) {
        int j1 = 4194304 / width;
        setTextureFilter(useLinearFilter);
        setTextureClamped(useClampWrap);
        int i2;

        for (int k1 = 0; k1 < width * height; k1 += width * i2) {
            int l1 = k1 / width;
            i2 = Math.min(j1, height - l1);
            int j2 = width * i2;
            copyToBufferPos(texelData, k1, j2);
            GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, originX, originY + l1, width, i2, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer);
        }

        dataBuffer.clear();
    }


    private void setTextureFilter(boolean useLinearFilter) {
        if (useLinearFilter) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    private void setTextureClamped(boolean useClampWrap) {
        if (useClampWrap) {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        } else {
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        }
    }

    private void copyToBufferPos(int[] p_110994_0_, int p_110994_1_, int p_110994_2_) {
        dataBuffer.clear();
        dataBuffer.put(p_110994_0_, p_110994_1_, p_110994_2_);
        dataBuffer.position(0).limit(p_110994_2_);
    }

}
