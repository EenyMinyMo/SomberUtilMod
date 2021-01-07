package ru.somber.util.clientutil.opengl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.*;

@SideOnly(Side.CLIENT)
public class ShaderProgram {
    private final int shaderProgramID;
    private List<Shader> attachShaders;
    private Map<String, Integer> uniformMap;


    public ShaderProgram() {
        shaderProgramID = GL20.glCreateProgram();
        attachShaders = new ArrayList<>();
        uniformMap = new HashMap<>();
    }


    public int getShaderProgramID() {
        return shaderProgramID;
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

    public Shader[] getAttachShaders() {
        return attachShaders.toArray(new Shader[0]);
    }


    public void setUniforms(String ... uniformNames) {
        int uniformLocation;

        uniformMap.clear();
        for (String name : uniformNames) {
            uniformLocation = GL20.glGetUniformLocation(shaderProgramID, name);
            if (uniformLocation == -1) {
                throw new RuntimeException("Uniform " + name + " not found");
            }

            uniformMap.put(name, uniformLocation);
        }
    }

    public int getUniformLocation(String uniformName) {
        return uniformMap.getOrDefault(uniformName, -1);
    }


    public boolean linkProgram() {
        GL20.glLinkProgram(shaderProgramID);
        return isLink();
    }

    public boolean isLink() {
        return getLinkStatus() == GL11.GL_TRUE;
    }

    public void deleteProgram() {
        GL20.glDeleteProgram(shaderProgramID);
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
                ", uniformMap=" + uniformMap +
                '}';
    }


    protected int getLinkStatus() {
        int linkStatus = GL20.glGetProgrami(shaderProgramID, GL20.GL_LINK_STATUS);
        return linkStatus;
    }

    protected String getInfoLog() {
        int messageLength = GL20.glGetProgrami(shaderProgramID, GL20.GL_INFO_LOG_LENGTH);
        String message = GL20.glGetProgramInfoLog(shaderProgramID, messageLength);
        return message;
    }

    protected void printInfoLogMessage() {
        if (! isLink()) {
            System.out.println(getInfoLog());
        }
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
