package me.redstoner2019.fnaf.game.game;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.Menu;
import me.redstoner2019.fnaf.game.Distribution;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Foxy;
import me.redstoner2019.fnaf.game.animatronics.Freddy;
import me.redstoner2019.fnaf.game.cameras.*;

import java.util.Random;

import static me.redstoner2019.fnaf.FNAFMain.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameManager {
    private static GameManager INSTANCE;

    public final static long NIGHT_LENGTH = 360 * 1000;
    private final static double idleUsage =.0025;
    private Random random = new Random();

    private long nightStart;
    private int hour = 0;
    private boolean isCameraUp = false;
    private Camera camera = Camera1A.getInstance();
    private long lastCameraCheck = System.currentTimeMillis();
    private double power = 100;
    private boolean isBlackout = false;
    private boolean isPowerout = false;
    private int devices = 1;

    private final Object CAMERA_DOWN_WAIT = new Object();
    private final Object CAMERA_UP_WAIT = new Object();
    private final Object FOXY_RUN_SLEEP = new Object();

    boolean nightRunning = false;

    private GameManager(){

    }

    public long getNightStart() {
        return nightStart;
    }

    public void setNightStart(long nightStart) {
        this.nightStart = nightStart;
    }

    public int getDevices() {
        return devices;
    }

    public boolean isBlackout() {
        return isBlackout;
    }

    public boolean isPowerout() {
        return isPowerout;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public void setNightRunning(boolean nightRunning) {
        this.nightRunning = nightRunning;
    }

    public void startNight(int night){
        startNight(night,0,0,0,0);
    }

    public void startNight(int night, int bonnieAI, int chicaAI, int freddyAI, int foxyAI){
        if(nightRunning) return;
        nightStart = System.currentTimeMillis();
        nightRunning = true;
        camera = Camera1A.getInstance();
        lastCameraCheck = System.currentTimeMillis();
        power = 100;
        isBlackout = false;
        isPowerout = false;

        Office.getInstance().setRightDoor(false);
        Office.getInstance().setLeftDoor(false);
        Office.getInstance().setLeftLight(false);
        Office.getInstance().setRightLight(false);
        Office.getInstance().setLeftDoorState(DoorState.OPEN);
        Office.getInstance().setRightDoorState(DoorState.OPEN);

        Freddy freddy = Freddy.getInstance();
        Bonnie bonnie = Bonnie.getInstance();
        Chica chica = Chica.getInstance();
        Foxy foxy = Foxy.getInstance();

        freddy.setCurrentCamera(Camera1A.getInstance());
        bonnie.setCurrentCamera(Camera1A.getInstance());
        chica.setCurrentCamera(Camera1A.getInstance());
        foxy.setCurrentCamera(Camera1C.getInstance());
        foxy.setStage(0);
        foxy.setRun_animation_image(-1);

        switch (night) {
            case 1 -> {
                freddy.setAI_LEVEL(0);
                bonnie.setAI_LEVEL(0);
                chica.setAI_LEVEL(0);
                foxy.setAI_LEVEL(0);
            }
            case 2 -> {
                freddy.setAI_LEVEL(0);
                bonnie.setAI_LEVEL(3);
                chica.setAI_LEVEL(1);
                foxy.setAI_LEVEL(1);
            }
            case 3 -> {
                freddy.setAI_LEVEL(1);
                bonnie.setAI_LEVEL(0);
                chica.setAI_LEVEL(5);
                foxy.setAI_LEVEL(2);
            }
            case 4 -> {
                freddy.setAI_LEVEL(Math.random() > 0.5 ? 1 : 2);
                bonnie.setAI_LEVEL(2);
                chica.setAI_LEVEL(4);
                foxy.setAI_LEVEL(6);
            }
            case 5 -> {
                freddy.setAI_LEVEL(3);
                bonnie.setAI_LEVEL(5);
                chica.setAI_LEVEL(7);
                foxy.setAI_LEVEL(5);
            }
            case 6 -> {
                freddy.setAI_LEVEL(4);
                bonnie.setAI_LEVEL(10);
                chica.setAI_LEVEL(12);
                foxy.setAI_LEVEL(16);
            }
            default -> {
                freddy.setAI_LEVEL(freddyAI);
                bonnie.setAI_LEVEL(bonnieAI);
                chica.setAI_LEVEL(chicaAI);
                foxy.setAI_LEVEL(foxyAI);
                System.out.println("Default");
                System.out.println(freddy.getAI_LEVEL());
                System.out.println(bonnie.getAI_LEVEL());
                System.out.println(chica.getAI_LEVEL());
                System.out.println(foxy.getAI_LEVEL());
            }
        }

        Thread mainManagement = new Thread(() -> {
            GameManager gameManager = getInstance();
            Office office = Office.getInstance();
            float deltaTime = 0;
            while (nightRunning) {
                int prevHour = hour;
                long nightTime = System.currentTimeMillis() - nightStart;
                hour = (int) (nightTime / (NIGHT_LENGTH / 6));
                double updateThreadStart = glfwGetTime();

                if(prevHour != hour){
                    switch (hour){
                        case 2 -> bonnie.setAI_LEVEL(bonnie.getAI_LEVEL() + 1);
                        case 3,4 -> {
                            bonnie.setAI_LEVEL(bonnie.getAI_LEVEL() + 1);
                            chica.setAI_LEVEL(chica.getAI_LEVEL() + 1);
                            foxy.setAI_LEVEL(foxy.getAI_LEVEL() + 1);
                        }
                        case 6 -> {
                            stopAllSounds();
                            nightRunning = false;
                            FNAFMain.fnafMain.menu = Menu.NIGHT_END;
                            sounds.get("chimes 2.ogg").play();
                            sounds.get("cheer.ogg").play();
                            nightRunning = false;
                            if(fnafMain.nightNumber < 5) fnafMain.nightNumber++;

                            try {
                                Thread.sleep(8000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            stopAllSounds();
                            fnafMain.menu = Menu.MAIN_MENU;
                            sounds.get("Static2.ogg").play();
                            sounds.get("Mainmenu1.ogg").play();
                            sounds.get("Mainmenu1.ogg").setRepeating(true);
                        }
                    }
                }

                if(hour == 6) break;

                if(!isPowerout){
                    if(random.nextInt(50000) == 1){
                        sounds.get("Circus.ogg").play();
                    }

                    if(camera.equals(Camera2A.getInstance()) && isCameraUp && foxy.getStage() == 3 && foxy.getRun_animation_image() == -1) {
                        synchronized (FOXY_RUN_SLEEP){
                            FOXY_RUN_SLEEP.notifyAll();
                        }
                    }

                    if(Chica.getInstance().getCurrentCamera().equals(Camera6.getInstance())) {
                        if(random.nextInt(1000) == 1){
                            if(camera.equals(Camera6.getInstance())) {
                                sounds.get("chica_kitchen1.oga").setVolume(0.7f);
                                sounds.get("chica_kitchen1.oga").play();
                            } else {
                                sounds.get("chica_kitchen1.oga").setVolume(0.2f);
                                sounds.get("chica_kitchen1.oga").play();
                            }
                        }
                    }

                    if(Freddy.getInstance().getCurrentCamera().equals(Camera6.getInstance()) && isCameraUp) {
                        if(random.nextInt(1000) == 1){
                            if(camera.equals(Camera6.getInstance())) {
                                sounds.get("powerout.ogg").setVolume(0.1f);
                                sounds.get("powerout.ogg").play();
                            } else {
                                sounds.get("powerout.ogg").stop();
                            }
                        }
                    } else if(!isPowerout) {
                        sounds.get("powerout.ogg").stop();
                    }
                }

                boolean lastCameraUp = isCameraUp;
                isCameraUp = FNAFMain.fnafMain.menu == Menu.CAMERAS;

                if(!isCameraUp && lastCameraUp){
                    if(camera.equals(freddy.getCurrentCamera())) freddy.setStalledUntil(System.currentTimeMillis() + random.nextInt(2000,10000));
                    else freddy.setStalledUntil(System.currentTimeMillis());
                    foxy.setStalledUntil(System.currentTimeMillis() + random.nextInt(2000,20000));

                    synchronized (CAMERA_DOWN_WAIT) {
                        System.out.println("Camera Down");
                        CAMERA_DOWN_WAIT.notifyAll();
                    }
                } else if(isCameraUp && !lastCameraUp){
                    synchronized (CAMERA_UP_WAIT) {
                        CAMERA_UP_WAIT.notifyAll();
                    }
                } else if(isCameraUp) {
                    lastCameraCheck = System.currentTimeMillis();
                    if(camera.equals(freddy.getCurrentCamera())) freddy.setStalledUntil(System.currentTimeMillis() + 100);
                    else freddy.setStalledUntil(System.currentTimeMillis());
                    foxy.setStalledUntil(System.currentTimeMillis() + 100);
                } else {
                    //System.out.println("Camera is Down");
                }

                double usage = 0;
                int devices = 1;

                if(Office.getInstance().isLeftDoor()) {devices++; usage+=idleUsage;}
                if(Office.getInstance().isRightDoor()) {devices++; usage+=idleUsage;}
                if(Office.getInstance().isLeftLight()) {devices++; usage+=idleUsage;}
                if(Office.getInstance().isRightLight()) {devices++; usage+=idleUsage;}
                if(fnafMain.menu == Menu.CAMERAS) {devices++; usage+=idleUsage;}

                gameManager.devices = devices;

                if(power > 0 && gameManager.isNightRunning()) {
                    power -= ((idleUsage + usage) * deltaTime);
                    if(power == 0) power = -1;
                } else if(power < 0 && gameManager.isNightRunning() && !isPowerout) {
                    power = 0;
                    stopAllSounds();

                    isPowerout = true;

                    nightRunning = false;
                    fnafMain.menu = Menu.OFFICE;

                    office.setLeftLight(false);
                    office.setRightLight(false);
                    if(office.getLeftDoorState() == DoorState.CLOSED) {
                        office.setLeftDoorState(DoorState.OPENING);
                        office.setLeftDoorAnimation(12);
                        office.setLeftDoor(false);
                        sounds.get("door.ogg").play();
                    }
                    if(office.getRightDoorState() == DoorState.CLOSED) {
                        office.setRightDoorState(DoorState.OPENING);
                        office.setRightDoorAnimation(12);
                        office.setRightDoor(false);
                        sounds.get("door.ogg").play();
                    }

                    Thread poweroutage = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int waitTime = getRandomNumberWithinBounds(1000, 20000,3000);
                                System.out.println("Freddy waiting " + waitTime + " ms");
                                Thread.sleep(waitTime);

                                if(hour >= 6) return;
                                sounds.get("powerout.ogg").setVolume(0.3f);
                                sounds.get("powerout.ogg").play();

                                waitTime = getRandomNumberWithinBounds(4000, 20000,8000);
                                System.out.println("Freddy waiting " + waitTime + " ms");
                                Thread.sleep(waitTime);

                                if(hour >= 6) return;
                                sounds.get("powerout.ogg").stop();
                                isBlackout = true;

                                waitTime = getRandomNumberWithinBounds(1000, 20000,3000);
                                System.out.println("Freddy waiting " + waitTime + " ms");
                                Thread.sleep(waitTime);

                                if(hour >= 6) return;
                                sounds.get("deep_steps.ogg").play();
                                sounds.get("deep_steps.ogg").setRepeating(true);

                                waitTime = getRandomNumberWithinBounds(4000, 16000,6000);
                                System.out.println("Freddy waiting " + waitTime + " ms");
                                Thread.sleep(waitTime);

                                if(hour >= 6) return;
                                sounds.get("deep_steps.ogg").stop();

                                nightStart = Math.min(System.currentTimeMillis() - NIGHT_LENGTH - 660,nightStart);
                                fnafMain.triggerJumpScare("freddy.blackout.", 20, true);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                    poweroutage.start();

                    sounds.get("powerdown.ogg").play();
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                deltaTime = (float) ((glfwGetTime() - updateThreadStart) / (1.0/60.0));
            }
        });

        Thread freddyThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(3020);
                    if(chica.getCurrentCamera().equals(Camera1A.getInstance()) || bonnie.getCurrentCamera().equals(Camera1A.getInstance())){
                        continue;
                    }
                    if(freddy.stillStalledFor() > 0){
                        continue;
                    }
                    if(isCameraUp && camera.equals(freddy.getCurrentCamera())) {
                        continue;
                    }
                    if(freddy.getCurrentCamera().equals(Camera4B.getInstance()) && !Office.getInstance().isRightDoor() && lastCameraCheck > 3020){
                        synchronized (CAMERA_DOWN_WAIT) {
                            freddy.moveTo(OfficeCamera.getInstance());
                            Thread.sleep(random.nextInt(15000,45000));
                            if(foxy.getCurrentCamera().equals(InOfficeCamera.getInstance())) continue;
                            if(nightRunning && isCameraUp) CAMERA_DOWN_WAIT.wait();
                            if (nightRunning) FNAFMain.fnafMain.triggerJumpScare("freddy.jump.",27,true);
                            nightRunning = false;
                        }
                    }
                    if(move(freddy.getAI_LEVEL())) freddy.move();
                    else System.out.println("Freddy Movement Failed");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread bonnieThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(4970);
                    if(move(bonnie.getAI_LEVEL())) {
                        if(bonnie.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                            if(Office.getInstance().isLeftDoor()){
                                Office.getInstance().setLeftDoorAnimatronic(null);
                                FNAFMain.sounds.get("Knock2.ogg").play();
                                bonnie.moveTo(Camera1B.getInstance());
                            } else {
                                System.out.println("Bonnie has entered the office");
                                bonnie.setCurrentCamera(InOfficeCamera.getInstance());
                                synchronized (CAMERA_DOWN_WAIT) {
                                    CAMERA_DOWN_WAIT.wait();
                                }
                                if(freddy.getCurrentCamera().equals(InOfficeCamera.getInstance())) continue;
                                if(foxy.getCurrentCamera().equals(InOfficeCamera.getInstance())) continue;
                                nightRunning = false;
                                FNAFMain.fnafMain.triggerJumpScare("bonnie.jump.",10,true);
                            }
                        } else {
                            bonnie.move();
                        }
                    }
                    else System.out.println("Bonnie Movement Failed");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread chicaThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(4980);
                    if(move(chica.getAI_LEVEL())) {
                        if(chica.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                            if(Office.getInstance().isRightDoor()){
                                Office.getInstance().setRightDoorAnimatronic(null);
                                FNAFMain.sounds.get("Knock2.ogg").play();
                                switch (Distribution.distribution(4, 3, 2, 1)) {
                                    case 0: {
                                        chica.moveTo(Camera4B.getInstance());
                                        break;
                                    }
                                    case 1: {
                                        if (Freddy.getInstance().getCurrentCamera().getCameraName().equals("Camera1A")) {
                                            chica.moveTo(Camera1A.getInstance());
                                        } else {
                                            chica.moveTo(Camera1B.getInstance());
                                        }
                                        break;
                                    }
                                    case 2: {
                                        chica.moveTo(Camera7.getInstance());
                                        break;
                                    }
                                    case 3: {
                                        chica.moveTo(Camera6.getInstance());
                                        break;
                                    }
                                }
                            } else {
                                System.out.println("Chica has entered the office");
                                chica.setCurrentCamera(InOfficeCamera.getInstance());
                                synchronized (CAMERA_DOWN_WAIT) {
                                    CAMERA_DOWN_WAIT.wait();
                                }
                                if(bonnie.getCurrentCamera().equals(InOfficeCamera.getInstance())) continue;
                                if(foxy.getCurrentCamera().equals(InOfficeCamera.getInstance())) continue;
                                nightRunning = false;
                                FNAFMain.fnafMain.triggerJumpScare("chica.jump.", 15, true);
                            }
                        } else {
                            chica.move();
                        }
                    }
                    else System.out.println("Chica Movement Failed");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread foxyThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(5010);
                    if(foxy.stillStalledFor() > 0){
                        continue;
                    }
                    if(isCameraUp && camera.equals(foxy.getCurrentCamera())) {
                        continue;
                    }
                    if(move(foxy.getAI_LEVEL())) foxy.move();
                    else System.out.println("Foxy Movement Failed");

                    if(foxy.getStage() == 3){
                        synchronized (FOXY_RUN_SLEEP) {
                            FOXY_RUN_SLEEP.wait(10000);
                        }

                        if(isPowerout || !nightRunning){
                            continue;
                        }
                        sounds.get("FoxyRun.ogg").setVolume(0.5f);
                        sounds.get("FoxyRun.ogg").play();
                        for (int i = 0; i < 30; i++) {
                            foxy.setRun_animation_image(i);
                            Thread.sleep(32);
                        }
                        foxy.setRun_animation_image(-1);
                        Thread.sleep(500);

                        if(Office.getInstance().getLeftDoorState() == DoorState.CLOSED){
                            sounds.get("FoxyKnock.ogg").play();
                            power-=foxy.getPowerDrain();
                            if(foxy.getPowerDrain() == 6) foxy.setPowerDrain(11);
                            if(foxy.getPowerDrain() == 1) foxy.setPowerDrain(6);
                            foxy.setStage(random.nextInt(2));
                        } else {
                            foxy.setCurrentCamera(InOfficeCamera.getInstance());
                            fnafMain.menu = Menu.OFFICE;
                            fnafMain.triggerJumpScare("foxy.enter.",20,true);
                            nightRunning = false;
                        }
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        mainManagement.start();
        freddyThread.start();
        bonnieThread.start();
        chicaThread.start();
        foxyThread.start();
    }

    public boolean isNightRunning() {
        return nightRunning;
    }

    public int getHour() {
        return hour;
    }

    public static GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }

    private boolean move(int ai){
        return random.nextInt(1,21) <= ai;
    }

    public static int getRandomNumberWithinBounds(double a, double b, double x) {
        if (a >= b) {
            throw new IllegalArgumentException("Lower bound 'a' must be less than upper bound 'b'");
        }

        Random random = new Random();
        double mean = x;  // The peak of the normal distribution
        double standardDeviation = (b - a) / 6;  // A common practice to cover most values within the bounds

        double result;
        do {
            result = mean + random.nextGaussian() * standardDeviation;
        } while (result < a || result > b);

        return (int) result;
    }
}
