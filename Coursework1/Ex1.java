/*
 * File: DumboController.java
 * Created: 17 September 2002, 00:34
 * Author: Stephen Jarvis
 *
 * Before moving, robot checks its surrounding and gets information about walls around it.I used "robot.look()" for that.
 * I used 4 variables to have this information. These variables are used in "if else" statements to identify in which
 * direction robot should go.
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex1
{

    public void controlRobot(IRobot robot) {

        int randno;
        int direction = 0;
        boolean indicator = true;
        String dir ="";
        String location = "";

        //these variable have information about walls around robot
        int right = robot.look(IRobot.RIGHT);
        int left = robot.look(IRobot.LEFT);
        int ahead = robot.look(IRobot.AHEAD);
        int back = robot.look(IRobot.BEHIND);
        //it will count amount of walls around robot
        int walls = 0;

        //these statements count walls around robot
        if (right == IRobot.WALL) walls++;
        if (left == IRobot.WALL) walls++;
        if (back == IRobot.WALL) walls++;
        if (ahead == IRobot.WALL) walls++;

        //this part of code decides what is the position of robot
        switch (walls) {
            case 0 -> location = "at a crossroad";
            case 1 -> location = "at a junction";
            case 2 -> location = "down a corridor";
            case 3 -> location = "at a deadend";
        }


        //this loop finds the direction the robot will move
        while (indicator) {
            // Select a random number
            randno = (int) Math.round(Math.random() * 3);

            if ((randno == 0) && (left != IRobot.WALL)) {
                direction = IRobot.LEFT;
                dir = "left";
                indicator = false;
            } else if ((randno == 1) && (right != IRobot.WALL)) {
                direction = IRobot.RIGHT;
                dir = "right";
                indicator = false;
            } else if ((randno == 2) && (back != IRobot.WALL)) {
                direction = IRobot.BEHIND;
                dir = "backwards";
                indicator = false;
            } else if ((randno == 3) && (ahead != IRobot.WALL)) {
                direction = IRobot.AHEAD;
                dir = "forward";
                indicator = false;
            } else {
                direction = 0;
            }
        }

        System.out.println("I am going " + dir + " " + location);

        robot.face(direction); // Face the robot in this direction

    }

}