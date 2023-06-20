package com.example.pizzarush.entities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.pizzarush.MainActivity;
import com.example.pizzarush.R;

public enum GameCharacters {

    PLAYER(R.drawable.player_sprite),
    PIZZA(R.drawable.pizza),
    ENEMY_MED(R.drawable.enemy_med_sprite),
    BULLET_SMALL(R.drawable.bullet_small),
    MAIN_MENU(R.drawable.placeholder_menu);

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
