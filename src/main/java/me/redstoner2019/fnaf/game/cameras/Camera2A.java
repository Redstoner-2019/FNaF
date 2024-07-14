package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Foxy;
import me.redstoner2019.fnaf.game.animatronics.Freddy;

public class Camera2A extends Camera {
    private static Camera2A INSTANCE;
    private Camera2A(){

    }
    public static Camera2A getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera2A();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness, int cameraRandomness2) {
        String name = "w.hall.";
        if(Bonnie.getInstance().getCurrentCamera().equals(this)) name += "bonnie.";
        if(name.equals("w.hall.")) name+="empty.";
        if(name.equals("w.hall.empty.") && cameraRandomness > 5) name+="light.";
        name+="png";
        if(Foxy.getInstance().getRun_animation_image() != -1 && Foxy.getInstance().getRun_animation_image() <= 30) {
            name = "foxy.run." + Foxy.getInstance().getRun_animation_image() + ".png";
        }
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera2A";
    }
}
