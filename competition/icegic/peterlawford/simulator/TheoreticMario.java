package competition.icegic.peterlawford.simulator;

import ch.idsia.mario.engine.sprites.Mario;
import ch.idsia.mario.environments.Environment;

public class TheoreticMario extends TheoreticSprite {	//extends Mario {

	//	private static float GROUND_INERTIA = 0.89f;
	//	private static float AIR_INERTIA = 0.89f;

	public boolean[] keys;
	public int facing;

	public int invulnerableTime = 0;
	public int powerUpTime = 0;
	public boolean fPauseWorld = false;
	public boolean fNextPauseWorld = false;

	public int deathTime = 0;

	private float runTime;
	public boolean wasOnGround = false;
	public boolean onGround = false;
	public boolean mayJump = false;
	private boolean ducking = false;
	public boolean sliding = false;
	public int jumpTime = 0;
	public float xJumpSpeed;
	public float yJumpSpeed;

	public boolean canShoot = false;

	public int status = Mario.STATUS_RUNNING;

	public boolean fire;
	public boolean large;

	private static final int FractionalPowerUpTime = 0;

	public int width = 4;
	public int height;	// = 24;

	TheoreticShell carried = null;

	TheoreticLevel level;

	boolean fDebug = true;
	
	//	    public TheoreticMario() { super(null); }
	public TheoreticMario(TheoreticMario in, TheoreticEnemies enemies) {
		this(in.level, in.x, in.y, 
				in.onGround, in.wasOnGround, in.mayJump,
				in.large, in.fire,
				in, enemies);

	}

	public TheoreticMario(TheoreticLevel level, 
			float x_in, float y_in,
			boolean fMarioIsOnGround, boolean fMarioWasOnGround, boolean fMarioMayJump,
			boolean isMarioLarge, boolean isMarioPacking,
			TheoreticMario mario, TheoreticEnemies enemies
	) {
		this(level,
				x_in, y_in,
				mario.xa,
				mario.ya,
				fMarioIsOnGround, fMarioWasOnGround, fMarioMayJump,

				mario.xJumpSpeed,
				mario.yJumpSpeed,
				mario.jumpTime,
				mario.sliding,
				mario.facing,
				mario.fIsHurt,

				isMarioLarge, isMarioPacking, mario.canShoot,
				mario.powerUpTime,
				mario.fPauseWorld,
				mario.invulnerableTime,
				mario.ducking);
		//				env.getMarioFloatPos()[0]-prevX,
		//				env.getMarioFloatPos()[1]-prevY	
		if (mario.carried != null) {
			for (TheoreticEnemy e: enemies.enemies) {
				if ((e instanceof TheoreticShell) &&
						(e.x == mario.carried.x) && (e.y == mario.carried.y)) {
					carried = (TheoreticShell)e;
					break;
				}
			}
		}

		if (enemies != null) {
			fDebug = enemies.fDebug;
		} else {
			fDebug = false;
		}
		
		status = mario.status;
		nXP = mario.nXP;
		nYP = mario.nYP;
	}

	public TheoreticMario(TheoreticLevel level, 
			float x, float y, float xa, float ya,
			boolean onGround, boolean wasOnGround, boolean mayJump,
			float xJumpSpeed, float yJumpSpeed, int jumpTime,
			boolean sliding, int facing, boolean fIsHurt,
			boolean large, boolean fire, boolean canShoot,
			int powerUpTime, boolean fPauseWorld,
			int invulnerableTime, boolean ducking) {
		super(Visualizer.MARIO, x, y, xa, ya);
		this.level = level;
		//		this.x = x; this.xa = xa;
		//		this.y = y; this.ya = ya;
		this.onGround = onGround;
		this.wasOnGround = wasOnGround;
		this.mayJump = mayJump;
		this.xJumpSpeed = xJumpSpeed;
		this.yJumpSpeed = yJumpSpeed;
		this.jumpTime = jumpTime;
		this.sliding = sliding;
		this.facing = facing;
		this.fIsHurt = fIsHurt;

		this.powerUpTime = powerUpTime;
		this.fPauseWorld = fPauseWorld;

		height = (large) ? 24 : 12;
		this.large = large;
		this.fire = fire;
		this.canShoot = canShoot;
		this.invulnerableTime = invulnerableTime;
		this.ducking = ducking;
		
		fDebug = false;
	}

	void setLarge(boolean large, boolean fire)
	{
//		System.out.println("large => "+large+", fire => "+fire);

		if (fire) large = true;
		if (!large) fire = false;

		//	        lastLarge = Mario.large;
		//	        lastFire = Mario.fire;

		this.large = large;
		this.fire = fire;

		//        newLarge = Mario.large;
		//        newFire = Mario.fire;

		//        blink(true);
	}


	public void move(TheoreticEnemies enemies, Frame frame) {
		if (fDebug)
		System.out.print("THEO_MARIO:"+fPauseWorld+":"+
				x+","+y+","+xa+","+ya+","+xJumpSpeed+
				"("+sliding+","+onGround+","+ducking+")");

		nXP = x; nYP = y;

		if (powerUpTime != 0)
		{
			if (powerUpTime > 0)
				powerUpTime--;
			else
				powerUpTime++;

			System.out.println("TPOWERUP="+powerUpTime);
			if (powerUpTime == 0) fNextPauseWorld = false;
			return;
		}

		if (invulnerableTime > 0) invulnerableTime--;

		fIsHurt = false;

		//This function is a sliced up version of the Mario::move function
		wasOnGround = onGround;
		float sideWaysSpeed = keys[Mario.KEY_SPEED] ? 1.2f : 0.6f;
		//        float sideWaysSpeed = onGround ? 2.5f : 1.2f;

		if (onGround)
		{
			if (keys[Mario.KEY_DOWN] && large)
			{
				ducking = true;
			}
			else
			{
				ducking = false;
			}
		}

		if (xa > 2)
		{
			facing = 1;
		}
		if (xa < -2)
		{
			facing = -1;
		}

		if (keys[Mario.KEY_JUMP] || (jumpTime < 0 && !onGround && !sliding))
		{
			if (jumpTime < 0)
			{
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				jumpTime++;
			}
			else if (onGround && mayJump)
			{
				xJumpSpeed = 0;
				yJumpSpeed = -1.9f;
				jumpTime = 7;
				ya = jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
			}
			else if (sliding && mayJump)
			{
				xJumpSpeed = -facing * 6.0f;
				yJumpSpeed = -2.0f;
				jumpTime = -6;
				xa = xJumpSpeed;
				ya = -jumpTime * yJumpSpeed;
				onGround = false;
				sliding = false;
				facing = -facing;
			}
			else if (jumpTime > 0)
			{
				xa += xJumpSpeed;
				ya = jumpTime * yJumpSpeed;
				jumpTime--;
			}
			if (fDebug)
			System.out.print(" ==> "+x+","+y+","+xa+","+ya+
					"("+jumpTime+"/"+xJumpSpeed+"/"+yJumpSpeed+"/"+facing+")");
		}
		else
		{
			jumpTime = 0;
		}

		if (keys[Mario.KEY_LEFT] && !ducking)
		{
			if (facing == 1) sliding = false;
			xa -= sideWaysSpeed;
			if (jumpTime >= 0) facing = -1;
		}

		if (keys[Mario.KEY_RIGHT] && !ducking)
		{
			if (facing == -1) sliding = false;
			xa += sideWaysSpeed;
			if (jumpTime >= 0) facing = 1;
		}

		if ((!keys[Mario.KEY_LEFT] && !keys[Mario.KEY_RIGHT]) || ducking || ya < 0 || onGround)
		{
			sliding = false;
		}

		if (frame != null) {
//			System.out.println("There are "+frame.fireballsOnScreen+" fireballs");
			if (fDebug)
		       System.out.print("  FIRE? "+
		    		   keys[Mario.KEY_SPEED]+","+canShoot+","+fire+
		        		","+frame.fireballsOnScreen);

		       if (keys[Mario.KEY_SPEED] && canShoot && fire && 
					(level != null) && frame.fireballsOnScreen<2)
			{
				TheoreticFireball f = new TheoreticFireball(
						enemies, level, null,
						x+facing*6, y-20, facing);
				enemies.enemies.addFirst(f);
				f.move(this, frame);
//				frame.fireballsOnScreen++;
				//            world.addSprite(new Fireball(world, x+facing*6, y-20, facing));
			}
		}

		/*************************************************************
		 *  Some irrelevant stuff
		 */

		fNextPauseWorld = (level == null) ? false : TheoreticLevel.pauseWorld;
		canShoot = !keys[Mario.KEY_SPEED];

		mayJump = (onGround || sliding) && !keys[Mario.KEY_JUMP];

		runTime += (Math.abs(xa)) + 5;
		if (Math.abs(xa) < 0.5f)
		{
			runTime = 0;
			xa = 0;
		}

		calcPic();

		if (sliding)
		{
			/*           for (int i = 0; i < 1; i++)
            {
                world.addSprite(new Sparkle((int) (x + Math.random() * 4 - 2) + facing * 8, (int) (y + Math.random() * 4) - 24, (float) (Math.random() * 2 - 1), (float) Math.random() * 1, 0, 1, 5));
            } */
			ya *= 0.5f;
		}


		if (fDebug)
		System.out.print(" **> "+x+","+y+","+xa+","+ya+
				"("+jumpTime+"/"+xJumpSpeed+"/"+yJumpSpeed+"/"+facing+"/"+sliding+
				"/"+ducking+"/"+onGround+")");

		onGround = false;
		move(xa, 0);
		move(0, ya);

		if (fDebug)
		System.out.print(" ::> "+x+","+y+","+xa+","+ya+
				"("+jumpTime+"/"+onGround+"/"+sliding+")");

		/*
        if (y > world.level.height * 16 + 16)
        {
            die();
        }
		 */
		if (x < 0)
		{
			x = 0;
			xa = 0;
		}
		/*
        if (x > world.level.width * 16)
        {
            x = world.level.width * 16;
            xa = 0;
        }
		 */
		ya *= 0.85f;
		if (onGround)
		{
			xa *= GROUND_INERTIA;
		}
		else
		{
			xa *= AIR_INERTIA;
		}

		if (!onGround)
		{
			ya += 3;
		}

		if (carried != null)
		{
//			System.out.println("Tcarrying-shell");

			carried.x = x + facing * 8;
			carried.y = y - 2;
			if (!keys[Mario.KEY_SPEED])
			{
				carried.release(this);
				carried = null;
			}
		}

		if (fDebug)
		System.out.println(" --> "+x+","+y+","+xa+","+ya+"("+sliding+")");
	}

	private void calcPic() {
		if (large)
		{
			height = ducking ? 12 : 24;
		}
		else
		{
			height = 12;
		}
	}

	private boolean move(float xa, float ya)
	{
//		if (fDebug) System.out.println("tmov("+xa+","+ya+","+width+","+height);
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
			if (isBlocking(x + xa - width, y + ya, xa, 0)) {
//				System.out.println("TCL1");
				collide = true;
			}
			else if (isBlocking(x + xa + width, y + ya, xa, 0)) {
//				System.out.println("TCL2");
				collide = true;
			}
			else if (isBlocking(x + xa - width, y + ya + 1, xa, ya)) {
//				System.out.println("TCL3");
				collide = true;
			}
			else if (isBlocking(x + xa + width, y + ya + 1, xa, ya)) {
//				System.out.println("TCL4");
				collide = true;
			}
		}
		if (ya < 0)
		{
			if (isBlocking(x + xa, y + ya - height, xa, ya)) {
	//			System.out.println("TCL5");
				collide = true;
			}
			else if (collide || isBlocking(x + xa - width, y + ya - height, xa, ya)) {
	//			System.out.println("TCL6");
				collide = true;
			}
			else if (collide || isBlocking(x + xa + width, y + ya - height, xa, ya)) {
				int x2 = (int) ( (x + xa + width) / 16);
				int y2 = (int) ( (y + ya - height) / 16);
	//			System.out.println("TCL7");
				collide = true;
			}
		}
		if (xa > 0)
		{
			sliding = true;
			if (isBlocking(x + xa + width, y + ya - height, xa, ya)) {
	//			System.out.println("TCL8");
				collide = true;
			}
			else {
				sliding = false;
	//			System.out.print("NOSLIDE1 ");
			}
			if (isBlocking(x + xa + width, y + ya - height / 2, xa, ya)) {
	//			System.out.println("TCL9");
				collide = true;
			}
			else {
				sliding = false;
	//			System.out.print("NOSLIDE2 ");
			}
			if (isBlocking(x + xa + width, y + ya, xa, ya)) {
	//			System.out.println("TCLa");
				collide = true;
			}
			else {
				sliding = false;
	//			System.out.print("NOSLIDE3 ");
			}
		}
		if (xa < 0)
		{
//			System.out.print(" xa<0 ");
			sliding = true;
			if (isBlocking(x + xa - width, y + ya - height, xa, ya)) {
//				System.out.println("TCLb");
				collide = true;
			}
			else {
				sliding = false;
			}
			if (isBlocking(x + xa - width, y + ya - height / 2, xa, ya)) {
//				System.out.println("TCLc:"+xa+","+sliding);
				collide = true;
			}
			else {
				sliding = false;
			}
			if (isBlocking(x + xa - width, y + ya, xa, ya)) {
//				System.out.println("TCLd");
				collide = true;
			}
			else {
				sliding = false;
			}
		}

//		System.out.println("SLIDING="+sliding);

		if (collide)
		{
//			System.out.println("TCOLLIDE("+x+","+y+","+xa+","+ya+","+width+","+height);
			if (xa < 0)
			{
				x = (int) ((x - width) / 16) * 16 + width;
				this.xa = 0;
			}
			if (xa > 0)
			{
				x = (int) ((x + width) / 16 + 1) * 16 - width - 1;
				this.xa = 0;
			}
			if (ya < 0)
			{
				y = (int) ((y - height) / 16) * 16 + height;
				jumpTime = 0;
				this.ya = 0;
			}
			if (ya > 0)
			{
//				System.out.print("ON-GROUND");
				y = (int) ((y - 1) / 16 + 1) * 16 - 1;
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

	public boolean isBlocking(float _x, float _y, float xa, float ya)
	{
		//		System.out.println("isB:"+_x+","+_y+","+xa+","+ya);
		int x = (int) (_x / 16);
		int y = (int) (_y / 16);
		if (x == (int) (this.x / 16) && y == (int) (this.y / 16)) return false;

		//        boolean blocking = false;

		boolean blocking = (level == null) ? false : level.isBlocking(x, y, xa, ya);

//		if (blocking) System.out.println("Tblock:"+_x+","+_y+","+xa+","+ya);

		byte block = (level == null) ? 0 : TheoreticLevel.getBlock(x, y);
		if (fDebug && blocking) {
			System.out.print("["+block+",x"+x+",y"+y+
					",Mx"+((int)(this.x/16))+",My"+((int)(this.y/16))+"]");
		}

		return blocking;
	}

	//    private boolean world_level_isBlocking(int x, int y, float xa, float ya) {

	//   }

	public void stomp(TheoreticEnemy enemy)
	{
//		System.out.print("STOMP:"+deathTime+","+fPauseWorld);
		if (deathTime > 0 || fPauseWorld) return;

		float targetY = enemy.y - enemy.height / 2;

//		System.out.println("TSTOMP-E: "+targetY+"-"+y);

		move(0, targetY - y);

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f;
		jumpTime = 8;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;
	}

	public void stomp(TheoreticShell shell)
	{
//		System.out.println("TSTOMP SHELL"+shell.facing+","+fPauseWorld+","+keys[Mario.KEY_SPEED]);
		if (deathTime > 0 || fPauseWorld) return;
		//		       if (fPauseWorld) return;
		//		       if (deathTime > 0) return;

		if (keys[Mario.KEY_SPEED] && shell.facing == 0)
		{
			carried = shell;
			shell.carried = true;
		}
		else
		{
//			System.out.println("!!! STOMP !!!");

			float targetY = shell.y - shell.height / 2;
			move(0, targetY - y);

			xJumpSpeed = 0;
			yJumpSpeed = -1.9f;
			jumpTime = 8;
			ya = jumpTime * yJumpSpeed;
			onGround = false;
			sliding = false;
			invulnerableTime = 1;
		}
	}

	public boolean fIsHurt;
	//    public boolean isHurt() { return fIsHurt; }

	public void getHurt() {
    	if (fDebug) System.out.println("mario::getHurt("+
    			deathTime+","+fPauseWorld+","+invulnerableTime);

    	if (deathTime > 0 || fPauseWorld) return;
		//		if (fPauseWorld) return;
		if (invulnerableTime > 0) return;

//		System.out.println("TMARIO IS HURT!("+fPauseWorld+","+invulnerableTime+","+fire);
		fIsHurt = true;

		if (large)
		{
			if (fDebug) System.out.println("TPAUSE");
			fNextPauseWorld = true;
			fPauseWorld = true;
			powerUpTime = -3 * FractionalPowerUpTime;
			if (fire)
			{
				setLarge(true, false);
			}
			else
			{
				setLarge(false, false);
			} 
			invulnerableTime = 32;
		}
		else
		{
			die();
		}
	}


	public void die()
	{
		//     xDeathPos = (int) x;
		//      yDeathPos = (int) y;
		fPauseWorld = true;
		deathTime = 25;
		status = Mario.STATUS_DEAD;
	}


	public void getFlower()
	{
		if (deathTime > 0 || fPauseWorld) return;

		if (!fire)
		{
			//          world.paused = true;
			powerUpTime = 3 * FractionalPowerUpTime;
			//         world.mario.setLarge(true, true);
		}
		else
		{
			Mario.getCoin();
		}
	}

	public void getMushroom()
	{
		if (deathTime > 0 || fPauseWorld) return;

		if (!large)
		{
			//          world.paused = true;
			powerUpTime = 3 * FractionalPowerUpTime;
			//         world.mario.setLarge(true, false);
		}
		else
		{
			Mario.getCoin();
		}
	}

	public void kick(TheoreticShell shell)
	{
		if (deathTime > 0 || fPauseWorld) return;

		if (keys[Mario.KEY_SPEED])
		{
			carried = shell;
			shell.carried = true;
		}
		else
		{
			invulnerableTime = 1;
		}
	}

	public void stomp(TheoreticBulletBill bill)
	{
		if (deathTime > 0 || fPauseWorld) return;

		float targetY = bill.y - bill.height / 2;
		move(0, targetY - y);

		xJumpSpeed = 0;
		yJumpSpeed = -1.9f;
		jumpTime = 8;
		ya = jumpTime * yJumpSpeed;
		onGround = false;
		sliding = false;
		invulnerableTime = 1;
	}




	public static TheoreticMario getInitialMarioOnGround(float X_A) {
		return new TheoreticMario(null, 
				0, 0, X_A, 0,
				true, true, true,
				0, 0, 0,
				false, 1, false, 
				true, true, true,	// is_large, fire, can_shoot
				0, false, 0, false);
	}

	private final static float INITIAL_XA = 0;
	private final static float INITIAL_YA = 3;

	public static TheoreticMario getInitialMarioLevelStart(
			Environment env, TheoreticLevel level) {
		float prevX = 0; float prevY = 0;
		boolean prevWasOnGround = false;

		final	float nGuessedXJumpSpeed = 0;
		final	float nGuessedYJumpSpeed = 0;
		final	boolean fGuessSliding = false;
		final	int fGuessFacing = 1;
		final	int fGuessJumpTime = 0;

		return new TheoreticMario(
				level,
				env.getMarioFloatPos()[0],
				env.getMarioFloatPos()[1],
				INITIAL_XA,
				INITIAL_YA,
				env.isMarioOnGround(), prevWasOnGround,
				env.mayMarioJump(),

				nGuessedXJumpSpeed, nGuessedYJumpSpeed,
				fGuessJumpTime,
				fGuessSliding, fGuessFacing, false,

				true, true,	true, // is_large, fire, can_shoot
				0, false, 0, false);
	}
}
