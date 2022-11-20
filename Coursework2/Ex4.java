import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.*;
/*

 */

public class Ex4 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode; // Identifies which controller should be called
    private int index1 = 0;
    public int firstDir = 0;
    private int startX = 0;
    private int startY = 0;
    private int previousLoc = 0;
    private int passNum = 0;


    public void controlRobot(IRobot robot){
        int direction = 0;
        if (robot.getRuns() == 0) {
            int x = robot.getLocation().x;
            int y = robot.getLocation().y;
            index1 = 0;
            firstDir = 0;

            // Saves information about previously unexplored junction or crossroad
            if (nonwallExits(robot) > 2 && robotData.juncIdent(x, y) == 0) {
                robotData.recordJunction(x, y, robot.getHeading());
                robotData.print();
                robotData.junctionCounter++;
            }

            // Resets RobotData information for new maze
            if ((robot.getRuns() == 0) && (pollRun == 0)) {
                robotData = new RobotData();
                reset();
                explorerMode = 1;
            }


            // Calls needed controller
            if (explorerMode == 0){
                direction = backtrackControl(robot);
            } else {
                direction = exploreControl(robot);
            }


        } else {
            System.out.println("pollRun: "+pollRun);
            if (pollRun == 0){
                index1 = 0;
                int loc = robot.getHeading();
                if (loc == IRobot.NORTH) {
                    switch (firstDir) {
                        case IRobot.NORTH -> direction = IRobot.AHEAD;
                        case IRobot.SOUTH -> direction = IRobot.BEHIND;
                        case IRobot.EAST -> direction = IRobot.RIGHT;
                        case IRobot.WEST -> direction = IRobot.LEFT;
                    }
                } else if (loc == IRobot.SOUTH) {
                    switch (firstDir) {
                        case IRobot.NORTH -> direction = IRobot.BEHIND;
                        case IRobot.SOUTH -> direction = IRobot.AHEAD;
                        case IRobot.EAST -> direction = IRobot.LEFT;
                        case IRobot.WEST -> direction = IRobot.RIGHT;
                    }
                } else if (loc == IRobot.EAST) {
                    switch (firstDir) {
                        case IRobot.NORTH -> direction = IRobot.LEFT;
                        case IRobot.SOUTH -> direction = IRobot.RIGHT;
                        case IRobot.EAST -> direction = IRobot.AHEAD;
                        case IRobot.WEST -> direction = IRobot.BEHIND;
                    }
                } else {
                    switch (firstDir) {
                        case IRobot.NORTH -> direction = IRobot.RIGHT;
                        case IRobot.SOUTH -> direction = IRobot.LEFT;
                        case IRobot.EAST -> direction = IRobot.BEHIND;
                        case IRobot.WEST -> direction = IRobot.AHEAD;
                    }
                }
                print();
            } else {
                robotData.getDir();
                direction = robotData.finalDir[index1+1];
                robotData.printDir(index1+1);
                index1++;
            }

        }
        if(pollRun == 0){
            startX = robot.getLocation().x;
            startY = robot.getLocation().y;
        }
        robot.face(direction);
        firstHeading(robot);
        if(passNum == 0){
            firstDir = robot.getHeading();
        }
        pollRun++; // Increases passes counter
    }

    // This controller is called when robot is in exploration mode
    private int exploreControl(IRobot robot){
        int exists = nonwallExits(robot);
        int direction = 0;
        switch (exists) {
            // Deadend case
            case 1 -> {
                explorerMode = 0;
                direction = deadEnd(robot);
                robotData.dirRemover();
                //robotData.arrayCounter--;
                if (robotData.junctionCounter == 0){
                    firstDir = 0;
                }
            }
            // Corridor case
            case 2 -> {
                direction = corridor(robot);
                robotData.dirSaver(direction);
            }
            // Junction case
            case 3 -> {
                direction = junction(robot);
                robotData.arrayCounter++;
                robotData.dirSaver(direction);
            }
            // Crossroad case
            case 4 -> {
                direction = crossroad(robot);
                robotData.arrayCounter++;
                robotData.dirSaver(direction);
            }
        }
        robotData.dirCounter++;

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
        int heading = robotData.searchJunction(x,y);

        // This statement identifies if robot is on junction or crossroad square
        if(exits > 2){
            // Identifies if there are any passages around
            if(passages > 0){
                // If there are, robot is controlled by "junction" or "crossroad" methods
                switch (exits){
                    case 3 -> direction = junction(robot);
                    case 4 -> direction = crossroad(robot);
                }
                robotData.dirSaver(direction);
                robotData.arrayCounter++;
                // Robot goes to exploration mode
                explorerMode = 1;
            } else {
                // If there are no passages around, robot takes information about his heading when he arrived to this junction and then set opposite heading
                switch (heading){
                    case IRobot.NORTH -> robot.setHeading(IRobot.SOUTH);
                    case IRobot.SOUTH -> robot.setHeading(IRobot.NORTH);
                    case IRobot.EAST -> robot.setHeading(IRobot.WEST);
                    case IRobot.WEST -> robot.setHeading(IRobot.EAST);
                }
                robotData.arrayCounter--;
                robotData.dirRemover();
                // then robot moves ahead
                direction = IRobot.AHEAD;
            }
        } else {
            // If it is not junction or crossroad, robot is controlled by "deadend" or "corridor" methods
            switch (exits){
                case 1 -> {
                    direction = deadEnd(robot);
                    robotData.dirRemover();
                    //robotData.arrayCounter--;
                }
                case 2 -> {
                    direction = corridor(robot);
                    if (previousLoc == 1){
                        firstDir = robot.getHeading();
                    }
                    if(x==startX && y==startY){
                        firstDir = 0;
                        previousLoc = 1;
                    }
                }
            }
        }
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

    private int dirFinder(IRobot robot){
        int loc = robot.getHeading();
        int direction = 0;
        if (loc == IRobot.NORTH) {
            switch (firstDir) {
                case IRobot.NORTH -> direction = IRobot.AHEAD;
                case IRobot.SOUTH -> direction = IRobot.BEHIND;
                case IRobot.EAST -> direction = IRobot.RIGHT;
                case IRobot.WEST -> direction = IRobot.LEFT;
            }
        } else if (loc == IRobot.SOUTH) {
            switch (firstDir) {
                case IRobot.NORTH -> direction = IRobot.BEHIND;
                case IRobot.SOUTH -> direction = IRobot.AHEAD;
                case IRobot.EAST -> direction = IRobot.LEFT;
                case IRobot.WEST -> direction = IRobot.RIGHT;
            }
        } else if (loc == IRobot.EAST) {
            switch (firstDir) {
                case IRobot.NORTH -> direction = IRobot.LEFT;
                case IRobot.SOUTH -> direction = IRobot.RIGHT;
                case IRobot.EAST -> direction = IRobot.AHEAD;
                case IRobot.WEST -> direction = IRobot.BEHIND;
            }
        } else {
            switch (firstDir) {
                case IRobot.NORTH -> direction = IRobot.RIGHT;
                case IRobot.SOUTH -> direction = IRobot.LEFT;
                case IRobot.EAST -> direction = IRobot.BEHIND;
                case IRobot.WEST -> direction = IRobot.AHEAD;
            }
        }
        return direction;
    }
    private void firstHeading(IRobot robot){
        int x = robot.getLocation().x;
        int y = robot.getLocation().y;
        if (startY == y && startX == x){
            passNum = 0;
        }
    }

    private void print(){
        String[] headingArr = {"NORTH","SOUTH","EAST","WEST"};
        String heading = "";
        switch (firstDir){
            case IRobot.NORTH -> heading = headingArr[0];
            case IRobot.SOUTH -> heading = headingArr[1];
            case IRobot.EAST -> heading = headingArr[2];
            case IRobot.WEST -> heading = headingArr[3];
        }
        System.out.println("firstDir: " + heading);
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
    private int[] juncX = new int[maxJunctions]; // X-coordinates of the junctions
    private int[] juncY = new int[maxJunctions]; // Y-coordinates of the junctions
    private int[] arrived = new int[maxJunctions]; // Heading the robot first arrived from
    public int[][] directions = new int[maxJunctions][maxJunctions];
    public int dirCounter = 0;
    public int arrayCounter = 0;
    public int[] finalDir = new int[maxJunctions];

    // Resets junctionCounter value
    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    // Record robot's coordinates and heading when it is at an unexplored junction
    public void recordJunction(int x, int y, int heading){
        juncX[junctionCounter] = x;
        juncY[junctionCounter] = y;
        arrived[junctionCounter] = heading;
    }

    // Identifies if the junction is already recorded
    public int juncIdent(int x, int y){
        int result = 0;
        // Search for coordinates of junction. If it finds, this junction is already recorded
        for (int i = 0; i < maxJunctions; i++){
            if (x == juncX[i] && y == juncY[i]){
                result = 1;
                break;
            }
        }
        return result;
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
        System.out.println("Junction " + (junctionCounter+1) + "(x=" + juncX[junctionCounter] + ",y=" + juncY[junctionCounter] +") heading " + heading);
    }

    // Search for robot's heading when it visited junction first time
    public int searchJunction(int x, int y){
        int index = 0;
        for (int j = 0; j < juncX.length; j++) {
            if (juncX[j] == x && juncY[j] == y) {
                index = j;
                break;
            }
        }
        // Returns heading
        return arrived[index];
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
        int counter = 0;
        for (int i = 0; i < directions.length; i++){
            for (int j = 0; j < directions.length; j++){
                if (directions[i][j] != 0) {
                    finalDir[counter] = directions[i][j];
                    counter++;
                }
            }
        }
    }

    public void printDir(int i){
        String[] headingArr = {"AHEAD","BEHIND","RIGHT","LEFT"};
        String heading = "";
        switch (finalDir[i]){
            case IRobot.AHEAD -> heading = headingArr[0];
            case IRobot.BEHIND -> heading = headingArr[1];
            case IRobot.RIGHT -> heading = headingArr[2];
            case IRobot.LEFT -> heading = headingArr[3];
        }
        System.out.println("Direction: "+heading);
    }

}