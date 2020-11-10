package ru.somber.util.clientutil.opengl;

import org.lwjgl.opengl.GL30;

import java.util.Objects;

/**
 * Класс для представления OpenGL - vertex array object.
 * В классе имеется набор статических методов для работы с объектом, повторяющих функционал OpenGL-функций
 */
public class VAO {
    /** ID объекта. */
    private int vaoID;

    public VAO(int vaoID) {
        this.vaoID = vaoID;
    }

    public int getID() {
        return vaoID;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VAO vao = (VAO) o;
        return vaoID == vao.vaoID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(vaoID);
    }


    @Override
    public String toString() {
        return "VAO{" +
                "vaoID=" + vaoID +
                '}';
    }


    /**
     * Создает vertex array object.
     */
    public static VAO createVAO() {
        int vaoID = GL30.glGenVertexArrays();
        VAO newVAO = new VAO(vaoID);

        return newVAO;
    }

    /**
     * Биндит переданный vertex array object.
     */
    public static void bindVAO(VAO vao) {
        GL30.glBindVertexArray(vao.getID());
    }

    /**
     * Биндит нулевой vertex array object.
     */
    public static void bindNone() {
        GL30.glBindVertexArray(0);
    }

    /**
     * Удаляет из видеопамяти переданный vertex array object.
     * Поля переданного объекта будут обращены в -1 как недействительные.
     */
    public static void deleteVAO(VAO vao) {
        GL30.glDeleteVertexArrays(vao.getID());
        vao.vaoID = -1;
    }

}
