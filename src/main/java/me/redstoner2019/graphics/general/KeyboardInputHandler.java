package me.redstoner2019.graphics.general;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyboardInputHandler extends GLFWKeyCallback {
    private StringBuilder inputString = new StringBuilder();
    private boolean shiftPressed = false;
    private boolean altGr = false;
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        if (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT) {
            if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                shiftPressed = true;
            } else {
                char character = translateKeyToChar(key, shiftPressed);
                if (character != 0) {
                    inputString.append(character);
                } else if (key == GLFW.GLFW_KEY_BACKSPACE && inputString.length() > 0) {
                    inputString.setLength(inputString.length() - 1);
                }
            }
        } else if (action == GLFW.GLFW_RELEASE) {
            if (key == GLFW.GLFW_KEY_LEFT_SHIFT || key == GLFW.GLFW_KEY_RIGHT_SHIFT) {
                shiftPressed = false;
            }
        }
    }

    private char translateKeyToChar(int key, boolean shiftPressed) {
        if (key >= GLFW.GLFW_KEY_A && key <= GLFW.GLFW_KEY_Z) {
            return shiftPressed ? (char) ('A' + (key - GLFW.GLFW_KEY_A)) : (char) ('a' + (key - GLFW.GLFW_KEY_A));
        } else if (key >= GLFW.GLFW_KEY_0 && key <= GLFW.GLFW_KEY_9) {
            return shiftPressed ? "=!\"§$%&/()".charAt(key - GLFW.GLFW_KEY_0) : (char) ('0' + (key - GLFW.GLFW_KEY_0));
        } else if (key >= GLFW.GLFW_KEY_KP_0 && key <= GLFW.GLFW_KEY_KP_9) {
            return (char) ('0' + (key - GLFW.GLFW_KEY_KP_0));
        } else if (key == GLFW.GLFW_KEY_SPACE) {
            return ' ';
        } else if (shiftPressed && !altGr) {
            return switch (key) {
                case GLFW.GLFW_KEY_MINUS -> '?';
                case GLFW.GLFW_KEY_EQUAL -> '°';
                case GLFW.GLFW_KEY_LEFT_BRACKET -> '{';
                case GLFW.GLFW_KEY_RIGHT_BRACKET -> '*';
                case GLFW.GLFW_KEY_BACKSLASH -> '\'';
                case GLFW.GLFW_KEY_SEMICOLON -> ':';
                case GLFW.GLFW_KEY_APOSTROPHE -> '"';
                case GLFW.GLFW_KEY_GRAVE_ACCENT -> '~';
                case GLFW.GLFW_KEY_COMMA -> ';';
                case GLFW.GLFW_KEY_PERIOD -> ':';
                case GLFW.GLFW_KEY_SLASH -> '_';
                case 162 -> '>';
                default -> 0;
            };
        } else if(!shiftPressed && !altGr) {
            return switch (key) {
                case GLFW.GLFW_KEY_MINUS -> 'ß';
                case GLFW.GLFW_KEY_EQUAL -> '´';
                case GLFW.GLFW_KEY_LEFT_BRACKET -> '[';
                case GLFW.GLFW_KEY_RIGHT_BRACKET -> '+';
                case GLFW.GLFW_KEY_BACKSLASH -> '#';
                case GLFW.GLFW_KEY_SEMICOLON -> ';';
                case GLFW.GLFW_KEY_APOSTROPHE -> '\'';
                case GLFW.GLFW_KEY_GRAVE_ACCENT -> '`';
                case GLFW.GLFW_KEY_COMMA -> ',';
                case GLFW.GLFW_KEY_PERIOD -> '.';
                case GLFW.GLFW_KEY_SLASH -> '-';
                case 162 -> '<';
                default -> 0;
            };
        } else if(altGr) {
            return switch (key) {
                case 51 -> '³';
                case 55 -> '{';
                case 56 -> '[';
                case 57 -> ']';
                case 58 -> '}';
                case 59 -> '\\';
                case 93 -> '~';
                case 162 -> '|';
                default -> 0;
            };
        }
        return 0;
    }

    public String getInputString() {
        return inputString.toString();
    }
    public void resetInputString(){
        inputString = new StringBuilder();
    }

    public void setInputString(String str){
        resetInputString();
        inputString.append(str);
    }
}
