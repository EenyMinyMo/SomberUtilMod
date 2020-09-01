package ru.somber.commonutil;

import net.minecraft.entity.Entity;
import org.lwjgl.util.vector.Vector3f;

public final class SomberUtils {
    private SomberUtils() {}

    /** number error - погрешность числа. */
    public static final double NUMBER_ERROR_1 = 0.1,
            NUMBER_ERROR_2 = 0.01,
            NUMBER_ERROR_4 = 0.0001,
            NUMBER_ERROR_6 = 0.000001,
            NUMBER_ERROR_8 = 0.00000001,
            NUMBER_ERROR_10 = 0.0000000001,
            NUMBER_ERROR_12 = 0.000000000001,
            NUMBER_ERROR_14 = 0.00000000000001,
            NUMBER_ERROR_16 = 0.0000000000000001,
            NUMBER_ERROR_18 = 0.000000000000000001;

    public static int getExp2MoreThanNumber(int number) {
        int exp = 1;
        while (exp < number) {
            exp *= 2;
        }
        return exp;
    }


    public static int timeToTick(int hours, int minute, float second) {
        int ticks = 0;
        ticks += hours * 60 * 60 * 20;
        ticks += minute * 60 * 20;
        ticks += (int) (second * 20);
        return ticks;
    }


    public static float interpolateBetween(float bottom, float top, float interpolationFactor) {
        return bottom + (top - bottom) * interpolationFactor;
    }

    public static double interpolateBetween(double bottom, double top, float interpolationFactor) {
        return bottom + (top - bottom) * interpolationFactor;
    }


    public static float interpolateMoveX(Entity entity, float interpolationFactor) {
        return (float) (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * interpolationFactor);
    }

    public static float interpolateMoveY(Entity entity, float interpolationFactor) {
        return (float) (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * interpolationFactor);
    }

    public static float interpolateMoveZ(Entity entity, float interpolationFactor) {
        return (float) (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * interpolationFactor);
    }

    public static Vector3f interpolateMove(Entity entity, float interpolationFactor) {
        float x = (float) (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * interpolationFactor);
        float y = (float) (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * interpolationFactor);
        float z = (float) (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * interpolationFactor);

        return new Vector3f(x, y, z);
    }

    public static void interpolateMove(Vector3f destination, Entity entity, float interpolationFactor) {
        float x = (float) (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * interpolationFactor);
        float y = (float) (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * interpolationFactor);
        float z = (float) (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * interpolationFactor);

        destination.set(x, y, z);
    }


}
