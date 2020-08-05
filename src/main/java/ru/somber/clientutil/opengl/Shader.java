package ru.somber.clientutil.opengl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class Shader {
    private int shaderType;
    private int shaderID;
    private String sourceCode;

    public Shader(int shaderType) {
        this.shaderType = shaderType;
        this.shaderID = GL20.glCreateShader(shaderType);
    }

    public Shader(int shaderType, int shaderID) {
        this.shaderType = shaderType;
        this.shaderID = shaderID;
    }

    public Shader(int shaderType, String sourceCode) {
        this.shaderType = shaderType;
        this.shaderID = GL20.glCreateShader(shaderType);
        this.sourceCode = sourceCode;
    }

    public Shader(int shaderType, int shaderID, String sourceCode) {
        this.shaderType = shaderType;
        this.shaderID = shaderID;
        this.sourceCode = sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public boolean compileShader() {
        GL20.glShaderSource(shaderID, sourceCode);
        GL20.glCompileShader(shaderID);

        return getCompileStatus() == GL11.GL_TRUE;
    }

    public int getCompileStatus() {
        int compileStatus = GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS);
        return compileStatus;
    }

    public boolean isCompile() {
        return getCompileStatus() == GL11.GL_TRUE;
    }

    public String getInfoLog() {
        int messageLength = GL20.glGetShaderi(shaderID, GL20.GL_INFO_LOG_LENGTH);
        String message = GL20.glGetShaderInfoLog(shaderID, messageLength);
        return message;
    }

    public int getShaderType() {
        return shaderType;
    }

    public int getShaderID() {
        return shaderID;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void deleteShader() {
        GL20.glDeleteShader(shaderID);
    }

    public void printInfoLogMessage() {
        if (! isCompile()) {
            System.out.println(getInfoLog());
        }
    }

    public void checkError() {
        if (! isCompile()) {
            printInfoLogMessage();
            throw new RuntimeException("Compile shader error " + toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shader shader = (Shader) o;
        return shaderType == shader.shaderType &&
                shaderID == shader.shaderID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shaderType, shaderID);
    }

    @Override
    public String toString() {
        return "Shader{" +
                "shaderType=" + shaderType +
                ", shaderID=" + shaderID +
                ", sourceCode='" + sourceCode + '\'' +
                '}';
    }

}
