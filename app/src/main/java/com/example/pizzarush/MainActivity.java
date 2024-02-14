package com.example.pizzarush;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;

import com.example.pizzarush.GamePanel;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static Context gameContext;
    public static final String FILE_NAME = "saveState";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameContext = this;
        setContentView(new GamePanel(this));


    }

    public static Context getGameContext(){
        return gameContext;
    }

}

