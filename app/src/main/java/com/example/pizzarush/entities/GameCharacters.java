package com.example.pizzarush.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.pizzarush.MainActivity;
import com.example.pizzarush.R;

public enum GameCharacters {

    PLAYER(R.drawable.player_sprite),
    PLAYER_THROWING_1(R.drawable.player_throwing_1),
    PLAYER_THROWING_2(R.drawable.player_throwing_2),
    PLAYER_THROWING_3(R.drawable.player_throwing_3),
    PIZZA(R.drawable.pizza),
    PLATE(R.drawable.plate),
    PATRON(R.drawable.patron_idle),
    PATRON_WALK1(R.drawable.patron_walk_1),
    PATRON_WALK2(R.drawable.patron_walk_2),
    PATRON_EAT1(R.drawable.patron_eat_1),
    PATRON_EAT2(R.drawable.patron_eat_2),
    MAIN_MENU(R.drawable.placeholder_menu),
    FLOOR(R.drawable.floor),
    GAMEOVER_FLOOR(R.drawable.floor_gameover),
    GAMEOVER_TEXT(R.drawable.gameover_text),
    COUNTER(R.drawable.counter);

    private Bitmap spriteSheet;
    private Bitmap spriteSheetNoScale;
    private BitmapFactory.Options options = new BitmapFactory.Options();
    GameCharacters(int resID) {
        options.inScaled = false;
        spriteSheet = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        spriteSheetNoScale = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        spriteSheet = getScaledBitmap(spriteSheet, 5);
    }

    private Bitmap getScaledBitmap(Bitmap bitmap, int newSize){

        return Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()*newSize, bitmap.getHeight()*newSize, false);
    }

    public Bitmap getSpriteSheet() {
        return spriteSheet;
    }

    public Bitmap getSpriteSheetNoScale() { return spriteSheetNoScale; }
}
