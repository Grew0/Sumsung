package sum.proj;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;


import java.io.File;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.security.auth.callback.Callback;

public class SpaceShip {
    char type = 'S';
    static MainActivity mainActivity;

    Block [][] mat = null;
    float x=10, y=10, angle=0, dx=0, dy=0, dan=0;
    float ddx=0, ddy=0, ddan=0;
    float mass_x=0, mass_y=0;
    float mass;

    static float friction = 0.01f;

    float radius=0;

    boolean onDelete=false;

    /*TODO: delete*/ int color_of_radius = Color.WHITE;


    SpaceShip(){
        int a[][] = new int[4][2];
        for(int i=0;i<a.length;i++)
            for(int j=0;j<a[i].length;j++)
                a[i][j] = (int)(Math.random()*5+1);
        load_matrix_from_int(a);
    }

    void load_matrix_from_int(int[][] MAT){
        mat = new Block[MAT.length][MAT[0].length];
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++) {
                if(MAT[i][j] == 0)
                    mat[i][j] = null;
                else
                    mat[i][j] = Block.parseBlock(MAT[i][j]);
            }
        }
        massPoint();
    }

    void massPoint(){
        mass = 0;
        mass_x = 0;
        mass_y = 0;
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++) {
                if(mat[i][j] != null){
                    mass_x += (i+0.5) * mat[i][j].mass;
                    mass_y += (j+0.5) * mat[i][j].mass;
                    mass += mat[i][j].mass;
                }
            }
        }
        mass_x /= mass;
        mass_y /= mass;

        radius = (float) Math.hypot(mass_x, mass_y);
        try{
            radius = (float) Math.max(radius, Math.hypot(mat.length-mass_x, mass_y));
            radius = (float) Math.max(radius, Math.hypot(mass_x, mat[0].length-mass_y));
            radius = (float) Math.max(radius, Math.hypot(mat.length-mass_x, mat[0].length-mass_y));
        }catch (Exception e){}
        radius *= Block.size;
    }

    void draw(Canvas canvas, SpaceShip player, float scale_parameter){
        int shift_x = (int)(x-player.x), shift_y = (int)(y-player.y);
        canvas.translate(shift_x, shift_y);

        Paint p = new Paint();
        canvas.rotate(-angle);

        p.setColor(Color.rgb(100, 100, 100));
        Paint cirpaint = new Paint();

        cirpaint.setColor(color_of_radius);
        canvas.drawCircle(mass_x, mass_y, radius, cirpaint);
        color_of_radius = Color.argb(50, 255, 255, 255);

        cirpaint.setColor(Color.argb(50, 0, 255, 0));
        for(int i=0;i<mat.length;i++)
            for(int j=0;j<mat[i].length;j++){
                if(mat[i][j] != null){
                    mat[i][j].draw(i, j, mass_x, mass_y, canvas, p);
                }
            }
        for(int i=0;i<mat.length;i++)
            for(int j=0;j<mat[i].length;j++){
                if(mat[i][j] != null){
                    //canvas.drawCircle((i+0.5f-mass_x)*Block.size, (j+0.5f-mass_y)*Block.size, Block.size/2, cirpaint);
                }
            }

        p.setColor(Color.WHITE);
        //canvas.drawCircle(mass_x, mass_y, 1, p);

        canvas.rotate(angle);
        canvas.translate(-shift_x, -shift_y);
    }

    void upd(MainGame mainGame, Canvas canvas){
        boolean onPossibleDelete = true;
        boolean need_to_crack = false;
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++) {
                if (mat[i][j] != null){
                    onPossibleDelete = false;
                    if (mat[i][j].is_activated) {
                        byte rot = mat[i][j].rot;
                        switch (mat[i][j].getType()) {
                            case 2: { // Engine
                                float force = 5f;

                                float force_x = (float) Math.cos((rot + 3) * 3.1415 / 2 - angle * 3.1415 / 180) * force;
                                float force_y = (float) Math.sin((rot + 3) * 3.1415 / 2 - angle * 3.1415 / 180) * force;

                                float x = (i + 0.5f - mass_x) * Block.size, y = (j + 0.5f - mass_y) * Block.size;
                                float cosa = (float) Math.cos(-angle * 3.14 / 180), sina = (float) Math.sin(-angle * 3.14 / 180);
                                float xn = x * cosa - y * sina, yn = y * cosa + x * sina;
                                apply_force(xn, yn, force_x, force_y, canvas);

                            }break;
                            case 3: { // GunBlock
                                if(((GunBlock)mat[i][j]).timeDelay>0)break;
                                float speed = 10f;
                                float _x = (float) Math.cos((rot+3)*1.57-angle * 3.1415 / 180);
                                float _y = (float) Math.sin((rot+3)*1.57-angle * 3.1415 / 180);
                                float x = (i + 0.5f - mass_x) * Block.size, y = (j + 0.5f - mass_y) * Block.size;
                                float cosa = (float) Math.cos(-angle * 3.14 / 180), sina = (float) Math.sin(-angle * 3.14 / 180);
                                float xn = x * cosa - y * sina, yn = y * cosa + x * sina;
                                xn += this.x;
                                yn += this.y;
                                ((GunBlock)mat[i][j]).timeDelay = ((GunBlock)mat[i][j]).shoutDelay;
                                mainGame.bullets.add(new Bullet(
                                        xn+Block.size*0.45f*_x+Block.size*0.3f*_y, yn+Block.size*0.45f*_y-Block.size*0.3f*_x, speed*_x, speed*_y));
                                mainGame.bullets.add(new Bullet(
                                        xn+Block.size*0.45f*_x-Block.size*0.3f*_y, yn+Block.size*0.45f*_y+Block.size*0.3f*_x, speed*_x, speed*_y));
                            }break;
                        }

                    }
                    mat[i][j].update_this_block();
                    if(mat[i][j].hp <= 0){
                        mat[i][j] = null;
                        need_to_crack = true;
                        //crack(mainGame);
                    }
                }
            }
        }

        dx += ddx;
        dy += ddy;
        x += dx;
        y += dy;
        dan += ddan;
        angle += dan;


        while (angle > 360) angle -= 360;
        while (angle < -360) angle += 360;

        if(dx > friction){ dx -= friction;
        }else if(dx < -friction){dx += friction;
        }else dx = 0;

        if(dy > friction){ dy -= friction;
        }else if(dy < -friction){dy += friction;
        }else dy = 0;

        if(dan > friction){ dan -= friction;
        }else if(dan < -friction){dan += friction;
        }else dan = 0;
        ddx = 0;ddy = 0;ddan = 0;

        // Tearing apart
        if(need_to_crack){
            crack(mainGame);
        }
        onDelete |= onPossibleDelete;
    }


    void setPosition(float nx, float ny, float na){
        x = nx; y = ny; angle = na;
    }

    void apply_force(float X, float Y, float force_x, float force_y, Canvas canvas) {
        ddx += force_x / mass; // F = m*a  |=>  a = F/m
        ddy += force_y / mass;
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        //canvas.drawLine(0, 0, X, Y, paint);
        paint.setStrokeWidth(3.5F);
        paint.setColor(Color.WHITE);
        //canvas.drawLine(X, Y, X+force_x*100, Y+force_y*100, paint);

        float moment = (force_x*Y) - (force_y*X);
        ddan += 0.03*moment / mass;

    }


    void parse_from_blockTree(Map<Point, Block> blMap){
        int xmin=Integer.MAX_VALUE, ymin=xmin, xmax=Integer.MIN_VALUE, ymax=xmax;
        for(Point point: blMap.keySet()){
            xmin = Math.min(xmin, point.x);
            ymin = Math.min(ymin, point.y);
            xmax = Math.max(xmax, point.x);
            ymax = Math.max(ymax, point.y);
        }

        mat = new Block[xmax-xmin+1][ymax-ymin+1];
        for(Map.Entry<Point, Block> ent: blMap.entrySet()){
            mat[ent.getKey().x-xmin][ent.getKey().y-ymin] = ent.getValue();
        }
        massPoint();
    }

    void loadFromFile(String name){
        parse_from_blockTree(SpaceShip.loadMapFromFile(name));
    }

    static TreeMap<Point, Block> loadMapFromFile(String name){
        File file = new File(mainActivity.getFilesDir(), name + ".txt");
        TreeMap<Point, Block> blMap = new TreeMap<>();
        try {
            Scanner in = new Scanner(file);
            /// На каждой строке - x, y, Block - (type, rotation, activation condition)
            while (in.hasNext()) {
                Point point = new Point(in.nextInt(), in.nextInt());
                Block block = Block.parseBlock(in.nextInt());
                block.rot = in.nextByte();
                block.activation_condition = in.nextByte();
                blMap.put(point, block);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return blMap;
    }

    public void xBullet(Bullet bullet,/*todo delete*/ Player player, Canvas canvas) {
        if(Round.Round_x_Round(bullet.x, bullet.y, 2, x, y, radius)){
            color_of_radius = Color.argb(150, 255, 0, 0);
            for(int i=0;i<mat.length;i++){
                for(int j=0;j<mat[i].length;j++){
                    if(mat[i][j] != null){
                        float xn, yn;
                        {
                            float x = (i + 0.5f - mass_x) * Block.size, y = (j + 0.5f - mass_y) * Block.size;
                            float cosa = (float) Math.cos(-angle * 3.14 / 180), sina = (float) Math.sin(-angle * 3.14 / 180);
                            xn = x * cosa - y * sina;
                            yn = y * cosa + x * sina;
                        }
                        float   dx= x+xn,
                                dy= y+yn;
                        if(Round.Round_x_Round(bullet.x, bullet.y, 2, dx, dy, Block.size/2)){
                            mat[i][j].getDamage(50);
                            bullet.toDelete=true;

                            //Todo: delete (
                            Paint cirpaint = new Paint();
                            cirpaint.setColor(Color.argb(150, 255, 0, 0));
                            canvas.drawCircle(dx+player.x, dy+player.y, Block.size/2, cirpaint);
                            //)
                        }
                    }
                }
            }
        }

    }

    void crack(MainGame mainGame){
        try {
            int types[][] = new int[mat.length][mat[0].length];
            for(int i=0;i<types.length;i++)
                for(int j=0;j<types[i].length;j++)
                    types[i][j] = -1;
            int type = 0;
            for(int i=0;i<types.length;i++)
                for(int j=0;j<types[i].length;j++){
                    if(mat[i][j] != null && types[i][j] == -1){
                        crackTypes(i, j, type, types);
                        type ++;
                    }
                }
            TreeMap<Point, Block> ships[] = new TreeMap[type];
            Point positions[] = new Point[type];

            for(int i=0;i<type;i++) {
                ships[i] = new TreeMap<>();
                positions[i] = new Point(mat.length, mat[0].length);
            }

            int type_with_controller = type-1;

            for(int i=0;i<mat.length;i++)
                for(int j=0;j<mat[i].length;j++)
                    if (types[i][j] != -1) {
                        if(mat[i][j].getType() == 5)type_with_controller = types[i][j];
                        ships[types[i][j]].put(new Point(i, j), mat[i][j]);
                        positions[types[i][j]].x = Math.min(i, positions[types[i][j]].x);
                        positions[types[i][j]].y = Math.min(j, positions[types[i][j]].y);
                    }
            float old_mass_x = mass_x, old_mass_y = mass_y, old_angle=angle;
            for(int I=0;I<type+1;I++){
                int i=I;
                SpaceShip last;
                if(I==type_with_controller){
                    continue;
                }else if(I == type){
                    last = this;
                    i = type_with_controller;
                }else{
                    mainGame.ships.add(new SpaceShip());
                    last = mainGame.ships.get(mainGame.ships.size()-1);
                }
                last.parse_from_blockTree(ships[i]);
                float _X=(last.mass_x-old_mass_x+positions[i].x)*Block.size, _Y=(last.mass_y-old_mass_y+positions[i].y)*Block.size;
                float cosa = (float) Math.cos(-old_angle * 3.14 / 180), sina = (float) Math.sin(-old_angle * 3.14 / 180);
                float __X = _X * cosa - _Y * sina, __Y = _Y * cosa + _X * sina;
                last.setPosition((x + __X), (y + __Y), old_angle);
            }

            if(type == 0)
                onDelete = true;
        }catch (Exception e){e.printStackTrace();}
    }

    void crackTypes(int i, int j, int type, int types[][]){
        if(i<0 || j<0 || i>=types.length)return;
        if(j>=types[i].length)return;
        if(types[i][j] != -1)return;
        if(mat[i][j] == null)return;
        types[i][j] = type;
        crackTypes(i+1, j, type, types);
        crackTypes(i-1, j, type, types);
        crackTypes(i, j+1, type, types);
        crackTypes(i, j-1, type, types);
    }

    public String toString(){
        if(mat == null)return "null";
        String ans = "SpaceShip";
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++){
                if(mat[i][j] == null)ans += "_";
                else ans += mat[i][j].getType();
                ans+= " ";
            }
            ans += '\n';
        }
        return ans;
    }

    public void xShip(SpaceShip other, Canvas canvas, Player pl) {
        if(Round.Round_x_Round(x+mass_x, y+mass_y, radius, other.x+other.mass_x, other.y+other.mass_y, other.radius)){
            for(int i=0;i<mat.length;i++){
                for(int j=0;j<mat[i].length;j++){
                    Paint paint = new Paint();
                    paint.setColor(Color.argb(100, 255, 0, 0));
                    float cosa = (float) Math.cos(-angle * 3.14 / 180), sina = (float) Math.sin(-angle * 3.14 / 180);
                    float   shift_x = (i+0.5f-mass_x)*Block.size * cosa - (j+0.5f-mass_y)*Block.size * sina,
                            shift_y = (j+0.5f-mass_y)*Block.size * cosa + (i+0.5f-mass_x)*Block.size * sina;
                    //canvas.drawCircle(x+shift_x-pl.x, y+shift_y-pl.y, Block.size>>1, paint);
                    other.xBlock(mat[i][j], x+shift_x, y+shift_y, this, pl, canvas);

                }
            }
        }
    }

    private void xBlock(Block block, float obx, float oby, SpaceShip other, Player pl, Canvas canvas) {
        for(int i=0;i<mat.length;i++){
            for(int j=0;j<mat[i].length;j++){
                Paint paint = new Paint();
                paint.setColor(Color.argb(100, 0, 255, 0));
                float cosa = (float) Math.cos(-angle * 3.14 / 180), sina = (float) Math.sin(-angle * 3.14 / 180);
                float   shift_x = (i+0.5f-mass_x)*Block.size * cosa - (j+0.5f-mass_y)*Block.size * sina,
                        shift_y = (j+0.5f-mass_y)*Block.size * cosa + (i+0.5f-mass_x)*Block.size * sina;
                //canvas.drawCircle(x+shift_x-pl.x, y+shift_y-pl.y, Block.size>>1, paint);
                float bx = x+shift_x, by = y+shift_y;
                if(Round.Round_x_Round(obx, oby, Block.size>>1, bx, by, Block.size>>1)){
                    float force_x=bx-obx, force_y=by-oby;
                    float len_of_force_vector = (float) Math.sqrt(force_x*force_x+force_y*force_y);
                    force_x *= (Block.size-len_of_force_vector)/len_of_force_vector;
                    force_y *= ((Block.size - len_of_force_vector) / len_of_force_vector);
                    this.apply_force(bx-x, by-y, force_x*10, force_y*10, canvas);
                    other.apply_force(obx-other.x, oby-other.y, -force_x*10, -force_y*10, canvas);
                }
            }
        }
    }
}