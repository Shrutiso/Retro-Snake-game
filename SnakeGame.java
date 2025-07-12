import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.io.File;
import javax.sound.sampled.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 600, HEIGHT = 600, UNIT_SIZE = 25;
    private final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private final int[] x = new int[GAME_UNITS];
    private final int[] y = new int[GAME_UNITS];
    private int bodyParts, applesEaten, appleX, appleY;
    private char direction;
    private boolean running = false;
    private boolean gameOver = false;
    private Timer timer;
    private Random random;

    private JButton replayButton;

    public SnakeGame() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        setLayout(null); 
        startGame();
    }

    public void startGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        gameOver = false;
        removeReplayButton();

        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - i * UNIT_SIZE;
            y[i] = 100;
        }

        newApple();
        running = true;
        timer = new Timer(100, this);
        timer.start();
        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        
        
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Serif", Font.BOLD, 24));
        String title = "Retro Snake Game";
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(title, (WIDTH - metrics.stringWidth(title)) / 2, 30);

        if (running) {
            g.setColor(new Color(30, 30, 30));
            for (int i = 0; i < WIDTH / UNIT_SIZE; i++) {
                g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, HEIGHT);
                g.drawLine(0, i * UNIT_SIZE, WIDTH, i * UNIT_SIZE);
            }

            Graphics2D g2d = (Graphics2D) g;
            GradientPaint gp = new GradientPaint(
                    appleX, appleY, Color.RED,
                    appleX + UNIT_SIZE, appleY + UNIT_SIZE, Color.PINK);
            g2d.setPaint(gp);
            g2d.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

            for (int i = 0; i < bodyParts; i++) {
                if (i == 0) {
                    g.setColor(new Color(0, 200, 0));
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                    g.setColor(Color.BLACK);
                    g.fillOval(x[i] + 4, y[i] + 4, 5, 5);
                    g.fillOval(x[i] + 14, y[i] + 4, 5, 5);
                } else {
                    g.setColor(new Color(34, 139, 34));
                    g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
                }
            }

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Score: " + applesEaten, 10, 55);
        } else {
            gameOverScreen(g);
        }
    }

    public void newApple() {
        appleX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    public void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U': y[0] -= UNIT_SIZE; break;
            case 'D': y[0] += UNIT_SIZE; break;
            case 'L': x[0] -= UNIT_SIZE; break;
            case 'R': x[0] += UNIT_SIZE; break;
        }
    }

    public void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();
            playSound("eat.wav");
        }
    }

    public void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
                break;
            }
        }

        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
            playSound("gameover.wav");
            gameOver = true;
            addReplayButton();
        }
    }

    public void gameOverScreen(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Game Over", WIDTH / 2 - 150, HEIGHT / 2 - 50);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 25));
        g.drawString("Final Score: " + applesEaten, WIDTH / 2 - 80, HEIGHT / 2);
    }

    public void addReplayButton() {
        replayButton = new JButton("Play Again");
        replayButton.setBounds(WIDTH / 2 - 80, HEIGHT / 2 + 40, 160, 40);
        replayButton.setFocusPainted(false);
        replayButton.setBackground(new Color(0, 150, 0));
        replayButton.setForeground(Color.WHITE);
        replayButton.setFont(new Font("Arial", Font.BOLD, 16));
        replayButton.addActionListener(e -> {
            removeReplayButton();
            startGame();
        });
        this.add(replayButton);
        this.repaint();
    }

    public void removeReplayButton() {
        if (replayButton != null) {
            this.remove(replayButton);
            replayButton = null;
        }
    }

    public void playSound(String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.err.println("Sound file not found: " + file.getAbsolutePath());
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    @Override public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (direction != 'R') direction = 'L';
                break;
            case KeyEvent.VK_RIGHT:
                if (direction != 'L') direction = 'R';
                break;
            case KeyEvent.VK_UP:
                if (direction != 'D') direction = 'U';
                break;
            case KeyEvent.VK_DOWN:
                if (direction != 'U') direction = 'D';
                break;
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Retro Snake Game");
        SnakeGame gamePanel = new SnakeGame();
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
