package ru.somber.clientutil.opengl.vbo;

import org.lwjgl.opengl.GL15;

import java.nio.*;
import java.util.Objects;

/**
 * Класс для представления OpenGL-VBO.
 * В классе имеется набор методов для работы с буфером, повторяющих функционал OpenGL-функций,
 * но предназначенных для работы с объектами этого класса.
 */
public class VBO {
    /** Цель прикрепления буфера (GL_ARRAY_BUFFER). */
    private static final int BUFFER_TARGET = GL15.GL_ARRAY_BUFFER;

    /** ID буфера. */
    private final int vboID;

    /** Режим ипользования VBO. */
    private int vboUsage;
    /** Размер буфера в видеопамяти (в байтах!). */
    private long vboSizeByte;
    /** true - идентификатор буфера дейстительный и может использоваться для работы с буфером. false - буфер удален. */
    private boolean isCreated;
    /** true - под буфер выделена память в видеокарте, иначе false. */
    private boolean isInit;


    public VBO(int vboID) {
        this.vboID = vboID;
        this.vboUsage = -1;
        this.vboSizeByte = 0;
        this.isCreated = true;
        this.isInit = false;
    }

    public VBO(int vboID, int vboUsage) {
        this.vboID = vboID;
        this.vboUsage = vboUsage;
        this.vboSizeByte = 0;
        this.isCreated = true;
        this.isInit = false;
    }


    /**
     * Возвращает цель прикрепления буфера.
     */
    public int getTarget() {
        return BUFFER_TARGET;
    }

    /**
     * Возвращает ID буфера.
     */
    public int getID() {
        return vboID;
    }

    /**
     * Возвращает режим использования буфера. Если память под буфер не выделялась, то возвращается -1.
     * Возвращаемый режим может не отображать режим использования буфера, это просто локальное поле.
     */
    public int getVBOUsage() {
        return vboUsage;
    }

    /**
     * Возвращает размер буфера в видеопамяти (в байтах!).
     * Возвращаемый размер может не отображать фактический размер буфера, это просто локальное поле.
     */
    public long getVboSizeByte() {
        return vboSizeByte;
    }

    /**
     * true - идентификатор буфера дейстительный и может использоваться для работы с буфером.
     * false - буфер удален.
     */
    public boolean isCreated() {
        return isCreated;
    }

    /**
     * true - под буфер выделена память в видеокарте, иначе false.
     */
    public boolean isInit() {
        return isInit;
    }


    /**
     * Устанавлавает режим использования буфера.
     * <p>
     * Установленный режим не влияет на фактический режим работы буфера, это просто локальное поле!!!
     * <p>
     * Для фактического изменения режима буфера вызвать {@code bufferData(BufferObject, long, int)}
     */
    public void setVBOUsage(int vboUsage) {
        this.vboUsage = vboUsage;
    }

    /**
     * Устанавлавает размер буфера в видеопамяти (в байтах!).
     * <p>
     * Установленный размер не влияет на фактический размер буфера в видеопамяти, это просто локальное поле!!!
     * <p>
     * Для фактического изменения размера буфера вызвать {@code bufferData(BufferObject, long, int)}
     */
    public void setVboSizeByte(long vboSizeByte) {
        this.vboSizeByte = vboSizeByte;
    }


    /**
     * Биндит VBO.
     */
    public void bindBuffer() {
        GL15.glBindBuffer(VBO.BUFFER_TARGET, getID());
    }

    /**
     * Биндит нулевой VBO.
     */
    public void bindNone() {
        GL15.glBindBuffer(BUFFER_TARGET, 0);
    }


    /**
     * Вызывет glBufferData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferData(long byteDataSize, int usage) {
        GL15.glBufferData(VBO.BUFFER_TARGET, byteDataSize, usage);

        setVBOUsage(usage);
        setVboSizeByte(byteDataSize);
        isInit = true;
    }

    /**
     * Вызывет glBufferData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferData(ByteBuffer data, int usage) {
        GL15.glBufferData(VBO.BUFFER_TARGET, data, usage);

        setVBOUsage(usage);
        setVboSizeByte(data.remaining());
        isInit = true;
    }

    /**
     * Вызывет glBufferData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferData(ShortBuffer data, int usage) {
        GL15.glBufferData(VBO.BUFFER_TARGET, data, usage);

        setVBOUsage(usage);
        setVboSizeByte(data.remaining() * 2);
        isInit = true;
    }

    /**
     * Вызывет glBufferData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferData(IntBuffer data, int usage) {
        GL15.glBufferData(VBO.BUFFER_TARGET, data, usage);

        setVBOUsage(usage);
        setVboSizeByte(data.remaining() * 4);
        isInit = true;
    }

    /**
     * Вызывет glBufferData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferData(FloatBuffer data, int usage) {
        GL15.glBufferData(VBO.BUFFER_TARGET, data, usage);

        setVBOUsage(usage);
        setVboSizeByte(data.remaining() * 4);
        isInit = true;
    }

    /**
     * Вызывет glBufferData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferData(DoubleBuffer data, int usage) {
        GL15.glBufferData(VBO.BUFFER_TARGET, data, usage);

        setVBOUsage(usage);
        setVboSizeByte(data.remaining() * 8);
        isInit = true;
    }


    /**
     * Вызывет glBufferSubData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferSubData(long byteOffset, ByteBuffer data) {
        GL15.glBufferSubData(VBO.BUFFER_TARGET, byteOffset, data);
    }

    /**
     * Вызывет glBufferSubData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferSubData(long byteOffset, ShortBuffer data) {
        GL15.glBufferSubData(VBO.BUFFER_TARGET, byteOffset, data);
    }

    /**
     * Вызывет glBufferSubData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferSubData(long byteOffset, IntBuffer data) {
        GL15.glBufferSubData(VBO.BUFFER_TARGET, byteOffset, data);
    }

    /**
     * Вызывет glBufferSubData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferSubData(long byteOffset, FloatBuffer data) {
        GL15.glBufferSubData(VBO.BUFFER_TARGET, byteOffset, data);
    }

    /**
     * Вызывет glBufferSubData для vbo с заданными параметрами.
     * Перед использованием вызвать {@code bindBuffer()} для этого же объекта.
     */
    public void bufferSubData(long byteOffset, DoubleBuffer data) {
        GL15.glBufferSubData(VBO.BUFFER_TARGET, byteOffset, data);
    }


    /**
     * Удаляет из видеопамяти VBO.
     * Поля переданного буферного объекта будут обращены в -1 как недействительные.
     * Перед удалением биндит нулевой буфер.
     */
    public void deleteVBO() {
        GL15.glBindBuffer(VBO.BUFFER_TARGET, 0);
        GL15.glDeleteBuffers(vboID);

        isCreated = false;
        isInit = false;
        setVBOUsage(-1);
        setVboSizeByte(-1);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VBO that = (VBO) o;
        return vboID == that.vboID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(BUFFER_TARGET, vboID);
    }

    @Override
    public String toString() {
        return "VBO{" +
                "bufferTarget=" + BUFFER_TARGET +
                ", bufferID=" + vboID +
                ", bufferSizeByte=" + vboSizeByte +
                '}';
    }


    /**
     * Создает VBO.
     */
    public static VBO createVBO() {
        int vboID = GL15.glGenBuffers();
        VBO newBuffer = new VBO(vboID);
        newBuffer.setVBOUsage(GL15.GL_STATIC_DRAW);

        return newBuffer;
    }

    /**
     * Создает VBO c переданным режимом использования.
     */
    public static VBO createVBO(int vboUsage) {
        int vboID = GL15.glGenBuffers();
        VBO newBuffer = new VBO(vboID);
        newBuffer.setVBOUsage(vboUsage);

        return newBuffer;
    }

}
