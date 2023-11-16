package radon.engine.audio;


import radon.engine.logging.Log;
import radon.engine.resource.Resource;
import radon.engine.util.handles.LongHandle;

import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static radon.engine.audio.AudioDebug.checkAudioErrors;

public final class AudioDevice implements LongHandle, Resource {

    static AudioDevice instance;

    public static AudioDevice get() {
        return instance;
    }


    private final String name;
    private long handle;

    AudioDevice(String name) {

        this.name = name;

        handle = alcOpenDevice(name);

        if(handle == NULL) {
            checkAudioErrors();
            Log.error("Failed to open Audio Device " + name);
        }
    }

    public String name() {
        return name;
    }

    @Override
    public long handle() {
        return handle;
    }

    @Override
    public void release() {
        alcCloseDevice(handle);
        handle = NULL;
    }
}
