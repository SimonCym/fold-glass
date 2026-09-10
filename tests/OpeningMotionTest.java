package studio.foldglass;

public final class OpeningMotionTest {
    private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        OpeningMotion late=new OpeningMotion(180,0);
        late.surfaceVisible(0);late.hinge(180,0);
        check(late.frame(0).amount>.99f,"late 180-degree reading must not remove the first-frame reveal");
        check(late.frame(950).amount>.4f,"reveal must survive the old 950ms timeout");
        check(late.frame(1400).amount>.1f,"reveal continues on the visible inner display");
        check(late.frame(1900).amount==0,"reveal finishes sharp");
        check(!late.frame(2000).active,"no permanent idle render loop");
        late.surfaceVisible(10000);
        check(late.frame(10000).amount>.99f,"resume must restart reveal even without new sensor events");
        OpeningMotion noSensor=new OpeningMotion(180,0);noSensor.surfaceVisible(0);
        check(noSensor.frame(200).amount>.9f,"works without sensor");
        check(noSensor.samples()==0,"replay never invents hinge readings");
        OpeningMotion partial=new OpeningMotion(90,0);
        partial.hinge(90,0);
        check(partial.frame(1200).amount>.99f,"intermediate posture does not disappear after 950ms");
        check(partial.frame(4000).amount==0,"Flex eventually becomes readable");
        OpeningMotion slow=new OpeningMotion(30,0);
        for(int i=0;i<300;i++){
            slow.hinge(30+i*.1f,i*16L);
            check(slow.frame(i*16L).amount>.4f,"slow continuous movement stays visible");
        }
        int samples=slow.samples();slow.hinge(Float.NaN,6000);slow.hinge(Float.POSITIVE_INFINITY,6000);
        check(slow.samples()==samples,"invalid readings ignored");
        for(int fps:new int[]{30,60,120}){
            OpeningMotion motion=new OpeningMotion(180,0);motion.surfaceVisible(0);
            float previous=1;
            for(int i=0;i<=fps*2;i++){
                OpeningMotion.Frame state=motion.frame(Math.round(i*1000.0/fps));
                check(Float.isFinite(state.angle) && Float.isFinite(state.amount),"finite output");
                check(state.amount<=previous+.000001f,"late-display reveal fades continuously");
                previous=state.amount;
            }
        }
        System.out.println("PASS: first frame, late 180-degree sensor, absent sensor, resume, Flex hold, slow motion, invalid values and 30/60/120 Hz.");
    }
}
