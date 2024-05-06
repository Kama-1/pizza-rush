package com.example.pizzarush;

import java.util.Random;

public class GameLoop implements Runnable{
    Random random = new Random();
    private final Thread gameThread;
    private final GamePanel gamePanel;
    public int patronSpawnRate = 5; // 1 min 50 max (50+ is the same rate as 50)
    private int increaseDifficulty = 0;
    private int failedSpawns = 0;

    public GameLoop(GamePanel gamePanel){
        gameThread = new Thread(this);
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {

        long lastFPScheck = System.currentTimeMillis();
        int fps = 0;

        long lastDelta = System.nanoTime();
        long nanoSec = 1_000_000_000;

        long patronSpawnTimer = System.currentTimeMillis();
        while(true){

            // FPS Tracker
            long nowDelta = System.nanoTime();
            long timeSinceLastDelta = nowDelta - lastDelta;
            double delta = (double) timeSinceLastDelta/nanoSec;

            gamePanel.update(delta);
            lastDelta = nowDelta;
            fps++;

            long now = System.currentTimeMillis();
            if(now - lastFPScheck >= 1000){
                System.out.println("FPS: "+fps);
                fps = 0;
                lastFPScheck+=1000;
            }

            // Spawn Patrons
            if(now - patronSpawnTimer >= 1000){
                int movementOpp = random.nextInt(50)+1;
                if(movementOpp <= patronSpawnRate || failedSpawns >= 4){
                    failedSpawns = 0;
                    if(random.nextInt(2) == 0) {
                        // Regular spawn
                        gamePanel.spawnPatron(0);
                    }
                    else {
                        // Pattern spawn
                        int spawnPattern = random.nextInt(2)+1; // Should be (3)+1 but I am omitting the 3rd pattern because it is really hard
                        gamePanel.spawnPatron(spawnPattern);
                    }
                }
                else
                    failedSpawns++;
                patronSpawnTimer +=1000;
                increaseDifficulty++;
                if(increaseDifficulty >= 10){
                    increaseDifficulty = 0;
                    patronSpawnRate++;
                    System.out.println("Current spawn-rate: "+patronSpawnRate);
                }
            }
        }
    }

    public void gameLoopStart(){
        gameThread.start();
    }
}