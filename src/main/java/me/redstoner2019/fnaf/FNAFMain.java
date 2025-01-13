package me.redstoner2019.fnaf;

import me.redstoner2019.Utilities;
import me.redstoner2019.client.AuthenticatorClient;
import me.redstoner2019.fnaf.game.NightConfiguration;
import me.redstoner2019.fnaf.game.game.Notification;
import me.redstoner2019.fnaf.game.stats.StatisticClient;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.general.*;
import me.redstoner2019.audio.Sound;
import me.redstoner2019.audio.SoundManager;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.animatronics.Bonnie;
import me.redstoner2019.fnaf.game.animatronics.Chica;
import me.redstoner2019.fnaf.game.animatronics.Foxy;
import me.redstoner2019.fnaf.game.animatronics.Freddy;
import me.redstoner2019.fnaf.game.cameras.*;
import me.redstoner2019.fnaf.game.game.GameManager;
import me.redstoner2019.graphics.general.Renderer;
import me.redstoner2019.util.http.Requests;
import org.json.JSONObject;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.redstoner2019.fnaf.Menu.CUSTOM_NIGHT;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

public class FNAFMain {

    private long window;
    private Texture loadingTexture;
    public static int width = 1280;
    public static int height = 720;
    public static float aspectRatio;
    private boolean showDebug = false;
    private TextureProvider textures = TextureProvider.getInstance();
    public static HashMap<String, Sound> sounds = new HashMap<>();
    private double mouseX[] = new double[1];
    private double mouseY[] = new double[1];
    private boolean isMouseClicked = false;
    public Menu menu = Menu.MAIN_MENU;
    private float deltaTime = 1;
    public int nightNumber = 1;
    private SoundManager soundManager;
    private float scroll = 0f;
    private int cameraRandomness = 0;
    private int cameraRandomness2 = 0;
    private Random random = new Random();
    private String jumpscare = null;
    private int jumpscareLength = 0;
    private int jumpscareFrame = 0;
    public static FNAFMain fnafMain;
    public GameManager gameManager = GameManager.getInstance();
    boolean fullscreen = false;
    private static final float defaultGlitchStrength = .2f;
    public float glitchStrength = defaultGlitchStrength;
    public long startTime = 0;
    public int cameraStage = -1;
    private boolean isCtrlDown = false;
    private boolean isShiftDown = false;
    private boolean vsync = false;
    private boolean exactNightTime = false;
    private long nightEndTime = 0;
    public List<Notification> notifications = new ArrayList<>();

    public boolean night6Unlocked = false;
    public boolean customNightUnlocked = false;
    public boolean ventaBlackNightUnlocked = false;
    public boolean ventaBlackNightCompleted = false;

    private int freddyAI = 8;
    private int bonnieAI = 0;
    private int chicaAI = 0;
    private int foxyAI = 8;

    private String TOKEN = "TOKEN";
    public StatisticClient client;
    public static boolean offlineMode = true;
    public static boolean loggedIn = false;
    public static String username = "";
    public static String displayName = "";
    public static long cameraBlackout = 0;

    public NightConfiguration nightConfiguration = new NightConfiguration(freddyAI,bonnieAI,chicaAI,foxyAI);

    private Renderer renderer;
    private TextRenderer textRenderer;
    private KeyboardInputHandler inputHandler = new KeyboardInputHandler();

    public String version = "v1.3.2";
    public int versionNumber = 1;

    public FNAFMain() {
        fnafMain = this;
    }

    public void setJumpscareFrame(int jumpscareFrame) {
        this.jumpscareFrame = jumpscareFrame;
    }

    public void setJumpscareLength(int jumpscareLength) {
        this.jumpscareLength = jumpscareLength;
    }

    public void setJumpscare(String jumpscare) {
        this.jumpscare = jumpscare;
    }

    public void run(String[] args) throws IOException {
        load();

        //TODO Change back to default
        client = new StatisticClient(Utilities.getIPData().getString("statistics-server"),Utilities.getIPData().getInt("statistics-server-port"));
        //client = new StatisticClient("localhost",Utilities.getIPData().getInt("statistics-server-port"));

        System.out.println("Stat client " + client.isConnected());

        if(new File("waitingToSend.json").exists() && client.isConnected()){
            System.out.println("Reading to send");
            FileInputStream fis = new FileInputStream("waitingToSend.json");
            try {
                System.out.println("Writing to send");
                client.sendRequest(new JSONObject(new String(fis.readAllBytes())));
                System.out.println("Request sent") ;
                new FileOutputStream("waitingToSend.json").write(new byte[0]);
                new File("waitingToSend.json").delete();
                System.out.println("To send deleted");
            } catch (Exception e) {

            }
        }

        System.out.println("Stat client " + client.isConnected());

        if(args.length > 0) TOKEN = args[0];

        loggedIn = false;

        init();

        System.out.println("Stat Server Connection: " + client.isConnected());
        System.out.println();

        if(client.isConnected()){
            JSONObject request = new JSONObject();
            request.put("token",TOKEN);
            JSONObject o = Requests.request("http://158.220.105.209:8080/verifyToken",request);

            System.out.println("Auth client Connected");

            if(o.getInt("status") != 0){
                loggedIn = false;
                System.out.println("Token not found");
            } else {
                o = Requests.request("http://158.220.105.209:8080/tokenInfo",request);
                username = o.getString("username");
                displayName = o.getString("displayname");
                System.out.println("Logged in as " + username);
                loggedIn = true;
            }
            offlineMode = false;
        }else {
            offlineMode = true;
            loggedIn = false;
            System.out.println("Offline mode, not connected to auth server");
        }

        save();
        loop();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(width, height, "OD Five Nights at Freddy's", MemoryUtil.NULL, MemoryUtil.NULL);

        GLFW.glfwSetWindowPos(window, (int) ((Toolkit.getDefaultToolkit().getScreenSize().getWidth() - width) / 2), (int) ((Toolkit.getDefaultToolkit().getScreenSize().getHeight() - height) / 2));

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        if(vsync) GLFW.glfwSwapInterval(1); //VSYNC
        else GLFW.glfwSwapInterval(0);

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                inputHandler.invoke(window,key,scancode,action,mods);
                if(menu == Menu.OFFICE){
                    if ((key == GLFW_KEY_SPACE || key == GLFW_KEY_S) && action == GLFW_RELEASE) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(cameraStage == -1) for (int i = 0; i <= 10; i++) {
                                    cameraStage = i;
                                    try {
                                        Thread.sleep(16);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                if(menu == Menu.OFFICE){
                                    cameraStage = 11;
                                    menu = Menu.CAMERAS;
                                }
                            }
                        });
                        t.start();
                        cameraRandomness = random.nextInt(100);
                        sounds.get("cameraFlip.oga").play();
                        sounds.get("blip.ogg").stop();
                        sounds.get("cameras.oga").play();
                        sounds.get("cameras.oga").setRepeating(true);
                    }

                    Office office = Office.getInstance();

                    if(key == GLFW_KEY_A && action == GLFW_RELEASE){
                        if(!gameManager.isPowerout()) {
                            if(Bonnie.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                if(office.isLeftDoor()) {
                                    office.setLeftDoorState(DoorState.OPENING);
                                    office.setLeftDoorAnimation(12);
                                }
                                else {
                                    office.setLeftDoorState(DoorState.CLOSING);
                                    office.setLeftDoorAnimation(0);
                                }
                                office.setLeftDoor(!office.isLeftDoor());
                                sounds.get("door.ogg").setVolume(1);
                                sounds.get("door.ogg").setAngle(-65,1);
                                sounds.get("door.ogg").stop();
                                sounds.get("door.ogg").play();
                            }
                        } else {
                            sounds.get("error.ogg").stop();
                            sounds.get("error.ogg").play();
                        }
                    }

                    if(key == GLFW_KEY_D && action == GLFW_RELEASE){
                        if(!gameManager.isPowerout()) {
                            if(Chica.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                if(office.isRightDoor()) {
                                    office.setRightDoorState(DoorState.OPENING);
                                    office.setRightDoorAnimation(12);
                                }
                                else {
                                    office.setRightDoorState(DoorState.CLOSING);
                                    office.setRightDoorAnimation(0);
                                }
                                office.setRightDoor(!office.isRightDoor());
                                sounds.get("door.ogg").setVolume(1);
                                sounds.get("door.ogg").stop();
                                sounds.get("door.ogg").setAngle(65,1);
                                sounds.get("door.ogg").play();
                            }
                        } else {
                            sounds.get("error.ogg").stop();
                            sounds.get("error.ogg").play();
                        }
                    }

                    if(key == GLFW_KEY_Q && action == GLFW_RELEASE){
                        if(!gameManager.isPowerout()) {
                            if(Bonnie.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                office.setLeftLight(!office.isLeftLight());
                                if(office.isRightLight()) office.setRightLight(false);
                                if(office.isLeftLight() || office.isRightLight()){
                                    sounds.get("lights.ogg").play();
                                    sounds.get("lights.ogg").setRepeating(true);
                                    if(office.getLeftDoorAnimatronic() != null){
                                        sounds.get("windowscare.ogg").play();
                                    }
                                } else {
                                    sounds.get("lights.ogg").stop();
                                }
                            }
                        } else {
                            sounds.get("error.ogg").stop();
                            sounds.get("error.ogg").play();
                        }
                    }

                    if(key == GLFW_KEY_E && action == GLFW_RELEASE){
                        if(!gameManager.isPowerout()) {
                            if(Chica.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                office.setRightLight(!office.isRightLight());
                                if(office.isLeftLight()) office.setLeftLight(false);
                                if(office.isLeftLight() || office.isRightLight()){
                                    sounds.get("lights.ogg").play();
                                    sounds.get("lights.ogg").setRepeating(true);
                                    if(office.getRightDoorAnimatronic() != null){
                                        sounds.get("windowscare.ogg").play();
                                    }
                                } else {
                                    sounds.get("lights.ogg").stop();
                                }
                            }
                        } else {
                            sounds.get("error.ogg").stop();
                            sounds.get("error.ogg").play();
                        }
                    }
                }

                if(menu == Menu.CAMERAS){
                    if ((key == GLFW_KEY_SPACE || key == GLFW_KEY_S) && action == GLFW_RELEASE) {
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(cameraStage == 11) for (int i = 10; i >= 0; i--) {
                                    cameraStage = i;
                                    try {
                                        Thread.sleep(16);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                                cameraStage = -1;
                            }
                        });
                        t.start();
                        cameraRandomness = random.nextInt(100);
                        sounds.get("cameraFlip.oga").play();
                        sounds.get("blip.ogg").stop();
                        sounds.get("cameras.oga").play();
                        sounds.get("cameras.oga").setRepeating(true);
                        menu = Menu.OFFICE;
                    }
                }

                if(key == GLFW_KEY_SPACE && action == GLFW_RELEASE){
                    /*sounds.get("door.ogg").setVolume(1);
                    sounds.get("door.ogg").stop();
                    sounds.get("door.ogg").play();
                    sounds.get("door.ogg").setAngle(90,10);*/
                }
                if ((key == GLFW_KEY_LEFT_CONTROL || key == GLFW_KEY_RIGHT_CONTROL) && action == GLFW_RELEASE) {
                    isCtrlDown = false;
                }
                if ((key == GLFW_KEY_LEFT_CONTROL || key == GLFW_KEY_RIGHT_CONTROL) && action == GLFW_PRESS) {
                    isCtrlDown = true;
                }

                if ((key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) && action == GLFW_RELEASE) {
                    isShiftDown = false;
                }
                if ((key == GLFW_KEY_LEFT_SHIFT || key == GLFW_KEY_RIGHT_SHIFT) && action == GLFW_PRESS) {
                    isShiftDown = true;
                }

                if (key == GLFW_KEY_V && action == GLFW_RELEASE && !loggedIn) {
                    sounds.get("ventablacklong.ogg").play();
                }
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_F1 && action == GLFW_RELEASE && !loggedIn) {
                    menu = Menu.OFFICE;
                }
                if (key == GLFW_KEY_F2 && action == GLFW_RELEASE && !loggedIn) {
                    gameManager.setNightStart(System.currentTimeMillis() - gameManager.getNIGHT_LENGTH() - 100);
                }
                if (key == GLFW_KEY_F3 && action == GLFW_RELEASE && !loggedIn) {
                    showDebug = !showDebug;
                }
                if (key == GLFW_KEY_F11 && action == GLFW_RELEASE) {
                    toggleFullscreen();
                }
                if (key == GLFW_KEY_KP_0 && action == GLFW_RELEASE && !loggedIn) {
                    gameManager.setPower(1);
                }
                if (key == GLFW_KEY_KP_1 && action == GLFW_RELEASE && !loggedIn) {
                    gameManager.setPower(100);
                }
                if (key == GLFW_KEY_J && action == GLFW_RELEASE && !loggedIn) {
                    switch (random.nextInt(5)) {
                        case 0 : {
                            jumpscare = "bonnie.jump.";
                            jumpscareFrame = 0;
                            jumpscareLength = 10;
                            break;
                        }
                        case 1 : {
                            jumpscare = "chica.jump.";
                            jumpscareFrame = 0;
                            jumpscareLength = 15;
                            break;
                        }
                        case 2 : {
                            jumpscare = "freddy.jump.";
                            jumpscareFrame = 0;
                            jumpscareLength = 27;
                            break;
                        }
                        case 3 : {
                            jumpscare = "freddy.blackout.";
                            jumpscareFrame = 0;
                            jumpscareLength = 20;
                            break;
                        }
                        case 4 : {
                            jumpscare = "foxy.enter.";
                            jumpscareFrame = 0;
                            jumpscareLength = 20;
                            break;
                        }
                    }
                    triggerJumpScare(jumpscare,jumpscareLength,false);
                }
            }
        });

        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                width = w;
                height = h;
                renderer.setHeight(h);
                renderer.setWidth(w);
                GL11.glViewport(0, 0, width, height);
                updateProjectionMatrix();
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            System.out.println("interact");
            if (action == GLFW.GLFW_PRESS) {
                isMouseClicked = true;

                float mx = (float) ((mouseX[0] / width) * 2) - 1;
                float my = (float) ((mouseY[0] / height) * 2) - 1;

                Office office = Office.getInstance();

                float offset = 25;

                if(menu == Menu.MAIN_MENU){

                    float titleFontSize = 80 * (height / 1080.0f);
                    float optionsYOffset = 6 * titleFontSize;
                    float optionsFontSize = 60 * (height / 1080.0f);

                    if(between(140 * (width / 1920f), 640 * (width / 1920f), mouseX[0])){
                        if(between(optionsYOffset + (optionsFontSize * 1.1f), optionsYOffset + (optionsFontSize * 1.9f),mouseY[0])){
                            menu = Menu.PRE_GAME;
                            startTime = System.currentTimeMillis();
                            nightNumber = 1;
                            nightConfiguration = NightConfiguration.getNight(nightNumber);
                            save();
                            load();
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                        }
                        if(between(optionsYOffset + (optionsFontSize * 2.1f), optionsYOffset + (optionsFontSize * 2.9f),mouseY[0])){
                            nightConfiguration = NightConfiguration.getNight(nightNumber);
                            menu = Menu.PRE_GAME;
                            startTime = System.currentTimeMillis();
                            save();
                            load();
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                        }
                        if(between(optionsYOffset + (optionsFontSize * 3.1f), optionsYOffset + (optionsFontSize * 3.9f),mouseY[0])){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            if(night6Unlocked){
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber = 6;
                                nightConfiguration = NightConfiguration.getNight(nightNumber);
                            } else {
                                Notification notification = new Notification("Complete Night 5 first!",3000,Color.RED, Color.GRAY);
                                notification.start();
                                notifications.add(notification);
                            }

                        }
                        if(between(optionsYOffset + (optionsFontSize * 4.1f), optionsYOffset + (optionsFontSize * 4.9f),mouseY[0])){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            if(customNightUnlocked){
                                load();
                                Freddy.getInstance().setAI_LEVEL(freddyAI);
                                Bonnie.getInstance().setAI_LEVEL(bonnieAI);
                                Chica.getInstance().setAI_LEVEL(chicaAI);
                                Foxy.getInstance().setAI_LEVEL(foxyAI);
                                menu = CUSTOM_NIGHT;
                            } else {
                                Notification notification = new Notification("Complete Night 6 first!",3000,Color.RED, Color.GRAY);
                                notification.start();
                                notifications.add(notification);
                            }
                        }
                        if(between(optionsYOffset + (optionsFontSize * 5.1f), optionsYOffset + (optionsFontSize * 5.9f),mouseY[0])){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            if(ventaBlackNightUnlocked){
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                                nightNumber = 8;
                                nightConfiguration = NightConfiguration.getNight(8);
                                System.out.println("Starting Venta Night");
                            } else {
                                Notification notification = new Notification("Complete Night 7, 4/20 first!",3000,Color.RED, Color.GRAY);
                                notification.start();
                                notifications.add(notification);
                            }
                        }
                        if(between(optionsYOffset + (optionsFontSize * 6.1f), optionsYOffset + (optionsFontSize * 6.9f),mouseY[0])){
                            menu = Menu.SETTINGS;
                            startTime = System.currentTimeMillis();
                        }
                        if(between(optionsYOffset + (optionsFontSize * 7.1f), optionsYOffset + (optionsFontSize * 7.9f),mouseY[0])){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            if(night6Unlocked){
                                if(loggedIn) menu = Menu.ONLINE;
                                else {
                                    Notification notification = new Notification("You are not Online!",3000,Color.RED, Color.GRAY);
                                    notification.start();
                                    notifications.add(notification);
                                }
                            } else {
                                Notification notification = new Notification("Complete Night 5 first!",3000,Color.RED, Color.GRAY);
                                notification.start();
                                notifications.add(notification);
                            }
                        }
                    }
                }else if(menu == Menu.OFFICE) {
                    if(!gameManager.isPowerout()) {
                        mx-=scroll;
                        if(between(-1.9f,-1.95f,mx) && between(-0.5,0.05,my)){
                            if(Bonnie.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                if(office.isLeftDoor()) {
                                    office.setLeftDoorState(DoorState.OPENING);
                                    office.setLeftDoorAnimation(12);
                                }
                                else {
                                    office.setLeftDoorState(DoorState.CLOSING);
                                    office.setLeftDoorAnimation(0);
                                }
                                office.setLeftDoor(!office.isLeftDoor());
                                sounds.get("door.ogg").setVolume(1);
                                sounds.get("door.ogg").stop();
                                sounds.get("door.ogg").setAngle(-65,1);
                                sounds.get("door.ogg").play();
                            }
                        }
                        if(between(-1.9f,-1.95f,mx) && between(0.13,0.23,my)){
                            if(Bonnie.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                office.setLeftLight(!office.isLeftLight());
                                if(office.isRightLight()) office.setRightLight(false);
                                if(office.isLeftLight() || office.isRightLight()){
                                    sounds.get("lights.ogg").play();
                                    sounds.get("lights.ogg").setRepeating(true);
                                    if(office.getLeftDoorAnimatronic() != null){
                                        sounds.get("windowscare.ogg").play();
                                    }
                                } else {
                                    sounds.get("lights.ogg").stop();
                                }
                            }
                        }

                        if(between(-.2 + scroll,-.17,mx + (scroll * .75f)) && between(-.355,-.330,my)){
                            sounds.get("honk.ogg").stop();
                            sounds.get("honk.ogg").play();
                        }

                        if(between(1.87f,1.93f,mx) && between(-0.5,0.05,my)){
                            if(Chica.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                if(office.isRightDoor()) {
                                    office.setRightDoorState(DoorState.OPENING);
                                    office.setRightDoorAnimation(12);
                                }
                                else {
                                    office.setRightDoorState(DoorState.CLOSING);
                                    office.setRightDoorAnimation(0);
                                }
                                office.setRightDoor(!office.isRightDoor());
                                sounds.get("door.ogg").setVolume(1);
                                sounds.get("door.ogg").stop();
                                sounds.get("door.ogg").setAngle(65,1);
                                sounds.get("door.ogg").play();
                            }
                        }
                        if(between(1.87f,1.93f,mx) && between(0.13,0.23,my)){
                            if(Chica.getInstance().getCurrentCamera().equals(InOfficeCamera.getInstance())){
                                sounds.get("error.ogg").play();
                            } else {
                                office.setRightLight(!office.isRightLight());
                                if(office.isLeftLight()) office.setLeftLight(false);
                                if(office.isLeftLight() || office.isRightLight()){
                                    sounds.get("lights.ogg").play();
                                    sounds.get("lights.ogg").setRepeating(true);
                                    if(office.getRightDoorAnimatronic() != null){
                                        sounds.get("windowscare.ogg").play();
                                    }
                                } else {
                                    sounds.get("lights.ogg").stop();
                                }
                            }

                        }
                    } else {
                        sounds.get("error.ogg").play();
                    }

                } else if (menu == Menu.CAMERAS){
                    my = my*-1;
                    float x = .625f;
                    float y = -.225f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera1A.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera1A.getInstance());
                    }

                    x = .6f;
                    y = -.35f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera1B.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera1B.getInstance());
                    }

                    x = .47f;
                    y = -.42f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera5.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera5.getInstance());
                    }

                    x = .57f;
                    y = -.525f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera1C.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera1C.getInstance());
                    }

                    x = .52f;
                    y = -.75f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera3.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera3.getInstance());
                    }

                    x = .625f;
                    y = -.82f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera2A.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera2A.getInstance());
                    }

                    y = -.9f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera2B.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera2B.getInstance());
                    }

                    x = .774f;
                    y = -.82f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera4A.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera4A.getInstance());
                    }

                    y = -.9f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera4B.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera4B.getInstance());
                    }

                    x = .9f;
                    y = -.725f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera6.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera6.getInstance());
                    }

                    y = -.42f;
                    if(between(x,x + 0.075f,mx) && between(y,y + 0.075f,my)){
                        if(!gameManager.getCamera().equals(Camera7.getInstance())) cameraRandomness2 = random.nextInt(101);
                        gameManager.setCamera(Camera7.getInstance());
                    }
                } else if(menu == Menu.ONLINE) {
                    float mod = height / 1080f;
                    float modX = width / 1920f;
                    float fontSize = 40 * mod;

                    int index = (int) (mouseY[0] / fontSize);

                    if(between(0,800 * modX,mouseX[0])){
                        switch (index) {
                            case 4 -> {
                                System.out.println("Create 1v1");
                            }
                            case 5 -> {
                                System.out.println("Join 1v1");
                            }
                            case 9 -> {
                                System.out.println("Night 6 run");
                                nightConfiguration = NightConfiguration.getNight(6);
                                nightConfiguration.setChallenge("night_6");
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                            }
                            case 10 -> {
                                System.out.println("Night 7, 4/20");
                                nightConfiguration = new NightConfiguration(20,20,20,20);
                                nightConfiguration.setNightNumber(7);
                                nightConfiguration.setChallenge("night_4_20");
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                            }
                            case 11 -> {
                                System.out.println("Ventablack");
                                nightConfiguration = NightConfiguration.getNight(8);
                                nightConfiguration.setChallenge("ventablack");
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                            }
                            case 13 -> {
                                System.out.println("Night 7, 4/20 endless");
                                nightConfiguration = new NightConfiguration(20,20,20,20);
                                nightConfiguration.setNightNumber(7);
                                nightConfiguration.setChallenge("night_4_20_endless");
                                nightConfiguration.setEndlessNight(true);
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                            }
                            case 14 -> {
                                System.out.println("Ventablack endless");
                                nightConfiguration = NightConfiguration.getNight(8);
                                nightConfiguration.setChallenge("ventablack_endless");
                                nightConfiguration.setEndlessNight(true);
                                menu = Menu.PRE_GAME;
                                startTime = System.currentTimeMillis();
                            }
                        }
                    }

                    if(between(-0.8,-1,mx) && between(0.8,1,my)){
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        menu = Menu.MAIN_MENU;
                        save();
                    }
                } else if(menu == Menu.SETTINGS) {
                    float mod = height / 1080f;
                    float fontSize = 60 * mod;

                    float checkBoxX = fontSize;
                    float checkBoxY = fontSize * 5;

                    if(between(-0.7,-1,mx) && between(0.7,1,my)){
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        menu = Menu.MAIN_MENU;
                        save();
                    }

                    if(between(checkBoxX,checkBoxX + fontSize,mouseX[0]) && between(checkBoxY,checkBoxY + fontSize,mouseY[0])){
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        vsync = !vsync;

                        if(vsync) GLFW.glfwSwapInterval(1);
                        else GLFW.glfwSwapInterval(0);
                        save();
                    }

                    checkBoxY+=(2 * fontSize);

                    if(between(checkBoxX,checkBoxX + fontSize,mouseX[0]) && between(checkBoxY,checkBoxY + fontSize,mouseY[0])){
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        exactNightTime = !exactNightTime;

                        save();
                    }
                } else if (menu == CUSTOM_NIGHT){
                    boolean yOnAI = between(0.15,0.3,my);
                    boolean yOnMovement = between(0.5,0.65,my);

                    float mod = height / 1080f;
                    float fontSize = 60 * mod;
                    float checkBoxX = ((-.1f+1)/2f) * width - 1.5f *fontSize;
                    float checkBoxY = ((.96f+1)/2f) * height - fontSize;

                    if(between(-0.15,-0.28,mx) && between(0.78f,0.9f,my)){
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        nightConfiguration.setEndlessNight(!nightConfiguration.isEndlessNight());

                        save();
                    }

                    int adder = 1;

                    if(yOnAI){
                        adder = 1;
                        if(isCtrlDown){
                            adder = 5;
                        }
                    } else if(yOnMovement){
                        adder = 100;
                        if(isCtrlDown && !isShiftDown){
                            adder = 1000;
                        }
                        if(isShiftDown && !isCtrlDown){
                            adder = 10;
                        }
                        if(isShiftDown && isCtrlDown){
                            adder = 1;
                        }
                    }



                    if(yOnAI){
                        if(between(-.95,-.85,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFreddyAI(Math.max(0, Math.min(nightConfiguration.getFreddyAI()-adder,20)));
                        }
                        if(between(-.45,-.35,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setBonnieAI(Math.max(0, Math.min(nightConfiguration.getBonnieAI()-adder,20)));
                        }
                        if(between(.05,.15,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setChicaAI(Math.max(0, Math.min(nightConfiguration.getChicaAI()-adder,20)));
                        }
                        if(between(.55,.65,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFoxyAI(Math.max(0, Math.min(nightConfiguration.getFoxyAI()-adder,20)));
                        }
                        if(between(-.55,-.65,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFreddyAI(Math.max(0, Math.min(nightConfiguration.getFreddyAI()+adder,20)));
                        }
                        if(between(-.15,-.05,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setBonnieAI(Math.max(0, Math.min(nightConfiguration.getBonnieAI()+adder,20)));
                        }
                        if(between(.35,.45,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setChicaAI(Math.max(0, Math.min(nightConfiguration.getChicaAI()+adder,20)));
                        }
                        if(between(.85,.95,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFoxyAI(Math.max(0, Math.min(nightConfiguration.getFoxyAI()+adder,20)));
                        }
                    }



                    if(yOnMovement){
                        if(between(-.95,-.85,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFreddyMovementSpeed(Math.max(100, Math.min(nightConfiguration.getFreddyMovementSpeed()-adder,9990)));
                        }
                        if(between(-.45,-.35,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setBonnieMovementSpeed(Math.max(100, Math.min(nightConfiguration.getBonnieMovementSpeed()-adder,9990)));
                        }
                        if(between(.05,.15,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setChicaMovementSpeed(Math.max(100, Math.min(nightConfiguration.getChicaMovementSpeed()-adder,9990)));
                        }
                        if(between(.55,.65,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFoxyMovementSpeed(Math.max(100, Math.min(nightConfiguration.getFoxyMovementSpeed()-adder,9990)));
                        }
                        if(between(-.55,-.65,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFreddyMovementSpeed(Math.max(100, Math.min(nightConfiguration.getFreddyMovementSpeed()+adder,9990)));
                        }
                        if(between(-.15,-.05,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setBonnieMovementSpeed(Math.max(100, Math.min(nightConfiguration.getBonnieMovementSpeed()+adder,9990)));
                        }
                        if(between(.35,.45,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setChicaMovementSpeed(Math.max(100, Math.min(nightConfiguration.getChicaMovementSpeed()+adder,9990)));
                        }
                        if(between(.85,.95,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            nightConfiguration.setFoxyMovementSpeed(Math.max(100, Math.min(nightConfiguration.getFoxyMovementSpeed()+adder,9990)));
                        }
                    }


                    if(between(0.7,1,mx) && between(0.7,1,my)){
                        System.out.println("start");
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        startTime = System.currentTimeMillis();
                        freddyAI = Freddy.getInstance().getAI_LEVEL();
                        bonnieAI = Bonnie.getInstance().getAI_LEVEL();
                        chicaAI = Chica.getInstance().getAI_LEVEL();
                        foxyAI = Foxy.getInstance().getAI_LEVEL();
                        save();
                        menu = Menu.PRE_GAME;
                        nightNumber = 7;
                        nightConfiguration.setNightNumber(7);
                    }

                    if(between(-0.7,-1,mx) && between(0.7,1,my)){
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        menu = Menu.MAIN_MENU;
                        startTime = System.currentTimeMillis();
                        freddyAI = Freddy.getInstance().getAI_LEVEL();
                        bonnieAI = Bonnie.getInstance().getAI_LEVEL();
                        chicaAI = Chica.getInstance().getAI_LEVEL();
                        foxyAI = Foxy.getInstance().getAI_LEVEL();
                        save();
                    }
                }
            } else if (action == GLFW.GLFW_RELEASE) {
                isMouseClicked = false;
            }
        });

        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        GL11.glEnable(GL13.GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        updateProjectionMatrix();

        renderer = Renderer.getInstance();
        textRenderer = TextRenderer.getInstance();

        renderer.setHeight(height);
        renderer.setWidth(width);

        setWindowIcon(window);

        System.out.println("Loading loading screen texture...");
        loadingTexture = Texture.loadTextureFromResource("textures/jump.jpg");
        textures.put("white.png",Texture.loadTextureFromResource("textures/white.png"));
        System.out.println();
    }

    public void startupLoading(int count, int max, String message){
        float progress = (float) max / count;
        float width = 1.46f * progress;

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderer.renderTexture(-1,-1,2,2,loadingTexture,true,false,0);
        renderer.renderTextureBounds(-0.75f,-0.6f,0.75f,-0.7f,textures.get("white.png"),true,false,0);
        renderer.renderTextureBounds(-0.74f,-0.61f,0.74f,-0.69f,textures.get("white.png"),true,false,0,Color.BLACK);
        renderer.renderTextureBounds(-0.73f,-0.62f,-0.73f + width,-0.68f,textures.get("white.png"),true,false,0);

        //message = "This is a Loading message";

        message = count + " / " + max + " loaded " + message;

        float textWidth = textRenderer.textWidth(message,20) / renderer.getWidth();

        float xOff = (0.72f - textWidth) / 2;

        textRenderer.renderText(message, ((textRenderer.fromNegativeRange(-0.75f) + xOff) * renderer.getWidth()) ,textRenderer.fromNegativeRange(0.72f) * renderer.getHeight() ,20, Color.WHITE);

        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }

    private void loop() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        Random random = new Random();
        long lastRandomChange = System.currentTimeMillis();

        /**
         * Performance Monitoring
         */
        long lastUpdate = System.currentTimeMillis();
        int frames = 0;
        int fps = 0;
        double lastFrameTime = 0;
        int componentsDrawn = 0;

        /**
         * Main Menu Data
         */

        boolean mainmenuFreddyTwitching = false;
        boolean noiseSwell = false;
        int mainMenuFreddyTwitchStage = 0;
        float baseNoise = .4f;
        float menuNoise = baseNoise;
        long noiseSwellStart = 0;
        int menuSelection = 0;

        /**
         * OFFICE data
         */

        boolean hasExitedCameraButton = true;
        int fan_stage = 0;

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        renderer.renderTexture(-1,-1,2,2,loadingTexture,true,false,0);

        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();

        try {
            soundManager = SoundManager.getInstance();

            JSONObject data = new JSONObject(new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("map.json").readAllBytes()));
            JSONObject texture = data.getJSONObject("textures");
            JSONObject audios = data.getJSONObject("audio");

            int loadCounter = 0;
            int totalToLoad = texture.keySet().size() + audios.keySet().size();

            for(String s : texture.keySet()){
                System.out.println("Loading buffered texture: " + s);
                textures.put(s,Texture.loadTextureFromResource(texture.getString(s)));
                System.out.println();

                loadCounter++;

                startupLoading(totalToLoad,loadCounter, s);
            }

            System.out.println("Loading sounds.");

            System.out.println();

            for(String s : audios.keySet()){
                Sound so = new Sound(audios.getString(s),false);
                sounds.put(s,so);

                loadCounter++;

                startupLoading(totalToLoad,loadCounter, s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        sounds.get("Static2.ogg").play();
        sounds.get("Mainmenu1.ogg").play();
        sounds.get("Mainmenu1.ogg").setRepeating(true);

        Thread cameraRandomizer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    cameraRandomness = random.nextInt(100);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        cameraRandomizer.start();

        while (!GLFW.glfwWindowShouldClose(window)) {
            double start = glfwGetTime();

            if(!client.isConnected()) {
                loggedIn = false;
            }

            //sounds.get("Mainmenu1.ogg").setAngle((float) start*10);

            glitchStrength-=0.005f*deltaTime;
            if(glitchStrength < defaultGlitchStrength) glitchStrength = defaultGlitchStrength;

            /**
             * Random Event Generator
             */

            if(System.currentTimeMillis() - lastRandomChange >= 10){
                lastRandomChange = System.currentTimeMillis();
                if(mainmenuFreddyTwitching){
                    if(mainMenuFreddyTwitchStage > 3){
                        mainMenuFreddyTwitchStage = 0;
                        mainmenuFreddyTwitching = false;
                    } else if(random.nextInt(10) == 2){
                        mainMenuFreddyTwitchStage++;
                        if(mainMenuFreddyTwitchStage == 4) {
                            mainMenuFreddyTwitchStage = 0;
                            mainmenuFreddyTwitching = false;
                        }
                    }
                } else if(random.nextInt(60) == 5){
                    mainmenuFreddyTwitching = true;
                }

                if(random.nextInt(120) == 1){
                    if(!noiseSwell) {
                        noiseSwell = true;
                        noiseSwellStart = System.currentTimeMillis();
                    }
                }

                fan_stage+=4;

                /**
                 * Door Management
                 */

                Office office = Office.getInstance();

                if(office.getLeftDoorState() == DoorState.CLOSING){
                    office.setLeftDoorAnimation(office.getLeftDoorAnimation() + 1);
                    if(office.getLeftDoorAnimation() == 14) office.setLeftDoorState(DoorState.CLOSED);
                }
                if(office.getLeftDoorState() == DoorState.OPENING){
                    office.setLeftDoorAnimation(office.getLeftDoorAnimation() - 1);
                    if(office.getLeftDoorAnimation() == -1) office.setLeftDoorState(DoorState.OPEN);
                }

                if(office.getRightDoorState() == DoorState.CLOSING){
                    office.setRightDoorAnimation(office.getRightDoorAnimation() + 1);
                    if(office.getRightDoorAnimation() == 14) office.setRightDoorState(DoorState.CLOSED);
                }
                if(office.getRightDoorState() == DoorState.OPENING){
                    office.setRightDoorAnimation(office.getRightDoorAnimation() - 1);
                    if(office.getRightDoorAnimation() == -1) office.setRightDoorState(DoorState.OPEN);
                }

                if(fan_stage >= 30) fan_stage = 0;
            }

            /**
             * Updating
             */

            //if (GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL) {
                GLFW.glfwGetCursorPos(window, mouseX, mouseY);
                switch (menu) {
                    case SETTINGS -> {
                        float mod = height / 1080f;
                        float fontSize = 60 * mod;
                        float volumeOffset = 2 * fontSize + textRenderer.textWidth("Volume (000.0%):", fontSize);
                        float volumeSliderWidth = (width - fontSize) - volumeOffset;

                        if(isMouseClicked) {
                            if(between(volumeOffset,volumeOffset+volumeSliderWidth,mouseX[0]) && between(fontSize * 3,fontSize * 4,mouseY[0])){
                                float volume = (float) Math.max(0,Math.min(1,(mouseX[0] - volumeOffset) / volumeSliderWidth));
                                soundManager.setVolume(volume);
                                for(Sound s : sounds.values()){
                                    try {
                                        s.updateGain();
                                    } catch (Exception e){}
                                }
                            }
                        }
                    }
                    case MAIN_MENU -> {
                        int selection = menuSelection;

                        float titleFontSize = 80 * (height / 1080.0f);
                        float optionsYOffset = 6 * titleFontSize;
                        float optionsFontSize = 60 * (height / 1080.0f);

                        if(between(140 * (width / 1920f), 640 * (width / 1920f), mouseX[0])){
                            if(between(optionsYOffset + (optionsFontSize * 1.1f), optionsYOffset + (optionsFontSize * 1.9f),mouseY[0])){
                                selection = 0;
                            }
                            if(between(optionsYOffset + (optionsFontSize * 2.1f), optionsYOffset + (optionsFontSize * 2.9f),mouseY[0])){
                                selection = 1;
                            }
                            if(between(optionsYOffset + (optionsFontSize * 3.1f), optionsYOffset + (optionsFontSize * 3.9f),mouseY[0])){
                                selection = 2;
                            }
                            if(between(optionsYOffset + (optionsFontSize * 4.1f), optionsYOffset + (optionsFontSize * 4.9f),mouseY[0])){
                                selection = 3;
                            }
                            if(between(optionsYOffset + (optionsFontSize * 5.1f), optionsYOffset + (optionsFontSize * 5.9f),mouseY[0])){
                                selection = 4;
                            }
                            if(between(optionsYOffset + (optionsFontSize * 6.1f), optionsYOffset + (optionsFontSize * 6.9f),mouseY[0])){
                                selection = 5;
                            }
                            if(between(optionsYOffset + (optionsFontSize * 7.1f), optionsYOffset + (optionsFontSize * 7.9f),mouseY[0])){
                                selection = 6;
                            }
                        }

                        if(menuSelection != selection){
                            menuSelection = selection;
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                        }
                    }
                    case OFFICE -> {
                        float xPos = (float) (((mouseX[0] / width) * 2) - 1);

                        float slowSpeed = 0.03f * deltaTime;
                        float fastSpeed = 0.08f * deltaTime;

                        boolean positive = xPos > 0;
                        if(between(0.25,0.75,Math.abs(xPos))){
                            if(positive) scroll-=slowSpeed; else scroll+=slowSpeed;
                        } else if(between(0.75,1,Math.abs(xPos))){
                            if(positive) scroll-=fastSpeed; else scroll+=fastSpeed;
                        }
                        if(scroll > 1) scroll = 1;
                        if(scroll < -1) scroll = -1;

                        if(between(0.25f,0.75f,mouseX[0] / width) && between(0.85f,100f,mouseY[0] / height) && !gameManager.isPowerout()){
                            if(hasExitedCameraButton) {
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(cameraStage == -1) for (int i = 0; i <= 10; i++) {
                                            cameraStage = i;
                                            try {
                                                Thread.sleep(16);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        cameraStage = 11;
                                        menu = Menu.CAMERAS;
                                    }
                                });
                                t.start();
                                cameraRandomness = random.nextInt(100);
                                sounds.get("cameraFlip.oga").play();
                                sounds.get("blip.ogg").stop();
                                sounds.get("cameras.oga").play();
                                sounds.get("cameras.oga").setRepeating(true);
                            }
                            hasExitedCameraButton = false;
                        } else {
                            hasExitedCameraButton = true;
                        }
                    }
                    case CAMERAS -> {
                        if(between(0.25f,0.75f,mouseX[0] / width) && between(0.85f,100f,mouseY[0] / height)){
                            if(hasExitedCameraButton) {
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(cameraStage == 11) for (int i = 10; i >= 0; i--) {
                                            cameraStage = i;
                                            try {
                                                Thread.sleep(16);
                                            } catch (InterruptedException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        cameraStage = -1;
                                    }
                                });
                                t.start();
                                menu = Menu.OFFICE;
                                sounds.get("blip.ogg").play();
                                sounds.get("cameraFlip.oga").stop();
                                sounds.get("cameras.oga").stop();
                            }
                            hasExitedCameraButton = false;
                        } else {
                            hasExitedCameraButton = true;
                        }
                    }
                }
            //}

            /**
             * Start rendering process
             */

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, width, height, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);

            if(menu.equals(Menu.CAMERAS) && gameManager.isPowerout()) menu = Menu.OFFICE;

            Office office = Office.getInstance();

            long timeUntilVentBlack = System.currentTimeMillis() - (gameManager.getNightStart() + gameManager.getNIGHT_LENGTH()) + (sounds.get("Ventablack.ogx").getLengthMS() * 1000);

            if(!gameManager.ventaNight && (Freddy.getInstance().getAI_LEVEL() + Bonnie.getInstance().getAI_LEVEL() + Chica.getInstance().getAI_LEVEL() + Foxy.getInstance().getAI_LEVEL()) / 4 >= 8) if(gameManager.isNightRunning() && timeUntilVentBlack >= 0)
            {
                sounds.get("Ventablack.ogx").setVolume(1f);
                sounds.get("Ventablack.ogx").play();
            }

            if (jumpscare == null) switch (menu) {
                case MAIN_MENU : {
                    if(noiseSwell) {
                        if(System.currentTimeMillis() - noiseSwellStart >= 5000){
                            noiseSwell = false;
                        } else {
                            float noiseTime = (float) ((System.currentTimeMillis() - noiseSwellStart) / 5000.0);
                            menuNoise = (float) (Math.sin(noiseTime * Math.PI)*0.2f + baseNoise);
                        }
                    }

                    switch (mainMenuFreddyTwitchStage) {
                        case 0: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.1.png"), true, true, menuNoise);
                            break;
                        }
                        case 1: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.2.png"), true, true, menuNoise);
                            break;
                        }
                        case 2: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.3.png"), true, true, menuNoise);
                            break;
                        }
                        case 3: {
                            renderer.renderTexture(-1, -1, 2, 2, textures.get("freddy.twitch.4.png"), true, true, menuNoise);
                            break;
                        }
                    }

                    float titleFontSize = 80 * (height / 1080.0f);
                    float optionsYOffset = 6 * titleFontSize;
                    float optionsFontSize = 60 * (height / 1080.0f);

                    textRenderer.renderTextOld("Five", 140 * (width / 1920f), 2 * titleFontSize,titleFontSize, Color.WHITE);
                    textRenderer.renderTextOld("Nights", 140 * (width / 1920f), 3 * titleFontSize,titleFontSize, Color.WHITE);
                    textRenderer.renderTextOld("at", 140 * (width / 1920f), 4 * titleFontSize,titleFontSize, Color.WHITE);
                    textRenderer.renderTextOld("Freddy's", 140 * (width / 1920f), 5 * titleFontSize,titleFontSize, Color.WHITE);

                    textRenderer.renderTextOld("New Game", 140 * (width / 1920f), optionsYOffset + (2 * optionsFontSize),optionsFontSize, Color.WHITE);
                    textRenderer.renderTextOld("Continue", 140 * (width / 1920f), optionsYOffset + (3 * optionsFontSize),optionsFontSize, Color.WHITE);
                    textRenderer.renderTextOld(" -> Night " + nightNumber, 350 * (width / 1920f), optionsYOffset + (2.75f * optionsFontSize),optionsFontSize/2f, Color.WHITE);
                    textRenderer.renderTextOld(night6Unlocked ?  "6th Night" : obfuscateText("6th Night"), 140 * (width / 1920f), optionsYOffset + (4 * optionsFontSize),optionsFontSize,  night6Unlocked ? Color.WHITE : Color.GRAY);
                    textRenderer.renderTextOld(customNightUnlocked ?  "Custom Night" : obfuscateText("Custom Night"), 140 * (width / 1920f), optionsYOffset + (5 * optionsFontSize),optionsFontSize,  customNightUnlocked ? Color.WHITE : Color.GRAY);
                    textRenderer.renderTextOld(ventaBlackNightUnlocked ?  "Venta Black Night" : obfuscateText("Venta Black Night"), 140 * (width / 1920f), optionsYOffset + (6 * optionsFontSize),optionsFontSize, ventaBlackNightUnlocked ? Color.WHITE : Color.GRAY);
                    textRenderer.renderTextOld("Settings", 140 * (width / 1920f), optionsYOffset + (7 * optionsFontSize),optionsFontSize, Color.WHITE);
                    textRenderer.renderTextOld(night6Unlocked ?  "Online" : obfuscateText("Online"), 140 * (width / 1920f), optionsYOffset + (8 * optionsFontSize),optionsFontSize, loggedIn ? (night6Unlocked ? Color.WHITE : Color.GRAY) : Color.GRAY);

                    switch (menuSelection) {
                        case 0 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (2 * optionsFontSize),optionsFontSize, Color.WHITE);
                        case 1 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (3 * optionsFontSize),optionsFontSize, Color.WHITE);
                        case 2 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (4 * optionsFontSize),optionsFontSize, Color.WHITE);
                        case 3 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (5 * optionsFontSize),optionsFontSize, Color.WHITE);
                        case 4 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (6 * optionsFontSize),optionsFontSize, Color.WHITE);
                        case 5 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (7 * optionsFontSize),optionsFontSize, Color.WHITE);
                        case 6 -> textRenderer.renderTextOld(">>", 70 * (width / 1920f), optionsYOffset + (8 * optionsFontSize),optionsFontSize, loggedIn ? Color.WHITE : Color.GRAY);
                    }

                    if(loggedIn) textRenderer.renderTextOld("Online - " + username + " (" + displayName + ")", 10 * (height / 1080f), (height-60 * (height / 1080f)),30 * (height / 1080f), Color.GREEN);
                    else textRenderer.renderTextOld("Offline", 10 * (height / 1080f), (height-60 * (height / 1080f)),30 * (height / 1080f), Color.RED);
                    textRenderer.renderTextOld(version, 10 * (height / 1080f), (height-20 * (height / 1080f)),30 * (height / 1080f), Color.WHITE);

                    renderer.renderTexture(.15f,.75f,.1f,.1f * ((float) width / height),textures.get(night6Unlocked ? "star.png" : "star.empty.png"),false,false,0);
                    renderer.renderTexture(.35f,.75f,.1f,.1f * ((float) width / height),textures.get(customNightUnlocked ? "star.png" : "star.empty.png"),false,false,0);
                    renderer.renderTexture(.55f,.75f,.1f,.1f * ((float) width / height),textures.get(ventaBlackNightUnlocked ? "star.png" : "star.empty.png"),false,false,0);
                    renderer.renderTexture(.75f,.7f,.15f,.15f * ((float) width / height),textures.get(ventaBlackNightCompleted ? "star.png" : "star.empty.png"),false,false,0);
                    break;
                }
                case PRE_GAME : {
                    long timeSinceStart = System.currentTimeMillis() - startTime;
                    if(timeSinceStart > 0 && timeSinceStart < 3000) renderer.renderTexture(-1,-1,2,2,textures.get("help_wanted.png"),true,false,0);
                    if(timeSinceStart > 3000 && timeSinceStart < 6000) {
                        stopAllSounds();
                        if(timeSinceStart < 3100)sounds.get("blip.ogg").play();
                        textRenderer.renderTextOld("12:00 AM",(width - textRenderer.textWidth("12:00 AM", 60)) / 2,(float) ((height-120) / 2), 60,Color.WHITE);
                        textRenderer.renderTextOld("Night " + nightConfiguration.getNightNumber(),(width - textRenderer.textWidth("Night " + nightConfiguration.getNightNumber(), 60)) / 2,(float) ((height-120) / 2) - 60, 60,Color.WHITE);
                    }
                    if(timeSinceStart > 6000) {
                        menu = Menu.OFFICE;
                        sounds.get("fan.oga").play();
                        sounds.get("fan.oga").setRepeating(true);
                        sounds.get("Ambiance1.ogg").play();
                        sounds.get("Ambiance1.ogg").setRepeating(true);
                        sounds.get("Ambience2.ogg").play();
                        sounds.get("Ambience2.ogg").setRepeating(true);

                        /*if(nightConfiguration.getNightNumber() != 7){
                            nightConfiguration.setEndlessNight(false);
                        }

                        if(nightNumber <= 5){
                            nightConfiguration = NightConfiguration.getNight(nightNumber);
                        }

                        if(nightNumber == 6){
                            nightConfiguration = NightConfiguration.getNight(6);
                        }

                        if(nightNumber == 8){
                            nightConfiguration = NightConfiguration.getNight(8);
                        }*/

                        System.out.println("starting " + nightConfiguration);

                        gameManager.startNight(nightConfiguration);
                        load();
                    }
                    break;
                }
                case OFFICE : {
                    if(gameManager.isBlackout()) break;

                    office = Office.getInstance();

                    String officeTexture = "office.png";

                    if(office.isLeftLight() && Math.random() < 0.95) {
                        if(office.getLeftDoorAnimatronic() == null){
                            officeTexture = "office.light.left.png";
                        } else {
                            officeTexture = "office.light.bonnie.png";
                        }
                    }
                    if(office.isRightLight() && Math.random() < 0.95) {
                        if(office.getRightDoorAnimatronic() == null){
                            officeTexture = "office.light.right.png";
                        } else {
                            officeTexture = "office.light.chica.png";
                        }
                    }

                    if(gameManager.isPowerout()){
                        officeTexture = "office.blackout.png";
                        if(sounds.get("powerout.ogg").isPlaying()){
                            if(Math.random() > 0.2) officeTexture = "office.blackout.freddy.png";
                        }
                    }

                    renderer.renderTexture(-1.25f + (scroll * 0.25f),-1,2.5f,2,textures.get(officeTexture),true,false,0);

                    if(office.getLeftDoorState() == DoorState.CLOSED) {
                        renderer.renderTexture(-1.15f + (scroll * 0.25f), -1, 0.36f, 2.01f, textures.get("left.door.closed.png"), true, false, 0);
                    } else if(office.getLeftDoorState() == DoorState.CLOSING || office.getLeftDoorState() == DoorState.OPENING) {
                        renderer.renderTexture(-1.15f + (scroll * 0.25f), -1, 0.36f, 2.01f, textures.get("left.door." + office.getLeftDoorAnimation() + ".png"), true, false, 0);
                    }

                    if(office.getRightDoorState() == DoorState.CLOSED) {
                        renderer.renderTexture(0.75f + (scroll * 0.25f), -1, 0.4f, 2.1f, textures.get("right.door.closed.png"), true, false, 0);
                    } else if(office.getRightDoorState() == DoorState.CLOSING || office.getRightDoorState() == DoorState.OPENING) {
                        renderer.renderTexture(0.75f + (scroll * 0.25f), -1, 0.4f, 2.1f, textures.get("right.door." + office.getRightDoorAnimation() + ".png"), true, false, 0);
                    }

                    renderer.renderTexture(-0.5f,-.9f,1,.15f,textures.get("camera.button.png"),true,false,0);
                    float w0 = .17f;
                    float w = w0*1.25f;
                    float h = w0*textures.get("fan.1.png").getAspectRatio();
                    h = w0*3.2f;
                    if(!gameManager.isPowerout()) switch ((int) ((float)fan_stage/10.0f)) {
                        case 0: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.1.png"),false,false,0);
                            break;
                        }
                        case 1: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.2.png"),false,false,0);
                            break;
                        }
                        case 2: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.3.png"),false,false,0);
                        }
                    }
                    String textureLeft;
                    String textureRight;

                    if(office.isLeftDoor()){
                        if(office.isLeftLight()){
                            textureLeft = "buttons.left.door.light.png";
                        } else {
                            textureLeft = "buttons.left.door.png";
                        }
                    } else {
                        if(office.isLeftLight()){
                            textureLeft = "buttons.left.light.png";
                        } else {
                            textureLeft = "buttons.left.png";
                        }
                    }

                    if(office.isRightDoor()){
                        if(office.isRightLight()){
                            textureRight = "buttons.right.door.light.png";
                        } else {
                            textureRight = "buttons.right.door.png";
                        }
                    } else {
                        if(office.isRightLight()){
                            textureRight = "buttons.right.light.png";
                        } else {
                            textureRight = "buttons.right.png";
                        }
                    }

                    renderer.renderTexture((scroll * 0.25f)+1.1f,-0.385f,w*0.6f,h,textures.get(textureRight),true,false,0);
                    renderer.renderTexture((scroll * 0.25f)-1.25f,-0.385f,w*0.6f,h,textures.get(textureLeft),true,false,0);

                    if(between(0,10,cameraStage)) renderer.renderTexture(-1,-1,2f,2,textures.get("camera.flip." + cameraStage + ".png"),true,false,0);

                    String nightText = "Night " + gameManager.nightNumber;
                    String currentTime;
                    float fontSize = 60 * (height/1080.0f);
                    float rightOffset = 20 * (height/1080.0f);
                    if(exactNightTime) {
                        currentTime = formatTime((long) ((System.currentTimeMillis() - gameManager.getNightStart()) * (360000f / gameManager.getNIGHT_LENGTH())));
                    } else {
                        currentTime = gameManager.getHour() == 0 ? "12 AM" : gameManager.getHour() + " AM";
                    }

                    if(gameManager.isEndless){
                        currentTime = formatTime(System.currentTimeMillis() - gameManager.getNightStart());
                    }

                    textRenderer.renderTextOld(currentTime,width - textRenderer.textWidth(currentTime,fontSize) - rightOffset,fontSize + rightOffset,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(nightText,width - textRenderer.textWidth(nightText,fontSize) - rightOffset,fontSize * 2 + rightOffset,fontSize,Color.WHITE);

                    textRenderer.renderTextOld(String.format("Power %.1f%%",gameManager.getPower()) ,10,height-25,40 * (height/1080.0f),Color.WHITE);
                    if(!gameManager.isPowerout()) renderer.renderTexture(-.99f,-0.8f,0.3f,0.1f,textures.get("power." + gameManager.getDevices() + ".png"),true,false,0);

                    if(nightConfiguration.getChallenge() != null && !loggedIn) {
                        textRenderer.renderText("Warning! You have been disconnected from the Statistic Server during the game!",0,0,fontSize,Color.RED);
                    }

                    break;
                }
                case CAMERAS : {
                    float cameraScroll = (float) (Math.sin(glfwGetTime() / 5));

                    String cameraImage = ((int) glfwGetTime()) % 2 == 0 ? "cam.blank.png" : "cam.selected.png";

                    if(System.currentTimeMillis() <= cameraBlackout) {
                        renderer.renderTexture(-1,-1,2,2,textures.get("white.png"),true,true,.3f,new Color(0,0,0,255));
                        System.out.println(System.currentTimeMillis() + " / " + cameraBlackout);
                    } else {
                        if(!gameManager.getCamera().equals(Camera6.getInstance())) renderer.renderTexture(-1.25f + (cameraScroll * 0.25f),-1,2.5f,2,textures.get(gameManager.getCamera().getImage(cameraRandomness,cameraRandomness2)),true,true,glitchStrength > 1 ? 1 : glitchStrength);
                        else {
                            renderer.renderTexture(-1f, -1f, 2f, 2f, textures.get(gameManager.getCamera().getImage(cameraRandomness, cameraRandomness2)), true, true, 1f);
                            renderer.renderTexture(-.5f, -.25f, 1f, .5f, textures.get(gameManager.getCamera().getImage(cameraRandomness, cameraRandomness2)), true, false, glitchStrength > 1 ? 1 : glitchStrength);
                        }
                        if(System.currentTimeMillis() - cameraBlackout < 1000){
                            float a = (System.currentTimeMillis() - cameraBlackout) / 1000f;
                            int alpha = (int) (255 - (a * 255));
                            if(between(0,255,alpha)) renderer.renderTexture(-1,-1,2,2,textures.get("white.png"),true,true,.3f,new Color(0,0,0,alpha));
                        }
                    }

                    renderer.renderTexture(0.5f,-1f,0.5f,0.9f,textures.get("layout.png"),true,false,0);
                    renderer.renderTexture(-0.5f,-.9f,1,.15f,textures.get("camera.button.png"),true,false,0);

                    float x = .625f;
                    float y = -.225f;

                    if(gameManager.getCamera().equals(Camera1A.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.1a.png"),true,false,0);

                    x = .6f;
                    y = -.35f;

                    if(gameManager.getCamera().equals(Camera1B.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.1b.png"),true,false,0);

                    x = .47f;
                    y = -.42f;

                    if(gameManager.getCamera().equals(Camera5.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.5.png"),true,false,0);

                    x = .57f;
                    y = -.525f;

                    if(gameManager.getCamera().equals(Camera1C.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.1c.png"),true,false,0);

                    x = .52f;
                    y = -.75f;

                    if(gameManager.getCamera().equals(Camera3.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.3.png"),true,false,0);

                    x = .625f;
                    y = -.82f;

                    if(gameManager.getCamera().equals(Camera2A.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.2a.png"),true,false,0);

                    y = -.9f;

                    if(gameManager.getCamera().equals(Camera2B.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.2b.png"),true,false,0);

                    x = .775f;
                    y = -.82f;

                    if(gameManager.getCamera().equals(Camera4A.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.4a.png"),true,false,0);

                    y = -.9f;

                    if(gameManager.getCamera().equals(Camera4B.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.4b.png"),true,false,0);

                    x = .9f;
                    y = -.725f;

                    if(gameManager.getCamera().equals(Camera6.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.6.png"),true,false,0);

                    y = -.42f;

                    if(gameManager.getCamera().equals(Camera7.getInstance())) renderer.renderTexture(x,y,0.075f,0.075f,textures.get(cameraImage),true,false,0);
                    else renderer.renderTexture(x,y,0.075f,0.075f,textures.get("cam.blank.png"),true,false,0);
                    renderer.renderTexture(x + 0.0125f,y + 0.0125f,0.05f,0.05f,textures.get("cam.7.png"),true,false,0);

                    String nightText = "Night " + gameManager.nightNumber;
                    String currentTime;
                    float fontSize = 60 * (height/1080.0f);
                    float rightOffset = 20 * (height/1080.0f);
                    if(exactNightTime) {
                        currentTime = formatTime((long) ((System.currentTimeMillis() - gameManager.getNightStart()) * (360000f / gameManager.getNIGHT_LENGTH())));
                    } else {
                        currentTime = gameManager.getHour() == 0 ? "12 AM" : gameManager.getHour() + " AM";
                    }

                    if(gameManager.isEndless){
                        currentTime = formatTime(System.currentTimeMillis() - gameManager.getNightStart());
                    }

                    textRenderer.renderTextOld(currentTime,width - textRenderer.textWidth(currentTime,fontSize) - rightOffset,fontSize + rightOffset,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(nightText,width - textRenderer.textWidth(nightText,fontSize) - rightOffset,fontSize * 2 + rightOffset,fontSize,Color.WHITE);

                    textRenderer.renderTextOld(String.format("Power %.1f%%",gameManager.getPower()) ,10,height-25,40 * (height/1080.0f),Color.WHITE);
                    if(!gameManager.isPowerout()) renderer.renderTexture(-.99f,-0.8f,0.3f,0.1f,textures.get("power." + gameManager.getDevices() + ".png"),true,false,0);
                    break;
                }
                case NIGHT_END : {
                    textRenderer.renderTextOld("6 AM" ,((width - textRenderer.textWidth("6 AM", 80 * (height / 1080f))) / 2), (float) ((height) / 2.0),80 * (height / 1080f),Color.WHITE);
                    textRenderer.renderTextOld("Night " + gameManager.nightNumber ,(width - textRenderer.textWidth("Night " + gameManager.nightNumber, 50 * (height / 1080f))) / 2, (float) ((height-60) / 2.0) - (80 * (height / 1080f)),50 * (height / 1080f),Color.WHITE);
                    if(nightNumber > 5) nightNumber = 5;
                    break;
                }
                case NIGHT_END_DEATH : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("loose.png"),true,false,0);
                    if(nightConfiguration.isEndlessNight()){
                        String text = "You survived for " + formatTimeNormal(nightEndTime - gameManager.getNightStart());
                        float w = textRenderer.textWidth(text,60 * (height / 1080f));
                        w = (width - w) / 2;

                        textRenderer.renderText(text,w,(height-60 * (height / 1080f)) / 2,60 * (height / 1080f),Color.WHITE);

                        text = "Power left: " + String.format("%.2f",gameManager.getPower());

                        w = textRenderer.textWidth(text,60 * (height / 1080f));
                        w = (width - w) / 2;

                        textRenderer.renderText(text,w,(height-120 * (height / 1080f)) / 2,60 * (height / 1080f),Color.WHITE);
                    }
                    if(nightNumber > 5) nightNumber = 5;
                    break;
                }
                case NOISE : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("loose.png"),true,true,1);
                    break;
                }
                case ENDING_WEEK_END : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("ending.week.end.png"),true,false,0);
                    break;
                }
                case ENDING_WEEK_OVERTIME : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("ending.week.overtime.png"),true,false,0);
                    break;
                }
                case ENDING_FIRED : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("ending.fired.png"),true,false,0);
                    break;
                }
                case ENDING_VENTA : {
                    float noiseLevel = 1 - ((System.currentTimeMillis() - gameManager.getNightStart() + gameManager.getNIGHT_LENGTH() + 10000) / 10500f);
                    noiseLevel = Math.max(0.1f,Math.min(1,noiseLevel));
                    renderer.renderTexture(-1,-1,2,2,textures.get("golden.freddy.png"),true,true,noiseLevel);
                    break;
                }
                case CUSTOM_NIGHT : {
                    float aspectRatio = (float) width /height;
                    float fontSize = 0.046f*width;
                    float x = (float) (.3 - (textRenderer.textWidth(">", fontSize) / width));
                    float x0 = textRenderer.textWidth("0", fontSize) / width;

                    //TODO: custom night rendering

                    textRenderer.renderTextOld("Customize Night",(width-textRenderer.textWidth("Customize Night",fontSize * 1.3f)) / 2.1f,((-0.85f + 1) / 2) * width,fontSize * 1.3f,Color.WHITE);

                    renderer.renderTexture(-0.9f,0,0.3f,0.3f * aspectRatio,textures.get("freddy.png"),false,false,0);
                    renderer.renderTexture(-0.4f,0,0.3f,0.3f * aspectRatio,textures.get("bonnie.png"),false,false,0);
                    renderer.renderTexture(0.1f,0,0.3f,0.3f * aspectRatio,textures.get("chica.png"),false,false,0);
                    renderer.renderTexture(0.6f,0,0.3f,0.3f * aspectRatio,textures.get("foxy.png"),false,false,0);

                    textRenderer.renderTextOld("Freddy", ((-0.9f+1)/2f) * width, ((-0.55f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    textRenderer.renderTextOld("Bonnie", ((-0.4f+1)/2f) * width, ((-0.55f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    textRenderer.renderTextOld("Chica", ((.125f+1)/2f) * width, ((-0.55f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    textRenderer.renderTextOld("Foxy", ((.65f+1)/2f) * width, ((-0.55f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);

                    textRenderer.renderTextOld("A.I. Level",((-.9f+1)/2f) * width, ((.2f+1)/2f) * height, fontSize,Color.WHITE);
                    textRenderer.renderTextOld("A.I. Level",((-.4f+1)/2f) * width, ((.2f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("A.I. Level",((.1f+1)/2f) * width, ((.2f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("A.I. Level",((.6f+1)/2f) * width, ((.2f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld("Movement",((-.9f+1)/2f) * width, ((.55f+1)/2f) * height, fontSize,Color.WHITE);
                    textRenderer.renderTextOld("Movement",((-.4f+1)/2f) * width, ((.55f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("Movement",((.1f+1)/2f) * width, ((.55f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("Movement",((.6f+1)/2f) * width, ((.55f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld("<",((-.9f+1)/2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("<",((-.4f+1)/2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("<",((.1f+1)/2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("<",((.6f+1)/2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld(">", (((-.9f + x) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(">", (((-.4f + x) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(">", (((.1f + x) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(">", (((.6f + x) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld("<",((-.9f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("<",((-.4f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("<",((.1f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld("<",((.6f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld(">", (((-.9f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(">", (((-.4f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(">", (((.1f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(">", (((.6f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld(String.format("%02d", nightConfiguration.getFreddyAI()), (((-.9f + x - (6 * x0)) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(String.format("%02d", nightConfiguration.getBonnieAI()), (((-.4f + x - (6 * x0)) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(String.format("%02d", nightConfiguration.getChicaAI()), (((.1f + x - (6 * x0)) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(String.format("%02d", nightConfiguration.getFoxyAI()), (((.6f + x - (6 * x0)) + 1) / 2f) * width, ((.35f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld(String.format("%04d", nightConfiguration.getFreddyMovementSpeed()), (((-.9f + x - (11.5f * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(String.format("%04d", nightConfiguration.getBonnieMovementSpeed()), (((-.4f + x - (11.5f * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(String.format("%04d", nightConfiguration.getChicaMovementSpeed()), (((.1f + x - (11.5f * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    textRenderer.renderTextOld(String.format("%04d", nightConfiguration.getFoxyMovementSpeed()), (((.6f + x - (11.5f * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);

                    Texture white = textures.get("white.png");
                    float mod = height / 1080f;
                    float checkBoxX = ((-.1f+1)/2f) * width - 1.5f *fontSize;
                    float checkBoxY = ((.96f+1)/2f) * height - fontSize;
                    float selectedSize = 40;
                    fontSize = 60 * mod;

                    renderer.renderTextureCoordinatesBounds(checkBoxX,checkBoxY,checkBoxX + fontSize,checkBoxY + fontSize,white,true,false,0);
                    renderer.renderTextureCoordinatesBounds(checkBoxX + (mod * 5),checkBoxY + (mod * 5),checkBoxX + fontSize - (mod * 5),checkBoxY + fontSize - (mod * 5),white,true,false,0, Color.BLACK);
                    if(nightConfiguration.isEndlessNight()) renderer.renderTextureCoordinatesBounds(checkBoxX + ((fontSize - (mod * selectedSize)) / 2),checkBoxY + ((fontSize - (mod * selectedSize)) / 2),checkBoxX + ((fontSize + (mod * selectedSize)) / 2),checkBoxY + ((fontSize + (mod * selectedSize)) / 2),white,true,false,0, Color.WHITE);

                    textRenderer.renderTextOld("Endless Mode", ((-.1f+1)/2f) * width, ((.925f+1)/2f) * height,fontSize,Color.WHITE);

                    textRenderer.renderTextOld("Start", ((.7f+1)/2f) * width, ((.95f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    textRenderer.renderTextOld("Back", ((-.95f+1)/2f) * width, ((.95f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    break;
                }
                case SETTINGS: {
                    float mod = height / 1080f;
                    float fontSize = 60 * mod;
                    float volumeOffset = 2 * fontSize + textRenderer.textWidth("Volume (000.0%):", fontSize);
                    float volumeSliderWidth = (width - fontSize) - volumeOffset;

                    Color volumeSliderColor = new Color(Math.min(soundManager.getVolume()*2,1),Math.min(((1-soundManager.getVolume())) * 2,1),0);
                    Texture white = textures.get("white.png");

                    textRenderer.renderText("Settings",(width-textRenderer.textWidth("Settings",80 * mod)) / 2, 0,80 * mod,Color.WHITE);

                    textRenderer.renderText("Volume (" + String.format("%.1f",soundManager.getVolume() * 100f) + "%):", 60 * mod, fontSize * 3,fontSize,Color.WHITE);
                    renderer.renderTextureCoordinatesBounds(volumeOffset,(fontSize * 3) + ((fontSize - (mod * 6)) / 2),width - fontSize,(fontSize * 3) + ((fontSize + (mod * 6)) / 2),white,true,false,0);
                    renderer.renderTextureCoordinates(volumeOffset,fontSize * 3,mod * 10,fontSize,white,true,false,0);
                    renderer.renderTextureCoordinates(width - fontSize,fontSize * 3,mod * 10,fontSize,white,true,false,0);
                    renderer.renderTextureCoordinates(volumeOffset + (volumeSliderWidth * soundManager.getVolume()),fontSize * 3,mod * 10,fontSize,white,true,false,0,volumeSliderColor);

                    float checkBoxX = fontSize;
                    float checkBoxY = fontSize * 5;
                    float selectedSize = 40;
                    renderer.renderTextureCoordinatesBounds(checkBoxX,checkBoxY,checkBoxX + fontSize,checkBoxY + fontSize,white,true,false,0);
                    renderer.renderTextureCoordinatesBounds(checkBoxX + (mod * 5),checkBoxY + (mod * 5),checkBoxX + fontSize - (mod * 5),checkBoxY + fontSize - (mod * 5),white,true,false,0, Color.BLACK);
                    if(vsync) renderer.renderTextureCoordinatesBounds(checkBoxX + ((fontSize - (mod * selectedSize)) / 2),checkBoxY + ((fontSize - (mod * selectedSize)) / 2),checkBoxX + ((fontSize + (mod * selectedSize)) / 2),checkBoxY + ((fontSize + (mod * selectedSize)) / 2),white,true,false,0, Color.WHITE);
                    textRenderer.renderText("VSync",checkBoxX * 2.5f, checkBoxY,fontSize,Color.WHITE);

                    checkBoxY = fontSize * 7;
                    selectedSize = 40;
                    renderer.renderTextureCoordinatesBounds(checkBoxX,checkBoxY,checkBoxX + fontSize,checkBoxY + fontSize,white,true,false,0);
                    renderer.renderTextureCoordinatesBounds(checkBoxX + (mod * 5),checkBoxY + (mod * 5),checkBoxX + fontSize - (mod * 5),checkBoxY + fontSize - (mod * 5),white,true,false,0, Color.BLACK);
                    if(exactNightTime) renderer.renderTextureCoordinatesBounds(checkBoxX + ((fontSize - (mod * selectedSize)) / 2),checkBoxY + ((fontSize - (mod * selectedSize)) / 2),checkBoxX + ((fontSize + (mod * selectedSize)) / 2),checkBoxY + ((fontSize + (mod * selectedSize)) / 2),white,true,false,0, Color.WHITE);
                    textRenderer.renderText("Exact Nighttime  (Display Nighttime as 12:04AM instead of 12AM)",checkBoxX * 2.5f, checkBoxY,fontSize,Color.WHITE);

                    textRenderer.renderText("Back", fontSize, (height - ((60 * 2) * mod)),fontSize,Color.WHITE);
                    break;
                }
                case ONLINE : {
                    float mod = height / 1080f;
                    float modX = width / 1920f;
                    float fontSize = 40 * mod;

                    textRenderer.renderText("Online",(width-textRenderer.textWidth("Settings",80 * mod)) / 2, 0,80 * mod,Color.WHITE);

                    textRenderer.renderText("Multiplayer:",100 * mod, fontSize * 2,fontSize,Color.WHITE);
                    textRenderer.renderText("Create 1v1",100 * mod, fontSize * 4,fontSize,Color.WHITE);
                    textRenderer.renderText("Join 1v1",100 * mod, fontSize * 5,fontSize,Color.WHITE);
                    textRenderer.renderText("Challenges:",100 * mod, fontSize * 7,fontSize,Color.WHITE);
                    textRenderer.renderText("Challenge: Night 6 run",100 * mod, fontSize * 9,fontSize,Color.WHITE);
                    textRenderer.renderText("Challenge: Night 7, 4/20 run",100 * mod, fontSize * 10,fontSize,Color.WHITE);
                    textRenderer.renderText("Challenge: Ventablack run",100 * mod, fontSize * 11,fontSize,Color.WHITE);
                    textRenderer.renderText("Challenge: Night 7, 4/20 endless run",100 * mod, fontSize * 13,fontSize,Color.WHITE);
                    textRenderer.renderText("Challenge: Ventablack endless run",100 * mod, fontSize * 14,fontSize,Color.WHITE);

                    int index = (int) (mouseY[0] / fontSize);

                    if(between(0,800 * modX,mouseX[0])){
                        switch (index) {
                            case 4 -> textRenderer.renderText(">>",50 * mod, fontSize * 4,fontSize,Color.WHITE);
                            case 5 -> textRenderer.renderText(">>",50 * mod, fontSize * 5,fontSize,Color.WHITE);
                            case 9 -> textRenderer.renderText(">>",50 * mod, fontSize * 9,fontSize,Color.WHITE);
                            case 10 -> textRenderer.renderText(">>",50 * mod, fontSize * 10,fontSize,Color.WHITE);
                            case 11 -> textRenderer.renderText(">>",50 * mod, fontSize * 11,fontSize,Color.WHITE);
                            case 13 -> textRenderer.renderText(">>",50 * mod, fontSize * 13,fontSize,Color.WHITE);
                            case 14 -> textRenderer.renderText(">>",50 * mod, fontSize * 14,fontSize,Color.WHITE);
                        }
                    }

                    textRenderer.renderText("Back", 0.5f * (60 * mod), (height - (1.5f * (60 * mod))),60 * mod,Color.WHITE);
                    break;
                }
            }

            if(jumpscare != null) {
                if(jumpscare.equals("foxy.run.")){
                    if(gameManager.getCamera().equals(Camera2A.getInstance())){
                        renderer.renderTexture(-1.25f + (scroll * 0.25f),-1,2.5f,2,textures.get(jumpscare + jumpscareFrame + ".png"),true,false,0);
                    }
                } else renderer.renderTexture(-1.25f + (scroll * 0.25f),-1,2.5f,2,textures.get(jumpscare + jumpscareFrame + ".png"),true,false,0);
            }

            if(showDebug){
                float fontSize = (30 * (height/1080.0f));

                textRenderer.renderTextOld("FPS: " + fps, 10, fontSize,fontSize, Color.WHITE);
                textRenderer.renderTextOld("Render Size: " + width + " / " + height, 10, fontSize * 2,fontSize, Color.WHITE);
                textRenderer.renderTextOld("Last Frame Time: " + String.format("%.2f ms",lastFrameTime*1000), 10, fontSize * 3,fontSize, Color.WHITE);
                textRenderer.renderTextOld("Time: " + String.format("%.4fs",glfwGetTime()), 10, fontSize * 4,fontSize, Color.WHITE);
                textRenderer.renderTextOld("Components Drawn: " + componentsDrawn, 10, fontSize * 5,fontSize, Color.WHITE);
                textRenderer.renderTextOld("Delta Time: " + String.format("%.2fs",deltaTime), 10, fontSize * 6,fontSize, Color.WHITE);
                textRenderer.renderTextOld("Scroll: " + String.format("%.2f",scroll), 10, fontSize * 7,fontSize, Color.WHITE);
                int i = 1;

                for(Sound s : sounds.values()){
                    if(s.isPlaying()) textRenderer.renderTextOld("Sound '" + s.getFilepath() + "' " + s.getCurrentTime() + " / " + s.getTotalLength(), 10, fontSize * (7 + i),fontSize, Color.WHITE);
                    if(s.isPlaying()) i++;
                }

                textRenderer.renderTextOld(String.format("%s %s     %02d","Bonnie", Bonnie.getInstance().getCurrentCamera().getCameraName(), Bonnie.getInstance().getAI_LEVEL()), 400 * (width/1920.0f), fontSize,fontSize, Color.RED);
                textRenderer.renderTextOld(String.format("%s %s     %02d","Chica  ", Chica.getInstance().getCurrentCamera().getCameraName(), Chica.getInstance().getAI_LEVEL()), 400 * (width/1920.0f), fontSize * 2,fontSize, Color.RED);
                textRenderer.renderTextOld(String.format("%s %s     %02d    %.2fs","Freddy", Freddy.getInstance().getCurrentCamera().getCameraName(), Freddy.getInstance().getAI_LEVEL(), Freddy.getInstance().stillStalledFor() / 1000f), 400 * (width/1920.0f), fontSize * 3,fontSize, Color.RED);
                textRenderer.renderTextOld(String.format("%s               %02d     %02d    %.2fs","Foxy  ", Foxy.getInstance().getStage(), Foxy.getInstance().getAI_LEVEL(), Foxy.getInstance().stillStalledFor() / 1000f), 400 * (width/1920.0f), fontSize * 4,fontSize, Color.RED);

            }

            for (Notification n : notifications) {
                if(n.isRunning()) {
                    n.render();
                }
            }

            deltaTime = (float) (lastFrameTime / (1.0/60.0));

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            /**
             * Performance monitoring updating
             */

            frames++;
            if(System.currentTimeMillis() - lastUpdate >= 1000){
                fps = frames;
                frames = 0;
                lastUpdate = System.currentTimeMillis();
            }
            lastFrameTime = glfwGetTime() - start;
        }

        glfwDestroyWindow(window);
        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        boolean enableLogging = true;

        if(args.length == 2){
            enableLogging = Boolean.valueOf(args[1]);
        }

        File f = new File("crash-report.txt");
        PrintStream debugStream = new PrintStream(new FileOutputStream(f));

        if(enableLogging){
            System.setOut(debugStream);
            System.setErr(debugStream);
        }

        try {
            new FNAFMain().run(args);
        } catch (Exception e) {
            System.out.println("\n\n\n\n");
            System.out.println("--- Crash ---");
            System.out.println(System.currentTimeMillis());
            System.out.println("\n\n\n\n");

            debugStream.println(e.toString());

            StackTraceElement[] trace = e.getStackTrace();
            for(StackTraceElement traceElement : trace){
                debugStream.println(traceElement.toString());
            }

            debugStream.flush();
            debugStream.close();

            JOptionPane.showMessageDialog(null,"FNaF seems to have crashed. Further crash information is in the crash-report.txt",e.toString(),JOptionPane.ERROR_MESSAGE);
        }
    }

    public void triggerJumpScare(final String jumpscare, int frames, boolean endGame){
        this.nightEndTime = System.currentTimeMillis();
        this.jumpscare = jumpscare;
        FNAFMain m = this;
        if(endGame) sounds.get("scream.ogg").play();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i <= frames; i++) {
                    if(jumpscare.contains("foxy") || jumpscare.contains("bonnie")) scroll = 1;
                    else scroll = 0;
                    jumpscareFrame = i;

                    try {
                        Thread.sleep(32);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(endGame) {
                    stopAllSounds();
                    m.jumpscare = null;
                    menu = Menu.NOISE;
                    sounds.get("humm.oga").play();
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    sounds.get("humm.oga").stop();
                    menu = Menu.NIGHT_END_DEATH;
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    menu = Menu.MAIN_MENU;
                    sounds.get("Static2.ogg").play();
                    sounds.get("Mainmenu1.ogg").play();
                    sounds.get("Mainmenu1.ogg").setRepeating(true);
                }
                m.jumpscare = null;
            }
        });
        t.start();
    }

    private void updateProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        aspectRatio = (float) width / height;
        GL11.glOrtho(-aspectRatio, aspectRatio, -1f, 1.0f, -1f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public static boolean between(double val1, double val2, double toCheck){
        return Math.min(val1,val2) <= toCheck && Math.max(val1,val2) >= toCheck;
    }

    public static void stopAllSounds(){
        for(Sound s : sounds.values()) s.stop();
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        if (fullscreen) {
            GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0, Math.min(vidMode.width(),1920), Math.min(vidMode.height(),1080), vidMode.refreshRate());
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW_CURSOR_CAPTURED);
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, 50, 50, 1280, 720, 0);
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    public void setWindowIcon(long window) {
        ByteBuffer icon16;
        ByteBuffer icon32;
        try {
            icon16 = IOUtil.ioResourceToByteBuffer("textures/icon16.png", 2048);
            icon32 = IOUtil.ioResourceToByteBuffer("textures/icon32.png", 4096);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        IntBuffer w = memAllocInt(1);
        IntBuffer h = memAllocInt(1);
        IntBuffer comp = memAllocInt(1);

        try (GLFWImage.Buffer icons = GLFWImage.malloc(2)) {
            stbi_set_flip_vertically_on_load(false);
            ByteBuffer pixels16 = stbi_load_from_memory(icon16, w, h, comp, 4);
            icons
                    .position(0)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels16);

            stbi_set_flip_vertically_on_load(false);
            ByteBuffer pixels32 = stbi_load_from_memory(icon32, w, h, comp, 4);
            icons
                    .position(1)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels32);

            pixels32.flip();
            pixels16.flip();

            icons.position(0);
            glfwSetWindowIcon(window, icons);

            stbi_image_free(pixels32);
            stbi_image_free(pixels16);
        }
    }

    public void load(){
        soundManager = SoundManager.getInstance();
        try {
            File data = new File("data.json");
            if(!data.exists()) {
                data.createNewFile();
                JSONObject saveData = new JSONObject();
                saveData.put("night",1);
                saveData.put("volume",.5f);
                saveData.put("freddy-ai",nightConfiguration.getFreddyAI());
                saveData.put("bonnie-ai",nightConfiguration.getBonnieAI());
                saveData.put("chica-ai",nightConfiguration.getChicaAI());
                saveData.put("foxy-ai",nightConfiguration.getFoxyAI());
                saveData.put("freddy-speed",nightConfiguration.getFreddyMovementSpeed());
                saveData.put("bonnie-speed",nightConfiguration.getBonnieMovementSpeed());
                saveData.put("chica-speed",nightConfiguration.getChicaMovementSpeed());
                saveData.put("foxy-speed",nightConfiguration.getFreddyMovementSpeed());
                saveData.put("endless-mode",nightConfiguration.isEndlessNight());
                saveData.put("vsync",vsync);
                saveData.put("exact-night-time",exactNightTime);

                JSONObject unlocks = new JSONObject();
                unlocks.put("night-6",false);
                unlocks.put("custom-night",false);
                unlocks.put("venta-black-night",false);
                unlocks.put("venta-black-night-completed",false);
                saveData.put("unlocks",unlocks);
                Util.writeStringToFile(saveData.toString(),data);
            }
            JSONObject object = new JSONObject(Util.readFile(data));
            JSONObject unlocks = object.getJSONObject("unlocks");
            soundManager.setVolume(object.getFloat("volume"));
            this.customNightUnlocked = unlocks.getBoolean("custom-night");
            this.night6Unlocked = unlocks.getBoolean("night-6");
            this.ventaBlackNightUnlocked = unlocks.getBoolean("venta-black-night");
            this.ventaBlackNightCompleted = unlocks.getBoolean("venta-black-night-completed");
            this.nightNumber = object.getInt("night");
            this.vsync = object.getBoolean("vsync");
            this.exactNightTime = object.getBoolean("exact-night-time");

            nightConfiguration.setFreddyAI(object.getInt("freddy-ai"));
            nightConfiguration.setBonnieAI(object.getInt("bonnie-ai"));
            nightConfiguration.setChicaAI(object.getInt("chica-ai"));
            nightConfiguration.setFoxyAI(object.getInt("foxy-ai"));
            nightConfiguration.setFreddyMovementSpeed(object.getInt("freddy-speed"));
            nightConfiguration.setBonnieMovementSpeed(object.getInt("bonnie-speed"));
            nightConfiguration.setChicaMovementSpeed(object.getInt("chica-speed"));
            nightConfiguration.setFoxyMovementSpeed(object.getInt("foxy-speed"));
            nightConfiguration.setEndlessNight(object.getBoolean("endless-mode"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(){
        try {
            File data = new File("data.json");
            if(!data.exists()) {
                data.createNewFile();
                JSONObject saveData = new JSONObject();
                saveData.put("night",1);
                saveData.put("volume",.5f);
                saveData.put("freddy-ai",nightConfiguration.getFreddyAI());
                saveData.put("bonnie-ai",nightConfiguration.getBonnieAI());
                saveData.put("chica-ai",nightConfiguration.getChicaAI());
                saveData.put("foxy-ai",nightConfiguration.getFoxyAI());
                saveData.put("freddy-speed",nightConfiguration.getFreddyMovementSpeed());
                saveData.put("bonnie-speed",nightConfiguration.getBonnieMovementSpeed());
                saveData.put("chica-speed",nightConfiguration.getChicaMovementSpeed());
                saveData.put("foxy-speed",nightConfiguration.getFreddyMovementSpeed());
                saveData.put("endless-mode",nightConfiguration.isEndlessNight());
                saveData.put("vsync",vsync);
                saveData.put("exact-night-time",exactNightTime);

                JSONObject unlocks = new JSONObject();
                unlocks.put("night-6",false);
                unlocks.put("custom-night",false);
                unlocks.put("venta-black-night",false);
                unlocks.put("venta-black-night-completed",false);
                saveData.put("unlocks",unlocks);
                Util.writeStringToFile(saveData.toString(),data);
            } else {
                JSONObject saveData = new JSONObject();
                saveData.put("night",this.nightNumber);
                saveData.put("volume",soundManager.getVolume());
                saveData.put("freddy-ai",nightConfiguration.getFreddyAI());
                saveData.put("bonnie-ai",nightConfiguration.getBonnieAI());
                saveData.put("chica-ai",nightConfiguration.getChicaAI());
                saveData.put("foxy-ai",nightConfiguration.getFoxyAI());
                saveData.put("freddy-speed",nightConfiguration.getFreddyMovementSpeed());
                saveData.put("bonnie-speed",nightConfiguration.getBonnieMovementSpeed());
                saveData.put("chica-speed",nightConfiguration.getChicaMovementSpeed());
                saveData.put("foxy-speed",nightConfiguration.getFreddyMovementSpeed());
                saveData.put("endless-mode",nightConfiguration.isEndlessNight());
                saveData.put("vsync",vsync);
                saveData.put("exact-night-time",exactNightTime);

                JSONObject unlocks = new JSONObject();
                unlocks.put("night-6",this.night6Unlocked);
                unlocks.put("custom-night",this.customNightUnlocked);
                unlocks.put("venta-black-night",this.ventaBlackNightUnlocked);
                unlocks.put("venta-black-night-completed",this.ventaBlackNightCompleted);
                saveData.put("unlocks",unlocks);
                Util.writeStringToFile(saveData.toString(),data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String obfuscateText(String text){
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012346789";
        StringBuilder returnVal = new StringBuilder();
        for(char c : text.toCharArray()){
            c = chars.toCharArray()[random.nextInt(chars.length())];
            returnVal.append(c);
        }
        return returnVal.toString();
    }

    public String formatTime(long millis) {
        if(gameManager.isEndless) return formatTimeNormal(millis);
        long hours =  TimeUnit.MILLISECONDS.toMinutes(millis) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        long minutes = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        if(hours == 0) hours = 12;
        return String.format("%02d:%02d AM", hours, minutes);
    }
    public String formatTimeNormal(long millis) {
        long hours = millis / (1000 * 60 * 60);
        long minutes = (millis / (1000 * 60)) % 60;
        long seconds = (millis / 1000) % 60;
        long remainingMillis = millis % 1000;

        return String.format("%02d:%02d:%02d.%d", hours, minutes, seconds, (int) (remainingMillis/100));
    }
}
