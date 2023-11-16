package radon.engine.audio;

import org.lwjgl.system.MemoryStack;

import static org.lwjgl.openal.AL10.*;
import static radon.engine.audio.AudioDebug.checkAudioErrors;
import static radon.engine.core.RadonConfigConstants.DEBUG;
import static radon.engine.util.Asserts.assertNonNull;
import static radon.engine.util.Asserts.assertTrue;
import static radon.engine.util.handles.IntHandle.NULL;

public class AudioQueue {

    private final int sourceHandle;

    AudioQueue(int sourceHandle) {
        this.sourceHandle = sourceHandle;
    }

    public void enqueue(AudioBuffer buffer) {
        assertNonNull(buffer);
        alSourceQueueBuffers(sourceHandle, buffer.handle());
        if(DEBUG) {
            checkAudioErrors();
        }
    }

    public boolean empty() {
        return size() == 0;
    }

    public int size() {
        return alGetSourcei(sourceHandle, AL_BUFFERS_QUEUED);
    }

    public int processed() {
        return alGetSourcei(sourceHandle, AL_BUFFERS_PROCESSED);
    }

    public int pending() {
        return size() - processed();
    }

    public int unqueue() {
        final int count = processed();
        if(count > 0) {
            alSourceUnqueueBuffers(sourceHandle);
        }
        return count;
    }

    public void unqueue(int count) {
        assertTrue(count >= 0);
        if(count == 0) {
            return;
        }
        try(MemoryStack stack = MemoryStack.stackPush()) {
            alSourceUnqueueBuffers(sourceHandle, stack.mallocInt(count));
        }
    }

    public void clear() {
        alSourcei(sourceHandle, AL_BUFFER, NULL);
    }

}
