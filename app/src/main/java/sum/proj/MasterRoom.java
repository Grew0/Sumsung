package sum.proj;

import static sum.proj.R.color.space_color;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class MasterRoom extends View implements View.OnTouchListener {
    float x=0, y=0, dx=0, dy=0;
    MainActivity context;
    MyThread thr;
    float scale_param=0.1f;
    TreeMap<Point, Block> blocks = new TreeMap<>();
    boolean was_move=false; // Показывает был сдвинут палец после нажатия на экран, чтобы различить перемещение по карте и добавления блока
    boolean was_button_clicked=false; // Показывает была ли нажата кнопка
    enum Mode{push, edit, delete};
    Mode mode = Mode.push;
    Block chosenBlock = null;
    byte parse_type = 1;

    ArrayList <MyButton> buttons = new ArrayList<>();

    MasterRoom(MainActivity context){
        super(context);
        this.context = context;
        setOnTouchListener(this);
        blocks = SpaceShip.loadMapFromFile("Player");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Добавление кнопок
        buttons.clear();
        float realsize = w/4.0f;
        for(int i=0;i<4;i++){
            buttons.add(new MyButton((int)(realsize*i), (int)(getHeight()-realsize),
                    (int)realsize, MyButton.variants.values()[new int[]{0, 1, 2, 5}[i]]));
        }
        realsize = w/8.0f;
        for(int i=0;i<8;i++){
            buttons.add(new MyButton((int)(realsize*i), 0,
                    (int)realsize, MyButton.variants.arrow));
            buttons.get(buttons.size()-1).rotation = (byte) ((i+3) % 4);
        }
        for(int i=0;i<4;i++)
            buttons.add(new MyButton((int)(realsize*(i+3)), 0, (int)realsize, MyButton.variants.arrow));
        buttons.get(12).rotation = 3; // ^
        buttons.get(13).rotation = 1; // v
        buttons.get(14).rotation = 2; // <
        buttons.get(15).rotation = 0; // >

    }

    void stop() {
        if(thr != null)thr.stop_this();
    }


    void onChoose(){
        thr = new MyThread();
        thr.start();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                dx = event.getX();
                dy = event.getY();
                was_button_clicked = false;
                for(int i=0;i<buttons.size();i++){
                    if(buttons.get(i).catchTouch((int)dx,(int)dy)) {
                        was_button_clicked = true;
                        switch (i){
                            // Main
                            case 0:
                                if(mode == Mode.push)
                                    mode = Mode.edit;
                                else {
                                    mode = Mode.push;
                                    chosenBlock = Block.parseBlock(parse_type);
                                }
                                break;
                            case 1:
                                if(mode == Mode.delete)
                                    mode = Mode.edit;
                                else
                                    mode = Mode.delete;
                                break;
                            case 2:
                                scale_param *= 0.9;
                                break;
                            case 3:
                                scale_param /= 0.9;
                                break;
                        }
                        if(mode == Mode.push){
                            switch (i){
                                case 12:
                                    chosenBlock.rot = (byte) ((chosenBlock.rot + 3) % 4);
                                    break;
                                case 13:
                                    chosenBlock.rot = (byte) ((chosenBlock.rot + 1) % 4);
                                    break;
                                case 14:
                                    chosenBlock = Block.parseBlock(chosenBlock.getType() - 1);
                                    parse_type = (byte) chosenBlock.getType();
                                    break;
                                case 15:
                                    chosenBlock = Block.parseBlock(chosenBlock.getType() + 1);
                                    parse_type = (byte) chosenBlock.getType();
                                    break;
                            }
                        }
                        if(mode == Mode.edit && i >= 4 && i <= 8+4){ /// Кнопки отвечающие за условие активации
                            int actcon = chosenBlock.activation_condition + 127;
                            actcon ^= (1 << (i-4));
                            chosenBlock.activation_condition = (byte)(actcon-127);
                        }
                    }
                }

                was_move = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float DX=event.getX(), DY=event.getY();
                x += (dx - DX)*scale_param;
                y += (dy - DY)*scale_param;
                dx = DX; dy = DY;
                was_move = true;
                break;
            case MotionEvent.ACTION_UP:
                dx = 0;
                dy = 0;
                if(!was_move && !was_button_clicked){
                    Point pos = new Point(0, 0);
                    pos.x = (int)(x+(event.getX()-getWidth()*0.5)*scale_param);
                    pos.y = (int)(y+(event.getY()-getHeight()*0.5)*scale_param);
                    // поправка формулы для отрицательных координат
                    if(pos.x<0)pos.x-=Block.size;
                    if(pos.y<0)pos.y-=Block.size;
                    pos.x /= Block.size;
                    pos.y /= Block.size;
                    switch (mode) {
                        case push:
                            Block bl = Block.parseBlock(chosenBlock.getType());
                            bl.rot = chosenBlock.rot;
                            blocks.put(pos, bl);
                            break;
                        case delete:
                            blocks.remove(pos);
                            break;
                        case edit:
                            if(blocks.containsKey(pos)){
                                chosenBlock = blocks.get(pos);
                            }
                            break;
                    }
                }

                // Сохранение
                if(!was_move){
                    FileOutputStream outputStream;
                    try {
                        outputStream = context.openFileOutput("Player.txt", Context.MODE_PRIVATE);
                        String string = "";
                        for(Map.Entry<Point, Block> entry: blocks.entrySet()){
                            string += entry.getKey().x + " " + entry.getKey().y + " ";
                            string += entry.getValue().getType() + " " + entry.getValue().rot + " ";
                            string += entry.getValue().activation_condition + "\n";
                        }
                        outputStream.write(string.getBytes());
                        outputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        float w = canvas.getWidth(), h = canvas.getHeight();
        canvas.translate(w/2.0f, h/2.0f);
        canvas.scale(1.0f/scale_param, 1.0f/scale_param);
        w = canvas.getWidth(); h = canvas.getHeight();
        canvas.drawColor(getResources().getColor(space_color));
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        for(float i=-w/2-x%Block.size;i<w/2;i+=Block.size)
            for(float j=-h/2-y%Block.size;j<h/2;j+=Block.size){
                canvas.drawCircle(i, j, 1, paint);
            }
        for(Map.Entry<Point, Block> ent: blocks.entrySet()){
            float DX = ent.getKey().x, DY = ent.getKey().y;
            ent.getValue().draw(ent.getKey().x, ent.getKey().y,
                    x/Block.size, y/Block.size,
                    canvas, new Paint());
        }

        canvas.translate(-getWidth()*scale_param/2, -getHeight()*scale_param/2);
        for(int i=0;i<4;i++){
            buttons.get(i).draw(canvas,
                    (i == 0 && mode == Mode.push) || (i == 1 && mode == Mode.delete),
                    scale_param);
        }
        if(mode == Mode.edit && chosenBlock != null) {
            for (int i = 4; i < 4+8; i++) {
                buttons.get(i).draw(canvas, chosenBlock.getActivate(i-4), scale_param);
            }
        }
        if(mode == Mode.push){
            for (int i = 4+8; i < buttons.size(); i++) {
                buttons.get(i).draw(canvas, false, scale_param);
            }
            int realsize = buttons.get(4+8).realsize;
            canvas.drawRect(0, 0, realsize*3*scale_param, realsize*3*scale_param, MyButton.paint);
            if(chosenBlock == null)chosenBlock = Block.parseBlock(parse_type);
            float time_scl = scale_param/Block.size*realsize;
            canvas.scale(time_scl, time_scl);
            chosenBlock.draw(0, 0, -1, -1, canvas, new Paint());
            canvas.scale(1/time_scl, 1/time_scl);
        }
        canvas.translate(w/2, h/2);
    }

    class MyThread extends Thread{
        boolean isRunning = true;
        @Override
        public void run() {
            super.run();
            while (isRunning){
                invalidate();
            }
        }

        public void stop_this(){
            isRunning = false;
        }
    }

}