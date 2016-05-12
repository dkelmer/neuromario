package competition.cig.robinbaumgarten;

import ch.idsia.ai.agents.Agent;
import ch.idsia.mario.environments.Environment;

import competition.cig.robinbaumgarten.astar.AStarSimulator;
import competition.cig.robinbaumgarten.astar.sprites.Mario;

public class AStarAgent implements Agent
{
    protected boolean action[] = new boolean[Environment.numberOfButtons];
    protected String name = "RobinBaumgarten_AStarAgent";
    private AStarSimulator sim;
    private int tickCounter = 0;
    private float lastX = 0;
    private float lastY = 0;
	int errCount = 0;
	AStarAgent errAgent;
	int directionFacing = 1; //means he's facing right

	public StringBuilder feature = new StringBuilder();
	public StringBuilder target = new StringBuilder();

    public void reset()
    {
        action = new boolean[Environment.numberOfButtons];
        sim = new AStarSimulator();
    }
    
    public void printLevel(byte[][] levelScene)
    {
    	for (int i = 0; i < levelScene.length; i++)
    	{
    		for (int j = 0; j < levelScene[i].length; j++)
    		{
    			System.out.print(levelScene[i][j]+"\t");
    		}
    		System.out.println("");
    	}
    }

    public boolean[] getAction(Environment observation)
    {
    	long startTime = System.currentTimeMillis();
    	tickCounter++;
    	String s = "Fire";
    	if (!sim.levelScene.mario.fire)
    		s = "Large";
    	if (!sim.levelScene.mario.large)
    		s = "Small";
    	if (sim.levelScene.verbose > 0) System.out.println("Next action! Tick " + tickCounter + " Simulated Mariosize: " + s);

    	boolean[] ac = new boolean[5];
    	ac[Mario.KEY_RIGHT] = true;
    	ac[Mario.KEY_SPEED] = true;
    	
    	//byte[][] scene = observation.getCompleteObservation();//observation.getLevelSceneObservation(0);
    	byte[][] scene = observation.getLevelSceneObservationZ(0);
    	float[] enemies = observation.getEnemiesFloatPos();
    	
    	//observation.getCompleteObservation();
    	//System.out.println("Clean scene:");
    	//printLevel(scene);
    	
    	//System.out.println("Complete Obs:");
    	//printLevel(observation.getCompleteObservation());
    	
    	if (sim.levelScene.verbose > 2) System.out.println("Simulating using action: " + sim.printAction(action));
        sim.advanceStep(action);   
    	
        if (sim.levelScene.verbose > 5) System.out.println("Simulated sprites: ");
        if (sim.levelScene.verbose > 5) sim.levelScene.dumpSprites();
        
    	//System.out.println("Internal scene after sim:");
        //printLevel(sim.levelScene.levelSceneObservation(0));
        
        //printLevel(sim.levelScene.levelSceneObservation(0));
		float[] f = observation.getMarioFloatPos();
		if (sim.levelScene.verbose > 5)
			System.out.println("Sim Mario Pos: " 
					+ sim.levelScene.mario.x + " " + sim.levelScene.mario.y + " " +
					" a: " + sim.levelScene.mario.xa + " " + sim.levelScene.mario.ya );
		if (sim.levelScene.mario.x != f[0] || sim.levelScene.mario.y != f[1])
		{
			if (f[0] == lastX && f[1] == lastY)
				return ac;
			//System.out.print("i");
			if (sim.levelScene.verbose > 0) System.out.println("INACURATEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE!");
			if (sim.levelScene.verbose > 0) System.out.println("Real: "+f[0]+" "+f[1]
			      + " Est: "+ sim.levelScene.mario.x + " " + sim.levelScene.mario.y +
			      " Diff: " + (f[0]- sim.levelScene.mario.x) + " " + (f[1]-sim.levelScene.mario.y));
			sim.levelScene.mario.x = f[0];
			sim.levelScene.mario.xa = (f[0] - lastX) *0.89f;
			if (Math.abs(sim.levelScene.mario.y - f[1]) > 0.1f)
				sim.levelScene.mario.ya = (f[1] - lastY) * 0.85f;// + 3f;

			sim.levelScene.mario.y = f[1];
			errCount++;
			//if (errCount > 1)
			//	errAgent.lastX++;
		}
		sim.setLevelPart(scene, enemies);
        
		lastX = f[0];
		lastY = f[1];

        action = sim.optimise();
        
        if (sim.levelScene.verbose > 1) System.out.println("Returning action: " + sim.printAction(action));
        sim.timeBudget += 39 - (int)(System.currentTimeMillis() - startTime);

		float distToEnemy = getDistToClosestEnemy(observation);
		float distToGap = getDistToGap(observation);
		updateDirection(action);

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

//        System.out.print(",");
		for(int i = 0; i < action.length; i++) {
			if(action[i] == false){
				target.append("-1");
			}
			else{
				target.append("1");
			}
			if (i != action.length-1) {
				target.append(" ");
			}
		}
		target.append("\n");

        return action;
    }

	private void updateDirection(boolean[] action) {
		if (directionFacing == 1 && action[ch.idsia.mario.engine.sprites.Mario.KEY_LEFT]) {
			directionFacing = -1;
			return;
		}
		if (directionFacing == -1 && action[ch.idsia.mario.engine.sprites.Mario.KEY_RIGHT]) {
			directionFacing = 1;
			return;
		}
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

    public AGENT_TYPE getType()
    {
        return Agent.AGENT_TYPE.AI;
    }

    public String getName() 
    {        
    	return name;    
    }

    public void setName(String Name) 
    { 
    	this.name = Name;    
    }
}
