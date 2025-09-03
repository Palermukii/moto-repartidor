package com.motorepartidor.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
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
    private Polygon polygon;

    private static final float UNIT_SCALE = 1 / 64f;

    private int vida;
    private static final int VIDA_MAXIMA = 100;

    // --- NUEVO: variables gasolina y dinero ---
    private float gasolina;
    private static final float GASOLINA_MAXIMA = 100;
    private int dinero;

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
                Gdx.app.error("Jugador", "¡ERROR! No se pudieron generar frames. Revisa frameWidth/frameHeight.");
                frames = new TextureRegion[]{new TextureRegion(textura)};
            }

        } catch (Exception e) {
            Gdx.app.error("Jugador", "¡ERROR CRÍTICO! No se pudo cargar textura: " + texturaPath, e);
            textura = new Texture(Gdx.files.internal("badlogic.jpg"));
            frames = new TextureRegion[]{new TextureRegion(textura)};
        }

        this.posicion = posicionInicial;
        this.angulo = 0;
        this.stateTime = 0f;
        this.frameActual = 0;

        this.vida = VIDA_MAXIMA;
        this.gasolina = GASOLINA_MAXIMA;
        this.dinero = 100;

        this.bounds = new Rectangle(posicion.x, posicion.y, frameWidth, frameHeight);

        // Polígono de colisión
        float[] vertices = new float[8];
        vertices[0] = 0;
        vertices[1] = 0;
        vertices[2] = frameWidth;
        vertices[3] = 0;
        vertices[4] = frameWidth;
        vertices[5] = frameHeight;
        vertices[6] = 0;
        vertices[7] = frameHeight;

        this.polygon = new Polygon(vertices);
        this.polygon.setOrigin(frameWidth / 2f, frameHeight / 2f);
        this.polygon.setPosition(posicionInicial.x, posicionInicial.y);
    }

    public void update(float deltaTime) {
        stateTime += deltaTime;
        int framesPorSegundo = 10;
        if (frames != null && frames.length > 0) {
            frameActual = (int)(stateTime * framesPorSegundo) % frames.length;
        } else {
            frameActual = 0;
        }

        polygon.setPosition(posicion.x, posicion.y);
        bounds.setPosition(posicion.x, posicion.y);
    }

    public void setAngulo(float angulo) {
        this.angulo = angulo;
    }

    public float getAngulo() {
        return this.angulo;
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

    public Polygon getPolygon() {
        return polygon;
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = Math.max(0, Math.min(vida, VIDA_MAXIMA));
    }

    public void restarVida(int cantidad) {
        this.vida = Math.max(0, this.vida - cantidad);
    }

    // --- NUEVOS MÉTODOS gasolina y dinero ---
    public float getGasolina() {
        return gasolina;
    }

    public void gastarGasolina(float cantidad) {
        gasolina -= cantidad;
        if (gasolina < 0) gasolina = 0;
    }

    public void recargarGasolina(float cantidad) {
        gasolina += cantidad;
        if (gasolina > GASOLINA_MAXIMA) gasolina = GASOLINA_MAXIMA;
    }

    public int getDinero() {
        return dinero;
    }

    public void setDinero(int dinero) {
        this.dinero = dinero;
    }

    public void sumarDinero(int cantidad) {
        this.dinero += cantidad;
    }

    public void restarDinero(int cantidad) {
        this.dinero -= cantidad;
        if (this.dinero < 0) this.dinero = 0;
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
                originX, originY,
                drawWidth, drawHeight,
                1f, 1f, angulo);
        } else {
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
