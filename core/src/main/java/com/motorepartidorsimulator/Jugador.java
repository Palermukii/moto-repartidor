package com.motorepartidorsimulator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Jugador {
    private Texture textura;
    private TextureRegion[] frames;
    private float velocidad = 200f;
    private Vector2 posicion;
    private float angulo;

    private float stateTime;
    private int frameActual;

    private Rectangle bounds;

    // Escala de unidades, debe coincidir con la de la clase Juego (1/64f)
    private static final float UNIT_SCALE = 1 / 64f; 

    public Jugador(String texturaPath, int frameWidth, int frameHeight, Vector2 posicionInicial) {
        try {
            textura = new Texture(Gdx.files.internal(texturaPath));

            // Dividir la textura en frames
            // Asume que los frames están en una sola fila horizontal
            TextureRegion[][] temp = TextureRegion.split(textura, frameWidth, frameHeight);

            // Suponiendo que tienes una fila, ponemos los frames de esa fila en un array 1D
            if (temp != null && temp.length > 0 && temp[0] != null) {
                int numFrames = temp[0].length;
                frames = new TextureRegion[numFrames];
                for (int i = 0; i < numFrames; i++) {
                    frames[i] = temp[0][i];
                }
            } else {
                Gdx.app.error("Jugador", "¡ERROR! La división de la textura no produjo ningún frame. Revisa frameWidth y frameHeight, y el formato de tu spritesheet.");
                frames = new TextureRegion[]{new TextureRegion(textura)}; // Usar la textura completa como fallback
            }

        } catch (Exception e) {
            Gdx.app.error("Jugador", "¡ERROR CRÍTICO! No se pudo cargar o dividir la textura del jugador: " + texturaPath, e);
            // Fallback: Si falla la carga, usa una textura por defecto para evitar un crash total
            textura = new Texture(Gdx.files.internal("badlogic.jpg")); // Asegúrate de tener badlogic.jpg en assets
            frames = new TextureRegion[]{new TextureRegion(textura)};
        }

        this.posicion = posicionInicial;
        this.angulo = 0;
        this.stateTime = 0f;
        this.frameActual = 0;

        // Inicializar el rectángulo de colisión con la posición y tamaño del frame
        // Asegúrate de que este tamaño sea el real de tu sprite
        this.bounds = new Rectangle(posicion.x, posicion.y, frameWidth, frameHeight);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        int framesPorSegundo = 10;
        if (frames != null && frames.length > 0) {
            frameActual = (int)(stateTime * framesPorSegundo) % frames.length;
        } else {
            frameActual = 0; // Si no hay frames, siempre usa el primero (o el fallback)
        }
        bounds.setPosition(posicion.x, posicion.y);
    }

    public void mover(float deltaX, float deltaY, float deltaTime) {
        // La lógica de movimiento real y colisiones se maneja en Juego.procesarMovimiento
    }

    public void setAngulo(float angulo) {
        this.angulo = angulo;
    }

    public Vector2 getPosicion() {
        return posicion;
    }

    public float getVelocidad() {
        return velocidad;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void dibujar(Batch batch) {
        if (frames != null && frames.length > 0) {
            // CORRECCIÓN AQUÍ: Escalar la posición y el tamaño del sprite al dibujarlo
            // para que coincida con las unidades de la cámara
            float drawX = posicion.x * UNIT_SCALE;
            float drawY = posicion.y * UNIT_SCALE;
            float drawWidth = frames[frameActual].getRegionWidth() * UNIT_SCALE;
            float drawHeight = frames[frameActual].getRegionHeight() * UNIT_SCALE;
            float originX = frames[frameActual].getRegionWidth()/2f * UNIT_SCALE;
            float originY = frames[frameActual].getRegionHeight()/2f * UNIT_SCALE;


            batch.draw(frames[frameActual], drawX, drawY, 
                originX, originY, // Centro para rotar
                drawWidth, drawHeight, 
                1f, 1f, angulo);
        } else {
            Gdx.app.error("Jugador", "No hay frames válidos para dibujar el jugador. ¿Textura cargada correctamente?");
            // Si no hay frames, al menos intenta dibujar la textura completa si se cargó
            if (textura != null) { 
                // CORRECCIÓN AQUÍ para el fallback también
                float drawX = posicion.x * UNIT_SCALE;
                float drawY = posicion.y * UNIT_SCALE;
                float drawWidth = textura.getWidth() * UNIT_SCALE;
                float drawHeight = textura.getHeight() * UNIT_SCALE;
                batch.draw(textura, drawX, drawY, drawWidth, drawHeight);
            }
        }
    }

    public void dispose() {
        if (textura != null) {
            textura.dispose();
        }
    }
}