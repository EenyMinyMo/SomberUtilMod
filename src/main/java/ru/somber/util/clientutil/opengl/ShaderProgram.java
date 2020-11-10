package ru.somber.util.clientutil.opengl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ShaderProgram {

    private int shaderProgramID;
    private List<Shader> attachShaders;

    public ShaderProgram() {
        shaderProgramID = GL20.glCreateProgram();
        attachShaders = new ArrayList<>();
    }

    public boolean attachShader(Shader shader) {
        if (! shader.isCompile())
            return false;

        GL20.glAttachShader(shaderProgramID, shader.getShaderID());
        attachShaders.add(shader);
        return true;
    }

    public void detachShader(Shader shader) {
        GL20.glDetachShader(shaderProgramID, shader.getShaderID());
        attachShaders.remove(shader);
    }

    public void detachAllShader() {
        attachShaders.forEach((Shader shader) -> { GL20.glDetachShader(shaderProgramID, shader.getShaderID()); });
    }

    public boolean linkProgram() {
        GL20.glLinkProgram(shaderProgramID);
        return isLink();
    }

    public int getLinkStatus() {
        int linkStatus = GL20.glGetProgrami(shaderProgramID, GL20.GL_LINK_STATUS);
        return linkStatus;
    }

    public boolean isLink() {
        return getLinkStatus() == GL11.GL_TRUE;
    }

    public String getInfoLog() {
        int messageLength = GL20.glGetProgrami(shaderProgramID, GL20.GL_INFO_LOG_LENGTH);
        String message = GL20.glGetProgramInfoLog(shaderProgramID, messageLength);
        return message;
    }

    public Shader[] getAttachShaders() {
        return attachShaders.toArray(new Shader[0]);
    }

    public int getShaderProgramID() {
        return shaderProgramID;
    }

    public void deleteProgram() {
        GL20.glDeleteProgram(shaderProgramID);
    }

    public void printInfoLogMessage() {
        if (! isLink()) {
            System.out.println(getInfoLog());
        }
    }

    public void checkError() {
        if (! isLink()) {
            printInfoLogMessage();
            throw new RuntimeException("Compile shader error " + toString());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShaderProgram that = (ShaderProgram) o;
        return shaderProgramID == that.shaderProgramID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shaderProgramID);
    }

    @Override
    public String toString() {
        return "ShaderProgram{" +
                "shaderProgramID=" + shaderProgramID +
                ", attachShaders=" + attachShaders +
                '}';
    }


    public static ShaderProgram createShaderProgram(Shader... attachShaders) {
        ShaderProgram shaderProgram = new ShaderProgram();

        for (Shader shader : attachShaders) {
            shaderProgram.attachShader(shader);
        }

        shaderProgram.linkProgram();
        shaderProgram.checkError();

        return shaderProgram;
    }

    public static void useShaderProgram(ShaderProgram shaderProgram) {
        GL20.glUseProgram(shaderProgram.getShaderProgramID());
    }

    public static void useNoneShaderProgram() {
        GL20.glUseProgram(0);
    }

}
