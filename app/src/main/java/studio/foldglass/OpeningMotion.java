package studio.foldglass;

/** Screen-appearance reveal is deliberately separate from physical hinge data. */
final class OpeningMotion {
    static final long REVEAL_MS=1800;
    private float target,shown,movementAnchor;
    private long revealStart=Long.MIN_VALUE,movedAt,lastFrame=-1;
    private int samples;
    private long lastSensorAt=-1;
    OpeningMotion(float initialAngle,long now){
        target=shown=movementAnchor=Float.isFinite(initialAngle)?GlassMath.clamp(initialAngle,0,180):180;
        movedAt=now;
    }
    void surfaceVisible(long now){revealStart=now;lastFrame=-1;}
    void hinge(float angle,long now){
        if(!Float.isFinite(angle))return;
        samples++;lastSensorAt=now;
        float value=GlassMath.clamp(angle,0,180);
        if(Math.abs(value-movementAnchor)>.25f){movedAt=now;movementAnchor=value;}
        target=value;
    }
    Frame frame(long now){
        float dt=lastFrame<0?0f:GlassMath.clamp((now-lastFrame)/1000f,0,.1f);
        lastFrame=now;shown=GlassMath.follow(shown,target,dt);
        if(Math.abs(shown-target)<.01f)shown=target;
        long idle=Math.max(0,now-movedAt);
        float physical=GlassMath.glass(shown)*(1f-GlassMath.smooth(2500,3500,idle));
        float elapsed=revealStart==Long.MIN_VALUE?1f:GlassMath.clamp((now-revealStart)/(float)REVEAL_MS,0,1);
        float reveal=1f-GlassMath.smooth(0,1,elapsed);
        float revealAngle=55f+125f*GlassMath.smooth(0,1,elapsed);
        float blend=reveal/(reveal+physical+.000001f);
        float visualAngle=shown+(revealAngle-shown)*blend;
        float amount=Math.max(physical,reveal);
        boolean active=elapsed<1 || Math.abs(shown-target)>.01f || (GlassMath.glass(shown)>.0001f && idle<3500);
        return new Frame(visualAngle,amount,active,reveal>0);
    }
    float rawAngle(){return target;}
    int samples(){return samples;}
    long lastSensorAt(){return lastSensorAt;}
    static final class Frame {
        final float angle,amount;
        final boolean active,revealing;
        Frame(float angle,float amount,boolean active,boolean revealing){this.angle=angle;this.amount=amount;this.active=active;this.revealing=revealing;}
    }
}
