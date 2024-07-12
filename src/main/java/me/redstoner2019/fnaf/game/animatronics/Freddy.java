package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.Menu;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;

import java.util.Random;

public class Freddy extends Animatronic{
    private static Freddy INSTANCE;
    private boolean waitingForAttack = false;
    private int failedAttacks = 0;
    private int maxFailedAttacks = 5;
    private long stalledUntil = 0;

    public void setStalledUntil(long stalledUntil) {
        this.stalledUntil = stalledUntil;
    }

    public long getStalledUntil() {
        return stalledUntil;
    }

    private Freddy(){
        setCurrentCamera(Camera1A.getInstance());
        setAI_LEVEL(5);
    }

    @Override
    public void movementOpportunity() {
        if(System.currentTimeMillis() < stalledUntil) return;
        if(Chica.getInstance().getCurrentCamera().getCameraName().equals("Camera1A") || Bonnie.getInstance().getCurrentCamera().getCameraName().equals("Camera1A")){
            return;
        }
        Random random = new Random();
        int roll = random.nextInt(19) + 1;
        System.out.println("Freddy rolled " + roll + " ai " + getAI_LEVEL());
        if(waitingForAttack) return;
        if(roll <= getAI_LEVEL()){
            switch (getCurrentCamera().getCameraName()) {
                case "Camera1A" : {
                    setCurrentCamera(Camera1B.getInstance());
                    break;
                }
                case "Camera1B" : {
                    setCurrentCamera(Camera7.getInstance());
                    break;
                }
                case "Camera7" : {
                    setCurrentCamera(Camera6.getInstance());
                    break;
                }
                case "Camera6" : {
                    setCurrentCamera(Camera4A.getInstance());
                    break;
                }
                case "Camera4A" : {
                    setCurrentCamera(Camera4B.getInstance());
                    failedAttacks = 0;
                    maxFailedAttacks = random.nextInt(2,5);
                    break;
                }
                case "Camera4B" : {
                    if(!Office.getInstance().isRightDoor() && FNAFMain.fnafMain.menu == Menu.CAMERAS){
                        waitingForAttack = true;
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
                                waitingForAttack = false;
                            }
                        });
                        t.start();
                    } else {
                        failedAttacks++;
                        if(failedAttacks >= maxFailedAttacks){
                            setCurrentCamera(Camera4A.getInstance());
                        }
                    }
                    break;
                }
            }
        }
    }

    public static Freddy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Freddy();
        }
        return INSTANCE;
    }
}
