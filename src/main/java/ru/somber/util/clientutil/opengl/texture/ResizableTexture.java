package ru.somber.util.clientutil.opengl.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

@SideOnly(Side.CLIENT)
public class ResizableTexture extends Texture {

    public ResizableTexture(int textureID) {
        super(textureID);
    }

    public ResizableTexture(int textureID, int width, int height) {
        super(textureID, width, height);
    }

    public ResizableTexture(int textureID, int width, int height, int formatColorGL, int internalFormatColorGL, int internalTypeGL) {
        super(textureID, width, height, formatColorGL, internalFormatColorGL, internalTypeGL);
    }

    public void updateSize(int width, int height) {
        if (width != getWidthTexture() ||
                height != getHeightTexture()) {

            Texture.recreateTexture(this, width, height);
        }
    }

    public static ResizableTexture createTexture(int width, int height) {
        int textureID = TextureUtil.glGenTextures();
        ResizableTexture texture = new ResizableTexture(textureID, width, height, GL11.GL_RGBA, GL11.GL_RGBA8, GL11.GL_UNSIGNED_BYTE);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, texture.getInternalFormatColorGL(),texture.getWidthTexture(),texture.getHeightTexture(), 0, texture.getFormatColorGL(), texture.getInternalTypeGL(), (ByteBuffer)null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return texture;
    }

}
