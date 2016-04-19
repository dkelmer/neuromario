package competition.icegic.peterlawford.simulator;


public class TheoreticEnemy extends TheoreticSprite {
	boolean fDebug;
	
	public boolean winged;
	public int facing = -1;

	boolean onGround = false;
	private boolean mayJump = false;

	public final boolean avoidCliffs;

	int width = 4;
	final int height;

	int deadTime = 0;

	final TheoreticLevel level;
	final TheoreticEnemies enemies;

	boolean fDirKnown = false;
	boolean flyDeath = false;
	boolean noFireballDeath = false;

	boolean fDirIdentified = false;
	boolean fInitializingMove = false;
	boolean fBeyondHorizonOnGround = true;


	// nType == -1 means unknown type
	public TheoreticEnemy(
			TheoreticEnemies enemies, TheoreticLevel level, TheoreticMario mario,
			byte nType, float x, float y, float xa, float ya, boolean winged
	) {

		super(nType, x, y, xa, ya);

		this.enemies = enemies;
		fDebug = (enemies == null) ? false : enemies.fDebug;

		this.level = level;

		this.winged = winged;
		avoidCliffs = isCliffAvoiding(nType);
		height = getHeight(nType);
		noFireballDeath = getFlameproof(nType);
		if (//(!winged) && 
				(nType >= 2) && (nType <= 10) && (nType != 8)) {
			if (!winged) onGround = ( 16*(int)(ya / 16) == ya );
			this.x += 1.75;
			fInitializingMove = true;
			move(mario, null);
			fInitializingMove = false;
		}
	}
	private static boolean isCliffAvoiding(byte nType) {
		if (nType == Visualizer.ENEMY_GOOMBA) return false;
		if (nType == Visualizer.ENEMY_FLYING_GOOMBA) return false;
		if (nType == Visualizer.ENEMY_GREEN_KOOPA) return false;
		if (nType == Visualizer.ENEMY_FLYING_GREEN_KOOPA) return false;
		if (nType == Visualizer.ENEMY_SPINY) return false;
		if (nType == Visualizer.ENEMY_FLYING_SPINY) return false;
		return true;
	}
	private static int getHeight(byte nType) {
		if (nType >= 4 && nType < 8) return 24;
		return 12;
	}
	private static boolean getFlameproof(byte nType) {
		if (nType == Visualizer.ENEMY_BULLET) return true;
		if (nType == Visualizer.ENEMY_FLYING_SPINY) return true;
		if (nType == Visualizer.ENEMY_SPINY) return true;
		return false;
	}

	public TheoreticEnemy(TheoreticEnemies enemies, TheoreticEnemy in) {

		super(in.nType, in.x, in.y, in.xa, in.ya);

		this.enemies = enemies;
		fDebug = (enemies == null) ? true : enemies.fDebug;

		level = in.level;
		facing = in.facing;

		mayJump = in.mayJump;
		winged = in.winged;
		avoidCliffs = in.avoidCliffs;
		fDirKnown = in.fDirKnown;
		onGround = in.onGround;

		deadTime = in.deadTime;
//		fIsDead = in.fIsDead;
		flyDeath = in.flyDeath;
		fDirIdentified = in.fDirIdentified;

		height = getHeight(nType);
		noFireballDeath = getFlameproof(nType);

		fBeyondHorizonOnGround = in.fBeyondHorizonOnGround;
	}

	public boolean isThisMe(byte nType, float x, float y) {
		if ((nType != this.nType) && (this.nType != -1)) return false;

		// We're dead
		if ((x == nXP+2) && (y == nYP - 5))
			return true;

		if (winged) {
			if ( (Math.abs(x-this.x)>4) ||
					(Math.abs(y-this.y)>12) ) {
				// We've been stomped
				if ((nType == 4) || (nType == 6) && (y == this.y-5)) {
					return true;
				}
				return false;
			}
		} else {
			if ( (Math.abs(x-this.x)>4) ||
					((y-this.y>4) || (this.y-y>4)) ) {
				// We've been stomped
				if ((nType == 4) || (nType == 6) && (y == this.y-5)) {
					return true;
				}
				return false;
			}			
		}


		if (this.nType == -1) this.nType = nType;

		return true;
	}

	private static final float sideWaysSpeed = 1.75f;

	boolean fIsBeyondHorizon = false;

	float nXAP;
	float nYAP;	// = (winged) ? -10 : 2;

	public boolean move(TheoreticMario mario, Frame frame) {
		if ((nType == Visualizer.SHELL) && (nXP == 0) && (nYP == 0)) {
			return false;
		}

		if (xOld == -1) {
			xOld = x; yOld =y;
		} else {
			xOld = nXP; yOld = nYP;
		}
		nXP = x; nYP = y; nYAP = ya; nXAP = xa;

		//        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (deadTime > 0) {

			deadTime--;

			if (deadTime == 0)
			{
				deadTime = 1;
				return true;
			}

			if (flyDeath) {
				x += xa;
				y += ya;
				ya = ya * 0.95f + 1;
			}
			//			ya += 1;

			if (xa > 30) throw new java.lang.NullPointerException();

			if (fDebug) 
			System.out.println(" &&dead"+
					((winged)?"W":"")+((mayJump)?"J":"")+((onGround)?"g":"")+
					((avoidCliffs)?"a":"")+
					":"+nXP+","+nYP+" => "+x+","+y+","+ya+" ");

			return false;
		}

		if (xa > 2)
		{
			facing = 1;
		}
		if (xa < -2)
		{
			facing = -1;
		}

		xa = facing * sideWaysSpeed;

		mayJump = (onGround);

		//        runTime += (Math.abs(xa)) + 5;

		if (mario == null) System.out.println("MERIO is NULL");
		if (mario != null) {
			// The sequence of events is:
			// 1. enemy initialization, 2. move mario, 3. move enemies
			// The collision grid is based on marios position before the move
			float mario_x = (!fInitializingMove) ? mario.nXP : mario.x;
			float mario_y = (!fInitializingMove) ? mario.nYP : mario.y;
			int nX = (int)(xOld/16)-(int)(mario_x/16) +11;
			int nY = (int)(yOld/16)-(int)(mario_y/16) +11;
			fIsBeyondHorizon = ((nX<0) || (nY<0) || (nX > 21) || (nY > 21));			
		}

		if (!move(xa, 0)) facing = -facing;
		onGround = false;

		move(0, ya);

		ya *= winged ? 0.95f : 0.85f;
		xa *= ((onGround) ? GROUND_INERTIA : AIR_INERTIA);

		if (!onGround)
		{
			if (winged)
			{
				ya += 0.6f;
			}
			else
			{
				ya += 2;
			}
		}
		else if (winged)
		{
			ya = -10;
		}

		if (fDebug) 
		System.out.println(" &&"+
				((winged)?"W":"")+((mayJump)?"J":"")+((onGround)?"g":"")+
				((avoidCliffs)?"a":"")+
				":"+nXP+","+nYP+","+nXAP+","+nYAP+" => "+x+","+y+","+xa+","+ya+" ");

		return false;
	}

	private boolean move(float xa, float ya)
	{
		
		while (xa > 8)
		{
			if (!move(8, 0)) return false;
			xa -= 8;
		}
		while (xa < -8)
		{
			if (!move(-8, 0)) return false;
			xa += 8;
		}
		while (ya > 8)
		{
			if (!move(0, 8)) return false;
			ya -= 8;
		}
		while (ya < -8)
		{
			if (!move(0, -8)) return false;
			ya += 8;
		}

		boolean collide = false;
		if (ya > 0)
		{
			if (!winged && fIsBeyondHorizon && fBeyondHorizonOnGround) {
				collide = true;
			}
			if (isBlocking(x + xa - width, y + ya, xa, 0)) collide = true;
			else if (isBlocking(x + xa + width, y + ya, xa, 0)) collide = true;
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) collide = true;
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) collide = true;			
		}
		if (ya < 0)
		{
			if (isBlocking(x + xa, y + ya - height, xa, ya)) collide = true;
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;

			if (winged && fIsBeyondHorizon) collide = false;		
		}
		if (xa > 0)
		{
			if (isBlocking(x + xa + width, y + ya - height, xa, ya)) collide = true;
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) collide = true;
			if (isBlocking(x + xa + width, y + ya, xa, ya)) collide = true;

			if (avoidCliffs && onGround && 
					!level.isBlocking((int) ((x + xa + width) / 16), (int) ((y) / 16 + 1), xa, 1)) collide = true;

			if (fIsBeyondHorizon) collide = false;
		}
		if (xa < 0)
		{
			if (isBlocking(x + xa - width, y + ya - height, xa, ya)) collide = true;
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) collide = true;
			if (isBlocking(x + xa - width, y + ya, xa, ya)) collide = true;

			if (avoidCliffs && onGround && 
					!level.isBlocking((int) ((x + xa - width) / 16),
							(int) ((y) / 16 + 1), xa, 1)) 
				collide = true;

			if (fIsBeyondHorizon) collide = false;
			if (collide && fDebug) System.out.println("Ecollide:-x:"+
					x+","+y+","+xa+","+ya+","+width+height);
		}

		if (collide)
		{
			if (xa < 0)
			{
				x = (int) ((x - width) / 16) * 16 + width;
//				System.out.println("Tenem-coll:-x="+x+", w="+width+" h="+height+" xa="+xa);
				this.xa = 0;
			}
			if (xa > 0)
			{
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
//				System.out.println("Tenem-coll:x="+x);
				this.xa = 0;
			}
			if (ya < 0)
			{
				y = (int) ((y - height) / 16) * 16 + height;
				//                jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0)
			{
//				System.out.println("TE-onground");
				y = (int) (y / 16 + 1) * 16 - 1;
				onGround = true;
			}
			return false;
		}
		else
		{
			x += xa;
			y += ya;
			return true;
		}
	}

	private boolean isBlocking(float _x, float _y, float xa, float ya)
	{
		//    	System.out.println("%"+_x+","+_y+"% ");

		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

		boolean blocking = level.isBlocking(x, y, xa, ya);

		byte block = TheoreticLevel.getBlock(x, y);

		if (blocking && fDebug)
			System.out.print("["+block+","+x+","+y+"]");

		return blocking;
	}


	public TheoreticEnemy collideCheck(TheoreticMario mario, Frame frame) {
		TheoreticEnemy result = null;
		
		if (deadTime != 0) return null;

		float xMarioD = mario.x - x;
		float yMarioD = mario.y - y;
		if (fDebug) System.out.print("E-check("+mario.y+"-"+y+"="+yMarioD+
				","+nType+","+mario.ya+","+mario.onGround+","+mario.wasOnGround+
				","+xMarioD+","+mario.x+","+x+","+height);
		
		float w = 16;
		if (xMarioD > -width*2-4 && xMarioD < width*2+4)
		{
	        if (fDebug) System.out.print("^#");	
			if (yMarioD > -height && yMarioD < mario.height)
			{
		        if (fDebug) System.out.print("v");	
//				System.out.println("TCOL-E!(E:"+nType+","+x+","+y+" D:"+xMarioD+","+yMarioD+",w"+width+" h"+height);

				if (nType != Visualizer.ENEMY_SPINY && nType != 10 && nType != 12 &&
						mario.ya > 0 && 
						yMarioD <= 0 && (!mario.onGround || !mario.wasOnGround))
				{
					mario.stomp(this);
					if (winged)
					{
						if (fDebug) System.out.println("T-UNWING");
						winged = false;
						ya = 0;
					}
					else
					{
						deadTime = 10;
						winged = false;

						if ((nType == 4) || (nType == 6)) {
							TheoreticShell shell = new TheoreticShell(enemies,
									level, x, y);
//							iterEnemies.add(shell);
							shell.move(mario, frame);
							result = shell;
						}
					}
				}
				else
				{
					mario.getHurt();
				}
			}
		}
		
		return result;
	}

	   public boolean shellCollideCheck(TheoreticShell shell, TheoreticMario mario)
	{
		   if (fDebug) System.out.println("enemy::shellCollideCheck("+this.nType);

	    	if (deadTime != 0) return false;

		float xD = shell.x - x;
		float yD = shell.y - y;

		if (xD > -16 && xD < 16)
		{
			if (yD > -height && yD < shell.height)
			{
				xa = shell.facing * 2;
				ya = -5;
				flyDeath = true;
				deadTime = 100;
				winged = false;
				return true;
			}
		}
		return false;
	}

	public boolean fireballCollideCheck(TheoreticFireball fireball)
	{
		if (deadTime != 0) return false;

		float xD = fireball.x - x;
		float yD = fireball.y - y;

		if (xD > -16 && xD < 16)
		{
			if (yD > -height && yD < fireball.height)
			{
				if (fDebug)
				System.out.println("Enemy "+nType+" was hit by fireball at "+
						x+","+y+","+noFireballDeath);

				if (noFireballDeath) return true;
				
				xa = fireball.facing * 2;
				ya = -5;
				flyDeath = true;
				deadTime = 100;
				winged = false;
				return true;
			}
		}
		return false;
	}

	public boolean fixupDefinite(byte nType, float x, float y) {
		if (nType != this.nType) return false;

//		if (fDebug)
//		System.out.println("fixing "+this.nType+","+this.x+","+this.y+
//				" => "+nType+","+x+","+y);

		if ((Math.abs(x-this.x) > 30) || (Math.abs(y-this.y) > 30))
			return false;
		
		if ((y==nYP) && (x == nXP - 1.75) && (deadTime > 0)) {
			System.out.println("Resurrecting!"+nType+","+x+","+y);
			deadTime = 0;
			this.x = x; this.y = y;
			xa = -1.75f; xa *= 0.89;
			this.ya = nYAP;
			ya *= winged ? 0.95f : 0.85f;
			return true;
//			throw new java.lang.NullPointerException();			
		}
		
		if ( (Math.abs(y - nYP + nYAP) <= 0.00001) && (x == this.x)) {
			this.y = y; onGround = false;

			ya = nYAP;
			ya *= winged ? 0.95f : 0.85f;
			this.ya += 2;
//			System.out.println("Simple fix: "+ya);
			return true;
		}

		// We thought we had collided horizontally while travelling on ground, but we hadn't
		if (((x == nXP +1.75) || (x == nXP -1.75)) && 
				((y == this.y) || (y == nYP))) {
//			System.out.println("Maybe reversing");
			this.x = x; this.y = y;
			xa = x-nXP;
			facing = (xa < 0) ? -1 : 1;
			xa *= 0.89;
			return true;
		}

		// We thought we had collided vertically, but we hadn't
		// (I.e. we didn't collide with the ground so we're falling
		if ((y == nYP + nYAP) && (x == this.x)) {
			onGround = false; fBeyondHorizonOnGround = false;
			this.y = y;
			ya = nYAP;
			ya *= 0.85f;			
			ya += 2;
//			System.out.println("FIXUP falling: ya:"+nYAP+" => "+ya);
			return true;
		}
		if ((x == this.x) && (y == nYP+2)) {
			onGround = false; fBeyondHorizonOnGround = false;
			this.y = y;
			ya = 2;
			ya *= 0.85f;			
			ya += 2;
			return true;
		}

		boolean fXCollision = (Math.abs(x-nXP) > (winged?16:11)) ? false :
			(16*(int)((x-width)/16) == x-width) || (16*(int)((x+5)/16) == x+5);

		// We didn't realize we had collided with something in the x-dir
		if ((y == this.y) && fXCollision) {
			this.x = x;
			xa = 0;
			//			throw new java.lang.NullPointerException();
			return true;
		}
		if ((y == nYP+nYAP) && fXCollision) {
			onGround = false; fBeyondHorizonOnGround = false;
			this.y = y; this.x = x;
			xa = 0; ya = nYAP; facing = -facing;
			ya *= 0.85f;			
			ya += 2;
			return true;
		}

		// We were falling and then collided with the ground but didn't notice
		if ((Math.abs(y-nYP) <= 11) && (x == this.x)) {
			if ( (16*(int)((y+4)/16) == y+4) || (16*(int)((y+8)/16) == y+8) ||
					(16*(int)((y+1)/16) == y+1)) {
				this.y = y; onGround = (!winged);
				this.ya = 0;
				ya += 0.6f;
				return true;
			}
		}

		// We were flying and then collided with the ground but didn't notice
		if (winged && (x == this.x) && (16*(int)((y+1)/16) == y+1) &&
				Math.abs(this.y-nYP) <= 11) {
//			System.out.println("FLYING -> GROUND");
			this.y = y; onGround = true;
			ya = -10;
			ya *= 0.89f;
			return true;
		}

		// We guessed the wrong direction
		if ( (y == this.y) && ((x-nXP) == -(this.x-nXP)) ) {
//			System.out.println("Flip direction:");
			this.x += 2*(x-nXP);
			xa = -xa; facing = -facing;
			return true;
		}

		if ((x == this.x) && (y == nYP-10) && !winged) {
			// We can fly!
			this.y = y;
			onGround = false; winged = true;
			ya = -10;
			ya *= 0.89;
			// throw new java.lang.NullPointerException();
			return true;
		}
		if ((x == this.x) && (y == nYP-10) && winged) {
			this.y = y;
			onGround = false;
			ya = -10;
			ya *= 0.89;
			return true;
		}
		// We were killed
		if ((Math.abs(x-nXP) == 2) && (y == nYP-5)) {
//			System.out.println("We thought it was alive but it's dead: "+x+","+y);
			this.x = x; this.y = y;
			ya = -3.75f;
			xa = x-nXP;
			deadTime = 9; flyDeath = true;
			return true;
		}
		if ((x == nXP+2) && (y == nYP+nYAP)) {
//			System.out.println("We thought it was alive but it's dead: "+x+","+y);
			this.x = x; this.y = y;
			ya = nYAP;			
			ya = ya * 0.95f + 1;
			xa = 2;
			deadTime = 9; flyDeath = true;
			return true;
		}

		// We thought we were flying, but we were on the ground
		if (winged && (x == this.x) && (y == nYP)) {
			this.y = y;
			onGround = true;
			ya= -10;
			ya *= 0.89;
			return true;
		}

		// We thought we were on-ground but we were flying and we collided
		if (winged && (y == nYP-10) && fXCollision) {
//			System.out.println("WE ARE FLYING");
			this.y = y; this.x =x;
			xa = 0;
			ya= -10;
			ya *= 0.89;
			return true;
		}

		return false;
//		System.out.println("Couldn't fix new:"+x+","+y+" guess:"+this.x+","+this.y+
//				" old:"+nXP+","+nYP+" dY:"+nYAP+", W="+width+", H="+height);
//		System.out.println(y+" VS "+(nYP + nYAP));

//		throw new java.lang.NullPointerException();
	}

	public boolean fixupProbable(byte nType, float x, float y) {
		if (nType != this.nType) return false;
		
		if ((Math.abs(x-this.x) > 5) || (Math.abs(y-this.y) > 5)) return false;
/*		
		if (winged && (y==this.y) && (Math.abs(x-nXP)<=16) && 
				(16*(int)((x-4)/16) == x-4)) {
			this.x = x; this.y = y; ya = 10; xa = 0;
			ya *= 0.89f;
			return true;
		}
*/		
		this.x = x; this.y = y;
		onGround = (y == nYP);
		xa = x - nXP; ya = y - nYP;
		
		ya *= winged ? 0.95f : 0.85f;
		xa *= ((onGround) ? GROUND_INERTIA : AIR_INERTIA);

		if (!onGround)
		{
			ya += (winged) ? 0.6f : 2;
		}
		else if (winged)
		{
			ya = -10;
		}
		
		return true;
	}
	
}
