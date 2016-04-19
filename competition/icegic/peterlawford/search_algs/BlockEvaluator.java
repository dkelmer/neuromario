package competition.icegic.peterlawford.search_algs;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import competition.icegic.peterlawford.SlowAgent.DisplayPathInfo;
import competition.icegic.peterlawford.simulator.TheoreticMario;
import competition.icegic.peterlawford.simulator.Visualizer;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

interface SurfaceEvaluator {

	boolean isSurfaceAt(float x, int yDiscrete);

}

class MemoizedJump {
	private static int MAX_POINTS = 30;
	private static int HALF_DISCRETIZED_PTS = 8;

	float[][] best_path;	// [0..MAX_POINTS-1, {x,y,xa}

	// There may be two points touch points in a path
	int[][] discretized_path_y = new int[HALF_DISCRETIZED_PTS*2+1][2];
	//	float[][] discretized_path_x = new float[HALF_DISCRETIZED_PTS*2+1][2];

	static float max_speed = -1;

	public MemoizedJump(int nPercentOfMaxSpeed) {
		if (max_speed == -1)
			max_speed = calcMaxSpeed();

		best_path = calcFurthestHighestJump(max_speed * nPercentOfMaxSpeed / 100);
		calcDiscretizedPaths();
	}


	boolean[] keys_sjr = new boolean[16];
	{
		keys_sjr[Mario.KEY_SPEED] = true;
		keys_sjr[Mario.KEY_JUMP] = true;
		keys_sjr[Mario.KEY_RIGHT] = true;		
	}

	boolean[] keys_jr = new boolean[16];
	{
		keys_jr[Mario.KEY_JUMP] = true;
		keys_jr[Mario.KEY_RIGHT] = true;		
	}

	boolean[] keys_sr = new boolean[16];
	{
		keys_sr[Mario.KEY_SPEED] = true;
		keys_sr[Mario.KEY_RIGHT] = true;		
	}

	private float calcMaxSpeed() {		
		TheoreticMario m = TheoreticMario.getInitialMarioOnGround(0);
		m.keys = keys_sr;

		float result = 0;
		for (int i=0; i<100; i++) {
			//			System.err.println(i+";"+m.x+";"+m.y+";"+m.xa);
			m.onGround = true;
			m.wasOnGround = true;
			m.move(null, null);
			if (m.xa-result<0.001) break;
			result = m.xa;
		}

		//		throw new java.lang.NullPointerException();
		return result;
	}

	private float[][] calcFurthestHighestJump(float X_A) {
		//		final float X_A = 0;
		float[][] result = new float[MAX_POINTS][3];

		TheoreticMario m = TheoreticMario.getInitialMarioOnGround(X_A);

		m.keys = keys_sjr;
		for (int i=0; i<result.length; i++) {
			//			System.err.println(i+";"+m.x+";"+m.y+";"+m.xa);
			result[i][0] = m.getX();
			result[i][1] = m.getY();
			result[i][2] = m.xa;
			m.move(null, null);
		}

		return result;
	}

	private void calcDiscretizedPaths() {
		for (int path_id = -8; path_id <= 8; path_id++) {
			discretized_path_y[path_id+8][0] = -1;
			discretized_path_y[path_id+8][1] = -1;

			float delta_y = path_id*16;

			for (int i=0; i<(best_path.length-2); i++) {
				if ((best_path[i][1] > delta_y) && 
						(best_path[i+1][1] <= delta_y)) {
					if (discretized_path_y[path_id+8][0] != -1) 
						throw new java.lang.NullPointerException();
					discretized_path_y[path_id+8][0] = i+1;
				}
				if ((best_path[i][1] <= delta_y) && 
						(best_path[i+1][1] > delta_y)) {
					if (discretized_path_y[path_id+8][1] != -1) 
						throw new java.lang.NullPointerException();
					discretized_path_y[path_id+8][1] = i;
				}			
			}	
		}
	}

	private DisplayPathInfo genPath_i(Color color,
			int start, float x, float y) {
		DisplayPathInfo result = new DisplayPathInfo(color);

		float start_x = best_path[start][0];
		float start_y = 16*(int)Math.ceil(best_path[start][1]/16);

		for (int i = start; i>=0; i--) {
			result.push(new float[]{
					best_path[i][0]-start_x+x,
					best_path[i][1]-start_y+y
			});
		}

		return result;
	}

	public LinkedList<DisplayPathInfo> generateDisplayPaths(
			Color color,
			float Mx, float My, float Critx, float Crity,
			SurfaceEvaluator eval) {
		LinkedList<DisplayPathInfo> result = 
			new LinkedList<DisplayPathInfo>();

		for (int path_id = -8; path_id <= 8; path_id++)
			for (int j=0; j<2; j++) {
				int nPathEndpt = discretized_path_y[path_id+8][j];
				if (nPathEndpt >= 0) {
					Color t_color = (eval.isSurfaceAt(
							Critx-best_path[nPathEndpt][0],
							//								path_id-(int)Math.ceil(best_path[nPathEndpt][1]/16))) )
							(int)(Crity/16)-path_id ) ) ? color : Color.GRAY;
					result.add(
							genPath_i(t_color,
									discretized_path_y[path_id+8][j], Critx, Crity) );
				}
			}

		return result;
	}

}

public class BlockEvaluator implements SurfaceEvaluator {
	private int[][] grid = new int[22][22];	// this is in x,y format.  not row,col.

	// grid values are as follows:
	// -1 represents impenetrable ground (or a platform)
	// 1 represents a pit
	// 2 represents a single space between two towers (a hole)
	// 3 represents shadows in front of towers

	int grid_loc_x;
	int grid_loc_y;

	MemoizedJump memoized_jump_min_speed = new MemoizedJump(0);
	MemoizedJump memoized_jump_max_speed = new MemoizedJump(100);

	public boolean fIsEndpoint;
	boolean fIsStartpoint;

	public void evaluate(Environment env) {
		grid_loc_x = (int)(env.getMarioFloatPos()[0]/16);
		grid_loc_y = (int)(env.getMarioFloatPos()[1]/16);

		fIsEndpoint = true;
		fIsStartpoint = true;

		for (int x=0; x<22; x++) {
			boolean fIsPit = true;
			for (int y=0; y<22; y++) {
				grid[x][y] = Integer.MAX_VALUE;

				byte scn = env.getLevelSceneObservation()[y][x];

				if ((x > 11) && (scn != 0)) fIsEndpoint = false;
				if ((x<=11) && (scn != 0) && (scn != Visualizer.MARIO))
					fIsStartpoint = false;

				if (((scn != 0) && (scn != Visualizer.MARIO) && (scn != Visualizer.COIN)) ||
						(env.getCompleteObservation()[y][x] == 14)) {
					grid[x][y] = -1;
					fIsPit = false;
				}
			}

			if (fIsPit) {
				for (int y=21; y>=0; y--) {
					if ((x == 0) || (grid[x-1][y]<=1))
						grid[x][y] = 1;
				}
			}
		}

		if (fIsStartpoint) fIsEndpoint = false;

		for (int y=20; y>=0; y--) {
			for (int x=20; x>0; x--) {
				if (grid[x][y] <= 2) continue;

				if ((grid[x-1][y] <= 2) && (grid[x+1][y] <= 2) && 
						(grid[x][y+1] <= 2) && (grid[x][y] > 2))
					grid[x][y] = 2;

				if ( (y!=0) && (grid[x][y] > 3) && (grid[x+1][y] <= 3) && 
						(grid[x+1][y-1] <= 3) && (grid[x][y+1] <= 3) ) {
					grid[x][y] = 3;
					for (int y_t=y-1; y_t>0; y_t--)
						if ( (y_t!=0) && (grid[x+1][y_t] <= 3) && (grid[x+1][y_t-1] <= 3) && 
								(grid[x][y_t+1] <= 3) )
							grid[x][y_t] = 3;

					y = 21;
					break;
				}
			}
		}
	}

	public int getTruePt(float x, float y) {
		int nTrueX = (int)(x/16); int nTrueY = (int)(y/16);
		int nCalcX = nTrueX-grid_loc_x+11;
		int nCalcY = nTrueY-grid_loc_y+11;
		if ((nCalcX < 0) || (nCalcY < 0) || (nCalcX > 21) || (nCalcY > 21))
			return 0;
		return grid[nCalcX][nCalcY];
	}

	public int getPt(int row, int col) {
		return grid[col][row];
	}

	LinkedList<DisplayPathInfo> path_infos = null;

	public LinkedList<CriticalPoint> findCriticalPoints(
			float xOff, float yOff) {

		LinkedList<CriticalPoint> result = new LinkedList<CriticalPoint>();
		for (int x=1; x<22; x++)
			for (int y=1; y<22; y++) {
				if ( (grid[x][y]<0) && (grid[x-1][y]>0) && 
						(grid[x][y-1]>0) && (grid[x-1][y-1]>0) ) {

					float nX = (x-11+ (int)(xOff/16))*16;
					float nY = (y-11+ (int)(yOff/16))*16;

					if (path_infos != null) {
						LinkedList<DisplayPathInfo> paths = 
							memoized_jump_min_speed.generateDisplayPaths(
									Color.GREEN,
									xOff, yOff, nX, nY, this);
						path_infos.addAll(paths);

						LinkedList<DisplayPathInfo> paths2 = 
							memoized_jump_max_speed.generateDisplayPaths(
									Color.RED,
									xOff, yOff, nX, nY, this);
						path_infos.addAll(paths2);
					}
				}
			}

		return result;
	}

	public class CriticalPoint {
		final Point2D.Float pt;
		public CriticalPoint(float x, float y) {
			pt = new Point2D.Float(x, y);
		}
	}

	// TODO: 1.6
	//	@Override
	public boolean isSurfaceAt(float x, int yDiscrete) {

		int nX = (int)(x/16) - grid_loc_x + 11;
		int nY = yDiscrete - grid_loc_y + 11;

		boolean result;

		if ((nX > 21) || (nX < 0) || (nY > 21) || (nY < 0)) {
			result = false;	
		} else {
			if ((grid[nX][nY] < 0) && 
					((nY == 0) || (grid[nX][nY-1] > 0))) {
				result = true;
			} else {
				result = false;
			}
		}

		return result;
	}
}
