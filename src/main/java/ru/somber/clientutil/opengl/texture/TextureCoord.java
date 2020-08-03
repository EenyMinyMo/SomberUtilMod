package ru.somber.clientutil.opengl.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureCoord {

    private float[] coords;

    public TextureCoord() {
        //дефолтные координаты
        coords = new float[] {
                0, 0,
                1, 0,
                1, 1,
                0, 1 };
    }

    public TextureCoord(float coordX_0, float coordY_0, float coordX_1, float coordY_1, float coordX_2, float coordY_2, float coordX_3, float coordY_3) {
        coords = new float[8];

        coords[0] = coordX_0;
        coords[2] = coordX_1;
        coords[4] = coordX_2;
        coords[6] = coordX_3;

        coords[1] = coordY_0;
        coords[3] = coordY_1;
        coords[5] = coordY_2;
        coords[7] = coordY_3;
    }

    public float getCoordX_0() {
        return coords[0];
    }

    public float getCoordY_0() {
        return coords[1];
    }

    public float getCoordX_1() {
        return coords[2];
    }

    public float getCoordY_1() {
        return coords[3];
    }

    public float getCoordX_2() {
        return coords[4];
    }

    public float getCoordY_2() {
        return coords[5];
    }

    public float getCoordX_3() {
        return coords[6];
    }

    public float getCoordY_3() {
        return coords[7];
    }

    public float[] getCoords() {
        return new float[] {coords[0], coords[1], coords[2], coords[3], coords[4], coords[5], coords[6], coords[7]};
    }


    public void setCoordX_0(float coordX_0) {
        coords[0] = coordX_0;
    }

    public void setCoordY_0(float coordY_0) {
        coords[1] = coordY_0;
    }

    public void setCoordX_1(float coordX_1) {
        coords[2] = coordX_1;
    }

    public void setCoordY_1(float coordY_1) {
        coords[3] = coordY_1;
    }

    public void setCoordX_2(float coordX_2) {
        coords[4] = coordX_2;
    }

    public void setCoordY_2(float coordY_2) {
        coords[5] = coordY_2;
    }

    public void setCoordX_3(float coordX_3) {
        coords[6] = coordX_3;
    }

    public void setCoordY_3(float coordY_3) {
        coords[7] = coordY_3;
    }

    public void setCoords(float[] coords) {
        this.coords[0] = coords[0];
        this.coords[1] = coords[1];
        this.coords[2] = coords[2];
        this.coords[3] = coords[3];
        this.coords[4] = coords[4];
        this.coords[5] = coords[5];
        this.coords[6] = coords[6];
        this.coords[7] = coords[7];
    }

}
