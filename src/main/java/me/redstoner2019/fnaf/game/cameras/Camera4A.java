package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera4A extends Camera {
    private static Camera4A INSTANCE;
    private Camera4A(){

    }
    public static Camera4A getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera4A();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness) {
        String name = "e.hall.";
        if(Freddy.getInstance().getCurrentCamera().equals(this)) name = "e.hall.freddy.";
        if(Chica.getInstance().getCurrentCamera().equals(this)) {
            name = "e.hall.chica.";
            if(cameraRandomness <= 20) name+="anomaly.";
        }
        if(name.equals("e.hall.")) {
            name += "empty.";
            if(cameraRandomness <= 10) name+="anomaly.1.";
            else if(cameraRandomness <= 20) name+="anomaly.2.";
        }
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera4A";
    }
}
