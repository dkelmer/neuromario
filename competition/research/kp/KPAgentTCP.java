package competition.research.kp;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.python.util.PythonInterpreter;
import org.python.core.*;
import java.io.*;
import java.net.*;

/**
 * Created by giorgio on 4/25/16.
 */
public class KPAgentTCP implements Agent {
    protected String name = "Kelmer&Pizzorni KPAgent";
    Socket socket;
    OutputStreamWriter osw;

    public void reset() {}

    public boolean[] getAction(Environment observation) {
        byte[][] lvlSceneObs = getAreaAroundMario(observation, 3, 2, 1);
        byte[][] enemySceneObs = getAreaAroundMario(observation, 3, 2, 0);
        byte[] message = new byte[lvlSceneObs.length + lvlSceneObs[0].length + enemySceneObs.length + enemySceneObs[0].length];
        String world = "";
        String response = "";

        /* Build observation of the world */

        for(int i = 0; i < lvlSceneObs.length; i++){
            for(int j = 0; j < lvlSceneObs[0].length; j++) {
                world = world + " " + (lvlSceneObs[i][j]);
                //message[i+j] = lvlSceneObs[i][j];
            }
        }
        for(int i = 0; i < enemySceneObs.length; i++){
            for(int j = 0; j < enemySceneObs[0].length; j++) {
                world = world + " " + (enemySceneObs[i][j]);
                //message[i+j] = enemySceneObs[i][j];
            }
        }

        /* Misc. Values for Neural Net input */

        if (observation.canShoot()) {
            world += " 1";
        } else {
            world += " -1";
        }

        if (observation.isMarioCarrying()) {
            world += " 1";
        } else {
            world += " -11";
        }

        if (observation.isMarioOnGround()) {
            world += " 1";
        } else {
            world += " -1";
        }

        float distToEnemy = getDistToClosestEnemy(observation);

        world += " 1 "; //hardwiring dir facing for now

        world += distToEnemy;

       // world += " 0 0 0 0";
        world += "\n";
        int len = world.length();
        boolean action[] = {false, false, false, false, false};
        try {
            socket = new Socket("localhost", 2016);
            socket.getOutputStream().write(world.getBytes("US-ASCII"));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = inFromServer.readLine();
            System.out.println("Response: " + response);
            socket.close();
        }
        catch(Exception ignore){}

        String[] stringArray = response.split(" ");
        //boolean[] intArray = new int[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            String numberAsString = stringArray[i];
            int temp = Integer.parseInt(numberAsString);
            if(temp == 1){
                action[i] = true;
            }
            else{
                action[i] = false;
            }
            System.out.println("Action:" + action[i]);
        }
        //socket.close();

        return action;
    }

    public AGENT_TYPE getType() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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
            levelObservation = observation.getEnemiesObservationZ(0);
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

    public float getDistToClosestEnemy(Environment observation) {
        float[] enemies = observation.getEnemiesFloatPos();
        float[] mario = observation.getMarioFloatPos();
        if (enemies.length == 0) {
            return 0f; //definitely v far away (about length of screen * 1.5)
        } else {
            float closestEnemyX = enemies[1];
            float closestEnemyY = enemies[2];
//            System.out.println("enemyY: " + closestEnemyY);
//            System.out.println("marioY: " + mario[1]);
            if (Math.abs(closestEnemyY - mario[1]) < 5) {
                return mario[0] - closestEnemyX;
            } else {
                return 222.0f; //this is maybe bad, want to say it's on screen but
                // not on same y level so picked an arbitrary number...
            }
        }
    }
}
