import java.awt.*;

public class Utils {

    public static boolean checkCollision (Rectangle rect1, Rectangle rect2) {
        return rect1.intersects(rect2);
    }

}
