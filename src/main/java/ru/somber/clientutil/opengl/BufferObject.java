package ru.somber.clientutil.opengl;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL31;

import java.nio.*;
import java.util.Objects;

/**
 * Класс для представления OpenGL-буферного объекта (допустим вершенный буфер (vbo) или юниформ-буфер (ubo)).
 * В классе имеется набор статических методов для работы с буфером, повторяющих функционал OpenGL-функций,
 * но предназначенных для работы с объектами этого класса.
 */
public class BufferObject {
    /** Цель прикрепления буфера (допустим GL_ARRAY_BUFFER). */
    private int bufferTarget;
    /** ID буфера. */
    private int bufferID;
    /** Размер буфера в видеопамяти (в байтах!). */
    private long bufferSizeByte;


    public BufferObject(int bufferTarget, int bufferID) {
        this.bufferTarget = bufferTarget;
        this.bufferID = bufferID;
        this.bufferSizeByte = 0;
    }

    /**
     * Возвращает цель прикрепления буфера.
     */
    public int getTarget() {
        return bufferTarget;
    }

    /**
     * Возвращает ID буфера.
     */
    public int getID() {
        return bufferID;
    }

    /**
     * Возвращает размер буфера в видеопамяти (в байтах!).
     * Возвращаемый размер может не отображать фактический размер буфера, это просто локальная переменная.
     */
    public long getBufferSizeByte() {
        return bufferSizeByte;
    }


    /**
     * Возвращает цель прикрепления буфера.
     */
    public void setTarget(int bufferTarget) {
        this.bufferTarget = bufferTarget;
    }

    /**
     * Устанавлавает размер буфера в видеопамяти (в байтах!).
     * <p>
     * Установленный размер не влияет на фактический размер буфера в видеопамяти, это просто локальная переменная!!!
     * <p>
     * Для фактического изменения размера буфера вызвать {@code bufferData(BufferObject, long, int)}
     */
    public void setBufferSizeByte(long bufferSizeByte) {
        this.bufferSizeByte = bufferSizeByte;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BufferObject that = (BufferObject) o;
        return bufferTarget == that.bufferTarget &&
                bufferID == that.bufferID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bufferTarget, bufferID);
    }


    @Override
    public String toString() {
        return "OpenGLBuffer{" +
                "bufferTarget=" + bufferTarget +
                ", bufferID=" + bufferID +
                ", bufferSizeByte=" + bufferSizeByte +
                '}';
    }



    /**
     * Создает буферный объект с заданным целевым прикреплением.
     */
    public static BufferObject createBuffer(int bufferTarget) {
        int bufferID = GL15.glGenBuffers();
        BufferObject newBuffer = new BufferObject(bufferTarget, bufferID);

        return newBuffer;
    }

    /**
     * Создает буферный объект с целевым прикреплением GL_ARRAY_BUFFER.
     */
    public static BufferObject createVBO() {
        return createBuffer(GL15.GL_ARRAY_BUFFER);
    }

    /**
     * Создает буферный объект с целевым прикреплением GL_UNIFORM_BUFFER.
     */
    public static BufferObject createUBO() {
        return createBuffer(GL31.GL_UNIFORM_BUFFER);
    }


    /**
     * Биндит переданный буферный объект.
     */
    public static void bindBuffer(BufferObject buffer) {
        GL15.glBindBuffer(buffer.getTarget(), buffer.getID());
    }

    /**
     * Биндит нулевой буферный объект с целевым прикреплением как у переданного буфера.
     */
    public static void bindNone(BufferObject buffer) {
        GL15.glBindBuffer(buffer.getTarget(), 0);
    }


    /* Тут вообще нужно что то расписывать? */

    public static void bufferData(BufferObject buffer, long dataSize, int usage) {
        GL15.glBufferData(buffer.bufferTarget, dataSize, usage);
        buffer.setBufferSizeByte(dataSize);
    }

    public static void bufferData(BufferObject buffer, ByteBuffer data, int usage) {
        GL15.glBufferData(buffer.bufferTarget, data, usage);
        buffer.setBufferSizeByte(data.remaining());
    }

    public static void bufferData(BufferObject buffer, ShortBuffer data, int usage) {
        GL15.glBufferData(buffer.bufferTarget, data, usage);
        buffer.setBufferSizeByte(data.remaining() * 2);
    }

    public static void bufferData(BufferObject buffer, IntBuffer data, int usage) {
        GL15.glBufferData(buffer.bufferTarget, data, usage);
        buffer.setBufferSizeByte(data.remaining() * 4);
    }

    public static void bufferData(BufferObject buffer, FloatBuffer data, int usage) {
        GL15.glBufferData(buffer.bufferTarget, data, usage);
        buffer.setBufferSizeByte(data.remaining() * 4);
    }

    public static void bufferData(BufferObject buffer, DoubleBuffer data, int usage) {
        GL15.glBufferData(buffer.bufferTarget, data, usage);
        buffer.setBufferSizeByte(data.remaining() * 8);
    }


    public static void bufferSubData(BufferObject buffer, long byteOffset, ByteBuffer data) {
        GL15.glBufferSubData(buffer.bufferTarget, byteOffset, data);
    }

    public static void bufferSubData(BufferObject buffer, long byteOffset, ShortBuffer data) {
        GL15.glBufferSubData(buffer.bufferTarget, byteOffset, data);
    }

    public static void bufferSubData(BufferObject buffer, long byteOffset, IntBuffer data) {
        GL15.glBufferSubData(buffer.bufferTarget, byteOffset, data);
    }

    public static void bufferSubData(BufferObject buffer, long byteOffset, FloatBuffer data) {
        GL15.glBufferSubData(buffer.bufferTarget, byteOffset, data);
    }

    public static void bufferSubData(BufferObject buffer, long byteOffset, DoubleBuffer data) {
        GL15.glBufferSubData(buffer.bufferTarget, byteOffset, data);
    }


    /**
     * Удаляет из видеопамяти переданный буферный объект.
     * Поля переданного буферного объекта будут обращены в -1 как недействительные.
     */
    public static void deleteBuffer(BufferObject buffer) {
        GL15.glDeleteBuffers(buffer.bufferID);

        buffer.bufferTarget = -1;
        buffer.bufferID = -1;
        buffer.bufferSizeByte = -1;
    }

}
