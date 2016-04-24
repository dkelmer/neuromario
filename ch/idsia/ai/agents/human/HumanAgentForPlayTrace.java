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
    private int numRecords = 0;

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

        byte[][] lvlSceneObs = getAreaAroundMario(observation, 3, 2);

        System.out.println("MARIO: ");
        for (byte[] b : lvlSceneObs) {
            System.out.println(Arrays.toString(b));
        }

//        System.out.println("LevelSceneObs: ");
//        lvlSceneObs = observation.getLevelSceneObservation();
//        for (byte[] b : lvlSceneObs) {
//            System.out.println(Arrays.toString(b));
//        }
//
//        System.out.println("EnemyObs: ");
//        lvlSceneObs = observation.getEnemiesObservation();
//        for (byte[] b : lvlSceneObs) {
//            System.out.println(Arrays.toString(b));
//        }
//
//        System.out.println("Combined");
//        lvlSceneObs = observation.getMergedObservationZ(1,0);
//        for (byte[] b : lvlSceneObs) {
//            System.out.println(Arrays.toString(b));
//        }

        System.out.println("==============");

        numRecords++;
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

    public byte[][] getAreaAroundMario(Environment observation, int xWidth, int yHeight) {
        int marioX = 11;
        int marioY = 11;
        byte[][] area = new byte[yHeight*2][xWidth*2+1];
        byte[][] levelObservation = observation.getMergedObservationZ(1,0);
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
