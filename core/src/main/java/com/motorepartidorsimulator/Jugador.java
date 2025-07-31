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

    private static final float UNIT_SCALE = 1 / 64f; 

    public Jugador(String texturaPath, int frameWidth, int frameHeight, Vector2 posicionInicial) {
        try {
            textura = new Texture(Gdx.files.internal(texturaPath));

            
            TextureRegion[][] temp = TextureRegion.split(textura, frameWidth, frameHeight);

            if (temp != null && temp.length > 0 && temp[0] != null) {
                int numFrames = temp[0].length;
                frames = new TextureRegion[numFrames];
                for (int i = 0; i < numFrames; i++) {
                    frames[i] = temp[0][i];
                }
            } else {
                Gdx.app.error("Jugador", "¡ERROR! La división de la textura no produjo ningún frame. Revisa frameWidth y frameHeight, y el formato de tu spritesheet.");
                frames = new TextureRegion[]{new TextureRegion(textura)};
            }

        } catch (Exception e) {
            Gdx.app.error("Jugador", "¡ERROR CRÍTICO! No se pudo cargar o dividir la textura del jugador: " + texturaPath, e);
            textura = new Texture(Gdx.files.internal("badlogic.jpg")); 
            frames = new TextureRegion[]{new TextureRegion(textura)};
        }

        this.posicion = posicionInicial;
        this.angulo = 0;
        this.stateTime = 0f;
        this.frameActual = 0;

   
        this.bounds = new Rectangle(posicion.x, posicion.y, frameWidth, frameHeight);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        int framesPorSegundo = 10;
        if (frames != null && frames.length > 0) {
            frameActual = (int)(stateTime * framesPorSegundo) % frames.length;
        } else {
            frameActual = 0;
        }
        bounds.setPosition(posicion.x, posicion.y);
    }

    public void mover(float deltaX, float deltaY, float deltaTime) {
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
            if (textura != null) { 
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
