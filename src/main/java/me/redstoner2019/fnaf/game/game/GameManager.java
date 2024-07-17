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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static me.redstoner2019.fnaf.FNAFMain.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class GameManager {
    private static GameManager INSTANCE;

    private long NIGHT_LENGTH = 360 * 1000;
    private double idleUsage =.0025;
    private Random random = new Random();
    private int freddyInterval = 3820;
    private int bonnieInterval = 4970;
    private int chicaInterval = 4980;
    private int foxyInterval = 5010;

    private long nightStart;
    private int hour = 0;
    private boolean isCameraUp = false;
    private Camera camera = Camera1A.getInstance();
    private long lastCameraCheck = System.currentTimeMillis();
    private double power = 100;
    private boolean isBlackout = false;
    private boolean isPowerout = false;
    private int devices = 1;
    public boolean customNight = false;
    public boolean ventaNight = false;
    public int nightNumber = 0;

    private final Object CAMERA_DOWN_WAIT = new Object();
    private final Object CAMERA_UP_WAIT = new Object();
    private final Object FOXY_RUN_SLEEP = new Object();

    boolean nightRunning = false;

    private GameManager(){

    }



    public long getNIGHT_LENGTH() {
        return NIGHT_LENGTH;
    }

    public void setNIGHT_LENGTH(long NIGHT_LENGTH) {
        this.NIGHT_LENGTH = NIGHT_LENGTH;
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

    public boolean isVentaNight() {
        return ventaNight;
    }

    public void setVentaNight(boolean ventaNight) {
        this.ventaNight = ventaNight;
    }

    public boolean isCustomNight() {
        return customNight;
    }

    public void setCustomNight(boolean customNight) {
        this.customNight = customNight;
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
        nightNumber = night;

        Office.getInstance().setRightDoor(false);
        Office.getInstance().setLeftDoor(false);
        Office.getInstance().setLeftLight(false);
        Office.getInstance().setRightLight(false);
        Office.getInstance().setLeftDoorState(DoorState.OPEN);
        Office.getInstance().setRightDoorState(DoorState.OPEN);
        Office.getInstance().setLeftDoorAnimatronic(null);
        Office.getInstance().setRightDoorAnimatronic(null);

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
        foxy.setPowerDrain(0);

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
                bonnie.setAI_LEVEL(2); //5
                chica.setAI_LEVEL(4); //6
                foxy.setAI_LEVEL(6); //8
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
            case 8 -> {
                freddy.setAI_LEVEL(20);
                bonnie.setAI_LEVEL(20);
                chica.setAI_LEVEL(20);
                foxy.setAI_LEVEL(20);

                idleUsage/=2.4f;

                customNight = true;
                ventaNight = true;
                setNIGHT_LENGTH(648000);

                freddyInterval = 2920;
                bonnieInterval = 3970;
                chicaInterval = 3980;
                foxyInterval = 4010;
            }
            default -> {
                freddy.setAI_LEVEL(freddyAI);
                bonnie.setAI_LEVEL(bonnieAI);
                chica.setAI_LEVEL(chicaAI);
                foxy.setAI_LEVEL(foxyAI);
                customNight = true;
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
                        case 2 -> {
                            if(!customNight) bonnie.setAI_LEVEL(bonnie.getAI_LEVEL() + 1);
                        }
                        case 3,4 -> {
                            if(!customNight) {
                                bonnie.setAI_LEVEL(bonnie.getAI_LEVEL() + 1);
                                chica.setAI_LEVEL(chica.getAI_LEVEL() + 1);
                                foxy.setAI_LEVEL(foxy.getAI_LEVEL() + 1);
                            }
                        }
                        case 6 -> {
                            stopAllSounds();
                            nightRunning = false;
                            FNAFMain.fnafMain.menu = Menu.NIGHT_END;
                            sounds.get("chimes 2.ogg").play();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            sounds.get("cheer.ogg").play();
                            nightRunning = false;
                            if(fnafMain.nightNumber < 5) fnafMain.nightNumber++;

                            System.out.println("Night " + night + " completed");

                            if(night == 5) fnafMain.night6Unlocked = true;
                            if(night == 6) fnafMain.customNightUnlocked = true;
                            if(night == 7 && bonnieAI + chicaAI + freddyAI + foxyAI == 80) fnafMain.ventaBlackNightUnlocked = true;
                            if(night == 8) fnafMain.ventaBlackNightCompleted = true;
                            System.out.println("Night " + night + " completed");

                            fnafMain.save();

                            try {
                                FileOutputStream outputStream = new FileOutputStream(new File("save.txt"));
                                outputStream.write(fnafMain.nightNumber);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            try {
                                Thread.sleep(8000);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            stopAllSounds();

                            if(night == 5){
                                fnafMain.menu = Menu.ENDING_WEEK_END;
                                sounds.get("powerout.ogg").play();
                                try {
                                    Thread.sleep(20000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            if(night == 6){
                                fnafMain.menu = Menu.ENDING_WEEK_OVERTIME;
                                sounds.get("powerout.ogg").play();
                                System.out.println(sounds.get("powerout.ogg").getVolume());
                                try {
                                    Thread.sleep(20000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            if(night == 7){
                                fnafMain.menu = Menu.ENDING_FIRED;
                                sounds.get("powerout.ogg").play();
                                try {
                                    Thread.sleep(20000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            if(night == 8){
                                fnafMain.menu = Menu.ENDING_FIRED;
                                sounds.get("powerout.ogg").play();
                                sounds.get("powerout.ogg").setVolume(.6f);
                                sounds.get("Ventablack.ogx").setVolume(0);
                                sounds.get("Ventablack.ogx").play();
                                long timer = System.currentTimeMillis();
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                while (System.currentTimeMillis() - timer < 20000) {
                                    try {
                                        int sleep = (int) Math.min(random.nextInt(fnafMain.menu == Menu.ENDING_VENTA ? 100 : 200,fnafMain.menu == Menu.ENDING_VENTA ? 200 : 1500),20000 - (System.currentTimeMillis() - timer));
                                        System.out.println(sleep);
                                        Thread.sleep(sleep);
                                        if(fnafMain.menu == Menu.ENDING_FIRED) {
                                            sounds.get("Static2.ogg").play();
                                            sounds.get("Static2.ogg").setVolume(.6f);
                                            fnafMain.menu = Menu.ENDING_VENTA;
                                            sounds.get("Ventablack.ogx").setVolume(5);
                                        } else {
                                            sounds.get("Static2.ogg").stop();
                                            fnafMain.menu = Menu.ENDING_FIRED;
                                            sounds.get("Ventablack.ogx").setVolume(0);
                                        }
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                sounds.get("scream.ogg").play();
                                sounds.get("Static2.ogg").play();
                                sounds.get("Static2.ogg").setVolume(.6f);
                                fnafMain.menu = Menu.ENDING_VENTA;
                                sounds.get("Ventablack.ogx").setVolume(100);
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            stopAllSounds();
                            sounds.get("powerout.ogg").setVolume(1);

                            if(customNight || ventaNight || night == 5 || night == 6){
                                fnafMain.menu = Menu.MAIN_MENU;
                                sounds.get("Static2.ogg").play();
                                sounds.get("Mainmenu1.ogg").play();
                                sounds.get("Mainmenu1.ogg").setRepeating(true);
                            } else {
                                fnafMain.startTime = System.currentTimeMillis() - 3000;
                                fnafMain.menu = Menu.PRE_GAME;
                                sounds.get("blip.ogg").play();
                            }
                        }
                    }
                }

                if(hour == 6) break;

                if(!isPowerout){
                    if(random.nextInt(200000) == 1){
                        sounds.get("Circus.ogg").setVolume(0.1f);
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
                                sounds.get("chica_kitchen1.oga").setAngle(0);
                                sounds.get("chica_kitchen1.oga").setVolume(0.7f);
                                sounds.get("chica_kitchen1.oga").play();
                            } else {
                                sounds.get("chica_kitchen1.oga").setAngle(45f);
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
                    if(camera.equals(freddy.getCurrentCamera())) {
                        if(!ventaNight) freddy.setStalledUntil(System.currentTimeMillis() + random.nextInt(2000, 10000));
                        else freddy.setStalledUntil(System.currentTimeMillis() + random.nextInt(800, 3000));
                    }
                    else freddy.setStalledUntil(System.currentTimeMillis());
                    if(!ventaNight) foxy.setStalledUntil(System.currentTimeMillis() + random.nextInt(2000,20000));
                    else foxy.setStalledUntil(System.currentTimeMillis() + random.nextInt(1000,6000));
                    Thread t = new Thread(() -> {
                        synchronized (CAMERA_DOWN_WAIT) {
                            System.out.println("Camera Down");
                            CAMERA_DOWN_WAIT.notifyAll();
                        }
                    });
                    t.start();
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

                Thread freddyOfficeEnter = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("freddy enter");
                        int talking = random.nextInt(4) + 1;
                        sounds.get("breath_talking." + talking + ".ogg").setVolume(0.05f);
                        sounds.get("breath_talking." + talking + ".ogg").play();
                        sounds.get("breath_talking." + talking + ".ogg").setRepeating(true);
                        try {
                            synchronized (CAMERA_DOWN_WAIT) {
                                freddy.moveTo(OfficeCamera.getInstance());
                                long sleep = random.nextInt(15000,25000);
                                System.out.println("Freddy entered the office, waiting " + sleep);
                                Thread.sleep(sleep);
                                if(foxy.getCurrentCamera().equals(InOfficeCamera.getInstance())) return;
                                if(nightRunning && isCameraUp) {
                                    sleep = random.nextInt(10000,20000);
                                    System.out.println("Waiting for camera down, max " + sleep);
                                    CAMERA_DOWN_WAIT.wait(sleep);
                                    if(isCameraUp) {
                                        if(fnafMain.cameraStage == 11) for (int i = 10; i >= 0; i--) {
                                            fnafMain.cameraStage = i;
                                            try {
                                                Thread.sleep(16);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        fnafMain.cameraStage = -1;
                                        fnafMain.menu = Menu.OFFICE;
                                    }
                                }
                                if (nightRunning && !isPowerout) FNAFMain.fnafMain.triggerJumpScare("freddy.jump.",27,true);
                                nightRunning = false;
                            }
                        } catch (Exception e){

                        }
                    }
                });
                if(freddy.getCurrentCamera().equals(Camera4B.getInstance()) && isCameraUp && !office.isRightDoor()) {
                    freddyOfficeEnter.start();
                }
                if(!nightRunning) freddyOfficeEnter.interrupt();

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
                    Thread.sleep(freddyInterval);
                    if(chica.getCurrentCamera().equals(Camera1A.getInstance()) || bonnie.getCurrentCamera().equals(Camera1A.getInstance())){
                        continue;
                    }
                    if(freddy.stillStalledFor() > 0){
                        continue;
                    }
                    if(isCameraUp && camera.equals(freddy.getCurrentCamera())) {
                        continue;
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
                    Thread.sleep(bonnieInterval);
                    if(move(bonnie.getAI_LEVEL())) {
                        if(bonnie.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                            if(Office.getInstance().isLeftDoor()){
                                Office.getInstance().setLeftDoorAnimatronic(null);
                                FNAFMain.sounds.get("Knock2.ogg").play();
                                bonnie.moveTo(Camera1B.getInstance());
                            } else {
                                System.out.println("Bonnie has entered the office");
                                bonnie.setCurrentCamera(InOfficeCamera.getInstance());
                                Office.getInstance().setLeftLight(false);
                                synchronized (CAMERA_DOWN_WAIT) {
                                    CAMERA_DOWN_WAIT.wait();
                                }
                                if(freddy.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                                    freddy.setCurrentCamera(Camera1A.getInstance());
                                }
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
                    Thread.sleep(chicaInterval);
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
                                Office.getInstance().setRightLight(false);
                                synchronized (CAMERA_DOWN_WAIT) {
                                    CAMERA_DOWN_WAIT.wait();
                                }
                                if(freddy.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                                    freddy.setCurrentCamera(Camera1A.getInstance());
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
                    Thread.sleep(foxyInterval);
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
                            if(ventaNight){
                                if(foxy.getPowerDrain() == 2) foxy.setPowerDrain(4);
                                if(foxy.getPowerDrain() == 1) foxy.setPowerDrain(2);
                                if(foxy.getPowerDrain() == 0) foxy.setPowerDrain(1);
                            } else {
                                if(foxy.getPowerDrain() == 6) foxy.setPowerDrain(11);
                                if(foxy.getPowerDrain() == 1) foxy.setPowerDrain(6);
                                if(foxy.getPowerDrain() == 0) foxy.setPowerDrain(1);
                            }
                            power-=foxy.getPowerDrain();
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
