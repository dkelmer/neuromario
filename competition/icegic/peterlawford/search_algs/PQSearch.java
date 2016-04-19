package competition.icegic.peterlawford.search_algs;

import java.util.concurrent.PriorityBlockingQueue;

public class PQSearch extends PriorityBlockingQueue<Option> {

	double nWorstScore = -Double.MAX_VALUE;
	
	public boolean add(Option o) {
		if (o.getFScore() > nWorstScore) nWorstScore = o.getFScore(); 
		return super.add(o);
	}
	public void clear() {
		nWorstScore = Double.MAX_VALUE;
		super.clear();
	}
	public double getWorstScore() { return nWorstScore; }
}
