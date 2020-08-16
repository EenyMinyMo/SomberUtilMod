package ru.somber.clientutil.opengl.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureCoordAABB {

    private float[] coords;

    public TextureCoordAABB() {
        //дефолтные координаты
        coords = new float[] {
                0.5F, 0.5F,
                0.5F, 0.5F
        };
    }

    public TextureCoordAABB(float centerX, float centerY, float halfWidth, float halfHeight) {
        coords = new float[8];

        coords[0] = centerX;
        coords[1] = centerY;
        coords[2] = halfWidth;
        coords[3] = halfHeight;
    }


    public float getCenterX() {
        return coords[0];
    }

    public float getCenterY() {
        return coords[1];
    }

    public float getHalfWidth() {
        return coords[2];
    }

    public float getHalfHeight() {
        return coords[3];
    }


    public void getCoords(float[] coord) {
        System.arraycopy(this.coords, 0, coord, 0, 4);
    }

    public float[] getCoords() {
        return this.coords;
    }


    public void setCenterX(float coordX_0) {
        coords[0] = coordX_0;
    }

    public void setCenterY(float coordY_0) {
        coords[1] = coordY_0;
    }

    public void setHalfWidth(float coordX_1) {
        coords[2] = coordX_1;
    }

    public void setHalfHeight(float coordY_1) {
        coords[3] = coordY_1;
    }
    

    public void setCoords(float[] coords) {
        System.arraycopy(coords, 0, this.coords, 0, 4);
    }

}
