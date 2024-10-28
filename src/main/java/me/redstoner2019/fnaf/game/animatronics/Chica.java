package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.game.Distribution;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;

public class Chica extends Animatronic{
    private static Chica INSTANCE;
    private Chica(){
        setCurrentCamera(Camera1A.getInstance());
        setAI_LEVEL(0);
    }

    @Override
    public void move() {
        switch (getCurrentCamera().getCameraName()) {
            case "Camera1A": {
                moveTo(Camera1B.getInstance());
                break;
            }
            case "Camera1B": {
                moveTo(Camera7.getInstance());
                break;
            }
            case "Camera7": {
                switch (Distribution.distribution(1, 2)) {
                    case 0: {
                        moveTo(Camera1B.getInstance());
                        break;
                    }
                    case 1: {
                        moveTo(Camera6.getInstance());
                        break;
                    }
                }
                break;
            }
            case "Camera6": {
                switch (Distribution.distribution(1, 1, 3)) {
                    case 0: {
                        moveTo(Camera7.getInstance());
                        break;
                    }
                    case 1: {
                        moveTo(Camera1B.getInstance());
                        break;
                    }
                    case 2: {
                        moveTo(Camera4A.getInstance());
                        break;
                    }
                }
                break;
            }
            case "Camera4A": {
                switch (Distribution.distribution(2, 1, 1)) {
                    case 0: {
                        moveTo(Camera4B.getInstance());
                        break;
                    }
                    case 1: {
                        moveTo(Camera6.getInstance());
                        break;
                    }
                    case 2: {
                        moveTo(Camera1B.getInstance());
                        break;
                    }
                }
                break;
            }
            case "Camera4B": {
                switch (Distribution.distribution(1, 2)) {
                    case 0: {
                        moveTo(Camera4A.getInstance());
                        break;
                    }
                    case 1: {
                        moveTo(OfficeCamera.getInstance());
                        Office.getInstance().setRightLight(false);
                        break;
                    }
                }
                break;
            }
        }
    }

    public void moveTo(Camera c){
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera())) FNAFMain.fnafMain.glitchStrength = 1;
        System.out.print("Chica: Moving from " + getCurrentCamera().getCameraName());
        if(c instanceof OfficeCamera) Office.getInstance().setRightDoorAnimatronic(this);
        setCurrentCamera(c);
        System.out.println(" to " + getCurrentCamera().getCameraName());
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera())) FNAFMain.fnafMain.glitchStrength = 1;
    }

    public static Chica getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Chica();
        }
        return INSTANCE;
    }
}
