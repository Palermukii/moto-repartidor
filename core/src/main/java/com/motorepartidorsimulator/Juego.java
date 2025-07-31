package com.motorepartidorsimulator;

import com.badlogic.gdx.Game;
import com.motorepartidorsimulator.screens.MainMenuScreen;
import com.motorepartidorsimulator.screens.GameScreen;

/**
 * Clase principal del juego que gestiona las diferentes pantallas (menús, juego, etc.).
 */
public class Juego extends Game {

    private MainMenuScreen mainMenuScreen;
    private GameScreen gameScreen;

    // Ruta del sprite por defecto cuando se inicia el juego directamente
    private static final String DEFAULT_SPRITE_PATH = "sprites/sprite.png";

    @Override
    public void create() {
        // Al iniciar el juego, mostramos la pantalla del menú principal
        mainMenuScreen = new MainMenuScreen(this);
        setScreen(mainMenuScreen);
    }

    // Métodos para cambiar de pantalla
    public void showMainMenuScreen() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }
        setScreen(mainMenuScreen);
    }

    public void showGameScreen() {
        // Creamos una nueva instancia de GameScreen cada vez que se inicia un juego
        // para asegurar un estado limpio.
        if (gameScreen != null) {
            gameScreen.dispose(); // Libera los recursos de la pantalla de juego anterior si existe
        }
        gameScreen = new GameScreen(this, DEFAULT_SPRITE_PATH); // Usa el sprite por defecto
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (mainMenuScreen != null) mainMenuScreen.dispose();
        if (gameScreen != null) gameScreen.dispose();
    }
}

