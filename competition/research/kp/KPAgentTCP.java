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
import java.io.*;
import java.net.*;

/**
 * Created by giorgio on 4/25/16.
 */
public class KPAgentTCP implements Agent {
    protected String name = "Kelmer&Pizzorni KPAgent";
    Socket socket;
    OutputStreamWriter osw;
    int directionFacing = 1; //means he's facing right


    public void reset() {}

    public boolean[] getAction(Environment observation) {
        StringBuilder feature = new StringBuilder();
        String response = "";

        float distToEnemy = getDistToClosestEnemy(observation);
        float distToGap = getDistToGap(observation);

        byte[][] lvlSceneObs = getAreaAroundMario(observation, 3, 4, 1);
        byte[][] enemySceneObs = getAreaAroundMario(observation, 3, 4, 0);

        for(int i = 0; i < lvlSceneObs.length; i++){
            for(int j = 0; j < lvlSceneObs[0].length; j++) {
                feature.append(lvlSceneObs[i][j]);
                feature.append(" ");
            }
        }
        for(int i = 0; i < enemySceneObs.length; i++){
            for(int j = 0; j < enemySceneObs[0].length; j++) {
                feature.append(enemySceneObs[i][j]);
                feature.append(" ");
            }
        }

        if (observation.canShoot()) {
            feature.append("1 ");
        } else {
            feature.append("-1 ");
        }

        if (observation.isMarioCarrying()) {
            feature.append("1 ");
        } else {
            feature.append("-1 ");
        }

        if (observation.isMarioOnGround()) {
            feature.append("1 ");
        } else {
            feature.append("-1 ");
        }

        feature.append(directionFacing + " ");

        feature.append(distToEnemy + " ");

        feature.append(distToGap + " ");

        feature.append(observation.getMarioMode());

        feature.append("\n");

        boolean action[] = {false, false, false, false, false};
        try {
            socket = new Socket("localhost", 2016);
            socket.getOutputStream().write(feature.toString().getBytes("US-ASCII"));
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = inFromServer.readLine();
            System.out.println("Response: " + response);
           // socket.close();
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

        byte[][] area = new byte[yHeight*2][xWidth*2];
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
            for (int x = -xWidth; x < xWidth; x++) {
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
