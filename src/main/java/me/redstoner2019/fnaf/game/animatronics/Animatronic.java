package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.game.cameras.Camera;

public abstract class Animatronic {
    private Camera currentCamera;
    private int AI_LEVEL = 0;

    public Animatronic(){

    }

    public int getAI_LEVEL() {
        return AI_LEVEL;
    }

    public void setAI_LEVEL(int AI_LEVEL) {
        this.AI_LEVEL = AI_LEVEL;
    }

    public abstract void move();

    public Camera getCurrentCamera() {
        return currentCamera;
    }

    public void setCurrentCamera(Camera currentCamera) {
        this.currentCamera = currentCamera;
    }
}
