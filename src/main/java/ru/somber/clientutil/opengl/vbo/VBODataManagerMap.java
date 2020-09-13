package ru.somber.clientutil.opengl.vbo;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Отображение VBO с VBODataManager'ми для хранения нескольких vbo одном месте.
 */
public class VBODataManagerMap {

    private final Map<VBO, VBODataManager> vboManagersMap;


    public VBODataManagerMap() {
        vboManagersMap = new HashMap<>(64);
    }


    /**
     * Добавляет VBO с соответствующим буфером на хранение и обновление размеров (оба не должны быть null).
     * Для обновления размеров также передаются дополнительные данные:
     * @param intervalTimeUpdate интервал времени между обновленими размеров.
     *                           Данный интервал учитывается только, если размер нужно уменьшать.
     *                           Не должен быть меньше 1.
     * @param expansionFactor коэффициент расширения буфера.
     *                        При обновлении размера буфера размер будет на соответственный коэффициент увеличиваться.
     *                        Не должен быть меньше 1.
     */
    public void addVBODataManager(VBO vbo, FloatBuffer dataBuffer, int intervalTimeUpdate, float expansionFactor) {
        VBODataManager vboDataManager = new VBODataManager(vbo, dataBuffer, intervalTimeUpdate, expansionFactor);
        vboManagersMap.put(vbo, vboDataManager);
    }

    public void addVBODataManager(VBODataManager dataManager) {
        if (dataManager == null) {
            throw new RuntimeException();
        }

        vboManagersMap.put(dataManager.getVbo(), dataManager);
    }

    /**
     * Возвращает vbo-менеджер для переданного vbo.
     */
    public VBODataManager getDataManager(VBO vbo) {
        return vboManagersMap.get(vbo);
    }

    /**
     * Возвращает FloatBuffer, соответствующий переданному vbo.
     */
    public FloatBuffer getDataBuffer(VBO vbo) {
        return vboManagersMap.get(vbo).getDataBuffer();
    }

    /**
     * Удаляет переданный vbo с отображения.
     */
    public void remove(VBO vbo) {
        vboManagersMap.remove(vbo);
    }

    /**
     * Возвращает true, если переданный vbo содержится в отображении.
     */
    public boolean contains(VBO vbo) {
        return vboManagersMap.containsKey(vbo);
    }

    /**
     * Возвращает количество записей в отображении.
     */
    public int getCountDataManagers() {
        return vboManagersMap.size();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VBODataManagerMap that = (VBODataManagerMap) o;
        return Objects.equals(vboManagersMap, that.vboManagersMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vboManagersMap);
    }

    @Override
    public String toString() {
        return "VBODataManagerMap{" +
                "vboManagersMap=" + vboManagersMap +
                '}';
    }

}
