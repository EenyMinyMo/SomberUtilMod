package ru.somber.util.clientutil.textureatlas.icon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

import java.awt.image.BufferedImage;

/**
 * Класс для представления иконок (по большей части это копия класса спрайта текстурного атласа из майкнрафта, но выпилен бесполезный функционал).
 * Включена поддержка инвертированных по осям U и V текстур.
 * <p>
 * Для использования прописывать название иконки следующим образом: "MOD_ID + ":название_файла_текстуры_иконки"".
 * В качестве названия файла указывать только само название файла!
 * Папки до файла текстуры не нужно (папки до текстуры должны быть прописаны в объекте текстуры атласа как название атласа).
 */
@SideOnly(Side.CLIENT)
public class AtlasIcon implements IIcon {
    /**
     * Название иконки.
     * По совместительству это и название файла с текстурой иконки.
     * Название иконки должно иметь следущий формат "MOD_ID + ":название_файла_текстуры_иконки"".
     */
    private final String iconName;

    /** Массив данных текселей соответствующей текстуры. */
    private int[] texelData;

    /** Флаг нужно ли учитывать использование анизатропной фильтрации при подготовке текселей текстуры. */
    private boolean useAnisotropicFiltering;
    /** Флаг хранится ли текстура в текстурном атласе в перевернутом виде. */
    private boolean isRotated;

    private float minU;
    private float maxU;
    private float minV;
    private float maxV;

    /** Смещение начала текстуры иконки в текстурном атласе по оси X (в пикселях). */
    private int originX;
    /** Смещение начала текстуры иконки в текстурном атласе по оси Y (в пикселях). */
    private int originY;

    /** Высота текстуры иконки в текстурном атласе (в пикселях). */
    private int width;
    /** Ширина текстуры иконки в текстурном атласе (в пикселях). */
    private int height;

    /** Флаг нужно ли инвертировать текстурные координты U. */
    private boolean isInvertedU;
    /** Флаг нужно ли инвертировать текстурные координты V. */
    private boolean isInvertedV;


    /**
     * @param iconName имя иконки.
     * @param invertU нужно ли инвертировать текстурные координаты по оси U.
     * @param invertV нужно ли инвертировать текстурные координаты по оси V.
     */
    public AtlasIcon(String iconName, boolean invertU, boolean invertV) {
        this.iconName = iconName;
        this.isInvertedU = invertU;
        this.isInvertedV = invertV;
    }


    /**
     * Иницализирует иконку переданными параметрами.
     *
     * @param widthAtlas - ширина атласа текстур, куда вошла эта иконка (в пикселях).
     * @param heightAtlas - высота атласа текстур, куда вошла эта иконка (в пикселях).
     * @param originXInAtlas - смещение начала текстуры иконки в текстурном атласе по оси X (в пикселях).
     * @param originYInAtlas - смещение начала текстуры иконки в текстурном атласе по оси Y (в пикселях).
     * @param isRotated - нужно ли повенуть текстуру.
     */
    public void initIcon(int widthAtlas, int heightAtlas, int originXInAtlas, int originYInAtlas, boolean isRotated) {
        this.originX = originXInAtlas;
        this.originY = originYInAtlas;
        this.isRotated = isRotated;

        //не до конца уверен что это за переменные и что они делают.
        //название этих переменных можн не совпадать с реальным назначением.
        double widthOffset = 0.01D / widthAtlas;
        double heightOffset = 0.01D / heightAtlas;

        this.minU = (float) ((double) originXInAtlas / widthAtlas + widthOffset);
        this.maxU = (float) ((double) (originXInAtlas + this.width) / widthAtlas - widthOffset);

        this.minV = (float) ((double) originYInAtlas / heightAtlas + heightOffset);
        this.maxV = (float) ((double) (originYInAtlas + this.height) / heightAtlas - heightOffset);

        if (this.useAnisotropicFiltering) {
            float widthAnisotropicOffset = 8.0F / (float) widthAtlas;
            float heightAnisotropicOffset = 8.0F / (float) heightAtlas;

            this.minU += widthAnisotropicOffset;
            this.maxU -= widthAnisotropicOffset;
            this.minV += heightAnisotropicOffset;
            this.maxV -= heightAnisotropicOffset;
        }

        if (isInvertedU) {
            float temp = minU;
            minU = maxU;
            maxU = temp;
        }

        if (isInvertedV) {
            float temp = minV;
            minV = maxV;
            maxV = temp;
        }
    }

    /**
     * Компирует данные иконки из переданной иконки.
     */
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

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }

    @Override
    public float getMinU() {
        return this.minU;
    }

    @Override
    public float getMaxU() {
        return this.maxU;
    }

    @Override
    public float getInterpolatedU(double interpolationFactor) {
        return minU + (maxU - minU) * (float) interpolationFactor / 16.0F;
    }

    @Override
    public float getMinV() {
        return this.minV;
    }

    @Override
    public float getMaxV() {
        return this.maxV;
    }

    @Override
    public float getInterpolatedV(double interpolatedFactor) {
        return minV + (maxV - minV) * ((float) interpolatedFactor / 16.0F);
    }

    @Override
    public String getIconName() {
        return this.iconName;
    }


    /**
     * Возвращает начальное положение X текстуры иконки в атласе текстур (в пикселях).
     */
    public int getOriginX() {
        return this.originX;
    }

    /**
     * Возвращает начальное положение Y текстуры иконки в атласе текстур (в пикселях).
     */
    public int getOriginY() {
        return this.originY;
    }

    /**
     * Устанавливает ширину иконки (в пикселях).
     */
    public void setIconWidth(int newIconWidth) {
        this.width = newIconWidth;
    }

    /**
     * Устанавливает высоту иконки (в пикселях).
     */
    public void setIconHeight(int newIconHeight) {
        this.height = newIconHeight;
    }

    /**
     * Возвращает массив с текстурными данными иконки.
     */
    public int[] getTexelData() {
        return texelData;
    }

    /**
     * Устанавливает массив с текстурными данными иконки.
     */
    public void setTexelData(int[] texelData) {
        this.texelData = texelData;
    }

    /**
     * Возвращает true, если для этой иконки включен флаг ипользования анизатропной фильтрации.
     * Этот флаг влияет на то, как загружается иконка.
     */
    public boolean isUseAnisotropicFiltering() {
        return useAnisotropicFiltering;
    }

    /**
     * Возвращает true, если иконка в атласе текстур хранится в перевернутом виде.
     */
    public boolean isRotated() {
        return isRotated;
    }

    /**
     * true, если иконка имеет вложенные фреймы.
     */
    public boolean isMultiFramesIcon() {
        return false;
    }

    /**
     * Возвращает true, если текстурные координаты minU и maxU будут браться наоборот (т.е. minU на самом деле maxU и наоборот).
     * Если возвращается false, то ничего не меняется.
     */
    public boolean isInvertedU() {
        return isInvertedU;
    }

    /**
     * Возвращает true, если текстурные координаты minV и maxV будут браться наоборот (т.е. minV на самом деле maxV и наоборот).
     * Если возвращается false, то ничего не меняется.
     */
    public boolean isInvertedV() {
        return isInvertedV;
    }

    /**
     * Устанавливает будут ли браться текстурные координаты minU и maxU наоборот.
     * Изменения вступят в силу только после вызова initIcon().
     */
    public void setInvertedU(boolean invertedU) {
        isInvertedU = invertedU;
    }

    /**
     * Устанавливает будут ли браться текстурные координаты minV и maxV наоборот.
     * Изменения вступят в силу только после вызова initIcon().
     */
    public void setInvertedV(boolean invertedV) {
        isInvertedV = invertedV;
    }

    /**
     * Загружает из переданного буфера тексельные данные и подготавливает к использованию
     * в качестве данных иконки с учетом флага использования анизатропной фильтрации.
     * После вызова этого метода можно использовать texelData иконки.
     */
    public void loadIconData(BufferedImage bufferedimage, boolean useAnisotropicFiltering) {
        this.useAnisotropicFiltering = useAnisotropicFiltering;
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

        //попробовать удалить эту проверку, чтобы появилась возможность грузить текстуры с разным соотношением сторон.
        if (height != width) {
            throw new RuntimeException("broken aspect ratio and not an animation");
        }

        fixTransparentPixels(texelDataArray);
        this.texelData = prepareAnisotropicFiltering(texelDataArray, width, height, isUseAnisotropicFiltering());
    }


    protected void setUseAnisotropicFiltering(boolean useAnisotropicFiltering) {
        this.useAnisotropicFiltering = useAnisotropicFiltering;
    }

    protected void setRotated(boolean rotated) {
        isRotated = rotated;
    }

    protected void setMinU(float minU) {
        this.minU = minU;
    }

    protected void setMaxU(float maxU) {
        this.maxU = maxU;
    }

    protected void setMinV(float minV) {
        this.minV = minV;
    }

    protected void setMaxV(float maxV) {
        this.maxV = maxV;
    }

    protected void setOriginX(int originX) {
        this.originX = originX;
    }

    protected void setOriginY(int originY) {
        this.originY = originY;
    }



    private static void fixTransparentPixels(int[] texelData) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        int i1;

        for (i1 = 0; i1 < texelData.length; ++i1) {
            if ((texelData[i1] & -16777216) != 0) {
                j += texelData[i1] >> 16 & 255;
                k += texelData[i1] >> 8 & 255;
                l += texelData[i1] >> 0 & 255;
                ++i;
            }
        }

        if (i != 0) {
            j /= i;
            k /= i;
            l /= i;

            for (i1 = 0; i1 < texelData.length; ++i1) {
                if ((texelData[i1] & -16777216) == 0) {
                    texelData[i1] = j << 16 | k << 8 | l;
                }
            }
        }
    }

    private static int[] prepareAnisotropicFiltering(int[] texelData, int width, int height, boolean useAnisotropicFiltering) {
        if (! useAnisotropicFiltering) {
            return texelData;
        } else {
            int[] texelAnisotropicData = texelData;
            int[] tempTexelData = new int[(width + 16) * (height + 16)];
            System.arraycopy(texelAnisotropicData, 0, tempTexelData, 0, texelAnisotropicData.length);
            texelAnisotropicData = prepareAnisotropicData(tempTexelData, width, height, 8);

            return texelAnisotropicData;
        }
    }

    /* копия метода из майкрафтовского TextureUtil. */
    private static int[] prepareAnisotropicData(int[] texelData, int width, int height, int anisotropicTexelOffset) {
        int l = width + 2 * anisotropicTexelOffset;
        int i1;
        int j1;

        for (i1 = height - 1; i1 >= 0; --i1) {
            j1 = i1 * width;
            int k1 = anisotropicTexelOffset + (i1 + anisotropicTexelOffset) * l;
            int l1;

            for (l1 = 0; l1 < anisotropicTexelOffset; l1 += width) {
                int i2 = Math.min(width, anisotropicTexelOffset - l1);
                System.arraycopy(texelData, j1 + width - i2, texelData, k1 - l1 - i2, i2);
            }

            System.arraycopy(texelData, j1, texelData, k1, width);

            for (l1 = 0; l1 < anisotropicTexelOffset; l1 += width) {
                System.arraycopy(texelData, j1, texelData, k1 + width + l1, Math.min(width, anisotropicTexelOffset - l1));
            }
        }

        for (i1 = 0; i1 < anisotropicTexelOffset; i1 += height) {
            j1 = Math.min(height, anisotropicTexelOffset - i1);
            System.arraycopy(texelData, (anisotropicTexelOffset + height - j1) * l, texelData, (anisotropicTexelOffset - i1 - j1) * l, l * j1);
        }

        for (i1 = 0; i1 < anisotropicTexelOffset; i1 += height) {
            j1 = Math.min(height, anisotropicTexelOffset - i1);
            System.arraycopy(texelData, anisotropicTexelOffset * l, texelData, (height + anisotropicTexelOffset + i1) * l, l * j1);
        }

        return texelData;
    }

}
