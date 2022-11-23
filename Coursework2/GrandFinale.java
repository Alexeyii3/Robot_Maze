import uk.ac.warwick.dcs.maze.logic.IRobot;

import java.util.Arrays;

/*
    I can not say that I used one of the routes. By the way, I used data structures from route A (2D array) and one of ideas from route B: storing each heading.
     However, my code usually stores directions. Headings are stored only when robot visits junctions. I would say that I implemented my own approach but used
     some ideas from given routes. I used my own approach because I wanted to get familiar with new data structures and create something new on my own.
     My tests did not show any crushes in Loopy Mazes, so I would say my code works for loopy mazes. However, I can not be sure that it works every time because
     it is optimized for prime generator.
     My code works for new mazes. It resets all needed variables and arrays. It is possible to run same maze as many times as you want.
 */
public class GrandFinale {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode; // Identifies which controller should be called
    private int index = 0; // Index of array that contains directions robot uses for 2nd or further runs

    public void controlRobot(IRobot robot){
        int direction = 0; // Variable will contain the direction robot faces

        // Resets RobotData information for new maze
        if ((robot.getRuns() == 0) && (pollRun == 0)) {
            robotData = new RobotData();
            reset();
            explorerMode = 1;
        }

        // If it is a first run, robot explores the maze and saves information
        if (robot.getRuns() == 0) {
            // Calls needed controller
            switch (explorerMode) {
                case 0 -> direction = backtrackControl(robot);
                case 1 -> direction = exploreControl(robot);
            }
        }

        // Resets variables for new run
        if (pollRun == 0){
            index=0;
            explorerMode = 1;
        }

        // Robot uses stored directions for non-first runs
        if (robot.getRuns()>0){
            if (robot.getRuns() == 1 && pollRun == 0) {
                robotData.getFinalDir(); // Creates 1D array with information from 2D array on the second run and first pass
            }
            direction = robotData.finalDir[index]; // Gets direction for each run
            index++; // Counter is increased by 1
            // If value is not a direction, then it is a heading
            if (direction < 2000){
                robot.setHeading(direction); // Sets heading of robot
                direction = IRobot.AHEAD;
            }
        }

        System.out.println("counter: " + robotData.counter);
        robot.face(direction);
        pollRun++; // Increases passes counter
    }


    // This controller is called when robot is in exploring mode
    private int exploreControl(IRobot robot){
        int x = robot.getLocation().x;
        int y = robot.getLocation().y;
        int exits = nonwallExits(robot);
        int direction = 0;

        // If robot is at a junction, his heading is saved and counter of junctions is increased by 1
        if (nonwallExits(robot) > 2) {
            robotData.recordJunction(robot.getHeading());
            robotData.junctionCounter++;
        }

        // On the first run robot does not save direction due to random heading. His heading on the nearest square will be saved
        if(pollRun == 0){
            switch (exits){
                case 1 -> direction = deadEnd(robot);
                case 2 -> direction = corridor(robot);
            }
            robotData.dirSaver(0);
            robotData.dirCounter++;
        } else {
            // If robot  explored at least one of the paths of a junction, and it leads to a deadend, then particular heading is needed to be saved
            if ((robotData.kindOfSquare[robotData.squareCounter-1] == 3 || robotData.kindOfSquare[robotData.squareCounter-1] == 4) && robotData.getElem(0) == 0){
                robotData.dirSaver(robot.getHeading()); // Saves the heading
                robotData.dirCounter++;
                direction = exploringDirection(robot,exits); // Gets the direction from exploringDirection
            } else { // If robot did not go to deadend, information is saved as usual
                direction = exploringDirection(robot,exits);
            }
        }
        // If robot goes through squares that are next to start point, program saves heading the robot has
        if ((x ==1 && y == 2) || (x ==2 && y ==1 )){
            robotData.firstStep = robot.getHeading();
        }
        // Saves kind of square where robot is (deadend, corridor, junction, crossroad)
        robotData.squareSaver(exits);
        // Direction is returned
        return direction;
    }

    // This method chooses direction due to location of robot. It is implemented in exploreControl
    private int exploringDirection(IRobot robot, int exits){
        int direction = 0;
        switch (exits) {
            // Deadend case
            case 1 -> {
                explorerMode = 0;
                direction = deadEnd(robot);
                robotData.dirRemover(); // Removes directions from sub-array if robot goes into deadend
            }
            // Corridor case
            case 2 -> {
                direction = corridor(robot);
                robotData.dirSaver(direction); // Saves direction of robot
                robotData.dirCounter++; // Increases counter of directions
            }
            // Junction case
            case 3 -> {
                direction = junction(robot);
                robotData.arrayCounter++; // Increases counter of sub-arrays
                robotData.dirCounter = 0; // Make direction's counter 0, so information will be saved from the first element of sub-array
                robotData.dirSaver(direction); // Saves direction of robot
                robotData.dirCounter++;
            }
            // Crossroad case
            case 4 -> {
                direction = crossroad(robot);
                robotData.arrayCounter++; // Increases counter of sub-arrays
                robotData.dirCounter = 0; // Make direction's counter 0, so information will be saved from the first element of sub-array
                robotData.dirSaver(direction); // Saves direction of robot
                robotData.dirCounter++; // Increases counter of directions
            }
        }
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
                // Controls that directions to the first junction will be written properly
                if (robotData.directions[robotData.arrayCounter][0] != 0){
                    robotData.arrayCounter++;
                    robotData.dirCounter = 0;
                } else {
                    robotData.dirCounter = 0;
                }

            } else {
                int heading = robotData.searchHeading(); // Takes heading when robot arrived to particular junction
                // If there are no passages around, robot takes information about his heading when he arrived to this junction and then set opposite heading
                switch (heading){
                    case IRobot.NORTH -> robot.setHeading(IRobot.SOUTH);
                    case IRobot.SOUTH -> robot.setHeading(IRobot.NORTH);
                    case IRobot.EAST -> robot.setHeading(IRobot.WEST);
                    case IRobot.WEST -> robot.setHeading(IRobot.EAST);
                }
                // then robot moves ahead
                direction = IRobot.AHEAD;

                // When robot explored all paths from particular junction, and they all lead to deadends. In this case, directions to this
                // junction are removed and robot goes back to previous junction.
                robotData.arrayCounter--;
                robotData.dirRemover();
                robotData.removeElem();
            }
        } else {
            // Controls robot while going through deadend or explored corridor
            if (passageExits(robot) == 0) {
                switch (exits){
                    case 1 -> direction = deadEnd(robot);
                    case 2 -> direction = corridor(robot);
                }
            } else {
                // Saves directions if there are passages
                direction = corridor(robot);
                robotData.dirSaver(direction);
                robotData.dirCounter++;

            }
            // If it is not junction or crossroad, robot is controlled by "deadend" or "corridor" methods
        }

        // If robot goes through squares that are next to start point, program saves heading the robot has
        if ((x ==1 && y == 2) || (x ==2 && y ==1 )){
            robotData.firstStep = robot.getHeading();
        }
        // Saves kind of square where robot is (deadend, corridor, junction, crossroad)
        robotData.squareSaver(exits);
        // Direction is returned
        return direction;
    }


    //This method counts non-wall squares around robot
    public static int nonwallExits(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.BEHIND,IRobot.LEFT,IRobot.RIGHT};
        int amount = 0;
        for (int i = 0; i < 4; i++){
            if (robot.look(sides[i]) != IRobot.WALL){amount++;} // Checks all sides and increases counter by 1 if there is no wall on the side
        }
        return amount;
    }

    //This method counts passages around robot
    private static int passageExits(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.LEFT,IRobot.RIGHT};
        int amount = 0;
        for (int i = 0; i < 3; i++){
            if (robot.look(sides[i]) == IRobot.PASSAGE){ // Checks all sides but behind and increases counter by 1 if there is a passage on the side
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
    public int[] finalDir = new int[1000]; // Final array of directions that are used on 2nd and further runs
    public int[] kindOfSquare = new int [maxJunctions]; // Contains information about each square (either deadend, corridor, junction or crossroad)
    public int[][] directions = new int[maxJunctions][1000]; // Array containing directions during the first run
    public int arrayCounter = 0; // Counter of sub-arrays of "directions" array
    public int dirCounter = 0; // Counter of directions for sub-arrays of "directions" array
    public int squareCounter = 0; // Counter of squares robot visited
    public int counter = 0; // Counter used when making finalDir
    public int firstStep = 0; // This variable will contain heading of robot for the first step


    // Resets junctionCounter value
    public void resetJunctionCounter() {
        junctionCounter = 0;
    }

    // Records robot's heading at the junction
    public void recordJunction(int heading){
        arrived[junctionCounter] = heading;
    }

    // This method searches for heading of robot when it arrived at junction
    public int searchHeading(){
        int result = 0;
        result = arrived[junctionCounter-1];
        return result;
    }

    // This method removes element of array of robot's headings
    public void removeElem(){
        junctionCounter--; // Counter of junctions is decreased by 1
        arrived[junctionCounter] = 0; // Heading of deleted junction is rewritten
    }

    // Saves direction of robot
    public void dirSaver(int dir){
        directions[arrayCounter][dirCounter] = dir;
    }

    // Removes all directions in sub-array
    public void dirRemover(){
        for(int i = 0; i < directions[arrayCounter].length; i++){
            directions[arrayCounter][i] = 0; //all directions in sub-array are changed to 0
        }
        dirCounter = 0; // `Resets counter of directions
    }

    // Creates 1D array of directions of robot
    public void getFinalDir() {
        directions[0][0] = firstStep;// Heading for first step of robot is saved in variable
        if (counter < 1000) {
            for (int i = 0; i < maxJunctions; i++) {
                for (int j = 0; j < 1000; j++) {
                    if (directions[i][j] != 0) {
                        finalDir[counter] = directions[i][j]; // All directions in 2D array storing in 1D array
                        counter++;
                    }
                }
            }
        }
    }

    // Saves information about each square robot visits
    public void squareSaver(int square){
        kindOfSquare[squareCounter] = square; // saves amount of exits
        squareCounter++; // Counter of square increased by 1
    }

    // Gets element from 2D array
    public int getElem(int i){
        return directions[arrayCounter][i];
    }
}