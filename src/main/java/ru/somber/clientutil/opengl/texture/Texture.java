package ru.somber.clientutil.opengl.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class Texture {
    private int textureID;
    private int widthTexture;
    private int heightTexture;
    private int formatColorGL;
    private int internalFormatColorGL;
    private int internalTypeGL;


    public Texture(int textureID) {
        this.textureID = textureID;
        widthTexture = -1;
        heightTexture = -1;
        formatColorGL = GL11.GL_RGBA;
        internalFormatColorGL = GL11.GL_RGBA8;
        internalTypeGL = GL11.GL_UNSIGNED_BYTE;
    }

    public Texture(int textureID, int width, int height) {
        this(textureID);
        this.widthTexture = width;
        this.heightTexture = height;
    }

    public Texture(int textureID, int width, int height, int formatColorGL, int internalFormatColorGL, int internalTypeGL) {
        this(textureID, width, height);
        this.formatColorGL = formatColorGL;
        this.internalFormatColorGL = internalFormatColorGL;
        this.internalTypeGL = internalTypeGL;
    }


    public int getTextureID() {
        return textureID;
    }

    public int getWidthTexture() {
        return widthTexture;
    }

    public int getHeightTexture() {
        return heightTexture;
    }

    public int getFormatColorGL() {
        return formatColorGL;
    }

    public int getInternalFormatColorGL() {
        return internalFormatColorGL;
    }

    public int getInternalTypeGL() {
        return internalTypeGL;
    }


    public void setTextureID(int textureID) {
        this.textureID = textureID;
    }

    public void setWidthTexture(int widthTexture) {
        this.widthTexture = widthTexture;
    }

    public void setHeightTexture(int heightTexture) {
        this.heightTexture = heightTexture;
    }

    public void setFormatColorGL(int formatColorGL) {
        this.formatColorGL = formatColorGL;
    }

    public void setInternalFormatColorGL(int internalFormatColorGL) {
        this.internalFormatColorGL = internalFormatColorGL;
    }

    public void setInternalTypeGL(int internalTypeGL) {
        this.internalTypeGL = internalTypeGL;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Texture texture = (Texture) o;
        return textureID == texture.textureID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureID);
    }

    @Override
    public String toString() {
        return "Texture{" +
                "textureID=" + textureID +
                ", widthTexture=" + widthTexture +
                ", heightTexture=" + heightTexture +
                '}';
    }


    public static Texture createTexture(int width, int height) {
        int textureID = TextureUtil.glGenTextures();
        Texture texture = new Texture(textureID, width, height, GL11.GL_RGBA, GL11.GL_RGBA8, GL11.GL_UNSIGNED_BYTE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, texture.getInternalFormatColorGL(),texture.getWidthTexture(),texture.getHeightTexture(), 0, texture.getFormatColorGL(), texture.getInternalTypeGL(), (ByteBuffer)null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return texture;
    }

    public static void recreateTexture(Texture texture, int width, int height) {
        texture.setWidthTexture(width);
        texture.setHeightTexture(height);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getTextureID());
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, texture.getInternalFormatColorGL(),texture.getWidthTexture(),texture.getHeightTexture(), 0, texture.getFormatColorGL(), texture.getInternalTypeGL(), (ByteBuffer)null);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
//        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

}
