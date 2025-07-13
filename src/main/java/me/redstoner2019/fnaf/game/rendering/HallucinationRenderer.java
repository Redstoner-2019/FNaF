package me.redstoner2019.fnaf.game.rendering;

import me.redstoner2019.audio.Sound;
import me.redstoner2019.audio.SoundManager;
import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.game.Distribution;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.TextureProvider;

import java.awt.*;
import java.util.Random;

import static me.redstoner2019.fnaf.FNAFMain.sounds;

public class HallucinationRenderer {

    public static boolean active = false;
    public static long ticks = 0;
    public static int type = 0;
    public static Sound garble;

    public static void start(){
        if(garble == null) garble = new Sound(sounds.get("garble.ogg").getFilepath(), false);
        garble.stop();
        garble.setCursor(0);
        garble.play();
        System.out.println("Start");
        ticks = 0;
        active = true;
    }

    public static boolean isActive() {
        return active;
    }

    public static void tick(){
        if(!active) return;

        ticks++;
        if(ticks % 2 == 0) {
            if(ticks % 4 == 0) type = Distribution.distribution(3, 1, 1, 1);
            if(ticks > 200){
                active = false;
                garble.stop();
            }
        }
    }

    public static void render(){
        if(!active) return;

        Renderer renderer = Renderer.getInstance();
        TextRenderer textRenderer = TextRenderer.getInstance();
        TextureProvider textureProvider = TextureProvider.getInstance();

        switch (type){
            case 0 -> {
                return;
            }
            case 1 -> {
                renderer.renderTexture(-1,-1,2,2, textureProvider.get("golden.freddy.bonnie.png"), false, false,0);
            }
            case 2 -> {
                renderer.renderTexture(-1,-1,2,2, textureProvider.get("golden.freddy.freddy.png"), false, false,0);
            }
            case 3 -> {
                renderer.renderTexture(-1,-1,2,2, textureProvider.get("white.png"), false, false,0, Color.BLACK);

                float fontSize = (renderer.getHeight() / 1080f) * 200;
                float width = textRenderer.textWidth("IT'S ME",fontSize);
                float height = textRenderer.textHeight("IT'S ME",fontSize);

                textRenderer.renderText("IT'S ME",(renderer.getWidth() / 2f) - (width / 2),(renderer.getHeight() / 2f) - (height / 2),fontSize, Color.WHITE);
            }
        }
    }
}
