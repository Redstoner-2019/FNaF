package me.redstoner2019.fnaf.game;

import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.TextureProvider;

import java.util.Random;

import static me.redstoner2019.fnaf.FNAFMain.*;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

public class Jumpscare {
    public static void playGoldenFreddyJumpscare(){
        Renderer renderer = Renderer.getInstance();
        Random r = new Random();

        renderer.setActiveShader(renderer.getVhsShader());

        for (int i = 0; i < 200; i++) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            stopAllSounds();
            sounds.get("XSCREAM2.ogg").play();

            float x0 = ((r.nextFloat() * 2 - 1) / 10) - 0.1f;
            float y0 = 0;
            float angleOffset = (r() / 100) + 0.2f;

            int vao = renderer.createVertexArray(-1 + x0 + angleOffset,1 + y0,-1 + x0, -1 + y0, 1 + x0, -1 + y0, 1 + x0 + angleOffset, 1 + y0);

            String texture = "golden.freddy.png";

            switch (r.nextInt(5)){
                case 0 -> {
                    texture = "golden.freddy.bonnie.png";
                    break;
                }
                case 1 -> {
                    texture = "golden.freddy.freddy.png";
                    break;
                }
            }

            renderer.renderTexture(-0.5f,-0.5f,1,1, TextureProvider.getInstance().get(texture),true,false,0, vao);
            glfwSwapBuffers(fnafMain.window);
        }

        stopAllSounds();
        renderer.setActiveShader(renderer.getRenderShader());
        fnafMain.triggerJumpScare("golden.freddy",0,true, true);
    }

    private static float r(){
        return new Random().nextFloat() * 2 -1;
    }
}
