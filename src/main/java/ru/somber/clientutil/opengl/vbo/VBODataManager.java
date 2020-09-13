package ru.somber.clientutil.opengl.vbo;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Objects;

/**
 * Менеджер данных VBO.
 * Хранит VBO с FloatBuffer'ом (в котором хранятся данные VBO), а также данные для управление размеров VBO и соответствующего буфера данных.
 * Обеспечивает обновление размером VBO и связанного FloatBuffer'a в соответствии
 * с размером новых буферов, количеством попыток обновлений, интервала между фактическими обновлениями и коэффициента расширения буферов.
 */
public class VBODataManager implements Comparable<VBODataManager> {
    /** Сам vbo. */
    private final VBO vbo;
    /** Интервал количества обновлений между обновленими размеров буферов. */
    private final int intervalCountUpdates;

    /** Связанный с VBO буфер данных. */
    private FloatBuffer dataBuffer;
    /** Коэффициент расширения. (Т.е. во столько раз размер буфера будет больше требуемого.) */
    private float expansionFactor;
    /** Размер буфера при последнем обновлении размеров буферов. */
    private long lastUpdateVBOSize;
    /** Номер последенего обновления размеров буферов. */
    private int lastCountUpdatesSize;
    /** Текущее количество попыток обновлений. */
    private int currentCountUpdates;

    /**
     * @param vbo vbo, для которого создается менеджер. Не должен быть null.
     * @param dataBuffer связанный с vbo FloatBuffer. Не должен быть null.
     * @param intervalCountUpdates интервал количества обновлений между обновленими размеров буферов. Не должно быть меньше 1.
     * @param expansionFactor коэффициент расширения. (Т.е. во столько раз размер буфера будет больше требуемого при обновлении размеров.)
     *                        Выставлять в большие значения (> 1.5), если размеры буферов часто меняются, чтобы снизить количество выделений памяти.
     *                        Не должен быть меньше 1.
     */
    public VBODataManager(VBO vbo, FloatBuffer dataBuffer, int intervalCountUpdates, float expansionFactor) {
        if (vbo == null || dataBuffer == null || expansionFactor < 1 || intervalCountUpdates < 1) {
            throw new RuntimeException();
        }

        this.vbo = vbo;
        this.intervalCountUpdates = intervalCountUpdates;

        this.dataBuffer = dataBuffer;
        this.expansionFactor = expansionFactor;
        this.lastUpdateVBOSize = vbo.getVboSizeByte();
        this.lastCountUpdatesSize = 0;
        this.currentCountUpdates = 0;
    }


    /**
     * Возвращает vbo.
     */
    public VBO getVbo() {
        return vbo;
    }

    /**
     * Возвращает связанный с vbo буфер данных.
     */
    public FloatBuffer getDataBuffer() {
        return dataBuffer;
    }

    /**
     * Возвращает интервал количества обновлений между обновлениями размеров памяти.
     * Т.е. если количество обновлений превышает данное число, а требуемый размер буфера меньше, чем при последнем обновлении, то буфер уменьшается.
     * Нужно для своевременного сжимания буфера, чтобы экономить память.
     */
    public int getIntervalCountUpdates() {
        return intervalCountUpdates;
    }

    /**
     * Возвращает по сути текущий размер vbo, т.е. последний изменившийся размер буфера.
     */
    public long getLastUpdateVBOSize() {
        return lastUpdateVBOSize;
    }

    /**
     * Возвращает коэффициент расширения буфера.
     */
    public float getExpansionFactor() {
        return expansionFactor;
    }

    /**
     * Возвращает номер последнего обновления, при котором размер буферов изменился.
     */
    public int getLastCountUpdatesSize() {
        return lastCountUpdatesSize;
    }


    /**
     * Устанавливает коэффициент расширения буфера.
     */
    public void setExpansionFactor(float expansionFactor) {
        if (expansionFactor < 1) {
            throw new RuntimeException("expansionFactor should be equal or greater than 1: " + expansionFactor);
        }

        this.expansionFactor = expansionFactor;
    }


    /**
     * Производит !попытку! обновления размера буфера.
     * Если переданный размер больше размера буфера, то обновление происходит немедленно.
     * Если переданный размер меньше размера буфера, то обновление размера буфера происходит только,
     * если с последнего фактического обновления размера прошло попыток обновлений больше, чет интервал между обновлениями.
     * Т.е. уменьшение буфера происходит раз в некоторое количество попыток обновления для того, чтобы уменьшить количество перераспределения памяти.
     *
     * @param newSize новый размер буфера.
     */
    public void updateSize(long newSize) {
        currentCountUpdates++;

        if ((newSize > lastUpdateVBOSize) ||
                (((currentCountUpdates - lastCountUpdatesSize) >= intervalCountUpdates) && (((float) lastUpdateVBOSize / newSize) > (expansionFactor + 0.05F)))) {

            lastUpdateVBOSize = (long) (newSize * expansionFactor);
            dataBuffer = BufferUtils.createFloatBuffer((int) (lastUpdateVBOSize));

            vbo.bindBuffer();
            vbo.bufferData(lastUpdateVBOSize * 4, vbo.getVBOUsage());
            vbo.bindNone();

            this.lastCountUpdatesSize = currentCountUpdates;
        }
    }


    /**
     * Для чего здесь нужен метод сравнения?
     * На всяких случай, если объекты этого класса потребуется хранить в структурах, где требуеся данный метод.
     */
    @Override
    public int compareTo(VBODataManager otherManager) {
        return Integer.compare(vbo.getID(), otherManager.getVbo().getID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VBODataManager that = (VBODataManager) o;
        return Objects.equals(vbo, that.vbo) &&
                Objects.equals(dataBuffer, that.dataBuffer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vbo, dataBuffer);
    }

    @Override
    public String toString() {
        return "VBODataManager{" +
                "vbo=" + vbo +
                ", intervalCountUpdates=" + intervalCountUpdates +
                ", dataBuffer=" + dataBuffer +
                ", expansionFactor=" + expansionFactor +
                ", lastUpdateVBOSize=" + lastUpdateVBOSize +
                ", lastCountUpdatesSize=" + lastCountUpdatesSize +
                ", currentCountUpdates=" + currentCountUpdates +
                '}';
    }

}
