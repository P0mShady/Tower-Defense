import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;


//launches game window

public class TowerDefense extends JFrame {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TowerDefense().setVisible(true));
    }

    public TowerDefense() {
        setTitle("Tower Defense");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        //title screen
        StartScreen screen = new StartScreen(this);
        add(screen);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * called by start screen once difficulty is chosen
     * swaps start screen for game panel
     */
    public void startGame(Difficulty difficulty) {
        getContentPane().removeAll();
        GamePanel panel = new GamePanel(difficulty);
        add(panel);
        pack();
        setLocationRelativeTo(null);
        revalidate();
        repaint();
        panel.requestFocusInWindow();
    }
}


// difficulty enum – controls enemy HP, speed,
// spawn rate, and starting gold/lives

enum Difficulty {
    //             name        hpMult           speedMult       spawnInterval                 startGold       startLives
    EASY  ("Easy",   0.75,   0.85,      42,            175,       25),
    MEDIUM("Medium", 1.00,   1.00,      35,            125,       20),
    HARD  ("Hard",   1.50,   1.20,      27,             100,       15);

    final String label;
    final double hpMult;       // Enemy HP multiplier relative to base
    final double speedMult;    // Enemy movement speed multiplier
    final int    spawnInterval;// Ticks between enemy spawns
    final int    startGold;    // Gold the player begins with
    final int    startLives;   // Lives the player begins with

    Difficulty(String label, double hpMult, double speedMult,
               int spawnInterval, int startGold, int startLives) {
        this.label         = label;
        this.hpMult        = hpMult;
        this.speedMult     = speedMult;
        this.spawnInterval = spawnInterval;
        this.startGold     = startGold;
        this.startLives    = startLives;
    }
}


// Startscreen title cards and difficulty buttons

class StartScreen extends JPanel {

    private static final int W = 24 * 40; // same width as the game grid
    private static final int H = 18 * 40 + 80; // same total height as the game

    private final TowerDefense parent;

    // rectangles for each difficulty button (used for hit-testing clicks)
    private final Rectangle easyRect   = new Rectangle(W/2 - 100, H/2 + 20,  200, 55);
    private final Rectangle medRect    = new Rectangle(W/2 - 100, H/2 + 95,  200, 55);
    private final Rectangle hardRect   = new Rectangle(W/2 - 100, H/2 + 170, 200, 55);

    // Which button the mouse is currently hovering over (null = none)
    private Difficulty hovered = null;

    StartScreen(TowerDefense parent) {
        this.parent = parent;
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(18, 20, 26));

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                // Check which difficulty button was clicked
                if (easyRect.contains(e.getPoint()))  parent.startGame(Difficulty.EASY);
                else if (medRect.contains(e.getPoint()))  parent.startGame(Difficulty.MEDIUM);
                else if (hardRect.contains(e.getPoint())) parent.startGame(Difficulty.HARD);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                // Track hovered button so we can highlight it
                if      (easyRect.contains(e.getPoint())) hovered = Difficulty.EASY;
                else if (medRect.contains(e.getPoint()))  hovered = Difficulty.MEDIUM;
                else if (hardRect.contains(e.getPoint())) hovered = Difficulty.HARD;
                else                                       hovered = null;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Background gradient ──
        GradientPaint bg = new GradientPaint(0, 0, new Color(18, 20, 30), 0, H, new Color(10, 12, 20));
        g2.setPaint(bg);
        g2.fillRect(0, 0, W, H);

        // ── Decorative grid lines (subtle) ──
        g2.setColor(new Color(255, 255, 255, 12));
        for (int x = 0; x < W; x += 40) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 40) g2.drawLine(0, y, W, y);

        // ── Main title: "TOWER DEFENSE" ──
        String title = "TOWER DEFENSE";
        g2.setFont(new Font("SansSerif", Font.BOLD, 72));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (W - fm.stringWidth(title)) / 2;
        int ty = H / 2 - 100;

        // Drop shadow for the title
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(title, tx + 4, ty + 4);

        // Gold gradient for title text
        GradientPaint titleGrad = new GradientPaint(tx, ty - 60, new Color(255, 220, 50), tx, ty, new Color(200, 130, 0));
        g2.setPaint(titleGrad);
        g2.drawString(title, tx, ty);

        // ── Subtitle ──
        g2.setColor(new Color(180, 190, 220));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        fm = g2.getFontMetrics();
        String sub = "Select a difficulty to begin";
        g2.drawString(sub, (W - fm.stringWidth(sub)) / 2, ty + 38);

        // ── Difficulty buttons ──
        drawDiffButton(g2, easyRect,  "Easy",   "Relaxed pace, more gold",  new Color(50, 180, 80),  hovered == Difficulty.EASY);
        drawDiffButton(g2, medRect,   "Medium", "Balanced challenge",        new Color(220, 160, 30), hovered == Difficulty.MEDIUM);
        drawDiffButton(g2, hardRect,  "Hard",   "Fast & deadly enemies",     new Color(210, 50, 50),  hovered == Difficulty.HARD);

        // ── Footer hint ──
        g2.setColor(new Color(100, 110, 140));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String hint = "Left-click: place tower  |  Right-click: remove tower";
        fm = g2.getFontMetrics();
        g2.drawString(hint, (W - fm.stringWidth(hint)) / 2, H - 16);
    }

    /** Draws a single difficulty button with a label, subtitle, and hover highlight. */
    private void drawDiffButton(Graphics2D g2, Rectangle r, String label,
                                 String desc, Color base, boolean hover) {
        // Button fill – brighter when hovered
        Color fill = hover ? base.brighter() : base.darker();
        g2.setColor(fill);
        g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);

        // Border
        g2.setColor(hover ? Color.WHITE : base);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g2.setStroke(new BasicStroke(1));

        // Button label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2, r.y + 30);

        // Button description
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        fm = g2.getFontMetrics();
        g2.setColor(new Color(230, 230, 230, 210));
        g2.drawString(desc, r.x + (r.width - fm.stringWidth(desc)) / 2, r.y + 47);
    }
}

// ─────────────────────────────────────────────
//  GamePanel – main game loop and rendering
// ─────────────────────────────────────────────
class GamePanel extends JPanel implements ActionListener {

    // Grid dimensions (columns × rows) and pixel size of each cell
    static final int COLS = 24, ROWS = 18, CELL = 40;
    static final int W = COLS * CELL, H = ROWS * CELL;
    static final int PANEL_H = 80; // height of the HUD bar below the grid

    // Path the enemies walk along, defined as (col, row) waypoints
    static final int[][] PATH_WAYPOINTS = {
        {0,4},{5,4},{5,2},{10,2},{10,8},{15,8},{15,14},{20,14},{20,10},{23,10}
    };

    // ── Tower type indices (used as array keys throughout) ──
    // 0=Basic, 1=Sniper, 2=Splash, 3=Freezer, 4=RapidFire, 5=Minigun
    static final String[] TOWER_NAMES  = {"Basic","Sniper","Splash","Freezer","Rapid","Minigun"};
    static final int[]    TOWER_COSTS  = { 25,     75,      50,      40,    60,    200};
    static final Color[]  TOWER_COLORS = {
        new Color(30, 144, 255),   // Basic  – blue
        new Color(220, 50,  50),   // Sniper – red
        new Color(50,  180, 50),   // Splash – green
        new Color(120, 60,  200),  // Slow   – purple
        new Color(255, 165, 0),    // Rapid  – orange
        new Color(200, 200, 200)   // Mini   – silver
    };
    static final int[]    TOWER_RANGE  = { 3,  6,  2,  3,  3,  3};
    static final int[]    TOWER_DAMAGE = {10, 35,  9,  3, 7,  25};
    // Fire rate = ticks between shots; LOWER = faster
    static final int[]    TOWER_RATE   = {30, 70, 20,  40,  8,  5};
    // Whether this tower type deals splash (area) damage
    static final boolean[]TOWER_SPLASH = {false, false, true, false, false, false};
    // Whether this tower type applies a slow effect on hit
    static final boolean[]TOWER_SLOWS  = {false, false, false, true, false, false};

    // Tower type descriptions shown in the HUD tooltip area
    static final String[] TOWER_DESC = {
        "Dmg:10 Rng:3",    // Basic
        "Dmg:35 Rng:6",    // Sniper
        "Dmg:9  Rng:2 AoE",// Splash
        "Dmg:3  Slow",     // Slow
        "Dmg:7  Fast",     // Rapid Fire
        "Dmg:25 VFast"     // Minigun
    };

    // Grid flags marking which cells are part of the enemy path
    private final boolean[][] onPath;

    // Swing timer drives the game loop (~30 fps at 33 ms/tick)
    private final javax.swing.Timer timer;

    // Game object lists
    private final List<Tower>      towers      = new ArrayList<>();
    private final List<Enemy>      enemies     = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<FloatText>  floatTexts  = new ArrayList<>(); // damage/gold pop-ups

    // ── Game state ──
    private int gold, lives, wave = 0, score = 0;
    private int spawnTimer = 0, spawnCount = 0, spawnTotal = 0;
    private boolean waveActive = false, gameOver = false;
    private int selectedType = 0; // currently selected tower type in the HUD
    private int hoverCol = -1, hoverRow = -1; // cell under the mouse cursor

    // Difficulty settings for this session
    private final Difficulty difficulty;

    // ── HUD layout: how many tower buttons fit per row, and page offset ──
    // We have 6 towers; show 3 per row across 2 visual rows
    private static final int TOWERS_PER_ROW = 3;

    public GamePanel(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.gold  = difficulty.startGold;
        this.lives = difficulty.startLives;

        setPreferredSize(new Dimension(W, H + PANEL_H));
        setBackground(new Color(30, 32, 38));

        onPath = computePathCells();

        // Mouse click: place or remove tower
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { handleClick(e); }
        });

        // Mouse move: track hovered cell for range preview
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                hoverCol = e.getX() / CELL;
                hoverRow = e.getY() / CELL;
                repaint();
            }
        });

        timer = new javax.swing.Timer(33, this); // ~30 fps
        timer.start();
    }

    // ── Build the boolean grid of path cells from the waypoints ──
    private boolean[][] computePathCells() {
        boolean[][] grid = new boolean[COLS][ROWS];
        for (int i = 0; i < PATH_WAYPOINTS.length - 1; i++) {
            int c0 = PATH_WAYPOINTS[i][0],   r0 = PATH_WAYPOINTS[i][1];
            int c1 = PATH_WAYPOINTS[i+1][0], r1 = PATH_WAYPOINTS[i+1][1];
            int dc = Integer.compare(c1, c0), dr = Integer.compare(r1, r0);
            int c = c0, r = r0;
            while (c != c1 || r != r1) { grid[c][r] = true; c += dc; r += dr; }
            grid[c1][r1] = true;
        }
        return grid;
    }

    // ─────────────────────────────────────────
    //  GAME LOOP (called every timer tick)
    // ─────────────────────────────────────────
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // ── Spawn enemies during an active wave ──
        if (waveActive) {
            spawnTimer--;
            if (spawnTimer <= 0 && spawnCount < spawnTotal) {
                // Base HP scales with wave; difficulty multiplier applied here
                int baseHp = (int)((50 + wave * 30 + (int)(Math.random() * wave * 10))
                                   * difficulty.hpMult);
                enemies.add(new Enemy(baseHp, wave, difficulty.speedMult));
                spawnCount++;
                // Time between spawns shrinks with wave count but never below 10 ticks
                spawnTimer = Math.max(10, difficulty.spawnInterval - wave * 2);
            }

            // Wave ends when all enemies are spawned AND the field is clear
            if (spawnCount >= spawnTotal && enemies.isEmpty() && projectiles.isEmpty()) {
                waveActive = false;
                int bonus = 30 + wave * 5;
                gold += bonus;
                floatTexts.add(new FloatText("Wave " + wave + " complete! +" + bonus + "g",
                        W / 2, H / 2, new Color(255, 215, 0), 90));
            }
        }

        // ── Move enemies and check if any reached the end ──
        Iterator<Enemy> ei = enemies.iterator();
        while (ei.hasNext()) {
            Enemy en = ei.next();
            en.update();
            if (en.reachedEnd()) {
                lives--;
                ei.remove();
                if (lives <= 0) { gameOver = true; timer.stop(); }
            }
            // Tick down the slow effect duration on every enemy each frame
            if (en.slowTicks > 0) en.slowTicks--;
        }

        // ── Towers fire at the furthest enemy in range ──
        for (Tower t : towers) {
            t.cooldown--;
            if (t.cooldown <= 0) {
                Enemy target = findTarget(t);
                if (target != null) {
                    projectiles.add(new Projectile(t, target));
                    t.cooldown = TOWER_RATE[t.type];
                }
            }
        }

        // ── Move projectiles and apply damage on hit ──
        Iterator<Projectile> pi = projectiles.iterator();
        while (pi.hasNext()) {
            Projectile p = pi.next();
            p.update();
            if (p.hit()) {
                if (TOWER_SPLASH[p.towerType])  splashDamage(p);
                else                             dealDamage(p.target, TOWER_DAMAGE[p.towerType], p.x, p.y);

                // Apply slow effect if this tower slows enemies
                if (TOWER_SLOWS[p.towerType] && p.target != null && enemies.contains(p.target)) {
                    p.target.slowTicks = 80; // slow lasts ~2.6 s at 30 fps
                }
                pi.remove();
            } else if (p.expired()) {
                pi.remove();
            }
        }

        // ── Advance and prune floating damage/gold text ──
        floatTexts.removeIf(FloatText::update);

        repaint();
    }

    // Returns the enemy that is furthest along the path within a tower's range
    private Enemy findTarget(Tower t) {
        double rangePx = t.range * CELL;
        double tx = t.col * CELL + CELL / 2.0, ty = t.row * CELL + CELL / 2.0;
        Enemy best = null;
        double bestProgress = -1;
        for (Enemy en : enemies) {
            double dist = Math.hypot(en.x - tx, en.y - ty);
            if (dist <= rangePx && en.progress > bestProgress) {
                best = en;
                bestProgress = en.progress;
            }
        }
        return best;
    }

    // Deals damage to a single enemy and removes it if dead
    private void dealDamage(Enemy en, int dmg, double x, double y) {
        en.hp -= dmg;
        floatTexts.add(new FloatText("-" + dmg, (int)x, (int)y - 10, new Color(255, 80, 80), 30));
        if (en.hp <= 0) {
            gold  += en.reward;
            score += en.reward * 10;
            floatTexts.add(new FloatText("+" + en.reward + "g", (int)en.x, (int)en.y - 20, new Color(255, 215, 0), 45));
            enemies.remove(en);
        }
    }

    // Deals damage to every enemy within the splash radius of the projectile impact
    private void splashDamage(Projectile p) {
        double splashPx = TOWER_RANGE[p.towerType] * CELL * 0.75;
        new ArrayList<>(enemies).forEach(en -> {
            if (Math.hypot(en.x - p.x, en.y - p.y) <= splashPx)
                dealDamage(en, TOWER_DAMAGE[p.towerType], p.x, p.y);
        });
    }

    // ─────────────────────────────────────────
    //  INPUT HANDLING
    // ─────────────────────────────────────────
    private void handleClick(MouseEvent e) {
        if (gameOver) return;
        int bx = e.getX(), by = e.getY();

        // Click in the HUD bar below the grid
        if (by >= H) { handlePanelClick(bx, by); return; }

        int col = bx / CELL, row = by / CELL;
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return;

        // Right-click removes an existing tower
        if (e.getButton() == MouseEvent.BUTTON3) {
            towers.removeIf(t -> t.col == col && t.row == row);
            return;
        }

        // Can't place on path or occupied cell
        if (onPath[col][row]) return;
        if (towers.stream().anyMatch(t -> t.col == col && t.row == row)) return;

        int cost = TOWER_COSTS[selectedType];
        if (gold < cost) {
            floatTexts.add(new FloatText("Not enough gold!", bx, by, Color.RED, 60));
            return;
        }
        gold -= cost;
        towers.add(new Tower(col, row, selectedType));
    }

    private void handlePanelClick(int bx, int by) {
        // Tower selection buttons (6 towers, displayed across 2 rows of 3)
        int numTowers = TOWER_NAMES.length; // 6
        for (int i = 0; i < numTowers; i++) {
            int row  = i / TOWERS_PER_ROW;
            int col  = i % TOWERS_PER_ROW;
            int btnX = 5  + col * 125;
            int btnY = H  + 5 + row * 34;
            if (bx >= btnX && bx <= btnX + 118 && by >= btnY && by <= btnY + 28) {
                selectedType = i;
                return;
            }
        }

        // "Start Wave" button (bottom-right of HUD)
        if (!waveActive && bx >= W - 170 && bx <= W - 10 && by >= H + 15 && by <= H + 65) {
            wave++;
            spawnTotal = 5 + wave * 3;
            spawnCount = 0;
            spawnTimer = 10;
            waveActive = true;
        }
    }

    // ─────────────────────────────────────────
    //  RENDERING
    // ─────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawPath(g2);
        drawTowers(g2);
        drawEnemies(g2);
        drawProjectiles(g2);
        drawHover(g2);
        drawFloatTexts(g2);
        drawPanel(g2);
        if (gameOver) drawGameOver(g2);
    }

    // Draws the dark tiled background grid
    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(40, 44, 52));
        g.fillRect(0, 0, W, H);
        g.setColor(new Color(50, 55, 65));
        for (int c = 0; c <= COLS; c++) g.drawLine(c * CELL, 0, c * CELL, H);
        for (int r = 0; r <= ROWS; r++) g.drawLine(0, r * CELL, W, r * CELL);
    }

    // Fills path cells and draws the center-line connecting waypoints
    private void drawPath(Graphics2D g) {
        for (int c = 0; c < COLS; c++)
            for (int r = 0; r < ROWS; r++)
                if (onPath[c][r]) {
                    g.setColor(new Color(70, 60, 45));
                    g.fillRect(c * CELL + 1, r * CELL + 1, CELL - 2, CELL - 2);
                }
        g.setColor(new Color(100, 90, 70));
        for (int i = 0; i < PATH_WAYPOINTS.length - 1; i++) {
            g.drawLine(PATH_WAYPOINTS[i][0]*CELL+CELL/2, PATH_WAYPOINTS[i][1]*CELL+CELL/2,
                       PATH_WAYPOINTS[i+1][0]*CELL+CELL/2, PATH_WAYPOINTS[i+1][1]*CELL+CELL/2);
        }
    }

    // Draws each placed tower; slow towers get a purple ring, rapid/mini get gear marks
    private void drawTowers(Graphics2D g) {
        for (Tower t : towers) {
            int x = t.col * CELL, y = t.row * CELL;
            Color base = TOWER_COLORS[t.type];

            // Tower body
            g.setColor(base.darker());
            g.fillRoundRect(x + 4, y + 4, CELL - 8, CELL - 8, 8, 8);
            g.setColor(base);
            g.fillRoundRect(x + 5, y + 5, CELL - 10, CELL - 10, 7, 7);

            // Gun barrel (all towers except Slow)
            if (t.type != 3) {
                g.setColor(Color.DARK_GRAY);
                g.fillRect(x + CELL/2 - 3, y + 2, 6, CELL / 2);
            } else {
                // Slow tower: draw concentric rings to hint at slowing aura
                g.setColor(new Color(180, 100, 255, 140));
                g.setStroke(new BasicStroke(2));
                g.drawOval(x + 2, y + 2, CELL - 4, CELL - 4);
                g.setStroke(new BasicStroke(1));
            }

            // Minigun: draw double barrel
            if (t.type == 5) {
                g.setColor(new Color(80, 80, 80));
                g.fillRect(x + CELL/2 - 6, y + 2, 4, CELL / 2);
                g.fillRect(x + CELL/2 + 2, y + 2, 4, CELL / 2);
            }

            // Type initial label at bottom of tower
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.drawString(TOWER_NAMES[t.type].substring(0, 1), x + 14, y + CELL - 8);
        }
    }

    // Draws each enemy as a colored circle with an HP bar; slowed enemies are tinted blue
    private void drawEnemies(Graphics2D g) {
        for (Enemy en : enemies) {
            int ex = (int)en.x - 12, ey = (int)en.y - 12;

            // Drop shadow
            g.setColor(new Color(0, 0, 0, 60));
            g.fillOval(ex + 3, ey + 3, 24, 24);

            // Body color: shift toward blue when slowed
            float hf = (float)en.hp / en.maxHp;
            Color bodyColor;
            if (en.slowTicks > 0) {
                // Blend the normal HP color toward icy blue to show slow
                bodyColor = new Color(
                    (int)(180 * (1 - hf)),
                    (int)(100 * hf),
                    200);
            } else {
                bodyColor = new Color((int)(255 * (1 - hf)), (int)(200 * hf), 40);
            }
            g.setColor(bodyColor);
            g.fillOval(ex, ey, 24, 24);

            // Outline
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(ex, ey, 24, 24);
            g.setStroke(new BasicStroke(1));

            // HP bar (red background, green fill proportional to remaining HP)
            g.setColor(new Color(180, 0, 0));
            g.fillRect(ex, ey - 6, 24, 4);
            g.setColor(new Color(0, 200, 80));
            g.fillRect(ex, ey - 6, (int)(24 * hf), 4);
        }
    }

    // Draws projectiles; splash shots are larger with a glowing ring
    private void drawProjectiles(Graphics2D g) {
        for (Projectile p : projectiles) {
            Color c = TOWER_COLORS[p.towerType];
            g.setColor(c);
            int px = (int)p.x, py = (int)p.y;
            if (TOWER_SPLASH[p.towerType]) {
                // Splash: bigger green orb with outer ring
                g.fillOval(px - 5, py - 5, 10, 10);
                g.setColor(c.brighter());
                g.drawOval(px - 7, py - 7, 14, 14);
            } else if (p.towerType == 4 || p.towerType == 5) {
                // Rapid / Minigun: tiny fast bullet
                g.fillOval(px - 2, py - 2, 5, 5);
            } else {
                g.fillOval(px - 4, py - 4, 8, 8);
            }
        }
    }

    // Semi-transparent overlay on the hovered cell; shows range ring
    private void drawHover(Graphics2D g) {
        if (hoverCol >= 0 && hoverCol < COLS && hoverRow >= 0 && hoverRow < ROWS) {
            boolean blocked = onPath[hoverCol][hoverRow]
                || towers.stream().anyMatch(t -> t.col == hoverCol && t.row == hoverRow);
            g.setColor(blocked ? new Color(255, 60, 60, 80) : new Color(255, 255, 255, 40));
            g.fillRect(hoverCol * CELL, hoverRow * CELL, CELL, CELL);

            if (!blocked) {
                double rp = TOWER_RANGE[selectedType] * CELL;
                int cx = hoverCol * CELL + CELL / 2, cy = hoverRow * CELL + CELL / 2;
                g.setColor(new Color(255, 255, 255, 25));
                g.fillOval((int)(cx - rp), (int)(cy - rp), (int)(rp * 2), (int)(rp * 2));
                g.setColor(new Color(255, 255, 255, 80));
                g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                            0, new float[]{4}, 0));
                g.drawOval((int)(cx - rp), (int)(cy - rp), (int)(rp * 2), (int)(rp * 2));
                g.setStroke(new BasicStroke(1));
            }
        }
    }

    // Draws all active floating damage/gold text, fading out as their life ticks down
    private void drawFloatTexts(Graphics2D g) {
        for (FloatText f : floatTexts) {
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            float alpha = Math.min(1f, f.life / 20f);
            g.setColor(new Color(f.color.getRed(), f.color.getGreen(),
                                 f.color.getBlue(), (int)(255 * alpha)));
            g.drawString(f.text, f.x, f.y);
        }
    }

    // Draws the HUD bar at the bottom: tower buttons, stats, wave button
    private void drawPanel(Graphics2D g) {
        // Panel background
        g.setColor(new Color(20, 22, 28));
        g.fillRect(0, H, W, PANEL_H);
        g.setColor(new Color(60, 65, 80));
        g.drawLine(0, H, W, H);

        // Difficulty badge (top-right of HUD)
        Color diffColor = switch (difficulty) {
            case EASY   -> new Color(50, 180, 80);
            case MEDIUM -> new Color(220, 160, 30);
            case HARD   -> new Color(210, 50, 50);
        };
        g.setColor(diffColor);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.drawString("[" + difficulty.label + "]", W - 65, H + 12);

        // ── Tower selection buttons (2 rows × 3 columns) ──
        int numTowers = TOWER_NAMES.length; // 6
        for (int i = 0; i < numTowers; i++) {
            int btnRow = i / TOWERS_PER_ROW;
            int btnCol = i % TOWERS_PER_ROW;
            int bx = 5  + btnCol * 125;
            int by = H  + 5 + btnRow * 34;
            boolean sel = (selectedType == i);

            // Button fill
            g.setColor(sel ? TOWER_COLORS[i] : TOWER_COLORS[i].darker().darker());
            g.fillRoundRect(bx, by, 118, 28, 8, 8);

            // Selection highlight border
            if (sel) {
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawRoundRect(bx, by, 118, 28, 8, 8);
                g.setStroke(new BasicStroke(1));
            }

            // Tower name
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString(TOWER_NAMES[i], bx + 4, by + 13);

            // Cost and quick stats
            g.setFont(new Font("SansSerif", Font.PLAIN, 9));
            g.drawString(TOWER_COSTS[i] + "g  " + TOWER_DESC[i], bx + 4, by + 25);
        }

        // ── Resource stats ──
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setColor(new Color(255, 215, 0));
        g.drawString("Gold: " + gold,  385, H + 25);
        g.setColor(new Color(255, 100, 100));
        g.drawString("Lives: " + lives, 385, H + 48);
        g.setColor(new Color(150, 200, 255));
        g.drawString("Wave: " + wave,   500, H + 25);
        g.setColor(new Color(180, 255, 180));
        g.drawString("Score: " + score, 500, H + 48);

        // ── Wave button ──
        if (!waveActive) {
            g.setColor(new Color(50, 180, 50));
            g.fillRoundRect(W - 170, H + 15, 160, 50, 12, 12);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.drawString("Start Wave " + (wave + 1), W - 148, H + 46);
        } else {
            g.setColor(new Color(60, 65, 80));
            g.fillRoundRect(W - 170, H + 15, 160, 50, 12, 12);
            g.setColor(Color.GRAY);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            g.drawString("Wave in progress...", W - 162, H + 46);
        }

        // ── Control hint ──
        g.setColor(new Color(100, 105, 120));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g.drawString("Left-click: place  |  Right-click: remove", 5, H + PANEL_H - 4);
    }

    // Full-screen game-over overlay
    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, W, H);

        g.setColor(new Color(255, 80, 80));
        g.setFont(new Font("SansSerif", Font.BOLD, 60));
        FontMetrics fm = g.getFontMetrics();
        String txt = "GAME OVER";
        g.drawString(txt, (W - fm.stringWidth(txt)) / 2, H / 2 - 20);

        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        fm = g.getFontMetrics();
        String s2 = "Score: " + score + "  |  Wave: " + wave + "  [" + difficulty.label + "]";
        g.drawString(s2, (W - fm.stringWidth(s2)) / 2, H / 2 + 30);
    }
}

// ─────────────────────────────────────────────
//  Tower – a placed defensive structure
// ─────────────────────────────────────────────
class Tower {
    int col, row, type;
    int cooldown = 0; // ticks remaining until this tower can fire again
    int range, damage;

    Tower(int col, int row, int type) {
        this.col    = col;
        this.row    = row;
        this.type   = type;
        this.range  = GamePanel.TOWER_RANGE[type];
        this.damage = GamePanel.TOWER_DAMAGE[type];
    }
}

// ─────────────────────────────────────────────
//  Enemy – moves along the path toward the end
// ─────────────────────────────────────────────
class Enemy {
    double x, y;
    double progress = 0; // how far along the path (used to find "furthest" target)
    int hp, maxHp, reward;
    double baseSpeed; // speed without any slow effect
    int slowTicks = 0; // ticks remaining on slow effect (0 = not slowed)

    private final int[][] wp = GamePanel.PATH_WAYPOINTS;
    private int wpIdx = 0; // which waypoint the enemy is heading toward next

    /**
     * @param hp         Base hit points (already scaled by difficulty before passing in)
     * @param wave       Current wave number; affects reward and base speed
     * @param speedMult  Difficulty speed multiplier
     */
    Enemy(int hp, int wave, double speedMult) {
        this.hp       = this.maxHp = hp;
        this.reward   = 5 + wave * 2;
        // Base speed increases slightly with each wave; difficulty scales it further
        this.baseSpeed = (1.0 + wave * 0.1) * speedMult;
        // Start at the first waypoint's pixel center
        x = wp[0][0] * GamePanel.CELL + GamePanel.CELL / 2.0;
        y = wp[0][1] * GamePanel.CELL + GamePanel.CELL / 2.0;
    }

    void update() {
        if (wpIdx >= wp.length - 1) return; // already at end

        // When slowed, move at 40% of base speed; otherwise full speed
        double speed = (slowTicks > 0) ? baseSpeed * 0.40 : baseSpeed;

        double tx = wp[wpIdx + 1][0] * GamePanel.CELL + GamePanel.CELL / 2.0;
        double ty = wp[wpIdx + 1][1] * GamePanel.CELL + GamePanel.CELL / 2.0;
        double dx = tx - x, dy = ty - y, dist = Math.hypot(dx, dy);

        if (dist < speed) {
            // Snap to the waypoint and advance to the next one
            x = tx; y = ty; wpIdx++;
            progress++;
        } else {
            x += dx / dist * speed;
            y += dy / dist * speed;
            progress += speed / GamePanel.CELL;
        }
    }

    boolean reachedEnd() { return wpIdx >= wp.length - 1; }
}

// ─────────────────────────────────────────────
//  Projectile – moves from tower toward target
// ─────────────────────────────────────────────
class Projectile {
    double x, y, vx, vy;
    int towerType;
    int life = 120; // max ticks before auto-expiry
    Enemy target;

    // Rapid-fire and minigun bullets travel faster to feel snappier
    private final double speed;

    Projectile(Tower t, Enemy e) {
        x = t.col * GamePanel.CELL + GamePanel.CELL / 2.0;
        y = t.row * GamePanel.CELL + GamePanel.CELL / 2.0;
        this.target    = e;
        this.towerType = t.type;
        // Rapid (4) and Minigun (5) fire faster projectiles
        this.speed = (t.type == 4 || t.type == 5) ? 14 : 10;

        double dx = e.x - x, dy = e.y - y, d = Math.hypot(dx, dy);
        vx = dx / d * speed;
        vy = dy / d * speed;
    }

    void update() {
        x += vx; y += vy; life--;
        // Homing: steer toward the target's current position each frame
        if (target != null) {
            double dx = target.x - x, dy = target.y - y, d = Math.hypot(dx, dy);
            if (d > 0) { vx = dx / d * speed; vy = dy / d * speed; }
        }
    }

    // Returns true when the projectile is close enough to count as hitting the target
    boolean hit()     { return target == null || Math.hypot(target.x - x, target.y - y) < 10; }
    boolean expired() { return life <= 0; }
}

// ─────────────────────────────────────────────
//  FloatText – brief pop-up text (damage, gold)
// ─────────────────────────────────────────────
class FloatText {
    String text;
    int x, y, life;
    Color color;

    FloatText(String text, int x, int y, Color c, int life) {
        this.text  = text;
        this.x     = x;
        this.y     = y;
        this.color = c;
        this.life  = life;
    }

    /** Moves text upward each tick; returns true when it should be removed. */
    boolean update() { y--; life--; return life <= 0; }
}
