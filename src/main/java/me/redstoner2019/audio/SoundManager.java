package me.redstoner2019.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import static org.lwjgl.openal.ALC10.*;

public class SoundManager {

    private long audioContext;
    private long audioDevice;

    public SoundManager(){
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice,attributes);
        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if(!alcCapabilities.OpenALC10) {
            assert false : "Audio library not supported.";
        }
    }

    public long getAudioDevice() {
        return audioDevice;
    }

    public long getAudioContext() {
        return audioContext;
    }

    public void destroy(){
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);
    }
}
