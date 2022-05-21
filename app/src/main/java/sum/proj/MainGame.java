package sum.proj;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;

public class MainGame extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {
    ArrayList<SpaceShip> ships = new ArrayList<>();
    Player player;

    MainThread thr;
    MainActivity act;
    ArrayList<MyButton> buttons = new ArrayList<>();
    ArrayList <Bullet> bullets = new ArrayList<>();

    int width, height;
    float scale_parameter = 1f;
    float nscl = 1f; // Новый scale_parameter. еобходим для синхронизации потоков

    public MainGame(Context context, MainActivity Act) {
        super(context);
        getHolder().addCallback(this);
        act = Act;
        this.setOnTouchListener(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w; height = h;
        if(player == null)player = new Player();
        player.setWH(w, h, scale_parameter);

        // Добавление кнопок
        buttons.clear();
        float realsize = w/8.0f;
        for(int i=0;i<2;i++){
            buttons.add(new MyButton((int)(width - realsize*(i+1)), 0,
                    (int)realsize, MyButton.variants.values()[new int[]{5, 2}[i]]));
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if(player == null)player = new Player();
        player.setPosition(0, 150, 0);
        ships.add(player);

        ships.add(new SpaceShip(){
            @Override
            void upd(MainGame mainGame, Canvas canvas) {
                super.upd(mainGame, canvas);
                //if(this.mat != null) this.mat[1][0].activate((byte) (255-127));
            }
        });
        ships.get(1).setPosition(0, -150, 0);
        //ships.get(1).loadFromFile("Player");

        ships.add(new SpaceShip());
        ships.get(1).setPosition(150, -150, 0);

        thr = new MainThread(getHolder());
        thr.start();
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thr.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                thr.join();
                retry = false;
            } catch (InterruptedException e) { }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        player.leftStick.onTouch(event);
        player.rightStick.onTouch(event);
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN ||
                event.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN){
            int ind = event.getActionIndex();
            for (int i = 0; i < buttons.size(); i++) {
                if (buttons.get(i).catchTouch((int) event.getX(ind), (int) event.getY(ind))) {
                    switch (i) {
                        case 0:
                            nscl *= 0.9;
                            break;
                        case 1:
                            nscl /= 0.9;
                            break;
                    }
                }
            }
        }


        return true;
    }

    public void stop() {
        thr.requestStop();
    }


    public class MainThread extends Thread {

        private SurfaceHolder surfaceHolder;

        private volatile boolean running = true;//флаг для остановки потока

        public MainThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        public void requestStop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    try{
                        scale_parameter = nscl;
                        canvas.drawColor(getResources().getColor(R.color.space_color));
                        canvas.translate(width>>1, height>>1);
                        canvas.rotate(player.angle);
                        canvas.scale(scale_parameter, scale_parameter);
                        //
                        // Отрисовка------------------------------------------------------------------------
                        //

                        // Цикл по кораблям
                        for(SpaceShip ship: ships) {
                            ship.draw(canvas, player, scale_parameter);
                        }

                        // Отрисовка пуль
                        canvas.translate(-player.x, -player.y);
                        for(Bullet bullet: bullets){ bullet.draw(canvas); }
                        canvas.translate(player.x, player.y);

                        canvas.rotate(-player.angle);

                        canvas.translate((-width>>1)/scale_parameter, (-height>>1)/scale_parameter);
                        // Отрисовка кнопок
                        for(MyButton btn: buttons)
                            btn.draw(canvas, false, 1/scale_parameter);

                        /// todo del
                        /*for(int i=0;i<ships.size();i++) {
                            Paint paint = new Paint();
                            paint.setColor(Color.WHITE);
                            paint.setTextSize(40);
                            canvas.drawText(ships.get(i).toString(), 0, 40+i*50, paint);
                        }*/
                        /// end todo

                        canvas.translate((width>>1)/scale_parameter, (height>>1)/scale_parameter);
                        canvas.rotate(player.angle);




                        //
                        // Обновления------------------------------------------------------------------------
                        //
                        player.activate_blocks();
                        // Обновление timer у блока для анимации
                        Block.update_all_blocks();
                        // Обновление кораблей
                        for(int i=ships.size()-1;i>=0;i--) {

                            SpaceShip ship = ships.get(i);
                            //try{
                            ship.upd(MainGame.this, canvas);
                            //}catch (Exception e){e.printStackTrace();}

                            for(Bullet bullet: bullets){
                                ship.xBullet(bullet, player, canvas);
                            }
                            for(int j=0;j<i;j++){
                                ship.xShip(ships.get(j), canvas, player);
                            }
                            if(ship.onDelete){
                                ships.set(i, ships.get(ships.size()-1));
                                ships.remove(ships.size()-1);
                            }
                        }

                        // Обновление пуль
                        for(Bullet bullet: bullets){ bullet.upd(); }
                        for(int i=bullets.size()-1;i>=0;i--){
                            if(bullets.get(i).toDelete){
                                bullets.set(i, bullets.get(bullets.size()-1));
                                bullets.remove(bullets.get(bullets.size()-1));
                            }
                        }
                    }finally{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}