/*
 * File: DumboController.java
 * Created: 17 September 2002, 00:34
 * Author: Stephen Jarvis
 */


public class test
{

    public static void main(String args[]) {

        int randno;
        int direction;
        int z = 0;
        int o = 0;
        int t = 0;
        int th = 0;
        // Select a random number



        // Convert this to a direction
        for (int i = 0; i<10001; i++) {
            randno = (int) Math.round(Math.random()*3);
            if (randno == 0)
                z++;
            else if (randno == 1)
                o++;
            else if (randno == 2)
                t++;
            else
                th++;
        }

        System.out.println("zero = " + z);
        System.out.println("one = " + o);
        System.out.println("two = " + t);
        System.out.println("three = " + th);

    }

}