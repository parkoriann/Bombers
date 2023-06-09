import gui.*;
import gui.components.Dialog;
import gui.window.GamePanel;
import gui.window.A_Panel;
import input_output.KeyHandler;
import input_output.Keys;
import objects.*;
import objects.game_elements.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for the gameplay screen. It contains the game loop.
 */
public class Gameplay {

    private static final int INITIAL_OBSTACLES = 5;
    private static final int VICTORY_SCORE = 150;

    final GamePanel panel;
    final KeyHandler keyHandler;
    final StateManager stateManager;

    Player player;
    Coin coin;
    Ammo ammo;
    List<Bomb> placedBombs;
    List<Obstacle> obstacles;
    Teleporter teleporter;
    Statistics statistics;

    Scoreboard scoreboard;
    Dialog dialog;

    //Constructor
    public Gameplay(A_Panel panel, KeyHandler keyHandler, StateManager stateManager) {
        this.panel = (GamePanel) panel;
        this.keyHandler = keyHandler;
        this.stateManager = stateManager;
        teleporter = null;
        statistics = new Statistics();
    }

    //Methods
    public void init() {
        player = new Player();
        coin = new Coin();
        ammo = new Ammo();
        placedBombs = new ArrayList<>();
        initObstacles();
        initTeleporter();
        statistics.reset();
        scoreboard = new Scoreboard(statistics);
        stateManager.setState(GameStates.RUNNING);
    }


    private void initTeleporter() {
        boolean randomBoolean = Math.random() < 0.5;
        if (randomBoolean) {
            teleporter = new Teleporter();
        } else {
            teleporter = null;
        }
    }

    private void initObstacles() {
        obstacles = new ArrayList<>();
        for (int i = 0; i < INITIAL_OBSTACLES; i++) {
            obstacles.add(new Obstacle());
        }
    }

    public void run() {

        long lastTick = System.currentTimeMillis();

        while (true) {

            long currentTick = System.currentTimeMillis();
            double diffSeconds = (currentTick - lastTick) / 1000.0;
            lastTick = currentTick;

            handleUserInput();
            update(diffSeconds);
            // get user input
            // do something with user input (update)
            panel.clear();
            drawElements();
            drawUI();
            panel.redraw();
            System.out.flush();

        }

    }

    private void drawUI() {
        panel.draw(scoreboard);

        if (dialog != null) {
            panel.draw(dialog);
        }
    }

    private void drawElements() {
        panel.draw(player);
        for (Obstacle obstacle : obstacles) {
            panel.draw(obstacle);
        }
        panel.draw(coin);
        panel.draw(ammo);
        if (teleporter != null) {
            panel.draw(teleporter);
        }
        for (Bomb bomb : placedBombs) {
            panel.draw(bomb);
        }
    }

    private void update(double diffSeconds) {

        player.move(diffSeconds);
        if (teleporter != null) {
            teleporter.move(diffSeconds);
        }
        checkCollisions();
        updateStats();

        if(statistics.getScore()>VICTORY_SCORE){
            victory();
        }


        // update the game state
        // check if the game is over
        // check if the game is won
    }

    private void updateStats() {

        if (stateManager.gameState == GameStates.RUNNING) {
            statistics.setObstaclesOnScreen(obstacles.size());
            statistics.updateTimer();
        }
    }

    private void checkCollisions() {

        //Crash with obstacles
        for (int i = 0; i < obstacles.size(); i++) {
            if (player.isColliding(obstacles.get(i))) {
                if (statistics.getBombsAccumulated() > 0) {
                    obstacles.remove(i);
                    statistics.reduceBombsAccumulated();
                    player.decreaseSpeed();
                } else
                    gameOver();
            }
        }

        //Get coin
        if (player.isColliding(coin)) {
            statistics.increaseLevel();
            statistics.increaseScore(coin.getValue());
            statistics.increaseCoinsCollected();
            addObstacles(2);
            initTeleporter();
            coin = new Coin();
            ammo = new Ammo();
            player.increaseSpeed();
        }

        //Get bomb
        if (player.isColliding(ammo)) {
            statistics.increaseBombsAccumulated();
            ammo = new Ammo();
            coin = new Coin();
            initTeleporter();
            player.increaseSpeed();
        }

        //Teleport
        if (teleporter != null) {
            if (player.isColliding(teleporter)) {
                statistics.increaseLevel();
                initTeleporter();
                coin = new Coin();
                ammo = new Ammo();

                for (int i = 0; i < 5; i++) {
                    obstacles.add(new Obstacle());
                }

                statistics.increaseTeleports();
                statistics.increaseScore(10);
                player.increaseSpeed();
            }
        }
    }

    /**
     * Adds a random number of obstacles to the screen based on the level of the player.
     */
    private void addObstacles(int limit) {
        int n = (int) (Math.random() * limit);

        for (int i = 0; i < n; i++) {
            obstacles.add(new Obstacle());
            if (obstacles.get(i).isColliding(player)) {
                obstacles.remove(i);
            }
        }
    }

    private void placeBomb() {
        statistics.reduceBombsAccumulated();
        double coordX = player.getCoordinates().topLeftCorner_x - player.getSize().x / 2 + Bomb.BOMB_RADIUS;
        double coordY = player.getCoordinates().topLeftCorner_y - player.getSize().y / 2 + Bomb.BOMB_RADIUS;

        Bomb newBomb = new Bomb(new Coordinates(coordX, coordY, Bomb.BOMB_RADIUS, Bomb.BOMB_RADIUS));
        placedBombs.add(newBomb);

        for (int j = 0; j < obstacles.size(); j++) {
            Obstacle obstacle = obstacles.get(j);
            if (newBomb.isInRadius(obstacle, Bomb.ACTION_RADIUS)) {
                obstacles.remove(obstacle);
                break;
            }
        }

        placedBombs.remove(newBomb);
//        placedBombs.get(placedBombs.size() - 1).fire();
    }

    private void pause() {
        statistics.pauseTimer();
        //TODO: pause the game, not only the state but timer and so on
        player.stopMovement();
        if (teleporter != null)
            teleporter.stopMovement();
        stateManager.setState(GameStates.PAUSED);
    }

    private void resume() {
        statistics.restartTimer();
        player.continueMovement();
        if (teleporter != null)
            teleporter.continueMovement();
        stateManager.setState(GameStates.RUNNING);
    }

    private void victory(){
        player.stopMovement();
        stateManager.setState(GameStates.FINISHED);

        dialog = new Dialog("You won bitch",
                "I hate you but at least you played the game. Fuck off!",
                new Size(550,450));
    }

    private void gameOver() {
        statistics.pauseTimer();
        player.stopMovement();
        if (teleporter != null)
            teleporter.stopMovement();
        stateManager.setState(GameStates.FINISHED);

        dialog = new EndDialog("Game Over", statistics.toString());
    }

    private void handleUserInput() {
        Keys keyPressed = keyHandler.getKeyPressed();
        Keys keyReleased = keyHandler.getKeyReleased();

        if (keyReleased != null) {
            if (keyReleased == Keys.PAUSE) {
                if (stateManager.gameState == GameStates.RUNNING) {
                    keyHandler.resetKeyReleased();
                    pause();
                } else if (stateManager.gameState == GameStates.PAUSED) {
                    keyHandler.resetKeyReleased();
                    resume();
                    //TODO add direction so the player continues moving
                }
            }

            if (keyReleased == Keys.SHOOT && stateManager.gameState == GameStates.FINISHED) {
                keyHandler.resetKeyReleased();
                dialog = null;
                init();
                stateManager.gameState = GameStates.RUNNING;
            }

            if (keyReleased == Keys.SHOOT && stateManager.gameState == GameStates.RUNNING) {
                keyHandler.resetKeyReleased();
                if (statistics.getBombsAccumulated() > 0) {
                    placeBomb();
                }
            }
        }

        if (stateManager.gameState == GameStates.PAUSED) {
            return;
        }

        switch (keyPressed) {
            case UP -> player.setDirectionMovement(Direction.UP);
            case DOWN -> player.setDirectionMovement(Direction.DOWN);
            case LEFT -> player.setDirectionMovement(Direction.LEFT);
            case RIGHT -> player.setDirectionMovement(Direction.RIGHT);
        }
    }
}
