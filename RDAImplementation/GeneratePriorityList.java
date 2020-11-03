package RDAImplementation;

import java.io.FileWriter;
import java.io.IOException;

// this class will generate priority list of VMs and hosts and store them in a txt file
public class GeneratePriorityList {
	
	
	public void generatePriorityList(int numberOfVMs, int numberOfSPs, int[] sortedSPs, int[] sortedVMs, SP[] arrayOfSPs, VM[] arrayOfVMs) throws IOException{
		// s will contain all the priority of all the VMs and Hosts
		String s = "";
		FileWriter fw = new FileWriter("Prioritylist.txt");
		for(int i=0; i<numberOfVMs; i++) {
			int counter = 0;
			while(counter < numberOfSPs) {
				if(arrayOfVMs[i].requirement <= arrayOfSPs[sortedSPs[counter]].capacity) {
					int indexOfSP = sortedSPs[counter];
					s += Integer.toString(indexOfSP); 
					s += " ";
				}
				counter += 1;
			}
			s += "\n";
		} 
		  
		for(int i=0; i<numberOfSPs; i++) {
			int counter = 0;
			while(counter < numberOfVMs) {
				if(arrayOfSPs[i].capacity >= arrayOfVMs[sortedVMs[counter]].requirement) {
					int indexOfVM = sortedVMs[counter];
					s += Integer.toString(indexOfVM);
					s += " ";
				}
				counter += 1;
			}
			s += "\n";
		} 
		
		for(int i =0; i<s.length(); i++) {
			fw.write(s.charAt(i));
			
		}
		//System.out.println("written successfully");
		fw.close();
	}
	
	
 
}
