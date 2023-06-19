package com.example.pizzarush;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

import com.example.pizzarush.GamePanel;

public class MainActivity extends AppCompatActivity {

    private static Context gameContext;

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