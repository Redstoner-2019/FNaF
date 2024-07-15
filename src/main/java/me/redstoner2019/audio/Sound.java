package me.redstoner2019.audio;

import me.redstoner2019.graphics.general.Texture;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SAMPLE_OFFSET;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound {
    private int bufferId;
    private int sourceId;
    private float volume = 1f;
    private float general_volume = 0.15f;
    private String filepath;

    private boolean isPlaying = false;

    public Sound(String filepath, boolean loops){
        this.filepath = filepath;

        // Allocate space to store the return information from stb
        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        ByteBuffer b = Texture.createBuffer(filepath);

        System.out.println(b);

        ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(b,channelsBuffer,sampleRateBuffer);

        if(rawAudioBuffer == null) {
            System.out.println("Could not load sound '" + filepath + "'");
            stackPop();
            stackPop();
            return;
        } else {
            System.out.println("Loaded sound '" + filepath + "'");
        }

        int channels = channelsBuffer.get();
        int sampleRate = sampleRateBuffer.get();

        stackPop();
        stackPop();

        int format = -1;
        if(channels == 1) {
            format = AL_FORMAT_MONO16;
        } else if(channels == 2){
            format = AL_FORMAT_STEREO16;
        }

        bufferId = alGenBuffers();
        alBufferData(bufferId, format, rawAudioBuffer, sampleRate);

        sourceId = alGenSources();

        alSourcei(sourceId, AL_BUFFER, bufferId);
        alSourcei(sourceId, AL_LOOPING, loops ? 1 : 0);
        alSourcei(sourceId, AL_POSITION, 0);
        alSourcef(sourceId, AL_GAIN, 0.15f);  //Volume

        free(rawAudioBuffer);

        updateGain();
    }

    public void delete(){
        alDeleteBuffers(sourceId);
        alDeleteBuffers(bufferId);
    }

    public void play(){
        updateGain();
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if(state == AL_STOPPED){
            isPlaying = false;
            alSourcei(sourceId, AL_POSITION, 0);
        }

        if(!isPlaying) {
            alSourcePlay(sourceId);
            isPlaying = true;
        }
    }

    public void stop(){
        if(isPlaying) {
            alSourceStop(sourceId);
            isPlaying = false;
        }
    }

    public int getBufferId() {
        return bufferId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public String getFilepath() {
        return filepath;
    }

    public boolean isPlaying() {
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if(state == AL_STOPPED) isPlaying = false;
        return isPlaying;
    }
    public void setRepeating(boolean repeating){
        alSourcei(sourceId, AL_LOOPING, repeating ? 1 : 0);
    }
    public void updateGain(){
        general_volume = SoundManager.getInstance().getVolume();
        alSourcef(sourceId, AL_GAIN, volume * general_volume);
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        updateGain();
    }

    public void setCursor(int frame){
        alSourcei(sourceId, AL_POSITION, frame);
    }
    public int getCursor(){
        return alGetSourcei(sourceId, AL_SAMPLE_OFFSET);
    }
    public int getLength(){
        int sizeInBytes = alGetBufferi(bufferId, AL_SIZE);

        int channels = alGetBufferi(bufferId, AL_CHANNELS);
        int bitsPerSample = alGetBufferi(bufferId, AL_BITS);

        int bytesPerSample = channels * (bitsPerSample / 8);

        if(bytesPerSample == 0) return 0;

        return sizeInBytes / bytesPerSample;
    }

    public int getLengthMS(){
        return getLength() / alGetBufferi(bufferId, AL_FREQUENCY);
    }

    public String getCurrentTime(){
        int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        if(frequency == 0) frequency = 44100;
        return alGetSourcei(sourceId, AL_SAMPLE_OFFSET) / frequency + "s";
    }

    public String getTotalLength(){
        int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        if(frequency == 0) return "0";
        return getLength() / frequency + "s";
    }
}
