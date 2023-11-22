package radon.engine.tiles.autotiling;

import radon.engine.graphics.opengl.textures.GLTexture2D;
import radon.engine.sprites.Sprite;
import radon.engine.util.geometry.Rect;

import java.util.HashMap;
import java.util.Map;

public class AutoTillingGenerator {
    private static final Map<Integer, Integer> INDICES = new HashMap<>();

    static {

        final String indexSrc = "0 = 34, 1 = 42, 2 = 26, 3 = 13, 4 = 33, 5 = 41, 6 = 43, 7 = 36, 8 = 30, 9 = 28, 10 = 14, 11 = 44, 12 = 45, 13 = 40, 14 = 7, 15 = 12, 16 = 4, 17 = 1, 18 = 23, 19 = 24, 20 = 31, 21 = 5, 22 = 8, 23 = 2, 24 = 0, 25 = 47, 26 = 38, 27 = 25, 28 = 20, 29 = 17, 30 = 29, 31 = 37, 32 = 18, 33 = 21, 34 = 15, 35 = 35, 36 = 27, 37 = 10, 38 = 11, 39 = 19, 40 = 22, 41 = 16, 42 = 46, 43 = 48, 44 = 39, 45 = 32, 46 = 6, 47 = 9, 48 = 3";
        String[] split =indexSrc.split(",", -1);
        for (String value : split) {
            String[] keyValue = value.split("=",2);

            Integer s = Integer.parseInt(keyValue[0].trim());
            Integer e = Integer.parseInt(keyValue[1].trim());

            INDICES.put(s, e);
        }
    }

    public static Sprite[] generateSpriteSheet(GLTexture2D texture, int spriteWidth, int spriteHeight) {

        int row = texture.height() / spriteHeight;
        int col = texture.width() / spriteWidth;
        int numSprites = row * col;

        Sprite[] sprites = new Sprite[numSprites];

        for (int j = 0; j < row; j++) {
            for (int i = 0; i < col; i++) {

                int leftX = i * spriteWidth;
                int bottomY = j * spriteHeight;
                int rightX = leftX + spriteWidth;
                int topY = bottomY + spriteHeight;

                Rect bounds = new Rect(leftX, rightX, bottomY, topY);
                Sprite sprite = new Sprite(texture, bounds);


                int texId = i + j * row;

                int id = INDICES.getOrDefault(texId, 0);
                sprites[id] = sprite;
            }
        }

        /*
        int currentX = 0;
        int currentY = texture.height() - spriteHeight;
        for (int i = 0; i < numSprites; i++) {
            int topY = currentY + spriteHeight;
            int rightX = currentX + spriteWidth;
            int leftX = currentX;
            int bottomY = currentY;

            Rect bounds = new Rect(leftX, rightX, bottomY, topY);
            Sprite sprite = new Sprite(texture, bounds);

            System.out.print(i + " -> ");

            int id = INDICES.get(i);
            sprites[id] = sprite;
            System.out.println(id);

            currentX += spriteWidth;
            if (currentX >= texture.width()) {
                currentX = 0;
                currentY -= spriteHeight;
            }
        }*/

        return sprites;
    }
}
