import java.awt.*;

public class Player extends Thread {
    /*In this class we're going to make player's plane*/
    private final int WIDTH, HEIGHT;
    private int START_X;
    private int START_Y;

    private final int moveX;
    private final int speed;
    private final Image image;
    public Player (int speed, int START_X, int START_Y, int width, int height, Image image) {
        this.START_X = START_X;
        this.moveX = START_X;
        this.START_Y = START_Y;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.image = image;
        this.speed = speed;
    }
    public void moveRight(){
        START_X += speed;
    }
    public void moveLeft(){
        START_X -= speed;
    }
    public void paint(Graphics graphics){
        graphics.drawImage(image, START_X, START_Y, START_X + WIDTH, START_Y + HEIGHT, 0, 0, image.getWidth(null), image.getHeight(null), null);
    }

    public void run(){
        while (true){
            if (this.moveX > 0 && this.moveX < 800/4){
                this.START_X++;
            }else {
                this.START_X--;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public int getX(){
        return this.START_X;
    }

    public int getY(){
        return this.START_Y;
    }

    public void setX(int START_X){
        this.START_X = START_X;
    }

    public void setY(int START_Y){
        this.START_Y = START_Y;
    }

    public int getWIDTH() {
        return WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }
}
