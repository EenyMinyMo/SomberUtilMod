package ru.somber.clientutil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.nio.FloatBuffer;

/**
 * Утилиты для клиентской стороны.
 */
@SideOnly(Side.CLIENT)
public final class SomberClientUtils {
    private SomberClientUtils() {}

    /**
     * 00 01
     * 10 11
     */
    public static float getElementFromMatrix22(FloatBuffer fb, int row, int column) {
        return fb.get(column * 2 + row);
    }

    /**
     * 00 01 12
     * 10 11 12
     * 20 21 22
     */
    public static float getElementFromMatrix33(FloatBuffer fb, int row, int column) {
        return fb.get(column * 3 + row);
    }

    /**
     * 00 01 02 03
     * 10 11 12 13
     * 20 21 22 23
     * 30 31 32 33
     */
    public static float getElementFromMatrix44(FloatBuffer fb, int row, int column) {
        return fb.get(column * 4 + row);
    }

    public static void printMatrix44(FloatBuffer fb) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                System.out.print(fb.get(x * 4 + y) + " ");
            }
            System.out.println();
        }
    }

}
