package competition.icegic.peterlawford.search_algs;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import competition.icegic.peterlawford.SlowAgent;
import competition.icegic.peterlawford.SlowAgent.DisplayPathInfo;
import competition.icegic.peterlawford.simulator.Frame;
import competition.icegic.peterlawford.simulator.TheoreticMario;
import ch.idsia.mario.engine.sprites.Mario;

class Option implements Comparable<Option> {
	final Frame f;

	Option parent;
	Option[] children = new Option[10];
	final double raw_gScore;
	double calc_dist;
	final double hScore;
	final int coded_action;
	int nCycleCnt;
	int frameCycleCnt;
	
	float height;
	
	int maximize = -1;	// 1 for minimize

	private double distance_to(Frame f_old) {
		int nScnVal = AStarThreaded.block_eval.getTruePt(f.mario.getX(), f.mario.getY());
		//		if (nScnVal == 1)
		//			return maximize * Float.MAX_VALUE;
		if ((nScnVal == 1) || (nScnVal == 2)) {
			//			System.err.println("$"+nScnVal+"$");
			return maximize * Float.MAX_VALUE;
		}
		if (nScnVal == 3) return
		(f.mario.getX() - f_old.mario.getX())/10;

		if (f.mario.fIsHurt) return maximize * Float.MAX_VALUE;
		if (f.mario.getY() > 256) return maximize * Float.MAX_VALUE;

		// Option 1
//		return (f.mario.getX() - f_old.mario.getX());
// Option 2 & 3
//		float height = TheoreticLevel.getHeight(f.mario.x, f.mario.y);
//Option 2
		//		return (f.mario.getX() - f_old.mario.getX()) *
//			((320-height)/320);//+(f.mario.xa - f.mario.xa);		
// Option 3
//		return (f.mario.getX() - f_old.mario.getX()) -
//		(height/100);//+(f.mario.xa - f.mario.xa);
	// Option 4
		return (f.mario.getX() - f_old.mario.getX()) -
					Math.abs(f.mario.getY() - f_old.mario.getY())/10;
	}

	public boolean isFailure() {
		if (nCycleCnt < 16) return false;
		if (AStarThreaded.block_eval.fIsStartpoint) return false;
		
		if (parent != null) {
		int nScnVal = AStarThreaded.block_eval.getTruePt(f.mario.getX(), f.mario.getY());
		int nScnParentVal = AStarThreaded.block_eval.getTruePt(
				parent.f.mario.getX(), parent.f.mario.getY());
		if ( ((nScnVal == 1) || (nScnVal == 2)) && 
				(nScnParentVal != 1) && (nScnParentVal != 2) )
			return true;
		}
		if (f.mario.fIsHurt) return true;	

		return false;
	}

// This is the version for the standard a-star
	public Option(Option old, Frame f, int coded_action,
			SlowAgent.DisplayPathInfo line) {
		this.f = f;
		this.parent = old;
		old.children[coded_action] = this;
		this.line = line;
		this.nCycleCnt = old.nCycleCnt+1;
		
		
		//		if (Math.abs(old.f.mario.y+f.mario.ya-f.mario.y) > 5) {
		//			System.err.println(old.f.mario.x+","+old.f.mario.y+","+
		//					old.f.mario.xa+","+old.f.mario.ya+"\t"+
		//					f.mario.x+","+f.mario.y+","+f.mario.xa+","+f.mario.ya);
		//			throw new java.lang.NullPointerException();
		//		}
		this.coded_action = coded_action;

		int nOldCnt = old.nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;

		if (nNewCnt > 110) throw new java.lang.NullPointerException();
		
		// First method
//		raw_gScore =  (old.raw_gScore*(nOldCnt) + distance_to(old.f)) / (nNewCnt);
		calc_dist = distance_to(old.f);
		// Second method
//		raw_gScore =  old.raw_gScore + calc_dist;
		// Third method
		double new_dist = 0;
		Option opt_t = this;
		for (int i=0; i<32; i++) {
			if (opt_t == null) break;
			new_dist += this.calc_dist;
			opt_t = opt_t.parent;
		}
		raw_gScore = new_dist;
		
		int nBackCnt = 0;
		/*		Option opt_t = this;
		int nCodedAction = opt_t.coded_action;
		if (nCodedAction != 0)
			while ((opt_t.parent != null) && (opt_t.parent.coded_action == nCodedAction)) {
				opt_t = opt_t.parent;
				nBackCnt++;
			} */
		hScore = - ((f.mario.xa+(120-nOldCnt)*16) / (120-nNewCnt)) -nBackCnt;

		//		raw_gScore = old.raw_gScore+distance_to(old.f);
		//		hScore = AStar2.endpt_x - f.mario.x;
	}

	// This is the version for the inverted a-star
	public Option(Option old, Frame f, int coded_action) {
		this.f = f;
		this.parent = old;
		old.children[coded_action] = this;
		this.coded_action = coded_action;
		this.nCycleCnt = old.nCycleCnt+1;
		line = null;

		boolean[] action = AStarThreaded.coded_keys[coded_action];
		calc_dist = estimate_distance_travelled(f.mario, 
				action[Mario.KEY_LEFT], action[Mario.KEY_RIGHT],
				action[Mario.KEY_JUMP], action[Mario.KEY_DOWN],
				action[Mario.KEY_SPEED]);
		
		int nCycles = 0;
		double new_dist = 0;
/*		Option opt_t = this;
		while ((opt_t.parent != null) && (nCycles < 32)) {
			new_dist += opt_t.calc_dist; nCycles++;
			opt_t = opt_t.parent;
		}
*/		// alternate processing
//		int nCycles = 0;
		{
		Option opt_t = this.parent;
		while ((opt_t.parent != null) && (nCycles < 31)) {
			nCycles++;
			opt_t = opt_t.parent;
		}
		new_dist = calc_dist+(this.parent.f.mario.x - opt_t.f.mario.x);
		}
/*		if (new_dist != new_dist2) {
			System.err.println(new_dist+" VS "+new_dist2);
			nCycles = 0;
			while ((opt_t.parent != null) && (nCycles < 32)) {
				new_dist += opt_t.calc_dist; nCycles++;
				opt_t = opt_t.parent;
			}
			throw new java.lang.NullPointerException();
		}
	*/	
		raw_gScore = new_dist / nCycles;
//		if (f.mario.y == parent.f.mario.y) {
//		hScore = f.mario.ya/10;	
//		} else {
			int nOldCnt = old.nCycleCnt - AStarThreaded.initial_cycle_cnt;
			int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
//		hScore = - ((calc_dist+(120-nOldCnt)*16) / (120-nNewCnt));
			hScore = -calc_dist;
//		}
	}

	public Option(Frame f, int nCycleCnt) {
		this.f = f;
		this.nCycleCnt = nCycleCnt;

		int nNewCnt = nCycleCnt - AStarThreaded.initial_cycle_cnt;
		int nOldCnt = nNewCnt-1;

		parent = null;
		coded_action = -1;
		line = null;

		raw_gScore = 0;
		calc_dist = 0;
//		hScore = -(16 - (f.mario.xa+(120+1)*16) / (120+1+1)));
		hScore = -((f.mario.xa+(120-nOldCnt*16) / (120-nNewCnt)));
		//		hScore = AStar2.endpt_x;
	}

	final SlowAgent.DisplayPathInfo line;

	static long max_true_sim_time = 0;
	
	public Option clone_and_update(int i, List<SlowAgent.DisplayPathInfo> lines) {
		// TODO Auto-generated method stub
		long true_sim_time = System.currentTimeMillis();
		Frame f_new = new Frame(f, false);
		f_new.move(AStarThreaded.coded_keys[i]);
		true_sim_time = System.currentTimeMillis() - true_sim_time;
		if (true_sim_time > max_true_sim_time) max_true_sim_time = true_sim_time;

		if (lines != null) {
		SlowAgent.DisplayPathInfo line = new SlowAgent.DisplayPathInfo(Color.RED);
		line.push(new float[]{f.mario.getX(), f.mario.getY()});
		line.push(new float[]{f_new.mario.getX(), f_new.mario.getY()});
		//		SlowAgent.lines.add(line);
		lines.add(line);
		}

		return new Option(this, f_new, i, line);		
	}

	public double getGScore() {
		// First method
//		return maximize * raw_gScore;
		// Second method
//		return maximize * (raw_gScore/nCycleCnt);
		// Third method
		return (maximize * raw_gScore);
	}

	public double getHScore() {
		//		return (AStar2.endpt_x - f.mario.x) + (f.nCycleId*16);
		//		return (AStar2.endpt_x - f.mario.x) / 
//		return 16;
		return hScore;
	}

	public double getFScore() {
		return getGScore() + getHScore();
	}

	public int reconstruct_path() {		
		if (parent == null) {
			return -1;
		} 
		if (line != null) {
			//			System.err.println("CHG COLOR");
			line.color = Color.WHITE;
			float[] pt1 = line.path.getFirst();
			float[] pt2 = line.path.get(1);
			//			System.err.println(line.path.size()+":"+
			//					pt1[0]+","+pt1[1]+"\t"+
			//					pt2[0]+","+pt2[1]
			///			);
			//		AStar2.lines.add(line);
		}
		int result = parent.reconstruct_path();
		if (result == -1) result = coded_action;
		//		result.addFirst(new Integer(coded_action));
		return result;
	}

	// TODO: 1.6
//	@Override
	public int compareTo(Option o) {
		double my_f_score = getFScore();
		double other_f_score = o.getFScore();
		// TODO Auto-generated method stub
		if (my_f_score < other_f_score) return -1;
		if (my_f_score > other_f_score) return 1;
		return 0;
	}

	@Override
	public boolean equals(Object o) {		
		Option opt = (Option)o;
		if ((opt.nCycleCnt == nCycleCnt) &&
				(opt.f.mario.getX() == f.mario.getX()) && 
				(opt.f.mario.getY() == f.mario.getY()) &&
				(opt.f.mario.xa == f.mario.xa) &&
				(opt.f.mario.ya == f.mario.ya))
			return true;
		return false;
	}

	public int hashCode() {
		return (int)(1000*(nCycleCnt + f.mario.getX() + f.mario.getY() + f.mario.xa + f.mario.ya));
	}

	public void dump() {
		if (parent != null) parent.dump();
		System.err.print(coded_action);
	}

	public Option clone_and_guess(int i, LinkedList<DisplayPathInfo> lines) {
		Frame f_new = new Frame(f, false);
		return new Option(this, f_new, i);		
	}

	public void update(List<SlowAgent.DisplayPathInfo> lines) {
		float old_x = f.mario.getX(); float old_y = f.mario.getY();
		
		long true_sim_time = System.currentTimeMillis();
//		Frame f_new = new Frame(f, false);
		f.move(AStarThreaded.coded_keys[coded_action]);
		true_sim_time = System.currentTimeMillis() - true_sim_time;
		if (true_sim_time > max_true_sim_time) max_true_sim_time = true_sim_time;

		calc_dist = verify_distance_travelled(f.mario, parent.f.mario);
		
		if (lines != null) {
			SlowAgent.DisplayPathInfo line = new SlowAgent.DisplayPathInfo(Color.RED);
			line.push(new float[]{old_x, old_y});
			line.push(new float[]{f.mario.getX(), f.mario.getY()});
			//		SlowAgent.lines.add(line);
			lines.add(line);
		}
	}

	private double estimate_distance_travelled(TheoreticMario mario, boolean l, boolean r,
			boolean j, boolean d, boolean s) {
		float xa = mario.xa;
		xa *= 0.89;
		float sideWaysSpeed = (s) ? 1.2f : 0.6f;
		if (l) xa -= sideWaysSpeed;
		if (r) xa += sideWaysSpeed;
		if ((mario.onGround) && j) xa -= 0.01;
		return xa;
	}

	private double verify_distance_travelled(TheoreticMario mario_new,
			TheoreticMario mario_old) {
		return mario_new.x - mario_old.x;
	}
	
}
