package com.example.pizzarush;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Color;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.pizzarush.entities.GameCharacters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    final private Paint textPaint = new Paint();
    final private Paint textPaintBlack = new Paint();
    final private Paint textPaintHighscore = new Paint();
    final private Paint banner = new Paint();
    final private Paint paintGrey = new Paint();
    final private Paint textPaintBorder = new Paint();
    final private Paint textScorePaint = new Paint();
    final private Paint tutorialGrey = new Paint();
    final private Paint blackPaint = new Paint();
    final private Paint menuBackgroundPaint = new Paint();
    final private Paint paintRed = new Paint();
    final private Paint paintBlue = new Paint();
    final private Paint paintYellow = new Paint();

    final private Paint buttonText = new Paint();
    private SurfaceHolder holder;
    private Random random = new Random();
    private ArrayList<pizza> pizzas = new ArrayList<>();
    private ArrayList<patron> patrons = new ArrayList();
    private ArrayList<emptyPlate> plates = new ArrayList<>();
    private ArrayList<point> points = new ArrayList<>();
    private ArrayList<guessBox> guessBoxes = new ArrayList<>();
    private ArrayList<menuBG> menuPar = new ArrayList<>();
    private int menuBGHeight = GameCharacters.TITLE_BG.getSpriteSheetNoScale().getHeight();
    private int patronSpeed = 5;
    private int level = 1;
    private int playerPosition = 2;
    private double playerAnimationState = 0;
    private Bitmap playerSprite = GameCharacters.PLAYER.getSpriteSheet();
    private int score = 0;
    private int highScore = 0;
    private boolean hasMoved = false;
    private int newPosition = playerPosition;
    final private int playerWidth = GameCharacters.PLAYER.getSpriteSheet().getWidth();
    final private int pizzaWidth = GameCharacters.PIZZA.getSpriteSheet().getWidth();
    final private int emptyPlateWidth = GameCharacters.PLATE.getSpriteSheet().getWidth();
    final private int guessBoxWidth = GameCharacters.PIZZABOX.getSpriteSheet().getWidth();
    Object patronSleep = new Object();
    private GameLoop gameLoop;
    String gameOverReason = "Default";
    private enum gameState{
        ACTIVE,
        GAME_OVER,
        MAIN_MENU,
        TUTORIAL,
        GUESSING,
        GUESSING_INTO,
        GUESSING_OUT
    }
    private gameState currentGameState = gameState.MAIN_MENU;
    int tutorialState = 0;
    boolean guessingIntroFinished = false;
    private long swapTimer = System.currentTimeMillis();
    int swaps = 20; // Swaps that must be done
    // 5 - slow, 10 - medium, 5 - faster
    long introTimer = 0;
    long animTimer = 0;
    long graceTimer = 0;
    int correctBox = 2;
    int guessAnswer = -1;
    int chef_head_bob = 0;
    boolean chef_head_bob_up = true;
    int timesCorrectGuess = 0;
    int guessingIntroBlack = 0;
    boolean blink = true;
    boolean guessCorrect = false;
    boolean leftCheck = false;
    boolean rightCheck = false;
    int doubleCheck = 0;
    boolean finishedTutorial = false;
    DisplayMetrics displayMetrics = new DisplayMetrics();
    int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    public GamePanel(Context context) {
        super(context);

        holder = getHolder();
        holder.addCallback(this);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(75);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        textPaintBlack.setColor(Color.BLACK);
        textPaintBlack.setTextAlign(Paint.Align.CENTER);
        textPaintBlack.setStyle(Paint.Style.FILL);
        textPaintBlack.setTextSize(45);

        banner.setColor(Color.argb(110,210,145,58));

        paintRed.setColor(Color.rgb(233,61,52));
        paintBlue.setColor(Color.rgb(65,131,255));
        paintYellow.setColor(Color.rgb(247,153,38));

        paintGrey.setColor(Color.LTGRAY);

        buttonText.setColor(Color.BLACK);
        buttonText.setTextAlign(Paint.Align.CENTER);
        buttonText.setTextSize(90);
        buttonText.setStyle(Paint.Style.FILL);

        textPaintHighscore.setColor(Color.BLACK);
        textPaintHighscore.setTextSize(75);
        textPaintHighscore.setTextAlign(Paint.Align.CENTER);
        textPaintHighscore.setStyle(Paint.Style.FILL);

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
        System.out.println("ScreenHeight: "+screenHeight);
        System.out.println("ScreenWidth: "+screenWidth);

        menuBackgroundPaint.setColor(Color.parseColor("#FFCF87"));

        blackPaint.setColor(Color.BLACK);

        gameLoop = new GameLoop(this);

        guessBoxes.add(new guessBox(0));
        guessBoxes.add(new guessBox(1));
        guessBoxes.add(new guessBox(2));
        guessBoxes.add(new guessBox(3));
        guessBoxes.add(new guessBox(4));
        menuPar.add(new menuBG(-menuBGHeight));
        menuPar.add(new menuBG(0));
        menuPar.add(new menuBG(menuBGHeight));
        menuPar.add(new menuBG(menuBGHeight*2));
        menuPar.add(new menuBG(menuBGHeight*3));
        menuPar.add(new menuBG(menuBGHeight*4));

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
                            canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+patron.patronSize/2, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);
                        canvas.drawRect(0, 0, 1080, 1500, tutorialGrey);
                        canvas.drawText("Tap to throw pizza", 520, 1950, textPaint);
                        break;
                    case 1: // Throwing pizza
                        synchronized (pizzas) {
                            for(patron patron : patrons){
                                canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+patron.patronSize/2, patron.patronPosition, null);
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
                canvas.drawRect(0,0,screenWidth,screenHeight, menuBackgroundPaint);
                // Pizzas
                ArrayList<menuBG> toRemoveSlides = new ArrayList();
                int minSlides = 0;
                synchronized (menuPar){
                    for(menuBG slide : menuPar){
                        canvas.drawBitmap(GameCharacters.TITLE_BG.getSpriteSheetNoScale(), 0, slide.posY, null);
                        if(slide.posY < 0)
                            minSlides++;
                        if(slide.posY >= screenHeight)
                            toRemoveSlides.add(slide);
                        else
                            slide.posY+=2;
                    }
                    if(minSlides==0){
                        menuPar.add(new menuBG(-menuBGHeight));
                        minSlides=0;
                    }
                    else
                        minSlides=0;
                    if(toRemoveSlides!=null){
                        menuPar.removeAll(toRemoveSlides);
                    }
                }
                // HighScore
                canvas.drawRect(0,screenHeight/3,screenWidth,screenHeight/3+100, banner);
                canvas.drawText("HighScore: "+highScore, screenWidth/2, screenHeight/3+75, textPaintHighscore);

                // Buttons
                drawButton("PLAY", 1000, paintRed, buttonText, canvas);
                drawButton("CHALLENGES", 1300, paintYellow, buttonText, canvas);
                drawButton("TUTORIAL", 1600, paintBlue, buttonText, canvas);

                canvas.drawBitmap(GameCharacters.TITLE.getSpriteSheet(), screenWidth/2 - GameCharacters.TITLE.getSpriteSheet().getWidth()/2, 50, null);
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
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+patron.patronSize/2, patron.patronPosition, null);
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
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+patron.patronSize/2, patron.patronPosition, null);
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
                drawButton("MENU", 1700, paintRed, buttonText, canvas);
                if(score>highScore){
                    canvas.drawText("NEW HIGH-SCORE: "+score, 520, 1200, textPaintBorder);
                    canvas.drawText("NEW HIGH-SCORE: "+score, 520, 1200, textPaint);
                }
                else{
                    canvas.drawText("Score: "+score, 520, 1200, textPaintBorder);
                    canvas.drawText("Score: "+score, 520, 1200, textPaint);
                }
                break;
            case GUESSING:
                canvas.drawColor(Color.rgb(55,55,55));
                canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(),screenWidth/2-screenWidth/8, 0, null);
                canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(),screenWidth/2-screenWidth/8, chef_head_bob, null);
                canvas.drawRect(0, screenHeight/3, screenWidth, screenHeight/2+screenHeight/6+guessBoxWidth*2, paintGrey);
                canvas.drawBitmap(GameCharacters.SPEECH.getSpriteSheet2xScale(), 0, 100, null);
                if(guessAnswer == 5){
                    if(guessCorrect){
                        canvas.drawText("Correct! +2000 points!", GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth()/2, GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight()/2+100, textPaintBlack);
                        if(chef_head_bob_up){
                            if(chef_head_bob >= 10)
                                chef_head_bob_up = false;
                            chef_head_bob++;
                        }
                        else{
                            if(chef_head_bob <=-10)
                                chef_head_bob_up = true;
                            chef_head_bob--;
                        }
                    }
                    else{
                        canvas.drawText("Better luck next time", GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth()/2, GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight()/2+100, textPaintBlack);
                        if(chef_head_bob_up){
                            if(chef_head_bob >= 10)
                                chef_head_bob_up = false;
                            chef_head_bob++;
                        }
                        else{
                            if(chef_head_bob <=-10)
                                chef_head_bob_up = true;
                            chef_head_bob--;
                        }
                    }
                }
                else if(swaps <= 0)
                    canvas.drawText("Tap the correct box", GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth()/2, GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight()/2+100, textPaintBlack);
                else {
                    canvas.drawText("Pay attention to the pizza", GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth() / 2, GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight() / 2 + 100, textPaintBlack);
                }
                int speed = 30;
                for(guessBox box : guessBoxes) {
                    canvas.drawBitmap(box.spriteSheet, box.posX, box.posY, null);
                    if(box.posX != box.desiredX){
                        if(Math.abs(box.desiredX - box.posX) < speed){
                            box.posX = box.desiredX;
                        }
                        else if(box.desiredX > box.posX){
                            box.posX += speed;
                        }
                        else{
                            box.posX -= speed;
                        }
                    }
                    if(box.posY != box.desiredY) {
                        if(Math.abs(box.desiredY - box.posY) < speed){
                            box.posY = box.desiredY;
                        }
                        else if (box.desiredY > box.posY) {
                            box.posY += speed;
                        } else {
                            box.posY -= speed;
                        }
                    }
                }
                break;
            case GUESSING_INTO:
                canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);

                // PATRONS
                synchronized (patrons){
                    for(patron patron : patrons){
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+patron.patronSize/2, patron.patronPosition, null);
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
                canvas.drawText("Score :"+score, 10, 100, textScorePaint);

                canvas.drawRect(0,0,screenWidth,guessingIntroBlack,blackPaint);
                canvas.drawRect(0,screenHeight-guessingIntroBlack,screenWidth,screenHeight+1000,blackPaint);
                canvas.drawText("Level "+level+" Complete!",530, 500, textPaint);
                guessingIntroBlack+=7;
                if(guessingIntroBlack >= 2500){
                    clearEntities();
                    graceTimer = System.currentTimeMillis();
                    guessingIntroBlack = 0;
                    currentGameState = gameState.GUESSING;
                }
                break;
            case GUESSING_OUT:
                canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);
                canvas.drawBitmap(playerSprite, playerPosition*360-180-playerWidth/2, 1640, null);

                if(System.currentTimeMillis() - graceTimer < 3500){
                    if((System.currentTimeMillis() - graceTimer)%200 <= 4){
                        blink = !blink;
                    }
                    if(blink){
                        canvas.drawText("GET READY TO SERVE",500, 500, textPaint);
                    }
                }
                else{
                    gameLoop.patronSpawnRate = 5+level*5;
                    currentGameState = gameState.ACTIVE;
                }
                break;
        }

        holder.unlockCanvasAndPost(canvas); // Take the canvas and draw it
    }
    class guessBox{
        int id;
        int posX = -100;
        int posY = 1000;
        int desiredX = 0;
        int desiredY = 0;
        int posId;
        Bitmap spriteSheet = GameCharacters.PIZZABOX.getSpriteSheet();
        public guessBox(int id){
            this.id = id;
        }
    }

    public void update(double delta){
        long currentTime = SystemClock.elapsedRealtime();
        switch(currentGameState){
            case TUTORIAL:
            case ACTIVE:
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
                if(score >= Math.pow(level, 2)*1500+timesCorrectGuess*2000){
                    currentGameState = gameState.GUESSING_INTO;
                }
                break;
            case GUESSING:
                // currentTime is already updated every update();
                if(System.currentTimeMillis() - graceTimer > 4000 && guessAnswer == 5){
                    level++;
                    guessAnswer = -1;
                    guessCorrect = false;
                    swaps = 20;
                    guessingIntroFinished = false;
                    graceTimer = System.currentTimeMillis();
                    currentGameState = gameState.GUESSING_OUT;
                }
                else if(guessAnswer == 5){
                    boxAnimation();
                }
                else if(guessAnswer>=0){
                    synchronized (guessBoxes){
                        for(guessBox box : guessBoxes){
                            if(box.posId == guessAnswer && box.id == correctBox){
                                guessCorrect = true;
                                synchronized (points){
                                    timesCorrectGuess++;
                                    points.add(new point(500,700,2000));
                                }
                                break;
                            }
                        }
                    }
                    guessAnswer = 5;
                    graceTimer = System.currentTimeMillis();
                    introTimer = System.currentTimeMillis();
                    animTimer = System.currentTimeMillis();
                }
                else{
                    // Top Left
                    int x0 = screenWidth/5-guessBoxWidth/2;
                    int y0 = screenHeight/3+guessBoxWidth/2;
                    // Top Right
                    int x1 = screenWidth-screenWidth/5-guessBoxWidth/2;
                    int y1 = screenHeight/3+guessBoxWidth/2;
                    // Bottom Right
                    int x2 = screenWidth-screenWidth/5-guessBoxWidth/2;
                    int y2 = screenHeight/2+screenHeight/6+guessBoxWidth/2;
                    // Bottom Left
                    int x3 = screenWidth/5-guessBoxWidth/2;
                    int y3 = screenHeight/2+screenHeight/6+guessBoxWidth/2;
                    // Middle
                    int x4 = screenWidth/2-guessBoxWidth/2;
                    int y4 = screenHeight/2+guessBoxWidth/2;

                    // Updating desired positions based off of posID
                    synchronized (guessBoxes) {
                        for (guessBox box : guessBoxes) {
                            switch (box.posId) {
                                case 0:
                                    box.desiredX = x0;
                                    box.desiredY = y0;
                                    break;
                                case 1:
                                    box.desiredX = x1;
                                    box.desiredY = y1;
                                    break;
                                case 2:
                                    box.desiredX = x2;
                                    box.desiredY = y2;
                                    break;
                                case 3:
                                    box.desiredX = x3;
                                    box.desiredY = y3;
                                    break;
                                case 4:
                                    box.desiredX = x4;
                                    box.desiredY = y4;
                                    break;
                            }
                        }
                    }
                    if(!guessingIntroFinished){
                        // Set up
                        correctBox = random.nextInt(5); // The box with this ID will be correct
                        synchronized (guessBoxes) {
                            for (guessBox box : guessBoxes) {
                                box.spriteSheet = GameCharacters.PIZZABOX.getSpriteSheet();
                                switch (box.id) {
                                    case 0:
                                        box.desiredX = x0;
                                        box.desiredY = y0;
                                        box.posId = 0;
                                        break;
                                    case 1:
                                        box.desiredX = x1;
                                        box.desiredY = y1;
                                        box.posId = 1;
                                        break;
                                    case 2:
                                        box.desiredX = x2;
                                        box.desiredY = y2;
                                        box.posId = 2;
                                        break;
                                    case 3:
                                        box.desiredX = x3;
                                        box.desiredY = y3;
                                        box.posId = 3;
                                        break;
                                    case 4:
                                        box.desiredX = x4;
                                        box.desiredY = y4;
                                        box.posId = 4;
                                        break;
                                }
                            }
                            // Create guessing game intro
                            synchronized (guessBoxes) {
                                guessingIntroFinished = true;
                                for (guessBox box : guessBoxes) {
                                    if (box.posX != box.desiredX || box.posY != box.desiredY) {
                                        guessingIntroFinished = false;
                                        break;
                                    }
                                }
                            }
                        }
                        introTimer = System.currentTimeMillis();
                        animTimer = System.currentTimeMillis();
                    }
                    else{
                        if(System.currentTimeMillis() - introTimer < 4000){
                            boxAnimation();
                        }
                        else if(System.currentTimeMillis() - introTimer < 5500){
                            // Closing boxes
                            synchronized (guessBoxes) {
                                for (guessBox box : guessBoxes) {
                                    if (System.currentTimeMillis() - animTimer <= 4200) {
                                        if (box.id == correctBox)
                                            box.spriteSheet = GameCharacters.PIZZABOX_CORRECT_1.getSpriteSheet();
                                        else
                                            box.spriteSheet = GameCharacters.PIZZABOX_INCORRECT_1.getSpriteSheet();
                                    } else if (System.currentTimeMillis() - animTimer < 4250) {
                                        box.spriteSheet = GameCharacters.PIZZABOX.getSpriteSheet();
                                    }
                                }
                            }
                        }
                        else if(swaps > 0){
                            // Main game
                            if(System.currentTimeMillis() - swapTimer >= 300){
                                swapBoxes();
                                swapTimer = System.currentTimeMillis();
                                swaps--;
                            }
                        }
                        else{
                            // Guessing stage
                        }
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
                                currentGameState = gameState.ACTIVE;
                            else finishedTutorial = true;
                        }
                        break;
                }
                break;
            case MAIN_MENU:
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getX()>=screenWidth/5 && event.getX()<screenWidth-screenWidth/5){
                        // PLAY
                        if(event.getY()>1000 && event.getY()<1200)
                            currentGameState = gameState.ACTIVE;

                        // CHALLENGES


                        // TUTORIAL
                        if(event.getY()>1600 && event.getY()<1800) {
                            tutorialState = 0;
                            spawnPatron(-1);
                            currentGameState = gameState.TUTORIAL;
                        }
                    }
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
                    if(event.getX()>=screenWidth/5 && event.getX()<=screenWidth-screenWidth/5 && event.getY() >= 1700 && event.getY() <= 1900){
                        clearEntities();
                        gameLoop.patronSpawnRate = 10;
                        if(score > highScore)
                            highScore = score;
                        score = 0;
                        currentGameState = gameState.MAIN_MENU;
                    }
                }
                break;
            case GUESSING:
                if(swaps <= 0){
                    // Player will guess
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        float x = event.getX();
                        float y = event.getY();
                        // Top Left
                        int x0 = screenWidth/5-guessBoxWidth/2;
                        int y0 = screenHeight/3+guessBoxWidth/2;
                        // Top Right
                        int x1 = screenWidth-screenWidth/5-guessBoxWidth/2;
                        int y1 = screenHeight/3+guessBoxWidth/2;
                        // Bottom Right
                        int x2 = screenWidth-screenWidth/5-guessBoxWidth/2;
                        int y2 = screenHeight/2+screenHeight/6+guessBoxWidth/2;
                        // Bottom Left
                        int x3 = screenWidth/5-guessBoxWidth/2;
                        int y3 = screenHeight/2+screenHeight/6+guessBoxWidth/2;
                        // Middle
                        int x4 = screenWidth/2-guessBoxWidth/2;
                        int y4 = screenHeight/2+guessBoxWidth/2;

                        if(guessAnswer<0){
                            if(x>=x0 && x<=x0+guessBoxWidth && y>=y0 && y<=y0+guessBoxWidth){
                                guessAnswer = 0;
                                System.out.println("Answer: "+guessAnswer);
                            }
                            else if(x>=x1 && x<=x1+guessBoxWidth && y>=y1 && y<=y1+guessBoxWidth){
                                guessAnswer = 1;
                                System.out.println("Answer: "+guessAnswer);
                            }
                            else if(x>=x2 && x<=x2+guessBoxWidth && y>=y2 && y<=y2+guessBoxWidth){
                                guessAnswer = 2;
                                System.out.println("Answer: "+guessAnswer);
                            }
                            else if(x>=x3 && x<=x3+guessBoxWidth && y>=y3 && y<=y3+guessBoxWidth){
                                guessAnswer = 3;
                                System.out.println("Answer: "+guessAnswer);
                            }
                            else if(x>=x4 && x<=x4+guessBoxWidth && y>=y4 && y<=y4+guessBoxWidth) {
                                guessAnswer = 4;
                                System.out.println("Answer: " + guessAnswer);
                            }
                        }
                    }
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
    public class menuBG{
        int posY = -menuBGHeight;
        public menuBG(int posY){
            this.posY = posY;
        }
    }
    public void drawButton(String text, int posY, Paint colour, Paint textColour, Canvas canvas){
        int widthLeft = screenWidth/5;
        int widthRight = screenWidth - screenWidth/5;
        int height = 200;
        canvas.drawRect(widthLeft, posY, widthRight, posY+height, colour);
        canvas.drawText(text, widthLeft+(widthRight-widthLeft)/2, posY+125, textColour);
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
        if(pattern == -1){
            patrons.add(new patron(400, 2, GameCharacters.PATRON_WALK1.getSpriteSheet()));
        }
    }
    public void clearEntities(){
        synchronized (pizzas) {
            pizzas.clear();
        }
        synchronized (patrons) {
            patrons.clear();
        }
        synchronized (plates){
            plates.clear();
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
    public void swapBoxes(){
        int type = random.nextInt(4);
        switch(type){
            case 0:
                // Clockwise swap
                synchronized (guessBoxes){
                    for(guessBox box : guessBoxes){
                        if(box.posId != 4){
                            // Change desired positions
                            if(box.posId<3){
                                box.posId++;
                            }
                            else{
                                box.posId = 0;
                            }
                        }
                    }
                }
                break;
            case 1:
                // Counter-clockwise swap
                synchronized (guessBoxes){
                    for(guessBox box : guessBoxes){
                        if(box.posId != 4){
                            // Change desired positions
                            if(box.posId>0){
                                box.posId--;
                            }
                            else{
                                box.posId = 3;
                            }
                        }
                    }
                }
                break;
            case 2:
            case 3:
                // Middle swap
                boolean swap1 = false, swap2 = false;
                int target = random.nextInt(4);
                synchronized (guessBoxes){
                    for(guessBox box : guessBoxes){
                        if(box.posId == 4 && !swap1){
                            box.posId = target;
                            swap1 = true;
                        }
                        else if(box.posId == target && !swap2){
                            box.posId = 4;
                            swap2 = true;
                        }
                    }
                }
                break;
        }
    }
    public void boxAnimation(){
        // Opening boxes
        synchronized (guessBoxes) {
            for (guessBox box : guessBoxes) {
                if (System.currentTimeMillis() - animTimer < 250) {
                    if (box.id == correctBox)
                        box.spriteSheet = GameCharacters.PIZZABOX_CORRECT_1.getSpriteSheet();
                    else
                        box.spriteSheet = GameCharacters.PIZZABOX_INCORRECT_1.getSpriteSheet();
                } else if (System.currentTimeMillis() - animTimer < 500) {
                    if (box.id == correctBox)
                        box.spriteSheet = GameCharacters.PIZZABOX_CORRECT_2.getSpriteSheet();
                    else
                        box.spriteSheet = GameCharacters.PIZZABOX_INCORRECT_2.getSpriteSheet();
                }
            }
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
