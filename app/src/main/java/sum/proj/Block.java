package sum.proj;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class Block {
    PlayerResources res = new PlayerResources().setIron(0);
    Block() {}

    static Bitmap bpm;
    byte rot = 0;
    byte activation_condition=-127; // Это условие активации блока. 8-бит - 8 кнопок (1-нажата, 0-не нажата).
    // Если любой бит activation_condition и этот же бит у функции активации равны 1, то блок считается активированным
    boolean is_activated = false;

    final static int size = 16;
    float mass = 10;
    int max_hp=1000, hp=max_hp;
    int invincible_frames = 3;
    int invic_left = 0;

    static int timer = 0; // Идет от 0 до 599. Используется для анимации.

    static Block parseBlock(int type) {
        switch (type) {
            case 1:
                return new DefenceBlock();
            case 2:
                return new EngineBlock();
            case 3:
                return new GunBlock();
            case 4:
                return new EnergyShieldBlock();
            case 5:
                return new ControllerBlock();
            default:
                return new Block(){ int getType() { return type; } };
        }
    }

    int getType(){
        return 0;
    }

    static void loadPicture(Resources res){
        Matrix matrix = new Matrix();
        bpm = BitmapFactory.decodeResource(res, R.drawable.details);
        float width = bpm.getWidth(), height = bpm.getHeight();
        matrix.setScale(960/width, 1280/height);
        bpm = Bitmap.createBitmap(bpm, 0, 0, (int)width, (int)height, matrix, false);
    }

    void draw(int i, int j, float dx, float dy, Canvas canvas, Paint paint) {
        dx *= size;
        dy *= size;
        dx += i * size;
        dy += j * size;
        canvas.drawRect(dx, dy, dx + size * 0.9f, dy + size * 0.9f, paint);
    }

    void draw_rect_from_bitmap(int i, int j, float dx, float dy, Canvas canvas, Paint paint,
                               int cenx, int ceny, int ltx, int lty, int rbx, int rby) {

        paint.setColorFilter(
                new LightingColorFilter(
                        Color.rgb(255, 150 + hp * 105 /max_hp, 150 + hp * 105 /max_hp),
                        Color.rgb(0, 0, 0)));


        dx = (i - dx) * size;
        dy = (j - dy) * size;
        canvas.rotate(rot*90, dx+size/2, dy+size/2);

        canvas.drawBitmap(
                bpm,
                new Rect(ltx*10+2, lty*10+2, rbx*10-2, rby*10-2),
                new RectF((dx-cenx+ltx), (dy-ceny+lty), (dx-cenx+rbx), (dy-ceny+rby)),
                paint
        );
        canvas.rotate(-rot*90, dx+size/2, dy+size/2);
    }

    void activate(byte buttons){
        is_activated = ((buttons+127) & (activation_condition+127))>0;
    }

    void activate(){ is_activated = true; }

    static void update_all_blocks(){
        timer = (timer+1)%600;
    }

    public boolean getActivate(int i) {
        return (((activation_condition+127)>>i)&1) == 1;
    }

    void update_this_block(){
        if(invic_left > 0)
            invic_left --;
    }

    void getDamage(int damage){
        if(invic_left > 0)return;
        hp -= damage;
        invic_left = invincible_frames;
    }
}

class DefenceBlock extends Block{
    DefenceBlock(){
        mass = 15;
        res.setIron(25);
    }

    @Override
    void draw(int i, int j, float dx, float dy, Canvas canvas, Paint paint) {
        draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 0, 0, 0, 0, 16, 16);
    }

    @Override
    int getType() {
        return 1;
    }
}

class EngineBlock extends Block{
    EngineBlock(){
        mass = 25;
        res.setIron(45);
    }

    @Override
    void draw(int i, int j, float dx, float dy, Canvas canvas, Paint paint) {
        if(is_activated){
            int tm = timer % 30;
            int x = (tm/10)*16;
            draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, x, 32, x, 32, x+16, 64);
        }else{
            draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 16, 0, 16, 0, 32, 32);
        }
    }

    @Override int getType(){ return 2; }
}

class EnergyShieldBlock extends Block{
    EnergyShieldBlock(){
        mass = 25;
        res.setIron(65);
    }

    @Override
    int getType() {
        return 4;
    }

    @Override
    void draw(int i, int j, float dx, float dy, Canvas canvas, Paint paint) {
        if(is_activated){
            int tm = timer % 30;
            if(tm < 10){ draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 16, 112, 0, 96, 48, 128);
            }else if(tm < 20){draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 64, 80, 48, 64, 96, 96);
            }else {draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 16, 80, 0, 64, 48, 96);
            }
        }else{
            draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 32, 16, 32, 0, 48, 32);
        }
    }
}

class GunBlock extends Block{
    int shoutDelay=3;
    int timeDelay=0;
    GunBlock(){
        mass = 25;
        res.setIron(65);
    }

    @Override
    int getType() {
        return 3;
    }

    @Override
    void update_this_block() {
        super.update_this_block();
        if(timeDelay > 0)timeDelay --;
    }

    @Override
    void draw(int i, int j, float dx, float dy, Canvas canvas, Paint paint) {
        if(is_activated){
            int tm = timer % 21;
            int x = (tm/7)*16 + 48;
            draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, x, 48, x, 32, x+16, 64);
        }else{
            draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 48, 16, 48, 0, 64, 32);
        }
    }
}

class ControllerBlock extends Block{
    ControllerBlock(){
        mass = 35;
        res.setIron(100);
    }

    @Override
    int getType() {
        return 5;
    }

    @Override
    void draw(int i, int j, float dx, float dy, Canvas canvas, Paint paint) {
        draw_rect_from_bitmap(i, j, dx, dy, canvas, paint, 64, 0, 64, 0, 80, 16);
    }
}