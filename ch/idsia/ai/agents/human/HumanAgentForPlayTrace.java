package ch.idsia.ai.agents.human;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Sergey Karakovskiy
 * Date: Mar 29, 2009
 * Time: 12:19:49 AM
 * Package: ch.idsia.ai.agents.ai;
 */
public class HumanAgentForPlayTrace extends KeyAdapter implements Agent
{
    List<boolean[]> history = new ArrayList<boolean[]>();
    private boolean[] Action = null;
    private String Name = "HumanKeyboardAgent";
    int directionFacing = 1; //means he's facing right

    public HumanAgentForPlayTrace()
    {
        this.reset ();
//        RegisterableAgent.registerAgent(this);
    }

    public void reset()
    {
        // Just check you keyboard. Especially arrow buttons and 'A' and 'S'!
        Action = new boolean[Environment.numberOfButtons];
    }

    public boolean[] getAction(Environment observation)
    {

        float distToEnemy = getDistToClosestEnemy(observation);
        float distToGap = getDistToGap(observation);

        byte[][] lvlSceneObs = getAreaAroundMario(observation, 3, 4, 1);
        byte[][] enemySceneObs = getAreaAroundMario(observation, 3, 4, 0);

        for(int i = 0; i < lvlSceneObs.length; i++){
            for(int j = 0; j < lvlSceneObs[0].length; j++) {
                System.out.print(lvlSceneObs[i][j]);
                System.out.print(" ");
            }
        }
        for(int i = 0; i < enemySceneObs.length; i++){
            for(int j = 0; j < enemySceneObs[0].length; j++) {
                System.out.print(enemySceneObs[i][j]);
                System.out.print(" ");
            }
        }

//        if (observation.canShoot()) {
//            System.out.print("1 ");
//        } else {
//            System.out.print("-1 ");
//        }
//
//        if (observation.isMarioCarrying()) {
//            System.out.print("1 ");
//        } else {
//            System.out.print("-1 ");
//        }
//
//        if (observation.isMarioOnGround()) {
//            System.out.print("1 ");
//        } else {
//            System.out.print("-1 ");
//        }
//
//        System.out.print(directionFacing + " ");
//
//        System.out.print(distToEnemy + " ");
//
//        System.out.print(distToGap + " ");
//
//        System.out.print(observation.getMarioMode());

        System.out.print(",");
        for(int i = 0; i < Action.length; i++) {
            if(Action[i] == false){
                System.out.print("-1");
            }
            else{
                System.out.print("1");
            }
            if (i != Action.length-1) {
                System.out.print(" ");
            }
        }

        System.out.println();

        return Action;
    }

    public AGENT_TYPE getType() {        return AGENT_TYPE.HUMAN;    }

    public String getName() {   return Name; }

    public void setName(String name) {        Name = name;    }

    public void keyPressed (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), true);
    }

    public void keyReleased (KeyEvent e)
    {
        toggleKey(e.getKeyCode(), false);
    }

    private void toggleKey(int keyCode, boolean isPressed) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                if (directionFacing == 1) {
                    directionFacing = -1;
                //    System.out.println("switched dir");
                }
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
                if (directionFacing == -1) {
              //      System.out.println("switched dir");
                    directionFacing = 1;
                }
                break;
            case KeyEvent.VK_DOWN:
                Action[Mario.KEY_DOWN] = isPressed;
                break;

            case KeyEvent.VK_S:
                Action[Mario.KEY_JUMP] = isPressed;
                break;
            case KeyEvent.VK_A:
                Action[Mario.KEY_SPEED] = isPressed;
                break;
        }
    }

    public List<boolean[]> getHistory () {
        return history;
    }

    public byte[][] getAreaAroundMario(Environment observation, int xWidth, int yHeight, int flag) {
        int marioX = 11;
        int marioY = 11;
        int realX = xWidth*2+1;
        int realY = yHeight*2;
        byte[][] area = new byte[yHeight*2][xWidth*2+1];
        byte[][] levelObservation;
        if(flag == 1) {
            levelObservation = observation.getLevelSceneObservationZ(1);
        }
        else {
            levelObservation = observation.getEnemiesObservationZ(1);
        }
        System.out.println("we suck at math: " + realX + ", " + realY );
        int xLoc = 0;
        int yLoc = 0;
        for (int y = -yHeight; y < yHeight; y++) {
            xLoc = 0;
            for (int x = -xWidth; x <= xWidth; x++) {
//                System.out.printf("(lvlX, lvlY): (%d, %d)\n", marioX+x, marioY+y);
//                System.out.printf("(xLoc, yLoc): (%d, %d)\n", xLoc, yLoc);
//                System.out.printf("lvlObservation[marioY+y][marioX+x]: %d\n",levelObservation[marioY+y][marioX+x]);
                area[yLoc][xLoc] = levelObservation[marioY+y][marioX+x];
//                System.out.printf("area[yLoc][xLoc] = %d\n", area[yLoc][xLoc]);
                xLoc++;
            }
            yLoc++;
        }
        return area;
    }

    public float getDistToClosestEnemy(Environment observation) {
        float[] enemies = observation.getEnemiesFloatPos();
        float[] mario = observation.getMarioFloatPos();
        if (enemies.length ==  0) {
            return 0f; //definitely v far away (about length of screen * 1.5)
        } else {
            for (int i = 2; i < enemies.length; i+=3) {
                float closestEnemyX = enemies[i-1];
                float closestEnemyY = enemies[i];
//            System.out.println("enemyY: " + closestEnemyY);
//            System.out.println("marioY: " + mario[1]);
                if (Math.abs(closestEnemyY - mario[1]) < 5) {
                    float xDistToEnemy = mario[0] - closestEnemyX;
                    if (xDistToEnemy < 16) {
                        return 1f;
                    } else if (xDistToEnemy < 32) {
                        return 0.66f;
                    } else if (xDistToEnemy < 48) {
                        return 0.33f;
                    }
                }
            }
        }
        //none on my level
        return 0f; //this is maybe bad, want to say it's on screen but
        // not on same y level so picked an arbitrary number...

    }

    public float getDistToGap(Environment observation) {
        int marioX = 11;
        int marioY = 11;

        byte[][] obs = observation.getLevelSceneObservation();
        boolean haveGap = true;
        for (int j = 1; j < 4; j++) {
            haveGap = true;
            if (obs[marioY][marioX + j] == 0) {
                for (int i = 1; i < 10; i++) {
                    if (obs[marioY + i][marioX + j] != 0) {
                        haveGap = false;
                        break;
                    }
                }
                if (haveGap) {
                    if (j == 1) {
                        return 1f;
                    } else if (j == 2) {
                        return .66f;
                    } else if (j == 3) {
                        return .33f;
                    }
                }
            }
        }
        return 0f;
    }
}
