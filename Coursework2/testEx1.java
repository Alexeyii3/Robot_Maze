import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.Arrays;

/*
My code from exercise 2 works perfectly for exercise 3. I did not identify any collisions or crushes by testing this code a lor of times.
BacktrackingControl and exploreControl identify paths correctly. Headings are saved and used correctly.
 */
public class testEx1 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode; // Identifies which controller should be called
    private int index1 = 0;

    public void controlRobot(IRobot robot){
        // Resets RobotData information for new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData();
            reset();
            explorerMode = 1;
        }

        int direction = 0;

        if (pollRun == 0){
            explorerMode = 1;
            index1=0;
        }

        if(robot.getRuns() == 0) {
            // Calls needed controller
            switch (explorerMode) {
                case 0 -> direction = backtrackControl(robot);
                case 1 -> direction = exploreControl(robot);
            }
        }

        if (robot.getRuns()>0){
            robotData.getDir();
            direction = robotData.finalDir[index1];
            index1++;
            System.out.println("direction= " + direction);
            if (direction < 2000) {
                robot.setHeading(direction);
                direction = IRobot.AHEAD;
            }
        }

        System.out.println("Junctioncounter= " + robotData.junctionCounter);
        print1(IRobot.AHEAD,IRobot.BEHIND,IRobot.LEFT,IRobot.RIGHT,IRobot.NORTH,IRobot.SOUTH,IRobot.EAST,IRobot.WEST);
        robot.face(direction);
        pollRun++; // Increases passes counter
    }
    public void print1(int x, int y, int z, int m, int a, int b, int c, int d){
        System.out.println("AHEAD: " + x);
        System.out.println("BEHIND: " + y);
        System.out.println("LEFT: " + z);
        System.out.println("RIGHT: " + m);
        System.out.println("NORTH: " + a);
        System.out.println("SOUTH: " + b);
        System.out.println("EAST: " + c);
        System.out.println("WEST: " + d);
        System.out.println(Arrays.deepToString(robotData.directions));
        System.out.println("final dir: " + Arrays.toString(robotData.finalDir));
        //System.out.println("index 1: " + index1);

    }

    // This controller is called when robot is in exploration mode
    // This controller is called when robot is in exploration mode
    private int exploreControl(IRobot robot){
        int exits = nonwallExits(robot);
        int direction = 0;
        int x = robot.getLocation().x;
        int y = robot.getLocation().y;
        if(nonwallExits(robot) > 2){
            robotData.recordJunction(robot.getHeading(),exits);
            robotData.print();
            robotData.junctionCounter++;
        }
        if(pollRun == 0){
            direction = deadEnd(robot);
            robotData.dirSaver(direction);
            robotData.dirCounter++;
        } else {
            if ((robotData.kindOfSquare[robotData.squareCounter-1] == 3 || robotData.kindOfSquare[robotData.squareCounter-1] == 4) && robotData.getElem(0) == 0){
                robotData.dirSaver(robotData.prevSquare(robot));
                robotData.dirCounter++;
                switch (exits) {
                    // Deadend case
                    case 1 -> {
                        explorerMode = 0;
                        direction = deadEnd(robot);
                        robotData.dirRemover();
                    }
                    // Corridor case
                    case 2 -> {
                        direction = corridor(robot);
                        robotData.dirSaver(direction);
                        robotData.dirCounter++;
                    }
                    // Junction case
                    case 3 -> {
                        direction = junction(robot);
                        robotData.arrayCounter++;
                        robotData.dirCounter = 0;
                        robotData.dirSaver(direction);
                        robotData.dirCounter++;
                    }
                    // Crossroad case
                    case 4 -> {
                        direction = crossroad(robot);
                        robotData.arrayCounter++;
                        robotData.dirCounter = 0;
                        robotData.dirSaver(direction);
                        robotData.dirCounter++;
                    }
                }

            } else {
                switch (exits) {
                    // Deadend case
                    case 1 -> {
                        explorerMode = 0;
                        direction = deadEnd(robot);
                        robotData.dirRemover();
                    }
                    // Corridor case
                    case 2 -> {
                        direction = corridor(robot);
                        robotData.dirSaver(direction);
                        robotData.dirCounter++;
                    }
                    // Junction case
                    case 3 -> {
                        direction = junction(robot);
                        robotData.arrayCounter++;
                        robotData.dirCounter = 0;
                        robotData.dirSaver(direction);
                        robotData.dirCounter++;
                    }
                    // Crossroad case
                    case 4 -> {
                        direction = crossroad(robot);
                        robotData.arrayCounter++;
                        robotData.dirCounter = 0;
                        robotData.dirSaver(direction);
                        robotData.dirCounter++;
                    }
                }
            }
        }
        if ((x ==1 && y == 2) || (x ==2 && y ==1 )){
            robotData.directions[0][0] = robot.getHeading();
        }
        robotData.squareSaver(exits);
        // Direction is returned
        return direction;
    }

    // This controller is called when robot is in backtracking mode
    private int backtrackControl(IRobot robot){
        int x = robot.getLocation().x;
        int y = robot.getLocation().y;
        int direction = 0;
        int exits = nonwallExits(robot);
        int passages = passageExits(robot);

        // This statement identifies if robot is on junction or crossroad square
        if(exits > 2){
            // Identifies if there are any passages around
            if(passages > 0){
                // If there are, robot is controlled by "junction" or "crossroad" methods
                switch (exits){
                    case 3 -> direction = junction(robot);
                    case 4 -> direction = crossroad(robot);
                }
                // Robot goes to exploration mode
                explorerMode = 1;
                robotData.dirCounter = 0;

            } else {
                int heading = robotData.searchHeading();
                // If there are no passages around, robot takes information about his heading when he arrived to this junction and then set opposite heading
                switch (heading){
                    case IRobot.NORTH -> robot.setHeading(IRobot.SOUTH);
                    case IRobot.SOUTH -> robot.setHeading(IRobot.NORTH);
                    case IRobot.EAST -> robot.setHeading(IRobot.WEST);
                    case IRobot.WEST -> robot.setHeading(IRobot.EAST);
                }
                // then robot moves ahead
                direction = IRobot.AHEAD;
                robotData.arrayCounter--;
                robotData.dirRemover();
                robotData.removeElem();
            }
        } else {
            if (passageExits(robot) == 0) {
                switch (exits){
                    case 1 -> direction = deadEnd(robot);
                    case 2 -> direction = corridor(robot);
                }
            } else {
                direction = corridor(robot);
                robotData.dirSaver(direction);
                robotData.dirCounter++;

            }
            // If it is not junction or crossroad, robot is controlled by "deadend" or "corridor" methods
        }
        if ((x ==1 && y == 2) || (x ==2 && y ==1 )){
            robotData.directions[0][0] = robot.getHeading();
        }
        robotData.squareSaver(exits);
        // Direction is returned
        return direction;
    }

    //This method counts non-wall squares around robot
    public static int nonwallExits(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.BEHIND,IRobot.LEFT,IRobot.RIGHT};
        int amount = 0;
        for (int i = 0; i < 4; i++){
            if (robot.look(sides[i]) != IRobot.WALL){amount++;}
        }
        return amount;
    }

    //This method counts passages around robot
    private static int passageExits(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.LEFT,IRobot.RIGHT};
        int amount = 0;
        for (int i = 0; i < 3; i++){
            if (robot.look(sides[i]) == IRobot.PASSAGE){
                amount++;
            }
        }
        return amount;
    }

    // This method controls robot if it is at a deadend
    private static int deadEnd(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.LEFT,IRobot.RIGHT};
        int result = 0;
        //if there is no wall behind, robot goes there
        if (robot.look(IRobot.BEHIND) != IRobot.WALL){
            result = IRobot.BEHIND;
        } else {
            //if there is wall behind, robot finds non-wall square and goes there
            for (int i = 0; i < 3; i++){
                if (robot.look(sides[i]) != IRobot.WALL){
                    result = sides[i];
                }
            }
        }
        // Returns direction
        return result;
    }

    // This method controls robot if it is at a corridor
    private static int corridor(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.LEFT,IRobot.RIGHT};
        int result = 0;
        // Finds non-wall exit but behind
        for (int i = 0; i < 3; i++) {
            if (robot.look(sides[i]) != IRobot.WALL) {
                result = sides[i];
            }
        }
        // Returns direction
        return result;
    }

    // This method controls robot if it is at a junction
    private static int junction(IRobot robot){
        int result = 0;
        if (passageExits(robot) > 0){
            //if there are several passages around robot, robot chooses randomly between them. if there is only one passage, robot goes there
            while (true){
                int rand = (int) (Math.random() * 3);
                if (robot.look(IRobot.AHEAD) == IRobot.PASSAGE && rand == 0){
                    result = IRobot.AHEAD;
                    break;
                } else if (robot.look(IRobot.LEFT) == IRobot.PASSAGE && rand == 1) {
                    result = IRobot.LEFT;
                    break;
                } else if (robot.look(IRobot.RIGHT) == IRobot.PASSAGE && rand == 2) {
                    result = IRobot.RIGHT;
                    break;
                }
            }

        } else {
            //if there are no passages, robot goes to random non-wall square
            while (true){
                int rand = (int) (Math.random() * 3);
                if (robot.look(IRobot.AHEAD) != IRobot.WALL && rand == 0){
                    result = IRobot.AHEAD;
                    break;
                } else if (robot.look(IRobot.LEFT) != IRobot.WALL && rand == 1) {
                    result = IRobot.LEFT;
                    break;
                } else if (robot.look(IRobot.RIGHT) != IRobot.WALL && rand == 2) {
                    result = IRobot.RIGHT;
                    break;
                }
            }
        }
        // Return direction
        return result;
    }

    // This method controls robot if it is at a crossroad
    private static int crossroad(IRobot robot){
        int result = 0;
        if (passageExits(robot) > 0){
            //if there are several passages around robot, robot chooses randomly between them. if there is only one passage, robot goes there
            while (true){
                int rand = (int) (Math.random() * 3);
                if (robot.look(IRobot.AHEAD) == IRobot.PASSAGE && rand == 0){
                    result = IRobot.AHEAD;
                    break;
                } else if (robot.look(IRobot.LEFT) == IRobot.PASSAGE && rand == 1) {
                    result = IRobot.LEFT;
                    break;
                } else if (robot.look(IRobot.RIGHT) == IRobot.PASSAGE && rand == 2) {
                    result = IRobot.RIGHT;
                    break;
                }
            }

        } else {
            //if there are no passages, robot goes to random non-wall square
            while (true){
                int rand = (int) (Math.random() * 3);
                if (robot.look(IRobot.AHEAD) != IRobot.WALL && rand == 0){
                    result = IRobot.AHEAD;
                    break;
                } else if (robot.look(IRobot.LEFT) != IRobot.WALL && rand == 1) {
                    result = IRobot.LEFT;
                    break;
                } else if (robot.look(IRobot.RIGHT) != IRobot.WALL && rand == 2) {
                    result = IRobot.RIGHT;
                    break;
                }
            }
        }
        // Return direction
        return result;
    }

    // Resets pollRun and junctionCounter values
    public void reset() {
        robotData.resetJunctionCounter();
        pollRun = 0;
    }

}

class RobotData {
    private static int maxJunctions = 10000; // Max number likely to occur
    public static int junctionCounter = 0; // No. of junctions stored
    private int[] arrived = new int[maxJunctions]; // Heading the robot first arrived from
    public int[] finalDir = new int[200];
    public int[] kindOfSquare = new int [maxJunctions];
    public int[][] directions = new int[100][15];
    public int[] kindOfJunc = new int [maxJunctions];
    public int dirCounter = 0;
    public int squareCounter = 0;
    public int counter2 = 0;
    public int arrayCounter = 0;

    // Resets junctionCounter value
    public void resetJunctionCounter() {
        junctionCounter = 0;

    }

    // Record robot's coordinates and heading when it is at an unexplored junction
    public void recordJunction(int heading, int nonwalls){
        arrived[junctionCounter] = heading;
        if(nonwalls == 3){
            kindOfJunc[junctionCounter] = 3;
        } else if(nonwalls == 4){
            kindOfJunc[junctionCounter] = 4;
        }
    }


    // Prints junction's number, coordinates and heading
    public void print(){
        String[] headingArr = {"NORTH","SOUTH","EAST","WEST"};
        String heading = "";
        switch (arrived[junctionCounter]){
            case IRobot.NORTH -> heading = headingArr[0];
            case IRobot.SOUTH -> heading = headingArr[1];
            case IRobot.EAST -> heading = headingArr[2];
            case IRobot.WEST -> heading = headingArr[3];
        }
        System.out.println("Junction " + (junctionCounter+1) + " " + "heading " + heading);
    }

    // This method searches for heading of robot when it arrived at junction
    public int searchHeading(){
        int result = 0;
        result = arrived[junctionCounter-1];
        return result;
    }

    // This method removes element of array of robot's heading
    public int removeElem(){
        int result = 0;
        junctionCounter--;
        result = arrived[junctionCounter];
        arrived[junctionCounter] = 0;
        return result;
    }
    public void dirSaver(int dir){
        directions[arrayCounter][dirCounter] = dir;
    }

    public void dirRemover(){
        for(int i = 0; i < directions[arrayCounter].length; i++){
            directions[arrayCounter][i] = 0;
        }
        dirCounter = 0;
    }

    public void getDir(){
        if (counter2 < 100) {
            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 15; j++) {
                    if (directions[i][j] != 0) {
                        finalDir[counter2] = directions[i][j];
                        counter2++;
                    }
                }
            }
        }
    }
    public void squareSaver(int square){
        kindOfSquare[squareCounter] = square;
        squareCounter++;
    }
    public int prevSquare(IRobot robot){
        int result = 0;
        if (kindOfSquare[squareCounter - 1] == 3 || kindOfSquare[squareCounter - 1] == 4){
            result = robot.getHeading();
        }
        return result;
    }
    public int getElem(int i){
        return directions[arrayCounter][i];
    }


}