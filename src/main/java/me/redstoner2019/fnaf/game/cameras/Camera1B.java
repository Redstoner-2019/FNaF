package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera1B extends Camera {
    private static Camera1B INSTANCE;

    private Camera1B() {

    }

    public static Camera1B getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera1B();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness, int cameraRandomness2) {
        String name = "dining";
        if (Freddy.getInstance().getCurrentCamera().equals(this)) name = "dining.freddy.";
        if (Bonnie.getInstance().getCurrentCamera().equals(this)) name = "dining.bonnie.";
        if (Chica.getInstance().getCurrentCamera().equals(this)) name = "dining.chica.";
        if (name.equals("dining")) name = "dining.empty.";
        name += "png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera1B";
    }
}
