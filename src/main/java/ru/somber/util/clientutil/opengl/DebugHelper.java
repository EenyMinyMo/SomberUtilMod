package ru.somber.util.clientutil.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.opengl.KHRDebugCallback;

import java.nio.IntBuffer;

public final class DebugHelper {
    private DebugHelper() {}


    private static boolean isEnable = false;


    public static boolean isEnable() {
        return isEnable;
    }

    public static void enable() {
        GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT);
        GL11.glEnable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);
        KHRDebug.glDebugMessageCallback(new KHRDebugCallback());

        IntBuffer idsBuffer = BufferUtils.createIntBuffer(1);
        idsBuffer.put(0).flip();
        KHRDebug.glDebugMessageControl(GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, GL11.GL_DONT_CARE, idsBuffer, true);

        isEnable = true;
    }

    public static void disable() {
        GL11.glDisable(KHRDebug.GL_DEBUG_OUTPUT);
        GL11.glDisable(KHRDebug.GL_DEBUG_OUTPUT_SYNCHRONOUS);

        isEnable = false;
    }

}
