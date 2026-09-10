package studio.foldglass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Presentation;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class MainActivity extends Activity implements SensorEventListener,DisplayManager.DisplayListener {
    private SensorManager sensors;
    private DisplayManager displays;
    private Sensor hinge;
    private GlassView mainView;
    private OpeningMotion motion;
    private boolean started,framePending,registered,awaitingVisibleWindow,cover;
    private int lastMainDisplayState=Display.STATE_UNKNOWN;
    private String trigger="inicio",displayError="";
    private final Map<Integer,GlassPresentation> presentations=new HashMap<>();
    private final Choreographer.FrameCallback frame=time->drawFrame();

    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        sensors=(SensorManager)getSystemService(SENSOR_SERVICE);
        displays=(DisplayManager)getSystemService(DISPLAY_SERVICE);
        hinge=sensors.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE);
        long now=SystemClock.uptimeMillis();
        motion=new OpeningMotion(saved==null?180f:saved.getFloat("angle",180f),now);
        motion.surfaceVisible(now);
        cover=getResources().getConfiguration().screenWidthDp<600;
        mainView=new GlassView(this);mainView.setCover(cover);attachGestures(mainView);
        // Seed the shader before attaching the view: no sharp first frame.
        applyFrame(motion.frame(now));
        setContentView(mainView,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
        immersive(getWindow());
        mainView.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            int width=r-l;
            if(width>0 && or-ol>0 && Math.abs(width-(or-ol))>80*getResources().getDisplayMetrics().density){
                boolean nextCover=width/getResources().getDisplayMetrics().density<600;
                if(nextCover!=cover){cover=nextCover;mainView.setCover(cover);reveal("cambio de superficie");}
            }
        });
    }
    static void immersive(Window window){
        if(window==null)return;
        window.setDecorFitsSystemWindows(false);
        WindowManager.LayoutParams params=window.getAttributes();
        params.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        window.setAttributes(params);
        WindowInsetsController controller=window.getInsetsController();
        if(controller!=null){
            controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            controller.hide(WindowInsets.Type.systemBars());
        }
    }
    private void attachGestures(GlassView view){
        view.setOnClickListener(v->reveal("repetición manual"));
        view.setOnLongClickListener(v->{showDiagnostics();return true;});
    }
    @Override protected void onStart(){
        super.onStart();started=true;awaitingVisibleWindow=true;
        displays.registerDisplayListener(this,new Handler(Looper.getMainLooper()));
        if(hinge!=null)registered=sensors.registerListener(this,hinge,SensorManager.SENSOR_DELAY_GAME);
        Display current=getDisplay();lastMainDisplayState=current==null?Display.STATE_UNKNOWN:current.getState();
        reveal("pantalla activada");reconcileDisplays();
    }
    @Override public void onWindowFocusChanged(boolean focused){
        super.onWindowFocusChanged(focused);
        if(focused){
            immersive(getWindow());
            if(started && awaitingVisibleWindow){awaitingVisibleWindow=false;reveal("primera imagen visible");}
        }
    }
    @Override protected void onStop(){
        started=false;sensors.unregisterListener(this);registered=false;
        displays.unregisterDisplayListener(this);
        Choreographer.getInstance().removeFrameCallback(frame);framePending=false;
        for(GlassPresentation p:presentations.values())p.dismiss();presentations.clear();
        super.onStop();
    }
    @Override public void onSaveInstanceState(Bundle out){out.putFloat("angle",motion.rawAngle());super.onSaveInstanceState(out);}
    @Override public void onConfigurationChanged(Configuration next){
        super.onConfigurationChanged(next);
        boolean nextCover=next.screenWidthDp<600;
        if(nextCover!=cover){cover=nextCover;mainView.setCover(cover);reveal("cambio exterior/interior");}
        immersive(getWindow());reconcileDisplays();
    }
    private void reveal(String reason){
        trigger=reason;long now=SystemClock.uptimeMillis();motion.surfaceVisible(now);
        applyFrame(motion.frame(now));wake();
    }
    @Override public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType()==Sensor.TYPE_HINGE_ANGLE && event.values.length>0){motion.hinge(event.values[0],SystemClock.uptimeMillis());wake();}
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
    private void wake(){if(started && !framePending){framePending=true;Choreographer.getInstance().postFrameCallback(frame);}}
    private void drawFrame(){
        framePending=false;if(!started)return;
        OpeningMotion.Frame state=motion.frame(SystemClock.uptimeMillis());applyFrame(state);
        if(state.active)wake();
    }
    private void applyFrame(OpeningMotion.Frame state){
        mainView.setState(state.angle,state.amount);
        for(GlassPresentation p:presentations.values())if(p.view!=null)p.view.setState(state.angle,state.amount);
    }
    private void reconcileDisplays(){
        if(!started)return;
        Display current=getDisplay();Set<Integer> available=new HashSet<>();displayError="";
        for(Display display:displays.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)){
            if(!display.isValid() || display.getState()==Display.STATE_OFF || (current!=null && current.getDisplayId()==display.getDisplayId()))continue;
            int id=display.getDisplayId();available.add(id);
            GlassPresentation previous=presentations.get(id);
            if(previous!=null && previous.isShowing())continue;
            presentations.remove(id);
            GlassPresentation p=new GlassPresentation(display);
            try{p.show();presentations.put(id,p);}
            catch(WindowManager.InvalidDisplayException | SecurityException e){displayError=e.getClass().getSimpleName();}
        }
        for(Integer id:new HashSet<>(presentations.keySet()))if(!available.contains(id))presentations.remove(id).dismiss();
    }
    private void showDiagnostics(){
        long age=motion.lastSensorAt()<0?-1:SystemClock.uptimeMillis()-motion.lastSensorAt();
        String report=String.format(Locale.ROOT,
                "Fold Glass 0.3\nSensor: %s\nRegistrado: %s\nLecturas: %d\nÚltimo ángulo recibido: %s\nEdad de lectura: %d ms\nSuperficie: %s (%d × %d)\nPantallas adicionales: %d\nÚltimo revelado: %s\nError de pantalla: %s\nRenderizador: %s\n\nEl revelado de 1,8 s es una transición visual al aparecer la pantalla. No es una medición de la bisagra.",
                hinge==null?"no disponible":hinge.getName(),registered,motion.samples(),motion.samples()==0?"sin lecturas":String.format(Locale.ROOT,"%.1f°",motion.rawAngle()),age,
                cover?"exterior":"interior",mainView.getWidth(),mainView.getHeight(),presentations.size(),trigger,displayError,mainView.rendererStatus());
        new AlertDialog.Builder(this).setTitle(R.string.diagnostics_title).setMessage(report)
                .setPositiveButton(R.string.replay,(d,w)->reveal("repetición manual"))
                .setNeutralButton(R.string.copy_diagnostics,(d,w)->{
                    ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Fold Glass",report));
                }).setNegativeButton(android.R.string.cancel,null).show();
    }
    @Override public void onDisplayAdded(int id){reconcileDisplays();}
    @Override public void onDisplayRemoved(int id){reconcileDisplays();}
    @Override public void onDisplayChanged(int id){
        Display current=getDisplay();
        if(current!=null && current.getDisplayId()==id){
            int state=current.getState();
            if(state==Display.STATE_ON && lastMainDisplayState!=Display.STATE_ON)reveal("pantalla encendida");
            lastMainDisplayState=state;
        }
        reconcileDisplays();
    }
    private final class GlassPresentation extends Presentation{
        GlassView view;
        GlassPresentation(Display display){super(MainActivity.this,display);}
        @Override protected void onCreate(Bundle saved){
            super.onCreate(saved);requestWindowFeature(Window.FEATURE_NO_TITLE);
            view=new GlassView(getContext());view.setCover(getContext().getResources().getConfiguration().screenWidthDp<600);
            attachGestures(view);
            OpeningMotion.Frame current=motion.frame(SystemClock.uptimeMillis());view.setState(current.angle,current.amount);
            setContentView(view);immersive(getWindow());
        }
        @Override protected void onStart(){
            super.onStart();if(getWindow()!=null)getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
            reveal("pantalla secundaria disponible");
            OpeningMotion.Frame state=motion.frame(SystemClock.uptimeMillis());view.setState(state.angle,state.amount);
        }
    }
}
