package me.redstoner2019.fnaf;

import me.redstoner2019.Main;
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
import me.redstoner2019.graphics.Renderer;
import me.redstoner2019.graphics.general.IOUtil;
import me.redstoner2019.graphics.general.Texture;
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
    private HashMap<String,Texture> textures = new HashMap<>();
    public static HashMap<String, Sound> sounds = new HashMap<>();
    private Renderer renderer;
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

    public void run(int night) throws IOException {
        nightNumber = night;
        init();
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

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(0); //VSYNC

        glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
                if (key == GLFW_KEY_F1 && action == GLFW_RELEASE) {
                    menu = Menu.OFFICE;
                }
                if (key == GLFW_KEY_F2 && action == GLFW_RELEASE) {
                    gameManager.setNightStart(System.currentTimeMillis() - GameManager.NIGHT_LENGTH - 100);
                }
                if (key == GLFW_KEY_F3 && action == GLFW_RELEASE) {
                    showDebug = !showDebug;
                }
                if (key == GLFW_KEY_F11 && action == GLFW_RELEASE) {
                    toggleFullscreen();
                }
                if (key == GLFW_KEY_KP_0 && action == GLFW_RELEASE) {
                    gameManager.setPower(1);
                }
                if (key == GLFW_KEY_KP_1 && action == GLFW_RELEASE) {
                    gameManager.setPower(100);
                }
                if (key == GLFW_KEY_J && action == GLFW_RELEASE) {
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
            if (action == GLFW.GLFW_PRESS) {
                isMouseClicked = true;

                float mx = (float) ((mouseX[0] / width) * 2) - 1;
                float my = (float) ((mouseY[0] / height) * 2) - 1;

                Office office = Office.getInstance();

                float offset = 25;

                if(menu == Menu.MAIN_MENU){
                    if(mouseX[0] >= 140 && mouseX[0] <= 440) {
                        if (mouseY[0] >= 410 - offset && mouseY[0] <= 460 - offset) {
                            menu = Menu.PRE_GAME;
                            startTime = System.currentTimeMillis();
                            //nightNumber = 6;
                        } else if (mouseY[0] >= 350 - offset && mouseY[0] <= 400 - offset) {
                            menu = Menu.PRE_GAME;
                            startTime = System.currentTimeMillis();
                            //nightNumber = 6;
                        } else if (mouseY[0] >= 470 - offset && mouseY[0] <= 520 - offset) {
                            menu = Menu.PRE_GAME;
                            startTime = System.currentTimeMillis();
                            nightNumber = 6;
                        } else if (mouseY[0] >= 530 - offset && mouseY[0] <= 580 - offset) {
                            Freddy.getInstance().setAI_LEVEL(1);
                            Bonnie.getInstance().setAI_LEVEL(3);
                            Chica.getInstance().setAI_LEVEL(3);
                            Foxy.getInstance().setAI_LEVEL(2);
                            menu = Menu.CUSTOM_NIGHT;
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
                }else if (menu == Menu.CUSTOM_NIGHT){
                    boolean yOnButtons = between(0.5,0.65,my);

                    if(yOnButtons){
                        if(between(-.95,-.85,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Freddy.getInstance().setAI_LEVEL(Math.max(0, Math.min(Freddy.getInstance().getAI_LEVEL()-1,20)));
                        }
                        if(between(-.45,-.35,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Bonnie.getInstance().setAI_LEVEL(Math.max(0, Math.min(Bonnie.getInstance().getAI_LEVEL()-1,20)));
                        }
                        if(between(.05,.15,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Chica.getInstance().setAI_LEVEL(Math.max(0, Math.min(Chica.getInstance().getAI_LEVEL()-1,20)));
                        }
                        if(between(.55,.65,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Foxy.getInstance().setAI_LEVEL(Math.max(0, Math.min(Foxy.getInstance().getAI_LEVEL()-1,20)));
                        }
                        if(between(-.55,-.65,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Freddy.getInstance().setAI_LEVEL(Math.max(0, Math.min(Freddy.getInstance().getAI_LEVEL()+1,20)));
                        }
                        if(between(-.15,-.05,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Bonnie.getInstance().setAI_LEVEL(Math.max(0, Math.min(Bonnie.getInstance().getAI_LEVEL()+1,20)));
                        }
                        if(between(.35,.45,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Chica.getInstance().setAI_LEVEL(Math.max(0, Math.min(Chica.getInstance().getAI_LEVEL()+1,20)));
                        }
                        if(between(.85,.95,mx)){
                            sounds.get("blip.ogg").stop();
                            sounds.get("blip.ogg").play();
                            Foxy.getInstance().setAI_LEVEL(Math.max(0, Math.min(Foxy.getInstance().getAI_LEVEL()+1,20)));
                        }
                    }

                    if(between(0.7,1,mx) && between(0.7,1,my)){
                        System.out.println("start");
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        startTime = System.currentTimeMillis();
                        menu = Menu.PRE_GAME;
                        nightNumber = 7;
                    }

                    if(between(-0.7,-1,mx) && between(0.7,1,my)){
                        System.out.println("return");
                        sounds.get("blip.ogg").stop();
                        sounds.get("blip.ogg").play();
                        menu = Menu.MAIN_MENU;
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

        renderer = new Renderer();

        renderer.setHeight(height);
        renderer.setWidth(width);

        setWindowIcon(window);

        System.out.println("Loading loading screen texture...");
        loadingTexture = Texture.loadTextureFromResource("textures/jump.jpg");
        System.out.println();
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
            soundManager = new SoundManager();

            JSONObject data = new JSONObject(new String(Thread.currentThread().getContextClassLoader().getResourceAsStream("map.json").readAllBytes()));
            JSONObject texture = data.getJSONObject("textures");
            JSONObject audios = data.getJSONObject("audio");

            for(String s : texture.keySet()){
                System.out.println("Loading buffered texture: " + s);
                textures.put(s,Texture.loadTextureFromResource(texture.getString(s)));
                System.out.println();
            }

            System.out.println("Loading sounds.");

            System.out.println();

            for(String s : audios.keySet()){
                Sound so = new Sound(audios.getString(s),false);
                sounds.put(s,so);
                System.out.println();
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

                fan_stage++;

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

                if(fan_stage == 2) fan_stage = 0;
            }

            /**
             * Updating
             */

            if (GLFW.glfwGetInputMode(window, GLFW.GLFW_CURSOR) == GLFW.GLFW_CURSOR_NORMAL) {
                GLFW.glfwGetCursorPos(window, mouseX, mouseY);
                switch (menu) {
                    case MAIN_MENU -> {
                        int selection = menuSelection;
                        int offset = 25;

                        if(mouseX[0] >= 140 && mouseX[0] <= 440) {
                            if (mouseY[0] >= 410 - offset && mouseY[0] <= 460 - offset) {
                                selection = 1;
                            } else if (mouseY[0] >= 350 - offset && mouseY[0] <= 400 - offset) {
                                selection = 0;
                            } else if (mouseY[0] >= 470 - offset && mouseY[0] <= 520 - offset) {
                                selection = 2;
                            } else if (mouseY[0] >= 530 - offset && mouseY[0] <= 580 - offset) {
                                selection = 3;
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
            }

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

            int ventaBlackLengthMs = 160000;
            long timeUntilVentBlack = System.currentTimeMillis() - (gameManager.getNightStart() + GameManager.NIGHT_LENGTH) + ventaBlackLengthMs;

            if((Freddy.getInstance().getAI_LEVEL() + Bonnie.getInstance().getAI_LEVEL() + Chica.getInstance().getAI_LEVEL() + Foxy.getInstance().getAI_LEVEL()) / 4 >= 10) if(gameManager.isNightRunning() && timeUntilVentBlack >= 0)
            {
                sounds.get("ventablack.ogg").play();
                sounds.get("Ambiance1.ogg").stop();
                sounds.get("Ambiance1.ogg").setRepeating(false);
            } else if(timeUntilVentBlack >= -1000){
                sounds.get("Ambiance1.ogg").setVolume(0.3f * (Math.abs(timeUntilVentBlack) / 1000.0f));
            } else {
                sounds.get("Ambiance1.ogg").play();
                sounds.get("Ambiance1.ogg").setRepeating(true);
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
                    renderer.renderText("Five", 140, 100,50, Color.WHITE);
                    renderer.renderText("Nights", 140, 150,50, Color.WHITE);
                    renderer.renderText("at", 140, 200,50, Color.WHITE);
                    renderer.renderText("Freddy's", 140, 250,50, Color.WHITE);

                    renderer.renderText("New Game", 140, 350,50, Color.WHITE);
                    renderer.renderText("Continue", 140, 420,50, Color.WHITE);
                    renderer.renderText("Night " + nightNumber, 140, 432,25, Color.WHITE);
                    renderer.renderText("6th Night", 140, 490,50, Color.WHITE);
                    renderer.renderText("Custom Night", 140, 560,50, Color.GRAY);

                    switch (menuSelection) {
                        case 0 -> renderer.renderText(">>", 70, 350,50, Color.WHITE);
                        case 1 -> renderer.renderText(">>", 70, 420,50, Color.WHITE);
                        case 2 -> renderer.renderText(">>", 70, 490,50, Color.GRAY);
                        case 3 -> renderer.renderText(">>", 70, 560,50, Color.GRAY);
                    }

                    renderer.renderText("v1.0.0 - alpha", 10, height-20,20, Color.WHITE);
                    break;
                }
                case PRE_GAME : {
                    long timeSinceStart = System.currentTimeMillis() - startTime;
                    if(timeSinceStart > 0 && timeSinceStart < 3000) renderer.renderTexture(-1,-1,2,2,textures.get("help_wanted.png"),true,false,0);
                    if(timeSinceStart > 3000 && timeSinceStart < 6000) {
                        stopAllSounds();
                        if(timeSinceStart < 3100)sounds.get("blip.ogg").play();
                        renderer.renderText("12:00 AM",(width - renderer.textWidth("12:00 AM", 60)) / 2,(float) ((height-120) / 2), 60,Color.WHITE);
                        renderer.renderText("Night " + nightNumber,(width - renderer.textWidth("Night " + nightNumber, 60)) / 2,(float) ((height-120) / 2) - 60, 60,Color.WHITE);
                    }
                    if(timeSinceStart > 6000) {
                        menu = Menu.OFFICE;
                        sounds.get("fan.oga").play();
                        sounds.get("fan.oga").setRepeating(true);
                        sounds.get("Ambiance1.ogg").play();
                        sounds.get("Ambiance1.ogg").setRepeating(true);

                        if(nightNumber <= 6) gameManager.startNight(nightNumber);
                        else gameManager.startNight(7,Bonnie.getInstance().getAI_LEVEL(),Chica.getInstance().getAI_LEVEL(),Freddy.getInstance().getAI_LEVEL(),Foxy.getInstance().getAI_LEVEL());
                        if(nightNumber > 5) nightNumber = 5;
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
                    if(!gameManager.isPowerout()) switch (fan_stage) {
                        case 0: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.1.png"),true,false,0);
                            break;
                        }
                        case 1: {
                            renderer.renderTexture((scroll * 0.25f) - 0.03f,-0.385f,w,h,textures.get("fan.2.png"),true,false,0);
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

                    renderer.renderText(gameManager.getHour() == 0 ? ("12 AM") : (gameManager.getHour() + " AM") ,width - 150,80,60,Color.WHITE);
                    renderer.renderText(String.format("Power %.1f%%",gameManager.getPower()) ,10,height-25,40,Color.WHITE);
                    if(!gameManager.isPowerout()) renderer.renderTexture(-.99f,-0.8f,0.3f,0.1f,textures.get("power." + gameManager.getDevices() + ".png"),true,false,0);
                    break;
                }
                case CAMERAS : {
                    float cameraScroll = (float) (Math.sin(glfwGetTime() / 5));

                    String cameraImage = ((int) glfwGetTime()) % 2 == 0 ? "cam.blank.png" : "cam.selected.png";

                    if(!gameManager.getCamera().equals(Camera6.getInstance())) renderer.renderTexture(-1.25f + (cameraScroll * 0.25f),-1,2.5f,2,textures.get(gameManager.getCamera().getImage(cameraRandomness,cameraRandomness2)),true,true,glitchStrength);
                    else {
                        renderer.renderTexture(-1f, -1f, 2f, 2f, textures.get(gameManager.getCamera().getImage(cameraRandomness, cameraRandomness2)), true, true, 1f);
                        renderer.renderTexture(-.5f, -.25f, 1f, .5f, textures.get(gameManager.getCamera().getImage(cameraRandomness, cameraRandomness2)), true, false, glitchStrength);
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

                    int ti = gameManager.getHour();
                    renderer.renderText(ti == 0 ? ("12 AM") : (ti + " AM") ,width - 150,80,60,Color.WHITE);
                    renderer.renderText(String.format("Power %.1f%%",gameManager.getPower()) ,10,height-25,40,Color.WHITE);
                    if(!gameManager.isPowerout()) renderer.renderTexture(-.99f,-0.8f,0.3f,0.1f,textures.get("power." + gameManager.getDevices() + ".png"),true,false,0);
                    break;
                }
                case NIGHT_END : {
                    renderer.renderText("6 AM" ,(width - renderer.textWidth("6 AM", 80)) / 2, (float) ((height-60) / 2.0),80,Color.WHITE);
                    break;
                }
                case NIGHT_END_DEATH : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("loose.png"),true,false,0);
                    break;
                }
                case NOISE : {
                    renderer.renderTexture(-1,-1,2,2,textures.get("loose.png"),true,true,1);
                    break;
                }
                case CUSTOM_NIGHT : {
                    float aspectRatio = (float) width /height;
                    float fontSize = 0.046f*width;

                    //TODO: custom night rendering

                    renderer.renderText("Customize Night",(width-renderer.textWidth("Customize Night",fontSize * 1.3f)) / 2.1f,((-0.8f + 1) / 2) * width,fontSize * 1.3f,Color.WHITE);

                    renderer.renderTexture(-0.9f,-0.3f,0.3f,0.3f * aspectRatio,textures.get("freddy.png"),false,false,0);
                    renderer.renderTexture(-0.4f,-0.3f,0.3f,0.3f * aspectRatio,textures.get("bonnie.png"),false,false,0);
                    renderer.renderTexture(0.1f,-0.3f,0.3f,0.3f * aspectRatio,textures.get("chica.png"),false,false,0);
                    renderer.renderTexture(0.6f,-0.3f,0.3f,0.3f * aspectRatio,textures.get("foxy.png"),false,false,0);

                    renderer.renderText("Freddy", ((-0.9f+1)/2f) * width, ((-0.3f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    renderer.renderText("Bonnie", ((-0.4f+1)/2f) * width, ((-0.3f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    renderer.renderText("Chica", ((.125f+1)/2f) * width, ((-0.3f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    renderer.renderText("Foxy", ((.65f+1)/2f) * width, ((-0.3f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);

                    renderer.renderText("A.I. Level",((-.9f+1)/2f) * width, ((.55f+1)/2f) * height, fontSize,Color.WHITE);
                    renderer.renderText("A.I. Level",((-.4f+1)/2f) * width, ((.55f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText("A.I. Level",((.1f+1)/2f) * width, ((.55f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText("A.I. Level",((.6f+1)/2f) * width, ((.55f+1)/2f) * height,fontSize,Color.WHITE);

                    renderer.renderText("<",((-.9f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText("<",((-.4f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText("<",((.1f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText("<",((.6f+1)/2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);

                    float x = (float) (.3 - (renderer.textWidth(">", fontSize) / width));
                    float x0 = renderer.textWidth("0", fontSize) / width;

                    renderer.renderText(">", (((-.9f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText(">", (((-.4f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText(">", (((.1f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText(">", (((.6f + x) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);

                    renderer.renderText(String.format("%02d", Freddy.getInstance().getAI_LEVEL()), (((-.9f + x - (6 * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText(String.format("%02d", Bonnie.getInstance().getAI_LEVEL()), (((-.4f + x - (6 * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText(String.format("%02d", Chica.getInstance().getAI_LEVEL()), (((.1f + x - (6 * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);
                    renderer.renderText(String.format("%02d", Foxy.getInstance().getAI_LEVEL()), (((.6f + x - (6 * x0)) + 1) / 2f) * width, ((.7f+1)/2f) * height,fontSize,Color.WHITE);

                    renderer.renderText("(0-2) easy    (3-6)med    (7-12)hard   (12+)extreme", ((-.5f+1)/2f) * width, ((.9f+1)/2f) * height,fontSize * 0.6f,Color.WHITE);

                    renderer.renderText("Start", ((.7f+1)/2f) * width, ((.95f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
                    renderer.renderText("Back", ((-.95f+1)/2f) * width, ((.95f+1)/2f) * height,fontSize * 1.3f,Color.WHITE);
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
                renderer.renderText("FPS: " + fps, 10, 20,20, Color.WHITE);
                renderer.renderText("Render Size: " + width + " / " + height, 10, 40,20, Color.WHITE);
                renderer.renderText("Last Frame Time: " + String.format("%.2f ms",lastFrameTime*1000), 10, 60,20, Color.WHITE);
                renderer.renderText("Time: " + String.format("%.4fs",glfwGetTime()), 10, 80,20, Color.WHITE);
                renderer.renderText("Components Drawn: " + componentsDrawn, 10, 100,20, Color.WHITE);
                renderer.renderText("Delta Time: " + String.format("%.2fs",deltaTime), 10, 120,20, Color.WHITE);
                renderer.renderText("Scroll: " + String.format("%.2f",scroll), 10, 140,20, Color.WHITE);
                int y = 160;
                for(Sound s : sounds.values()){
                    if(s.isPlaying()) renderer.renderText("Sound '" + s.getFilepath() + "' " + s.getCurrentTime() + " / " + s.getTotalLength(), 10, y,20, Color.WHITE);
                    if(s.isPlaying()) y+=20;
                }

                renderer.renderText(String.format("%s %s     %02d","Bonnie", Bonnie.getInstance().getCurrentCamera().getCameraName(), Bonnie.getInstance().getAI_LEVEL()), 400, 40,40, Color.RED);
                renderer.renderText(String.format("%s %s     %02d","Chica  ", Chica.getInstance().getCurrentCamera().getCameraName(), Chica.getInstance().getAI_LEVEL()), 400, 80,40, Color.RED);
                renderer.renderText(String.format("%s %s     %02d    %.2fs","Freddy", Freddy.getInstance().getCurrentCamera().getCameraName(), Freddy.getInstance().getAI_LEVEL(), Freddy.getInstance().stillStalledFor() / 1000f), 400, 120,40, Color.RED);
                renderer.renderText(String.format("%s               %02d     %02d    %.2fs","Foxy  ", Foxy.getInstance().getStage(), Foxy.getInstance().getAI_LEVEL(), Foxy.getInstance().stillStalledFor() / 1000f), 400, 160,40, Color.RED);

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
        boolean enableLogging = false;

        File f = new File("crash-report.txt");
        File data = new File("save.txt");
        if(!f.exists()) f.createNewFile();
        if(!data.exists()) {
            data.createNewFile();
            FileOutputStream outputStream = new FileOutputStream(new File("save.txt"));
            outputStream.write(1);
        }

        FileInputStream inputStream = new FileInputStream(data);

        PrintStream debugStream = new PrintStream(new FileOutputStream(f));

        if(enableLogging){
            System.setOut(debugStream);
            System.setErr(debugStream);
        }

        try {
            new FNAFMain().run(inputStream.read());
        } catch (Exception e) {
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

    private boolean between(double val1, double val2, double toCheck){
        return Math.min(val1,val2) <= toCheck && Math.max(val1,val2) >= toCheck;
    }

    public static void stopAllSounds(){
        for(Sound s : sounds.values()) s.stop();
    }

    private void toggleFullscreen() {
        fullscreen = !fullscreen;

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        if (fullscreen) {
            GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, 50, 50, 1280, 720, 0);
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
}
