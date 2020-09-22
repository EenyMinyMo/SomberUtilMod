package ru.somber.clientutil.textureatlas;

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
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import ru.somber.clientutil.textureatlas.icon.AtlasIcon;
import ru.somber.clientutil.textureatlas.icon.MultiFrameAtlasIcon;
import ru.somber.clientutil.textureatlas.stitcher.Stitcher;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
     * Нужно для идетификации в менеджере ресурсов и формирования путей до ресурсов иконок.
     */
    private final String atlasName;

    /** Иконка для отсутствующих текстур. */
    private final AtlasIcon missingImage = new AtlasIcon("missingno", false);

    /**
     * Здесь хранятся иконки, которые будут загружаться.
     * Хранение в формате <Название иконки, соответствующий AtlasIcon>.
     */
    private final Map<String, AtlasIcon> mapRegisteredIcons = Maps.newHashMap();
    /**
     * Здесь хранятся иконки, вошедшие в текущий текстурный атлас. Т.е. мапа заполнена иконками, имеющимися в уже готовом атласе.
     * Хранение в формате <Название иконки, соответствующий AtlasIcon>.
     */
    private final Map<String, AtlasIcon> mapUploadedIcons = Maps.newHashMap();
    /**
     * Список иконок, у которых есть внутренние фреймы.
     */
    private final List<MultiFrameAtlasIcon> listMultiFramesIcons = Lists.newArrayList();

    /** Уровень анизатропной фильтрации для этого алтаса. */
    private int anisotropicFiltering = 1;


    /**
     * @param atlasName название атласа. По совместительству это путь до папки с тексутрами для атласа.
     *                  Все текстуры атласа должны храниться в этой папке.
     *                  atlasName должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами"
     */
    public AtlasTexture(String atlasName) {
        this(atlasName, 1);
    }

    /**
     * @param atlasName название атласа. По совместительству это путь до папки с тексутрами для атласа.
     *                  Все текстуры атласа должны храниться в этой папке.
     *                  atlasName должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами"
     * @param anisotropicFiltering уровень анизатропной фильтрации.
     */
    public AtlasTexture(String atlasName, int anisotropicFiltering) {
        this.atlasName = atlasName;
        this.anisotropicFiltering = anisotropicFiltering;

        initMissingImage();
        Minecraft.getMinecraft().renderEngine.loadTickableTexture(new ResourceLocation(atlasName), this);
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
        for (MultiFrameAtlasIcon icon : listMultiFramesIcons) {
            if (icon.getIconName().equals(iconName)) {
                return icon;
            }
        }

        throw new RuntimeException("MultiFrameAtlasIcon with name:" + iconName + " not found.");
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
        this.listMultiFramesIcons.clear();

        //инициализация ститчера.
        int maximumTextureSize = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(maximumTextureSize, maximumTextureSize, true, 0);

        //Здесь происходит загрузка и дальнешая подготовка всех иконок из mapRegisteredIcons
        loadAllIcons(resourceManager, stitcher);

        //подготовка текстуры-заглушки.
        stitcher.addSprite(this.missingImage);

        //Сшиваем будущий атлас.
        stitcher.doStitch();

        //Выделяем место под текстуру-атлас в OGL. Данные в эту текстуру загружаем ниже.
        logger.info("Created a texture-atlas with name: {} and size: {}x{}", this.atlasName, stitcher.getCurrentWidth(), stitcher.getCurrentHeight());
        TextureUtil.allocateTextureImpl(
                this.getGlTextureId(),
                0,
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
                subDataUpload.uploadTextureSub(
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
                listMultiFramesIcons.add((MultiFrameAtlasIcon) icon);
            }
        }
    }


    /**
     * Вносит переданную иконку в список иконок для загрузки и формирования атласа с ней.
     */
    public IIcon registerIcon(AtlasIcon icon) {
        String iconName = icon.getIconName();

        if (iconName == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        } else if (iconName.indexOf(92) == -1) {  // Disable backslashes (\) in texture asset paths.
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
                textureAtlasSprite = new AtlasIcon(iconName, false);

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
    public void loadTexture(IResourceManager resourceManager) throws IOException {}

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

        this.missingImage.setTexelData(textureData);
    }

    /**
     * Загружает иконки из mapRegisteredIcons стандартным майновским способом.
     */
    private void loadAllIcons(IResourceManager resourceManager, Stitcher stitcher) {
        //здесь происходит загрузка текстур иконок.
        for (Map.Entry<String, AtlasIcon> entry : this.mapRegisteredIcons.entrySet()) {
            String iconName = entry.getKey();
            AtlasIcon textureAtlasSprite = entry.getValue();

            ResourceLocation completeResourceLocation = this.completeResourceLocation(iconName);
            try {
                IResource textureImageResource = resourceManager.getResource(completeResourceLocation);
                BufferedImage buffImageData = ImageIO.read(textureImageResource.getInputStream());

                textureAtlasSprite.loadIconData(buffImageData, this.anisotropicFiltering > 1);
            } catch (RuntimeException runtimeexception) {
                //logger.error("Unable to parse metadata from " + completeResourceLocation, runtimeexception);
                cpw.mods.fml.client.FMLClientHandler.instance().trackBrokenTexture(completeResourceLocation, runtimeexception.getMessage());
                continue;
            } catch (IOException ioexception1) {
                //logger.error("Using missing texture, unable to load " + completeResourceLocation, ioexception1);
                cpw.mods.fml.client.FMLClientHandler.instance().trackMissingTexture(completeResourceLocation);
                continue;
            }

            stitcher.addSprite(textureAtlasSprite);
        }
    }

    /**
     * Путь до ресурса выглядит следующим образом:
     * <p>atlasPath + "/" + iconName + ".png"
     * <p>Примеры: "atlasPath/iconTexture.png".
     */
    private ResourceLocation completeResourceLocation(String iconName) {
        int divider = iconName.indexOf(':');
        String domain = iconName.substring(0, divider);
        String path = iconName.substring(divider + 1, iconName.length());

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


        public void uploadTextureSub(int[] texelData, int width, int height, int originX, int originY, boolean useLinearFilter, boolean useClampWrap) {
            int j1 = 4_194_304 / width;
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

        private void copyToBufferPos(int[] data, int offset, int dataLength) {
            dataBuffer.clear();
            dataBuffer.put(data, offset, dataLength);
            dataBuffer.position(0).limit(dataLength);
        }

    }

}

