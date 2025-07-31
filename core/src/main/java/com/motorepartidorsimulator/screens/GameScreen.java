package com.motorepartidorsimulator.screens; // Cambiado el paquete a screens

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.motorepartidorsimulator.Juego; // Importa Juego (la nueva clase Game)
import com.motorepartidorsimulator.Jugador; // Asegúrate de que Jugador esté en el paquete correcto

public class GameScreen implements Screen, InputProcessor {

    private Juego game;
    private String chosenSpritePath;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Viewport viewport;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private MapObjects colisionObjetos;

    private Jugador jugador;
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    private static final float UNIT_SCALE = 1 / 64f; 
    private static final float VIRTUAL_WIDTH = 20f; 
    private static final float VIRTUAL_HEIGHT = 15f; 

    public GameScreen(Juego game, String chosenSpritePath) {
        this.game = game;
        this.chosenSpritePath = chosenSpritePath;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        tiledMap = new TmxMapLoader().load("map/p.tmx"); 
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply(); 

        jugador = new Jugador(chosenSpritePath, 18, 36, new Vector2(5 * 64, 5 * 64)); 
        


        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        procesarMovimiento(delta);

        camera.position.x = jugador.getPosicion().x * UNIT_SCALE;
        camera.position.y = jugador.getPosicion().y * UNIT_SCALE;
        camera.update();

        tiledMapRenderer.setView(camera); 
        tiledMapRenderer.render(); 

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        jugador.dibujar(batch); 
        batch.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1); 
        Rectangle playerBoundsScaled = new Rectangle(
            jugador.getBounds().x * UNIT_SCALE, 
            jugador.getBounds().y * UNIT_SCALE, 
            jugador.getBounds().width * UNIT_SCALE, 
            jugador.getBounds().height * UNIT_SCALE
        );
        shapeRenderer.rect(playerBoundsScaled.x, playerBoundsScaled.y, playerBoundsScaled.width, playerBoundsScaled.height);

        shapeRenderer.setColor(0, 1, 0, 1); 
        if (colisionObjetos != null) {
            for (MapObject object : colisionObjetos) {
                if (object instanceof RectangleMapObject) {
                    Rectangle collisionRect = ((RectangleMapObject) object).getRectangle();
                    shapeRenderer.rect(
                        collisionRect.x * UNIT_SCALE, 
                        collisionRect.y * UNIT_SCALE, 
                        collisionRect.width * UNIT_SCALE, 
                        collisionRect.height * UNIT_SCALE
                    );
                }
            }
        }
        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        tiledMap.dispose(); 
        tiledMapRenderer.dispose(); 
        jugador.dispose(); 
        shapeRenderer.dispose(); 
    }

    private void procesarMovimiento(float deltaTime) {
        float dx = 0, dy = 0;

        if (upPressed) {
            dy += 1;
        }
        if (downPressed) {
            dy -= 1;
        }
        if (leftPressed) {
            dx -= 1;
        }
        if (rightPressed) {
            dx += 1;
        }

        if (dx != 0 || dy != 0) {
            float oldX = jugador.getPosicion().x;
            float oldY = jugador.getPosicion().y;

            float moveAmountX = dx * jugador.getVelocidad() * deltaTime;
            float moveAmountY = dy * jugador.getVelocidad() * deltaTime;

            jugador.getPosicion().x += moveAmountX;
            jugador.getBounds().setPosition(jugador.getPosicion().x, jugador.getPosicion().y);
            if (checkCollisions()) {
                jugador.getPosicion().x = oldX; 
                jugador.getBounds().setPosition(jugador.getPosicion().x, jugador.getPosicion().y); 
            }

            jugador.getPosicion().y += moveAmountY;
            jugador.getBounds().setPosition(jugador.getPosicion().x, jugador.getPosicion().y);
            if (checkCollisions()) {
                jugador.getPosicion().y = oldY; 
                jugador.getBounds().setPosition(jugador.getPosicion().x, jugador.getPosicion().y); 
            }

            double anguloRad = Math.atan2(dy, dx);
            float anguloGrados = (float) Math.toDegrees(anguloRad);
            anguloGrados -= 90; 
            if (anguloGrados < 0) anguloGrados += 360;
            jugador.setAngulo(anguloGrados);
        }
        
        jugador.update(deltaTime); 
    }

    private boolean checkCollisions() {
        if (colisionObjetos == null || colisionObjetos.getCount() == 0) {
            return false; 
        }

        Rectangle jugadorBounds = jugador.getBounds();

        for (MapObject object : colisionObjetos) {
            if (object instanceof RectangleMapObject) {
                Rectangle collisionRect = ((RectangleMapObject) object).getRectangle();
                if (jugadorBounds.overlaps(collisionRect)) {
                    return true; 
                }
            }
        }
        return false; 
    }

    @Override
    public boolean keyDown(int keycode) {
        switch(keycode) {
            case Input.Keys.W: upPressed = true; break;
            case Input.Keys.S: downPressed = true; break;
            case Input.Keys.A: leftPressed = true; break;
            case Input.Keys.D: rightPressed = true; break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch(keycode) {
            case Input.Keys.W: upPressed = false; break;
            case Input.Keys.S: downPressed = false; break;
            case Input.Keys.A: leftPressed = false; break;
            case Input.Keys.D: rightPressed = false; break;
        }
        return true;
    }

    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}