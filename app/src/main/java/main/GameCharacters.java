package main;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.kama.pizzarush.R;

public enum GameCharacters {

    PLAYER(R.drawable.player_sprite),
    PLAYER_THROWING_1(R.drawable.player_throwing_1),
    PLAYER_THROWING_2(R.drawable.player_throwing_2),
    PLAYER_THROWING_3(R.drawable.player_throwing_3),
    PIZZA(R.drawable.pizza),
    PLATE(R.drawable.plate),
    PIZZABOX(R.drawable.pizzabox),
    PIZZABOX_CORRECT_1(R.drawable.pizzabox_correct1),
    PIZZABOX_CORRECT_2(R.drawable.pizzabox_correct2),
    PIZZABOX_INCORRECT_1(R.drawable.pizzabox_incorrect1),
    PIZZABOX_INCORRECT_2(R.drawable.pizzabox_incorrect2),
    DANCE1(R.drawable.dance1),
    DANCE2(R.drawable.dance2),
    DANCE3(R.drawable.dance3),
    DANCE4(R.drawable.dance4),
    DANCE5(R.drawable.dance5),
    PATRON(R.drawable.patron_idle),
    PATRON_WALK1(R.drawable.patron_walk_1),
    PATRON_WALK2(R.drawable.patron_walk_2),
    PATRON_EAT1(R.drawable.patron_eat_1),
    PATRON_EAT2(R.drawable.patron_eat_2),
    CHEF_BODY(R.drawable.chef_body),
    CHEF_HEAD(R.drawable.chef_head),
    COUNTER(R.drawable.counter),
    HEART(R.drawable.heart),
    TITLE_BG(R.drawable.title_bg);

    private Bitmap spriteSheet;
    private Bitmap spriteSheetNoScale;
    private Bitmap spriteSheet2xScale;
    final int screenWidth = GamePanel.screenWidth == 0 ? 1080 : GamePanel.screenWidth;
    final int screenHeight = GamePanel.screenHeight == 0 ? 2097 : GamePanel.screenHeight;
    final float response = (float) (screenWidth / 1080 + screenHeight / 2097) /2;

    GameCharacters(int resID) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;


        spriteSheet = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        spriteSheetNoScale = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        spriteSheet2xScale = BitmapFactory.decodeResource(MainActivity.getGameContext().getResources(), resID, options);
        spriteSheet = getScaledBitmap(spriteSheet, 5*response);
        spriteSheetNoScale = getScaledBitmap(spriteSheetNoScale, 1*response);
        spriteSheet2xScale = getScaledBitmap(spriteSheet2xScale, 2*response);

    }

    private Bitmap getScaledBitmap(Bitmap bitmap, double newSize){
        if(newSize<=0)
            newSize=1;

        int sizeX = (int) (bitmap.getWidth()*newSize);
        int sizeY = (int) (bitmap.getHeight()*newSize);


        return Bitmap.createScaledBitmap(bitmap, sizeX, sizeY, false);
    }

    public Bitmap getSpriteSheet() {
        return spriteSheet;
    }

    public Bitmap getSpriteSheetNoScale() { return spriteSheetNoScale; }
    public Bitmap getSpriteSheet2xScale() { return spriteSheet2xScale; }

}