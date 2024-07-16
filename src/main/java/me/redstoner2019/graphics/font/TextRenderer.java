package me.redstoner2019.graphics.font;

import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.Texture;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.stb.STBTruetype.*;

public class TextRenderer {
    private static TextRenderer INSTANCE;

    private Map<Float, FontData> fontDataMap = new HashMap<>();
    private String fontPath = "fonts/font.ttf";
    private Renderer renderer;

    private final int BITMAP_W = 2048;
    private final int BITMAP_H = 2048;

    private TextRenderer(Renderer renderer){
        this.renderer = renderer;
    }

    public static TextRenderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TextRenderer(Renderer.getInstance());
        }
        return INSTANCE;
    }

    public float textWidth(String text, float fontSize){
        float x = 0;
        float end = 0;

        FontData fontData = fontDataMap.get(fontSize);

        if (fontData == null) {
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] xPos = {0};
            float[] yPos = {0};

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
                stbtt_GetBakedQuad(fontData.charData, BITMAP_W, BITMAP_H, c - 32, xPos, yPos, quad, true);

                if(end < quad.x1()) end = quad.x1();
            }
        }
        return end;
    }

    public float textHeight(String text, float fontSize){
        FontData fontData = fontDataMap.get(fontSize);

        if (fontData == null) {
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }

        float height = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] xPos = {0};
            float[] yPos = {0};

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.mallocStack(stack);
                stbtt_GetBakedQuad(fontData.charData, BITMAP_W, BITMAP_H, c - 32, xPos, yPos, quad, true);

                float h = quad.y1() - quad.y0();

                if(height<h) height = h;
            }
        }

        return height;
    }

    public void renderText(String text, float x, float y, float fontSize, Color c) {
        renderText(text,x,y + (fontSize * 1.175f),fontSize,c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f);
    }

    public void renderTextScreen(String text, float x, float y, float fontSize, Color c) {
        renderText(text,toNegativeRange(x / renderer.getWidth()),toNegativeRange(y / renderer.getHeight()),fontSize,c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f);
    }

    @Deprecated
    public void renderTextOld(String text, float x, float y, float fontSize, Color c) {
        renderText(text,x,y,fontSize,c.getRed() / 255f,c.getGreen() / 255f,c.getBlue() / 255f);
    }

    public void renderText(String text, float x, float y, float fontSize, float r, float g, float b) {
        FontData fontData = fontDataMap.get(fontSize);

        if (fontData == null) {
            loadFontTexture(fontPath, fontSize);
            fontData = fontDataMap.get(fontSize);
        }

        glColor3f(r, g, b);

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontData.textureID);
        glBegin(GL_QUADS);

        float end = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            float[] xPos = {x};
            float[] yPos = {y};

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) {
                    System.out.println(c + " not allowed");
                    continue;
                }

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
    }
    public ByteBuffer loadFont(String path) throws IOException {
        return Texture.createBuffer(path);
    }
    public void loadFontTexture(String fontPath, float fontSize) {
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

    private float toNegativeRange(float f){
        return (f * 2) - 1;
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
