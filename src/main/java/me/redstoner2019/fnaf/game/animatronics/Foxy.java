package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.game.cameras.Camera1C;

public class Foxy extends Animatronic{
    private static Foxy INSTANCE;
    private int stage = 0;
    private long stalledUntil = 0;
    private int powerDrain = 1;
    private long leftCove = -1;
    private int run_animation_image = -1;

    public long stillStalledFor(){
        long time = getStalledUntil() - System.currentTimeMillis();
        if(time < 0) time = 0;
        return time;
    }

    public void setStalledUntil(long stalledUntil) {
        this.stalledUntil = stalledUntil;
    }

    public long getStalledUntil() {
        return stalledUntil;
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

    public int getRun_animation_image() {
        return run_animation_image;
    }

    public void setRun_animation_image(int run_animation_image) {
        this.run_animation_image = run_animation_image;
    }

    public void setPowerDrain(int powerDrain) {
        this.powerDrain = powerDrain;
    }

    public int getPowerDrain() {
        return powerDrain;
    }

    @Override
    public void move() {
        if(stillStalledFor() > 0) return;
        stage++;
        if(stage == 4) stage = 3;
        else FNAFMain.fnafMain.glitchStrength = 1;
        /*if(stage == 3 && leftCove == -1) {
            leftCove = System.currentTimeMillis();
            System.out.println("Foxy Left Cove");
        }
        if(stage == 3 && System.currentTimeMillis() - leftCove > 10000) {
            startRun(FNAFMain.fnafMain);
            FNAFMain.sounds.get("FoxyRun.ogg").play();
        }*/
    }

    public static Foxy getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Foxy();
        }
        return INSTANCE;
    }
    public void startRun(FNAFMain main){
        /*leftCove = -1;
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
                    gameManager.power-=powerDrain;
                    gam
                    if(powerDrain == 6) powerDrain = 11;
                    if(powerDrain == 1) powerDrain = 6;
                    stage = random.nextInt(2);
                    System.out.println("Foxy returned to stage " + stage);
                }
            }
        });*/
        //t.start();
    }
}
