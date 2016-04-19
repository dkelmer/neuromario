package competition.icegic.peterlawford.simulator;

import java.util.LinkedList;
import java.util.ListIterator;

import competition.icegic.peterlawford.SlowAgent;

public class Frame {

	public static long marioTime = 0;
	public static long simTime = 0;

	public static long max_marioTime = 0;
	public static long max_simTime = 0;

	public final SlowAgent agent;
	public final TheoreticMario mario;
	public final TheoreticEnemies enemies;

	//		public int nCycleId;

	private LinkedList<TheoreticShell> shellsToCheck = null;
	private LinkedList<TheoreticFireball> fireballsToCheck = null;

	public int fireballsOnScreen = 0;

	public Frame(SlowAgent agent,
			TheoreticMario mario,
			TheoreticEnemies enemies,
			int fireballsOnScreen) {
		this.mario = mario;
		this.agent = agent;
		this.enemies = enemies;
		this.fireballsOnScreen = fireballsOnScreen;
		//			nCycleId = 0;
	}

	public Frame(Frame in, boolean fDebug) {
		agent = in.agent;
		// The enemies can see and react to mario
		enemies = new TheoreticEnemies(in.enemies, fDebug);
		mario = new TheoreticMario(in.mario, enemies);
		fireballsOnScreen = in.fireballsOnScreen;
		//			nCycleId = in.nCycleId;
	}

	public void checkShellCollide(TheoreticShell shell) {
		if (shellsToCheck == null) 
			shellsToCheck = new LinkedList<TheoreticShell>();
		shellsToCheck.add(shell);
		//		throw new java.lang.NullPointerException();
	}
	public void checkFireballCollide(TheoreticFireball fireball) {
		//		System.out.println("Registering fireball");
		if (fireballsToCheck == null) 
			fireballsToCheck = new LinkedList<TheoreticFireball>();
		fireballsToCheck.add(fireball);
		//		throw new java.lang.NullPointerException();
	}

	public void move(boolean[] keys) {
		//			nCycleId++;

		long time_t = System.currentTimeMillis();

		mario.keys = keys;
		boolean fPause = mario.fPauseWorld;

		float xCam = mario.x - 160;
		float yCam = 0;

		if (xCam < 0) xCam = 0;
		//        if (xCam > level.width * 16 - 320) xCam = level.width * 16 - 320;

		fireballsOnScreen = 0;
		//		for (TheoreticEnemy e : enemies.enemies)
		//			if (e instanceof TheoreticFireball) fireballsOnScreen++;

		ListIterator<TheoreticEnemy> iterT = enemies.enemies.listIterator();
		//		for (TheoreticEnemy e : enemies.enemies) {
		while (iterT.hasNext()) {
			TheoreticEnemy e = iterT.next();
			float xd = e.x - xCam;
			float yd = e.y - yCam;
			if (xd < -64 || xd > 320 + 64 || yd < -64 || yd > 240 + 64) {
				iterT.remove();
			} else {
				if (e instanceof TheoreticFireball)
					fireballsOnScreen++;
			}
		}


		if (fireballsOnScreen > 2) {
			System.err.println("Too many fireballs on screen:"+fireballsOnScreen);
			throw new java.lang.NullPointerException();
		}

		if (!fPause) {
			enemies.move(mario, this);
		}

		{
			long time_m = System.currentTimeMillis();
			// Move mario last
			mario.move(enemies, this);
			time_m = System.currentTimeMillis() - time_m;
			marioTime += time_m;
			if (time_m > max_marioTime) max_marioTime = time_m;
		}

		if (!fPause) {
			//				for (TheoreticEnemy enemy : enemies.enemies) {

			// If mario stomps a shell before colliding with an enemy he's safe,
			// but the other way round he gets hurt.

			// because of this, new enemies MUST be added to the front of the list
			LinkedList<TheoreticEnemy> new_enemies = null;
			//			Iterator<TheoreticEnemy> iterEnemies = enemies.enemies.iterator();
			//			while (iterEnemies.hasNext()) {
			//				TheoreticEnemy enemy = iterEnemies.next();
			for (TheoreticEnemy enemy : enemies.enemies) {
				if (enemy instanceof TheoreticFireball) continue;

				TheoreticEnemy result = enemy.collideCheck(mario, this);
				if (result != null) {
					if (new_enemies == null) new_enemies = new LinkedList<TheoreticEnemy>();
					new_enemies.add(result);
				}
			}	

			if (shellsToCheck != null) {
				if (enemies.fDebug)
					System.out.println("=== Checking for shell collision ===");
				for (TheoreticShell shell : shellsToCheck)
					for (TheoreticEnemy sprite : enemies.enemies) {
						if (sprite instanceof TheoreticFireball) continue;
						if ((sprite != shell) && !shell.dead)
							if (sprite.shellCollideCheck(shell, mario))
							{
								if (enemies.fDebug) System.out.println("SHELL-COLLISION");

								if ((mario.carried == shell) && !shell.dead)
								{
									mario.carried = null;
									shell.die();
								}
							}
					}

				shellsToCheck = null;
			}

			if (fireballsToCheck != null) {
				for (TheoreticFireball fireball : fireballsToCheck)
					for (TheoreticEnemy sprite : enemies.enemies) {
						if (sprite instanceof TheoreticFireball) continue;
						if (!fireball.dead) {
							if (sprite.fireballCollideCheck(fireball)) {
								//								System.out.println("somebody killed fireball");
								fireball.die();
							}
						}
					}
				fireballsToCheck= null;
			}

			if (new_enemies != null)
				enemies.enemies.addAll(0, new_enemies);

		} else {
			if (enemies.fDebug)
			System.out.println(" - skipping enemies due to pain - ");
		}

		mario.fPauseWorld = mario.fNextPauseWorld;

		simTime += (System.currentTimeMillis() - time_t);

	}

	@Override
	public boolean equals(Object o) {
		Frame f_other = (Frame)o;

		if ((mario.getX() != f_other.mario.getX()) ||
				(mario.getY() != f_other.mario.getY()) ||
				(mario.xa != f_other.mario.xa) ||
				(mario.ya != f_other.mario.ya)) {
//			if (mario.getX() != f_other.mario.getX()) System.err.print("Mx");
//			if (mario.getY() != f_other.mario.getY()) System.err.print("My");
//			if (mario.xa != f_other.mario.xa) System.err.print("Mxa");
//			if (mario.ya != f_other.mario.ya) System.err.print("Mya");
			return false;
		}
		if (enemies != null) {
			if (!enemies.equals(f_other.enemies)) return false;
		} else {
			if (f_other.enemies != null) return false;
		}

		return true;
	}
}
