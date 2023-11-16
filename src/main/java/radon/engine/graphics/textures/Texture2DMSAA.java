package radon.engine.graphics.textures;

import radon.engine.images.PixelFormat;

public interface Texture2DMSAA extends Texture {


    int width();
    int height();
    int samples();

    void allocate(int samples, int width, int height, PixelFormat internalFormat);

}
