package radon.engine.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.system.MemoryStack;
import radon.engine.logging.Log;
import radon.engine.resource.Resource;
import radon.engine.util.handles.LongHandle;

import java.nio.IntBuffer;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.openal.ALC10.*;
import static radon.engine.audio.AudioDebug.checkAudioErrors;

public final class AudioContext implements LongHandle, Resource {

    static AudioContext instance;
    private long handle;
    private AudioDevice device;
    private ALCCapabilities capabilities;

    public AudioContext(AudioDevice device) {
        this.device = requireNonNull(device);
        createOpenALContext();
        makeCurrent();
        capabilities = ALC.createCapabilities(handle);

        if (handle != NULL) {
            AL.createCapabilities(capabilities);
        }
        checkAudioErrors();
    }

    public static AudioContext get() {
        return instance;
    }

    public void makeCurrent() {
        alcMakeContextCurrent(handle);
    }

    public boolean isCurrent() {
        return alcGetCurrentContext() == handle;
    }

    @Override
    public long handle() {
        return handle;
    }

    public ALCCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public void release() {
        handle = NULL;
        device = null;
    }

    private void createOpenALContext() {

        try (MemoryStack stack = MemoryStack.stackPush()) {

            IntBuffer attributes = stack.ints(0);

            handle = alcCreateContext(device.handle(), attributes);

            if (handle == NULL) {
                Log.error("Failed to create OpenAL context");
            }
        }
    }
}
