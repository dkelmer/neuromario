package competition.icegic.peterlawford.simulator;

public class TheoreticComparable {
	byte nType;
	public float x;
	public float y;
	
	public float getX() { return x; }
	public float getY() { return y; }
	
	public TheoreticComparable(byte nType, float x, float y) {
		this.nType = nType;
		this.x =x;
		this.y = y;
		
	}
}
