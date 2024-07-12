package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;

import java.util.Random;

public class Bonnie extends Animatronic{
    private static Bonnie INSTANCE;
    private Bonnie(){
        setCurrentCamera(Camera1A.getInstance());
        setAI_LEVEL(5);
    }

    @Override
    public void movementOpportunity() {
        Random random = new Random();
        int roll = random.nextInt(19) + 1;
        System.out.println("Bonnie rolled " + roll + " ai " + getAI_LEVEL());
        if(roll <= getAI_LEVEL()){
            System.out.print("Moving from " + getCurrentCamera().getCameraName() + " to ");
            switch (getCurrentCamera().getCameraName()) {
                case "Camera1A" : {
                    setCurrentCamera(Camera1B.getInstance());
                    break;
                }
                case "Camera1B" : {
                    switch (random.nextInt(6)) {
                        case 0,1,2: {
                            setCurrentCamera(Camera5.getInstance());
                            break;
                        }
                        case 3,4: {
                            setCurrentCamera(Camera3.getInstance());
                            break;
                        }
                        case 5: {
                            setCurrentCamera(Camera2A.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera5" : {
                    switch (random.nextInt(3)) {
                        case 0: {
                            setCurrentCamera(Camera1B.getInstance());
                            break;
                        }
                        case 1: {
                            setCurrentCamera(Camera3.getInstance());
                            break;
                        }
                        case 2: {
                            setCurrentCamera(Camera2A.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera3" : {
                    switch (random.nextInt(3)) {
                        case 0: {
                            setCurrentCamera(Camera1B.getInstance());
                            break;
                        }
                        case 1: {
                            setCurrentCamera(Camera5.getInstance());
                            break;
                        }
                        case 2: {
                            setCurrentCamera(Camera2A.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera2A" : {
                    switch (random.nextInt(2)) {
                        case 0: {
                            setCurrentCamera(Camera2B.getInstance());
                            break;
                        }
                        case 1: {
                            setCurrentCamera(Camera3.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera2B" : {
                    switch (random.nextInt(4)) {
                        case 0: {
                            setCurrentCamera(Camera2A.getInstance());
                            break;
                        }
                        case 1,2,3: {
                            Office.getInstance().setLeftDoorAnimatronic(this);
                            setCurrentCamera(OfficeCamera.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "OfficeCamera" : {
                    if(Office.getInstance().getLeftDoorState() == DoorState.OPEN){
                        System.out.println("Bonnie -> Kill");
                        FNAFMain.fnafMain.triggerJumpScare("bonnie.jump",10,true);
                    } else {
                        Office.getInstance().setLeftDoorAnimatronic(null);
                        FNAFMain.sounds.get("Knock2.ogg").play();
                        switch (random.nextInt(10)) {
                            case 0,1,2,3: {
                                setCurrentCamera(Camera1B.getInstance());
                                break;
                            }
                            case 4,5,6: {
                                if(Freddy.getInstance().getCurrentCamera().getCameraName().equals("Camera1A")){
                                    setCurrentCamera(Camera1A.getInstance());
                                } else {
                                    setCurrentCamera(Camera5.getInstance());
                                }
                                break;
                            }
                            case 7,8: {
                                setCurrentCamera(Camera3.getInstance());
                                break;
                            }
                            case 9: {
                                setCurrentCamera(Camera5.getInstance());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
            System.out.println(getCurrentCamera().getCameraName());
        }
    }

    public static Bonnie getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Bonnie();
        }
        return INSTANCE;
    }
}
