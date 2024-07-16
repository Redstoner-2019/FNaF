package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.Menu;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;

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
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera())) FNAFMain.fnafMain.glitchStrength = 1;
        System.out.print("Freddy: Moving from " + getCurrentCamera().getCameraName());
        setCurrentCamera(c);
        System.out.println(" to " + getCurrentCamera().getCameraName());
        if(getCurrentCamera().equals(FNAFMain.fnafMain.gameManager.getCamera())) FNAFMain.fnafMain.glitchStrength = 1;
        int laugh = new Random().nextInt(1,4);
        FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").stop();
        switch (getCurrentCamera().getCameraName()) {
            case "Camera1B" -> {
                FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(10,3f);
            }
            case "Camera7" -> {
                FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(20,2.5f);
            }
            case "Camera6" -> {
                FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(45,2f);
            }
            case "Camera4A" -> {
                FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(45,1.5f);
            }
            case "Camera4B" -> {
                FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").setAngle(65,1);
            }
        }
        FNAFMain.sounds.get("freddy_laugh_" + laugh + ".oga").play();
    }

    public static Freddy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Freddy();
        }
        return INSTANCE;
    }
}
