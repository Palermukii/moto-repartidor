package com.motorepartidorsimulator;

import com.badlogic.gdx.Game;
import com.motorepartidorsimulator.screens.MainMenuScreen;
import com.motorepartidorsimulator.screens.GameScreen;

/*
  Clase principal del juego que gestiona las diferentes pantallas (menús, juego, etc.).
 */
public class Juego extends Game {

    private MainMenuScreen mainMenuScreen;
    private GameScreen gameScreen;

    private static final String DEFAULT_SPRITE_PATH = "sprites/sprite.png";

    @Override
    public void create() {
        mainMenuScreen = new MainMenuScreen(this);
        setScreen(mainMenuScreen);
    }

    public void showMainMenuScreen() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }
        setScreen(mainMenuScreen);
    }

    public void showGameScreen() {
       
        if (gameScreen != null) {
            gameScreen.dispose(); 
        }
        gameScreen = new GameScreen(this, DEFAULT_SPRITE_PATH); 
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (mainMenuScreen != null) mainMenuScreen.dispose();
        if (gameScreen != null) gameScreen.dispose();
    }
}

