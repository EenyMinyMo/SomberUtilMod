package ru.somber.clientutil.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.*;

public class VBODataManager {

    private Map<BufferObject, VBOEntry> vboEntryMap;


    public VBODataManager() {
        vboEntryMap = new HashMap<>(64);
    }

    public void addVBO(BufferObject vbo, FloatBuffer dataBuffer, int vboUsage, int intervalTimeUpdate, float expansionFactor) {
        if (vbo == null || vbo.getTarget() != GL15.GL_ARRAY_BUFFER) {
            throw new RuntimeException();
        }

        VBOEntry vboEntry = new VBOEntry(vbo, dataBuffer, vboUsage, intervalTimeUpdate, expansionFactor, 0);

        vboEntryMap.put(vbo, vboEntry);
    }

    public VBOEntry getEntry(BufferObject vbo) {
        return vboEntryMap.get(vbo);
    }

    public FloatBuffer getDataBuffer(BufferObject vbo) {
        return vboEntryMap.get(vbo).getDataBuffer();
    }

    public void remove(BufferObject vbo) {
        vboEntryMap.remove(vbo);
    }

    public boolean contains(BufferObject vbo) {
        return vboEntryMap.containsKey(vbo);
    }

    public boolean contains(BufferObject vbo, FloatBuffer dataBuffer) {
        FloatBuffer buff = vboEntryMap.get(vbo).getDataBuffer();

        return buff != null && buff.equals(dataBuffer);
    }


    public static class VBOEntry implements Comparable<VBOEntry> {
        private final BufferObject vbo;
        private final int vboUsage;
        private final int intervalTimeUpdate;

        private FloatBuffer dataBuffer;
        private long lastUpdateVBOSize;
        private float expansionFactor;
        private int lastUpdateTime;
        private int currentUpdateTime;

        public VBOEntry(BufferObject vbo, FloatBuffer dataBuffer, int vboUsage, int intervalTimeUpdate, float expansionFactor, int currentTime) {
            if (vbo == null || expansionFactor < 1 || intervalTimeUpdate < 1) {
                throw new RuntimeException();
            }

            if (dataBuffer == null) {
                dataBuffer = BufferUtils.createFloatBuffer(0);
            }

            this.vbo = vbo;
            this.vboUsage = vboUsage;
            this.intervalTimeUpdate = intervalTimeUpdate;
            this.dataBuffer = dataBuffer;
            this.expansionFactor = expansionFactor;
            this.lastUpdateTime = currentTime;

            this.lastUpdateVBOSize = vbo.getBufferSizeByte();
            this.currentUpdateTime = 0;
        }


        public BufferObject getVbo() {
            return vbo;
        }

        public FloatBuffer getDataBuffer() {
            return dataBuffer;
        }

        public int getIntervalTimeUpdate() {
            return intervalTimeUpdate;
        }

        public long getLastUpdateVBOSize() {
            return lastUpdateVBOSize;
        }

        public float getExpansionFactor() {
            return expansionFactor;
        }

        public int getLastUpdateTime() {
            return lastUpdateTime;
        }

        public int getVboUsage() {
            return vboUsage;
        }


        public void setExpansionFactor(float expansionFactor) {
            if (expansionFactor < 1) {
                throw new RuntimeException("expansionFactor should be equal or greater than 1: " + expansionFactor);
            }

            this.expansionFactor = expansionFactor;
        }

        public void setLastUpdateTime(int lastUpdateTime) {
            this.lastUpdateTime = lastUpdateTime;
        }


        public void updateSize(long newSize) {
            currentUpdateTime++;

            if ((newSize > lastUpdateVBOSize) ||
                (((currentUpdateTime - lastUpdateTime) >= intervalTimeUpdate) && (((float) lastUpdateVBOSize / newSize) > (expansionFactor + 0.05F)))) {

                lastUpdateVBOSize = (long) (newSize * expansionFactor);
                dataBuffer = BufferUtils.createFloatBuffer((int) (lastUpdateVBOSize));

                BufferObject.bindBuffer(vbo);
                BufferObject.bufferData(vbo, lastUpdateVBOSize * 4, vboUsage);
                BufferObject.bindNone(vbo);

                this.lastUpdateTime = currentUpdateTime;
            }
        }


        @Override
        public int compareTo(VBOEntry o) {
            return Integer.compare(vbo.getID(), o.vbo.getID());
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VBOEntry VBOEntry = (VBOEntry) o;

            return Objects.equals(vbo, VBOEntry.vbo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(vbo);
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "vbo=" + vbo +
                    ", intervalTimeUpdate=" + intervalTimeUpdate +
                    ", dataBuffer=" + dataBuffer +
                    ", lastUpdateVBOSize=" + lastUpdateVBOSize +
                    ", arrayExpansionFactor=" + expansionFactor +
                    ", lastUpdateTime=" + lastUpdateTime +
                    ", vboUsage=" + vboUsage +
                    '}';
        }

    }

}
