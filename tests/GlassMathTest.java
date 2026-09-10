package studio.foldglass;

public final class GlassMathTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        check(Math.abs(GlassMath.glass(0))<.0001,"closed sharp");
        check(Math.abs(GlassMath.glass(180))<.0001,"open sharp");
        check(GlassMath.glass(.1f)>0f,"responds in first fraction of a degree");
        check(GlassMath.glass(179.9f)>0f,"responds in last fraction of a degree");
        float previous=GlassMath.glass(0f);
        for(int tick=1;tick<=1800;tick++){
            float value=GlassMath.glass(tick/10f);
            check(Math.abs(value-previous)<.0018f,"continuous across every 0.1 degree");
            previous=value;
        }
        for(int a=-20;a<=200;a++){
            float effect=GlassMath.glass(a);
            check(Float.isFinite(effect) && effect>=-.0001f && effect<=1.0001f,"bounded effect");
        }
        float reference=0;
        for(int fps:new int[]{30,60,120}){
            float angle=0;
            for(int i=0;i<fps;i++)angle=GlassMath.follow(angle,180,1f/fps);
            check(Math.abs(angle-180)<.001,"converges at "+fps);
            if(reference!=0)check(Math.abs(angle-reference)<.001,"rate independent");
            reference=angle;
        }
        check(GlassMath.follow(120,30,.016f)<120,"reversal follows target");
        check(GlassMath.smooth(650,950,650)==0,"Flex hold");
        check(GlassMath.smooth(650,950,950)==1,"Flex clears");
        System.out.println("PASS: effect bounds, sharp endpoints, reversal, smoothing at 30/60/120 Hz, Flex settling.");
    }
}
