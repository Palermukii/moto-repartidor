package com.motorepartidor;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.motorepartidor.audio.AudioManager;
import com.motorepartidor.screens.GameScreen;
import com.motorepartidor.screens.MainMenuScreen;

/*
  Clase principal del juego que gestiona las diferentes pantallas (menús, juego, etc.).
  Ahora mantiene un único AudioManager compartido.
*/
public class Main extends Game {

    private MainMenuScreen mainMenuScreen;
    private GameScreen gameScreen;

    private AudioManager audio; // AudioManager global




    @Override
    public void create() {
        // Inicializar el AudioManager una sola vez
        audio = new AudioManager();

        // Pantalla inicial
        mainMenuScreen = new MainMenuScreen(this, this.audio);
        setScreen(mainMenuScreen);
    }

    /** Acceso global al AudioManager. */
    public AudioManager getAudio() {
        return audio;
    }

    /** Vuelve al menú principal. */
    public void showMainMenu() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this, audio);
        }
        setScreen(mainMenuScreen);
    }

    /** Inicia una nueva partida. */
    public void startGame() {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        gameScreen = new GameScreen(this, audio);
        setScreen(gameScreen);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (mainMenuScreen != null) mainMenuScreen.dispose();
        if (gameScreen != null) gameScreen.dispose();

        // Liberar el audio global al cerrar el juego
        if (audio != null) {
            try {
                audio.dispose();
            } catch (Exception e) {
                Gdx.app.error("Main", "Error liberando AudioManager", e);
            }
        }
    }
}
