package ru.somber.util.clientutil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Утилиты для клиентской стороны.
 */
@SideOnly(Side.CLIENT)
public final class SomberClientUtil {
    private SomberClientUtil() {}

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


    /**
     * Сохраняет на диск D в файл "D:\filename_mipmapLevel.png" текстуру с переданными id и размерами.
     */
    public static void saveTextureInFile(int textureID, int width, int height, String name, int countMipmaps) {
        for (int level = 0; level <= countMipmaps; level++) {
            try {
                int widthOfMipmap = width >> level;
                int heightOfMipmap = height >> level;

                int e = widthOfMipmap * heightOfMipmap;
                IntBuffer pixelBuffer = BufferUtils.createIntBuffer(e);
                int[] pixelValues = new int[e];
                GL11.glPixelStorei(3333, 1);
                GL11.glPixelStorei(3317, 1);
                pixelBuffer.clear();
                GL11.glBindTexture(3553, textureID);
                GL11.glGetTexImage(3553, level, '\u80e1', '\u8367', pixelBuffer);
                pixelBuffer.get(pixelValues);
                TextureUtil.func_147953_a(pixelValues, widthOfMipmap, heightOfMipmap);
                BufferedImage bufferedimage = null;
                bufferedimage = new BufferedImage(widthOfMipmap, heightOfMipmap, 1);

                for (int i1 = 0; i1 < heightOfMipmap; ++i1) {
                    for (int j1 = 0; j1 < widthOfMipmap; ++j1) {
                        bufferedimage.setRGB(j1, i1, pixelValues[i1 * widthOfMipmap + j1]);
                    }
                }

                ImageIO.write(bufferedimage, "png", new File("D:\\" + name + "_" + level + ".png"));
            } catch (Exception var10) {
                var10.printStackTrace();
            }
        }
    }

}
