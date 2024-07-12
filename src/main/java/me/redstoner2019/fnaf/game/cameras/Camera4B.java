package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera4B extends Camera {
    private static Camera4B INSTANCE;
    private Camera4B(){

    }
    public static Camera4B getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera4B();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness) {
        String name = "e.hall.corner.";
        if(Freddy.getInstance().getCurrentCamera().equals(this)) name = "e.hall.corner.freddy.";
        if(Chica.getInstance().getCurrentCamera().equals(this)) {
            name = "e.hall.corner.chica.";
            if(cameraRandomness <= 10) {
                name+="anomaly.1.";
            } else if(cameraRandomness <= 20) {
                name+="anomaly.2.";
            }
        }
        if(name.equals("e.hall.corner.")) {
            name += "empty.";
            if(cameraRandomness <= 5) {
                name+="anomaly.1.";
            } else if(cameraRandomness <= 10) {
                name+="anomaly.2.";
            } else if(cameraRandomness <= 15) {
                name+="anomaly.3.";
            } else if(cameraRandomness <= 20) {
                name+="anomaly.4.";
            }
        }
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera4B";
    }
}
