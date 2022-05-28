package sum.proj;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class PlayerResources {
    int iron = 0;

    public PlayerResources setIron(int iron) {
        this.iron = iron;
        return (PlayerResources) this;
    }

    void load(SharedPreferences preferences){
        iron = preferences.getInt("Iron", 345);
    }

    void save(SharedPreferences preferences){
        SharedPreferences.Editor editor = preferences.edit();
        try {
            editor.putInt("Iron", iron);
        }catch (Exception e){}
        editor.commit();
    }

    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(40);

        paint.setColor(Color.parseColor("#aaaaaa"));
        canvas.drawText("Iron: " + iron, 30, 40 + 50*0, paint);
    }

    public void add(PlayerResources res) {
        iron += res.iron;
    }

    public PlayerResources multiply (float param){
        PlayerResources ans = new PlayerResources().
                setIron((int) (this.iron * param));
        return ans;
    }

    public boolean sub(PlayerResources res){
        if(iron - res.iron < 0){
            return false;
        }

        iron -= res.iron;
        return true;
    }
}
