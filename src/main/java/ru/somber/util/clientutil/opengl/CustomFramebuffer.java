package ru.somber.util.clientutil.opengl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import ru.somber.util.clientutil.opengl.texture.Texture;

import java.nio.IntBuffer;
import java.util.*;

@SideOnly(Side.CLIENT)
public class CustomFramebuffer {
    /** ID буфера (что тут не понятно?). */
    private int framebufferID;
    /** Ширина буфера кадра. */
    private int width;
    /** Высота буфера кадра. */
    private int height;
    /**
     * Фильтр для текстур фреймбуфера. Нужно, чтобы задавать этот фильтр текстурам в этом фреймбуфере. (В частности при их добавлении или при ресайзе всего фреймбуфера.)
     * Вообще использование этого фильтра стоит под сомнением, но пусть будет.
     * Рассмотреть возможность удаления этой переменной.
     */
    private int framebufferFilter;
    /**
     * Цвет очистки буфера кадра.
     * [0] - red, [1] - green, [2] - blue, [3] - alpha.
     */
    private float[] clearColor;

    /**
     * Флаг для определения используется ли буфер глубины в буфере.
     * true - используется, иначе false.
     */
    private boolean isUsedDepth;
    /**
     * ID буфера глубины. Он равен -1, если isUsedDepth = false или буфер не создан.
     */
    private int depthBufferID;

    /**
     * Флаг для определения создан ли фактически буфер.
     * true - создан, иначе false.
     */
    private boolean isAlive;

    /**
     * Мапа формата "Integer, Texture", где Integer - номер слота прикрепления текстуры (допустим COLOR_ATTACHMENT_N),
     * Texture - объект соответствуеющего класса, где хранятся данные об этой текстуре.
     */
    private Map<Integer, Texture> attachmentsTexture;


    public CustomFramebuffer(int width, int height, boolean isUsedDepth) {
        this.framebufferID = -1;
        this.width = width;
        this.height = height;
        this.framebufferFilter = GL11.GL_NEAREST;
        this.clearColor = new float[] {1.0F, 1.0F, 1.0F, 1.0F};

        this.isUsedDepth = isUsedDepth;
        this.depthBufferID = -1;

        this.attachmentsTexture = new HashMap<>();

        initBuffer();
    }

    private void initBuffer() {
        if (isAlive()) {
            throw new IllegalStateException("Буфер уже создан!");
        }
        setAlive(true);

        this.framebufferID = GL30.glGenFramebuffers();
        bindFramebuffer(true);

        if (isUsedDepth) {
            depthBufferID = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBufferID);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBufferID);
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        }

        checkFramebufferComplete();
        setFramebufferFilter(framebufferFilter);
        framebufferClear();

        unbindFramebuffer();
    }

    public int getFramebufferID() {
        return framebufferID;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void attachTexture(int attachSlot, Texture texture) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        GL30.glBindFramebuffer(OpenGlHelper.field_153198_e, framebufferID);
        GL30.glFramebufferTexture2D(OpenGlHelper.field_153198_e, attachSlot, GL11.GL_TEXTURE_2D, texture.getTextureID(), 0);
        GL30.glBindFramebuffer(OpenGlHelper.field_153198_e, 0);

        attachmentsTexture.put(attachSlot, texture);

        setFramebufferFilter(framebufferFilter);
    }

    public void detachTexture(int attachSlot) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachSlot, GL11.GL_TEXTURE_2D, 0, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        attachmentsTexture.remove(attachSlot);
    }

    public Texture getFramebufferTexture(int attachSlot) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        return attachmentsTexture.get(attachSlot);
    }

    public void bindFramebufferTexture(int attachSlot) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        Texture texture = attachmentsTexture.get(attachSlot);
        int textureID = (texture == null) ? 0 : texture.getTextureID();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
    }

    public float[] getClearColor() {
        return new float[] { clearColor[0], clearColor[1], clearColor[2], clearColor[3]};
    }

    public void setClearColor(float red, float green, float blue, float alpha) {
        clearColor[0] = red;
        clearColor[1] = green;
        clearColor[2] = blue;
        clearColor[3] = alpha;
    }

    public int getFramebufferFilter() {
        return framebufferFilter;
    }

    public void setFramebufferFilter(int filter) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }
        this.framebufferFilter = filter;

        attachmentsTexture.forEach((colorAttachment, texture) -> {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
        });
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void bindFramebuffer(boolean rewriteViewportSize) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebufferID);

        if (rewriteViewportSize) {
            GL11.glViewport(0, 0, this.width, this.height);
        }
    }

    public void unbindFramebuffer() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void resizeFramebuffer(int width, int height) {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        this.width = width;
        this.height = height;

        bindFramebuffer(true);
        if (isUsedDepth()) {
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, 0);
            GL30.glDeleteRenderbuffers(depthBufferID);
            depthBufferID = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBufferID);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBufferID);
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        }

        attachmentsTexture.forEach((attachSlot, texture) -> {
            Texture.recreateTexture(texture, width, height);

            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
            OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, framebufferID);
            OpenGlHelper.func_153188_a(OpenGlHelper.field_153198_e, attachSlot, GL11.GL_TEXTURE_2D, texture.getTextureID(), 0);
            OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, 0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        });

        checkFramebufferComplete();

        setFramebufferFilter(framebufferFilter);
        framebufferClear();
    }

    public void framebufferClear() {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        this.bindFramebuffer(true);

        setDefaultAllDrawBuffer();

        GL11.glClearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
        int clearBufferBit = GL11.GL_COLOR_BUFFER_BIT;

        if (this.isUsedDepth) {
            GL11.glClearDepth(1.0);
            clearBufferBit |= GL11.GL_DEPTH_BUFFER_BIT;
        }

        GL11.glClear(clearBufferBit);

        setOneDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);

        this.unbindFramebuffer();
    }

    public void deleteFramebuffer() {
        if (! isAlive()) {
            throw new IllegalStateException("Буфер еще не создан!");
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL30.glDeleteFramebuffers(framebufferID);
        framebufferID = -1;

        if (isUsedDepth()) {
            GL30.glDeleteRenderbuffers(depthBufferID);
            depthBufferID = -1;
        }

        attachmentsTexture.forEach((colorAttachment, texture) -> {
            TextureUtil.deleteTexture(texture.getTextureID());
        });
        attachmentsTexture.clear();

        setAlive(false);
    }

    public void checkFramebufferComplete() {
        int i = OpenGlHelper.func_153167_i(OpenGlHelper.field_153198_e);

        if (i != OpenGlHelper.field_153202_i) {
            if (i == OpenGlHelper.field_153203_j) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
            } else if (i == OpenGlHelper.field_153204_k) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
            } else if (i == OpenGlHelper.field_153205_l) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
            } else if (i == OpenGlHelper.field_153206_m) {
                throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
            } else {
                throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
            }
        }
    }

    public boolean isUsedDepth() {
        return isUsedDepth;
    }

    public boolean isAlive() {
        return isAlive;
    }


    /**
     * Сюда пихать int'ы, которые ест GL20.glDrawBuffers()!!!!!!!!!
     * (аля GL_COLOR_ATTACHMENT_N)
     */
    public void setOneDrawBuffer(int attachSlot) {
        IntBuffer drawBuff = BufferUtils.createIntBuffer(1);
        drawBuff.put(attachSlot);
        drawBuff.flip();
        GL20.glDrawBuffers(drawBuff);
    }

    /**
     * Сюда пихать int'ы, которые ест GL20.glDrawBuffers()!!!!!!!!!
     * (аля GL_COLOR_ATTACHMENT_N)
     */
    public void setArrayDrawBuffer(int[] attachSlots) {
        IntBuffer drawBuff = BufferUtils.createIntBuffer(attachSlots.length);
        drawBuff.put(attachSlots);
        drawBuff.flip();
        GL20.glDrawBuffers(drawBuff);
    }

    public void setFirstLowDrawBuffer() {
        if (attachmentsTexture.isEmpty()) {
            return;
        }

        List<Integer> attachSlots = new ArrayList<>(attachmentsTexture.keySet());
        attachSlots.sort(Comparator.comparingInt(n -> n));
        setOneDrawBuffer(attachSlots.get(0));
    }

    public void setDefaultAllDrawBuffer() {
        if (attachmentsTexture.isEmpty()) {
            return;
        }

        List<Integer> attachSlots = new ArrayList<>(attachmentsTexture.keySet());
        attachSlots.sort(Comparator.comparingInt(n -> n));
        int[] arr = new int[attachSlots.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = attachSlots.get(i);
        }

        setArrayDrawBuffer(arr);
    }

    public void drawColorInTextureAttachSlot(int attachSlot, float r, float g, float b, float a) {
        Texture texture = getFramebufferTexture(attachSlot);
        if (texture == null) {
            throw new RuntimeException("Текстура в attachSlot " + attachSlot + " отсутсвует.");
        }

        bindFramebuffer(true);
        setOneDrawBuffer(attachSlot);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);

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
        GL11.glDisable(GL11.GL_CULL_FACE);

        setFirstLowDrawBuffer();
        unbindFramebuffer();
    }

    public void drawTextureInTextureAttachSlot(int attachSlot, Texture texture) {
        Texture textureAttachSlot = getFramebufferTexture(attachSlot);
        if (textureAttachSlot == null) {
            throw new RuntimeException("Текстура в attachSlot " + attachSlot + " отсутсвует.");
        }

        bindFramebuffer(true);
        setOneDrawBuffer(attachSlot);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPushMatrix();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorRGBA_F(1, 1, 1, 1);
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(-1, -1, 0.2F, 0, 0);
        tessellator.addVertexWithUV(1, -1, 0.2F, 1, 0);
        tessellator.addVertexWithUV(1, 1, 0.2F, 1, 1);
        tessellator.addVertexWithUV(-1, 1, 0.2F, 0, 1);
        tessellator.draw();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        setFirstLowDrawBuffer();
        unbindFramebuffer();
    }


    protected void setAlive(boolean alive) {
        this.isAlive = alive;
    }

}
