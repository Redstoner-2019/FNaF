package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera1A extends Camera {
    private static Camera1A INSTANCE;
    private Camera1A(){

    }
    public static Camera1A getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera1A();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness, int cameraRandomness2) {
        String name = "stage.";
        if(Bonnie.getInstance().getCurrentCamera().equals(this)) name += "bonnie.";
        if(Freddy.getInstance().getCurrentCamera().equals(this)) name += "freddy.";
        if(Chica.getInstance().getCurrentCamera().equals(this)) name += "chica.";
        if(name.equals("stage.")) name+="empty.";
        if(name.equals("stage.bonnie.freddy.chica.") && cameraRandomness2 >= 80) name = "stage.looking.";
        if(name.equals("stage.freddy.") && cameraRandomness2 >= 80) name += "looking.";
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera1A";
    }
}
