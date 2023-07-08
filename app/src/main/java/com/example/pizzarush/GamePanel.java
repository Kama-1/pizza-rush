package com.example.pizzarush;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.example.pizzarush.entities.GameCharacters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    final private Paint textPaint = new Paint();
    final private Paint textPaintBorder = new Paint();
    final private Paint textScorePaint = new Paint();
    final private Paint tutorialGrey = new Paint();
    private SurfaceHolder holder;
    private Random random = new Random();
    private ArrayList<pizza> pizzas = new ArrayList<>();
    private ArrayList<patron> patrons = new ArrayList();
    private ArrayList<emptyPlate> plates = new ArrayList<>();
    private ArrayList<point> points = new ArrayList<>();
    private int patronSpeed = 5;
    private int playerPosition = 2;
    private double playerAnimationState = 0;
    private Bitmap playerSprite = GameCharacters.PLAYER.getSpriteSheet();
    private int score = 0;
    private int highScore = 0;
    private boolean hasMoved = false;
    private int newPosition = playerPosition;
    // ------
    final private int playerWidth = GameCharacters.PLAYER.getSpriteSheet().getWidth();
    final private int pizzaWidth = GameCharacters.PIZZA.getSpriteSheet().getWidth();
    final private int emptyPlateWidth = GameCharacters.PLATE.getSpriteSheet().getWidth();
    Object patronSleep = new Object();
    private GameLoop gameLoop;
    String gameOverReason = "Default";
    private enum gameState{
        ACTIVE,
        GAME_OVER,
        MAIN_MENU,
        TUTORIAL,
    }
    int tutorialState = 0;
    boolean leftCheck = false;
    boolean rightCheck = false;
    int doubleCheck = 0;
    boolean finishedTutorial = false;
    private gameState currentGameState = gameState.MAIN_MENU;

    public GamePanel(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(75);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        textPaintBorder.setStyle(Paint.Style.STROKE);
        textPaintBorder.setTextSize(75);
        textPaintBorder.setTextAlign(Paint.Align.CENTER);
        textPaintBorder.setStrokeWidth(12);
        textPaintBorder.setColor(Color.BLACK);

        textScorePaint.setColor(Color.WHITE);
        textScorePaint.setTextSize(75);
        textScorePaint.setTextAlign(Paint.Align.LEFT);
        textScorePaint.setStyle(Paint.Style.FILL);

        tutorialGrey.setColor(Color.BLACK);
        tutorialGrey.setAlpha(125);
        gameLoop = new GameLoop(this);

        spawnPatron(-1);
    }

    public void render(double delta){
        Canvas canvas = holder.lockCanvas();
        switch(currentGameState) {
            case TUTORIAL:
                canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);
                switch(tutorialState){
                    case 0:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-260+patron.patronSize/2, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        canvas.drawRect(0, 0, 1080, 1500, tutorialGrey);
                        canvas.drawText("Tap to throw pizza", 520, 1950, textPaint);
                        break;
                    case 1: // Throwing pizza
                        synchronized (pizzas) {
                            for(patron patron : patrons){
                                canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-260+patron.patronSize/2, patron.patronPosition, null);
                            }
                            for(pizza pizza : pizzas){
                                canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),pizza.pizzaAislePosition*360-180-pizzaWidth/2, pizza.pizzaPosition, null);
                                // canvas.drawRect(pizza.pizzaAislePosition*360-180+pizza.pizzaSize/2, pizza.pizzaPosition, pizza.pizzaAislePosition*360 - 180-pizza.pizzaSize/2, pizza.pizzaPosition+pizza.pizzaSize, bluePaint);
                                pizza.pizzaPosition -= delta * pizza.pizzaSpeed * 60;
                                if(pizza.pizzaPosition<=0)
                                    gameOver(0, delta);
                            }
                        }
                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        break;

                    case 2: // Eating

                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        break;
                    case 3: // Telling how to catch plates
                        for(emptyPlate plate : plates){
                            canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180-emptyPlateWidth/2, plate.emptyPlatePos, null);
                        }
                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);

                        canvas.drawRect(0, 0, 1080, 370, tutorialGrey);
                        canvas.drawRect(0, 370, 300, 670, tutorialGrey);
                        canvas.drawRect(780, 370, 1080, 670, tutorialGrey);
                        canvas.drawRect(0, 670, 1080, 2300, tutorialGrey);

                        canvas.drawText("Be in the right aisle", 520, 750, textPaint);
                        canvas.drawText("to catch the plates", 520, 850, textPaint);
                        canvas.drawText("(Tap to continue)", 520, 950, textPaint);
                        break;
                    case 4: // Catching the plate
                        for(emptyPlate plate : plates){
                            canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180-emptyPlateWidth/2, plate.emptyPlatePos, null);
                            plate.emptyPlatePos += delta * plate.emptyPlateSpeed * 60;
                            if(plate.emptyPlatePos>=1900)
                                gameOver(2, delta);
                            canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        }
                        break;
                    case 5: // Telling them to move left and right
                        // Maybe add one of those little cursor graphics
                        // TODO
                        canvas.drawRect(0, 0, 1080, 1500, tutorialGrey);
                        canvas.drawText("Tap and hold to move aisles", 520, 1950, textPaint);
                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        if(playerPosition == 1)
                            leftCheck = true;
                        if(playerPosition == 3)
                            rightCheck = true;
                        if(leftCheck && rightCheck){
                            if(doubleCheck >= 2)
                                tutorialState = 6;
                            doubleCheck++;
                            leftCheck = false;
                            rightCheck = false;
                        }
                        break;
                    case 6: // Final notes & start the game
                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        canvas.drawRect(0, 0, 1080, 2200, tutorialGrey);
                        canvas.drawText("Feed the patrons", 520, 700, textPaint);
                        canvas.drawText("and catch the plates :)", 520, 800, textPaint);
                        canvas.drawText("HIGHSCORE HOLDER", 520, 1100, textPaint);
                        canvas.drawText("Alyssa: 6900", 530, 1200, textPaint);
                        canvas.drawText("Thank you for play testing <3", 530, 1400, textPaint);
                        break;
                }
                break;

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
                canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
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
                canvas.drawBitmap(GameCharacters.GAMEOVER_FLOOR.getSpriteSheetNoScale(), 0, 0, null);
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
                canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);

                canvas.drawBitmap((GameCharacters.GAMEOVER_TEXT.getSpriteSheetNoScale()),0,800, null);
                canvas.drawText(gameOverReason,540, 1100, textPaintBorder);
                canvas.drawText(gameOverReason,540, 1100, textPaint);
                canvas.drawText("Score: "+score, 520, 1200, textPaintBorder);
                canvas.drawText("Score: "+score, 520, 1200, textPaint);
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
                        playAudioPlateCollect();
                        tutorialState = 5;
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
                            playAudioHitPatron();
                            if(currentGameState != gameState.TUTORIAL) {
                                synchronized (points) {
                                    points.add(new point(patron.patronAisle * 360 - patron.patronSize, patron.patronPosition, 100));
                                }
                            }
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
        if(currentGameState == gameState.ACTIVE || currentGameState == gameState.TUTORIAL) {
            for (patron patron : patrons) {
                if (patron.satisfied) {
                    if (patron.satisfiedTimer <= 0) {
                        toRemovePatron.add(patron);
                        plates.add(new emptyPlate(patron.patronPosition, patron.patronAisle));
                        tutorialState = 3;
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
        long currentTime = SystemClock.elapsedRealtime();
        // Updating patron animations
        if(currentGameState == gameState.ACTIVE || currentGameState == gameState.TUTORIAL) {
            for (patron patron : patrons) {
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
        // Updating player animations
        if(currentGameState != gameState.GAME_OVER){
            if(playerAnimationState <= 0) {
                playerSprite = GameCharacters.PLAYER.getSpriteSheet();
            }
            else if (playerAnimationState <= 1) {
                playerSprite = GameCharacters.PLAYER_THROWING_3.getSpriteSheet();
                playerAnimationState -= 0.1;
            } else if (playerAnimationState <= 2) {
                playerSprite = GameCharacters.PLAYER_THROWING_2.getSpriteSheet();
                playerAnimationState -= 0.1;
            } else if (playerAnimationState <= 3) {
                playerSprite = GameCharacters.PLAYER_THROWING_1.getSpriteSheet();
                playerAnimationState -= 0.1;
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
            case TUTORIAL:
                switch(tutorialState){
                    case 0:
                        if(event.getAction() == MotionEvent.ACTION_UP){
                            synchronized (pizzas) {
                                pizzas.add(new pizza());
                            }
                            playAudioThrowPizza();
                            playerAnimationState = 3.0;
                            tutorialState = 1;
                        }
                        break;
                    case 1:
                    case 2:
                    case 4:
                        break;
                    case 3:
                        if(event.getAction() == MotionEvent.ACTION_UP)
                            tutorialState = 4;
                        break;
                    case 5:
                        if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                            if(event.getX()<=360)
                                playerPosition=1;
                            else if(event.getX()<=720)
                                playerPosition=2;
                            else
                                playerPosition=3;
                        }
                        break;
                    case 6:
                        if(event.getAction() == MotionEvent.ACTION_UP) {
                            if (finishedTutorial)
                                currentGameState = gameState.MAIN_MENU;
                            else finishedTutorial = true;
                        }
                        break;
                }
                break;
            case MAIN_MENU:
                if(event.getAction() == MotionEvent.ACTION_UP){
                    currentGameState = gameState.ACTIVE;
                }
                break;
            case ACTIVE:
                // 0 = no position/failure | 1 = left position | 2 = mid position | 3 = right position
                if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(event.getX()<=360) {
                        newPosition = 1;
                    }
                    else if(event.getX()<=720)
                        newPosition=2;
                    else
                        newPosition=3;
                    if(newPosition != playerPosition) {
                        hasMoved = true;
                        playerAnimationState = 0;
                    }
                    playerPosition = newPosition;
                }

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if (!hasMoved) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            synchronized (pizzas) {
                                pizzas.add(new pizza());
                            }
                            playAudioThrowPizza();
                            playerAnimationState = 3.0;
                        }
                    } else {
                        hasMoved = false;
                    }
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
                            patrons.add(0, new patron(0, 0, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                        case 1:
                            patrons.add(0, new patron(0, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(0, new patron(-150, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                        case 2:
                            patrons.add(0, new patron(0, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(0, new patron(-150, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(0, new patron(-300, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                        case 3:
                            patrons.add(0, new patron(0, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(0, new patron(-250, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            patrons.add(0, new patron(-400, groupAisle, GameCharacters.PATRON_WALK1.getSpriteSheet()));
                            break;
                    }
                }
            }
        }
        if(currentGameState == gameState.TUTORIAL && pattern == -1){
            patrons.add(new patron(400, 2, GameCharacters.PATRON_WALK1.getSpriteSheet()));
        }
    }

    public void gameOver(int reason, double delta){
        playAudioGameOver();
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
    public void playAudioHitPatron(){
        MediaPlayer hitPatron = MediaPlayer.create(MainActivity.getGameContext(), R.raw.hitpatron );
        hitPatron.start();
    }
    public void playAudioThrowPizza(){
        MediaPlayer throwPizza = MediaPlayer.create(MainActivity.getGameContext(), R.raw.throwpizza );
        throwPizza.setVolume(1,1);
        throwPizza.start();
    }
    public void playAudioGameOver(){
        MediaPlayer gameOver = MediaPlayer.create(MainActivity.getGameContext(), R.raw.gameover );
        gameOver.start();
    }
    public void playAudioPlateCollect(){
        MediaPlayer plateCollect = MediaPlayer.create(MainActivity.getGameContext(), R.raw.platecollect );
        plateCollect.start();
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
