package competition.icegic.peterlawford.simulator;

import java.util.LinkedList;
import java.util.ListIterator;

import competition.icegic.peterlawford.SlowAgent;
import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TheoreticEnemies {
	public LinkedList<TheoreticEnemy> enemies = new LinkedList<TheoreticEnemy>();
	TheoreticLevel level;

	final boolean fDebug;

	public TheoreticEnemies(TheoreticLevel level) {
		this.level = level;
		this.fDebug = SlowAgent.DEBUG;
	}
	public TheoreticEnemies(TheoreticEnemies in, boolean fDebug) {
		this.fDebug = fDebug;

		level = in.level;
		for (TheoreticEnemy e : in.enemies) {
			boolean fProcessed = false;
			if (e instanceof TheoreticFlowerEnemy) {
				enemies.add(new TheoreticFlowerEnemy(this,
						(TheoreticFlowerEnemy)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticBulletBill) {
				enemies.add(new TheoreticBulletBill(this,
						(TheoreticBulletBill)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticShell) {
				enemies.add(new TheoreticShell(this,
						(TheoreticShell)e));
				fProcessed = true;
			}
			if (e instanceof TheoreticFireball) {
				enemies.add(new TheoreticFireball(this,
						(TheoreticFireball)e));
				fProcessed = true;
			}
			if (!fProcessed) {
				enemies.add(new TheoreticEnemy(this,e));
			}
		}
	}

	@Override
	public boolean equals(Object in) {
		if (in == null) return false;
		TheoreticEnemies e = (TheoreticEnemies)in;
		if (e.enemies.size() != enemies.size()) {
			if (fDebug) 
				System.err.print("("+e.enemies.size()+"!="+enemies.size()+")");
			return false;
		}

		ListIterator<TheoreticEnemy> iterSelf = enemies.listIterator();
		ListIterator<TheoreticEnemy> iterOther = e.enemies.listIterator();
		while (iterSelf.hasNext()) {
			TheoreticEnemy self = iterSelf.next();
			TheoreticEnemy other = iterOther.next();
			if ((self.deadTime != 0) && (other.deadTime != 0) && 
					(self.nType == other.nType))
				continue;
			if ((self.x != other.x) ||
					(self.y != other.y) ||
					(self.xa != other.xa) ||
					(self.ya != other.ya))
				return false;
		}
		return true;
	}



	public void processEnemyInfo(Environment env, TheoreticMario mario) {
		float[] posEnemies = env.getEnemiesFloatPos();

		if (fDebug && SlowAgent.DEBUG) {
			if ((enemies.size() > 0) || (posEnemies.length > 0)) {
				System.out.print("guess:("+enemies.size()+")");
				for (TheoreticEnemy e: enemies)
					System.out.print("["+e.nType+","+e.x+","+e.y+"] ");
				System.out.print("\tgiven:");

				//		for (int i=posEnemies.length-3; i>=0; i-=3) {
				for (int i=0; i<posEnemies.length; i+=3) {
					System.out.print("["+(byte)posEnemies[i]+","+posEnemies[i+1]+","+
							posEnemies[i+2]+"] ");
				}
				System.out.println();
			}
		}

		LinkedList<TheoreticEnemy> temp = 
			new LinkedList<TheoreticEnemy>(enemies);
		//		LinkedList<TheoreticComparable> new_enemies =
		//			new LinkedList<TheoreticComparable>();
		LinkedList<TheoreticEnemy> new_enemies =
			new LinkedList<TheoreticEnemy>();
		//		for (int i=0; i<posEnemies.length; i+=3) {
		for (int i=posEnemies.length-3; i>=0; i-=3) {
			//			TheoreticComparable comp = new TheoreticComparable(
			//					(byte)posEnemies[i], posEnemies[i+1], posEnemies[i+2]);

			// Exact matches
			ListIterator<TheoreticEnemy> iterEnemies = temp.listIterator();
			boolean fFoundMatch = false;
			while (iterEnemies.hasNext()) {
				TheoreticEnemy e = iterEnemies.next();
				if (e instanceof TheoreticFireball) {
					iterEnemies.remove();
					continue;
				}
				if ((e.nType == (byte)posEnemies[i]) &&
						(e.x == posEnemies[i+1]) && (e.y == posEnemies[i+2])) {
					fFoundMatch = true;
					iterEnemies.remove();
					break;
				}
			}
			if (fFoundMatch) continue;

			// Non-exact but definite matches
			iterEnemies = temp.listIterator();
			while (iterEnemies.hasNext()) {
				TheoreticEnemy e = iterEnemies.next();
				/*				if (e.isThisDefinitelyMe((byte)posEnemies[i],
						posEnemies[i+1], posEnemies[i+2])) {
					e.fixupDefinite((byte)posEnemies[i], posEnemies[i+1], posEnemies[i+2]); */
				if (e.fixupDefinite((byte)posEnemies[i], posEnemies[i+1], posEnemies[i+2])) {
					if (fDebug)
						System.out.println(e.x+","+e.y+" == "+
								posEnemies[i+1]+","+posEnemies[i+2]);
					fFoundMatch = true;
					iterEnemies.remove();
					break;
				}
			}
			if (fFoundMatch) continue;

			// Resynchronize
			iterEnemies = temp.listIterator();
			while (iterEnemies.hasNext()) {
				TheoreticEnemy e = iterEnemies.next();
				if (e.fixupProbable((byte)posEnemies[i],
						posEnemies[i+1], posEnemies[i+2])) {
					if (fDebug)
						System.out.println(e.x+","+e.y+" == "+
								posEnemies[i+1]+","+posEnemies[i+2]);
					fFoundMatch = true;
					iterEnemies.remove();
					break;
				}
			}
			if (fFoundMatch) continue;


			// All new enemies should be added to the front of the list
			if (!fFoundMatch) {
				byte nType = (byte)posEnemies[i];
				float x = posEnemies[i+1];
				float y = posEnemies[i+2];
				//				new_enemies.add(new TheoreticComparable());
				switch(nType) {
				case Visualizer.ENEMY_GOOMBA:
				case Visualizer.ENEMY_RED_KOOPA:
				case Visualizer.ENEMY_GREEN_KOOPA:
				case Visualizer.ENEMY_SPINY:
					enemies.addFirst(new TheoreticEnemy(this, level, mario,
							nType, x, y, 0, 0, false));
					break;
				case Visualizer.ENEMY_FLYING_GOOMBA:
				case Visualizer.ENEMY_FLYING_RED_KOOPA:
				case Visualizer.ENEMY_FLYING_GREEN_KOOPA:
				case Visualizer.ENEMY_FLYING_SPINY:
					enemies.addFirst(new TheoreticEnemy(this, level, mario,
							nType, x, y, 0, 0, true));
					break;
				case Visualizer.ENEMY_PIRANHA_PLANT:
					enemies.addFirst(new TheoreticFlowerEnemy(this, level, mario, x, y));
					break;
				case Visualizer.ENEMY_BULLET:
					enemies.addFirst(new TheoreticBulletBill(this, x, y, (x>mario.x)?-1:1));
					break;
				case Visualizer.SHELL:
					enemies.addFirst(new TheoreticShell(this, level, x, y));
				case 14:
					if (fDebug) System.out.println("WHAT IS 14?");
					break;
				default:
					System.out.println("Unrecognized enemy type: "+nType+", "+x+","+y+
							" M:"+mario.x+","+mario.y);					
					throw new java.lang.NullPointerException();
				}
			}
		}

		// Clean up the dead
		ListIterator<TheoreticEnemy> iter = temp.listIterator();
		while (iter.hasNext()) {
			TheoreticEnemy e = iter.next();
			if (e.nType == 25) {
				iter.remove();
				continue;
			}
			if ((e.deadTime != 0) || (e.nType == 13) || (e.deadTime > 0) || 
					(mario.x - e.x > 230)) {	// 240?
				enemies.remove(e);
				iter.remove();
				if (fDebug)
					System.out.println("Removing dead or shell "+e.nType+", "+e.x+","+e.y);
			} else {
				if (fDebug)
					System.out.println("Unable to match "+
							e.nType+", "+e.x+","+e.y+","+e.deadTime);
				if ((mario.status != Mario.STATUS_DEAD) &&
						(mario.status != Mario.STATUS_WIN)) {
					//					throw new java.lang.NullPointerException();				
					enemies.remove(e);
					iter.remove();
				}
			}
		}

		//		if (!temp.isEmpty()) {
		//			throw new java.lang.NullPointerException();
		//		}
	}


	public boolean isOutOfBounds(Environment env, float x, float y) {
		if ((x==0) || (y==0)) return true;
		if (y > 255) return true;

		if (x - env.getMarioFloatPos()[0] > (15.5)*11) return true;
		if (env.getMarioFloatPos()[0] - x > 15*11) return true;
		if (y - env.getMarioFloatPos()[1] > (15.5)*11) return true;
		if (env.getMarioFloatPos()[1] - y > 15*11) return true;

		return false;
	}

	private byte getWiggleY(Environment env, int Dx, int My, float y, int nYold) {
		int EY = (int)(y/16);
		int nYW = EY - My +11;
		if ((nYW == nYold) || (nYW < 0) || (nYW >= 22))
			return -1;
		return env.getEnemiesObservation()[nYW][Dx];
	}
	private byte getWiggleX(Environment env, int Dy, int Mx, float x, int nXold) {
		int EX = (int)(x/16);
		int nXW = EX - Mx +11;
		if ((nXW == nXold) || (nXW < 0) || (nXW >= 22))
			return -1;
		return env.getEnemiesObservation()[Dy][nXW];
	}
	private byte getWiggleXY(Environment env,
			int Mx, int My, float x, float y, int nXold, int nYold) {
		int EX = (int)(x/16); int nXW = EX - Mx +11;
		int EY = (int)(y/16); int nYW = EY - My +11;
		if ((nXW == nXold) || (nXW < 0) || (nXW >= 22) ||
				(nYW == nYold) || (nYW < 0) || (nYW >= 22))
			return -1;
		return env.getEnemiesObservation()[nYW][nXW];
	}

	private byte getDiamondWiggle(Environment env, int nXCalcOld, int nYCalcOld, 
			int Mx, int My, float x, float y, float nOffset) {
		byte temp;
		temp = getWiggleX(env, nYCalcOld, Mx, x+nOffset, nXCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleX(env, nYCalcOld, Mx, x-nOffset, nXCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleY(env, nXCalcOld, My, y+nOffset, nYCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleY(env, nXCalcOld, My, y-nOffset, nYCalcOld);
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		return -1;
	}

	private byte getRectWiggle(Environment env, int nXCalcOld, int nYCalcOld,
			int Mx, int My, float x, float y, float xOff, float yOff ) {
		byte temp;
		temp = getWiggleXY(env, Mx, My, x+xOff,  y+yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleXY(env, Mx, My, x+xOff,  y-yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleXY(env, Mx, My, x-xOff,  y+yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		temp = getWiggleXY(env, Mx, My, x-xOff,  y-yOff, nXCalcOld, nYCalcOld);		
		if ((temp != -1) && (temp != Visualizer.MARIO)) return temp;
		return -1;
	}

	public byte getEnemyType(Environment env, float x, float y) {
		/*			int nX = (int)((x-env.getMarioFloatPos()[0])/16)+11;
			int nY = (int)((y-env.getMarioFloatPos()[1])/16)+11; */
		if ((x==0) || (y==0)) return -1;
		if (y > 255) return -1;

		//			x+=3;
		//			y += 15;
		float xMario = env.getMarioFloatPos()[0];
		float yMario = env.getMarioFloatPos()[1];

		//			xMario += 1;
		//			yMario += 16;
		//			yMario += 1;

		int MarioXInMap = (int)xMario/16;
		int MarioYInMap = (int)yMario/16;
		int EnemyXInMap = (int)(x/16);
		int EnemyYInMap = (int)(y/16);
		int nX = EnemyXInMap - MarioXInMap +11;
		int nY = EnemyYInMap - MarioYInMap +11;

		//			int EXp4 = (int)(x/16);
		//			int EYp4 = (int)(y/16);

		//			System.out.print("("+nX+","+nY+")");
		if ((nX >= 0) && (nX < 22) && (nY >= 0) && (nY < 22)) {
			System.out.print("[["+
					MarioXInMap+","+MarioYInMap+"|"+
					EnemyXInMap+","+EnemyYInMap+"|"+
					nY+","+nX+","+
					env.getEnemiesObservation()[nY][nX]+","+
					env.getLevelSceneObservation()[nY][nX]+"]]");
			byte result = env.getEnemiesObservation()[nY][nX];
			if (result == -1) {
				//					System.out.println("\t"+x+","+y+"\tMARIO:"+xMario+","+yMario);
				/*
					System.out.println("Enemy no change in x if -"+
							((x-EnemyXInMap*16)-1));
					System.out.println("Mario no change in x if -"+
							((xMario-MarioXInMap*16)-2));
					System.out.println("Mx:"+MarioXInMap*16+" Ex:"+EnemyXInMap*16);

					/*					System.out.println("Guess 1: add 6 to enemy.y");
					y += 6;
					nY = (int)(y/16) - MarioYInMap +11;
					if (nY >= 22) {
						System.out.println("OUT-OF-BOUNDS");
					} else */ 
				{
					byte temp = getDiamondWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 1);
					if (temp != -1) return temp;
				}

				{
					byte temp = getRectWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 1, 1 );					
					if (temp != -1) return temp;
				}

				{
					int bX = (int)((x-4)/16);
					int nBX = bX - MarioXInMap +11;
					if ((nBX >= 0) && (env.getEnemiesObservation()[nY][nBX] == 8))
						return 8;
					bX = (int)((x+4)/16);
					nBX = bX - MarioXInMap +11;
					if ((nBX < 22) && (env.getEnemiesObservation()[nY][nBX] == 8))
						return 8;
				}

				System.out.println(
						" v:"+((nY+1>21)?'-':env.getEnemiesObservation()[nY+1][nX])+
						" >:"+((nX+1>21)?'-':env.getEnemiesObservation()[nY][nX+1])+
				"]]");

				if (nX+1 < 22) { 
					result = env.getEnemiesObservation()[nY][nX+1];
					if ((result >= 2) && (result <= 10)) return result;
				}
				if (nY+1 < 22) {
					result = env.getEnemiesObservation()[nY+1][nX];
					if (result == 12) return result;
				}
				int EYm4 = (int)((y-4)/16);
				int nYm4 = EYm4 - MarioYInMap +11;
				byte ym4result = -1;
				if ((nYm4 != nY) && (nYm4 > 0)) {
					ym4result = env.getEnemiesObservation()[nYm4][nX];
					if ((env.getLevelSceneObservation()[nY][nX] == 0) &&
							(result == 2) || (result == 6) || (result == 9))
						return ym4result;
					if ((result == 3) || (result == 5) || 
							(result == 7) || (result == 10))
						return ym4result;
				}

				int EXm4 = (int)((x-4)/16);
				int nXm4 = EXm4 - MarioXInMap +11;
				if ((nXm4 != nX) && (nXm4 > 0)) {
					result = env.getEnemiesObservation()[nY][nXm4];							
					if ((result >= 2) && (result <= 10)) return result;
				}

				{
					byte temp = getDiamondWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 2);
					if (temp != -1) return temp;
				}

				// The enemy may be dead at this point
				if ((ym4result >= 2) && (ym4result <= 10)) return ym4result;
				/*						int EXp5 = (int)((x+5)/16);
						int nXp5 = EXp5 - MarioXInMap +11;
						if ((nXp5 != nX) && (nXp5 > 0)) {
							result = env.getEnemiesObservation()[nY][nXp5];							
							if ((result >= 2) && (result <= 10)) return result;
						}
				 */						
				{
					byte temp = getDiamondWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 3);
					if (temp != -1) return temp;
				}

				{
					byte temp = getRectWiggle(env, nX, nY,
							MarioXInMap, MarioYInMap, x, y, 2, 2 );					
					if (temp != -1) return temp;
				}

				System.out.println("Ex="+x+", Ey="+y);
				System.out.println("!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!");
				//					throw new java.lang.NullPointerException();
				return -1;
			}
			return result;
		}
		return -1;
	}	

	public void move(TheoreticMario mario, Frame frame) {
		//		System.out.println("MOVING "+enemies.size()+" ENEMIES");

		float xCam = mario.x - 160;
		float yCam = 0;
		if (xCam < 0) xCam = 0;
		//	        if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;

		ListIterator<TheoreticEnemy> iter = enemies.listIterator();
		//		for (TheoreticEnemy enemy : enemies.enemies) {
		while (iter.hasNext()) {
			TheoreticEnemy enemy = iter.next();

			float xd = enemy.x - xCam;
			float yd = enemy.y - yCam;
			if ((xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64) &&
					(enemy.nType != 13))
			{
				//				System.out.println("Removing enemy at "+enemy.x+","+enemy.y);
				iter.remove();
				continue;
			}

			//			if ( (enemy.x < mario.x - (12*16)) ||
			//					(enemy.y > 22*16) ) {
			//				iter.remove();
			//			} else {
			if (enemy.move(mario, frame)) {
				iter.remove();
			} else {
				//			enemy.path.push(new float[]{enemy.x, enemy.y});
			}
			//			}
		}
	}
}
