package sum.proj;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class MyButton {
    static Bitmap bpm = null;
    static int size = 16;
    int x, y, realsize;
    byte rotation=0;
    static Paint paint;

    enum variants{edit, delete, plus, arrow, pause, minus};
    variants chosen;


    public MyButton(int x, int y, int realsize, variants chosen) {
        this.x = x;
        this.y = y;
        this.realsize = realsize;
        this.chosen = chosen;
        rotation=0;
    }

    public static void loadPicture(Resources res) {
        Matrix matrix = new Matrix();
        bpm = BitmapFactory.decodeResource(res, R.drawable.buttons);
        float width = bpm.getWidth(), height = bpm.getHeight();
        matrix.setScale(960/width , 160/height);
        bpm = Bitmap.createBitmap(bpm, 0, 0, (int)width, (int)height, matrix, false);
        paint = new Paint();
        paint.setColor(res.getColor(R.color.space_color));
    }

    void draw(Canvas canvas, boolean onTouch, float scaleParameter){
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        if(onTouch)
            paint.setColorFilter(new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN));

        canvas.rotate(rotation*90, (x+realsize/2)*scaleParameter, (y+realsize/2)*scaleParameter);
        RectF dest = new RectF(x*scaleParameter, y*scaleParameter, (x+realsize)*scaleParameter, (y+realsize)*scaleParameter);
        canvas.drawRect(dest, MyButton.paint);
        canvas.drawBitmap(bpm,
                new Rect(chosen.ordinal()*size*10, 0, (chosen.ordinal()*size+size)*10, size*10 ),
                dest,
                paint);
        canvas.rotate(-rotation*90, (x+realsize/2)*scaleParameter, (y+realsize/2)*scaleParameter);
    }

    boolean catchTouch(int X, int Y){
        return x <= X && x + realsize >= X && y <= Y && y + realsize >= Y;
    }
}