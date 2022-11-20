import uk.ac.warwick.dcs.maze.logic.IRobot;
/*
My code from exercise 2 works perfectly for exercise 3. I did not identify any collisions or crushes by testing this code a lor of times.
BacktrackingControl and exploreControl identify paths correctly. Headings are saved and used correctly.
 */
public class Ex3 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode; // Identifies which controller should be called

    public void controlRobot(IRobot robot){
        // Resets RobotData information for new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData();
            reset();
            explorerMode = 1;
        }

        int direction = 0;

        // Calls needed controller
        switch (explorerMode){
            case 0 -> direction = backtrackControl(robot);
            case 1 -> direction = exploreControl(robot);
        }

        robot.face(direction);
        pollRun++; // Increases passes counter
    }

    // This controller is called when robot is in exploration mode
    private int exploreControl(IRobot robot){
        // Saves robot's heading when it arrives at junction
        if(nonwallExits(robot) > 2){
            robotData.recordJunction(robot.getHeading());
            robotData.print();
            robotData.junctionCounter++;
        }
        int exists = nonwallExits(robot);
        int direction = 0;
        switch (exists) {
            // Deadend case
            case 1 -> {
                explorerMode = 0;
                direction = deadEnd(robot);
            }
            // Corridor case
            case 2 -> {
                direction = corridor(robot);
            }
            // Junction case
            case 3 -> {
                direction = junction(robot);
            }
            // Crossroad case
            case 4 -> {
                direction = crossroad(robot);
            }
        }
        // Direction is returned
        return direction;
    }

    // This controller is called when robot is in backtracking mode
    private int backtrackControl(IRobot robot){
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
                robotData.removeElem();
            }
        } else {
            // If it is not junction or crossroad, robot is controlled by "deadend" or "corridor" methods
            switch (exits){
                case 1 -> direction = deadEnd(robot);
                case 2 -> direction = corridor(robot);
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

    // Resets junctionCounter value
    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    // Record robot's coordinates and heading when it is at an unexplored junction
    public void recordJunction(int heading){
        arrived[junctionCounter] = heading;
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


}