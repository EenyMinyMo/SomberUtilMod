package ru.somber.clientutil.opengl.vbo;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

/**
 * Класс, связывающий VBO (в лице VBODataManager) и атрибуты вершин (glVertexAttribPointer допустим).
 * В классе хранятся данные атрибутов вершин.
 */
public class VertexAttribVBO {
    /** Менеджер VBO, в котором хранится VBO. */
    private VBODataManager vboDataManager;

    /** Индекс атрибута вершин. */
    private int attributeIndex;
    /** Размер одного атрибута вершин (допустм для одного атрибута требуется 3 float'а). */
    private int attributeSize;
    /** Тип атрибута вершин. Сейчас поддерживается только GL_FLOAT из-за VBODataManager. */
    private int attributeType;
    /** Шаг смещения в буфере между отдельными атрибутами. */
    private int attributeStride;
    /** Смещения начала рамзещения атрибутов в буфере. */
    private int attributeOffset;
    /** Делитель изъятия атрибутов из буфера. Нужен для шейдеров на основе инстансов. */
    private int vertexAttribDivisor;


    public VertexAttribVBO(VBODataManager vboDataManager,
                           int attributeIndex,
                           int attributeSize) {

        this.vboDataManager = vboDataManager;

        this.attributeIndex = attributeIndex;
        this.attributeSize = attributeSize;
        this.attributeType = GL11.GL_FLOAT;
        this.attributeStride = 0;
        this.attributeOffset = 0;
        this.vertexAttribDivisor = 0;
    }


    /**
     * Биндит VBO и вызывает glVertexAttribPointer и glVertexAttribDivisor с данными из этого класса.
     */
    public void setVertexAttribPointerVBO() {
        vboDataManager.getVbo().bindBuffer();
        GL20.glVertexAttribPointer(attributeIndex, attributeSize, attributeType, false, attributeStride, attributeOffset);
    }

    /**
     * Вызывает попытку обновления размеров vbo и связанного буфера данных через обновление размера VBODataManager'а.
     */
    public void allocateVBO(int countAttributes) {
        countAttributes *= attributeSize;
        vboDataManager.updateSize(countAttributes);
    }


    /**
     * Включает атрибуты вершин по индексу объекта.
     */
    public void enableVertexAttribArray() {
        GL20.glEnableVertexAttribArray(attributeIndex);
    }

    /**
     * Выключает атрибуты вершин по индексу объекта.
     */
    public void disableVertexAttribArray() {
        GL20.glDisableVertexAttribArray(attributeIndex);
    }

    /**
     * Включает деление атрибуты вершин по индексу объекта.
     */
    public void enableVertexAttribDivisor() {
        GL33.glVertexAttribDivisor(attributeIndex, vertexAttribDivisor);
    }

    /**
     * Выключает деление атрибуты вершин по индексу объекта.
     */
    public void disableVertexAttribDivisor() {
        GL33.glVertexAttribDivisor(attributeIndex, 0);
    }


    public VBO getVbo() {
        return vboDataManager.getVbo();
    }

    public VBODataManager getVboDataManager() {
        return vboDataManager;
    }

    public int getAttributeIndex() {
        return attributeIndex;
    }

    public int getAttributeSize() {
        return attributeSize;
    }

    public int getAttributeType() {
        return attributeType;
    }

    public int getAttributeStride() {
        return attributeStride;
    }

    public int getAttributeOffset() {
        return attributeOffset;
    }

    public int getVertexAttribDivisor() {
        return vertexAttribDivisor;
    }


    public void setVboDataManager(VBODataManager vboDataManager) {
        this.vboDataManager = vboDataManager;
    }

    public void setAttributeIndex(int attributeIndex) {
        this.attributeIndex = attributeIndex;
    }

    public void setAttributeSize(int attributeSize) {
        this.attributeSize = attributeSize;
    }

    public void setAttributeType(int attributeType) {
        this.attributeType = attributeType;
    }

    public void setAttributeStride(int attributeStride) {
        this.attributeStride = attributeStride;
    }

    public void setAttributeOffset(int attributeOffset) {
        this.attributeOffset = attributeOffset;
    }

    public void setVertexAttribDivisor(int vertexAttribDivisor) {
        this.vertexAttribDivisor = vertexAttribDivisor;
    }

}
