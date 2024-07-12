package me.redstoner2019.audio;

import me.redstoner2019.graphics.general.Texture;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.AL_SAMPLE_OFFSET;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.libc.LibCStdlib.free;

public class Sound {
    private int bufferId;
    private int sourceId;
    private String filepath;

    private boolean isPlaying = false;

    public Sound(String filepath, boolean loops){
        this.filepath = filepath;

        // Allocate space to store the return information from stb
        stackPush();
        IntBuffer channelsBuffer = stackMallocInt(1);
        stackPush();
        IntBuffer sampleRateBuffer = stackMallocInt(1);

        ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(Texture.createBuffer(filepath),channelsBuffer,sampleRateBuffer);

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
        alSourcef(sourceId, AL_GAIN, 0.3f);  //Volume

        free(rawAudioBuffer);
    }

    public void delete(){
        alDeleteBuffers(sourceId);
        alDeleteBuffers(bufferId);
    }

    public void play(){
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
    public void setVolume(float volume){
        alSourcef(sourceId, AL_GAIN, volume);
    }
    public void setCursor(int frame){
        alSourcei(sourceId, AL_POSITION, frame);
    }
    public int getCursor(){
        return alGetSourcei(sourceId, AL_SAMPLE_OFFSET);
    }
    public int getLength(){
        int sizeInBytes = alGetBufferi(bufferId, AL_SIZE);
        //int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        int channels = alGetBufferi(bufferId, AL_CHANNELS);
        int bitsPerSample = alGetBufferi(bufferId, AL_BITS);

        int bytesPerSample = channels * (bitsPerSample / 8);
        return sizeInBytes / bytesPerSample;
    }

    public String getCurrentTime(){
        int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        return alGetSourcei(sourceId, AL_SAMPLE_OFFSET) / frequency + "s";
    }

    public String getTotalLength(){
        int frequency = alGetBufferi(bufferId, AL_FREQUENCY);
        return getLength() / frequency + "s";
    }
}
