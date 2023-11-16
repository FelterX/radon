package radon.engine.audio;

import org.lwjgl.openal.ALC;
import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.logging.Log;
import radon.engine.util.types.Singleton;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcGetString;
import static radon.engine.audio.AudioDistanceModel.EXPONENT_DISTANCE;

public final class AudioSystem extends RadonSystem {

    @Singleton
    private static AudioSystem instance;

    private static AudioDistanceModel distanceModel;

    public static AudioDistanceModel distanceModel() {
        return distanceModel;
    }

    public static void distanceModel(AudioDistanceModel distanceModel) {
        alDistanceModel(asOpenALDistanceModel(requireNonNull(distanceModel)));
        AudioSystem.distanceModel = distanceModel;
    }

    private AudioContext context;
    private AudioDevice device;
    private AudioListener listener;

    private AudioSystem(RadonSystemManager systemManager) {
        super(systemManager);
    }

    @Override
    protected void init() {

        device = createDefaultDevice();
        context = new AudioContext(device);
        listener = new AudioListener();

        distanceModel(EXPONENT_DISTANCE);

        AudioListener.instance = listener;
        AudioDevice.instance = device;
        AudioContext.instance = context;
    }

    @Override
    protected void terminate() {

        context.release();
        device.release();

        ALC.destroy();

        AudioListener.instance = null;
        AudioDevice.instance = null;
        AudioContext.instance = null;
    }

    public void recreate() {
        init();
    }

    private AudioDevice createDefaultDevice() {
        return new AudioDevice(alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER));
    }

    public void update() {
        listener.update();
    }

    private static int asOpenALDistanceModel(AudioDistanceModel distanceModel) {

        switch(distanceModel) {

            case EXPONENT_DISTANCE:
                return AL_EXPONENT_DISTANCE;
            case EXPONENT_DISTANCE_CLAMPED:
                return AL_EXPONENT_DISTANCE_CLAMPED;
            case INVERSE_DISTANCE:
                return AL_INVERSE_DISTANCE;
            case INVERSE_DISTANCE_CLAMPED:
                return AL_INVERSE_DISTANCE_CLAMPED;
            case LINEAR_DISTANCE:
                return AL_LINEAR_DISTANCE;
            case LINEAR_DISTANCE_CLAMPED:
                return AL_LINEAR_DISTANCE_CLAMPED;
        }

        Log.error("Unknown distance model " + distanceModel());
        return AL_EXPONENT_DISTANCE;
    }
}
