package objects.game_elements;

import gui.window.A_Panel;
import objects.Coordinates;
import objects.Direction;
import objects.Size;

import java.awt.*;

public class Player extends A_GameObject implements Movable {

    private static final int SPEED = 500;

    Direction directionMovement;
    double radius;

    public Player() {
        super();
        size = new Size(50, 50);
        radius = size.x / 2;
        speed = SPEED;
        init();
    }

    @Override
    public void init() {
        coordinates = new Coordinates(A_Panel.WIDTH/2 - radius , A_Panel.HEIGHT / 2 - radius, size.x, size.y);
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public Size getSize() {
        return size;
    }

    public void increaseSpeed(){
        speed += 10;
    }

    public void decreaseSpeed(){
        speed -= 10;
    }

    public void setDirectionMovement(Direction directionMovement) {
        this.directionMovement = directionMovement;
    }

    public void stopMovement() {
        speed = 0;
    }

    public void continueMovement() {
        speed = SPEED;
    }

    public void draw(Graphics graphics) {
        graphics.setColor(Color.WHITE);

        graphics.fillOval((int) coordinates.topLeftCorner_x, (int) coordinates.topLeftCorner_y, (int) size.x, (int) size.y);

        //Checks if player is partially off the horizontal side of the screen
        if (coordinates.topLeftCorner_x > A_Panel.WIDTH - size.x) {
            int sizeLeft = (int) (coordinates.bottomRightCorner_x - A_Panel.WIDTH);
            graphics.fillOval((int) -(size.x - sizeLeft), (int) coordinates.topLeftCorner_y, (int) size.x, (int) size.y);
        }

        //Checks if player is partially off the vertical side of the screen
        if(coordinates.topLeftCorner_y > A_Panel.HEIGHT - size.y){
            int sizeLeft = (int) (coordinates.bottomRightCorner_y - A_Panel.HEIGHT);
            graphics.fillOval((int)coordinates.topLeftCorner_x, (int) -(size.y-sizeLeft), (int) size.x, (int) size.y);
        }

        //Checks if player is partially off the horizontal and vertical side of the screen
        if (coordinates.topLeftCorner_x > A_Panel.WIDTH - size.x && coordinates.topLeftCorner_y > A_Panel.HEIGHT - size.y) {
            int sizeLeftX = (int) (coordinates.bottomRightCorner_x - A_Panel.WIDTH);
            int sizeLeftY = (int) (coordinates.bottomRightCorner_y - A_Panel.HEIGHT);
            graphics.fillOval((int) -(size.x - sizeLeftX), (int) -(size.y - sizeLeftY), (int) size.x, (int) size.y);
        }
    }

    @Override
    public void move(double diffSeconds) {

        if (directionMovement != null) {
            switch (directionMovement) {
                case UP -> coordinates.moveY(-speed * diffSeconds);
                case DOWN -> coordinates.moveY(speed * diffSeconds);
                case LEFT -> coordinates.moveX(-speed * diffSeconds);
                case RIGHT -> coordinates.moveX(speed * diffSeconds);
            }
        }
    }
}
