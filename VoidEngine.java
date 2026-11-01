import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public final class VoidEngine extends JFrame implements Runnable {
    public static final int WIDTH = 960;
    public static final int HEIGHT = 720;

    private final Canvas canvas;
    private final BufferedImage screenBuffer;
    private final int[] pixels;

    private final Map map;
    private final Camera camera;
    private final RayCaster rayCaster;

    private boolean forward, backward, strafeLeft, strafeRight, turnLeft, turnRight, sprint;
    private boolean running = false;
    private boolean missionComplete = false;

    public VoidEngine() {
        setTitle("VoidCaster — Production Software Raycaster Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        canvas.setFocusable(false);
        add(canvas);
        pack();
        setLocationRelativeTo(null);

        screenBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) screenBuffer.getRaster().getDataBuffer()).getData();

        map = new Map();
        camera = new Camera(1.5, 1.5, 0.0, 1.0, 0.66);
        rayCaster = new RayCaster(WIDTH, HEIGHT);

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) { handleKey(e.getKeyCode(), true); }
            @Override
            public void keyReleased(KeyEvent e) { handleKey(e.getKeyCode(), false); }
        });
    }

    private void handleKey(int keyCode, boolean state) {
        switch (keyCode) {
            case KeyEvent.VK_W, KeyEvent.VK_UP -> forward = state;
            case KeyEvent.VK_S, KeyEvent.VK_DOWN -> backward = state;
            case KeyEvent.VK_A -> strafeLeft = state;
            case KeyEvent.VK_D -> strafeRight = state;
            case KeyEvent.VK_LEFT -> turnLeft = state;
            case KeyEvent.VK_RIGHT -> turnRight = state;
            case KeyEvent.VK_SHIFT -> {
                sprint = state;
                camera.setSprinting(sprint);
            }
        }
    }

    public synchronized void start() {
        canvas.createBufferStrategy(3);
        running = true;
        new Thread(this, "VoidCaster-Main").start();
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1_000_000_000.0 / 60.0;
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (delta >= 1.0) {
                update(1.0 / 60.0);
                delta--;
            }

            render();

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void update(double dt) {
        if (missionComplete) return;

        rayCaster.update(dt);

        if (forward) camera.moveForward(dt, map);
        if (backward) camera.moveBackward(dt, map);
        if (strafeLeft) camera.strafeLeft(dt, map);
        if (strafeRight) camera.strafeRight(dt, map);
        if (turnLeft) camera.rotate(-camera.rotSpeed * dt);
        if (turnRight) camera.rotate(camera.rotSpeed * dt);

        map.checkPickup(camera.posX, camera.posY);

        int curX = (int) camera.posX;
        int curY = (int) camera.posY;
        if (map.getTile(curX, curY) == 4 && map.getRemainingCores() == 0) {
            missionComplete = true;
        }
    }

    private void render() {
        BufferStrategy bs = canvas.getBufferStrategy();
        if (bs == null) return;

        rayCaster.render(pixels, camera, map);

        Graphics g = bs.getDrawGraphics();
        g.drawImage(screenBuffer, 0, 0, WIDTH, HEIGHT, null);
        drawTacticalHUD((Graphics2D) g);
        g.dispose();

        bs.show();
        Toolkit.getDefaultToolkit().sync();
    }

    private void drawTacticalHUD(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = WIDTH / 2;
        int cy = HEIGHT / 2;
        g.setColor(new Color(0, 245, 212, 180));
        g.drawLine(cx - 6, cy, cx + 6, cy);
        g.drawLine(cx, cy - 6, cx, cy + 6);
        g.drawOval(cx - 3, cy - 3, 6, 6);

        double nearestDist = 999.0;
        Map.CoreEntity nearestCore = null;
        for (Map.CoreEntity core : map.getCores()) {
            if (!core.collected) {
                double d = Math.hypot(core.x - camera.posX, core.y - camera.posY);
                if (d < nearestDist) {
                    nearestDist = d;
                    nearestCore = core;
                }
            }
        }

        g.setColor(new Color(15, 23, 42, 230));
        g.fillRoundRect(WIDTH / 2 - 250, 14, 500, 48, 8, 8);
        g.setColor(new Color(0, 245, 212, 120));
        g.drawRoundRect(WIDTH / 2 - 250, 14, 500, 48, 8, 8);

        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        int cores = map.getRemainingCores();

        if (cores > 0 && nearestCore != null) {
            g.setColor(new Color(255, 190, 11));
            g.drawString("CORES: [" + cores + " / 3]  NEAREST: " + String.format("%.1fm", nearestDist * 2.5), WIDTH / 2 - 225, 43);
        } else {
            double exitDist = Math.hypot(18.5 - camera.posX, 18.5 - camera.posY) * 2.5;
            g.setColor(new Color(0, 255, 136));
            g.drawString("ALL CORES SECURED -> EVAC AIRLOCK (" + String.format("%.1fm", exitDist) + ")", WIDTH / 2 - 225, 43);
        }

        int mapSize = 140;
        int mx = 16;
        int my = 16;

        g.setColor(new Color(10, 15, 26, 220));
        g.fillRoundRect(mx, my, mapSize, mapSize, 8, 8);
        g.setColor(new Color(0, 245, 212, 140));
        g.drawRoundRect(mx, my, mapSize, mapSize, 8, 8);

        int tileSize = mapSize / map.getWidth();
        for (int r = 0; r < map.getHeight(); r++) {
            for (int c = 0; c < map.getWidth(); c++) {
                int tile = map.getTile(c, r);
                if (tile == 1) {
                    g.setColor(new Color(30, 41, 59));
                    g.fillRect(mx + c * tileSize, my + r * tileSize, tileSize, tileSize);
                } else if (tile == 2) {
                    g.setColor(new Color(67, 40, 24));
                    g.fillRect(mx + c * tileSize, my + r * tileSize, tileSize, tileSize);
                } else if (tile == 3) {
                    g.setColor(new Color(100, 116, 139));
                    g.fillRect(mx + c * tileSize, my + r * tileSize, tileSize, tileSize);
                } else if (tile == 4) {
                    g.setColor(new Color(0, 255, 136));
                    g.fillRect(mx + c * tileSize, my + r * tileSize, tileSize, tileSize);
                }
            }
        }

        for (Map.CoreEntity core : map.getCores()) {
            if (!core.collected) {
                g.setColor(new Color(255, 190, 11));
                g.fillOval(mx + (int) (core.x * tileSize) - 2, my + (int) (core.y * tileSize) - 2, 5, 5);
            }
        }

        int px = mx + (int) (camera.posX * tileSize);
        int py = my + (int) (camera.posY * tileSize);

        g.setColor(new Color(0, 245, 212, 60));
        int coneLength = 16;
        int[] coneX = { px, (int) (px + (camera.dirX - camera.planeX) * coneLength), (int) (px + (camera.dirX + camera.planeX) * coneLength) };
        int[] coneY = { py, (int) (py + (camera.dirY - camera.planeY) * coneLength), (int) (py + (camera.dirY + camera.planeY) * coneLength) };
        g.fillPolygon(coneX, coneY, 3);

        g.setColor(Color.WHITE);
        g.fillOval(px - 2, py - 2, 5, 5);

        if (sprint) {
            g.setColor(new Color(0, 245, 212));
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            g.drawString("BOOST ACTIVE", mx, my + mapSize + 16);
        }

        if (missionComplete) {
            g.setColor(new Color(10, 15, 26, 240));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            g.setColor(new Color(0, 255, 136));
            g.setFont(new Font("Monospaced", Font.BOLD, 36));
            g.drawString("FACILITY EVACUATION COMPLETE", WIDTH / 2 - 320, HEIGHT / 2 - 20);

            g.setFont(new Font("Monospaced", Font.PLAIN, 18));
            g.setColor(Color.WHITE);
            g.drawString("All 3 Energy Cores retrieved. Sector evacuated successfully.", WIDTH / 2 - 330, HEIGHT / 2 + 25);
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            VoidEngine engine = new VoidEngine();
            engine.setVisible(true);
            engine.start();
        });
    }
}