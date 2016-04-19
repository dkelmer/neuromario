package competition.icegic.peterlawford.simulator;

import ch.idsia.mario.environments.Environment;

public class ClonedEnvironment implements Environment {
	
	final private byte[][] enemies;
	final private byte[][] scenery;
	final private byte[][] everything;
	final private float[] mario;
	final private int mario_mode;
	final private boolean mario_on_ground;
	final private boolean mario_may_jump;
	final private boolean mario_carrying;
	final private float[] enemies_list;
	
	public ClonedEnvironment(Environment env) {
		enemies = env.getEnemiesObservation();
		scenery = env.getLevelSceneObservation();
		everything = env.getCompleteObservation();
		mario = env.getMarioFloatPos();
		mario_mode = env.getMarioMode();
		mario_on_ground = env.isMarioOnGround();
		mario_may_jump = env.mayMarioJump();
		mario_carrying = env.isMarioCarrying();
		enemies_list = env.getEnemiesFloatPos();
	}

	// TODO: 1.6
//	@Override
	public String getBitmapEnemiesObservation() {
		throw new java.lang.NullPointerException();
	}

	// TODO: 1.6
//	@Override
	public String getBitmapLevelObservation() {
		throw new java.lang.NullPointerException();
	}

	// TODO: 1.6
//	@Override
	public float[] getEnemiesFloatPos() {
		return enemies_list;
	}

	// TODO: 1.6
//	@Override
	public byte[][] getEnemiesObservation() {
		return enemies;
	}

	// TODO: 1.6
//	@Override
	public byte[][] getLevelSceneObservation() {
		return scenery;
	}

	// TODO: 1.6
//	@Override
	public float[] getMarioFloatPos() {
		return mario;
	}

	// TODO: 1.6
//	@Override
	public int getMarioMode() {
		return mario_mode;
	}

	// TODO: 1.6

    public byte[][] getMergedObservationZ(int ZLevelScene, int ZLevelEnemies) {
        return new byte[0][];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte[][] getLevelSceneObservationZ(int ZLevelScene) {
        return new byte[0][];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte[][] getEnemiesObservationZ(int ZLevelEnemies) {
        return new byte[0][];  //To change body of implemented methods use File | Settings | File Templates.
    }//	@Override

    public int getKillsTotal() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getKillsByFire() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getKillsByStomp() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public int getKillsByShell() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean canShoot() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public byte[][] getCompleteObservation() {
		return everything;
	}

	// TODO: 1.6
//	@Override
	public boolean isMarioCarrying() {
		return mario_carrying;
	}

	// TODO: 1.6
//	@Override
	public boolean isMarioOnGround() {
		return mario_on_ground;
	}

	// TODO: 1.6
//	@Override
	public boolean mayMarioJump() {
		return mario_may_jump;
	}
}
