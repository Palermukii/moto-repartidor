package com.motorepartidor.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
// ⛔️ quitado: import com.badlogic.gdx.audio.Music;
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
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.motorepartidor.Main;
import com.motorepartidor.entities.Jugador;
import com.motorepartidor.entities.components.PlayerController;
import com.motorepartidor.input.GameInputProcessor;
import com.motorepartidor.ui.HUD;

public class GameScreen implements Screen {

    private Main game;
    private boolean initialized = false;
    private String chosenSpritePath;
    private String chosenSpritePath2;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;

    private OrthographicCamera camera1;
    private Viewport viewport1;
    private OrthographicCamera camera2;
    private Viewport viewport2;

    private HUD hud;

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private MapLayer collisionLayer;
    private MapLayer gasLayer;

    // ⛔️ quitado: private Music backgroundMusic;

    private Jugador jugador1;
    private Jugador jugador2;

    private GameInputProcessor inputProcessor;

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
        // 1. Si ya inicializamos todo, solo reactivamos el Input y salimos.
        if (initialized) {
            Gdx.input.setInputProcessor(inputProcessor);
            // Opcional: reiniciar música si se detuvo en hide()
            try {
                if (game.getAudio() != null) {
                    game.getAudio().playMusic("audio/song.mp3", true, 0.1f);
                }
            } catch (Exception ignored) {}
            return;
        }

        // ESTO SOLO SE EJECUTA LA PRIMERA VEZ:

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        hud = new HUD();

        // === AUDIO: usar AudioManager global (reemplaza Music local) ===
        try {
            if (game.getAudio() != null) {
                game.getAudio().playMusic("audio/song.mp3", true, 0.1f);
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Error al reproducir música de juego: audio/song.mp3", e);
        }

        tiledMap = new TmxMapLoader().load("map/Map.tmx");
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, UNIT_SCALE);

        collisionLayer = tiledMap.getLayers().get("colisiones");
        if (collisionLayer == null) {
            Gdx.app.error("GameScreen", "¡ERROR! Capa 'colisiones' no encontrada en el mapa.");
        }

        gasLayer = tiledMap.getLayers().get("Gasolina");
        if (gasLayer == null) {
            Gdx.app.error("GameScreen", "¡ERROR! Capa 'Gasolina' no encontrada en el mapa.");
        }

        // Ahora se crea el Jugador después de que las capas del mapa están cargadas.
        jugador1 = new Jugador(chosenSpritePath, 18, 36, new Vector2(100, 3470), collisionLayer);
        jugador2 = new Jugador(chosenSpritePath2, 18, 36, new Vector2(6400, 1), collisionLayer);

        camera1 = new OrthographicCamera();
        viewport1 = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera1);

        camera2 = new OrthographicCamera();
        viewport2 = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera2);

        inputProcessor = new GameInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);

        // 2. Marcamos como inicializado para evitar que se ejecute de nuevo.
        initialized = true;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player1InGasArea = checkPlayerInGasArea(jugador1);
        player2InGasArea = checkPlayerInGasArea(jugador2);

        if (player1InGasArea && inputProcessor.isEPressed() && !eKeyHandled && jugador1.getDinero() >= 10) {
            jugador1.recargarGasolina(100);
            jugador1.restarDinero(10);
            eKeyHandled = true;
            // === AUDIO: SFX opcional de recarga
            if (game.getAudio() != null) game.getAudio().playSound("audio/refuel.wav", 1.0f);
            Gdx.app.log("GameScreen", "¡Jugador 1 recargó gasolina!");
        } else if (!inputProcessor.isEPressed()) {
            eKeyHandled = false;
        }

        if (player2InGasArea && inputProcessor.isPPressed() && !pKeyHandled && jugador2.getDinero() >= 10) {
            jugador2.recargarGasolina(100);
            jugador2.restarDinero(10);
            pKeyHandled = true;
            // === AUDIO: SFX opcional de recarga
            if (game.getAudio() != null) game.getAudio().playSound("audio/refuel.wav", 1.0f);
            Gdx.app.log("GameScreen", "¡Jugador 2 recargó gasolina!");
        } else if (!inputProcessor.isPPressed()) {
            pKeyHandled = false;
        }

        jugador1.update(new PlayerController.PlayerInput() {
            @Override public boolean accelerate() { return inputProcessor.isUpPressed(); }
            @Override public boolean brake() { return inputProcessor.isDownPressed(); }
            @Override public boolean turnLeft() { return inputProcessor.isLeftPressed(); }
            @Override public boolean turnRight() { return inputProcessor.isRightPressed(); }
        }, delta);

        jugador2.update(new PlayerController.PlayerInput() {
            @Override public boolean accelerate() { return inputProcessor.isArrowUpPressed(); }
            @Override public boolean brake() { return inputProcessor.isArrowDownPressed(); }
            @Override public boolean turnLeft() { return inputProcessor.isArrowLeftPressed(); }
            @Override public boolean turnRight() { return inputProcessor.isArrowRightPressed(); }
        }, delta);

        boolean p1CollidingWithObstacle = checkPolygonCollisions(jugador1.getPolygon());
        if (p1CollidingWithObstacle && !player1IsCollidingWithObstacle) {
            jugador1.restarVida(1);
            player1IsCollidingWithObstacle = true;
        } else if (!p1CollidingWithObstacle) {
            player1IsCollidingWithObstacle = false;
        }

        boolean p2CollidingWithObstacle = checkPolygonCollisions(jugador2.getPolygon());
        if (p2CollidingWithObstacle && !player2IsCollidingWithObstacle) {
            jugador2.restarVida(1);
            player2IsCollidingWithObstacle = true;
        } else if (!p2CollidingWithObstacle) {
            player2IsCollidingWithObstacle = false;
        }

        if (Intersector.overlapConvexPolygons(jugador1.getPolygon(), jugador2.getPolygon())) {
            if (!playersAreColliding) {
                Gdx.app.log("GameScreen", "¡Colisión entre jugadores detectada!");
                jugador1.restarVida(10);
                jugador2.restarVida(10);
                playersAreColliding = true;
            }
        } else {
            playersAreColliding = false;
        }

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

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f - 2, 0, 4, Gdx.graphics.getHeight());
        shapeRenderer.end();

        hud.render(jugador1, jugador2, player1InGasArea, player2InGasArea);
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new OptionsScreen(game,this));
        }
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
        // Opcional: podés parar la música al salir de la pantalla si querés:
        // if (game.getAudio() != null) game.getAudio().stopMusic();
    }

    @Override
    public void dispose() {
        batch.dispose();
        tiledMap.dispose();
        tiledMapRenderer.dispose();
        jugador1.dispose();
        jugador2.dispose();
        shapeRenderer.dispose();
        // ⛔️ quitado: no se dispone música aquí; la maneja el AudioManager global.
        hud.dispose();
    }
}
