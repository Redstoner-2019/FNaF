package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera2B extends Camera {
    private static Camera2B INSTANCE;
    private Camera2B(){

    }
    public static Camera2B getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera2B();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness) {
        String name = "w.hall.corner.";
        if(Bonnie.getInstance().getCurrentCamera().equals(this)) {
            name += "bonnie.";
            if(cameraRandomness<=10){
                name+="anomaly.1.";
            } else if(cameraRandomness<=20){
                name+="anomaly.2.";
            }
        } else {
            name += "empty.";
            if(cameraRandomness<=20){
                name+="anomaly.1.";
            }
        }
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera2B";
    }
}
