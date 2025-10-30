package com.motorepartidor.screens;

import com.badlogic.gdx.Game;
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
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import com.motorepartidor.audio.AudioManager;
import com.motorepartidor.entities.Jugador;
import com.motorepartidor.entities.components.PlayerController;
import com.motorepartidor.input.GameInputProcessor;
import com.motorepartidor.ui.HUD;
import com.motorepartidor.ui.DeliveryIndicator;
import com.badlogic.gdx.graphics.Color;
import jdk.internal.org.jline.terminal.TerminalBuilder;

public class GameScreen implements Screen {

    private Game game;
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
    private MapLayer dealerLayer;
    private MapLayer entregasLayer;
    private final List<Rectangle> dealerAreas = new ArrayList<>();
    private final List<Rectangle> entregaAreas = new ArrayList<>();



    private AudioManager audio;
    private Jugador jugador1;
    private Jugador jugador2;

    private GameInputProcessor inputProcessor;

    private boolean eKeyHandled = false;
    private boolean pKeyHandled = false;

    private boolean playersAreColliding = false;
    private boolean player1IsCollidingWithObstacle = false;
    private boolean player2IsCollidingWithObstacle = false;
    private static final String DEFAULT_SPRITE_PATH = "sprites/sprite.png";
    private static final String DEFAULT_SPRITE_PATH2 = "sprites/sprite2.png";

    private boolean player1InGasArea = false;
    private boolean player2InGasArea = false;

    private DeliveryIndicator p1Indicator = new DeliveryIndicator();
    private DeliveryIndicator p2Indicator = new DeliveryIndicator();

    private static final float UNIT_SCALE = 1 / 64f;
    private static final float VIRTUAL_WIDTH = 20f;
    private static final float VIRTUAL_HEIGHT = 15f;

    // Estado por jugador
    private static class ActiveDelivery {
        Rectangle target;
        boolean dangerous;
        int reward;
    }
    private ActiveDelivery p1Delivery = null;
    private ActiveDelivery p2Delivery = null;

    // Flags de proximidad y control de “edge” de teclas
    private boolean p1NearDealer = false, p2NearDealer = false;
    private boolean p1NearDrop   = false, p2NearDrop   = false;
    private boolean gKeyHandled = false, lKeyHandled = false;

    private final Random rng = new Random();

    public GameScreen(Game game, AudioManager audio) {
        this.game = game;
        this.audio = audio;
        this.chosenSpritePath = DEFAULT_SPRITE_PATH;
        this.chosenSpritePath2 = DEFAULT_SPRITE_PATH2;
    }

    @Override
    public void show() {
        // 1. Si ya inicializamos todo, solo reactivamos el Input y salimos.
        if (initialized) {
            Gdx.input.setInputProcessor(inputProcessor);
            // Opcional: reiniciar música si se detuvo en hide()
            return;
        }

        // ESTO SOLO SE EJECUTA LA PRIMERA VEZ:

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        hud = new HUD();

        // === AUDIO: usar AudioManager global (reemplaza Music local) ===
        try {
            if (this.audio != null) {
                this.audio.playMusic("audio/song.mp3", true, 0.1f);
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


        dealerLayer = tiledMap.getLayers().get("dealer");
        if (dealerLayer == null) {
            Gdx.app.error("GameScreen", "¡ERROR! Capa 'dealer' no encontrada en el mapa.");
        } else {
            for (MapObject obj : dealerLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    dealerAreas.add(((RectangleMapObject) obj).getRectangle());
                }
            }
        }

        entregasLayer = tiledMap.getLayers().get("entregas");
        if (entregasLayer == null) {
            Gdx.app.error("GameScreen", "¡ERROR! Capa 'entregas' no encontrada en el mapa.");
        } else {
            for (MapObject obj : entregasLayer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    entregaAreas.add(((RectangleMapObject) obj).getRectangle());
                }
            }
        }

        p1Indicator.setColor(Color.CYAN);
        p2Indicator.setColor(Color.MAGENTA);

        // 2. Marcamos como inicializado para evitar que se ejecute de nuevo.
        initialized = true;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- GASOLINA / INPUT ---
        player1InGasArea = checkPlayerInGasArea(jugador1);
        player2InGasArea = checkPlayerInGasArea(jugador2);

        if (player1InGasArea && inputProcessor.isEPressed() && !eKeyHandled && jugador1.getDinero() >= 10) {
            int restaj1 = 100 - (int)jugador1.getGasolina();
            jugador1.restarDinero(restaj1);
            jugador1.recargarGasolina(100);
            eKeyHandled = true;
            if (this.audio != null) this.audio.playSound("audio/refuel.wav", 1.0f);
            Gdx.app.log("GameScreen", "¡Jugador 1 recargó gasolina!");
        } else if (!inputProcessor.isEPressed()) {
            eKeyHandled = false;
        }

        if (player2InGasArea && inputProcessor.isPPressed() && !pKeyHandled && jugador2.getDinero() >= 10) {
            jugador2.recargarGasolina(100);
            jugador2.restarDinero(10);
            pKeyHandled = true;
            if (this.audio != null) this.audio.playSound("audio/refuel.wav", 1.0f);
            Gdx.app.log("GameScreen", "¡Jugador 2 recargó gasolina!");
        } else if (!inputProcessor.isPPressed()) {
            pKeyHandled = false;
        }

        // --- PROXIMIDAD A ZONAS ---
        p1NearDealer = isInAny(jugador1, dealerAreas);
        p2NearDealer = isInAny(jugador2, dealerAreas);
        p1NearDrop   = (p1Delivery != null) && jugador1.getBounds().overlaps(p1Delivery.target);
        p2NearDrop   = (p2Delivery != null) && jugador2.getBounds().overlaps(p2Delivery.target);

        // --- ACEPTAR / ENTREGAR: JUGADOR 1 (G) ---
        if (inputProcessor.isGPressed()) {
            if (!gKeyHandled) {
                if (p1Delivery == null && p1NearDealer) {
                    p1Delivery = createDelivery();
                    if (p1Delivery != null) {
                        if (this.audio != null) this.audio.playSound("audio/pickup.wav", 1f);
                        Gdx.app.log("GameScreen", p1Delivery.dangerous ? "P1 tomó pedido PELIGROSO" : "P1 tomó pedido");
                    }
                } else if (p1Delivery != null && p1NearDrop) {
                    jugador1.sumarDinero(p1Delivery.reward);
                    if (this.audio != null) this.audio.playSound("audio/deliver.wav", 1f);
                    Gdx.app.log("GameScreen", "P1 entregó pedido. +$" + p1Delivery.reward);
                    p1Delivery = null;
                }
                gKeyHandled = true;
            }
        } else {
            gKeyHandled = false;
        }

        // --- ACEPTAR / ENTREGAR: JUGADOR 2 (L) ---
        if (inputProcessor.isLPressed()) {
            if (!lKeyHandled) {
                if (p2Delivery == null && p2NearDealer) {
                    p2Delivery = createDelivery();
                    if (p2Delivery != null) {
                        if (this.audio != null) this.audio.playSound("audio/pickup.wav", 1f);
                        Gdx.app.log("GameScreen", p2Delivery.dangerous ? "P2 tomó pedido PELIGROSO" : "P2 tomó pedido");
                    }
                } else if (p2Delivery != null && p2NearDrop) {
                    jugador2.sumarDinero(p2Delivery.reward);
                    if (this.audio != null) this.audio.playSound("audio/deliver.wav", 1f);
                    Gdx.app.log("GameScreen", "P2 entregó pedido. +$" + p2Delivery.reward);
                    p2Delivery = null;
                }
                lKeyHandled = true;
            }
        } else {
            lKeyHandled = false;
        }

        // Centro del destino -> ESCALADO a mundo
        if (p1Delivery != null && p1Delivery.target != null) {
            float cx = (p1Delivery.target.x + p1Delivery.target.width  * 0.5f) * UNIT_SCALE;
            float cy = (p1Delivery.target.y + p1Delivery.target.height * 0.5f) * UNIT_SCALE;
            p1Indicator.setTarget(cx, cy);
        } else {
            p1Indicator.clearTarget();
        }

        if (p2Delivery != null && p2Delivery.target != null) {
            float cx = (p2Delivery.target.x + p2Delivery.target.width  * 0.5f) * UNIT_SCALE;
            float cy = (p2Delivery.target.y + p2Delivery.target.height * 0.5f) * UNIT_SCALE;
            p2Indicator.setTarget(cx, cy);
        } else {
            p2Indicator.clearTarget();
        }

        // --- UPDATE JUGADORES ---
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

        // --- COLISIONES / DAÑO ---
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

        // --- RENDER SPLIT-SCREEN ---
        int halfW = Gdx.graphics.getWidth() / 2;
        int h = Gdx.graphics.getHeight();

        // ===== VIEWPORT IZQUIERDO (P1) =====
        Gdx.gl.glViewport(0, 0, halfW, h);

        // cámara 1 sigue a P1
        camera1.position.set(
            jugador1.getPosicion().x * UNIT_SCALE,
            jugador1.getPosicion().y * UNIT_SCALE,
            0f
        );
        camera1.update();

        tiledMapRenderer.setView(camera1);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camera1.combined);
        batch.begin();
        // si querés solo P1 acá, podés dejar solo jugador1
        jugador1.dibujar(batch);
        jugador2.dibujar(batch);
        batch.end();

        // Indicador P1 en su mitad
        float p1x = (jugador1.getBounds().x + jugador1.getBounds().width  * 1.5f) * UNIT_SCALE;
        float p1y = (jugador1.getBounds().y + jugador1.getBounds().height * 1.5f) * UNIT_SCALE;
        p1Indicator.renderWorld(p1x, p1y, camera1, delta);




        // ===== VIEWPORT DERECHO (P2) =====
        Gdx.gl.glViewport(halfW, 0, halfW, h);

        // cámara 2 sigue a P2
        camera2.position.set(
            jugador2.getPosicion().x * UNIT_SCALE,
            jugador2.getPosicion().y * UNIT_SCALE,
            0f
        );
        camera2.update();

        tiledMapRenderer.setView(camera2);
        tiledMapRenderer.render();

        batch.setProjectionMatrix(camera2.combined);
        batch.begin();
        // si querés solo P2 acá, podés dejar solo jugador2
        jugador1.dibujar(batch);
        jugador2.dibujar(batch);
        batch.end();

        // Indicador P2 en su mitad
        float p2x = (jugador2.getBounds().x + jugador2.getBounds().width  * 1.5f) * UNIT_SCALE;
        float p2y = (jugador2.getBounds().y + jugador2.getBounds().height * 1.5f) * UNIT_SCALE;
        p2Indicator.renderWorld(p2x, p2y, camera2, delta);

        // ===== VOLVER A PANTALLA COMPLETA (UI / DIVISOR) =====
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Línea roja central
        shapeRenderer.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(Gdx.graphics.getWidth() / 2f - 2, 0, 4, Gdx.graphics.getHeight());
        shapeRenderer.end();

        // --- HUD ---
        hud.setP1NearDealer(p1NearDealer);
        hud.setP2NearDealer(p2NearDealer);
        hud.setP1NearDrop(p1NearDrop);
        hud.setP2NearDrop(p2NearDrop);
        hud.setDeliveryStatus1(
            (p1Delivery == null) ? "Pedido: ninguno"
                : (p1Delivery.dangerous ? "Pedido: PELIGROSO $" + p1Delivery.reward
                : "Pedido: Normal $" + p1Delivery.reward)
        );
        hud.setDeliveryStatus2(
            (p2Delivery == null) ? "Pedido: ninguno"
                : (p2Delivery.dangerous ? "Pedido: PELIGROSO $" + p2Delivery.reward
                : "Pedido: Normal $" + p2Delivery.reward)
        );
        hud.render(jugador1, jugador2, player1InGasArea, player2InGasArea);

        // --- ESC → Options ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new OptionsScreen(game, this, audio));
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

    private boolean isInAny(Jugador jugador, List<Rectangle> zonas) {
        Rectangle b = jugador.getBounds();
        for (Rectangle r : zonas) {
            if (b.overlaps(r)) return true;
        }
        return false;
    }

    private Rectangle randomEntrega() {
        if (entregaAreas.isEmpty()) return null;
        return entregaAreas.get(rng.nextInt(entregaAreas.size()));
    }

    private ActiveDelivery createDelivery() {
        ActiveDelivery d = new ActiveDelivery();
        d.target = randomEntrega();
        if (d.target == null) return null;
        d.dangerous = rng.nextFloat() < 0.25f; // 25% peligroso
        d.reward = d.dangerous ? 60 : 30;
        return d;
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
        p1Indicator.dispose();
        p2Indicator.dispose();
        hud.dispose();
    }
}
