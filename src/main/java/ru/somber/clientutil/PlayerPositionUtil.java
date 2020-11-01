package ru.somber.clientutil;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import ru.somber.commonutil.SomberCommonUtil;

/**
 * Утилитный класс, вычисляющий позицию игрока, позицию камеры и смещения камеры относительно позиции игрока
 * (смещение камеры происходит, когда игрок входит в 3е лицо).
 */
public class PlayerPositionUtil {
    private static final PlayerPositionUtil instance = new PlayerPositionUtil();

    /** Позиция игрока. */
    private float xPlayer, yPlayer, zPlayer;
    /** Позиция камеры (позиция игрока + смещение камеры) */
    private float xCamera, yCamera, zCamera;
    /** Смещение камеры (в 3ем лице). */
    private float xOffsetCamera, yOffsetCamera, zOffsetCamera;


    private PlayerPositionUtil() { }


    public float getXPlayer() {
        return xPlayer;
    }

    public float getYPlayer() {
        return yPlayer;
    }

    public float getZPlayer() {
        return zPlayer;
    }


    public float getXCamera() {
        return xCamera;
    }

    public float getYCamera() {
        return yCamera;
    }

    public float getZCamera() {
        return zCamera;
    }


    public float getXOffsetCamera() {
        return xOffsetCamera;
    }

    public float getYOffsetCamera() {
        return yOffsetCamera;
    }

    public float getZOffsetCamera() {
        return zOffsetCamera;
    }


    /**
     * Обновляет позицию игрока, смещение камеры и позицию камеры.
     */
    public void updateRender(float interpolationFactor) {
        //вычисляем координаты головы игрока
        EntityLivingBase renderViewEntity = Minecraft.getMinecraft().renderViewEntity;
        xPlayer = SomberCommonUtil.interpolateBetween((float) renderViewEntity.lastTickPosX, (float) renderViewEntity.posX, interpolationFactor);
        yPlayer = SomberCommonUtil.interpolateBetween((float) renderViewEntity.lastTickPosY, (float) renderViewEntity.posY, interpolationFactor);
        zPlayer = SomberCommonUtil.interpolateBetween((float) renderViewEntity.lastTickPosZ, (float) renderViewEntity.posZ, interpolationFactor);

        //вычисляем смещение камеры, если оно есть
        computeCameraOffset();

        //смещаем камеру на вычисленное смещение
        xCamera = xPlayer + xOffsetCamera;
        yCamera = yPlayer + yOffsetCamera;
        zCamera = zPlayer + zOffsetCamera;
    }

    /**
     * Вычисляет смещения камеры относительно игрока.
     * Если камера от первого лица, то обнуляет смещения.
     * Если камера от третьего лиуа, то вычисляет смещение относительно головы игрока.
     */
    private void computeCameraOffset() {
        //обнуляем смещение
        xOffsetCamera = 0;
        yOffsetCamera = 0;
        zOffsetCamera = 0;

        Minecraft mc = Minecraft.getMinecraft();
        //здесь будем вычислять смещения для 3го лица, если включен режим 3го лица.
        if (mc.gameSettings.thirdPersonView > 0) {
            EntityLivingBase renderViewEntity = mc.renderViewEntity;

            //смещение камеры в режиме третьего лица в майнкрафте (длина вектора смещения)
            float cameraOffset = 4;
            //угол смещения вдоль оси У
            float yaw = renderViewEntity.rotationYaw;
            //угол смещения вдоль оси Х
            float pitch = renderViewEntity.rotationPitch;

            //поворот камеры на 180, если 2ой режим от третьего лица
            if (mc.gameSettings.thirdPersonView == 2) {
                pitch += 180.0F;
            }

            //вычисляем вектор lookAt камеры.
            float xCameraLookAt = -MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);
            float yCameraLookAt = -MathHelper.sin(pitch / 180.0F * (float) Math.PI);
            float zCameraLookAt = MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);

            //вектор lookAt длиной cameraOffset
            float xOffsetCameraTemp = xCameraLookAt * cameraOffset;
            float yOffsetCameraTemp = yCameraLookAt * cameraOffset;
            float zOffsetCameraTemp = zCameraLookAt * cameraOffset;

            //здесь проверяем сталкивается ли вектор lookAt камеры длиной cameraOffset с чем-то
            //если сталкивается, уменьшаем его длину.
            for (int k = 0; k < 8; k++) {
                float f3 = (float) ((k & 1) * 2 - 1);
                float f4 = (float) ((k >> 1 & 1) * 2 - 1);
                float f5 = (float) ((k >> 2 & 1) * 2 - 1);
                f3 *= 0.1F;
                f4 *= 0.1F;
                f5 *= 0.1F;
                MovingObjectPosition movingobjectposition =
                        mc.theWorld.rayTraceBlocks(
                                Vec3.createVectorHelper(xPlayer + f3, yPlayer + f4, zPlayer + f5),
                                Vec3.createVectorHelper(xPlayer - xOffsetCameraTemp + f3 + f5, yPlayer - yOffsetCameraTemp + f4, zPlayer - zOffsetCameraTemp + f5));

                if (movingobjectposition != null) {
                    float d6 = (float) movingobjectposition.hitVec.distanceTo(Vec3.createVectorHelper(xPlayer, yPlayer, zPlayer));

                    if (d6 < cameraOffset) {
                        cameraOffset = d6;
                    }
                }
            }

            //если 2ой режим от третьего лица, то реверсим вектор lookAt камеры
            if (mc.gameSettings.thirdPersonView == 2) {
                xOffsetCamera = -xOffsetCamera;
                yOffsetCamera = -yOffsetCamera;
                zOffsetCamera = -zOffsetCamera;
            }

            //вычисляем lookAt камеры длиной cameraOffset с учетом препятствий
            xOffsetCamera = xCameraLookAt * cameraOffset;
            yOffsetCamera = yCameraLookAt * cameraOffset;
            zOffsetCamera = zCameraLookAt * cameraOffset;

            xOffsetCamera = -xOffsetCamera;
            yOffsetCamera = -yOffsetCamera;
            zOffsetCamera = -zOffsetCamera;
        }
    }


    public static PlayerPositionUtil getInstance() {
        return instance;
    }

}
