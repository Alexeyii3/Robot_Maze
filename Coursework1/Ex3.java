/*
 * File:    Broken	.java
 * Created: 7 September 2001
 * Author:  Stephen Jarvis
 */
/* I have decided to make my headingController through statements tree.These statements firstly check absolute location of target from the robot.
 * After that statements check if there are walls on the way which lets robot move closely to the target. If there is no such opportunity,
 * robot will move in a random direction. In my opinion, such logic allows to be sure that robot will move closer to the target if it is possible.
 * By the way, robot might not reach the target: for instance, if target is in the south and robot goes to the south and hits a deadend.
 * In this case, robot moves to the north because this is the only possible move. After that, robot moves back to the deadend because this move gets it closer
 * to the target. So robot permanently goes up and down. Probably, good improvement is some kind of algorithm that will prevent robot from such issues.  */

import uk.ac.warwick.dcs.maze.logic.IRobot;

public class Ex3 {

    public void controlRobot(IRobot robot) {
        //this part of code is used for testing
        int heading = headingController(robot);
        ControlTest.test(heading, robot);
        robot.setHeading(heading);

        int loc = robot.getHeading();
        int direction = 0;

        //these statements identify direction robot should move based on absolute direction returned by headinController method
        if (loc == IRobot.NORTH) {
            switch (heading) {
                case IRobot.NORTH -> direction = IRobot.AHEAD;
                case IRobot.SOUTH -> direction = IRobot.BEHIND;
                case IRobot.EAST -> direction = IRobot.RIGHT;
                case IRobot.WEST -> direction = IRobot.LEFT;
            }
        } else if (loc == IRobot.SOUTH) {
            switch (heading) {
                case IRobot.NORTH -> direction = IRobot.BEHIND;
                case IRobot.SOUTH -> direction = IRobot.AHEAD;
                case IRobot.EAST -> direction = IRobot.LEFT;
                case IRobot.WEST -> direction = IRobot.RIGHT;
            }
        } else if (loc == IRobot.EAST) {
            switch (heading) {
                case IRobot.NORTH -> direction = IRobot.LEFT;
                case IRobot.SOUTH -> direction = IRobot.RIGHT;
                case IRobot.EAST -> direction = IRobot.AHEAD;
                case IRobot.WEST -> direction = IRobot.BEHIND;
            }
        } else {
            switch (heading) {
                case IRobot.NORTH -> direction = IRobot.RIGHT;
                case IRobot.SOUTH -> direction = IRobot.LEFT;
                case IRobot.EAST -> direction = IRobot.BEHIND;
                case IRobot.WEST -> direction = IRobot.AHEAD;
            }
        }

        //this code faces code towards needed direction
        robot.face(direction);
    }

    //This method identifies if target located in the north from the robot's location
    private static byte isTargetNorth(IRobot robot) {
        byte result;
        int target = robot.getTargetLocation().y;
        int robot_loc = robot.getLocation().y;
        if (target < robot_loc) {
            result = 1;
        } else if (target == robot_loc) {
            result = 0;
        } else {
            result = -1;
        }
        return result;
    }

    //This method identifies if target located in the east from the robot's location
    private static byte isTargetEast(IRobot robot) {
        byte result;
        int target = robot.getTargetLocation().x;
        int robot_loc = robot.getLocation().x;
        if (target > robot_loc) {
            result = 1;
        } else if (target == robot_loc) {
            result = 0;
        } else {
            result = -1;
        }
        return result;
    }

    //This method identifies which squares (wall,passage,beenbefore) surrounds robot
    public static int lookHeading(IRobot robot, int absDir) {
        //this array contains information about squares around robot
        int square[] = new int[4];
        int head = robot.getHeading();

        //this statement put information about squares in array based on heading of robot
        switch (head) {
            case IRobot.NORTH -> {
                square[0] = robot.look(IRobot.AHEAD);
                square[1] = robot.look(IRobot.BEHIND);
                square[2] = robot.look(IRobot.RIGHT);
                square[3] = robot.look(IRobot.LEFT);
            }
            case IRobot.SOUTH -> {
                square[0] = robot.look(IRobot.BEHIND);
                square[1] = robot.look(IRobot.AHEAD);
                square[2] = robot.look(IRobot.LEFT);
                square[3] = robot.look(IRobot.RIGHT);
            }
            case IRobot.EAST -> {
                square[0] = robot.look(IRobot.LEFT);
                square[1] = robot.look(IRobot.RIGHT);
                square[2] = robot.look(IRobot.AHEAD);
                square[3] = robot.look(IRobot.BEHIND);
            }
            case IRobot.WEST -> {
                square[0] = robot.look(IRobot.RIGHT);
                square[1] = robot.look(IRobot.LEFT);
                square[2] = robot.look(IRobot.BEHIND);
                square[3] = robot.look(IRobot.AHEAD);
            }
        }

        //this statement returns one of the element of array based on issued heading
        if (absDir == IRobot.NORTH) {
            return square[0];
        } else if (absDir == IRobot.SOUTH) {
            return square[1];
        } else if (absDir == IRobot.EAST) {
            return square[2];
        } else if (absDir == IRobot.WEST) {
            return square[3];
        } else {
            return 0;
        }
    }

    //this method returns the heading where robot should move
    public static int headingController(IRobot robot) {
        //these variables contain information about states of squares around robot
        int north = lookHeading(robot, IRobot.NORTH);
        int south = lookHeading(robot, IRobot.SOUTH);
        int east = lookHeading(robot, IRobot.EAST);
        int west = lookHeading(robot, IRobot.WEST);

        //this variable contains heading robot will move
        int dir = 0;
        //this variable contains random number
        int rand;
        //these statements identify absolute direction the robot should move
        //first statement identifies if target located in the north from the robot
        if (isTargetNorth(robot) == 1) {
            //this statement identifies if target located in the east from the robot
            if (isTargetEast(robot) == 1) {
                //this statement checks if there is a wall on the way that will move the robot closer to the target
                if (north != IRobot.WALL | south != IRobot.WALL) {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && north != IRobot.WALL) {
                            dir = IRobot.NORTH;
                            break;
                        } else if (rand == 1 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        }
                    }
                } else {
                    //this loop finds the direction robot will move otherwise
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        } else if (rand == 1 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        }
                    }
                }
                //this statement identifies if target located on the same level as robot between east and west (target is still located in the north)
            } else if (isTargetEast(robot) == 0) {
                if (north != IRobot.WALL) {
                    dir = IRobot.NORTH;
                } else {
                    //this loop finds random route is the best one is unavailable
                    while (true) {
                        rand = (int) (Math.random() * 3);
                        if (rand == 0 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        } else if (rand == 1 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        } else if (rand == 2 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        }
                    }
                }
                //this statement is launched if target located in the west from the robot (target is still located in the north)
            } else {
                //this statement checks if there is a wall on the way that will move the robot closer to the target
                if (north != IRobot.WALL | west != IRobot.WALL) {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && north != IRobot.WALL) {
                            dir = IRobot.NORTH;
                            break;
                        } else if (rand == 1 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        }
                    }
                } else {
                    //this loop finds the direction robot will move otherwise
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        } else if (rand == 1 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        }
                    }
                }
            }
            //this statement identifies if target is located on the same level between north and south as the robot, this repeats the logic of the first statement
        } else if (isTargetNorth(robot) == 0) {
            if (isTargetEast(robot) == 1) {
                if (east != IRobot.WALL) {
                    dir = IRobot.EAST;
                } else {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        } else if (rand == 1 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        }
                    }
                }
            } else if (isTargetEast(robot) == 0) {
                while (true) {
                    rand = (int) (Math.random() * 4);
                    if (rand == 0 && west != IRobot.WALL) {
                        dir = IRobot.WEST;
                        break;
                    } else if (rand == 1 && east != IRobot.WALL) {
                        dir = IRobot.EAST;
                        break;
                    } else if (rand == 2 && south != IRobot.WALL) {
                        dir = IRobot.SOUTH;
                        break;
                    } else if (rand == 3 && north != IRobot.WALL) {
                        dir = IRobot.NORTH;
                        break;
                    }
                }
            } else {
                if (west != IRobot.WALL) {
                    dir = IRobot.WEST;
                } else {
                    while (true) {
                        rand = (int) (Math.random() * 3);
                        if (rand == 0 && north != IRobot.WALL) {
                            dir = IRobot.NORTH;
                            break;
                        } else if (rand == 1 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        } else if (rand == 2 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        }
                    }
                }
            }
            //this statement is launched if target is in the south from the robot, this repeats same logic as the first statement
        } else {
            if (isTargetEast(robot) == 1) {
                if (south != IRobot.WALL | east != IRobot.WALL) {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        } else if (rand == 1 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        }
                    }
                } else {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        } else if (rand == 1 && north != IRobot.WALL) {
                            dir = IRobot.NORTH;
                            break;
                        }
                    }
                }
            } else if (isTargetEast(robot) == 0) {
                if (south != IRobot.WALL) {
                    dir = IRobot.SOUTH;
                } else {
                    while (true) {
                        rand = (int) (Math.random() * 3);
                        if (rand == 0 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        } else if (rand == 1 && north != IRobot.WALL) {
                            dir = IRobot.NORTH;
                            break;
                        } else if (rand == 2 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        }
                    }
                }
            } else {
                if (south != IRobot.WALL | west != IRobot.WALL) {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && south != IRobot.WALL) {
                            dir = IRobot.SOUTH;
                            break;
                        } else if (rand == 1 && west != IRobot.WALL) {
                            dir = IRobot.WEST;
                            break;
                        }
                    }
                } else {
                    while (true) {
                        rand = (int) (Math.random() * 2);
                        if (rand == 0 && north != IRobot.WALL) {
                            dir = IRobot.NORTH;
                            break;
                        } else if (rand == 1 && east != IRobot.WALL) {
                            dir = IRobot.EAST;
                            break;
                        }
                    }
                }
            }

        }
        //method returns absolute direction the robot should move
        return dir;
    }

    //this method is used for testing
    public void reset() {
        ControlTest.printResults();
    }

}