package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.game.Distribution;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;

public class Bonnie extends Animatronic{
    private static Bonnie INSTANCE;
    private Bonnie(){
        setCurrentCamera(Camera1A.getInstance());
        setAI_LEVEL(0);
    }

    @Override
    public void move() {
        switch (getCurrentCamera().getCameraName()) {
            case "Camera1A" : {
                switch (Distribution.distribution(5,3)) {
                    case 0 -> moveTo(Camera1B.getInstance());
                    case 1 -> moveTo(Camera5.getInstance());
                }
                break;
            }
            case "Camera5" : {
                switch (Distribution.distribution(1,1)) {
                    case 0 -> moveTo(Camera1B.getInstance());
                    case 1 -> moveTo(Camera2A.getInstance());
                }
                break;
            }
            case "Camera1B" : {
                switch (Distribution.distribution(1,1)) {
                    case 0 -> moveTo(Camera2A.getInstance());
                    case 1 -> moveTo(Camera5.getInstance());
                }
                break;
            }
            case "Camera2A" : {
                switch (Distribution.distribution(1,1)) {
                    case 0 -> moveTo(Camera3.getInstance());
                    case 1 -> moveTo(Camera2B.getInstance());
                }
                break;
            }
            case "Camera3" : {
                switch (Distribution.distribution(1,2)) {
                    case 0 -> moveTo(OfficeCamera.getInstance());
                    case 1 -> moveTo(Camera2A.getInstance());
                }
                break;
            }
            case "Camera2B" : {
                switch (Distribution.distribution(1,1)) {
                    case 0 -> moveTo(OfficeCamera.getInstance());
                    case 1 -> moveTo(Camera3.getInstance());
                }
                break;
            }
        }
    }

    public void moveTo(Camera c){
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera())) FNAFMain.fnafMain.glitchStrength = 1;
        System.out.print("Bonnie: Moving from " + getCurrentCamera().getCameraName());
        if(c instanceof OfficeCamera) Office.getInstance().setLeftDoorAnimatronic(this);
        setCurrentCamera(c);
        System.out.println(" to " + getCurrentCamera().getCameraName());
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera())) FNAFMain.fnafMain.glitchStrength = 1;
    }

    public static Bonnie getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Bonnie();
        }
        return INSTANCE;
    }
}
