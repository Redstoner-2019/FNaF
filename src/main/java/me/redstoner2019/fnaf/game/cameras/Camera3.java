package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera3 extends Camera {
    private static Camera3 INSTANCE;
    private Camera3(){

    }
    public static Camera3 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera3();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness) {
        String name = "supply.";
        if(Bonnie.getInstance().getCurrentCamera().equals(this)) name += "bonnie.";
        else name += "empty.";
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera3";
    }
}
