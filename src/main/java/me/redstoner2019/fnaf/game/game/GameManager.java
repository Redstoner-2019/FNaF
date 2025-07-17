package me.redstoner2019.fnaf.game.game;

import me.redstoner2019.audio.Sound;
import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.Menu;
import me.redstoner2019.fnaf.game.Distribution;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.NightConfiguration;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Foxy;
import me.redstoner2019.fnaf.game.animatronics.Freddy;
import me.redstoner2019.fnaf.game.cameras.*;
import me.redstoner2019.fnaf.game.rendering.HallucinationRenderer;
import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.graphics.general.TextureProvider;
import me.redstoner2019.util.http.Method;
import me.redstoner2019.util.http.Requests;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Handler;

import static me.redstoner2019.fnaf.FNAFMain.*;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;

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
    public boolean isEndless = false;
    private boolean keysAllowed = false;
    private boolean goldenFreddy = false;
    private boolean prepGoldenFreddy = false;
    private long goldenFreddyAppearTime = 0;

    private Thread freddyThread = null;
    private Thread foxyThread = null;
    private Thread bonnieThread = null;
    private Thread chicaThread = null;

    private final Object CAMERA_DOWN_WAIT = new Object();
    private final Object CAMERA_UP_WAIT = new Object();
    private final Object FOXY_RUN_SLEEP = new Object();

    private String challenge = null;

    boolean nightRunning = false;

    /**
     * Tracking
     */

    private int foxyAttacks = 0;
    private String deathTo = "Survived";
    private int timesLeftDoorClosed = 0;
    private int timesRightDoorClosed = 0;
    private int hallucinations = 0;

    private GameManager(){

    }

    public boolean isGoldenFreddy() {
        return goldenFreddy;
    }

    public void setGoldenFreddy(boolean goldenFreddy) {
        this.goldenFreddy = goldenFreddy;
    }

    public boolean isKeysAllowed() {
        return keysAllowed;
    }

    public void setKeysAllowed(boolean keysAllowed) {
        this.keysAllowed = keysAllowed;
    }

    public void endFreddy(){
        if(!freddyThread.isInterrupted()) freddyThread.interrupt();
    }
    public void endFoxy(){
        if(!foxyThread.isInterrupted()) foxyThread.interrupt();
    }
    public void endBonnie(){
        if(!bonnieThread.isInterrupted()) bonnieThread.interrupt();
    }
    public void endChica(){
        if(!chicaThread.isInterrupted()) chicaThread.interrupt();
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
        startNight(NightConfiguration.getNight(night));
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

    public void startNight(NightConfiguration nightConfiguration){
        if(nightConfiguration.getNightNumber() != 7) nightConfiguration = NightConfiguration.getNight(nightConfiguration.getNightNumber());
        if(nightConfiguration.getNightNumber() == 8) setKeysAllowed(true);

        Sound giggle = new Sound(FNAFMain.sounds.get("giggle.ogg.ogx").getFilepath(),false);

        if(nightRunning) return;
        nightStart = System.currentTimeMillis();
        nightRunning = true;
        camera = Camera1A.getInstance();
        lastCameraCheck = System.currentTimeMillis();
        power = 100;
        isBlackout = false;
        isPowerout = false;
        nightNumber = nightConfiguration.getNightNumber();
        this.hallucinations = 0;
        final int[] night = {nightConfiguration.getNightNumber()};

        goldenFreddy = false;
        goldenFreddyAppearTime = 0;
        prepGoldenFreddy = false;

        System.out.println("Starting night " + nightConfiguration.getNightNumber());

        if(nightConfiguration.getNightNumber() == 7){
            customNight = true;
        }

        if(freddyThread != null && !freddyThread.isInterrupted()) freddyThread.interrupt();
        if(bonnieThread != null && !bonnieThread.isInterrupted()) bonnieThread.interrupt();
        if(chicaThread != null && !chicaThread.isInterrupted()) chicaThread.interrupt();
        if(foxyThread != null && !foxyThread.isInterrupted()) foxyThread.interrupt();

        isEndless = nightConfiguration.isEndlessNight();

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

        freddy.setAI_LEVEL(nightConfiguration.getFreddyAI());
        bonnie.setAI_LEVEL(nightConfiguration.getBonnieAI());
        chica.setAI_LEVEL(nightConfiguration.getChicaAI());
        foxy.setAI_LEVEL(nightConfiguration.getFoxyAI());

        freddyInterval = nightConfiguration.getFreddyMovementSpeed();
        bonnieInterval = nightConfiguration.getBonnieMovementSpeed();
        chicaInterval = nightConfiguration.getChicaMovementSpeed();
        foxyInterval = nightConfiguration.getFoxyMovementSpeed();

        System.out.println(nightConfiguration);

        if(nightNumber == 8) {
            sounds.get("ventablacklong.ogg").setVolume(1f);
            sounds.get("ventablacklong.ogg").play();
            sounds.get("ventablacklong.ogg").setRepeating(true);
        }

        System.out.println("Night " + nightNumber);
        System.out.println(nightConfiguration.convertToJSON().toString(3));

        if(nightNumber == 1 && !fileData.optBoolean("phoneguy_night1", false)) {
            fileData.put("phoneguy_night1", true);
            sounds.get("voiceover1.ogg").setVolume(1f);
            sounds.get("voiceover1.ogg").play();
        }

        if(nightNumber == 2 && !fileData.optBoolean("phoneguy_night2", false)) {
            fileData.put("phoneguy_night2", true);
            sounds.get("voiceover1.ogg").setVolume(1f);
            sounds.get("voiceover1.ogg").play();
        }

        if(nightNumber == 3 && !fileData.optBoolean("phoneguy_night3", false)) {
            fileData.put("phoneguy_night3", true);
            sounds.get("voiceover1.ogg").setVolume(1f);
            sounds.get("voiceover1.ogg").play();
        }

        if(nightNumber == 4 && !fileData.optBoolean("phoneguy_night4", false)) {
            fileData.put("phoneguy_night4", true);
            sounds.get("voiceover1.ogg").setVolume(1f);
            sounds.get("voiceover1.ogg").play();
        }

        if(nightNumber == 5 && !fileData.optBoolean("phoneguy_night5", false)) {
            fileData.put("phoneguy_night5", true);
            sounds.get("voiceover1.ogg").setVolume(1f);
            sounds.get("voiceover1.ogg").play();
        }

        idleUsage = nightConfiguration.getIdleUsage();
        challenge = nightConfiguration.getChallenge();

        setNIGHT_LENGTH(nightConfiguration.getNightLength());

        if(NIGHT_LENGTH == 0) NIGHT_LENGTH = 6 * 60000;


        if(nightConfiguration.getNightNumber() == 7){
            customNight = true;
        }
        if(nightConfiguration.getNightNumber() == 8){
            ventaNight = true;
        }

        /*System.out.println(nightConfiguration);

        /*switch (night) {
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
        }*/

        NightConfiguration finalNightConfiguration = nightConfiguration;

        freddyThread = new Thread(() -> {
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
                    if(move(freddy.getAI_LEVEL())) {
                        freddy.move();
                        triggerCameraBlackout();
                    }
                    else System.out.println("Freddy Movement Failed");
                } catch (InterruptedException e) {

                }
            }
            System.out.println("Freddy Ended");
        });

        bonnieThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(bonnieInterval);
                    if(move(bonnie.getAI_LEVEL())) {
                        if(bonnie.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                            if(Office.getInstance().isLeftDoor()){
                                if(bonnie.getAI_LEVEL() < random.nextInt(41)) {
                                    Office.getInstance().setLeftDoorAnimatronic(null);
                                    Office.getInstance().setLeftLight(false);
                                    //FNAFMain.sounds.get("Knock2.ogg").play();
                                    bonnie.moveTo(Camera1B.getInstance());
                                }
                            } else {
                                System.out.println("Bonnie has entered the office");
                                bonnie.setCurrentCamera(InOfficeCamera.getInstance());
                                Office.getInstance().setLeftLight(false);
                                System.out.println("Bonnie entering office complete");
                                synchronized (CAMERA_DOWN_WAIT) {
                                    System.out.println("Bonnie Waiting for Cam Down");
                                    CAMERA_DOWN_WAIT.wait();
                                    System.out.println("Cam Down Success");
                                }
                                if(!nightRunning || isPowerout) return;
                                System.out.println("Camera Down Sync done");
                                chicaThread.interrupt();
                                foxyThread.interrupt();
                                freddyThread.interrupt();
                                nightRunning = false;
                                deathTo = "Bonnie";
                                FNAFMain.fnafMain.triggerJumpScare("bonnie.jump.",10,true);
                                sendData();
                            }
                        } else {
                            bonnie.move();
                            triggerCameraBlackout();
                        }
                    }
                    else System.out.println("Bonnie Movement Failed");
                } catch (InterruptedException e) {

                }
            }
            System.out.println("Bonnie Ended");
        });

        chicaThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(chicaInterval);
                    if(move(chica.getAI_LEVEL())) {
                        if(chica.getCurrentCamera().equals(OfficeCamera.getInstance())) {
                            if(Office.getInstance().isRightDoor()){
                                if(chica.getAI_LEVEL() < random.nextInt(41)) {
                                    Office.getInstance().setRightDoorAnimatronic(null);
                                    //FNAFMain.sounds.get("Knock2.ogg").play();
                                    Office.getInstance().setRightLight(false);
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
                                }
                            } else {
                                System.out.println("Chica has entered the office");
                                chica.setCurrentCamera(InOfficeCamera.getInstance());
                                Office.getInstance().setRightLight(false);
                                synchronized (CAMERA_DOWN_WAIT) {
                                    CAMERA_DOWN_WAIT.wait();
                                }
                                if(!nightRunning || isPowerout) return;
                                endFreddy();
                                endBonnie();
                                endFoxy();
                                nightRunning = false;
                                deathTo = "Chica";
                                FNAFMain.fnafMain.triggerJumpScare("chica.jump.", 15, true);
                                sendData();
                            }
                        } else {
                            chica.move();
                            triggerCameraBlackout();
                        }
                    }
                    else System.out.println("Chica Movement Failed");
                } catch (InterruptedException e) {

                }
            }
            System.out.println("Chica Ended");
        });

        foxyThread = new Thread(() -> {
            while (nightRunning) {
                try {
                    Thread.sleep(foxyInterval);
                    if(foxy.stillStalledFor() > 0){
                        continue;
                    }
                    if(isCameraUp && camera.equals(foxy.getCurrentCamera())) {
                        continue;
                    }
                    if(move(foxy.getAI_LEVEL())) {
                        foxy.move();
                        triggerCameraBlackout();
                    }
                    else System.out.println("Foxy Movement Failed");

                    if(foxy.getStage() == 3){

                        if(!sounds.get("pirate_song.ogg").isPlaying() && new Random().nextBoolean()) {
                            sounds.get("pirate_song.ogg").setVolume(.35f);
                            sounds.get("pirate_song.ogg").play();
                        }

                        synchronized (FOXY_RUN_SLEEP) {
                            FOXY_RUN_SLEEP.wait(10000);
                        }

                        if(isPowerout || !nightRunning){
                            continue;
                        }

                        if(camera.equals(Camera2A.getInstance())){
                            sounds.get("FoxyRun.ogg").setVolume(10.0f);
                            sounds.get("FoxyRun.ogg").play();
                        } else {
                            sounds.get("FoxyRun.ogg").stop();
                        }



                        for (int i = 0; i < 30; i++) {
                            foxy.setRun_animation_image(i);
                            Thread.sleep(32);
                        }
                        foxy.setRun_animation_image(-1);
                        Thread.sleep(500);

                        if(Office.getInstance().getLeftDoorState() == DoorState.CLOSED){
                            sounds.get("FoxyKnock.ogg").play();
                            foxyAttacks++;
                            if(ventaNight){
                                if(foxy.getPowerDrain() == 2) foxy.setPowerDrain(7);
                                if(foxy.getPowerDrain() == 1) foxy.setPowerDrain(3);
                                if(foxy.getPowerDrain() == 0) foxy.setPowerDrain(1);
                            } else {
                                if(foxy.getPowerDrain() == 6) foxy.setPowerDrain(11);
                                if(foxy.getPowerDrain() == 1) foxy.setPowerDrain(6);
                                if(foxy.getPowerDrain() == 0) foxy.setPowerDrain(1);
                            }
                            power-=foxy.getPowerDrain();
                            foxy.setStage(random.nextInt(2));
                        } else {
                            if(!nightRunning || isPowerout) return;
                            endFreddy();
                            endBonnie();
                            endChica();
                            foxy.setCurrentCamera(InOfficeCamera.getInstance());
                            fnafMain.menu = Menu.OFFICE;
                            deathTo = "Foxy";
                            fnafMain.triggerJumpScare("foxy.enter.",20,true);
                            nightRunning = false;
                            sendData();
                        }
                    }
                } catch (InterruptedException e) {

                }
            }
            System.out.println("Foxy Ended");
        });

        Thread randomEvents = new Thread(new Runnable() {
            @Override
            public void run() {
                while (nightRunning) {
                    try {
                        Thread.sleep(50);
                        if(isPowerout) break;
                        if(random.nextInt(600) == 50){
                            if(!sounds.get("swirl_ambience.ogg").isPlaying()) sounds.get("swirl_ambience.ogg").play();
                        }
                        if(random.nextInt(2500) == 50){
                            if(!sounds.get("pirate_song.ogg").isPlaying()) {
                                sounds.get("pirate_song.ogg").setVolume(.35f);
                                sounds.get("pirate_song.ogg").play();
                            }
                        }
                        if(random.nextInt(6000) == 50 && hallucinations < 2){
                            HallucinationRenderer.start();
                            hallucinations++;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Thread mainManagement = new Thread(() -> {
            GameManager gameManager = getInstance();
            Office office = Office.getInstance();
            float deltaTime = 0;
            boolean previousWasGoldenFreddy = false;
            while (nightRunning) {
                //try{
                    Thread freddyOfficeEnter = null;
                    int prevHour = hour;
                    long nightTime = System.currentTimeMillis() - nightStart;
                    hour = (int) (nightTime / (Math.max(NIGHT_LENGTH,1) / 6));
                    double updateThreadStart = glfwGetTime();

                    if(prevHour != hour){
                        switch (hour){
                            case 2 -> {
                                if(nightNumber <= 6) bonnie.setAI_LEVEL(bonnie.getAI_LEVEL() + 1);
                            }
                            case 3,4 -> {
                                if(nightNumber <= 6) {
                                    bonnie.setAI_LEVEL(bonnie.getAI_LEVEL() + 1);
                                    chica.setAI_LEVEL(chica.getAI_LEVEL() + 1);
                                    foxy.setAI_LEVEL(foxy.getAI_LEVEL() + 1);
                                }
                            }
                            case 6 -> {
                                System.out.println("\n\n");
                                System.out.println("Night Ending");
                                System.out.println("\n\n");

                                if(isEndless) continue;

                                endChica();
                                endBonnie();
                                endFreddy();
                                endFoxy();
                                if(freddyOfficeEnter != null) freddyOfficeEnter.interrupt();

                                stopAllSounds();
                                nightRunning = false;

                                Thread statConnector = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sendData();
                                    }
                                });
                                statConnector.start();

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

                                System.out.println("Night " + night[0] + " completed");

                                if(night[0] == 5) fnafMain.night6Unlocked = true;
                                if(night[0] == 6) fnafMain.customNightUnlocked = true;
                                if(night[0] == 7 && finalNightConfiguration.getBonnieAI() + finalNightConfiguration.getChicaAI() + finalNightConfiguration.getFreddyAI() + finalNightConfiguration.getFoxyAI() == 80
                                && freddyInterval == 3820
                                && bonnieInterval == 4970
                                && chicaInterval == 4980
                                && foxyInterval == 5010) fnafMain.ventaBlackNightUnlocked = true;
                                if(night[0] == 8) fnafMain.ventaBlackNightCompleted = true;
                                System.out.println("Night " + night[0] + " completed");

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

                                if(night[0] == 5){
                                    fnafMain.menu = Menu.ENDING_WEEK_END;
                                    sounds.get("powerout.ogg").play();
                                    try {
                                        Thread.sleep(20000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                if(night[0] == 6){
                                    fnafMain.menu = Menu.ENDING_WEEK_OVERTIME;
                                    sounds.get("powerout.ogg").play();
                                    System.out.println(sounds.get("powerout.ogg").getVolume());
                                    try {
                                        Thread.sleep(20000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                if(night[0] == 7){
                                    fnafMain.menu = Menu.ENDING_FIRED;
                                    sounds.get("powerout.ogg").play();
                                    try {
                                        Thread.sleep(20000);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                if(night[0] == 8){
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

                                if(customNight || ventaNight || night[0] == 5 || night[0] == 6){
                                    fnafMain.menu = Menu.MAIN_MENU;
                                    sounds.get("Static2.ogg").play();
                                    sounds.get("Mainmenu1.ogg").play();
                                    sounds.get("Mainmenu1.ogg").setRepeating(true);
                                } else {
                                    fnafMain.nightConfiguration = NightConfiguration.getNight(fnafMain.nightNumber);
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
                            sounds.get("Circus.ogg").setVolume(0.3f);
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
                            if(random.nextInt(10) == 1){
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
                        System.out.println("Camera Down Trigger");
                        synchronized (CAMERA_DOWN_WAIT) {
                            System.out.println("Camera Down");
                            CAMERA_DOWN_WAIT.notifyAll();

                            if(prepGoldenFreddy){
                                prepGoldenFreddy = false;
                                if(!goldenFreddy && !previousWasGoldenFreddy) {
                                    goldenFreddyAppearTime = System.currentTimeMillis();
                                    goldenFreddy = true;
                                }
                            }
                            previousWasGoldenFreddy = false;
                        }
                    } else if(isCameraUp && !lastCameraUp){
                        if(random.nextInt(15) == 1){
                            cameraBlackout = System.currentTimeMillis() + random.nextInt(10000) + 5000;
                        }

                        synchronized (CAMERA_UP_WAIT) {
                            CAMERA_UP_WAIT.notifyAll();
                            if(goldenFreddy) {
                                goldenFreddy = false;
                                previousWasGoldenFreddy = true;
                                goldenFreddyAppearTime = 0;
                            }

                            System.out.println(previousWasGoldenFreddy);
                            if(!previousWasGoldenFreddy) {
                                prepGoldenFreddy = random.nextFloat() < finalNightConfiguration.getGoldenFreddyChance();
                                if(prepGoldenFreddy) giggle.play();
                            }
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

                    if(isGoldenFreddy() && System.currentTimeMillis() - goldenFreddyAppearTime > 1000){
                        if(!nightRunning || isPowerout) return;
                        endFreddy();
                        endBonnie();
                        endFoxy();
                        endChica();
                        goldenFreddy = false;
                        goldenFreddyAppearTime = 0;
                        prepGoldenFreddy = false;
                        nightRunning = false;
                        deathTo = "Chica";

                        fnafMain.setJumpscareFrame(-1);
                        fnafMain.setJumpscare("golden.freddy.png");
                        sendData();
                    }

                    freddyOfficeEnter = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("freddy enter");
                            int talking = random.nextInt(4) + 1;
                            sounds.get("breath_talking." + talking + ".ogg").setVolume(0.3f);
                            sounds.get("breath_talking." + talking + ".ogg").play();
                            sounds.get("breath_talking." + talking + ".ogg").setRepeating(true);
                            try {
                                freddy.moveTo(OfficeCamera.getInstance());
                                long sleep = random.nextInt(15000,25000);
                                System.out.println("Freddy entered the office, waiting " + sleep);
                                Thread.sleep(sleep);
                                if(foxy.getCurrentCamera().equals(InOfficeCamera.getInstance())) return;
                                if(nightRunning && isCameraUp) {
                                    sleep = random.nextInt(10000,20000);
                                    System.out.println("Waiting for camera down, max " + sleep);
                                    endBonnie();
                                    endChica();
                                    endFoxy();
                                    synchronized (CAMERA_DOWN_WAIT) {
                                        CAMERA_DOWN_WAIT.wait(sleep);
                                    }
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
                                if (nightRunning && !isPowerout) {
                                    deathTo = "Freddy";
                                    FNAFMain.fnafMain.triggerJumpScare("freddy.jump.", 27, true);
                                    sendData();
                                }
                                nightRunning = false;
                            } catch (Exception e){

                            }
                        }
                    });
                    if(freddy.getCurrentCamera().equals(Camera4B.getInstance()) && isCameraUp && !office.isRightDoor() && !camera.equals(Camera4B.getInstance())) {
                        freddyOfficeEnter.start();
                    }
                    if(!nightRunning) freddyOfficeEnter.interrupt();

                    if(power > 0 && gameManager.isNightRunning()) {
                        float difficulty = getDifficulty();
                        float minReg = 0f;
                        float maxReg = 1.5f;

                        float reg = minReg + ((maxReg - minReg) * difficulty);

                        if(usage == 0 && isEndless){
                            power = Math.min(power + (reg * deltaTime * idleUsage), 100);
                        } else {
                            power -= ((idleUsage + (usage * 1.1f)) * deltaTime);
                            if(power == 0) power = -1;
                        }

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

                                    deathTo = "Power out";
                                    fnafMain.triggerJumpScare("freddy.blackout.", 20, true);
                                    sendData();
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
                /*}catch (Exception e){

                }*/


            }
        });

        mainManagement.start();
        randomEvents.start();
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

    public float getDifficulty(){
        int animatronicAI = Freddy.getInstance().getAI_LEVEL() + Foxy.getInstance().getAI_LEVEL() + Bonnie.getInstance().getAI_LEVEL() + Chica.getInstance().getAI_LEVEL();
        int animatronicSpeed = freddyInterval + foxyInterval + bonnieInterval + chicaInterval;

        float aniAI = animatronicAI / 80f;
        float aniSpeed = (animatronicSpeed - 400f) / (9500f * 4f);

        return aniAI * aniSpeed;
    }

    public void sendChallenge(){
        JSONObject request = new JSONObject();

        request.put("game","b98b9e85-6508-b355-f068-d792e1c251c8");
        request.put("version",FNAFMain.fnafMain.version);

        switch (challenge) {
            case "night_6" -> request.put("challengeId","5be56dfa-4480-cc1b-1481-f09593adac79");
            case "ventablack" -> request.put("challengeId","fa2f2058-db97-2e52-509a-64976e83ec73");
            default -> request.put("challengeId","");
        }

        JSONObject data = new JSONObject();

        switch (challenge) {
            case "ventablack_endless", "night_4_20_endless" -> {
                data.put("death",deathTo);
                data.put("timeLasted",System.currentTimeMillis() - nightStart);
                data.put("powerLeft",power);
                data.put("foxyAttacks",foxyAttacks);
                request.put("score",System.currentTimeMillis() - nightStart);
            }
            case "night_6", "night_4_20", "ventablack" -> {
                data.put("death",deathTo);
                data.put("powerLeft",power);
                //data.put("timeLasted",System.currentTimeMillis() - nightStart);
                //data.put("foxyAttacks",foxyAttacks);
                request.put("score",power*100);
            }
        }

        data.put("place",1);

        request.put("data",data);
        request.put("score", 0);




        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization","Bearer " + fnafMain.TOKEN);

        System.out.println(request.toString(3));

        JSONObject result = Requests.request(Method.POST,"https://stats.redstonerdev.io/stats/challengeEntry/create",request, headers);

        System.out.println(result.toString(3));

        if(result.getInt("code") == -1 || (result.getInt("code") != 200 && result.getInt("code") != 206)){
            try {
                File file = new File("waitingToSend.json");
                boolean exists = file.exists();
                FileOutputStream fos = new FileOutputStream(file);
                if(exists) {
                    FileInputStream fis = new FileInputStream(file);
                    JSONArray waiting = new JSONArray(new String(fis.readAllBytes()));
                    waiting.put(request);
                    fos.write(waiting.toString(3).getBytes());
                } else {
                    JSONArray waiting = new JSONArray();
                    waiting.put(request);
                    fos.write(waiting.toString(3).getBytes());
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            System.out.println(result.toString(3));
        }
    }

    public void sendData(){
        System.out.println("Challenge " + challenge);
        if(challenge != null) {
            sendChallenge();
        }
    }

    public void triggerCameraBlackout(){
        if(random.nextInt(50) == 1){
            cameraBlackout = System.currentTimeMillis() + random.nextInt(10000) + 5000;
        }
    }
}
