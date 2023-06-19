package com.example.pizzarush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private ArrayList<pizza> pizzas = new ArrayList<>();
    private ArrayList<patron> patrons = new ArrayList();
    private ArrayList<emptyPlate> plates = new ArrayList<>();
    private ArrayList<point> points = new ArrayList<>();
    private int patronSpeed = 5;
    private int playerPosition = 2;
    private int score = 0;
    private int highScore = 0;
    // OnTouch Variables -----
    private long startTime = 0;
    private long endTime = 0;
    private boolean isPressed = false;
    // ------
    final private int playerWidth = GameCharacters.PLAYER.getSpriteSheet().getWidth();
    final private int pizzaWidth = GameCharacters.PIZZA.getSpriteSheet().getWidth();
    final private int emptyPlateWidth = GameCharacters.PLATE.getSpriteSheet().getWidth();
    Object patronSleep = new Object();
    private GameLoop gameLoop;
    String gameOverReason = null;
    private enum gameState{
        ACTIVE,
        GAME_OVER,
        MAIN_MENU
    }
    private gameState currentGameState = gameState.MAIN_MENU;

    public GamePanel(Context context) {
        super(context);
        System.out.println(playerWidth);
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

        spawnPatron(-1);
    }

    public void render(double delta){
        Canvas canvas = holder.lockCanvas();
        switch(currentGameState) {
            case MAIN_MENU:
                canvas.drawBitmap(GameCharacters.MAIN_MENU.getSpriteSheetNoScale(),0, 100, null);
                canvas.drawText("HighScore: "+highScore, 520, 1600, textPaint);
                break;
            case ACTIVE:
                canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);

                // PATRONS
                synchronized (patrons){
                    for(patron patron : patrons){
                        // canvas.drawRect(patron.patronAisle*360-240+patron.patronSize/2, patron.patronPosition, patron.patronAisle*360 - 240-patron.patronSize/2, patron.patronPosition+patron.patronSize, greenPaint);
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-260+patron.patronSize/2, patron.patronPosition, null);
                        if(!patron.satisfied) {
                            patron.patronPosition += delta * patronSpeed * 60;
                            if (patron.patronPosition >= 1640)
                                gameOver(1, delta);
                        }
                    }

                }
                // PLATES
                synchronized (plates){
                    for(emptyPlate plate : plates){
                        canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180-emptyPlateWidth/2, plate.emptyPlatePos, null);
                        plate.emptyPlatePos += delta * plate.emptyPlateSpeed * 60;
                        if(plate.emptyPlatePos>=1900) // TEST VALUE (1800)
                            gameOver(2, delta);
                    }
                }
                // PIZZAS
                synchronized (pizzas) {
                    for(pizza pizza : pizzas){
                        canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),pizza.pizzaAislePosition*360-180-pizzaWidth/2, pizza.pizzaPosition, null);
                        // canvas.drawRect(pizza.pizzaAislePosition*360-180+pizza.pizzaSize/2, pizza.pizzaPosition, pizza.pizzaAislePosition*360 - 180-pizza.pizzaSize/2, pizza.pizzaPosition+pizza.pizzaSize, bluePaint);
                        pizza.pizzaPosition -= delta * pizza.pizzaSpeed * 60;
                        if(pizza.pizzaPosition<=0)
                            gameOver(0, delta);
                    }
                }
                // PLAYER
                canvas.drawBitmap(GameCharacters.PLAYER.getSpriteSheet(), playerPosition*360-180-playerWidth/2, 1640, null);
                // POINTS
                synchronized (points){
                    for(point point : points) {
                        canvas.drawText("+"+point.pointsAmt, point.posX, point.posY, textPaint);
                        point.posY-=2;
                        point.framesAlive++;
                    }
                }
                canvas.drawText("Score :"+score, 10, 100, textScorePaint);
                break;
            case GAME_OVER:
                canvas.drawBitmap(GameCharacters.FLOOR_GAMEOVER.getSpriteSheetNoScale(), 0, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);

                // PATRONS
                synchronized (patrons){
                    for(patron patron : patrons){
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-260+patron.patronSize/2, patron.patronPosition, null);
                    }
                }
                // PLATES
                synchronized (plates){
                    for(emptyPlate plate : plates){
                        canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180-emptyPlateWidth/2, plate.emptyPlatePos, null);
                    }
                }
                // PIZZAS
                synchronized (pizzas) {
                    for(pizza pizza : pizzas){
                        canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),pizza.pizzaAislePosition*360-180-pizzaWidth/2, pizza.pizzaPosition, null);
                    }
                }
                // PLAYER
                canvas.drawBitmap(GameCharacters.PLAYER.getSpriteSheet(), playerPosition*360-180-playerWidth/2, 1640, null);

                canvas.drawText("GAME OVER",540, 900, textPaint);
                canvas.drawText(gameOverReason,540, 1000, textPaint);
                canvas.drawText("Score: "+score, 520, 1100, textPaint);
            default:

                break;
        }

        holder.unlockCanvasAndPost(canvas); // Take the canvas and draw it
    }

    public void update(double delta){
        List<pizza> toRemovePizza = new ArrayList<pizza>();
        List<patron> toRemovePatron = new ArrayList<patron>();
        List<emptyPlate> toRemovePlate = new ArrayList<emptyPlate>();
        List<point> toRemovePoint = new ArrayList<point>();
        synchronized (plates){
            for(emptyPlate plate : plates){
                if(plate.emptyPlatePos >=1300){ // TEST VALUE (1640)
                    if(plate.emptyPlateAisle == playerPosition){
                        toRemovePlate.add(plate);
                    }
                }
            }
        }
        synchronized (pizzas){
            for(pizza pizza : pizzas){
                synchronized (patrons) {
                    for (patron patron : patrons) {
                        if(isColliding(pizza, patron)){
                            patron.satisfied = true;
                            patron.spriteToRender = GameCharacters.PATRON_EAT1.getSpriteSheet();
                            toRemovePizza.add(pizza);
                        }
                    }
                }
            }
        }
        synchronized (points){
            for(point point : points){
                if(point.framesAlive >= point.endFrames){
                    toRemovePoint.add(point);
                }
            }
        }
        // Satisfied Patrons
        if(currentGameState == gameState.ACTIVE) {
            for (patron patron : patrons) {
                if (patron.satisfied) {
                    if (patron.satisfiedTimer <= 0) {
                        toRemovePatron.add(patron);
                        plates.add(new emptyPlate(patron.patronPosition, patron.patronAisle));
                    } else
                        patron.satisfiedTimer--;
                }
            }
        }
        if(toRemovePizza != null){
            synchronized (pizzas){
                pizzas.removeAll(toRemovePizza);
            }
        }
        if(toRemovePatron != null){
            synchronized (patrons){
                patrons.removeAll(toRemovePatron);
            }
        }
        if(toRemovePlate != null){
            synchronized (plates){
                plates.removeAll(toRemovePlate);
            }
        }
        if(toRemovePoint != null){
            synchronized (points){
                points.removeAll(toRemovePoint);
            }
        }
        // Updating patron animations
        if(currentGameState == gameState.ACTIVE) {
            for (patron patron : patrons) {
                long currentTime = SystemClock.elapsedRealtime();
                if (currentTime - patron.timer >= 400) {
                    if (!patron.satisfied) {
                        if(currentGameState == gameState.ACTIVE) {
                            if (patron.spriteToRender.sameAs(GameCharacters.PATRON_WALK1.getSpriteSheet()))
                                patron.spriteToRender = GameCharacters.PATRON_WALK2.getSpriteSheet();
                            else
                                patron.spriteToRender = GameCharacters.PATRON_WALK1.getSpriteSheet();
                        }
                        else
                            patron.spriteToRender = GameCharacters.PATRON.getSpriteSheet();
                    } else {
                        if (patron.spriteToRender.sameAs(GameCharacters.PATRON_EAT1.getSpriteSheet()))
                            patron.spriteToRender = GameCharacters.PATRON_EAT2.getSpriteSheet();
                        else
                            patron.spriteToRender = GameCharacters.PATRON_EAT1.getSpriteSheet();
                    }
                    patron.timer = SystemClock.elapsedRealtime();
                }
            }
        }
        render(delta);
    }
    private boolean isColliding(pizza pizza, patron patron) {
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
                // 0 = no position/failure | 1 = left position | 2 = mid position | 3 = right position
                if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(isPressed == false) {
                        startTime = SystemClock.elapsedRealtime();
                        isPressed = true;
                    }
                    if(event.getX()<=360)
                        playerPosition=1;
                    else if(event.getX()<=720)
                        playerPosition=2;
                    else
                        playerPosition=3;
                }
                if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    endTime = SystemClock.elapsedRealtime();
                    if(endTime - startTime <=100) {
                        synchronized (pizzas) {
                            pizzas.add(new pizza());
                        }
                    }
                    isPressed = false;
                }
                break;
            case GAME_OVER:
                if(event.getAction() == MotionEvent.ACTION_UP){
                    synchronized (pizzas) {
                        pizzas.clear();
                    }
                    synchronized (patrons) {
                        patrons.clear();
                    }
                    synchronized (plates){
                        plates.clear();
                    }
                    currentGameState = gameState.MAIN_MENU;
                    gameLoop.patronSpawnRate = 10;
                    if(score > highScore)
                        highScore = score;
                    score = 0;
                }
                break;

            default:

                break;
        }
        return true;
    }
    public class pizza{
        int pizzaAislePosition = playerPosition; // X
        int pizzaSpeed = 10;
        int pizzaPosition = 1640; // Y
        int pizzaSize = 100;
    }
    public class patron{
        private int patronPosition;
        private int patronAisle;
        boolean satisfied = false;
        int satisfiedTimer = 2 * 60;
        private Bitmap spriteToRender;
        private long timer = SystemClock.elapsedRealtime();
        private int patronSize = 100;
        public patron(int pos, int aisle, Bitmap spriteToRender){
            this.patronPosition = pos;
            this.spriteToRender = spriteToRender;
            if(aisle==0){
                patronAisle= random.nextInt(3)+1;
            }
            else
                this.patronAisle = aisle;
        }
    }
    public class emptyPlate{
        private int emptyPlateSpeed = 13;
        private int emptyPlateAisle;
        private int emptyPlatePos;
        public emptyPlate(int pos, int aisle){
            this.emptyPlatePos = pos;
            this.emptyPlateAisle = aisle;
        }
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
            synchronized (patrons) {
                synchronized (patronSleep) {
                    int groupAisle = random.nextInt(3)+1;
                    switch (pattern) {
                        case 0:
                            patrons.add(new patron(0, 0, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                        case 1:
                            patrons.add(new patron(0, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(new patron(-150, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                        case 2:
                            patrons.add(new patron(0, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(new patron(-150, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(new patron(-300, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                        case 3:
                            patrons.add(new patron(0, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(new patron(-250, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(new patron(-400, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                    }
                }
            }
        }
    }

    public void gameOver(int reason, double delta){
        currentGameState = gameState.GAME_OVER;
        switch (reason){
            case 0:
                System.out.println("GAME OVER: \"Reason 0\" (Pizza has fallen off the edge)");
                gameOverReason = "A pizza fell off the edge!";
                break;
            case 1:
                System.out.println("GAME OVER: \"Reason 1\" (Patron has reached player)");
                gameOverReason = "A customer was unhappy!";
                break;
            case 2:
                System.out.println("GAME OVER: \"Reason 2\" (Empty plate has fallen)");
                gameOverReason = "A plate has fallen!";
                break;
            default:
                break;
        }
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
