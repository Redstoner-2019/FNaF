package me.redstoner2019.chatgptbs;

import me.redstoner2019.Frame;
import me.redstoner2019.graphics.Renderer;
import me.redstoner2019.graphics.general.Shader;
import me.redstoner2019.graphics.general.ShaderProgram;
import me.redstoner2019.graphics.general.Texture;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static me.redstoner2019.Frame.textureShader;
import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class TrueTypeFontExample {

    private long window;
    private int width = 800;
    private int height = 600;
    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;

    private Map<Float, FontData> fontDataMap = new HashMap<>();
    private String fontPath = "C:\\Users\\Redstoner_2019\\Projects\\LWJGL\\src\\main\\resources\\fonts\\TNR.ttf";

    public static void main(String[] args) {
        new TrueTypeFontExample().run();
    }

    public void run() {
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        GLFW.glfwTerminate();
    }

    private void init() {
        // Initialize GLFW
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        GLFW.glfwDefaultWindowHints(); // optional, the current window hints are already the default
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE); // the window will stay hidden after creation
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE); // the window will be resizable

        // Create the window
        window = GLFW.glfwCreateWindow(width, height, "TrueType Font Example", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        // Center the window
        GLFW.glfwSetWindowPos(
                window,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
        );

        // Make the OpenGL context current
        GLFW.glfwMakeContextCurrent(window);
        // Enable v-sync
        GLFW.glfwSwapInterval(1);

        // Make the window visible
        GLFW.glfwShowWindow(window);

        // Initialize OpenGL capabilities
        GL.createCapabilities();

        // Enable blending for transparency
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void loadFontTexture(String fontPath, float fontSize) {
        ByteBuffer ttfBuffer = null;
        try {
            ttfBuffer = loadFont(fontPath);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        // Create a bitmap for the font texture
        ByteBuffer bitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H);

        STBTTBakedChar.Buffer charData = STBTTBakedChar.malloc(96);
        stbtt_BakeFontBitmap(ttfBuffer, fontSize, bitmap, BITMAP_W, BITMAP_H, 32, charData);

        // Create OpenGL texture
        int textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_ALPHA, BITMAP_W, BITMAP_H, 0, GL_ALPHA, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        MemoryUtil.memFree(bitmap);

        // Store the font data in the map
        fontDataMap.put(fontSize, new FontData(textureID, charData));
    }

    private void loop() {
        // Set up 2D rendering
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER,"shader/vertex.vert");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER,"shader/fragment.frag");

        textureShader = new ShaderProgram();
        textureShader.attachShader(vertexShader);
        textureShader.attachShader(fragmentShader);
        textureShader.link();

        Texture texture = Texture.loadTexture("C:\\Users\\Redstoner_2019\\Projects\\LWJGL\\src\\main\\resources\\textures\\optadata.jpg");

        while (!GLFW.glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            // Render text with different font sizes
            renderText("Dies ist ein Test!", 50, 200, 90,Color.GREEN);

            //Renderer.renderTexture(0,0,1,1,texture,true);

            GLFW.glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            GLFW.glfwPollEvents();
        }
    }

    private void renderText(String text, float x, float y, float fontSize, Color c) {
        renderText(text,x,y,fontSize,c.getRed()/255f,c.getGreen()/255f,c.getBlue()/255f);
    }

    private void renderText(String text, float x, float y, float fontSize, float r, float g, float b) {
        FontData fontData = fontDataMap.get(fontSize);

        if (fontData == null) {
            // Load the font texture for the given font size if it doesn't exist
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }

        glColor3f(r, g, b); // Set the color for the text

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontData.textureID);
        glBegin(GL_QUADS);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] xPos = {x};
            float[] yPos = {y};

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
                stbtt_GetBakedQuad(fontData.charData, BITMAP_W, BITMAP_H, c - 32, xPos, yPos, quad, true);

                glTexCoord2f(quad.s0(), quad.t0());
                glVertex2f(quad.x0(), quad.y0());

                glTexCoord2f(quad.s1(), quad.t0());
                glVertex2f(quad.x1(), quad.y0());

                glTexCoord2f(quad.s1(), quad.t1());
                glVertex2f(quad.x1(), quad.y1());

                glTexCoord2f(quad.s0(), quad.t1());
                glVertex2f(quad.x0(), quad.y1());
            }
        }

        glEnd();
        glDisable(GL_TEXTURE_2D);

        glColor3f(1.0f, 1.0f, 1.0f); // Reset the color to white
    }

    private ByteBuffer loadFont(String path) throws IOException {
        Path fontPath = Paths.get(path);
        ByteBuffer buffer;
        try (FileChannel fc = (FileChannel) Files.newByteChannel(fontPath, StandardOpenOption.READ)) {
            buffer = ByteBuffer.allocateDirect((int) fc.size());
            while (fc.read(buffer) > 0);
            buffer.flip();
        }
        return buffer;
    }

    private static class FontData {
        int textureID;
        STBTTBakedChar.Buffer charData;

        FontData(int textureID, STBTTBakedChar.Buffer charData) {
            this.textureID = textureID;
            this.charData = charData;
        }
    }
}
