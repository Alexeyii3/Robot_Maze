import uk.ac.warwick.dcs.maze.logic.IRobot;
/*
I have tried to create efficient program that satisfies exercise requirements. "passageExits" method checks if there are any passages around robot. It does not check
if there is a passage behind the robot because it can not be there. "nonwallExits" method checks how many non-wall squares around the robot. This method helps to decide
which control method should be called next. "deadEnd" method controls robot if there is only 1 non-wall square around it. This method also handles the case of the
really first run, when robot is at the deadend but non-wall square could be not behind. In this case it will find such square and go there. "corridor" method checks
where robot should go if there are 2 non-wall exits. In this case robot could go ahead,right or left. "junction" and "crossroad" method are actually the same. They
decide where robot will go based on amount of passages and non-wall exits around robot. If there are more than 1 passages, they choose randomly between them. If
there are no passages, robot goes to backtracking. RobotData contains coordinates of visited junctions and crossroads and heading of robot when it first discovered it.
It helps to identify where robot should go if it visits fully explored junction or crossroad. "exploreControl" controls robot when it explores a maze. If robot goes
at deadend, "backtrackingControl" is called. It makes robot to come back to the latest junction and explore unexplored routes. There are some repeating code lines
if robot goes through corridor of if it is at the deadend. "corridor" and "deadend" are called in this case.
Robot always is going to achieve target if amount of junctions and crossroads is less than 10 000 because it will explore all squares. The
maximum of steps is 1000.
 */

public class Ex1 {
    private int pollRun = 0; // Incremented after each pass
    private RobotData robotData; // Data store for junctions
    private int explorerMode; // Identifies which controller should be called

    public void controlRobot(IRobot robot){
        int x = robot.getLocation().x;
        int y = robot.getLocation().y;

        // Saves information about previously unexplored junction or crossroad
        if(nonwallExits(robot) > 2 && robotData.juncIdent(x,y) == 0){
            robotData.recordJunction(x,y,robot.getHeading());
            robotData.print();
            robotData.junctionCounter++;
        }

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
        if(exits > 2){
            if(passages > 0){
                switch (exits){
                    case 3 -> direction = junction(robot);
                    case 4 -> direction = crossroad(robot);
                }
                explorerMode = 1;
            } else {
                switch (heading){
                    case IRobot.NORTH -> robot.setHeading(IRobot.SOUTH);
                    case IRobot.SOUTH -> robot.setHeading(IRobot.NORTH);
                    case IRobot.EAST -> robot.setHeading(IRobot.WEST);
                    case IRobot.WEST -> robot.setHeading(IRobot.EAST);
                }
                direction = IRobot.AHEAD;
            }
        } else {
            switch (exits){
                case 1 -> direction = deadEnd(robot);
                case 2 -> direction = corridor(robot);
            }
        }
        return direction;
    }

    public static int nonwallExits(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.BEHIND,IRobot.LEFT,IRobot.RIGHT};
        int amount = 0;
        for (int i = 0; i < 4; i++){
            if (robot.look(sides[i]) != IRobot.WALL){amount++;}
        }
        return amount;
    }

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

    private static int deadEnd(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.LEFT,IRobot.RIGHT};
        int result = 0;
        if (robot.look(IRobot.BEHIND) != IRobot.WALL){
            result = IRobot.BEHIND;
        } else {
            for (int i = 0; i < 3; i++){
                if (robot.look(sides[i]) != IRobot.WALL){
                    result = sides[i];
                }
            }
        }
        return result;
    }

    private static int corridor(IRobot robot){
        int[] sides = {IRobot.AHEAD,IRobot.LEFT,IRobot.RIGHT};
        int result = 0;
        for (int i = 0; i < 3; i++) {
            if (robot.look(sides[i]) != IRobot.WALL) {
                result = sides[i];
            }
        }
        return result;
    }

    private static int junction(IRobot robot){
        int result = 0;
        if (passageExits(robot) > 0){
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
        return result;
    }

    private static int crossroad(IRobot robot){
        int result = 0;
        if (passageExits(robot) > 0){
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
        return result;
    }


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

    public void resetJunctionCounter() {
        junctionCounter = 0;
    }
    public void recordJunction(int x, int y, int heading){
        juncX[junctionCounter] = x;
        juncY[junctionCounter] = y;
        arrived[junctionCounter] = heading;
    }
    public int juncIdent(int x, int y){
        int result = 0;
        for (int i = 0; i < maxJunctions; i++){
            if (x == juncX[i] && y == juncY[i]){
                result = 1;
                break;
            }
        }
        return result;
    }
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
    public int searchJunction(int x, int y){
        int index = 0;
        for (int j = 0; j < juncX.length; j++) {
            if (juncX[j] == x && juncY[j] == y) {
                index = j;
                break;
            }
        }
        return arrived[index];
    }

}