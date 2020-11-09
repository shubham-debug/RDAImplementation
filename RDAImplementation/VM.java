package RDAImplementation;

import java.util.Arrays;
import java.util.HashSet;

public class VM {
	// these are the instance variables of VM or we can simply call it as VM requirement from the hosts
	
	int requirement;
	HashSet<Integer> h = new HashSet<Integer>();
	int count;
	int priorityListOfSPs[];
	int SP;
	int pointer;
	boolean currentlyMatched;
	// best rejected is here to tell us that we should not accept any SPs whose priority is less than bestRejected
	int bestRejected;
	
	//constructor to initialize VMs
	VM(int numSP){
		priorityListOfSPs = new int[numSP];
		Arrays.fill(priorityListOfSPs, -1);
		requirement = (int)(1*Math.random())+10;
		currentlyMatched = false;
		SP = -1;
		pointer = 0;
		count = 0;
		bestRejected = 999999999;
	}

}

   