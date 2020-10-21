package ru.somber.clientutil.camera;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;

import java.nio.FloatBuffer;

public class CameraPositionUtil {

    private static final CameraPositionUtil instance = new CameraPositionUtil();
    private float x, y, z;
    private float xOffset, yOffset, zOffset;
    private Matrix4f cameraMatrix;
    private FloatBuffer cameraMatrixBuffer;


    private CameraPositionUtil() {
        cameraMatrix = new Matrix4f();
        cameraMatrixBuffer = BufferUtils.createFloatBuffer(16);
    }


    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void updateTick() {




    }

    public void updateRender() {
        cameraMatrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, cameraMatrixBuffer);
        cameraMatrix.load(cameraMatrixBuffer);

        EntityLivingBase renderEntity = Minecraft.getMinecraft().renderViewEntity;
        if (renderEntity != null) {
            float xEntity = (float) renderEntity.posX;
            float yEntity = (float) renderEntity.posY;
            float zEntity = (float) renderEntity.posZ;
        }

        xOffset = cameraMatrix.m30;
        yOffset = cameraMatrix.m31;
        zOffset = cameraMatrix.m32;

    }

    public static CameraPositionUtil getInstance() {
        return instance;
    }
}
