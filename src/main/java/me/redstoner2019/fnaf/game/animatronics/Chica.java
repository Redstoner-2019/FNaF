package me.redstoner2019.fnaf.game.animatronics;

import me.redstoner2019.fnaf.FNAFMain;
import me.redstoner2019.fnaf.game.DoorState;
import me.redstoner2019.fnaf.game.Office;
import me.redstoner2019.fnaf.game.cameras.*;

import java.util.Random;

public class Chica extends Animatronic{
    private static Chica INSTANCE;
    private Chica(){
        setCurrentCamera(Camera1A.getInstance());
        setAI_LEVEL(5);
    }

    @Override
    public void movementOpportunity() {
        Random random = new Random();
        int roll = random.nextInt(19) + 1;
        System.out.println("Chica rolled " + roll + " ai " + getAI_LEVEL());
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
                    switch (random.nextInt(3)) {
                        case 0 : {
                            setCurrentCamera(Camera1B.getInstance());
                            break;
                        }
                        case 1,2 : {
                            setCurrentCamera(Camera6.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera6" : {
                    switch (random.nextInt(5)) {
                        case 0 : {
                            setCurrentCamera(Camera7.getInstance());
                            break;
                        }
                        case 1 : {
                            setCurrentCamera(Camera1B.getInstance());
                            break;
                        }
                        case 2,3,4 : {
                            setCurrentCamera(Camera4A.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera4A" : {
                    switch (random.nextInt(4)) {
                        case 0,1 : {
                            setCurrentCamera(Camera4B.getInstance());
                            break;
                        }
                        case 2 : {
                            setCurrentCamera(Camera6.getInstance());
                            break;
                        }
                        case 3 : {
                            setCurrentCamera(Camera1B.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "Camera4B" : {
                    switch (random.nextInt(3)) {
                        case 0 : {
                            setCurrentCamera(Camera4A.getInstance());
                            break;
                        }
                        case 1,2 : {
                            Office.getInstance().setRightDoorAnimatronic(this);
                            setCurrentCamera(OfficeCamera.getInstance());
                            break;
                        }
                    }
                    break;
                }
                case "OfficeCamera" : {
                    if(Office.getInstance().getRightDoorState() == DoorState.OPEN){
                        System.out.println("Chica -> Kill");
                        FNAFMain.fnafMain.triggerJumpScare("chica.jump.",15,true);
                    } else {
                        Office.getInstance().setRightDoorAnimatronic(null);
                        FNAFMain.sounds.get("Knock2.ogg").play();
                        switch (random.nextInt(10)) {
                            case 0,1,2,3: {
                                setCurrentCamera(Camera4B.getInstance());
                                break;
                            }
                            case 4,5,6: {
                                if(Freddy.getInstance().getCurrentCamera().getCameraName().equals("Camera1A")){
                                    setCurrentCamera(Camera1A.getInstance());
                                } else {
                                    setCurrentCamera(Camera1B.getInstance());
                                }
                                break;
                            }
                            case 7,8: {
                                setCurrentCamera(Camera7.getInstance());
                                break;
                            }
                            case 9: {
                                setCurrentCamera(Camera6.getInstance());
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public static Chica getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Chica();
        }
        return INSTANCE;
    }
}
