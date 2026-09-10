package studio.foldglass;

/** Shared transition: angle drives position; elapsed time only smooths noise / settles Flex. */
final class GlassMath {
    static float clamp(float value, float low, float high) {
        return Math.max(low, Math.min(high, value));
    }
    static float smooth(float low, float high, float value) {
        float t = clamp((value-low)/(high-low), 0f, 1f);
        return t*t*(3f-2f*t);
    }
    static float glass(float angle) {
        float p=clamp(angle/180f,0f,1f);
        return p<=0f || p>=1f ? 0f : (float)Math.sin(Math.PI*p);
    }
    static float follow(float value, float target, float seconds) {
        return value+(target-value)*(1f-(float)Math.exp(-seconds/0.045f));
    }
    private GlassMath() {}
}
