package me.redstoner2019.fnaf.game.game;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.Texture;
import me.redstoner2019.graphics.general.TextureProvider;
import me.redstoner2019.graphics.general.Util;

import java.awt.*;

public class Notification {
    private String text;
    private int fadeIn = 100;
    private int fadeOut = 100;
    private int duration;
    private Color textColor;
    private Color backgroundColor;
    private long start = 0;
    public static Renderer renderer = Renderer.getInstance();
    public static TextRenderer textRenderer = TextRenderer.getInstance();
    public static TextureProvider textureProvider = TextureProvider.getInstance();

    public Notification(String text, int fadeIn, int fadeOut, int duration, Color textColor, Color backgroundColor) {
        this.text = text;
        this.fadeIn = fadeIn;
        this.fadeOut = fadeOut;
        this.duration = duration;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public Notification(String text, int duration, Color textColor, Color backgroundColor) {
        this.text = text;
        this.duration = duration;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    public void start(){
        start = System.currentTimeMillis();
    }

    public void render(){
        float alpha = 0.0f;
        long timeSinceStart = System.currentTimeMillis() - start;
        if(FNAFMain.between(start,start + fadeIn,System.currentTimeMillis())){
            alpha = (float) timeSinceStart / (float) fadeIn;
        }
        if(FNAFMain.between(start + fadeIn,start + fadeIn + duration,System.currentTimeMillis())){
            alpha = 1;
        }
        if(FNAFMain.between(start + fadeIn + duration,start + fadeIn + duration + fadeOut,System.currentTimeMillis())){
            alpha = (float) (timeSinceStart - fadeIn - duration) / (float) fadeOut;
            alpha = 1 - alpha;
        }
        int alphaInt = (int) (255f * alpha * 0.9f);

        float mod = 1;
        float fontSize = 40 * mod;

        int x = (int) ((renderer.getWidth() - textRenderer.textWidth(text,fontSize)) / 2);

        Texture texture = textureProvider.get("white.png");
        renderer.renderTextureBounds(1f,0.2f,-1f,-0.2f,texture,true,false,0,new Color(backgroundColor.getRed(),backgroundColor.getGreen(),backgroundColor.getBlue(),alphaInt));

        textRenderer.renderText(text,x, (float) (renderer.getHeight() - fontSize) / 2f,fontSize,new Color(textColor.getRed(),textColor.getGreen(),textColor.getBlue(),alphaInt));
    }

    public boolean isRunning(){
        return FNAFMain.between(start,start + duration + fadeIn + fadeOut,System.currentTimeMillis());
    }

}
