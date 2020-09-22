package ru.somber.clientutil.opengl.texture.atlas;

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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс для представления текстурного атласа частиц.
 * Во многом скопирован с манйкрафтовского TextureMap, однако здесь добавлена документация и вырезана часть ненужного кода.
 *
 * <p> Создание атласа:
 * <p> 1. Создаем объект класса с корректными atlasPath и уровнями анизатропной фильтрации и мипмапы.
 * <p> Для atlasPath: путь до папки с тексутрами для атласа. Все текстуры атласа должны храниться в этой папке.
 * atlasPath должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами".
 *
 * <p> 2. Все используемые для частиц спрайты зарегистрировать. Лучше делать это, используя метод с параматром-объектом готовой иконки.
 * Т.е. объект иконки создать заранее и прописать ему нужные характеристики.
 * Для использования иконок подойдут только иконки-наследники от ParticleAtlasIcon.
 * Для объекта иконок прописывать имя следующим образом: "MOD_ID + ":название_файла_частицы"".
 * В качестве названия файла указывать только само название файла! Папки до файла не нужно!
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
     * Здесь хранятся иконки, которые будут загружаться.
     * Хранение в формате <Название иконки, соответствующий ParticleAtlasIcon>.
     */
    private final Map<String, AtlasIcon> mapRegisteredIcons = Maps.newHashMap();
    /**
     * Здесь хранятся иконки, вошедшие в текущий текстурный атлас. Т.е. мапа заполнена иконки, имеющися в уже готовом атласе.
     * <Название иконки, соответствующий ParticleAtlasIcon>.
     */
    private final Map<String, AtlasIcon> mapUploadedIcons = Maps.newHashMap();
    /** Список иконок, которые могут в анимацию. */
    private final List<AnimatedAtlasIcon> listAnimatedIcons = Lists.newArrayList();

    /** Путь до атласа. Нужен для идетификации в менеджере ресурсов и формирования путей до ресурсов частиц. */
    private final String atlasPath;
    /** Уровень анизатропной фильтрации для этого алтаса. */
    private int anisotropicFiltering = 1;

    /** Спрайт для отсутствующих текстур. */
    private final AtlasIcon missingImage = new AtlasIcon("missingno");


    /**
     * @param atlasPath путь до папки с тексутрами для атласа. Все текстуры атласа должны храниться в этой папке.
     *                  atlasPath должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами"
     */
    public AtlasTexture(String atlasPath) {
        this(atlasPath, 1);
    }

    /**
     * @param atlasPath путь до папки с тексутрами для атласа. Все текстуры атласа должны храниться в этой папке.
     *                  atlasPath должен быть следующего формата: "MOD_ID:путь_до_папки_с_текстурами"
     * @param anisotropicFiltering уровень анизатропной фильтрации.
     */
    public AtlasTexture(String atlasPath, int anisotropicFiltering) {
        this.atlasPath = atlasPath;
        this.anisotropicFiltering = anisotropicFiltering;

        initMissingImage();
        Minecraft.getMinecraft().renderEngine.loadTickableTexture(new ResourceLocation(atlasPath), this);
    }


    /**
     * Загружает частицы и формирует текстурный атлас частиц.
     */
    public void loadTextureAtlas(IResourceManager resourceManager) {
        this.deleteGlTexture();

        //они будут заполнены далее.
        this.mapUploadedIcons.clear();
        this.listAnimatedIcons.clear();

        //инициализация ститчера.
        int maximumTextureSize = Minecraft.getGLMaximumTextureSize();
        Stitcher stitcher = new Stitcher(maximumTextureSize, maximumTextureSize, true, 0);

        //Здесь происходит загрузка и дальнешая подготовка всех спрайтов из mapRegisteredIcons
        loadAllIcons(resourceManager, stitcher, maximumTextureSize);

        //подготовка текстуры-заглушки.
        stitcher.addSprite(this.missingImage);

        //Сшиваем текстуру.
        stitcher.doStitch();

        //Выделяем место под текстуру-атлас в OGL. Данные в эту текстуру загружаем ниже.
        logger.info("Created: {}x{} {}-atlas", stitcher.getCurrentWidth(), stitcher.getCurrentHeight(), this.atlasPath);
        TextureUtil.allocateTextureImpl(
                this.getGlTextureId(),
                0,
                stitcher.getCurrentWidth(),
                stitcher.getCurrentHeight(),
                this.anisotropicFiltering);

        //Все спрайты в mapRegisteredSprites, которые вошли в stitcher, загрузить в текстуру openGL вместе с их мипмапами.
        HashMap<String, AtlasIcon> tempMapRegisteredIcons = Maps.newHashMap(this.mapRegisteredIcons);
        List<AtlasIcon> stichSlots = stitcher.getStitchSlots();
        for (AtlasIcon atlasIcons : stichSlots) {
            String iconName = atlasIcons.getIconName();
            tempMapRegisteredIcons.remove(iconName);
            this.mapUploadedIcons.put(iconName, atlasIcons);

            try {
                UtilTextureSubDataUpload subDataUpload = new UtilTextureSubDataUpload();
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

                crashreportcategory1.addCrashSection("Atlas path", this.atlasPath);
                crashreportcategory1.addCrashSection("Sprite", atlasIcons);

                throw new ReportedException(crashreport1);
            }
        }

        //Для всех, не вошедших в mapRegisteredSprites спрайтов, установить атрибуты от missingImage.
        for (AtlasIcon atlasSprite : tempMapRegisteredIcons.values()) {
            atlasSprite.copyFrom(this.missingImage);
        }

        for (AtlasIcon icon : mapRegisteredIcons.values()) {
            if (icon.isAnimatedIcon()) {
                listAnimatedIcons.add((AnimatedAtlasIcon) icon);
            }
        }
    }

    /**
     * Возвращает зарегистрированный спрайт по его имени.
     */
    public AtlasIcon getRegisteredParticleAtlasIcon(String iconName) {
        return mapRegisteredIcons.get(iconName);
    }

    /**
     * Возвращает путь до папки с атласом.
     * Все текстуры атласа должны храниться в этой папке.
     */
    public String getAtlasPath() {
        return atlasPath;
    }

    /**
     * Возвращает уровень анизатропной фильтрации.
     */
    public int getAnisotropicFiltering() {
        return anisotropicFiltering;
    }

    /**
     * Возвращает спрайт по имени, если он входит в текущий текстурный атлас.
     * Иначе возвращается текстура-заглушка.
     */
    public AtlasIcon getAtlasIcon(String iconName) {
        AtlasIcon particleAtlasIcon = this.mapUploadedIcons.get(iconName);
        if (particleAtlasIcon == null) {
            particleAtlasIcon = this.missingImage;
        }

        return particleAtlasIcon;
    }

    public AnimatedAtlasIcon getAnimatedAtlasIcon(String iconName) {
        for (AnimatedAtlasIcon icon : listAnimatedIcons) {
            if (icon.getIconName().equals(iconName)) {
                return icon;
            }
        }

        throw new RuntimeException("AnimatedAtlasIcon with name:" + iconName + " not found.");
    }

    /**
     * Устанавливается уровень анизатропной фильтрации для атласа.
     * Атлас с этим уровнем анизатропной фильтрации будет создан только после вызова loadTextureAtlas().
     */
    public void setAnisotropicFiltering(int newAnisotropicFiltering) {
        this.anisotropicFiltering = newAnisotropicFiltering;
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {}

    @Override
    public void tick() {}

    @Override
    public IIcon registerIcon(String iconName) {
        if (iconName == null) {
            throw new IllegalArgumentException("Name cannot be null!");
        } else if (iconName.indexOf(92) == -1) {  // Disable backslashes (\) in texture asset paths.
            AtlasIcon textureAtlasSprite = this.mapRegisteredIcons.get(iconName);

            if (textureAtlasSprite == null) {
                textureAtlasSprite = new AtlasIcon(iconName);

                this.mapRegisteredIcons.put(iconName, textureAtlasSprite);
            }

            return textureAtlasSprite;
        } else {
            throw new IllegalArgumentException("Name cannot contain slashes!");
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
    private void loadAllIcons(IResourceManager resourceManager, Stitcher stitcher, int maximumTextureSize) {
        //сохранит количество текселей самой маленькой стороны спрайта среди всех спрайтов.

        //здесь происходит загрузка спрайтов.
        for (Map.Entry<String, AtlasIcon> entry : this.mapRegisteredIcons.entrySet()) {
            ResourceLocation resourcelocation = new ResourceLocation(entry.getKey());
            AtlasIcon textureAtlasSprite = entry.getValue();
            ResourceLocation completeResourceLocation = this.completeResourceLocation(resourcelocation);

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
     * В зависимости от номера мипмапа формирует путь до ресурса.
     * Путь до ресурса выглядит следующим образом:
     * <p>atlasPath + "/" + iconLocation + ".png"
     * <p>Примеры: "atlasPath/iconTexture.png".
     */
    private ResourceLocation completeResourceLocation(ResourceLocation iconLocation) {
        return new ResourceLocation(iconLocation.getResourceDomain(), String.format("%s/%s%s", this.atlasPath, iconLocation.getResourcePath(), ".png"));
    }

}

