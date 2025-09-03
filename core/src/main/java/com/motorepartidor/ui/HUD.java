package com.motorepartidor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.motorepartidor.entities.Jugador;

public class HUD {

    private OrthographicCamera hudCamera;
    private SpriteBatch hudBatch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    public HUD() {
        hudBatch = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        shapeRenderer = new ShapeRenderer();
    }

    /**
     * Dibuja y actualiza el HUD en la pantalla.
     * @param jugador1 El objeto Jugador 1 con los datos a mostrar.
     * @param jugador2 El objeto Jugador 2 con los datos a mostrar.
     * @param player1InGasArea Estado de si el jugador 1 está en el área de gasolina.
     * @param player2InGasArea Estado de si el jugador 2 está en el área de gasolina.
     */
    public void render(Jugador jugador1, Jugador jugador2, boolean player1InGasArea, boolean player2InGasArea) {
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();

        // Información del Jugador 1
        font.draw(hudBatch, "Vida: " + jugador1.getVida(), 20, Gdx.graphics.getHeight() - 20);
        font.draw(hudBatch, "Gasolina: " + (int)jugador1.getGasolina(), 20, Gdx.graphics.getHeight() - 40);
        font.draw(hudBatch, "Dinero: $" + jugador1.getDinero(), 20, Gdx.graphics.getHeight() - 60);
        if (player1InGasArea) font.draw(hudBatch, "[E] Cargar nafta ($10)", 20, Gdx.graphics.getHeight() - 130);

        // Información del Jugador 2
        font.draw(hudBatch, "Vida: " + jugador2.getVida(), Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 20);
        font.draw(hudBatch, "Gasolina: " + (int)jugador2.getGasolina(), Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 40);
        font.draw(hudBatch, "Dinero: $" + jugador2.getDinero(), Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 60);
        if (player2InGasArea) font.draw(hudBatch, "[P] Cargar nafta ($10)", Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 130);

        hudBatch.end();

        // Barras de vida y gasolina
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Jugador 1
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(20, Gdx.graphics.getHeight() - 90, 100, 15);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(20, Gdx.graphics.getHeight() - 90, jugador1.getVida(), 15);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(20, Gdx.graphics.getHeight() - 110, 100, 15);
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.rect(20, Gdx.graphics.getHeight() - 110, jugador1.getGasolina(), 15);

        // Jugador 2
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 90, 100, 15);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 90, jugador2.getVida(), 15);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 110, 100, 15);
        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f + 20, Gdx.graphics.getHeight() - 110, jugador2.getGasolina(), 15);

        shapeRenderer.end();
    }

    /**
     * Actualiza la cámara del HUD cuando la ventana cambia de tamaño.
     * @param width El nuevo ancho de la ventana.
     * @param height La nueva altura de la ventana.
     */
    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
    }

    /**
     * Libera los recursos del HUD.
     */
    public void dispose() {
        hudBatch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
