package competition.icegic.peterlawford;

import java.awt.Color;
import java.util.LinkedList;

import ch.idsia.ai.agents.Agent;
import ch.idsia.ai.agents.ai.BasicAIAgent;
import competition.icegic.peterlawford.search_algs.AStarThreaded;
import competition.icegic.peterlawford.search_algs.AbstractSearch;
import competition.icegic.peterlawford.search_algs.BlockEvaluator;
import competition.icegic.peterlawford.simulator.ClonedEnvironment;
import competition.icegic.peterlawford.simulator.Frame;
import competition.icegic.peterlawford.simulator.TheoreticEnemies;
import competition.icegic.peterlawford.simulator.TheoreticLevel;
import competition.icegic.peterlawford.simulator.TheoreticMario;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class SlowAgent extends BasicAIAgent implements Agent {

	public static final boolean DEBUG = false;

	public static final int MAX_PATH_LENGTH = 0;

	private LinkedList<PathInfo> best_path = new LinkedList<PathInfo>();

	class PathInfo {
		final float nSpeedX;
		final float nSpeedY;
		boolean fOnGround;
		public PathInfo(float nSpeedX, float nSpeedY, boolean fOnGround) {
			super();
			this.nSpeedX = nSpeedX;
			this.nSpeedY = nSpeedY;
			this.fOnGround = fOnGround;
		}

		public String toString() {
			if ((nSpeedX > 0) && (nSpeedY > 0)) return "↗";
			if ((nSpeedX > 0) && (nSpeedY < 0)) return "↘";
			if ((nSpeedX < 0) && (nSpeedY < 0)) return "↙";
			if ((nSpeedX < 0) && (nSpeedY > 0)) return "↖";
			if ((nSpeedX > 0) && (nSpeedY == 0)) return "→";
			if ((nSpeedX < 0) && (nSpeedY == 0)) return "←";
			if ((nSpeedX == 0) && (nSpeedY > 0)) return "↑";
			if ((nSpeedX == 0) && (nSpeedY < 0)) return "↓";
			return "*";
		}
	}

	public SlowAgent() {
		super("SlowAgent");
		reset();
	}

	class Observations {
		final byte[][] all;
		final byte[][] enemies;
		final byte[][] scene;
		public Observations(byte[][] all, byte[][] enemies, byte[][] scene) {
			super();
			this.all = all;
			this.enemies = enemies;
			this.scene = scene;
		}
	}

	//	private byte[][] obsAllPrev = null;
	//	private byte[][] obsAllPrevPrev = null;

	//	private byte[][] obsEnemyPrev = null;
	//	private byte[][] obsEnemyPrevPrev = null;

	private byte[][] obsScenePrev = null;
	//	private byte[][] obsScenePrevPrev = null;
	//	private byte[][] obsScenePrevPrevPrev = null;

	boolean fFlyingPrev = true;
	boolean fFlyingPrevPrev = true;
	//	boolean fFlyingPrevPrevPrev = true;

	int dir = -1;
	int dirPrev = -1;
	int dirPrevPrev = -1;

	int jump_count = 0;


	boolean fEmergencyHoleJump = false;

	int getXDir(int nCode) {
		if ((nCode == 0) || (nCode == 3) || (nCode == 6)) return -1;
		if ((nCode == 2) || (nCode == 5) || (nCode == 8)) return 1;
		return 0;
	}
	int getYDir(int nCode) {
		if ((nCode == 0) || (nCode == 1) || (nCode == 2)) return 1;
		if ((nCode == 6) || (nCode == 7) || (nCode == 8)) return -1;
		return 0;
	}

	private float calcSpeedY(float n1, float n2, float n3) {
		if ((n1 >= 0) && (n2 >= 0) && (n3 >= 0))
			return (n1+n2+n3)/3;
		if ((n1 <= 0) && (n2 <= 0) && (n3 <= 0))
			return (n1+n2+n3)/3;
		if ((n1 >= 0) && (n2 >= 0)) 
			return (n1+n2)/2;
		if ((n1 <= 0) && (n2 <= 0)) 
			return (n1+n2)/2;
		return n1;
	}
	private float calcSpeedX(float n1, float n2, float n3) {
		if ((n1 >= 0) && (n2 >= 0) && (n3 >= 0))
			return (n1+n2+n3)/3;
		if ((n1 <= 0) && (n2 <= 0) && (n3 <= 0))
			return (n1+n2+n3)/3;
		if ((n1 >= 0) && (n2 >= 0)) 
			return (n1+n2)/2;
		if ((n1 <= 0) && (n2 <= 0)) 
			return (n1+n2)/2;
		return n1;
	}

	public static class DisplayPathInfo {
		public final LinkedList<float[]> path = new LinkedList<float[]>();
		public Color color;
		public DisplayPathInfo(Color color) {
			this.color = color;
		}
		public void push(float[] x) {
			path.add(x);
		}		
	}

	BlockEvaluator block_eval = new BlockEvaluator();

	AbstractSearch a_star = new AStarThreaded();

	float old_mario_x = -1;
	float old_mario_y = -1;


	@Override
	public boolean[] getAction(Environment env) {
		Environment env_orig = env;
		env = new ClonedEnvironment(env);

		float mario_x = env.getMarioFloatPos()[0];
		float mario_y = env.getMarioFloatPos()[1];

		if ((mario_x == 32) && (mario_y == 0)) {
			reset();
		}

		old_mario_x = mario_x;
		old_mario_y = mario_y;


		if ((f1 != null) && block_eval.fIsEndpoint) f1.mario.status = Mario.STATUS_WIN;

		byte[][] obs = env.getCompleteObservation();
		byte[][] obsScene = env.getLevelSceneObservation();

		block_eval.evaluate(env);


		dirPrevPrev = dirPrev;
		dirPrev = dir;
		if (obsScenePrev != null)
			dir = deltaScene2(obsScenePrev, obsScene);
		int nX1 = getXDir(dir);
		int nY1 = getYDir(dir);
		int nX2 = getXDir(dirPrev);
		int nY2 = getYDir(dirPrev);
		int nX3 = getXDir(dirPrevPrev);
		int nY3 = getYDir(dirPrevPrev);

		float nSpeedY = calcSpeedY(nY1,nY2,nY3);
		float nSpeedX = calcSpeedX(nX1,nX2,nX3);
		boolean fFalling = nSpeedY < 0;

		fFlyingPrevPrev = fFlyingPrev;
		fFlyingPrev = env.isMarioOnGround();


		if (env.isMarioOnGround()) {
			jump_count = 0;
			fEmergencyHoleJump = false;
		}

		obsScenePrev = obsScene;




		TheoreticMario mario_t = (f1 != null) ? f1.mario : 
			TheoreticMario.getInitialMarioLevelStart(env, level);

		TheoreticLevel.updateImage(env);

		TheoreticEnemies enemies_t = (f1 != null) ? f1.enemies : new TheoreticEnemies(level);
		enemies_t.processEnemyInfo(env, mario_t);

		Frame f0;
		if (f1 == null) {
			f0 = new Frame(this, mario_t, enemies_t, 0);
		} else {
			f0 = new Frame(
					//				((MarioComponent)env).mario.world,
					this,
					new TheoreticMario(level, 
							env.getMarioFloatPos()[0],
							env.getMarioFloatPos()[1],
							env.isMarioOnGround(), prevWasOnGround,
							env.mayMarioJump(),
							(env.getMarioMode() != 0),
							(env.getMarioMode() == 2),
							f1.mario, enemies_t),
							enemies_t, f1.fireballsOnScreen);
		}



		int coded_action = a_star.a_star(env.getMarioFloatPos()[0],
				env.getMarioFloatPos()[1], f0, null, block_eval);
		action = AbstractSearch.coded_keys[coded_action];

		//				action = actor0.suggestAction(action, env, nSpeedX, nSpeedY);
		//				projectAction(action, env, nSpeedX, nSpeedY);



		f1 = new Frame(f0, SlowAgent.DEBUG);
		f1.move(action);

		prevWasOnGround = env.isMarioOnGround();

		return action;
	}
	Frame f1 = null;
	final TheoreticLevel level = new TheoreticLevel();
	boolean prevWasOnGround = false;


	DisplayPathInfo guess;

	static final int CLEAR = 0;
	static final int COIN = 34;


	private int deltaScene2(byte[][] oldSc, byte[][] newSc) {
		boolean fMaxOneResult = true;
		boolean fMinOneResult = false;

		boolean[][] guess = deltaScene1(oldSc, newSc);
		int nResult = -1;

		for (int i=0; i<9; i++) {
			boolean t = guess[i/3][i%3];
			fMaxOneResult = fMaxOneResult && !(fMinOneResult && t);
			fMinOneResult = fMinOneResult || t;
			if (t) {
				nResult = i;
			}
		}
		if (!fMaxOneResult) return -1;
		return nResult;
	}

	private boolean[][] deltaScene1(byte[][] oldSc, byte[][] newSc) {
		boolean[][] possBits = new boolean[3][3];
		for (int k=-1; k<2; k++) {
			for (int l=-1; l<2; l++) {
				possBits[k+1][l+1] = true;
			}
		}

		for (int i=0; i<22; i++)
			for (int j=0; j<22; j++) {
				for (int k=-1; k<2; k++) {
					for (int l=-1; l<2; l++) {
						if ((i+k < 0) || (i+k > 21) ||
								(j+l < 0) || (j+l > 21))
							continue;
						if ( (oldSc[i][j] == 1) || (newSc[i+k][j+l] == 1))
							continue;	// ignore mario
						if (newSc[i+k][j+l] != oldSc[i][j]) {
							possBits[1-k][1-l] = false;
						}
					}
				}
			}
		return possBits;
	}


	@Override
	public void reset() {
		action = new boolean[Environment.numberOfButtons];
		action[Mario.KEY_RIGHT] = true;
		action[Mario.KEY_SPEED] = true;

		old_mario_x = -1;
		old_mario_y = -1;		
		f1 = null;
		prevWasOnGround = false;
		obsScenePrev = null;

		level.reset();
		a_star.reset();
	}
}
