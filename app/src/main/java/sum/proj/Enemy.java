package sum.proj;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.TreeMap;

public class Enemy extends SpaceShip{
    Enemy(){type = 'E';}
    void attack(SpaceShip ship){}
}

class SimEnemy extends Enemy{
    SimEnemy(){
        super();
        TreeMap<Point, Block> tr = new TreeMap<>();
        tr.put(new Point(1, 0), new GunBlock(){});
        ((GunBlock)tr.get(new Point(1, 0))).shoutDelay=15;


        tr.put(new Point(0, 1), new EngineBlock());
        tr.put(new Point(2, 1), new EngineBlock());

        tr.put(new Point(0, 0), new EngineBlock());
        tr.put(new Point(2, 0), new EngineBlock());

        tr.get(new Point(0, 0)).rot = 2;
        tr.get(new Point(2, 0)).rot = 2;

        tr.put(new Point(1, 1), new ControllerBlock());
        parse_from_blockTree(tr);
    }

    @Override
    void attack(SpaceShip ship) {
        if(mat != null)
            for(int i=0;i<mat.length;i++)
                for(int j=0;j<mat[i].length;j++)
                    if(mat[i][j] != null)
                        mat[i][j].is_activated = false;
        try{
            super.attack(ship);
            mat[1][0].activate();
            if((ship.x - x) * Math.cos(-angle*3.1415/180) + (ship.y - y) * Math.sin(-angle*3.1415/180) < 0){
                mat[2][1].activate();
                mat[0][0].activate();
            }else{
                mat[2][0].activate();
                mat[0][1].activate();
            }
        }catch (Exception e){}
    }
}
