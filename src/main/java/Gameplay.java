import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;


public class Gameplay extends JPanel implements KeyListener
{
    /*In this class we're going to combine the whole game*/
    private static final int WIDTH = 800, HEIGHT = 800;

    private final Target[][] targets;
    private final Player[] planes;
    private final Bomb[] bombs;

    private final boolean[] pressedKeys;
    private static final int ENTER = 0;
    private static final int SPACE = 1;
    private static final int ROWS = 8;
    private static final int COLUMN = 38;
    private static int blueChecker = 0;
    private static int  orangeChecker = 0;
    private static int orangeScore = 0;
    private static int blueScore = 0;
    private final JLabel blueLabel;
    private final JLabel orangeLabel;
    private static final int GAME_DURATION_MS = 60000;  // 60 seconds
    private Timer gameTimer;
    private int timeLeft;
    private static final Font PIXEL_FONT = Assets.font("/Resource/Font/Pixel.ttf", 30f);
    private final Image background = Assets.image("/Resource/General/background.png");
    private boolean running = true;



    public Gameplay(int START_X, int START_Y, int GAMEPLAY_WIDTH, int GAMEPLAY_HEIGHT)
    {

        this.setBounds(START_X, START_Y, GAMEPLAY_WIDTH, GAMEPLAY_HEIGHT);
        this.setBackground(Color.BLUE);
        this.pressedKeys = new boolean[2];

        blueLabel = new JLabel("0");
        orangeLabel = new JLabel("0");

        blueLabel.setFont(PIXEL_FONT);
        orangeLabel.setFont(PIXEL_FONT);

        timeLeft = GAME_DURATION_MS;

        orangeLabel.setBounds(80, 10, 50, 50);
        blueLabel.setBounds(700, 10, 50, 50);

        orangeLabel.setForeground(new Color(255, 165, 0));
        blueLabel.setForeground(new Color(0, 0, 255));

        this.setLayout(null);
        this.add(orangeLabel);
        this.add(blueLabel);

        Image[] planeImages = {
                Assets.image(styleOption("BLUE", new Random().nextInt(5) + 1)),
                Assets.image(styleOption("ORANGE", new Random().nextInt(5) + 1))
        };

        Image[] bombImages = {
                Assets.image("/Resource/Bomb/Blue.png"),
                Assets.image("/Resource/Bomb/Orange.png")
        };

        planes = new Player[]{
                new Player(
                        WIDTH / 2,
                        WIDTH / 8,
                        HEIGHT / 4,
                        planeImages[0].getWidth(null) / 10,
                        planeImages[0].getHeight(null) / 10,
                        planeImages[0]
                ),
                new Player(
                WIDTH / 2,
                WIDTH / 4 * 3,
                HEIGHT / 4,
                planeImages[1]
                        .getWidth(null) / 10,
                planeImages[1]
                        .getHeight(null) / 10,
                planeImages[1]
        )
        };

        bombs = new Bomb[]{
                new Bomb(
                        planes[0].getX() + planes[0].getWIDTH(),
                        planes[0].getY() + planes[0].getHEIGHT() / 2,
                        planeImages[0].getWidth(null) / 40,
                        planeImages[0].getWidth(null) / 40,
                        10,
                        bombImages[0]
                ),
                new Bomb(
                        planes[1].getX(),
                        planes[1].getY() + planes[1].getHEIGHT() / 2,
                        planeImages[1].getWidth(null) / 40,
                        planeImages[1].getWidth(null) / 40,
                        10,
                        bombImages[1]
                )
        };


        this.planes[0].start();this.planes[1].start();
        this.bombs[0].start();this.bombs[1].start();

        int blue = 0;
        int orange = 1;

        this.targets = new Target[ROWS][COLUMN];

        initializeTargets();

        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocus();

        gameTimer = new Timer(1000, e -> {
            timeLeft -= 1000;
            if (timeLeft <= 0) {
                gameTimer.stop();
                stopGame();
                determineWinner();
            }
            repaint();
        });
        gameTimer.start();
        new Thread(() ->
        {
            while (running)
            {
                repaint();
                handlePlayerLoop(blue, -20);
                handlePlayerLoop(orange, WIDTH + 10);

                dropBomb(blue);
                dropBomb(orange);

                handleBombReturn(blue);
                handleBombReturn(orange);

                checkCollisions();

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }).start();


        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocus();


    }

    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        // Paint the background first
        graphics.drawImage(background, 0, 0, null);
        // Paint other components
        this.planes[0].paint(graphics);
        this.planes[1].paint(graphics);
        this.bombs[0].paint(graphics);
        this.bombs[1].paint(graphics);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMN; j++) {
                this.targets[i][j].draw(graphics);
            }
        }
        graphics.setFont(PIXEL_FONT);
        graphics.setColor(Color.WHITE);
        graphics.drawString(Integer.toString(timeLeft / 1000), WIDTH / 2 - 10, WIDTH / 20);
    }

    private void determineWinner() {
        if (orangeScore > blueScore) {
            JOptionPane.showMessageDialog(this, "Orange wins!");
        } else if (blueScore > orangeScore) {
            JOptionPane.showMessageDialog(this, "Blue wins!");
        } else {
            JOptionPane.showMessageDialog(this, "It's a tie!");
        }
    }

    private void initializeTargets() {
        Target baseTarget = new Target(
                2,
                HEIGHT / 16 * 11,
                20,
                20,
                1,
                Assets.image("/Resource/Target/1.png")
        );

        int numberType = 1;

        for (int i = 0; i < ROWS; i++) {
            if(i % 2 == 0 && i != 0){
                numberType++;
            }
            for (int j = 0; j < COLUMN; j++) {
                Target target = new Target(
                        j * (baseTarget.getX() + baseTarget.getHEIGHT()),
                        baseTarget.getY() + (i * baseTarget.getWIDTH()),
                        20,
                        20,
                        numberType,
                        Assets.image("/Resource/Target/" + numberType + ".png")
                );
                this.targets[i][j] = target;
                this.targets[i][j].start();
            }
        }
    }

    private void handlePlayerLoop(int color, int startingPoint) {
        if (this.planes[0].getX() > getWidth() &&
                (this.bombs[0].getX() > getWidth() && this.bombs[0].getY() == this.planes[0].getY())) {
            this.planes[0].setX(-20);
            this.bombs[0].setX(planes[0].getX() + planes[0].getWIDTH());
        } else if (this.planes[1].getX() < -20 &&
                (this.bombs[1].getX() < -20 && this.bombs[1].getY() == this.planes[1].getY())) {
            this.planes[1].setX(WIDTH + 20);
            this.bombs[1].setX(planes[1].getX());
        }
    }

    private void dropBomb(int bombIndex) {
        if (this.pressedKeys[bombIndex]) {
            if (bombIndex == 0) {
                this.bombs[bombIndex].moveRight();
            } else if (bombIndex == 1) {
                this.bombs[bombIndex].moveLeft();
            }
            this.bombs[bombIndex].setSleep(40);
        }
    }

    private void handleBombReturn(int bombIndex) {
        if (this.bombs[bombIndex].getY() == getHeight()) {
            this.bombs[bombIndex].reload(planes[bombIndex].getX(), planes[bombIndex].getY());
            this.bombs[bombIndex].setSleep(10);
            if (bombIndex == 0) {
                this.pressedKeys[ENTER] = false;
                if (this.bombs[bombIndex].getX() == getWidth()) {
                    this.bombs[bombIndex].setX(planes[bombIndex].getX() + planes[bombIndex].getWIDTH());
                }
            } else if (bombIndex == 1) {
                this.pressedKeys[SPACE] = false;
                if (this.bombs[bombIndex].getX() == -10) {
                    this.bombs[bombIndex].setX(planes[bombIndex].getX());
                }
            }
        }
    }

    private void checkCollisions() {
        Rectangle blueBombRec = this.bombs[0].calculateRectangle();
        Rectangle orangeBombRec = this.bombs[1].calculateRectangle();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLUMN; j++) {
                Rectangle targetRect = this.targets[i][j].calculateRectangle();

                handleCollision(0, blueBombRec, targetRect, i, j);
                handleCollision(1, orangeBombRec, targetRect, i, j);
                checkReload(0, ENTER, this.bombs[0], this.planes[0]);
                checkReload(1, SPACE, this.bombs[1], this.planes[1]);

            }
        }
    }

    private void checkReload(int index, int key, Bomb bomb, Player plane) {
        if (index == 0 && blueChecker == 8) {
            blueChecker = 0;
            bomb.reload(plane.getX(), plane.getY());
            this.pressedKeys[key] = false;
            bomb.setSleep(10);
        } else if (index == 1 && orangeChecker == 8) {
            orangeChecker = 0;
            bomb.reload(plane.getX(), plane.getY());
            this.pressedKeys[key] = false;
            bomb.setSleep(10);
        }
    }

    private void handleCollision(int bombIndex, Rectangle bombRect, Rectangle targetRect, int i, int j) {
        if (Utils.checkCollision(bombRect, targetRect)) {
            int targetNumber = this.targets[i][j].getNumber(); // Get the number from the original targets

            this.targets[i][j].setSTART_X(0);
            this.targets[i][j].setSTART_Y(0);
            this.targets[i][j].setWIDTH(0);
            this.targets[i][j].setHEIGHT(0);
            this.targets[i][j].setNumber(0);
            this.targets[i][j].setImage(Assets.image("/Resource/Target/1.png"));

            if (bombIndex == 0) {
                blueChecker++;
                blueScore += targetNumber; // Use the original targets number
                blueLabel.setText(Integer.toString(blueScore));
            } else if (bombIndex == 1) {
                orangeChecker++;
                orangeScore += targetNumber; // Use the original targets number
                orangeLabel.setText(Integer.toString(orangeScore));
            }
        }
    }

    public void stopGame() {
        running = false;
    }

    private String styleOption(String color, int type){
        return "/Resource/Player/" + color + "_TEAM/option" + type + ".png";
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        Integer toPress = null;
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            toPress = ENTER;
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            toPress = SPACE;
        }
        if (toPress != null){
            this.pressedKeys[toPress] =true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        Integer toRelease = null;
        if (this.bombs[0].getY() == HEIGHT) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                toRelease = ENTER;
            }
        } else if (this.bombs[1].getY() == HEIGHT) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                toRelease = SPACE;
            }
        }
        if (toRelease != null) {
            this.pressedKeys[toRelease] = false;
        }
    }
}