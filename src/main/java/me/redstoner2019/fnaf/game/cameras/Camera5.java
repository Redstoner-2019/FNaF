package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera5 extends Camera{
    private static Camera5 INSTANCE;

    private Camera5() {

    }

    public static Camera5 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera5();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness, int cameraRandomness2) {
        String name = "backstage.";
        if (Bonnie.getInstance().getCurrentCamera().equals(this)) name += "bonnie.";
        else name+="empty.";
        if(cameraRandomness2<=20) name+="anomaly.";
        name += "png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera5";
    }
}
