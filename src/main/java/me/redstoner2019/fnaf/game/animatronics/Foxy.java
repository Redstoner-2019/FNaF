package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.Menu;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.Camera1C;

import java.util.Random;

public class Foxy extends Animatronic{
    private static Foxy INSTANCE;
    private int stage = 0;
    private long stalledUntil = 0;

    public void setStalledUntil(long stalledUntil) {
        this.stalledUntil = stalledUntil;
    }

    public int getStage() {
        return stage;
    }

    private Foxy(){
        setCurrentCamera(Camera1C.getInstance());
        setAI_LEVEL(5);
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public void movementOpportunity() {
        if(System.currentTimeMillis() < stalledUntil) return;
        Random random = new Random();
        int roll = random.nextInt(19) + 1;
        System.out.println("Foxy rolled " + roll + " ai " + getAI_LEVEL());
        if(roll <= getAI_LEVEL()){
            stage++;
            if(stage == 4) stage = 3;
        }
    }

    public static Foxy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Foxy();
        }
        return INSTANCE;
    }
    public void startRun(FNAFMain main){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                if(Office.getInstance().getLeftDoorState() != DoorState.CLOSED){
                    main.setMenu(Menu.OFFICE);
                    main.setJumpscare("foxy.enter.");
                    main.setJumpscareFrame(0);
                    main.setJumpscareLength(20);
                    main.triggerJumpScare("foxy.enter.",20,true);
                } else {
                    FNAFMain.sounds.get("FoxyKnock.ogg").play();
                    Random random = new Random();
                    stage = random.nextInt(2)-1;
                    if(stage < 0) stage = 0;
                }
            }
        });
        t.start();
    }
}
