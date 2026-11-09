import java.awt.*;

public class Bomb extends Thread{
    private final int WIDTH, HEIGHT;
    private int START_X, START_Y;

    private final int moveX;
    private int sleep;
    private final Image image;

    public Bomb(int START_X, int START_Y, int width, int height, int sleep, Image image){
        this.START_X = START_X;
        this.moveX = START_X;
        this.START_Y = START_Y;
        this.WIDTH = width;
        this.HEIGHT = height;
        this.sleep = sleep;
        this.image = image;
    }

    public void moveRight(){
        START_Y += 2;
        START_X ++;
    }
    public void moveLeft(){
        START_Y += 2;
        START_X --;
    }

public Rectangle calculateRectangle(){
        return new Rectangle(this.START_X,this.START_Y,this.WIDTH,this.HEIGHT);
    }

public void reload(int playersX, int playersY){
        this.START_X = playersX;
        this.START_Y = playersY;
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
                Thread.sleep(this.sleep);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public int getX(){
        return START_X;
    }
    public void setX(int START_X){
        this.START_X = START_X;
    }
    public int getY(){
        return START_Y;
    }

    public void setSleep(int sleep){
        this.sleep = sleep;
    }
}
