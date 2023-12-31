package radon.engine.audio.decoders;

import org.lwjgl.system.MemoryStack;
import radon.engine.audio.AudioBuffer;
import radon.engine.audio.AudioFormat;
import radon.engine.logging.Log;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Path;

import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAddress;
import static org.lwjgl.system.libc.LibCStdlib.free;
import static radon.engine.util.handles.IntHandle.NULL;
import static radon.engine.util.types.DataType.INT16_SIZEOF;

/**
 * A simple Vorbis (.ogg) audio decoder. Audio files should be 96 Kbps.
 *
 * */
public final class VorbisAudioDecoder implements AudioDecoder {

    public VorbisAudioDecoder() {
    }

    @Override
    public AudioBuffer decode(Path audioFile) {

        try(MemoryStack stack = stackPush()) {

            IntBuffer error = stack.ints(0);
            IntBuffer channels = stack.ints(0);
            IntBuffer frequency = stack.ints(0);

            String filename = audioFile.toAbsolutePath().toString();

            final long decoder = stb_vorbis_open_filename(filename, error, null);

            if(decoder == NULL) {
                Log.error("Failed to open .ogg audio file: " + filename + ". Error " + error.get(0));
                return null;
            }

            ShortBuffer data = stb_vorbis_decode_filename(filename, channels, frequency);

            if(data == null) {
                Log.error("Could not decode vorbis audio file: " + filename + ": " + stb_vorbis_get_error(decoder));
                return null;
            }

            AudioFormat format = AudioFormat.fromChannels(channels.get(0), INT16_SIZEOF);

            AudioBuffer buffer = new AudioBuffer();

            buffer.data(memAddress(data), data.capacity() * INT16_SIZEOF, format, frequency.get(0));

            free(data);

            return buffer;
        }
    }
}
