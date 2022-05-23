package sum.proj;

import static sum.proj.MainActivity.viewsChoose.*;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    PlayerResources plRes = new PlayerResources();

    public enum viewsChoose{mainMenu, world, masterRoom};
    viewsChoose  nowChoose = mainMenu;
    MainGame mainGame;
    MasterRoom masterRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /// Загрузка изображения блоков и кнопок
        Block.loadPicture(getResources());
        MyButton.loadPicture(getResources());

        /// Загрузка ресурсов игрока
        plRes.load(getPreferences(MODE_PRIVATE));


        SpaceShip.mainActivity = this;

        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        //setContentView(R.layout.start_menu);
        choose_of_view();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(masterRoom != null)masterRoom.stop();
        if(mainGame != null){
            mainGame.stop();
            plRes.save(getPreferences(MODE_PRIVATE));
        }
        if(keyCode == event.KEYCODE_BACK){
            switch (nowChoose){
                case mainMenu:
                    finish();
                    break;
                default:
                    nowChoose = mainMenu;
                    break;
            }
            choose_of_view();
        }
        return true;
    }

    void choose_of_view(){
        switch (nowChoose){
            case mainMenu: setContentView(R.layout.start_menu); return;
            case world:
                mainGame = new MainGame(this, this);

                setContentView(mainGame);
                return;
            case masterRoom:
                masterRoom = new MasterRoom(this);
                setContentView(masterRoom);
                masterRoom.onChoose();
                return;
        }
    }

    public void onClick_start_menu_atWorld(View view){
        nowChoose = world;
        choose_of_view();
    }

    public void onClick_start_menu_atMasterRoom(View view) {
        nowChoose = viewsChoose.masterRoom;
        choose_of_view();
    }

    public void onClick_start_menu_quite(View view) { finish(); }
}