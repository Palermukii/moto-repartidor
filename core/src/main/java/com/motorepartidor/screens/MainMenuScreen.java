package com.motorepartidor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.motorepartidor.Main;
import com.badlogic.gdx.audio.Music; // Importar la clase Music

/**
 * Pantalla del menú principal con opciones para Jugar y Salir.
 */
public class MainMenuScreen implements Screen {

    private Main game;
    private Stage stage;
    private Skin skin;
    private Viewport viewport;
    private OrthographicCamera camera;
    private Music backgroundMusic; // Declarar el objeto Music

    public MainMenuScreen(Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
    }

    @Override
    public void show() {
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error al cargar el skin de UI. Asegúrate de que 'ui/uiskin.json' y sus archivos asociados existan en la carpeta 'assets'.", e);
            Gdx.app.exit();
            return;
        }

        // Cargar la música de fondo
        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/alejo.mp3")); // Ruta a tu archivo song.mp3
            backgroundMusic.setLooping(true); // Hacer que la música se repita
            backgroundMusic.setVolume(0.1f); // Ajustar el volumen (0.0 a 1.0)
            backgroundMusic.play(); // Reproducir la música
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error al cargar la música de fondo: audio/song.mp3", e);
        }


        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Título del juego - CAMBIADO: Usando la fuente "default" que sí está en uiskin.json
        Label titleLabel = new Label("Moto Repartidor Simulator", skin, "default", "white");
        table.add(titleLabel).padBottom(50).row();

        // Botón Jugar - CAMBIADO: Usando el estilo "default" que usa la fuente "font"
        TextButton playButton = new TextButton("Jugar", skin, "default");
        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.showGameScreen();
            }
        });
        table.add(playButton).width(200).height(50).padBottom(20).row();

        // Botón Salir - CAMBIADO: Usando el estilo "default" que usa la fuente "font"
        TextButton exitButton = new TextButton("Salir", skin, "default");
        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
        table.add(exitButton).width(200).height(50).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
        // Pausar la música si la aplicación pierde el foco
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    @Override
    public void resume() {
        // Reanudar la música si la aplicación recupera el foco
        if (backgroundMusic != null && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    @Override
    public void hide() {
        // Llamado cuando esta pantalla deja de ser la activa
        Gdx.input.setInputProcessor(null);
        // Detener la música cuando la pantalla se oculta
        if (backgroundMusic != null) {
            backgroundMusic.stop();
        }
    }

    @Override
    public void dispose() {
        // Liberar recursos cuando la pantalla ya no se necesita
        stage.dispose();
        if (skin != null) {
            skin.dispose();
        }
        // Liberar la música
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
    }
}
