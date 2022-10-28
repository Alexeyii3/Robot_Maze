/*
 * File: DumboController.java
 * Created: 17 September 2002, 00:34
 * Author: Stephen Jarvis
 *
 * The probabilities for left, right, back, ahead directions distributed as 1/6, 1/3, 1/3, 1/6 respectively. (if
 * left,right,back,ahead are executed when we get random numbers 0,1,2,3 respectively)
 * For making these probability equal I slightly changed the method of getting random number: I deleted "Math.round"
 * and used only random integers from "Math.random" method.
 * In my opinion, unequal probabilities caused by rounding: for 0 random number should be 0 - 0.166, for 1:0.167 -0.499,
 *  for 2: 0.5 - 0.833, for 3: 0.834 - 1. We can see that 1 and 2 are going to be the most popular outcomes.
 *
 * After I had made probabilities equal, I found the way for doing 1-in-8 chance part of exercise. I have done it
 * through several "if else statements". I think this part of code appears to be quite massive. However, it works well.
 * I think exercise 2 could be done in more brief way. I will try to improve my JAVA skills and make my code shorter
 * and more efficient.
 */

import uk.ac.warwick.dcs.maze.logic.IRobot;
import java.util.Random;

public class Ex2
{

    public void controlRobot(IRobot robot) {

        //int randno;
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
            int randno = (int) (Math.random() * 8);

            if (randno == 7){
                int randno1 = (int) (Math.random() * 4);
                if ((randno1 == 0) && (left != IRobot.WALL)) {
                    direction = IRobot.LEFT;
                    dir = "left";
                    indicator = false;
                } else if ((randno1 == 1) && (right != IRobot.WALL)) {
                    direction = IRobot.RIGHT;
                    dir = "right";
                    indicator = false;
                } else if ((randno1 == 2) && (back != IRobot.WALL)) {
                    direction = IRobot.BEHIND;
                    dir = "backwards";
                    indicator = false;
                } else if ((randno1 == 3) && (ahead != IRobot.WALL)) {
                    direction = IRobot.AHEAD;
                    dir = "forward";
                    indicator = false;
                } else {
                    direction = 0;
                }
            } else {
                if (ahead != IRobot.WALL) {
                    direction = IRobot.AHEAD;
                    dir = "forward";
                    indicator = false;
                } else {
                    while (true){
                        int rando2 = (int) (Math.random() * 3);
                        if (back != IRobot.WALL && rando2 == 0){
                            direction = IRobot.BEHIND;
                            dir = "backwards";
                            break;
                        } else if(left != IRobot.WALL && rando2 == 1) {
                            direction = IRobot.LEFT;
                            dir = "left";
                            break;
                        } else if (right != IRobot.WALL && rando2 == 2) {
                            direction = IRobot.RIGHT;
                            dir = "right";
                            break;
                        } else {
                            direction = 0;
                        }
                    }

                }
            }
        }

        System.out.println("I'm going " + dir + " " + location);

        robot.face(direction); /* Face the robot in this direction */

    }

}