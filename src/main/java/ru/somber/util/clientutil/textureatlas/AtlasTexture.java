package ru.somber.util.clientutil.textureatlas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.ITickableTextureObject;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.somber.util.SomberUtilMod;
import ru.somber.util.clientutil.textureatlas.icon.AtlasIcon;
import ru.somber.util.clientutil.textureatlas.icon.MultiFrameAtlasIcon;
import ru.somber.util.clientutil.textureatlas.stitcher.Stitcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для представления текстурного атласа.
 * Во многом скопирован с манйкрафтовского TextureMap, однако здесь добавлена документация и вырезана часть ненужного кода.
 *
 * <p> Создание атласа:
 * <p> 1. Создаем объект класса с корректными atlasName и уровнями анизатропной фильтрации.
 * <p> Для atlasName: название атласа и путь до папки с тексутрами для атласа. Все текстуры атласа должны храниться в этой папке.
 * atlasName должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами".
 *
 * <p> 2. Все используемые для частиц иконки зарегистрировать. Лучше делать это, используя метод с параматром-объектом готовой иконки.
 * Т.е. объект иконки создать заранее и прописать ему нужные характеристики.
 * Для использования иконок подойдут только иконки-наследники от AtlasIcon этого же мода.
 * Для объекта иконок прописывать имя следующим образом: "MOD_ID + ":название_файла_текстуры_иконки"".
 * В качестве названия файла указывать только само название файла! Папки до файла указывать не нужно!
 *
 * <p> 3. Вызвать loadTextureAtlas с переданными файловым менеджером (можно юзать майнкрафтовский).
 * Этот метод загрузит текстурки частиц и сформирует текстурный атласа.
 *
 * <p> Использование атласа:
 * <p> 1. Забиндить атлас как текстуру.
 * <p> 2. Получить объекты иконок, в которых уже хранятся нужные текстурные координаты. Рисовать объекты с этими текстурными координатами.
 *
 * <p> При внесении изменений в атлас (добавление новой иконки и тд.) требуется пересоздавать атлас, иначе изменения не будут учтены.
 */
@SideOnly(Side.CLIENT)
public class AtlasTexture extends AbstractTexture implements ITickableTextureObject, IIconRegister {

    private static final Logger logger = LogManager.getLogger();

    /**
     * Название атласа, по совместительству это путь до папки с текстурами атласа.
     * Нужно для идентификации в менеджере ресурсов и формирования путей до ресурсов иконок.
     */
    private final String atlasName;

    /** Иконка для отсутствующих текстур. */
    private final AtlasIcon missingImage;

    /**
     * Здесь хранятся иконки, которые будут загружаться.
     * Хранение в формате <Название иконки, соответствующий AtlasIcon>.
     */
    private final Map<String, AtlasIcon> mapRegisteredIcons;
    /**
     * Здесь хранятся иконки, вошедшие в текущий текстурный атлас. Т.е. мапа заполнена иконками, имеющимися в уже готовом атласе.
     * Хранение в формате <Название иконки, соответствующий AtlasIcon>.
     */
    private final Map<String, AtlasIcon> mapUploadedIcons;
    /**
     * Отображение иконок, у которых есть внутренние фреймы.
     */
    private final Map<String, MultiFrameAtlasIcon> mapMultiFrameIcons;

    /**
     * Уровень анизатропной фильтрации для этого алтаса.
     */
    private int anisotropicFiltering;

    private int mipmapLevel;


    /**
     * @param atlasName название атласа. По совместительству это путь до папки с тексутрами для атласа.
     *                  Все текстуры атласа должны храниться в этой папке.
     *                  atlasName должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами"
     */
    public AtlasTexture(String atlasName) {
        this(atlasName, 0);
    }

    /**
     * @param atlasName название атласа. По совместительству это путь до папки с тексутрами для атласа.
     *                  Все текстуры атласа должны храниться в этой папке.
     *                  atlasName должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами"
     * @param anisotropicFiltering уровень анизатропной фильтрации.
     */
    public AtlasTexture(String atlasName, int anisotropicFiltering) {
        this.atlasName = atlasName;
        this.missingImage = new AtlasIcon("missingno", false, false);
        setAnisotropicFiltering(anisotropicFiltering);

        this.mapRegisteredIcons = new HashMap<>();
        this.mapUploadedIcons = new HashMap<>();
        this.mapMultiFrameIcons = new HashMap<>();
        this.mipmapLevel = 4;

        initMissingImage();
        Minecraft.getMinecraft().renderEngine.loadTickableTexture(new ResourceLocation(SomberUtilMod.MOD_ID, atlasName), this);
    }


    /**
     * Возвращает название атласа.
     * По совместительству это путь до папки с атласом.
     * Все текстуры атласа должны храниться в этой папке.
     */
    public String getAtlasName() {
        return atlasName;
    }

    /**
     * Возвращает зарегистрированную иконку по ее имени.
     */
    public AtlasIcon getRegisteredAtlasIcon(String iconName) {
        return mapRegisteredIcons.get(iconName);
    }

    /**
     * Возвращает уровень анизатропной фильтрации.
     */
    public int getAnisotropicFiltering() {
        return anisotropicFiltering;
    }

    /**
     * Возвращает иконку по имени, если она входит в текущий текстурный атлас.
     * Иначе возвращается текстура-заглушка.
     */
    public AtlasIcon getAtlasIcon(String iconName) {
        AtlasIcon particleAtlasIcon = this.mapUploadedIcons.get(iconName);
        if (particleAtlasIcon == null) {
            particleAtlasIcon = this.missingImage;
        }

        return particleAtlasIcon;
    }

    /**
     * Возвращает мультифреймовую иконку по имени, если она входит в текущий текстурный атлас.
     * Если иконка не найдена, выбрасывается RuntimeException.
     */
    public MultiFrameAtlasIcon getMultiFramesAtlasIcon(String iconName) {
        MultiFrameAtlasIcon icon = mapMultiFrameIcons.get(iconName);

        if (icon == null) {
            throw new RuntimeException("MultiFrameAtlasIcon with name:" + iconName + " not found.");
        }

        return icon;
    }

    /**
     * Устанавливает уровень анизатропной фильтрации для атласа.
     * Атлас с этим уровнем анизатропной фильтрации будет создан только после вызова loadTextureAtlas().
     */
    public void setAnisotropicFiltering(int newAnisotropicFiltering) {
        this.anisotropicFiltering = newAnisotropicFiltering;
    }

    /**
     * Загружает текстуры зарегистрированных иконкок и сшивает текстурный атлас.
     */
    public void stitchTextureAtlas(IResourceManager resourceManager) {
        this.deleteGlTexture();

        //Очищаем все загруженные иконки, коллекции будут заполнены далее новыми иконками.
        this.mapUploadedIcons.clear();
        this.mapMultiFrameIcons.clear();

        //инициализация ститчера.
        int maximumTextureSize = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(maximumTextureSize, maximumTextureSize, true, 0, mipmapLevel);

        //Здесь происходит загрузка и дальнешая подготовка всех иконок из mapRegisteredIcons.
        int minSizeDimensionIcon = loadAllIcons(resourceManager, stitcher);

        //предполагаемое кол-во мипмап уровней.
        int estimatedNumbersOfMips = MathHelper.calculateLogBaseTwo(minSizeDimensionIcon);
        if (estimatedNumbersOfMips < this.mipmapLevel) {
            logger.debug("{}: dropping miplevel from {} to {}, because of minTexel: {}",
                         new Object[] {this.getAtlasName(), Integer.valueOf(this.mipmapLevel), Integer.valueOf(estimatedNumbersOfMips), Integer.valueOf(minSizeDimensionIcon)});
            this.mipmapLevel = estimatedNumbersOfMips;
        }

        mapRegisteredIcons.forEach((iconName, icon) -> {
            icon.generateMipmaps(this.mipmapLevel);
        });
        missingImage.generateMipmaps(mipmapLevel);

        //подготовка текстуры-заглушки.
        stitcher.addSprite(this.missingImage);

        //Сшиваем будущий атлас.
        stitcher.doStitch();

        //Выделяем место под текстуру-атлас в OGL. Данные в эту текстуру загружаем ниже.
        logger.info("Created a texture-atlas with name: {} and size: {}x{}", this.atlasName, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
        TextureUtil.allocateTextureImpl(
                this.getGlTextureId(),
                this.mipmapLevel,
                stitcher.getCurrentWidth(),
                stitcher.getCurrentHeight(),
                this.anisotropicFiltering);


        //В tempMapRegisteredIcons после загрузки текстурный данных в атлас остануться иконки, которые не вошли в атлас по каким то причинам.
        HashMap<String, AtlasIcon> tempMapRegisteredIcons = Maps.newHashMap(this.mapRegisteredIcons);
        //Все иконки в mapRegisteredIcons, которые вошли в stitcher, загрузить в текстуру openGL.
        List<AtlasIcon> stitchSlots = stitcher.getStitchSlots();
        UtilTextureSubDataUpload subDataUpload = new UtilTextureSubDataUpload();
        for (AtlasIcon atlasIcons : stitchSlots) {
            String iconName = atlasIcons.getIconName();
            tempMapRegisteredIcons.remove(iconName);
            this.mapUploadedIcons.put(iconName, atlasIcons);

            try {
                //загрузить данные текстур в текстуру-атлас, по сути это glTexSubImage2D
                subDataUpload.uploadTextureMipmap(
                        atlasIcons.getTexelData(),
                        atlasIcons.getIconWidth(),
                        atlasIcons.getIconHeight(),
                        atlasIcons.getOriginX(),
                        atlasIcons.getOriginY(),
                        false,
                        false);
            } catch (Throwable throwable) {
                CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Stitching texture atlas");

                CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Texture being stitched together");

                crashreportcategory1.addCrashSection("Atlas path", this.atlasName);
                crashreportcategory1.addCrashSection("Sprite", atlasIcons);

                throw new ReportedException(crashreport1);
            }
        }

        //Для всех, не вошедших в mapRegisteredIcons спрайтов, установить атрибуты от missingImage.
        for (AtlasIcon atlasSprite : tempMapRegisteredIcons.values()) {
            atlasSprite.copyFrom(this.missingImage);
        }

        //Все иконки, у которых метод isMultiFramesIcon() возвращает true, закастить в MultiFrameAtlasIcon
        //и добавить в список мультифреймовых иконок.
        for (AtlasIcon icon : mapRegisteredIcons.values()) {
            if (icon.isMultiFramesIcon()) {
                mapMultiFrameIcons.put(icon.getIconName(), (MultiFrameAtlasIcon) icon);
            }
        }

        saveBufferAsTexture(getGlTextureId(), stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), "testatlas", mipmapLevel);
    }


    /**
     * Вносит переданную иконку в список иконок для загрузки и формирования атласа с ней.
     */
    public IIcon registerIcon(AtlasIcon icon) {
        String iconName = icon.getIconName();

        if (iconName == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        } else if (iconName.indexOf('\\') == -1) {  // Disable backslashes (\) in texture asset paths.
            this.mapRegisteredIcons.put(iconName, icon);
            return icon;
        } else {
            throw new IllegalArgumentException("Name cannot contain slashes!");
        }
    }

    /**
     * Создает простую иконку с переданным именем иконки
     * и вносит ее в список иконок для загрузки и формирования атласа с ней.
     */
    @Override
    public IIcon registerIcon(String iconName) {
        if (iconName == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        } else if (iconName.indexOf(92) == -1) {  // Disable backslashes (\) in texture asset paths.
            AtlasIcon textureAtlasSprite = this.mapRegisteredIcons.get(iconName);

            if (textureAtlasSprite == null) {
                textureAtlasSprite = new AtlasIcon(iconName, false, true);

                this.mapRegisteredIcons.put(iconName, textureAtlasSprite);
            }

            return textureAtlasSprite;
        } else {
            throw new IllegalArgumentException("Name cannot contain slashes!");
        }
    }

    /**
     * Метод ничего делает, т.к. текстура атласа не загружается, а формируется из других текстур.
     */
    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        stitchTextureAtlas(resourceManager);
    }

    @Override
    public void tick() {}


    /**
     * Создает текстуру-заглушку для случаев, когда необходимая текстура не найдена.
     */
    private void initMissingImage() {
        int[] textureData;

        if (this.anisotropicFiltering > 1) {
            textureData = new int[1024];

            this.missingImage.setIconWidth(32);
            this.missingImage.setIconHeight(32);

            System.arraycopy(TextureUtil.missingTextureData, 0, textureData, 0, TextureUtil.missingTextureData.length);
            TextureUtil.prepareAnisotropicData(textureData, 16, 16, 8);
        } else {
            textureData = TextureUtil.missingTextureData;

            this.missingImage.setIconWidth(16);
            this.missingImage.setIconHeight(16);
        }

        int[][] mipmapTextureData = new int[mipmapLevel + 1][];
        mipmapTextureData[0] = textureData;
        this.missingImage.setTexelData(mipmapTextureData);
    }

    /**
     * Загружает иконки из mapRegisteredIcons стандартным майновским способом.
     * Возвращает минимальный размер стороны иконки среди всех иконок.
     */
    private int loadAllIcons(IResourceManager resourceManager, Stitcher stitcher) {
        int minSizeDimensionOfIcon = Integer.MAX_VALUE;

        //здесь происходит загрузка текстур иконок.
        for (Map.Entry<String, AtlasIcon> entry : this.mapRegisteredIcons.entrySet()) {
            String iconName = entry.getKey();
            AtlasIcon icon = entry.getValue();

            ResourceLocation completeResourceLocation = this.completeResourceLocation(iconName);
            try {
                IResource textureImageResource = resourceManager.getResource(completeResourceLocation);
                BufferedImage buffImageData = ImageIO.read(textureImageResource.getInputStream());

                icon.loadIconData(buffImageData, this.anisotropicFiltering > 1, mipmapLevel);
            } catch (RuntimeException runtimeexception) {
                //logger.error("Unable to parse metadata from " + completeResourceLocation, runtimeexception);
                cpw.mods.fml.client.FMLClientHandler.instance().trackBrokenTexture(completeResourceLocation, runtimeexception.getMessage());
                continue;
            } catch (IOException ioexception1) {
                //logger.error("Using missing texture, unable to load " + completeResourceLocation, ioexception1);
                cpw.mods.fml.client.FMLClientHandler.instance().trackMissingTexture(completeResourceLocation);
                continue;
            }

            minSizeDimensionOfIcon = Math.min(minSizeDimensionOfIcon, Math.min(icon.getIconWidth(), icon.getIconHeight()));
            stitcher.addSprite(icon);
        }

        return minSizeDimensionOfIcon;
    }

    /**
     * Путь до ресурса выглядит следующим образом:
     * <p>atlasPath + "/" + iconName + ".png"
     * <p>Примеры: "atlasPath/iconTexture.png".
     */
    private ResourceLocation completeResourceLocation(String iconName) {
        int divider = iconName.indexOf(':');
        String domain = iconName.substring(0, divider);
        String path = iconName.substring(divider + 1);

        return new ResourceLocation(domain, String.format("%s/%s%s", this.atlasName, path, ".png"));
    }


    /**
     * Утилитный класс для применения метода glTexSubImage2D.
     * По сути это вырезка из майкрафтовского TextureUtil.
     */
    private static class UtilTextureSubDataUpload {

        private final IntBuffer dataBuffer;


        public UtilTextureSubDataUpload() {
            dataBuffer = BufferUtils.createIntBuffer(4_194_304);
        }


        public void uploadTextureMipmap(int[][] textureData, int width, int height, int originX, int originY, boolean isUseLinearFiltering, boolean isUseClampWrap) {
            for (int mipmapLevel = 0; mipmapLevel < textureData.length; mipmapLevel++) {
                int[] textureMipmapData = textureData[mipmapLevel];
                uploadTextureSub(mipmapLevel,
                                 textureMipmapData,
                                 width >> mipmapLevel,
                                 height >> mipmapLevel,
                                 originX >> mipmapLevel,
                                 originY >> mipmapLevel,
                                 isUseLinearFiltering,
                                 isUseClampWrap,
                                 textureData.length > 1);
            }
        }

        public void uploadTextureSub(int mipmapLevel, int[] texelData, int width, int height, int originX, int originY, boolean isUseLinearFiltering, boolean isUseClampWrap, boolean isUseMipmapFiltering) {
            int j1 = 4_194_304 / width;
            setTextureFilter(isUseLinearFiltering, isUseMipmapFiltering);
            setTextureClamped(isUseClampWrap);
            int i2;

            for (int k1 = 0; k1 < width * height; k1 += width * i2) {
                int l1 = k1 / width;
                i2 = Math.min(j1, height - l1);
                int j2 = width * i2;
                copyToBufferPos(texelData, k1, j2);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, mipmapLevel, originX, originY + l1, width, i2, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, dataBuffer);
            }

            dataBuffer.clear();
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

        private void setTextureFilter(boolean isLinearFiltering, boolean isMipmapFiltering) {
            if (isLinearFiltering) {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, isMipmapFiltering ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            } else {
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, isMipmapFiltering ? GL11.GL_NEAREST_MIPMAP_LINEAR : GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }
        }

        private void copyToBufferPos(int[] data, int offset, int dataLength) {
            dataBuffer.clear();
            dataBuffer.put(data, offset, dataLength);
            dataBuffer.position(0).limit(dataLength);
        }

    }

    public static void saveBufferAsTexture(int textureID, int width, int height, String name, int countMipmaps) {
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

                for(int i1 = 0; i1 < heightOfMipmap; ++i1) {
                    for(int j1 = 0; j1 < widthOfMipmap; ++j1) {
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

