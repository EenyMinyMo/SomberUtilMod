package ru.somber.clientutil.opengl;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;

public class VertexAttribVBO {
    private int attributeIndex;
    private int attributeSize;
    private int attributeType;
    private int attributeStride;
    private int attributeOffset;
    private int vertexAttribDivisor;

    private BufferObject vbo;
    private VBODataManager vboDataManager;
    private int vboUsage;

    private FloatBuffer vboBuffer;


    public VertexAttribVBO(int attributeIndex,
                           int attributeSize,
                           int attributeType,
                           BufferObject vbo,
                           FloatBuffer vboBuffer,
                           VBODataManager vboDataManager,
                           int vboUsage) {

        this.attributeIndex = attributeIndex;
        this.attributeSize = attributeSize;
        this.attributeType = attributeType;

        this.vbo = vbo;
        this.vboDataManager = vboDataManager;
        this.vboUsage = vboUsage;

        this.vboBuffer = vboBuffer;
    }

    public void addVBOInVBODataManager(int intervalTimeUpdate, float expansionFactor) {
        vboDataManager.addVBO(vbo, vboBuffer, vboUsage, intervalTimeUpdate, expansionFactor);
    }

    public void setVertexAttribPointerVBO() {
        BufferObject.bindBuffer(vbo);
        GL20.glVertexAttribPointer(attributeIndex, attributeSize, attributeType, false, attributeStride, attributeOffset);
    }

    public void allocateVBO(int countVertex) {
        countVertex *= attributeSize;
        vboDataManager.getEntry(vbo).updateSize(countVertex);
        vboBuffer = vboDataManager.getDataBuffer(vbo);
    }

    public void bufferSubData(long byteOffset) {
        BufferObject.bindBuffer(vbo);
        BufferObject.bufferSubData(vbo, byteOffset, vboBuffer);
    }

    public void bufferSubData(long byteOffset, FloatBuffer data) {
        BufferObject.bindBuffer(vbo);
        BufferObject.bufferSubData(vbo, byteOffset, data);
    }

    public void enableVertexAttribArray() {
        GL20.glEnableVertexAttribArray(attributeIndex);
    }

    public void disableVertexAttribArray() {
        GL20.glDisableVertexAttribArray(attributeIndex);
    }

    public void enableVertexAttribDivisor() {
        GL33.glVertexAttribDivisor(attributeIndex, vertexAttribDivisor);
    }

    public void disableVertexAttribDivisor() {
        GL33.glVertexAttribDivisor(attributeIndex, 0);
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

    public BufferObject getVbo() {
        return vbo;
    }

    public FloatBuffer getVboBuffer() {
        return vboBuffer;
    }

    public VBODataManager getVboDataManager() {
        return vboDataManager;
    }

    public int getVboUsage() {
        return vboUsage;
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

    public void setVbo(BufferObject vbo) {
        this.vbo = vbo;
    }

    public void setVboDataManager(VBODataManager vboDataManager) {
        this.vboDataManager = vboDataManager;
    }

    public void setVboUsage(int vboUsage) {
        this.vboUsage = vboUsage;
    }

    public void setVboBuffer(FloatBuffer vboBuffer) {
        this.vboBuffer = vboBuffer;
    }

}
