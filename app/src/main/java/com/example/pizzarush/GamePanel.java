package com.example.pizzarush;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
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

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback{
    SharedPreferences save = getContext().getSharedPreferences("saveState",0);
    MediaPlayer bgMusicMain = MediaPlayer.create(MainActivity.getGameContext(), R.raw.shopping_list);
    MediaPlayer bgMusicGuess = MediaPlayer.create(MainActivity.getGameContext(), R.raw.quiet_saturday);
    final private Paint textPaint = new Paint();
    final private Paint titlePaint1 = new Paint();
    final private Paint titlePaint2 = new Paint();
    final private Paint titlePaintBorder = new Paint();
    final private Paint textPaintBlack = new Paint();
    final private Paint textPaintHighscore = new Paint();
    final private Paint banner = new Paint();
    final private Paint paintGrey = new Paint();
    final private Paint textPaintBorder = new Paint();
    final private Paint textPaintTransition = new Paint();
    final private Paint textPaintTransitionBorder = new Paint();
    final private Paint speechBubbleWhite = new Paint();
    final private Paint speechBubbleBorder = new Paint();
    final private Paint textScorePaint = new Paint();
    final private Paint textScoreLevel = new Paint();
    final private Paint tutorialGrey = new Paint();
    final private Paint blackPaint = new Paint();
    final private Paint menuBackgroundPaint1 = new Paint();
    final private Paint menuBackgroundPaint2 = new Paint();
    final private Paint paintRed = new Paint();
    final private Paint paintDarkerRed = new Paint();
    final private Paint paintBlue = new Paint();
    final private Paint paintDarkerBlue = new Paint();
    final private Paint paintYellow = new Paint();
    final private Paint paintGreen = new Paint();
    final private Paint paintDarkerGreen = new Paint();
    final private Paint paintDarkGrey = new Paint();
    final private Paint paintButtonGrey = new Paint();
    final private Paint buttonText = new Paint();
    private final SurfaceHolder holder;
    private final Random random = new Random();
    private final ArrayList<pizza> pizzas = new ArrayList<>();
    private final ArrayList<patron> patrons = new ArrayList<>();
    private final ArrayList<emptyPlate> plates = new ArrayList<>();
    private final ArrayList<point> points = new ArrayList<>();
    private final ArrayList<guessBox> guessBoxes = new ArrayList<>();
    private final ArrayList<menuBG> menuPar = new ArrayList<>();
    private final int menuBGHeight = GameCharacters.TITLE_BG.getSpriteSheetNoScale().getHeight();
    private int level = save.getInt("level",1);
    private int playerPosition = 2;
    private double playerAnimationState = 0;
    private Bitmap playerSprite = GameCharacters.PLAYER.getSpriteSheet();
    private int score = save.getInt("score", 0);
    int timesCorrectGuess = save.getInt("timesCorrectGuess",0);
    private int scoreToNextLevel = (int) Math.pow(level, 2)*1500+timesCorrectGuess*2000;
    private int prevScore = (int) Math.pow(level-1, 2)*1500+timesCorrectGuess*2000;
    private int highScore = save.getInt("highscore",0);
    private boolean hasMoved = false;
    final private int playerWidth = GameCharacters.PLAYER.getSpriteSheet().getWidth();
    final private int pizzaWidth = GameCharacters.PIZZA.getSpriteSheet().getWidth();
    final private int emptyPlateWidth = GameCharacters.PLATE.getSpriteSheet().getWidth();
    final private int guessBoxWidth = GameCharacters.PIZZABOX.getSpriteSheet().getWidth();
    private int danceTimer=0, danceIndex=0;
    final Object patronSleep = new Object();
    private final GameLoop gameLoop;
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
    float bezierCurve = -1;
    int chef_head_bob = 0;
    boolean chef_head_bob_up = true;
    int guessingIntroBlack = 0;
    boolean guessCorrect = false;
    boolean leftCheck = false;
    boolean rightCheck = false;
    int doubleCheck = 0;
    // My devices screen dimensions 1080x2097 Length*Width
    int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

    float responsiveOffsetX = (float) screenWidth/1080;
    float responsiveOffsetY = (float) screenHeight/2097;
    // smaller screen <1 | bigger screen >1

    private int tutorialIn = -screenWidth/2;

    public GamePanel(Context context) {
        super(context);



        bgMusicGuess.start();
        bgMusicMain.start();
        bgMusicGuess.setVolume(1f,1f);
        bgMusicGuess.seekTo(18000);

        holder = getHolder();
        holder.addCallback(this);

        titlePaint1.setColor(Color.RED);
        titlePaint1.setTextSize(250*responsiveOffsetX);
        titlePaint1.setTextAlign(Paint.Align.CENTER);
        titlePaint1.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint1.setStrokeWidth(5);
        titlePaint1.setLetterSpacing(0.1f);

        titlePaint2.setColor(Color.rgb(255,149,56));
        titlePaint2.setTextSize(250*responsiveOffsetX);
        titlePaint2.setTextAlign(Paint.Align.CENTER);
        titlePaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        titlePaint2.setStrokeWidth(5);
        titlePaint2.setLetterSpacing(0.1f);

        titlePaintBorder.setColor(Color.BLACK);
        titlePaintBorder.setTextSize(250*responsiveOffsetX);
        titlePaintBorder.setTextAlign(Paint.Align.CENTER);
        titlePaintBorder.setStyle(Paint.Style.STROKE);
        titlePaintBorder.setStrokeWidth(25);
        titlePaintBorder.setLetterSpacing(0.1f);

        speechBubbleWhite.setColor(Color.WHITE);

        speechBubbleBorder.setColor(Color.rgb(238, 237, 237));

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(75*responsiveOffsetX);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setStyle(Paint.Style.FILL);

        textPaintTransition.setTextAlign(Paint.Align.CENTER);
        textPaintTransition.setTextSize(100);
        textPaintTransition.setStyle(Paint.Style.FILL);
        textPaintTransition.setColor(Color.rgb(255,110,84));

        textPaintTransitionBorder.setTextAlign(Paint.Align.CENTER);
        textPaintTransitionBorder.setTextSize(100);
        textPaintTransitionBorder.setStyle(Paint.Style.STROKE);
        textPaintTransitionBorder.setStrokeWidth(10);
        textPaintTransitionBorder.setColor(Color.rgb(252,198, 30));

        textPaintBlack.setColor(Color.BLACK);
        textPaintBlack.setTextAlign(Paint.Align.CENTER);
        textPaintBlack.setStyle(Paint.Style.FILL);
        textPaintBlack.setTextSize(45);

        Paint textToMenu = new Paint();
        textToMenu.setColor(Color.WHITE);
        textToMenu.setTextAlign(Paint.Align.CENTER);
        textToMenu.setTextSize(100);

        banner.setColor(Color.argb(110,210,145,58));

        paintRed.setColor(Color.rgb(233,61,52));
        paintDarkerRed.setColor(Color.rgb(205,50,42));
        paintBlue.setColor(Color.rgb(65,131,255));
        paintDarkerBlue.setColor(Color.rgb(55,110,211));
        paintYellow.setColor(Color.rgb(247,153,38));
        paintGreen.setColor((Color.rgb(134, 244, 87)));
        paintDarkerGreen.setColor(Color.rgb(109,199,71));

        paintGrey.setColor(Color.LTGRAY);
        paintDarkGrey.setColor(Color.rgb(55,55,55));
        paintButtonGrey.setColor(Color.parseColor("#808080"));

        buttonText.setColor(Color.BLACK);
        buttonText.setTextAlign(Paint.Align.CENTER);
        buttonText.setTextSize(90*responsiveOffsetX);
        buttonText.setStyle(Paint.Style.FILL);

        textPaintHighscore.setColor(Color.BLACK);
        textPaintHighscore.setTextSize(75*responsiveOffsetY);
        textPaintHighscore.setTextAlign(Paint.Align.CENTER);
        textPaintHighscore.setStyle(Paint.Style.FILL);

        textPaintBorder.setStyle(Paint.Style.STROKE);
        textPaintBorder.setTextSize(75);
        textPaintBorder.setTextAlign(Paint.Align.CENTER);
        textPaintBorder.setStrokeWidth(12);
        textPaintBorder.setColor(Color.BLACK);

        textScorePaint.setColor(Color.WHITE);
        textScorePaint.setTextSize(75);
        textScorePaint.setTextAlign(Paint.Align.CENTER);
        textScorePaint.setStyle(Paint.Style.FILL);

        textScoreLevel.setColor(Color.WHITE);
        textScoreLevel.setTextSize(60);
        textScoreLevel.setTextAlign(Paint.Align.CENTER);
        textScoreLevel.setStyle(Paint.Style.FILL);

        tutorialGrey.setColor(Color.BLACK);
        tutorialGrey.setAlpha(125);
        System.out.println("ScreenHeight: "+responsiveOffsetY);
        System.out.println("ScreenWidth: "+responsiveOffsetX);

        menuBackgroundPaint1.setColor(Color.parseColor("#FFCF87"));
        menuBackgroundPaint2.setColor(Color.parseColor("#e8af5a"));
        menuBackgroundPaint2.setAlpha(75);

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
        // Extra in case of a large phone
        menuPar.add(new menuBG(menuBGHeight*5));

    }

    public void render(double delta){
        Canvas canvas = holder.lockCanvas();

        switch(currentGameState) {
            case TUTORIAL:
                drawFloor(canvas);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70*responsiveOffsetX, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430*responsiveOffsetX, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790*responsiveOffsetX, 0, null);
                switch(tutorialState){
                    case 0:
                        int chefPosX = tutorialIn-screenWidth/15;
                        if(chefPosX>0)
                            chefPosX=0;
                        tutorialIn+=8;
                        if(tutorialIn>=screenWidth/15)
                            tutorialState++;

                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);


                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) chefPosX, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) chefPosX, 100*responsiveOffsetY, null);
                        drawSpeechBubble("", screenHeight/2-(int) (270*responsiveOffsetY), tutorialIn,canvas);
                        break;
                    case 1:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("Oh, YOU'RE the new chef??", screenHeight/2-(int) (270*responsiveOffsetY),canvas);
                        break;

                    case 2:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("If I had to look down any lower my hat", "would fall off! I guess that doesn't matter", "if you can do the job.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);

                        break;
                    case 3:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("First things first, lets see how well", "you can throw a pizza.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);

                        break;
                    case 4:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        drawSpeechBubble("That patron over there looks quite hungry.", "We NEVER let our patrons be hungry.", screenHeight/15,canvas);
                    break;
                    case 5:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        drawSpeechBubble("Your job is simple.Just tap the screen", "anywhere to throw a pizza.", screenHeight/15,canvas);

                        break;
                    case 6:
                        // Throwing pizza
                        synchronized (pizzas) {
                            for(patron patron : patrons){
                                canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                            }
                            for(pizza pizza : pizzas){
                                canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),(pizza.pizzaAislePosition*360-180- (float) pizzaWidth /2)*responsiveOffsetX, pizza.pizzaPosition, null);
                                pizza.pizzaPosition -= (int) (delta * pizza.pizzaSpeed * 60);
                                if(pizza.pizzaPosition<=0)
                                    gameOver(0);
                            }
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        break;
                    case 7:
                        // Eating
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        synchronized (pizzas){
                            for(pizza pizza : pizzas){
                                canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),(pizza.pizzaAislePosition*360-180- (float) pizzaWidth /2)*responsiveOffsetX, pizza.pizzaPosition, null);
                                pizza.pizzaPosition -= (int) (delta * pizza.pizzaSpeed * 60);
                                if(pizza.pizzaPosition<=0)
                                    gameOver(0);
                            }
                        }
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        break;
                    case 8:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(GameCharacters.PATRON.getSpriteSheet(), 2*360-360+50, (int) (700*responsiveOffsetY), null);

                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        drawSpeechBubble("Good, but the work is not over yet", screenHeight/15,canvas);

                        break;
                    case 9:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(GameCharacters.PATRON.getSpriteSheet(), 2*360-360+50, (int) (700*responsiveOffsetY), null);

                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        drawSpeechBubble("The plates we use are of the finest quality,", "and we can't have you breaking them.", screenHeight/15,canvas);

                        break;
                    case 10:
                        for(patron patron : patrons){
                            canvas.drawBitmap(patron.spriteToRender,(patron.patronAisle*360-360+ (float) patron.patronSize /2)*responsiveOffsetX, patron.patronPosition, null);
                        }
                        canvas.drawBitmap(GameCharacters.PATRON.getSpriteSheet(), 2*360-360+50, (int) (700*responsiveOffsetY), null);

                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        drawSpeechBubble("Simply stand where the plate is going to be", "to catch it", screenHeight/15,canvas);

                        break;
                    case 11:
                        // Catch the plate
                        for(emptyPlate plate : plates){
                            canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),(plate.emptyPlateAisle*360-180- (float) emptyPlateWidth /2)*responsiveOffsetX, plate.emptyPlatePos, null);
                            plate.emptyPlatePos += (int) (delta * plate.emptyPlateSpeed * 60);
                            if(plate.emptyPlatePos>=1900)
                                gameOver(2);
                            canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        }
                        break;
                    case 12:
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("Not bad. Maybe you are cut out for this job.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);

                        break;
                    case 13:
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("I won't mince words, we are severely", "understaffed. You're going to have to manage"," three aisles on your own.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);
                        break;
                    case 14:
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("You can move between aisles by tap and","holding the screen. Now lets see you do it.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);

                        break;
                    case 15:
                        // Moving aisles
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        if(playerPosition == 1)
                            leftCheck = true;
                        if(playerPosition == 3)
                            rightCheck = true;
                        if(leftCheck && rightCheck){
                            doubleCheck++;
                            leftCheck = false;
                            rightCheck = false;
                        }
                        break;
                    case 16:
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("This concludes your unpaid training,","good work employee.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);

                        break;
                    case 17:
                        canvas.drawBitmap(playerSprite, (playerPosition*360-180- (float) playerWidth /2)*responsiveOffsetX, 1640, null);
                        canvas.drawRect(0, 0, screenWidth, screenHeight, tutorialGrey);
                        canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) 0, 100*responsiveOffsetY, null);
                        drawSpeechBubble("Your real shift starts right about now.", screenHeight/2-(int) (270*responsiveOffsetY),canvas);

                        break;
                }
                break;

            case MAIN_MENU:
                canvas.drawRect(0,0,screenWidth,screenHeight, menuBackgroundPaint1);

                canvas.drawRoundRect(0,500*responsiveOffsetY ,screenWidth,screenHeight+400, 400,400, menuBackgroundPaint2);
                canvas.drawRoundRect(0,1000*responsiveOffsetY,screenWidth,screenHeight+400, 400,400, menuBackgroundPaint2);
                canvas.drawRoundRect(0,1500*responsiveOffsetY,screenWidth,screenHeight+400, 400,400, menuBackgroundPaint2);

                // Pizzas
                ArrayList<menuBG> toRemoveSlides = new ArrayList<>();
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
                    }
                    menuPar.removeAll(toRemoveSlides);
                }
                // HighScore
                canvas.drawRect(0, (float) screenHeight /3,screenWidth, (float) screenHeight /3+100*responsiveOffsetY, banner);
                canvas.drawText("HighScore: "+highScore, (float) screenWidth/2, (float) screenHeight /3+75*responsiveOffsetY, textPaintHighscore);

                // Buttons
                if (score!=0){
                    drawButton("CONTINUE", (int) (1000*responsiveOffsetY), paintGreen, buttonText, paintDarkerGreen, canvas);
                    canvas.drawText("("+score+")", (float) screenWidth /2*responsiveOffsetX, 1175*responsiveOffsetY, textPaintBlack);
                }
                else
                    drawButton("CONTINUE", (int) (1000*responsiveOffsetY), paintGrey, buttonText, paintButtonGrey, canvas);

                drawButton("NEW GAME", (int) (1300*responsiveOffsetY), paintRed, buttonText, paintDarkerRed, canvas);
                drawButton("TUTORIAL", (int) (1600*responsiveOffsetY), paintBlue, buttonText, paintDarkerBlue, canvas);

                canvas.drawText("PIZZA", (float) screenWidth/2, 320*responsiveOffsetY, titlePaintBorder);
                canvas.drawText("PIZZA", (float) screenWidth/2, 320*responsiveOffsetY, titlePaint1);
                canvas.drawText("RUSH", (float) screenWidth/2, 550*responsiveOffsetY, titlePaintBorder);
                canvas.drawText("RUSH", (float) screenWidth/2, 550*responsiveOffsetY, titlePaint2);




                break;
            case ACTIVE:
                // canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                drawFloor(canvas);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);

                // PATRONS
                synchronized (patrons){
                    for(patron patron : patrons){
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+ (float) patron.patronSize /2, patron.patronPosition, null);
                        if(!patron.satisfied) {
                            int patronSpeed = 5;
                            patron.patronPosition += (int) (delta * patronSpeed * 60);
                            if (patron.patronPosition >= 1640)
                                gameOver(1);
                        }
                    }

                }
                // PLATES
                synchronized (plates){
                    for(emptyPlate plate : plates){
                        canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180- (float) emptyPlateWidth /2, plate.emptyPlatePos, null);
                        plate.emptyPlatePos += (int) (delta * plate.emptyPlateSpeed * 60);
                        if(plate.emptyPlatePos>=1900) // TEST VALUE (1800)
                            gameOver(2);
                    }
                }
                // PIZZAS
                synchronized (pizzas) {
                    for(pizza pizza : pizzas){
                        canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),pizza.pizzaAislePosition*360-180- (float) pizzaWidth /2, pizza.pizzaPosition, null);
                        pizza.pizzaPosition -= (int) (delta * pizza.pizzaSpeed * 60);
                        if(pizza.pizzaPosition<=0)
                            gameOver(0);
                    }
                }
                canvas.drawRect(25,50, screenWidth-25, 100, paintGrey);
                canvas.drawRect(25,50, (int)( (double) (score-prevScore)/(scoreToNextLevel-prevScore)*(screenWidth-50)+25), 100, paintYellow);

                // PLAYER
                canvas.drawBitmap(playerSprite, playerPosition*360-180- (float) playerWidth /2, 1640, null);
                // POINTS
                synchronized (points){
                    for(point point : points) {
                        canvas.drawText("+"+point.pointsAmt, point.posX, point.posY, textPaint);
                        point.posY-=2;
                        point.framesAlive++;
                    }
                }
                canvas.drawText("Score: "+score, (float) screenWidth /2, 175, textScorePaint);
                canvas.drawText("Level "+level, (float) screenWidth /2, 100, textScoreLevel);
                break;
            case GAME_OVER:
                // canvas.drawBitmap(GameCharacters.GAMEOVER_FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                drawFloorGameOver(canvas);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);

                // PATRONS
                synchronized (patrons){
                    for(patron patron : patrons){
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+ (float) patron.patronSize /2, patron.patronPosition, null);
                    }
                }
                // PLATES
                synchronized (plates){
                    for(emptyPlate plate : plates){
                        canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180- (float) emptyPlateWidth /2, plate.emptyPlatePos, null);
                    }
                }
                // PIZZAS
                synchronized (pizzas) {
                    for(pizza pizza : pizzas){
                        canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),pizza.pizzaAislePosition*360-180- (float) pizzaWidth /2, pizza.pizzaPosition, null);
                    }
                }
                // PLAYER
                canvas.drawBitmap(playerSprite, playerPosition*360-180- (float) playerWidth /2, 1640, null);

                canvas.drawBitmap((GameCharacters.GAMEOVER_TEXT.getSpriteSheetNoScale()),0,800, null);
                canvas.drawText(gameOverReason,540, 1100, textPaintBorder);
                canvas.drawText(gameOverReason,540, 1100, textPaint);
                drawButton("MENU", 1700, paintRed, buttonText, paintDarkerRed, canvas);
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
                canvas.drawRect(0,0,screenWidth,screenHeight,paintDarkGrey);
                canvas.drawBitmap(GameCharacters.CHEF_BODY.getSpriteSheet2xScale(), (float) screenWidth /2- (float) screenWidth /8, 0, null);
                canvas.drawBitmap(GameCharacters.CHEF_HEAD.getSpriteSheet2xScale(), (float) screenWidth /2- (float) screenWidth /8, chef_head_bob, null);
                canvas.drawRect(0, (float) screenHeight /3, screenWidth, (float) screenHeight /2+ (float) screenHeight /6+guessBoxWidth*2, paintGrey);
                canvas.drawBitmap(GameCharacters.SPEECH.getSpriteSheet2xScale(), 0, 100, null);
                if(guessAnswer == 5){
                    if(guessCorrect){
                        canvas.drawText("Correct! +2000 points!", (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth() /2, (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight() /2+100, textPaintBlack);
                    }
                    else{
                        canvas.drawText("Better luck next time", (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth() /2, (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight() /2+100, textPaintBlack);
                    }
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
                else if(swaps <= 0)
                    canvas.drawText("Tap the correct box", (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth() /2, (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight() /2+100, textPaintBlack);
                else {
                    canvas.drawText("Pay attention to the pizza", (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getWidth() / 2, (float) GameCharacters.SPEECH.getSpriteSheet2xScale().getHeight() / 2 + 100, textPaintBlack);
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
                // canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                drawFloor(canvas);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);

                // PATRONS
                synchronized (patrons){
                    for(patron patron : patrons){
                        canvas.drawBitmap(patron.spriteToRender,patron.patronAisle*360-360+ (float) patron.patronSize /2, patron.patronPosition, null);
                    }
                }
                // PLATES
                synchronized (plates){
                    for(emptyPlate plate : plates){
                        canvas.drawBitmap(GameCharacters.PLATE.getSpriteSheet(),plate.emptyPlateAisle*360-180- (float) emptyPlateWidth /2, plate.emptyPlatePos, null);
                    }
                }
                // PIZZAS
                synchronized (pizzas) {
                    for(pizza pizza : pizzas){
                        canvas.drawBitmap(GameCharacters.PIZZA.getSpriteSheet(),pizza.pizzaAislePosition*360-180- (float) pizzaWidth /2, pizza.pizzaPosition, null);
                    }
                }
                // PLAYER and OTHERS
                canvas.drawBitmap(playerSprite, playerPosition*360-180- (float) playerWidth /2, 1640, null);
                canvas.drawRect(25,50, screenWidth-25, 100, paintYellow);
                canvas.drawText("Level "+(level-1), (float) screenWidth /2, 100, textScoreLevel);
                canvas.drawText("Score: "+score, (float) screenWidth /2, 175, textScorePaint);

                // THE GOOD STUFF
                canvas.drawRect(0,0,screenWidth,guessingIntroBlack,blackPaint);
                canvas.drawRect(0,screenHeight-guessingIntroBlack,screenWidth,screenHeight+1000,blackPaint);

                int yPosition = guessingIntroBlack;
                if(yPosition>=screenHeight/2)
                    yPosition=screenHeight/2;

                canvas.drawText("Level "+(level-1)+" Complete!", (float) screenWidth /2, yPosition-50, textPaintTransitionBorder);
                canvas.drawText("Level "+(level-1)+" Complete!", (float) screenWidth /2, yPosition-50, textPaintTransition);

                // little dancing guy
                if(danceTimer>=100){
                    danceTimer=0;
                    danceIndex++;
                    if(danceIndex==5)
                        danceIndex=0;
                }
                switch(danceIndex){
                    case 0:
                        canvas.drawBitmap(GameCharacters.DANCE1.getSpriteSheetNoScale(), (float) screenWidth /2-240, screenHeight-yPosition, null);
                        break;
                    case 1:
                        canvas.drawBitmap(GameCharacters.DANCE2.getSpriteSheetNoScale(), (float) screenWidth /2-240, screenHeight-yPosition, null);
                        break;
                    case 2:
                        canvas.drawBitmap(GameCharacters.DANCE3.getSpriteSheetNoScale(), (float) screenWidth /2-240, screenHeight-yPosition, null);
                        break;
                    case 3:
                        canvas.drawBitmap(GameCharacters.DANCE4.getSpriteSheetNoScale(), (float) screenWidth /2-240, screenHeight-yPosition, null);
                        break;
                    case 4:
                        canvas.drawBitmap(GameCharacters.DANCE5.getSpriteSheetNoScale(), (float) screenWidth /2-240, screenHeight-yPosition, null);
                        break;
                }


                guessingIntroBlack+=7;
                danceTimer+=7;
                if(guessingIntroBlack >= 2500){
                    clearEntities();
                    graceTimer = System.currentTimeMillis();
                    guessingIntroBlack = 0;
                    bezierCurve = -1;
                    stopGameBGMusic();
                    currentGameState = gameState.GUESSING;
                }
                break;
            case GUESSING_OUT:
                // canvas.drawBitmap(GameCharacters.FLOOR.getSpriteSheetNoScale(), 0, 0, null);
                drawFloor(canvas);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 70, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 430, 0, null);
                canvas.drawBitmap(GameCharacters.COUNTER.getSpriteSheet(), 790, 0, null);
                canvas.drawBitmap(playerSprite, playerPosition*360-180- (float) playerWidth /2, 1640, null);

                if(System.currentTimeMillis() - graceTimer < 3000){
                    // Score, and progress bar
                    canvas.drawRect(25,50, screenWidth-25, 100, paintGrey);
                    canvas.drawText("Level "+level, (float) screenWidth /2, 100, textScoreLevel);
                    canvas.drawText("Score: "+score, (float) screenWidth /2, 175, textScorePaint);

                    // Bezier Curve
                    float placement;
                    placement = (float) (Math.pow(bezierCurve, 3)*screenWidth + screenWidth/2);
                    bezierCurve+= 0.01f;
                    canvas.drawText("READY TO SERVE",placement, 800, textPaintTransitionBorder);
                    canvas.drawText("READY TO SERVE",placement, 800, textPaintTransition);
                }
                else{
                    prevScore = (int) Math.pow(level-1, 2)*1500+timesCorrectGuess*2000;
                    scoreToNextLevel = (int) Math.pow(level, 2)*1500+timesCorrectGuess*2000;

                    gameLoop.patronSpawnRate = level*7;
                    currentGameState = gameState.ACTIVE;
                }
                break;
        }

        holder.unlockCanvasAndPost(canvas); // Take the canvas and draw it
    }


    public void update(double delta){
        long currentTime = SystemClock.elapsedRealtime();

        // Music control
        switch(currentGameState){
            case MAIN_MENU:
            case TUTORIAL:
            case ACTIVE:
            case GUESSING_INTO:
            case GUESSING_OUT:
                stopGuessBGMusic();
                playGameBGMusic();
                break;
            case GUESSING:
                playGuessBGMusic();
            case GAME_OVER:
                stopGameBGMusic();
                break;
        }

        switch(currentGameState){
            case TUTORIAL:
            case ACTIVE:
                List<pizza> toRemovePizza = new ArrayList<>();
                List<patron> toRemovePatron = new ArrayList<>();
                List<emptyPlate> toRemovePlate = new ArrayList<>();
                List<point> toRemovePoint = new ArrayList<>();
                synchronized (plates){
                    for(emptyPlate plate : plates){
                        if(plate.emptyPlatePos >=1300){ // TEST VALUE (1640)
                            if(plate.emptyPlateAisle == playerPosition){
                                toRemovePlate.add(plate);
                                playAudioPlateCollect();
                                tutorialState = 12; // Wait for player to catch plate
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
                                tutorialState = 8; // Waiting for patron to finish eating
                            } else
                                patron.satisfiedTimer--;
                        }
                    }
                }
                synchronized (pizzas) {
                    pizzas.removeAll(toRemovePizza);
                }
                synchronized (patrons) {
                    patrons.removeAll(toRemovePatron);
                }
                synchronized (plates) {
                    plates.removeAll(toRemovePlate);
                }
                synchronized (points) {
                    points.removeAll(toRemovePoint);
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
                if(score >= scoreToNextLevel){
                    SharedPreferences.Editor editor = save.edit();
                    prevScore = (int) Math.pow(level, 2)*1500+timesCorrectGuess*2000;
                    level++;
                    scoreToNextLevel = (int) Math.pow(level, 2)*1500+timesCorrectGuess*2000;
                    editor.putInt("scoreToNextLevel", scoreToNextLevel);
                    editor.putInt("level",level).apply();
                    currentGameState = gameState.GUESSING_INTO;
                }
                break;
            case GUESSING:
                // currentTime is already updated every update();
                if(System.currentTimeMillis() - graceTimer > 4000 && guessAnswer == 5){
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
                                    scoreToNextLevel+=2000;
                                    SharedPreferences.Editor editor = save.edit();
                                    editor.putInt("timesCorrectGuess", timesCorrectGuess).apply();
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
                    }
                }
        }

        render(delta);
    }
    private boolean isColliding(pizza pizza, patron patron) {
        if(!patron.satisfied) {
            if (patron.patronAisle == pizza.pizzaAislePosition) {
                return pizza.pizzaPosition < patron.patronPosition + patron.patronSize && pizza.pizzaPosition > patron.patronPosition;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(currentGameState){
            case TUTORIAL:
                switch(tutorialState){
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 16:
                        // case 7: found around line 760
                    case 8:
                    case 9:
                    case 10:
                        // case 11 around line 840
                    case 12:
                    case 13:
                    case 14:
                        if(event.getAction() == MotionEvent.ACTION_UP)
                            tutorialState++;
                        break;
                    case 6:
                        // Throw the pizza
                        if(event.getAction() == MotionEvent.ACTION_UP){
                            synchronized (pizzas) {
                                        pizzas.add(new pizza());
                            }
                            playAudioThrowPizza();
                            playerAnimationState = 3.0;
                            tutorialState = 7;
                        }
                        break;
                    case 15:
                        if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                            if(event.getX()<=360)
                                playerPosition=1;
                            else if(event.getX()<=720)
                                playerPosition=2;
                            else
                                playerPosition=3;
                        }
                        if(event.getAction() == MotionEvent.ACTION_UP && doubleCheck>=1){
                            doubleCheck=0;
                            tutorialState++;
                        }
                        break;
                    case 17:
                        if(event.getAction() == MotionEvent.ACTION_UP) {
                            currentGameState = gameState.ACTIVE;
                        }
                        break;
                }
                break;
            case MAIN_MENU:
                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(event.getX()>= (float) screenWidth /5 && event.getX()<screenWidth- (float) screenWidth /5){
                        // CONTINUE
                        if(event.getY()>1000*responsiveOffsetY && event.getY()<1200*responsiveOffsetY && score!=0) {
                            level = save.getInt("level", 1);
                            score = save.getInt("score", 0);
                            gameLoop.patronSpawnRate = level*7;
                            currentGameState = gameState.ACTIVE;
                        }

                        // NEW GAME
                        if(event.getY()>1400*responsiveOffsetY && event.getY()<1600*responsiveOffsetY){
                            gameOver(-1); // Wipes cache
                            level = 1;
                            score = 0;
                            currentGameState = gameState.ACTIVE;
                            gameLoop.patronSpawnRate = 7;
                        }

                        // TUTORIAL
                        else if(event.getY()>1600*responsiveOffsetY && event.getY()<1800*responsiveOffsetY) {
                            tutorialState = 0;
                            spawnPatron(-1);
                            playerPosition = 2;
                            tutorialIn = -screenWidth/2;
                            currentGameState = gameState.TUTORIAL;
                        }
                    }
                }
                break;
            case ACTIVE:
                // 0 = no position/failure | 1 = left position | 2 = mid position | 3 = right position
                if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    int newPosition;
                    if(event.getX()<=360) {
                        newPosition = 1;
                    }
                    else if(event.getX()<=720)
                        newPosition =2;
                    else
                        newPosition =3;
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
                    if(event.getX()>= (float) screenWidth /5 && event.getX()<=screenWidth- (float) screenWidth /5 && event.getY() >= 1700 && event.getY() <= 1900){
                        clearEntities();

                        if(score > highScore) {
                            highScore = score;
                            SharedPreferences.Editor editor = save.edit();
                            editor.putInt("highscore", score).apply();
                        }
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


    public void drawButton(String text, int posY, Paint colour, Paint textColour, Paint borderColour, Canvas canvas){
        int widthLeft = screenWidth/5;
        int widthRight = screenWidth - screenWidth/5;
        int height = (int) (200*responsiveOffsetY);
        int borderSize = (int) (15*responsiveOffsetX);

        canvas.drawRoundRect(widthLeft-borderSize, posY-borderSize, widthRight+borderSize, posY+height+borderSize, 50, 50, borderColour);
        canvas.drawRoundRect(widthLeft, posY, widthRight, posY+height, 50, 50, colour);
        canvas.drawText(text, widthLeft+ (float) (widthRight - widthLeft) /2, posY+125*responsiveOffsetY, textColour);
    }

    public void drawSpeechBubble(String text, int posY, Canvas canvas){
        int widthLeft = screenWidth/15;
        int widthRight = screenWidth - screenWidth/15;
        int height = (int) (300*responsiveOffsetY);
        int borderSize = (int) (15*responsiveOffsetX);

        canvas.drawRoundRect(widthLeft-borderSize, posY-borderSize, widthRight+borderSize, posY+height+borderSize, 50, 50, speechBubbleBorder);
        canvas.drawRoundRect(widthLeft, posY, widthRight, posY+height, 50, 50, speechBubbleWhite);
        canvas.drawText(text, widthLeft+ (float) (widthRight - widthLeft) /2, posY+125*responsiveOffsetY, textPaintBlack);
    }
    public void drawSpeechBubble(String text, int posY, int posX, Canvas canvas){
        int totalWidth = screenWidth - screenWidth*2/15;
        int height = (int) (300*responsiveOffsetY);
        int borderSize = (int) (15*responsiveOffsetX);

        canvas.drawRoundRect(posX-borderSize, posY-borderSize, totalWidth+posX+borderSize, posY+height+borderSize, 50, 50, speechBubbleBorder);
        canvas.drawRoundRect(posX, posY, totalWidth+posX, posY+height, 50, 50, speechBubbleWhite);
        canvas.drawText(text, posX, posY+125*responsiveOffsetY, textPaintBlack);
    }
    public void drawSpeechBubble(String text, String text2, int posY, Canvas canvas){
        int widthLeft = screenWidth/15;
        int widthRight = screenWidth - screenWidth/15;
        int height = (int) (300*responsiveOffsetY);
        int borderSize = (int) (15*responsiveOffsetX);

        canvas.drawRoundRect(widthLeft-borderSize, posY-borderSize, widthRight+borderSize, posY+height+borderSize, 50, 50, speechBubbleBorder);
        canvas.drawRoundRect(widthLeft, posY, widthRight, posY+height, 50, 50, speechBubbleWhite);
        canvas.drawText(text, widthLeft+ (float) (widthRight - widthLeft) /2, posY+125*responsiveOffsetY, textPaintBlack);
        canvas.drawText(text2, widthLeft+ (float) (widthRight - widthLeft) /2, posY+175*responsiveOffsetY, textPaintBlack);
    }
    public void drawSpeechBubble(String text, String text2, String text3, int posY, Canvas canvas){
        int widthLeft = screenWidth/15;
        int widthRight = screenWidth - screenWidth/15;
        int height = (int) (300*responsiveOffsetY);
        int borderSize = (int) (15*responsiveOffsetX);

        canvas.drawRoundRect(widthLeft-borderSize, posY-borderSize, widthRight+borderSize, posY+height+borderSize, 50, 50, speechBubbleBorder);
        canvas.drawRoundRect(widthLeft, posY, widthRight, posY+height, 50, 50, speechBubbleWhite);
        canvas.drawText(text, widthLeft+ (float) (widthRight - widthLeft) /2, posY+100*responsiveOffsetY, textPaintBlack);
        canvas.drawText(text2, widthLeft+ (float) (widthRight - widthLeft) /2, posY+175*responsiveOffsetY, textPaintBlack);
        canvas.drawText(text3, widthLeft+ (float) (widthRight - widthLeft) /2, posY+250*responsiveOffsetY, textPaintBlack);
    }
    public void spawnPatron(int pattern){
        if(currentGameState == gameState.ACTIVE) {
            // Making it so early levels are easier
            if(level==1)
                pattern = 0;
            else if(level==2)
                pattern = random.nextInt(2);


            // Spawn Patterns
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
            patrons.add(new patron((int) (700*responsiveOffsetY), 2, GameCharacters.PATRON_WALK1.getSpriteSheet()));
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
    public void gameOver(int reason){
        prevScore = 0;
        timesCorrectGuess=0;
        scoreToNextLevel = 1500;
        save.edit().remove("score").apply();
        save.edit().remove("level").apply();
        save.edit().remove("timesCorrectGuess").apply();
        System.out.println("Wiped cache data");

        if(reason!=-1) {
            playAudioGameOver();
            currentGameState = gameState.GAME_OVER;
        }

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

    public void drawFloor(Canvas canvas){
        int amount = 9; // Must be odd
        int side = screenWidth/amount;
        int counter=0;
        Paint paint1 = new Paint();
        Paint paint2 = new Paint();
        paint1.setColor(Color.rgb(206, 201, 198));
        paint2.setColor(Color.rgb(124, 122, 118));

        for(int i=0; side*i<screenHeight; i++){

            for(int j=0; j<amount; j++, counter++){
                if(counter%2==0)
                    canvas.drawRect(j*side,i*side,(j+1)*side,(i+1)*side, paint1);
                else
                    canvas.drawRect(j*side,i*side,(j+1)*side,(i+1)*side, paint2);
            }
        }
    }

    public void drawFloorGameOver(Canvas canvas){
        int amount = 9; // Must be odd
        int side = screenWidth/amount;
        int counter=0;
        Paint paint1 = new Paint();
        Paint paint2 = new Paint();
        paint1.setColor(Color.rgb(218, 103, 103));
        paint2.setColor(Color.rgb(179, 22, 22));

        for(int i=0; side*i<screenHeight; i++){

            for(int j=0; j<amount; j++, counter++){
                if(counter%2==0)
                    canvas.drawRect(j*side,i*side,(j+1)*side,(i+1)*side, paint1);
                else
                    canvas.drawRect(j*side,i*side,(j+1)*side,(i+1)*side, paint2);
            }
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

    public void playGuessBGMusic(){
        if(!bgMusicGuess.isPlaying()){
            bgMusicGuess.start();
        }
        if(!bgMusicGuess.isLooping()){
            bgMusicGuess.setLooping(true);
        }
    }

    public void stopGuessBGMusic(){
        if(bgMusicGuess.isPlaying()) {
            bgMusicGuess.pause();
            bgMusicGuess.seekTo(18000);
            bgMusicGuess.setLooping(false);
        }
    }
    public void playGameBGMusic(){
        if(!bgMusicMain.isPlaying()) {
            bgMusicMain.start();
        }

        if(!bgMusicMain.isLooping()){
            bgMusicMain.setLooping(true);
        }
    }

    public void stopGameBGMusic(){
        if(bgMusicMain.isPlaying()) {
            bgMusicMain.pause();
            bgMusicMain.seekTo(0);
            bgMusicMain.setLooping(false);
        }
    }
    // Classes
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

    public class pizza{
        int pizzaAislePosition = playerPosition; // X
        int pizzaSpeed = 10;
        int pizzaPosition = 1640; // Y
    }
    public class patron{
        private int patronPosition;
        private final int patronAisle;
        boolean satisfied = false;
        int satisfiedTimer = 2 * 60;
        private Bitmap spriteToRender;
        private long timer = SystemClock.elapsedRealtime();
        private final int patronSize = 100;
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
        private final int emptyPlateSpeed = 13;
        private final int emptyPlateAisle;
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
            SharedPreferences.Editor editor = save.edit();
            editor.putInt("score",score).apply();
        }
    }
    public class menuBG{
        int posY;
        public menuBG(int posY){
            this.posY = posY;
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