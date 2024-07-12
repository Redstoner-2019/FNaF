package me.redstoner2019.audio;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class SoundPlayer {
    private long device;
    private long context;
    private int buffer;
    private int source;

    public SoundPlayer() {
        device = ALC10.alcOpenDevice((ByteBuffer) null);
        context = ALC10.alcCreateContext(device, (IntBuffer) null);
        ALC10.alcMakeContextCurrent(context);
    }

    public void loadSound(String soundFile) {
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(soundFile))) {
            int channels = audioInputStream.getFormat().getChannels();
            int sampleRate = (int) audioInputStream.getFormat().getSampleRate();
            int bitsPerSample = audioInputStream.getFormat().getSampleSizeInBits();

            byte[] data = new byte[audioInputStream.available()];
            audioInputStream.read(data);

            ShortBuffer shortBuffer = BufferUtils.createShortBuffer(data.length / 2);
            for (int i = 0; i < data.length; i += 2) {
                shortBuffer.put((short) (((data[i + 1] & 0xFF) << 8) | (data[i] & 0xFF)));
            }
            shortBuffer.flip();

            buffer = AL10.alGenBuffers();
            AL10.alBufferData(buffer, channels == 1 ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16, shortBuffer, sampleRate);

            source = AL10.alGenSources();
            AL10.alSourcei(source, AL10.AL_BUFFER, buffer);
        } catch (Exception e) {
            System.err.println("Error loading sound: " + e.getMessage());
        }
    }

    public void playSound() {
        AL10.alSourcePlay(source);
    }

    public void stopSound() {
        AL10.alSourceStop(source);
    }

    public void cleanup() {
        AL10.alDeleteSources(source);
        AL10.alDeleteBuffers(buffer);
        ALC10.alcDestroyContext(context);
        ALC10.alcCloseDevice(device);
    }
}