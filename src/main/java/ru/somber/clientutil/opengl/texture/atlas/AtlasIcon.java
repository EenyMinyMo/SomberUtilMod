package ru.somber.clientutil.opengl.texture.atlas;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.IIcon;

import java.awt.image.BufferedImage;

@SideOnly(Side.CLIENT)
public class AtlasIcon implements IIcon {
    private final String iconName;

    private int[] textureData;

    private boolean useAnisotropicFiltering;
    private boolean isRotated;

    private float minU;
    private float maxU;
    private float minV;
    private float maxV;


    private int originX;
    private int originY;

    private int width;
    private int height;


    public AtlasIcon(String iconName) {
        this.iconName = iconName;
    }

    /**
     * @param width - ширина в пикселях
     * @param height - высота в пикселях
     * @param originX - началная позиция по оси Х в пикселях (типо xMin, но в пискелях)
     * @param originY - началная позиция по оси У в пикселях (типо уMin, но в пискелях)
     * @param isRotated - нужно ли повенуть текстуру
     */
    public void initSprite(int width, int height, int originX, int originY, boolean isRotated) {
        this.originX = originX;
        this.originY = originY;
        this.isRotated = isRotated;
        float f = (float) (0.009999999776482582D / (double) width);
        float f1 = (float) (0.009999999776482582D / (double) height);
        this.minU = (float) originX / (float) ((double) width) + f;
        this.maxU = (float) (originX + this.width) / (float) ((double) width) - f;
        this.minV = (float) originY / (float) height + f1;
        this.maxV = (float) (originY + this.height) / (float) height - f1;

        if (this.useAnisotropicFiltering) {
            float f2 = 8.0F / (float) width;
            float f3 = 8.0F / (float) height;
            this.minU += f2;
            this.maxU -= f2;
            this.minV += f3;
            this.maxV -= f3;
        }
    }

    public void copyFrom(AtlasIcon icon) {
        this.originX = icon.originX;
        this.originY = icon.originY;
        this.width = icon.width;
        this.height = icon.height;
        this.isRotated = icon.isRotated;
        this.minU = icon.minU;
        this.maxU = icon.maxU;
        this.minV = icon.minV;
        this.maxV = icon.maxV;
    }

    /**
     * Returns the X position of this icon on its texture sheet, in pixels.
     */
    public int getOriginX() {
        return this.originX;
    }

    /**
     * Returns the Y position of this icon on its texture sheet, in pixels.
     */
    public int getOriginY() {
        return this.originY;
    }

    /**
     * Returns the width of the icon, in pixels.
     */
    @Override
    public int getIconWidth() {
        return this.width;
    }

    /**
     * Returns the height of the icon, in pixels.
     */
    @Override
    public int getIconHeight() {
        return this.height;
    }

    /**
     * Returns the minimum U coordinate to use when rendering with this icon.
     */
    @Override
    public float getMinU() {
        return this.minU;
    }

    /**
     * Returns the maximum U coordinate to use when rendering with this icon.
     */
    @Override
    public float getMaxU() {
        return this.maxU;
    }

    /**
     * Gets a U coordinate on the icon. 0 returns uMin and 16 returns uMax. Other arguments return in-between values.
     */
    @Override
    public float getInterpolatedU(double interpolationFactor) {
        return minU + (maxU - minU) * (float) interpolationFactor / 16.0F;
    }

    /**
     * Returns the minimum V coordinate to use when rendering with this icon.
     */
    @Override
    public float getMinV() {
        return this.minV;
    }

    /**
     * Returns the maximum V coordinate to use when rendering with this icon.
     */
    @Override
    public float getMaxV() {
        return this.maxV;
    }

    /**
     * Gets a V coordinate on the icon. 0 returns vMin and 16 returns vMax. Other arguments return in-between values.
     */
    @Override
    public float getInterpolatedV(double interpolatedFactor) {
        return minV + (maxV - minV) * ((float) interpolatedFactor / 16.0F);
    }

    @Override
    public String getIconName() {
        return this.iconName;
    }


    public void setIconWidth(int newIconWidth) {
        this.width = newIconWidth;
    }

    public void setIconHeight(int newIconHeight) {
        this.height = newIconHeight;
    }

    public int[] getTextureData() {
        return textureData;
    }

    public void setTextureData(int[] textureData) {
        this.textureData = textureData;
    }

    public void loadSprite(BufferedImage bufferedimage, boolean p_147964_3_) {
        this.useAnisotropicFiltering = p_147964_3_;
        int width = bufferedimage.getWidth();
        int height = bufferedimage.getHeight();
        this.width = width;
        this.height = height;

        if (this.useAnisotropicFiltering) {
            this.width += 16;
            this.height += 16;
        }

        int[] texelDataArray = new int[bufferedimage.getWidth() * bufferedimage.getHeight()];
        bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), texelDataArray, 0, bufferedimage.getWidth());

        if (height != width) {
            throw new RuntimeException("broken aspect ratio and not an animation");
        }

        this.fixTransparentPixels(texelDataArray);
        this.textureData = this.prepareAnisotropicFiltering(texelDataArray, width, height);
    }

    public boolean isAnimatedIcon() {
        return false;
    }


    private void fixTransparentPixels(int[] p_147961_1_) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int i1;

        for (i1 = 0; i1 < p_147961_1_.length; ++i1) {
            if ((p_147961_1_[i1] & -16777216) != 0) {
                j += p_147961_1_[i1] >> 16 & 255;
                k += p_147961_1_[i1] >> 8 & 255;
                l += p_147961_1_[i1] >> 0 & 255;
                ++i;
            }
        }

        if (i != 0) {
            j /= i;
            k /= i;
            l /= i;

            for (i1 = 0; i1 < p_147961_1_.length; ++i1) {
                if ((p_147961_1_[i1] & -16777216) == 0) {
                    p_147961_1_[i1] = j << 16 | k << 8 | l;
                }
            }
        }
    }

    private int[] prepareAnisotropicFiltering(int[] texelData, int width, int height) {
        if (! this.useAnisotropicFiltering) {
            return texelData;
        } else {
            int[] texelAnisotropicData = texelData;
            int[] tempTexelData = new int[width * height];
            System.arraycopy(texelAnisotropicData, 0, tempTexelData, 0, texelAnisotropicData.length);
            texelAnisotropicData = TextureUtil.prepareAnisotropicData(tempTexelData, width, height, 8);

            return texelAnisotropicData;
        }
    }

//    private static int[][] getFrameTextureData(int[][] p_147962_0_, int p_147962_1_, int p_147962_2_, int p_147962_3_) {
//        int[][] aint1 = new int[p_147962_0_.length][];
//
//        for (int l = 0; l < p_147962_0_.length; ++l) {
//            int[] aint2 = p_147962_0_[l];
//
//            if (aint2 != null) {
//                aint1[l] = new int[(p_147962_1_ >> l) * (p_147962_2_ >> l)];
//                System.arraycopy(aint2, p_147962_3_ * aint1[l].length, aint1[l], 0, aint1[l].length);
//            }
//        }
//
//        return aint1;
//    }

}
