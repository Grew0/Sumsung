package sum.proj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;

public class StickController {
    static int r;
    int x, y;
    int id = -1;
    float dx = 0, dy = 0;
    static Paint paint = new Paint();
    static int width=0, height=0;

    public StickController(){}
    public StickController(int X, int Y){
        x=X; y=Y;
    }

    void updateXY(int X, int Y){
        x=X; y=Y;
    }

    static void updateWH(int W, int H){
        width = W; height = H;
    }

    void draw(Canvas canvas, float X, float Y){
        X += x;
        Y += y;
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(X, Y, r, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(X + dx, Y + dy, r*0.1f, paint);
    }

    void onTouch(MotionEvent event){
        int nwid = event.getPointerId(event.getActionIndex());
        int X= (int) (x+width/2-event.getX(event.getActionIndex())), Y= (int) (y+height/2-event.getY(event.getActionIndex()));
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN)
            if(X*X + Y*Y < r*r){
                id = nwid;
            }

        if(id > -1){
            int i = 0;
            while (event.getPointerId(i) != id)i++;
            X= (int) (x+width/2-event.getX(i));
            Y= (int) (y+height/2-event.getY(i));
            dx = -X;
            dy = -Y;
        }
        if(nwid == id){
            if(event.getActionMasked() == MotionEvent.ACTION_UP ||
                    event.getActionMasked() == MotionEvent.ACTION_POINTER_UP){
                id = -1;
                dx = 0;
                dy = 0;
            }
        }
        if(dx*dx + dy*dy > r*r){
            float rad = r/(float) Math.sqrt(dx*dx + dy*dy);
            dx *= rad;
            dy *= rad;
        }
    }

    byte getDirection(){ // 0 - (+) 1 - (^) 2 - (>) 3 - (v) 4 - (<)
        if(dx*dx + dy*dy < r*r*0.25){return  0;}
        if(dx + dy > 0){
            if(dy - dx > 0)return 3;
            return 2;
        }else{
            if(dy - dx > 0)return 4;
            return 1;
        }
    }
}