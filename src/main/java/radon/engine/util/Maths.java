package radon.engine.util;

import org.joml.Quaternionf;
import org.joml.Random;
import org.joml.Vector3f;
import radon.engine.core.Time;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class Maths {

    private static final Random RANDOM = new Random(System.nanoTime());

    public static int clamp(int min, int max, int value) {
        return max(min, Math.min(max, value));
    }

    public static float clamp(float min, float max, float value) {
        return max(min, Math.min(max, value));
    }

    public static float radians(float angle) {
        return (float) Math.toRadians(angle);
    }

    public static float degrees(float radians) {
        return (float) Math.toDegrees(radians);
    }

    public static float sin(float radians) {
        return (float) Math.sin(radians);
    }

    public static float cos(float radians) {
        return (float) Math.cos(radians);
    }

    public static float tan(float radians) {
        return (float) Math.tan(radians);
    }

    public static float asin(float radians) {
        return (float) Math.asin(radians);
    }

    public static float acos(float radians) {
        return (float) Math.acos(radians);
    }

    public static float randomFloat() {
        return RANDOM.nextFloat();
    }

    public static int randomInt(int limit) {
        return RANDOM.nextInt(limit);
    }

    public static int roundUp(int number, int multiple) {
        Asserts.assertTrue(number >= 0);
        Asserts.assertTrue(multiple > 0);
        return ((number + multiple - 1) / multiple) * multiple;
    }

    public static int roundUp2(int number, int multiple) {
        Asserts.assertTrue(number > 0);
        Asserts.assertTrue(multiple > 0 && multiple % 2 == 0);
        return (number + multiple - 1) & (-multiple);
    }

    public static int log2(int n) {
        Asserts.assertTrue(n > 0);
        return Integer.SIZE - 1 - Integer.numberOfLeadingZeros(n);
    }

    public static float lerp(float a, float b, float t) {
        return (1 - t) * a + t * b;
    }

    public static Vector3f lerp(Vector3f start, Vector3f end, float t) {
        final float x = start.x + (end.x - start.x) * t;
        final float y = start.y + (end.y - start.y) * t;
        final float z = start.z + (end.z - start.z) * t;
        return new Vector3f(x, y, z);
    }

    public static Quaternionf lerp(Quaternionf a, Quaternionf b, float t) {

        Quaternionf result = new Quaternionf();

        final float dot = a.w * b.w + a.x * b.x + a.y * b.y + a.z * b.z;

        final float blendI = 1.0f - t;

        if (dot < 0) {
            result.w = blendI * a.w + t * -b.w;
            result.x = blendI * a.x + t * -b.x;
            result.y = blendI * a.y + t * -b.y;
            result.z = blendI * a.z + t * -b.z;
        } else {
            result.w = blendI * a.w + t * b.w;
            result.x = blendI * a.x + t * b.x;
            result.y = blendI * a.y + t * b.y;
            result.z = blendI * a.z + t * b.z;
        }

        return result.normalize();
    }

    public static Vector3f smoothDamp(Vector3f current, Vector3f target, Vector3f currentVelocity, float smoothTime, float maxSpeed) {
        float deltaTime = Time.deltaTime();
        return smoothDamp(current, target, currentVelocity, smoothTime, maxSpeed, deltaTime);
    }

    public static Vector3f smoothDamp(Vector3f current, Vector3f target, Vector3f currentVelocity, float smoothTime) {
        float deltaTime = Time.deltaTime();
        float maxSpeed = Float.MAX_VALUE;
        return smoothDamp(current, target, currentVelocity, smoothTime, maxSpeed, deltaTime);
    }

    public static Vector3f smoothDamp(Vector3f current, Vector3f target, Vector3f currentVelocity, float smoothTime, float maxSpeed, float deltaTime) {
        float outX = 0f;
        float outY = 0f;
        float outZ = 0f;

        smoothTime = max(0.0001f, smoothTime);
        float omega = 2.0f / smoothTime;

        float x = omega * deltaTime;
        float exp = 1.0f / (1.0f + x + 0.48f * x * x + 0.235f * x * x * x);

        float dx = current.x - target.x;
        float dy = current.y - target.y;
        float dz = current.z - target.z;
        Vector3f originalTo = target;

        float maxChange = maxSpeed * smoothTime;

        float maxChangeSq = maxChange * maxChange;
        float sqrmag = dx * dx + dy * dy + dz * dz;
        if (sqrmag > maxChangeSq) {
            var mag = (float) sqrt(sqrmag);
            dx = dx / mag * maxChange;
            dy = dy / mag * maxChange;
            dz = dz / mag * maxChange;
        }

        target.x = current.x - dx;
        target.y = current.y - dy;
        target.z = current.z - dz;

        float tmpX = (currentVelocity.x + omega * dx) * deltaTime;
        float tmpY = (currentVelocity.y + omega * dy) * deltaTime;
        float tmpZ = (currentVelocity.z + omega * dz) * deltaTime;

        currentVelocity.x = (currentVelocity.x - omega * tmpX) * exp;
        currentVelocity.y = (currentVelocity.y - omega * tmpY) * exp;
        currentVelocity.z = (currentVelocity.z - omega * tmpZ) * exp;

        outX = target.x + (dx + tmpX) * exp;
        outY = target.y + (dy + tmpY) * exp;
        outZ = target.z + (dz + tmpZ) * exp;

        float origDx = originalTo.x - current.x;
        float origDy = originalTo.y - current.y;
        float origDz = originalTo.z - current.z;
        float outOrigX = outX - originalTo.x;
        float outOrigY = outY - originalTo.y;
        float outOrigZ = outZ - originalTo.z;

        if (origDx * outOrigX + origDy * outOrigY + origDz * outOrigZ > 0) {
            outX = originalTo.x;
            outY = originalTo.y;
            outZ = originalTo.z;

            currentVelocity.x = (outX - originalTo.x) / deltaTime;
            currentVelocity.y = (outY - originalTo.y) / deltaTime;
            currentVelocity.z = (outZ - originalTo.z) / deltaTime;
        }

        return new Vector3f(outX, outY, outZ);
    }
}
