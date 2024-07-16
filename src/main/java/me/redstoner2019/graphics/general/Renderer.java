package me.redstoner2019.graphics.general;

import me.redstoner2019.graphics.font.TextRenderer;
import org.lwjgl.opengl.*;

import java.awt.*;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {
    private static Renderer INSTANCE = null;

    private int width;
    private int height;

    public int vao;
    private final ShaderProgram renderShader;
    private ShaderProgram[] postProcessingShaders;


    private Renderer(){
        vao = createVertexArray();

        Shader vertexShader = Shader.loadShader(GL20.GL_VERTEX_SHADER, "shader/default.vert");
        Shader fragmentShader = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "shader/default.frag");

        renderShader = new ShaderProgram();
        renderShader.attachShader(vertexShader);
        renderShader.attachShader(fragmentShader);
        renderShader.link();
    }

    public static Renderer getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Renderer();
        }
        return INSTANCE;
    }

    public float getAspectRatio(){
        return (float) width / height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public ShaderProgram[] getPostProcessingShaders() {
        return postProcessingShaders;
    }

    public void setPostProcessingShaders(ShaderProgram...postProcessingShaders) {
        this.postProcessingShaders = postProcessingShaders;
    }

    public void renderTexture(float x, float y, float w, float h, float sectionX, float sectionY, float sectionW, float sectionH, Texture texture, boolean overrideAspectRatio, Color color, boolean hasNoise, float noiseStrength){
        if(texture == null) {
            System.err.println("Texture is null.");
            throw new RuntimeException(new NullPointerException("Texture is null."));
        }
        renderTexture(x,y,w,h,sectionX,sectionY,sectionW,sectionH,texture.getId(),color,hasNoise,noiseStrength);
    }

    public void renderTexture(float x, float y, float w, float h, float sectionX, float sectionY, float sectionW, float sectionH, int texture, boolean overrideAspectRatio, Color color, boolean hasNoise, float noiseStrength){
        renderTexture(x,y,w,h,sectionX,sectionY,sectionW,sectionH,texture,color,hasNoise,noiseStrength);
    }

    public void renderTexture(float x, float y, float w, float h,float sectionX, float sectionY, float sectionW, float sectionH, int texture, Color color, boolean hasNoise, float noiseStrength){
        GL20.glUseProgram(renderShader.id);

        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);

        int textureUniformLocation = GL20.glGetUniformLocation(renderShader.id, "textureSampler");
        GL20.glUniform1i(textureUniformLocation, 0);

        int texOffsetLocation = GL20.glGetUniformLocation(renderShader.id, "texOffset");
        GL20.glUniform2f(texOffsetLocation, sectionX, sectionY);

        int texScaleLocation = GL20.glGetUniformLocation(renderShader.id, "texScale");
        GL20.glUniform2f(texScaleLocation, sectionW, sectionH);

        int offsetLocation = GL20.glGetUniformLocation(renderShader.id, "offset");
        GL20.glUniform2f(offsetLocation, x + (w/2), y + (h/2));

        int offsetScaleLocation = GL20.glGetUniformLocation(renderShader.id, "offsetScale");
        GL20.glUniform2f(offsetScaleLocation, w, h);

        int colorLocation = GL20.glGetUniformLocation(renderShader.id, "color");
        GL20.glUniform3f(colorLocation, color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);

        int seedLocation = GL20.glGetUniformLocation(renderShader.id, "seed");
        GL20.glUniform1f(seedLocation, new Random().nextFloat());

        float f;
        if(!hasNoise) f = 0;
        else f = noiseStrength;
        int noiseStrengthLocation = GL20.glGetUniformLocation(renderShader.id, "noiseLevel");
        GL20.glUniform1f(noiseStrengthLocation, f);

        GL30.glBindVertexArray(vao);
        GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);

        GL20.glUseProgram(0);
    }

    /**
     * Screenspace Rendering
     */

    public void renderTexture(float x, float y, float w, float h, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(x,y,w,h,0,0,1,1, texture,overrideAspectRatio, Color.WHITE, hasNoise,noiseStrength);
    }
    public void renderTexture(float x, float y, float w, float h, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTexture(x,y,w,h,0,0,1,1, texture,overrideAspectRatio, c, hasNoise,noiseStrength);
    }

    public void renderTexture(float x, float y, float w, float h, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(x,y,w,h,0,0,1,1, texture,overrideAspectRatio, Color.WHITE, hasNoise,noiseStrength);
    }
    public void renderTexture(float x, float y, float w, float h, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTexture(x,y,w,h,0,0,1,1, texture,overrideAspectRatio, c, hasNoise,noiseStrength);
    }

    public void renderTextureBounds(float x0, float y0, float x1, float y1, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture,overrideAspectRatio, Color.WHITE, hasNoise,noiseStrength);
    }
    public void renderTextureBounds(float x0, float y0, float x1, float y1, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture,overrideAspectRatio, c, hasNoise,noiseStrength);
    }

    public void renderTextureBounds(float x0, float y0, float x1, float y1, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture,overrideAspectRatio, Color.WHITE, hasNoise,noiseStrength);
    }
    public void renderTextureBounds(float x0, float y0, float x1, float y1, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTexture(x0,y0,x1-x0,y1-y0,0,0,1,1, texture,overrideAspectRatio, c, hasNoise,noiseStrength);
    }

    /**
     * Coordinate Rendering
     */

    public void renderTextureCoordinates(float x, float y, float w, float h, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture,overrideAspectRatio, Color.WHITE, hasNoise,noiseStrength);
    }

    public void renderTextureCoordinates(float x, float y, float w, float h, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture,overrideAspectRatio, c, hasNoise,noiseStrength);
    }

    public void renderTextureCoordinates(float x, float y, float w, float h, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture,overrideAspectRatio, Color.WHITE, hasNoise,noiseStrength);
    }

    public void renderTextureCoordinates(float x, float y, float w, float h, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTexture(toNegativeRange(x / width),-toNegativeRange(y / height),(w / width) * 2,(h / height) * -2,0,0,1,1, texture,overrideAspectRatio, c, hasNoise,noiseStrength);
    }

    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture,overrideAspectRatio,hasNoise,noiseStrength);
    }
    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, Texture texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture,overrideAspectRatio,hasNoise,noiseStrength, c);
    }

    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture,overrideAspectRatio,hasNoise,noiseStrength);
    }
    public void renderTextureCoordinatesBounds(float x0, float y0, float x1, float y1, int texture, boolean overrideAspectRatio, boolean hasNoise, float noiseStrength, Color c){
        renderTextureCoordinates(x0,y0,x1-x0,y1-y0,texture,overrideAspectRatio,hasNoise,noiseStrength, c);
    }

    private float toNegativeRange(float f){
        return (f * 2) - 1;
    }

    private int createVertexArray() {
        float[] vertices = {
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
}
