package radon.engine.audio.decoders;

import radon.engine.audio.AudioBuffer;
import radon.engine.audio.AudioDataFormat;
import radon.engine.logging.Log;

import java.nio.file.Path;

public interface AudioDecoder {

    static AudioBuffer decode(Path audioFile, AudioDataFormat dataFormat) {

        // Only supporting .ogg for now
        if (dataFormat == AudioDataFormat.OGG) {
            return new VorbisAudioDecoder().decode(audioFile);
        }

        Log.error("Unsupported audio data format: " + dataFormat);

        return null;
    }

    AudioBuffer decode(Path audioFile);

}
