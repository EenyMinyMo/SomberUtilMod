package ru.somber.clientutil.opengl;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;

public class VertexAttribVBO {
    private final int attributeIndex;
    private final int attributeSize;
    private final int attributeType;
    private final int attributeStride;
    private final int attributeOffset;
    private final int vertexAttribDivisor;

    private final BufferObject vbo;
    private final VBODataManager vboDataManager;
    private final int vboUsage;

    private FloatBuffer vboBuffer;


    public VertexAttribVBO(int attributeIndex,
                           int attributeSize,
                           int attributeType,
                           int attributeStride,
                           int attributeOffset,
                           int vertexAttribDivisor,
                           BufferObject vbo,
                           FloatBuffer vboBuffer,
                           VBODataManager vboDataManager,
                           int vboUsage) {

        this.attributeIndex = attributeIndex;
        this.attributeSize = attributeSize;
        this.attributeType = attributeType;
        this.attributeStride = attributeStride;
        this.attributeOffset = attributeOffset;
        this.vertexAttribDivisor = vertexAttribDivisor;

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


    public void setVboBuffer(FloatBuffer vboBuffer) {
        this.vboBuffer = vboBuffer;
    }

}
