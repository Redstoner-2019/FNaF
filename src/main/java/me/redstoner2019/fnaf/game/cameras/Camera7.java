package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera7 extends Camera {
    private static Camera7 INSTANCE;
    private Camera7(){

    }
    public static Camera7 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera7();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness, int cameraRandomness2) {
        String name = "restrooms.";
        if(Freddy.getInstance().getCurrentCamera().equals(this)) name = "restrooms.freddy.";
        if(Chica.getInstance().getCurrentCamera().equals(this)) {
            name = "restrooms.chica.";
            if(cameraRandomness2 <= 20){
                name+="anomaly.";
            }
        }
        if(name.equals("restrooms.")) name+="empty.";
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera7";
    }
}
