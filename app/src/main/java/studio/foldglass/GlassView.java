package studio.foldglass;

import android.content.Context;
import android.graphics.*;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/** Renders only our own demo scene. Never captures another app or changes SystemUI. */
final class GlassView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RuntimeShader shader;
    private final String shaderError;
    private final GestureDetector gestures;
    private final Rect fallbackSource=new Rect(0,0,1000,1000);
    private final Rect fallbackDestination=new Rect();
    private boolean replayGesture;
    private Bitmap scene;
    private float angle = 180f, amount = 0f;
    private boolean cover;

    private static final String SHADER = """
        uniform shader content;
        uniform float2 resolution;
        uniform float amount;
        uniform float progress;
        uniform float cover;
        half4 main(float2 xy) {
            float2 uv = xy / resolution;
            float2 st = float2(mix(uv.x, .5 + uv.x * .5, cover), uv.y);
            float zoom = 1.0 + .05 * amount * (1.0 - progress);
            st = (st - float2(.75, .5)) / zoom + float2(.75, .5);
            float bend = cos(progress * 3.14159265);
            float wave = sin(uv.y * 5.0 + progress * 6.2831853);
            st.x += (bend * .03 + wave * .008) * amount;
            st.y += (uv.x - .5) * bend * .022 * amount;
            float2 p = clamp(st, float2(.001), float2(.999)) * float2(1000., 1000.);
            float clarity = smoothstep(20.0/180.0, 145.0/180.0, progress);
            float r = 24.0 * amount * (.35 + .65 * (1.0 - clarity));
            half4 c = content.eval(p) * .20;
            c += content.eval(p + float2(r, 0.)) * .12;
            c += content.eval(p - float2(r, 0.)) * .12;
            c += content.eval(p + float2(0., r)) * .12;
            c += content.eval(p - float2(0., r)) * .12;
            c += content.eval(p + float2(r, r)) * .08;
            c += content.eval(p - float2(r, r)) * .08;
            c += content.eval(p + float2(r, -r)) * .08;
            c += content.eval(p + float2(-r, r)) * .08;
            float globalX = mix(uv.x, .5 + uv.x * .5, cover);
            float sweep = -.2 + progress * 1.4;
            float shine = exp(-pow((globalX + (uv.y-.5)*.12 - sweep)/.085, 2.0));
            float echo = exp(-pow((globalX + (uv.y-.5)*.12 - sweep+.24)/.03, 2.0));
            c.rgb = mix(c.rgb, half3(.77, .94, 1.), half(amount * (.13 + shine*.30 + echo*.08)));
            return half4(c.rgb, 1.);
        }
        """;

    GlassView(Context context) {
        super(context);
        setContentDescription("Fold Glass. Doble toque para repetir el revelado. Mantén pulsado para ver el diagnóstico.");
        RuntimeShader compiled=null;String failure="";
        try{compiled=new RuntimeShader(SHADER);}catch(IllegalArgumentException e){failure=e.getMessage();}
        shader=compiled;shaderError=failure;
        gestures=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
            @Override public boolean onDown(MotionEvent e){return true;}
            @Override public boolean onDoubleTap(MotionEvent e){replayGesture=true;return true;}
            @Override public void onLongPress(MotionEvent e){performLongClick();}
        });
        createScene();
    }

    void setState(float angle, float amount) {
        this.angle = angle;
        this.amount = amount;
        if(shader==null)setRenderEffect(amount>.001f?RenderEffect.createBlurEffect(14f*amount,14f*amount,Shader.TileMode.CLAMP):null);
        invalidate();
    }
    void setCover(boolean value) { cover = value;fallbackSource.left=value?500:0;invalidate(); }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){super.onSizeChanged(w,h,oldw,oldh);fallbackDestination.set(0,0,w,h);}

    private void createScene() {
        scene = Bitmap.createBitmap(1000,1000,Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(scene);
        paint.setShader(new LinearGradient(0,0,1000,1000,
                new int[]{Color.rgb(15,40,92),Color.rgb(28,108,166),Color.rgb(20,204,194)},null,Shader.TileMode.CLAMP));
        c.drawRect(0,0,1000,1000,paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(100);
        paint.setColor(0x386BE5FF);
        c.drawOval(160,-240,730,1330,paint);
        paint.setStrokeWidth(3);
        paint.setColor(0x559CEDFF);
        c.drawOval(110,-240,780,1330,paint);
        paint.setStyle(Paint.Style.FILL);
        text(c,"FOLD GLASS",55,80,25);
        text(c,"09:41",552,210,98);
        text(c,"Jueves, 10 de septiembre",555,265,24);
        paint.setColor(0x28FFFFFF);
        c.drawRoundRect(50,160,445,390,36,36,paint);
        text(c,"24°",80,285,90);
        text(c,"Un día despejado",80,345,26);
        String[] labels={"Teléfono","Mensajes","Cámara","Galería","Agenda","Ajustes"};
        String[] symbols={"T","M","C","G","10","A"};
        for(int pane=0;pane<2;pane++) for(int i=0;i<6;i++) {
            float x=pane*500+95+(i%3)*150, y=590+(i/3)*180;
            paint.setColor(0x44FFFFFF);
            c.drawRoundRect(x-42,y-45,x+42,y+39,24,24,paint);
            paint.setTextAlign(Paint.Align.CENTER);
            text(c,symbols[i],x,y+11,33);
            text(c,labels[i],x,y+79,22);
            paint.setTextAlign(Paint.Align.LEFT);
        }
        paint.setColor(0xCCFFFFFF);
        c.drawRoundRect(430,970,570,977,4,4,paint);
        if(shader!=null)shader.setInputShader("content",new BitmapShader(scene,Shader.TileMode.CLAMP,Shader.TileMode.CLAMP));
    }
    private void text(Canvas c,String value,float x,float y,float size) {
        paint.setShader(null);paint.setColor(Color.WHITE);paint.setTextSize(size);
        paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));
        c.drawText(value,x,y,paint);
    }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(getWidth()==0 || getHeight()==0)return;
        if(shader==null){
            canvas.drawBitmap(scene,fallbackSource,fallbackDestination,paint);
            return;
        }
        shader.setFloatUniform("resolution",getWidth(),getHeight());
        shader.setFloatUniform("amount",amount);
        shader.setFloatUniform("progress",angle/180f);
        shader.setFloatUniform("cover",cover?1f:0f);
        paint.setShader(shader);
        canvas.drawRect(0,0,getWidth(),getHeight(),paint);
        paint.setShader(null);
    }
    @Override public boolean onTouchEvent(MotionEvent event){
        boolean handled=gestures.onTouchEvent(event);
        if(replayGesture){replayGesture=false;performClick();}
        return handled || super.onTouchEvent(event);
    }
    @Override public boolean performClick(){super.performClick();return true;}
    String rendererStatus(){return shader==null?"Alternativo (RenderEffect): "+shaderError:"AGSL";}
}
