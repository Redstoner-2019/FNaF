package me.redstoner2019.fnaf.game.cameras;

import me.redstoner2019.fnaf.game.animatronics.Foxy;

public class Camera1C extends Camera {
    private static Camera1C INSTANCE;
    private Camera1C(){

    }
    public static Camera1C getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Camera1C();
        }
        return INSTANCE;
    }

    @Override
    public String getImage(int cameraRandomness, int cameraRandomness2) {
        String name = "cove." + Foxy.getInstance().getStage() + ".";
        name+="png";
        return name;
    }

    @Override
    public String getCameraName() {
        return "Camera1C";
    }
}
