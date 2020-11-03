package RDAImplementation;

import java.util.Arrays;
import java.util.ArrayList;

public class SP {
	int capacity;
	int bestRejected;
	ArrayList<Integer> currentlyMatched = new ArrayList<Integer>();
	int[] priorityListOfVMs;
	int pointer;
	int cost;
	SP(int numVM){
		priorityListOfVMs = new int[numVM];
		Arrays.fill(priorityListOfVMs, -1);
		capacity = (int)(1000*Math.random())+1;
		bestRejected = numVM;
		cost = (int)(10*Math.random())+1;
		pointer = 0;
	}
}
