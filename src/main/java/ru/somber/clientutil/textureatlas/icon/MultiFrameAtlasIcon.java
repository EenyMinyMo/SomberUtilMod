package ru.somber.clientutil.textureatlas.icon;

/**
 * Класс для представления мультифреймовых иконок.
 * Правила прописывания имени иконки аналогичные AtlasIcon.
 *
 * <p> Для деления текстуры на отдельные фреймы используются следующие поля:
 * <p> countFrameColumn - количество колонок с фреймами.
 * <p> countFrameRow - количество рядов с фреймами.
 * <p> Общее количество фреймов высчитывается так: countFrameColumn * countFrameRow.
 *
 * <p> Процесс выборки данных из мультифреймовой иконки происходит следующим образом:
 * Фреймы берутся в порядке слева направо, сверху вниз. Т.е. левый верхний фрейм - нулевой, нижний правый фрейм - последний.
 */
public class MultiFrameAtlasIcon extends AtlasIcon {

    /** Количество колонок фреймов. */
    private final int countFrameColumn;
    /** Количество строк фреймов. */
    private final int countFrameRow;
    /** Общее количество фреймов. */
    private final int countFrames;
    /** Массив AtlasIcon, где хранятся отдельные фреймы текстуры. */
    private final AtlasIcon[] frameIcons;


    public MultiFrameAtlasIcon(String iconName, int countFrameColumn, int countFrameRow, boolean isInverted) {
        super(iconName, isInverted);

        this.countFrameColumn = countFrameColumn;
        this.countFrameRow = countFrameRow;
        this.countFrames = countFrameColumn * countFrameRow;

        frameIcons = new AtlasIcon[countFrames];
    }


    @Override
    public void initIcon(int width, int height, int originX, int originY, boolean rotated) {
        super.initIcon(width, height, originX, originY, rotated);

        int frameWidth = getIconWidth() / countFrameColumn;
        int frameHeight = getIconHeight() / countFrameRow;
        for (int i = 0; i < countFrames; i++) {
            int currentFrameColumn = i % countFrameColumn;
            int currentFrameRow = i / countFrameColumn;

//            float animatedMinU = super.getMinU() + (currentFrameColumn + 0.0F) / countFrameColumn * (super.getMaxU() - super.getMinU());
//            float animatedMaxU = super.getMinU() + (currentFrameColumn + 1.0F) / countFrameColumn * (super.getMaxU() - super.getMinU());
//
//            float animatedMinV = super.getMinV() + (currentFrameRow + 0.0F) / countFrameRow * (super.getMaxV() - super.getMinV());
//            float animatedMaxV = super.getMinV() + (currentFrameRow + 1.0F) / countFrameRow * (super.getMaxV() - super.getMinV());
//
//            if (isInvertedY()) {
//                animatedMinV = super.getMinV() + (currentFrameRow + 1.0F) / countFrameRow * (super.getMaxV()- super.getMinV());
//                animatedMaxV = super.getMinV() + (currentFrameRow + 0.0F) / countFrameRow * (super.getMaxV()- super.getMinV());
//            }


            AtlasIcon iconFrame = new AtlasIcon(getIconName() + "_frame_" + i, true);
            iconFrame.initIcon(frameWidth, frameHeight, currentFrameColumn * width + getOriginX(), currentFrameRow * height + getOriginX(), isRotated());

            frameIcons[i] = iconFrame;
        }

    }

    /**
     * Возвращает общее количество фреймов.
     */
    public int getCountFrames() {
        return countFrames;
    }

    /**
     * Возвращает количество колонок фреймов.
     */
    public int getCountFrameColumn() {
        return countFrameColumn;
    }

    /**
     * Возвращает количество строк фреймов.
     */
    public int getCountFrameRow() {
        return countFrameRow;
    }

    /**
     * Возвращает AtlasIcon отдельного фрейма по номеру фрейма.
     */
    public AtlasIcon getFrameIcon(int frameNumber) {
        frameNumber %= countFrames;
        return frameIcons[frameNumber];
    }

    @Override
    public boolean isMultiFramesIcon() {
        return true;
    }



}
