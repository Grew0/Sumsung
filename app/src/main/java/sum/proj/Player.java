package sum.proj;

import android.graphics.Canvas;
import android.util.Log;

public class Player extends  SpaceShip{
    StickController leftStick = new StickController(), rightStick = new StickController();
    int width, height;

    public Player(){
        loadFromFile("Player");
        setPosition(0, 0, 0);
    }

    void setWH(int w, int h, float scale_parameter){
        width = (int) (w / scale_parameter);
        height = (int) (h / scale_parameter);
        int r = (int) (Math.min(width, height) / 6.0);
        StickController.r = r;
        StickController.updateWH(width, height);
        leftStick.updateXY((int)(-width/2.0f + 1.1f * r), (int)(height/2.0f - 1.1f * r));
        rightStick.updateXY((int)(width/2.0f - 1.1f * r), (int)(height/2.0f - 1.1f * r));
    }

    @Override
    void draw(Canvas canvas, SpaceShip player, float scale_parameter) {

        super.draw(canvas, player, scale_parameter);

        canvas.rotate(-angle, 0, 0);
        canvas.scale(1/scale_parameter, 1/scale_parameter);
        draw_interface(canvas, scale_parameter);
        canvas.scale(scale_parameter, scale_parameter);
        canvas.rotate(angle, 0, 0);
    }


    void draw_interface(Canvas canvas, float scale_parameter){
        int r = StickController.r;
        rightStick.draw(canvas, 0, 0);
        leftStick.draw(canvas, 0, 0);
    }

    void activate_blocks() {
        byte leftDirection = leftStick.getDirection();
        byte rightDirection = rightStick.getDirection();
        if(leftDirection != 0)leftDirection = (byte)((1<<(leftDirection-1)));
        if(rightDirection != 0)rightDirection = (byte)((1<<(rightDirection-1)));
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++){
                if(mat[i][j] != null)
                    mat[i][j].activate((byte) ((rightDirection<<4) + leftDirection-127));
            }
        }
    }

    public void copy(SpaceShip pl) {
        mat = pl.mat;
        mass = pl.mass;
        mass_x = pl.mass_x;
        mass_y = pl.mass_y;
        x = pl.x;
        y = pl.y;
        angle = pl.angle;
        dx = pl.dx;
        dy = pl.dy;
        dan = pl.dan;
        ddx = pl.ddx;
        ddy = pl.ddy;
        ddan = pl.ddan;
        radius = pl.radius;
        onDelete = pl.onDelete;
    }
}