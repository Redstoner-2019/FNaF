package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera6 extends Camera {
    private static Camera6 INSTANCE;

    private Camera6() {

    }

    public static Camera6 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera6();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness) {
        String name = "audio-only.png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera6";
    }
}
