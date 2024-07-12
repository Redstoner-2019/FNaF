package me.redstoner2019.chatgptbs;

import me.redstoner2019.Util;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class TextureExample {

    private int shaderProgram;
    private int vao;
    private int textureID;

    public void init() {
        String vertexShaderSource = null; // Load vertex.glsl as a string
        String fragmentShaderSource = null;
        try {
            vertexShaderSource = Util.readFile("shader/vertex.vert");
            fragmentShaderSource = Util.readFile("shader/fragment.frag");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        shaderProgram = createShaderProgram(vertexShaderSource, fragmentShaderSource);
        vao = createVertexArray();
        textureID = loadTexture("C:\\Users\\Redstoner_2019\\Projects\\LWJGL\\src\\main\\resources\\textures/optadata.jpg");
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT);
        render(shaderProgram, vao, textureID);
    }

    public void cleanup() {
        GL20.glDeleteProgram(shaderProgram);
        GL30.glDeleteVertexArrays(vao);
        GL11.glDeleteTextures(textureID);
    }

    private int compileShader(int type, String source) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            System.err.println(GL20.glGetShaderInfoLog(shader));
            throw new RuntimeException("Shader compilation failed");
        }
        return shader;
    }

    public int createShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        int shaderProgram = GL20.glCreateProgram();
        GL20.glAttachShader(shaderProgram, vertexShader);
        GL20.glAttachShader(shaderProgram, fragmentShader);
        GL20.glLinkProgram(shaderProgram);
        if (GL20.glGetProgrami(shaderProgram, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            System.err.println(GL20.glGetProgramInfoLog(shaderProgram));
            throw new RuntimeException("Program linking failed");
        }

        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);

        return shaderProgram;
    }

    public int loadTexture(String filePath) {
        int textureID;
        try (MemoryStack stack = stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = STBImage.stbi_load(filePath, width, height, channels, 4);
            if (image == null) {
                throw new RuntimeException("Failed to load texture file: " + filePath);
            }

            System.out.println("Loaded texture: " + filePath + " (Width: " + width.get(0) + ", Height: " + height.get(0) + ")");

            textureID = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(), height.get(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
            GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

            STBImage.stbi_image_free(image);
        }
        return textureID;
    }

    public int createVertexArray() {
        float[] vertices = {
                // Positions      // Texture Coords
                -0.5f,  0.5f, 0.0f,  0.0f, 1.0f,
                -0.5f, -0.5f, 0.0f,  0.0f, 0.0f,
                0.5f, -0.5f, 0.0f,  1.0f, 0.0f,
                0.5f,  0.5f, 0.0f,  1.0f, 1.0f
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        int vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        int vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices, GL15.GL_STATIC_DRAW);

        int ebo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_STATIC_DRAW);

        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        GL20.glEnableVertexAttribArray(1);

        GL30.glBindVertexArray(0);

        return vao;
    }

    public void render(int shaderProgram, int vao, int textureID) {
        GL20.glUseProgram(shaderProgram);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        int textureUniformLocation = GL20.glGetUniformLocation(shaderProgram, "textureSampler");
        GL20.glUniform1i(textureUniformLocation, 0);

        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        GL20.glUseProgram(0);
    }

    public static void main(String[] args) {
        // Initialize LWJGL, create window, etc.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        long window = glfwCreateWindow(800, 600, "Texture Example", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create GLFW window");
        }

        glfwMakeContextCurrent(window);
        glfwShowWindow(window);
        GL.createCapabilities();

        TextureExample example = new TextureExample();
        example.init();

        while (!glfwWindowShouldClose(window)) {
            example.render();
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        example.cleanup();
        glfwDestroyWindow(window);
        glfwTerminate();
    }
}

