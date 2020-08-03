package ru.somber.clientutil.opengl.texture;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TextureCoordSeparator {

    private int countRow;
    private int countColumn;
    private int countSubTexture;

    private float widthOnSub;
    private float heightOnSub;

    public TextureCoordSeparator(int countRow, int countColumn) {
        this.countRow = countRow;
        this.countColumn = countColumn;
        this.countSubTexture = countRow * countColumn;
        this.widthOnSub = 1.0F / countColumn;
        this.heightOnSub = 1.0F / countRow;
    }

    public int getCountRow() {
        return countRow;
    }

    public int getCountColumn() {
        return countColumn;
    }

    public int getCountSubTexture() {
        return countSubTexture;
    }

    public float getWidthOnSub() {
        return widthOnSub;
    }

    public float getHeightOnSub() {
        return heightOnSub;
    }

    public TextureCoord getCoordForRowColumn(int row, int column) {
        float xMin = (column + 0.0F) / countColumn;
        float xMax = (column + 1.0F) / countColumn;

        float yMax = (row + 0.0F) / countRow;
        float yMin = (row + 1.0F) / countRow;
        //Почему мы к максу прибавляем, чтобы получить минимум? А потому шо майнкрафт.
        //(текстура в памяти перевернута по оси У (там 0 у У находится сверху, в опенгл же снизу))

        return new TextureCoord(xMin, yMin, xMax, yMin, xMax, yMax, xMin, yMax);
    }

    public TextureCoord getCoordForNumber(int number) {
        number %= countSubTexture;

        int row = number / countColumn;
        int column = number % countColumn;

        return getCoordForRowColumn(row, column);
    }

}
