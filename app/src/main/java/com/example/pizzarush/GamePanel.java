package com.example.pizzarush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.pizzarush.entities.GameCharacters;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    final private Paint textPaint = new Paint();
    final private Paint textScorePaint = new Paint();
    private SurfaceHolder holder;
    private Random random = new Random();
    private ArrayList<point> points = new ArrayList<>();
    private ArrayList<enemy_med> enemy_meds = new ArrayList<>();

    // Player shit
    private PointF playerPosition = new PointF(520,1200);
    private Bitmap playerBullet = GameCharacters.BULLET_SMALL.getSpriteSheetNoScale();
    private int score = 0;
    private int highScore = 0;
    private GameLoop gameLoop;
    private enum gameState{
        ACTIVE,
        GAME_OVER,
        MAIN_MENU
    }
    private gameState currentGameState = gameState.MAIN_MENU;

    public GamePanel(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(75);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        textScorePaint.setColor(Color.WHITE);
        textScorePaint.setTextSize(75);
        textScorePaint.setTextAlign(Paint.Align.LEFT);
        textScorePaint.setStyle(Paint.Style.FILL);

        gameLoop = new GameLoop(this);
    }

    public void render(double delta){
        Canvas canvas = holder.lockCanvas();
        switch(currentGameState) {
            case MAIN_MENU:
                canvas.drawBitmap(GameCharacters.MAIN_MENU.getSpriteSheetNoScale(),0, 100, null);
                canvas.drawText("HighScore: "+highScore, 520, 1600, textPaint);
                break;
            case ACTIVE:
                // TODO
                break;
            case GAME_OVER:
                // TODO
                canvas.drawText("GAME OVER",540, 900, textPaint);
                canvas.drawText("Score: "+score, 520, 1100, textPaint);
            default:

                break;
        }

        holder.unlockCanvasAndPost(canvas); // Take the canvas and draw it
    }

    public void update(double delta){
        // TODO
        render(delta);
    }
    private boolean isColliding(pizza pizza, patron patron) {
        // TODO
        if(!patron.satisfied) {
            if (patron.patronAisle == pizza.pizzaAislePosition) {
                if (pizza.pizzaPosition < patron.patronPosition + patron.patronSize && pizza.pizzaPosition > patron.patronPosition)
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(currentGameState){
            case MAIN_MENU:
                if(event.getAction() == MotionEvent.ACTION_UP){
                    currentGameState = gameState.ACTIVE;
                }
                break;
            case ACTIVE:
                playerPosition.x = event.getX();
                playerPosition.y = event.getY();
                if(event.getAction() == MotionEvent.ACTION_MOVE){

                }
                break;
            case GAME_OVER:
                // TODO
                break;

            default:

                break;
        }
        return true;
    }

    public class point{
        int pointsAmt;
        int posY;
        int posX;
        int framesAlive = 0;
        int endFrames = 100; // How long it will stay on screen
        public point(int posX, int posY, int pointsGiven){
            this.pointsAmt = pointsGiven;
            score += pointsGiven;
            this.posY = posY;
            this.posX = posX;
        }
    }
    public void spawnPatron(int pattern){
        if(currentGameState == gameState.ACTIVE) {
            switch(pattern){
                // TODO
            }
        }
    }
    public class player_bullet{
        Bitmap bullet = GameCharacters.BULLET_SMALL.getSpriteSheetNoScale();
        int speed = 10;
        PointF pos = new PointF();
        public player_bullet(int x, int y){
            this.pos.x = x;
            this.pos.y = y;
        }
    }
    public class enemy_med{

        int health = 25;
        int speed = 5;
        int reloadTime = 1 * 120; // seconds * FPS
        int currentTimeForReload = 0;
        PointF pos = new PointF();
        Bitmap bullet = GameCharacters.BULLET_SMALL.getSpriteSheetNoScale();
        Bitmap sprite = GameCharacters.ENEMY_MED.getSpriteSheetNoScale();
        public enemy_med(int x, int y){
            this.pos.x = x;
            this.pos.y = y;
        }
    }
    public class enemy_small{

    }
    public class enemy_large{

    }
    public class enemy_boss{

    }

    public void gameOver(int reason, double delta){
        currentGameState = gameState.GAME_OVER;
    }


    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        gameLoop.gameLoopStart();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

}
