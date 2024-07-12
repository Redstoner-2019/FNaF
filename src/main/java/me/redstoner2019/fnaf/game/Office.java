package me.redstoner2019.fnaf.game;

import me.redstoner2019.fnaf.game.animatronics.Animatronic;

public class Office extends Room{
    private static Office INSTANCE;
    private boolean leftDoor = false;
    private boolean rightDoor = false;
    private boolean leftLight = false;
    private boolean rightLight = false;
    private DoorState leftDoorState = DoorState.OPEN;
    private DoorState rightDoorState = DoorState.OPEN;
    private int leftDoorAnimation = 0;
    private int rightDoorAnimation = 0;
    private Animatronic leftDoorAnimatronic = null;
    private Animatronic rightDoorAnimatronic = null;
    private Animatronic inRoomAnimatronic = null;

    public Animatronic getInRoomAnimatronic() {
        return inRoomAnimatronic;
    }

    public void setInRoomAnimatronic(Animatronic inRoomAnimatronic) {
        this.inRoomAnimatronic = inRoomAnimatronic;
    }

    public Animatronic getRightDoorAnimatronic() {
        return rightDoorAnimatronic;
    }

    public void setRightDoorAnimatronic(Animatronic rightDoorAnimatronic) {
        this.rightDoorAnimatronic = rightDoorAnimatronic;
    }

    public Animatronic getLeftDoorAnimatronic() {
        return leftDoorAnimatronic;
    }

    public void setLeftDoorAnimatronic(Animatronic leftDoorAnimatronic) {
        this.leftDoorAnimatronic = leftDoorAnimatronic;
    }

    public int getLeftDoorAnimation() {
        return leftDoorAnimation;
    }

    public int getRightDoorAnimation() {
        return rightDoorAnimation;
    }

    public void setLeftDoorAnimation(int leftDoorAnimation) {
        this.leftDoorAnimation = leftDoorAnimation;
    }

    public void setRightDoorAnimation(int rightDoorAnimation) {
        this.rightDoorAnimation = rightDoorAnimation;
    }

    private Office() {
    }

    public boolean isLeftDoor() {
        return leftDoor;
    }

    public DoorState getLeftDoorState() {
        return leftDoorState;
    }

    public void setLeftDoorState(DoorState leftDoorState) {
        this.leftDoorState = leftDoorState;
    }

    public DoorState getRightDoorState() {
        return rightDoorState;
    }

    public void setRightDoorState(DoorState rightDoorState) {
        this.rightDoorState = rightDoorState;
    }

    public void setLeftDoor(boolean leftDoor) {
        this.leftDoor = leftDoor;
    }

    public boolean isRightDoor() {
        return rightDoor;
    }

    public void setRightDoor(boolean rightDoor) {
        this.rightDoor = rightDoor;
    }

    public boolean isLeftLight() {
        return leftLight;
    }

    public void setLeftLight(boolean leftLight) {
        this.leftLight = leftLight;
    }

    public boolean isRightLight() {
        return rightLight;
    }

    public void setRightLight(boolean rightLight) {
        this.rightLight = rightLight;
    }

    public static Office getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Office();
        }

        return INSTANCE;
    }
}

