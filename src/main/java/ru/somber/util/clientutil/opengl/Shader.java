package ru.somber.util.clientutil.opengl;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class Shader {
    private final int shaderType;
    private final int shaderID;
    private String sourceCode;


    public Shader(int shaderType) {
        this(shaderType, GL20.glCreateShader(shaderType), null);
    }

    public Shader(int shaderType, int shaderID) {
        this(shaderType, shaderID, null);
    }

    public Shader(int shaderType, String sourceCode) {
        this(shaderType, GL20.glCreateShader(shaderType), sourceCode);
    }

    public Shader(int shaderType, int shaderID, String sourceCode) {
        this.shaderType = shaderType;
        this.shaderID = shaderID;
        this.sourceCode = sourceCode;
    }


    public int getShaderID() {
        return shaderID;
    }

    public int getShaderType() {
        return shaderType;
    }

    public String getSourceCode() {
        return sourceCode;
    }


    public boolean compileShader() {
        GL20.glShaderSource(shaderID, sourceCode);
        GL20.glCompileShader(shaderID);

        return getCompileStatus() == GL11.GL_TRUE;
    }

    public void deleteShader() {
        if (isCompile()) {
            GL20.glDeleteShader(shaderID);
        }
    }

    public boolean isCompile() {
        return getCompileStatus() == GL11.GL_TRUE;
    }

    public void checkError() {
        if (! isCompile()) {
            System.out.println(getInfoLog());
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


    protected void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    protected int getCompileStatus() {
        int compileStatus = GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS);
        return compileStatus;
    }

    protected String getInfoLog() {
        int messageLength = GL20.glGetShaderi(shaderID, GL20.GL_INFO_LOG_LENGTH);
        String message = GL20.glGetShaderInfoLog(shaderID, messageLength);
        return message;
    }


    public static Shader createShaderObject(int shaderType, ResourceLocation sourceCodeLocation) {
        Shader shader = new Shader(shaderType);

        try {
            shader.setSourceCode(OpenGLUtil.loadShaderCode(sourceCodeLocation));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        shader.compileShader();
        shader.checkError();

        return shader;
    }

}
