package sum.proj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Bullet {
    float x, y, dx, dy;
    static Paint paint = null;
    boolean toDelete = false;

    int time_bef_del=10000;

    public Bullet(float x, float y, float dx, float dy) {
        this.x = x+dx;
        this.y = y+dy;
        this.dx = dx;
        this.dy = dy;

        if(paint == null){
            paint = new Paint();
            paint.setColor(Color.parseColor("#00DDFF"));
        }
    }

    void draw(Canvas canvas){
        canvas.drawCircle(x, y, 2, paint);
    }

    public void upd() {
        if(--time_bef_del<1)toDelete = true;
        x += dx;
        y += dy;
    }
}