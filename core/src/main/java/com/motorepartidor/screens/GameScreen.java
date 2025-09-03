package com.motorepartidor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.motorepartidor.Main;
import com.motorepartidor.entities.Jugador;
import com.motorepartidor.input.GameInputProcessor;
import com.motorepartidor.ui.HUD;

public class GameScreen implements Screen {

    // --- VARIABLES ---
    private Main game;
    private String chosenSpritePath;
    private String chosenSpritePath2;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private OrthographicCamera camera1;
    private Viewport viewport1;
    private OrthographicCamera camera2;
    private Viewport viewport2;

    // HUD
    private HUD hud;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private MapLayer collisionLayer;
    private MapLayer gasLayer;

    private Music backgroundMusic;

    private Jugador jugador1;
    private Jugador jugador2;

    private GameInputProcessor inputProcessor;

    // Variables de estado para evitar la compra repetida
    private boolean eKeyHandled = false;
    private boolean pKeyHandled = false;

    private boolean playersAreColliding = false;
    private boolean player1IsCollidingWithObstacle = false;
    private boolean player2IsCollidingWithObstacle = false;

    private boolean player1InGasArea = false;
    private boolean player2InGasArea = false;

    private static final float UNIT_SCALE = 1 / 64f;
    private static final float VIRTUAL_WIDTH = 20f;
    private static final float VIRTUAL_HEIGHT = 15f;

    public GameScreen(Main game, String chosenSpritePath, String chosenSpritePath2) {
        this.game = game;
        this.chosenSpritePath = chosenSpritePath;
        this.chosenSpritePath2 = chosenSpritePath2;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Inicializa el HUD
        hud = new HUD();

        try {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/song.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.1f);
            backgroundMusic.play();
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Error al cargar la música de fondo: audio/song.mp3", e);
        }

        tiledMap = new TmxMapLoader().load("map/Map.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        camera1 = new OrthographicCamera();
        viewport1 = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera1);

        camera2 = new OrthographicCamera();
        viewport2 = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera2);

        jugador1 = new Jugador(chosenSpritePath, 18, 36, new Vector2(1 , 3600));
        jugador2 = new Jugador(chosenSpritePath2, 18, 36, new Vector2(6400, 1));

        collisionLayer = tiledMap.getLayers().get("colisiones");
        if (collisionLayer == null) {
            Gdx.app.error("GameScreen", "¡ERROR! Capa 'colisiones' no encontrada en el mapa.");
        }

        gasLayer = tiledMap.getLayers().get("Gasolina");
        if (gasLayer == null) {
            Gdx.app.error("GameScreen", "¡ERROR! Capa 'Gasolina' no encontrada en el mapa.");
        }

        inputProcessor = new GameInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player1InGasArea = checkPlayerInGasArea(jugador1);
        player2InGasArea = checkPlayerInGasArea(jugador2);

        // Recargar gasolina si se cumplen las condiciones
        if (player1InGasArea && inputProcessor.isEPressed() && !eKeyHandled && jugador1.getDinero() >= 10) {
            jugador1.recargarGasolina(100);
            jugador1.restarDinero(10);
            eKeyHandled = true;
            Gdx.app.log("GameScreen", "¡Jugador 1 recargó gasolina!");
        } else if (!inputProcessor.isEPressed()) {
            eKeyHandled = false;
        }

        if (player2InGasArea && inputProcessor.isPPressed() && !pKeyHandled && jugador2.getDinero() >= 10) {
            jugador2.recargarGasolina(100);
            jugador2.restarDinero(10);
            pKeyHandled = true;
            Gdx.app.log("GameScreen", "¡Jugador 2 recargó gasolina!");
        } else if (!inputProcessor.isPPressed()) {
            pKeyHandled = false;
        }

        procesarMovimientoJugador(jugador1, inputProcessor.isUpPressed(), inputProcessor.isDownPressed(), inputProcessor.isLeftPressed(), inputProcessor.isRightPressed(), delta);
        procesarMovimientoJugador(jugador2, inputProcessor.isArrowUpPressed(), inputProcessor.isArrowDownPressed(), inputProcessor.isArrowLeftPressed(), inputProcessor.isArrowRightPressed(), delta);

        // --- Colisión entre jugadores ---
        if (jugador1.getBounds().overlaps(jugador2.getBounds())) {
            if (!playersAreColliding) {
                Gdx.app.log("GameScreen", "¡Colisión entre jugadores detectada!");
                jugador1.restarVida(10);
                jugador2.restarVida(10);
                playersAreColliding = true;
            }
        } else {
            playersAreColliding = false;
        }

        // --- Vista jugador 1 (izquierda) ---
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
        camera1.position.set(jugador1.getPosicion().x * UNIT_SCALE, jugador1.getPosicion().y * UNIT_SCALE, 0);
        camera1.update();
        tiledMapRenderer.setView(camera1);
        tiledMapRenderer.render();
        batch.setProjectionMatrix(camera1.combined);
        batch.begin();
        jugador1.dibujar(batch);
        jugador2.dibujar(batch);
        batch.end();

        // --- Vista jugador 2 (derecha) ---
        Gdx.gl.glViewport(Gdx.graphics.getWidth() / 2, 0, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight());
        camera2.position.set(jugador2.getPosicion().x * UNIT_SCALE, jugador2.getPosicion().y * UNIT_SCALE, 0);
        camera2.update();
        tiledMapRenderer.setView(camera2);
        tiledMapRenderer.render();
        batch.setProjectionMatrix(camera2.combined);
        batch.begin();
        jugador1.dibujar(batch);
        jugador2.dibujar(batch);
        batch.end();

        // --- Línea divisoria ---
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f - 2, 0, 4, Gdx.graphics.getHeight());
        shapeRenderer.end();

        // Llama al método de renderizado de la clase HUD
        hud.render(jugador1, jugador2, player1InGasArea, player2InGasArea);
    }

    private void procesarMovimientoJugador(Jugador jugador, boolean up, boolean down, boolean left, boolean right, float deltaTime) {
        float dx = 0, dy = 0;
        boolean isMoving = false;

        // Determinar la velocidad de movimiento
        float velocidadActual = jugador.getVelocidad();
        if (jugador.getGasolina() <= 0) {
            velocidadActual = velocidadActual / 10f;
        }

        if (up) {
            dy += 1;
            isMoving = true;
        }
        if (down) {
            dy -= 1;
            isMoving = true;
        }
        if (left) {
            dx -= 1;
            isMoving = true;
        }
        if (right) {
            dx += 1;
            isMoving = true;
        }

        if (isMoving) {
            float consumoGasolina = 2f * deltaTime;
            jugador.gastarGasolina(consumoGasolina);
        }

        if (dx != 0 || dy != 0) {
            float oldX = jugador.getPosicion().x;
            float oldY = jugador.getPosicion().y;
            float moveAmountX = dx * velocidadActual * deltaTime;
            float moveAmountY = dy * velocidadActual * deltaTime;

            double anguloRad = Math.atan2(dy, dx);
            float anguloGrados = (float) Math.toDegrees(anguloRad);
            anguloGrados -= 90;
            if (anguloGrados < 0) anguloGrados += 360;
            jugador.setAngulo(anguloGrados);

            jugador.getPosicion().x += moveAmountX;
            jugador.getPolygon().setPosition(jugador.getPosicion().x, jugador.getPosicion().y);
            jugador.getPolygon().setRotation(jugador.getAngulo());

            boolean isCollidingX = checkPolygonCollisions(jugador.getPolygon());
            if (isCollidingX) {
                if ((jugador == jugador1 && !player1IsCollidingWithObstacle) || (jugador == jugador2 && !player2IsCollidingWithObstacle)) {
                    jugador.restarVida(1);
                    if (jugador == jugador1) player1IsCollidingWithObstacle = true;
                    else player2IsCollidingWithObstacle = true;
                }
                jugador.getPosicion().x = oldX;
                jugador.getPolygon().setPosition(oldX, jugador.getPosicion().y);
            }

            jugador.getPosicion().y += moveAmountY;
            jugador.getPolygon().setPosition(jugador.getPosicion().x, jugador.getPosicion().y);
            jugador.getPolygon().setRotation(jugador.getAngulo());

            boolean isCollidingY = checkPolygonCollisions(jugador.getPolygon());
            if (isCollidingY) {
                if ((jugador == jugador1 && !player1IsCollidingWithObstacle) || (jugador == jugador2 && !player2IsCollidingWithObstacle)) {
                    jugador.restarVida(1);
                    if (jugador == jugador1) player1IsCollidingWithObstacle = true;
                    else player2IsCollidingWithObstacle = true;
                }
                jugador.getPosicion().y = oldY;
                jugador.getPolygon().setPosition(jugador.getPosicion().x, oldY);
            }

            if (!isCollidingX && !isCollidingY) {
                if (jugador == jugador1) player1IsCollidingWithObstacle = false;
                else player2IsCollidingWithObstacle = false;
            }
        }
        jugador.update(deltaTime);
    }

    private boolean checkPlayerInGasArea(Jugador jugador) {
        if (gasLayer == null) return false;

        for (MapObject object : gasLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle gasRect = ((RectangleMapObject) object).getRectangle();
                if (jugador.getBounds().overlaps(gasRect)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkPolygonCollisions(Polygon polygon) {
        if (collisionLayer == null) return false;

        float[] playerVertices = polygon.getTransformedVertices();
        for (MapObject object : collisionLayer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle collisionRect = ((RectangleMapObject) object).getRectangle();

                if (!polygon.getBoundingRectangle().overlaps(collisionRect)) continue;

                for (int i = 0; i < playerVertices.length; i += 2) {
                    if (collisionRect.contains(playerVertices[i], playerVertices[i + 1])) return true;
                }

                float rectX = collisionRect.x;
                float rectY = collisionRect.y;
                float rectWidth = collisionRect.width;
                float rectHeight = collisionRect.height;

                if (polygon.contains(rectX, rectY) ||
                    polygon.contains(rectX + rectWidth, rectY) ||
                    polygon.contains(rectX + rectWidth, rectY + rectHeight) ||
                    polygon.contains(rectX, rectY + rectHeight)) return true;
            }
        }
        return false;
    }

    @Override
    public void resize(int width, int height) {
        viewport1.update(width / 2, height, false);
        viewport2.update(width / 2, height, false);
        hud.resize(width, height);
    }

    @Override public void pause() { }
    @Override public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        batch.dispose();
        tiledMap.dispose();
        tiledMapRenderer.dispose();
        jugador1.dispose();
        jugador2.dispose();
        shapeRenderer.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();
        // Libera los recursos del HUD
        hud.dispose();
    }
}
