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

/**
 * Created by giorgio on 4/25/16.
 */
public class KPAgent implements Agent {
    protected String name = "Kelmer&Pizzorni KPAgent";
    String[] command = {"python", "/Users/giorgio/projects/neuromario/competition/research/kp/predict.py", ""};
    public void reset() {

    }

    public boolean[] getAction(Environment observation) {
        byte[][] lvlSceneObs = getAreaAroundMario(observation, 3, 2);
        String world = "";

        for(int i = 0; i < lvlSceneObs.length; i++){
            for(int j = 0; j < lvlSceneObs[0].length; j++) {
                world = world + " " + (lvlSceneObs[i][j]);
            }
        }
        command[2] = world;
        ProcessBuilder p = new ProcessBuilder(command);
        boolean action[] = {false, false, false, false, false};
        try {
            Process process = p.start();
            process.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.println(result);
            String[] stringArray = result.split(" ");
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
            }
        }
        catch (Exception ignored){}


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
