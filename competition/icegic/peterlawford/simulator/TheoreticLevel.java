package competition.icegic.peterlawford.simulator;

import ch.idsia.mario.engine.level.Level;
import ch.idsia.mario.environments.Environment;

public class TheoreticLevel {
	private static final int MEMORY_SIZE = 16;
	
	private static byte[][][] scnX = new byte[MEMORY_SIZE][][];
	static byte[][][]  bgX = new byte[MEMORY_SIZE][][];

	{
		for (int i=0; i<MEMORY_SIZE; i++) {
			scnX[i] = null;
			bgX[i] = null;
		}
	}
	static int[] offX = new int[MEMORY_SIZE];
	static int[] offY = new int[MEMORY_SIZE];
	
	static int ptr = 0;
	
	static boolean pauseWorld = false;
	
	static Environment env;

	public static int fireballsOnScreen;
	
	public void reset() {
		fireballsOnScreen = 0;
		pauseWorld = false;
		ptr = 0;
		env = null;
		
		for (int i=0; i<MEMORY_SIZE; i++) {
			offX[i] = 0; offY[i] = 0;
			scnX[i] = null; bgX[i] = null;
		}
	}
	
	public static float getHeight(float x, float y) {
		// return value -1 implies infinite height
		// 0 implies we are inside the ground
		
		int xd = (int)(x/16);
		int yd = (int)(y/16);
		int j = (ptr+1) % MEMORY_SIZE;
		int offset_x = xd-offX[j]+11;
		if (offset_x < 0) offset_x = 0;
		if (offset_x > 21) offset_x = 21;
		
		boolean fFirst = true;
		for (int offset_y = yd-offY[j]+11; offset_y < 22; offset_y++) {
			byte scn_val = bgX[j][offset_y][offset_x];
			if ((scn_val != 0) && (scn_val != 1)) {
				if (!fFirst || (scn_val == -10)) {
					return (offset_y+offY[j]-11)*16;
				}
			}
					
					 fFirst = false;
		}
		return -1;
	}
	
	public boolean isBlocking(int x, int y, float xa, float ya)
	{
		byte block = getBlock(x, y);

		boolean blocking = ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_ALL) > 0;
		blocking |= (ya > 0) && ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_UPPER) > 0;
		blocking |= (ya < 0) && ((Level.TILE_BEHAVIORS[block & 0xff]) & Level.BIT_BLOCK_LOWER) > 0;

		return blocking;
	}

	static byte getBlock_i(int x,int y,
			byte[][] complete, byte[][] enemies, byte[][] scn,
			int nOffsetX, int nOffsetY
			) {
		if (y == 15) y = 14;	
		// For some reason we can't seem to see the last row of data
		
		int offY = y-nOffsetY+11;
		int offX = x-nOffsetX+11;
//		System.out.print("{"+offX+","+offY+"/"+x+","+y+"}");
		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21)) {
//			System.out.println("!!!"+offX+","+offY+"!!!");
			return -1;
		}
		
		byte result = complete[offY][offX];
		byte scn_result = scn[offY][offX];
//		byte enemy_result = enemies[offY][offX];
		
		
		// ================================================================
		// New-style processing based on Aug. 13,2009 upstream modifications
		// ================================================================

//		if ((x == 192) && (y == 15)) {
//			System.out.println("192-15:"+scn_result+","+result+"::"+offX+","+offY);
//		}

//		if ((scn_result == -128) && (result == -128))
//		System.out.print(scn_result+"/"+result+" x"+x+" y"+y);
		
		if (scn_result == -10)	// ground
//			return -127;
			return -127;
		if (scn_result == -11)	// platform
			return -123;
		if (scn_result == -12)	// rock
			return 9;
		if (scn_result == 0)
			return 0;
		
		// TODO: what about scn_result == 19?
		if ((scn_result >= 16) && (scn_result <= 22)) return scn_result;

		if (scn_result == Visualizer.MARIO) {
//			System.err.println("MARIO:"+result+","+x+","+y);
//			if (result == 0) return 0;
			throw new java.lang.NullPointerException();
		}
		// This is because Mario is considered part of the scenery,
		// and can cover up scenery.
		if (result == -11)
			return -123;
/*		
		if (result == -11) {
			System.out.println("SCN="+scn_result+":"+offX+","+offY);
			throw new java.lang.NullPointerException();
		} */
		
//		if (true) return 0;
		System.err.println(scn_result+","+result);
		if (true)
		throw new java.lang.NullPointerException();
		
		// ================= End new-style processing ===================
		
		
		if (result == Visualizer.FIREBALL) return -1;	// Fireballs cover scenery
		if (result == -1) return -1;	// sparkles cover scenery

		// Mario is invisible
		if (result == 1) return -1;	// keep searching
		
		// Pipes should be handled immediately
		// 10 is either a pipe or a flying spiny
		if (result == 10) {	// left hand side of pipe
//			System.out.println("P?s:"+
//					env.getLevelSceneObservation()[y-nOffsetY+11][x-nOffsetX+11]+","+
//					env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11]);
			if (env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11] == 10)
				return 0;
			return result;
		}

		// Pipes should be handled immediately
		if (result == 11) return result;	// right hand side of pipe
		
	/*	
		if ((result == 9) && 
				(enemies[offY][offX] == -1)) {
			System.out.println("R?S?T:"+
					enemies[y-nOffsetY+11][x-nOffsetX+11]);
			return result;	// stepping stones should be handled immediately
		}
*/
		
		// bullet shooters are handled later and they can 'cover up' scenery, so 
		// mark as unknown (TODO: verify this)
		if (result == 14) {
//			System.err.println("*14*");
			return 14;
		}
		
		// bullets cause problems because they are not collidable but we can't see under them
		if (result == 8) {
//			System.out.println("<"+result+","+
//					env.getLevelSceneObservation()[y-nOffsetY+11][x-nOffsetX+11]+","+
//					env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11]+">");	
			return -1;
		}
		
		// If there is an enemy underneath us then we will handle the sprite collision later
		if ((result >= 2) && (result <= 13)) {
//			System.out.println("{"+result+","+
//					env.getLevelSceneObservation()[y-nOffsetY+11][x-nOffsetX+11]+","+
//					env.getEnemiesObservation()[y-nOffsetY+11][x-nOffsetX+11]+"}");
			return 0;
		}
		
		return result;
	}

	
	static byte getBlock2(int x, int y) {
		int j = (ptr+1) % MEMORY_SIZE;
int nOffsetX = offX[j]; int nOffsetY = offY[j];
byte[][] scn = scnX[j];
		
		if (y == 15) y = 14;	
		// For some reason we can't seem to see the last row of data
		
		int offY = y-nOffsetY+11;
		int offX = x-nOffsetX+11;
//		System.out.print("{"+offX+","+offY+"/"+x+","+y+"}");
		if ((offX < 0) || (offX > 21) || (offY < 0) || (offY > 21)) {
			return 0;
		}
		
//		byte result = complete[offY][offX];
		byte scn_result = scn[offY][offX];

		if (scn_result == -10)	// ground
//			return -127;
			return -127;
		if (scn_result == -11)	// platform
			return -123;
		if (scn_result == -12)	// rock
			return 9;
		if (scn_result == 0)
			return 0;
		
		// TODO: what about scn_result == 19?
		if ((scn_result >= 16) && (scn_result <= 22)) return scn_result;	
		
		return 0;
	}
	
	static byte getBlock(int x,int y) {
		
		for (int i=0; i<MEMORY_SIZE; i++) {
//			System.out.print("-"+i);
			
			int j = (i+ptr+1) % MEMORY_SIZE;
			if (scnX[j] == null) break;
			byte result;
				result = getBlock_i(x,y,
						scnX[j], null, bgX[j],
						offX[j], offY[j]);
			
			if (result != -1) return result;				
		}
		
//		System.out.println("*-1*");
		return 0;	
	}

	public static void updateImage(Environment env) {
		
		float nMarioX = env.getMarioFloatPos()[0];
		                                      float nMarioY = env.getMarioFloatPos()[1];
		
		          TheoreticLevel.env = env;                            
		                                      
		scnX[ptr] = env.getCompleteObservation();
		bgX[ptr] = env.getLevelSceneObservation();
		offX[ptr] = (int)nMarioX/16;
		offY[ptr] = (int)nMarioY/16;
		ptr--;
		if (ptr < 0) ptr = MEMORY_SIZE-1;
	
//		SlowAgent.DisplayPathInfo path = new SlowAgent.DisplayPathInfo(Color.GREEN);
//		float y_bottom = nMarioY;
//		path.push(new float[]{nMarioX, y_bottom});
//		path.push(new float[]{nMarioX, getHeight(nMarioX, y_bottom)});
//		SlowAgent.lines.add(path);
		//	        System.out.println("MARIO:"+nOffsetX+", "+nOffsetY);
	}
}
