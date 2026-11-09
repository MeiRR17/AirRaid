import java.awt.*;

public class Target extends Thread {
    private int START_X, START_Y;
    private int WIDTH, HEIGHT;
    private int number;//number shown on the target
    private Image image;
    public Target(int START_X, int START_Y, int WIDTH, int HEIGHT, int number, Image image) {
        this.START_X = START_X;
        this.START_Y = START_Y;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.number = number;
        this.image = image;
    }
    public Rectangle calculateRectangle(){
        return new Rectangle(this.START_X,this.START_Y,this.WIDTH,this.HEIGHT);
    }
    public void draw(Graphics graphics){
        graphics.drawImage(image, START_X, START_Y, START_X + WIDTH, START_Y + HEIGHT, 0, 0, image.getWidth(null), image.getHeight(null), null);
    }

    public int getNumber(){
        return this.number;
    }

    public int getHEIGHT(){
        return this.HEIGHT;
    }

    public int getX() {
        return this.START_X;
    }

    public int getWIDTH() {
        return this.WIDTH;
    }

    public int getY() {
        return this.START_Y;
    }

    public void setSTART_X(int START_X) {
        this.START_X = START_X;
    }

    public void setSTART_Y(int START_Y) {
        this.START_Y = START_Y;
    }

    public void setWIDTH(int WIDTH) {
        this.WIDTH = WIDTH;
    }

    public void setHEIGHT(int HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
