package com.motorepartidor;

import com.badlogic.gdx.Game;
import com.motorepartidor.screens.MainMenuScreen;
import com.motorepartidor.screens.GameScreen;

/*
  Clase principal del juego que gestiona las diferentes pantallas (menús, juego, etc.).
 */
public class Main extends Game {

    private MainMenuScreen mainMenuScreen;
    private GameScreen gameScreen;

    private static final String DEFAULT_SPRITE_PATH = "sprites/sprite.png";
    private static final String DEFAULT_SPRITE_PATH2 = "sprites/sprite2.png";

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
        gameScreen = new GameScreen(this, DEFAULT_SPRITE_PATH, DEFAULT_SPRITE_PATH2);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (mainMenuScreen != null) mainMenuScreen.dispose();
        if (gameScreen != null) gameScreen.dispose();
    }
}

