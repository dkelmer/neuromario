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


        byte[][] lvlSceneObs = getAreaAroundMario(observation, 3, 2, 1);
        byte[][] enemySceneObs = getAreaAroundMario(observation, 3, 2, 0);

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


    private void toggleKey(int keyCode, boolean isPressed)
    {
        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                Action[Mario.KEY_LEFT] = isPressed;
                break;
            case KeyEvent.VK_RIGHT:
                Action[Mario.KEY_RIGHT] = isPressed;
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
        byte[][] area = new byte[yHeight*2][xWidth*2+1];
        byte[][] levelObservation;
        if(flag == 1) {
            levelObservation = observation.getLevelSceneObservationZ(1);
        }
        else {
            levelObservation = observation.getEnemiesObservationZ(1);
        }
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
}
