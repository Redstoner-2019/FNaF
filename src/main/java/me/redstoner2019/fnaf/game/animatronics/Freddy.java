package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.Menu;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;
import me.redstoner2019.fnaf.game.game.GameManager;

import java.util.Random;

public class Freddy extends Animatronic{
    private static Freddy INSTANCE;

    private int failedAttacks = 0;
    private long stalledUntil = 0;

    public long stillStalledFor(){
        long time = getStalledUntil() - System.currentTimeMillis();
        if(time < 0) time = 0;
        return time;
    }

    public long getStalledUntil() {
        return stalledUntil;
    }

    public void setStalledUntil(long stalledUntil) {
        this.stalledUntil = stalledUntil;
    }

    private Freddy(){
        setCurrentCamera(Camera1A.getInstance());
        setAI_LEVEL(5);
    }

    @Override
    public void move() {
        if(Chica.getInstance().getCurrentCamera().getCameraName().equals("Camera1A") || Bonnie.getInstance().getCurrentCamera().getCameraName().equals("Camera1A")){
            return;
        }
        switch (getCurrentCamera().getCameraName()) {
            case "Camera1A" : {
                moveTo(Camera1B.getInstance());
                break;
            }
            case "Camera1B" : {
                moveTo(Camera7.getInstance());
                break;
            }
            case "Camera7" : {
                moveTo(Camera6.getInstance());
                break;
            }
            case "Camera6" : {
                moveTo(Camera4A.getInstance());
                break;
            }
            case "Camera4A" : {
                moveTo(Camera4B.getInstance());
                failedAttacks = 0;
                break;
            }
            case "Camera4B" : {
                if(!Office.getInstance().isRightDoor() && FNAFMain.fnafMain.menu == Menu.CAMERAS){
                    if(!GameManager.getInstance().getCamera().equals(Camera4B.getInstance())){
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (FNAFMain.fnafMain.menu == Menu.CAMERAS){
                                    System.out.println("Waiting...");
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                FNAFMain.fnafMain.triggerJumpScare("freddy.jump.",27,true);
                            }
                        });
                        t.start();
                    }
                } else {
                    failedAttacks++;
                    if(failedAttacks >= 5){
                        moveTo(Camera4A.getInstance());
                    }
                }
                break;
            }
        }
    }

    public void moveTo(Camera c){
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera()) && FNAFMain.fnafMain.menu == Menu.CAMERAS) {
            FNAFMain.fnafMain.glitchStrength = 2;
            FNAFMain.sounds.get("camera_garble.ogg").stop();
            FNAFMain.sounds.get("camera_garble.ogg").setCursor(0);
            FNAFMain.sounds.get("camera_garble.ogg").play();
        }
        System.out.print("Freddy: Moving from " + getCurrentCamera().getCameraName());
        setCurrentCamera(c);
        System.out.println(" to " + getCurrentCamera().getCameraName());
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera()) && FNAFMain.fnafMain.menu == Menu.CAMERAS) {
            FNAFMain.fnafMain.glitchStrength = 2;
            FNAFMain.sounds.get("camera_garble.ogg").stop();
            FNAFMain.sounds.get("camera_garble.ogg").setCursor(0);
            FNAFMain.sounds.get("camera_garble.ogg").play();
        }
        int laugh = new Random().nextInt(1,4);
        boolean giggle = new Random().nextInt(75) == 5;
        FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").stop();
        switch (getCurrentCamera().getCameraName()) {
            case "Camera1B" -> {
                if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(10,3f);
                else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
            }
            case "Camera7" -> {
                if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(20,2.5f);
                else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
            }
            case "Camera6" -> {
                if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(45,2f);
                else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
            }
            case "Camera4A" -> {
                if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(45,1.5f);
                else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
            }
            case "Camera4B" -> {
                if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(65,1);
                else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
            }
            default -> {
                if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").play();
                else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
            }
        }

        if(!giggle) FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").play();
        else FNAFMain.sounds.get("giggle.ogg.ogx").setAngle(10,3f);
    }

    public static Freddy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Freddy();
        }
        return INSTANCE;
    }
}
