package competition.icegic.peterlawford.search_algs;

import competition.icegic.peterlawford.simulator.Frame;
import ch.idsia.mario.engine.sprites.Mario;

public abstract class AbstractSearch {
	public abstract int a_star(float mario_x, float mario_y,
			Frame f0, GoalEvaluator goal_eval_in, BlockEvaluator block_eval);

	private static final int ENCODED_CHOICEVAL_CNT = 10;
	// 0: --- 1: --L 2: --R 3: -JL 4: -JR 5: S-- 6: S-L 7: S-R 8: SJL 9: SJR
	private int[] choice_succ = new int[10];

	public static boolean[][] coded_keys = new boolean[ENCODED_CHOICEVAL_CNT][16];
	static {
		for (int i=0; i<ENCODED_CHOICEVAL_CNT; i++) {
			int i_t = i%5;
			if (i>=5) coded_keys[i][Mario.KEY_SPEED] = true;
			if (i_t>=3) coded_keys[i][Mario.KEY_JUMP] = true;
			if ((i_t == 1) || (i_t == 3))
				coded_keys[i][Mario.KEY_LEFT] = true;
			if ((i_t == 2) || (i_t == 4))
				coded_keys[i][Mario.KEY_RIGHT] = true;
		}
	}

	public abstract void reset();
}
