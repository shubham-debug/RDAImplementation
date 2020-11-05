package RDAImplementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;





public class RDAHostSpecific {
	
	// This method will return the index of element in array arr[]
	public int index(int[] arr, int element) {
		int ans = 100;
		for(int i=0; i<arr.length; i++) {
			if(arr[i]==element) {
				ans = i;
				break;
			}
		}
		return ans;
	}
	
	// this method will choose that whether currHost is more preferred or not than the current matching
	public int choose(int vm, VM currVM, int sp, SP currSP) {
		int priorityOfcurrSP = this.index(currVM.priorityListOfSPs, sp);
		int priorityOfMatchedSP = this.index(currVM.priorityListOfSPs, currVM.SP);
		if(priorityOfcurrSP>priorityOfMatchedSP) {
			if(currVM.bestRejected>priorityOfcurrSP) {
				currVM.bestRejected = priorityOfcurrSP; 
			}
			return -1; 
		} 
		currVM.bestRejected = priorityOfMatchedSP;
		int SPThatIsRejected = currVM.SP;
		currVM.SP = sp;
		return SPThatIsRejected; 	
	}
	
	
//	public void removeAllVMThatHasLessPriorityThanBestRejected(SP currSP, int vm, ArrayList<ArrayList<Integer>> matching, VM[] arrayOfVMs, int sp) {
////		int priorityOfvm = this.index(currSP.priorityListOfVMs, vm); 
////		if(priorityOfvm<currSP.bestRejected) { 
////			currSP.bestRejected = priorityOfvm;
//			for(int i = 0; i<currSP.currentlyMatched.size(); i++) { 
//				if(this.index(currSP.priorityListOfVMs, currSP.currentlyMatched.get(i))>currSP.bestRejected) {  
//					arrayOfVMs[currSP.currentlyMatched.get(i)].currentlyMatched = false;  
//					arrayOfVMs[currSP.currentlyMatched.get(i)].SP = -1; 
//					currSP.capacity += arrayOfVMs[currSP.currentlyMatched.get(i)].requirement;
//					matching.get(sp).remove(new Integer(currSP.currentlyMatched.get(i))); 
//					currSP.currentlyMatched.remove(i);
//					i--;
//				}
//			}
//		}
////	}
	 
	// This method will send proposal to the VMs one by one and check that VM can be matched or not
	// If the VM is currently matched then we check that whether currHost is better choice for that VM or not
	public int[] engage(int sp, SP currSP, VM[] arrayOfVMs, ArrayList<ArrayList<Integer>> matching, int numSPs, int numVMs, SP[] arrayOfSPs) { 
		int[] SPsWhichLostPairedVMs = new int[numSPs]; 
		int count = 0; 
		Arrays.fill(SPsWhichLostPairedVMs, -1); 
		// this loop will continue until the currHost has capacity and currHost does not propose all the VMs in its preference list
		while(currSP.capacity>0 && currSP.pointer<numVMs) {
			int vm = currSP.priorityListOfVMs[currSP.pointer]; 
			// if the vm = -1, this means that currHost proposed all the VMs
			// if vm>currHost.bestRejected we break the loop because the intuition says that
			// as the host is proposing VMs most prefered VM first and then on and on.
			// so if vm>currHost.bestRejected simply means that from this vm to end of preference list all the other VMs has less priority that bestRejected
			// here vm>currHost.bestRejected is taken because we are considering index of vm in the preference list as priority and the vm which has smaller index
			// is most preferred
			if(vm == -1 || currSP.capacity<=0) {  
				break;  
			}
			currSP.pointer += 1; 
			VM currVM = arrayOfVMs[vm];
			if(!currVM.currentlyMatched) {
				if(currVM.requirement<=currSP.capacity) {
					currVM.currentlyMatched = true;
					currVM.SP = sp;
					currSP.capacity -= currVM.requirement;
					currSP.currentlyMatched.add(vm);
					matching.get(sp).add(vm);
				}
			} 
			else if(currVM.requirement<=currSP.capacity){
				// this choose method will return the index of host that is currently matched to the currVM 
				// if the priority of currHost is less then this method will return -1
				int SPThatLoseVM = this.choose(vm,currVM,sp,currSP);
				if(SPThatLoseVM != -1) {
					arrayOfSPs[SPThatLoseVM].capacity += currVM.requirement;  
					//System.out.println(hostThatLoseVM);
					matching.get(SPThatLoseVM).remove(new Integer(vm));
					// the task is to update the currentlyMatchedList of hostThatLoseVM 
					arrayOfSPs[SPThatLoseVM].currentlyMatched.remove(new Integer(vm));
					// now the task is to update the best rejected of hostThatLoseVM and remove all the VM that has less priority than that
					//this.removeAllVMThatHasLessPriorityThanBestRejected(arrayOfSPs[SPThatLoseVM],vm,matching,arrayOfVMs,SPThatLoseVM);
					boolean flg = true;
					for(int i = 0; i<SPsWhichLostPairedVMs.length; i++) {
						if(SPThatLoseVM == SPsWhichLostPairedVMs[i]) {
							flg = false;
						}
					}
					if(flg) {
						SPsWhichLostPairedVMs[count] = SPThatLoseVM;
						count += 1; 
					}
					currSP.capacity -= currVM.requirement; 
					currSP.currentlyMatched.add(vm); 
					matching.get(sp).add(vm); 
				}
				else {
					continue;
				}	
			}  	
		}
		return SPsWhichLostPairedVMs;  
	}
	
	
	// this method will pop the first element of the array and return that element 
	// and also shift all the element one index toward left
	public int popArray(int[] arr) {
		int item = arr[0];
		for(int i = 1; i<arr.length; i++) {
			arr[i-1] = arr[i]; 
		}
		arr[arr.length-1]=-1;
		return item;
			
	}	
	
	
	
	// this method will calculate the satisfaction factor of the VMs based on the formula for satisfaction factor
	public int[] satisfactionFactor(VM[] arrayOfVMs, int numSP) {
		int[] factor = new int[arrayOfVMs.length];
		//Arrays.fill(factor, -1);
		for(int i = 0; i<arrayOfVMs.length; i++) {
			if(arrayOfVMs[i].SP == -1) {
				continue;
			}
			int j = this.index(arrayOfVMs[i].priorityListOfSPs, arrayOfVMs[i].SP);
			float sfactor = ((numSP - j)*100)/numSP; 
			int sfact = (int)sfactor;
			factor[i] = sfact;
		}
		
		return factor;
	}
	
	// this method will calculate the satisfaction factor of the Hosts based on the formula for the satisfaction factor.
	public int[] satisfactionFactorHost(SP[] arrayOfSPs, int numVM, int numSP) {
		int[] factor = new int[numSP];
		for(int i = 0; i<numSP; i++) {
			int sfactor = 0;
			for(int j = 0; j<arrayOfSPs[i].currentlyMatched.size(); j++) {
				int k = this.index(arrayOfSPs[i].priorityListOfVMs, arrayOfSPs[i].currentlyMatched.get(j));
				float temp = ((numVM-k)*100)/numVM;
				sfactor += (int)temp;
			}
			if(arrayOfSPs[i].currentlyMatched.size() == 0) {
				continue;
			}
			sfactor = sfactor/arrayOfSPs[i].currentlyMatched.size();
			factor[i] = sfactor;
		} 
		return factor;
	}
	
	
	private int[] revenueSP(SP[] arrayOfSPs, VM[] arrayOfVMs) {
		int[] revenue = new int[arrayOfSPs.length];
		
		for(int i = 0; i<arrayOfSPs.length; i++) {
			int earnedMoney = 0;
			//System.out.println(arrayOfSPs[i].currentlyMatched);
			for(int j = 0; j<arrayOfSPs[i].currentlyMatched.size(); j++) {
				earnedMoney += arrayOfVMs[arrayOfSPs[i].currentlyMatched.get(j)].requirement * arrayOfSPs[i].cost;
			}
			revenue[i] = earnedMoney;
		}
		return revenue;
	}
	
	
	
	// This is the rDA method to perform matching (Host Specific) 
	public void rDA(int numVMs, int numSPs, VM[] arrayOfVMs, SP[] arrayOfSPs){ 
		// waitingQueue will schedule the host to go for matching
		// It represent the host as the index of that host in arrayOfHosts array
		int[] waitingQueue = new int[numSPs];
		Arrays.fill(waitingQueue, -1);
		// matching will contain the VM matched to a particular host.
		// if matching.get(0) = [1,2,5]
		//    this means that host at index 0 in arrayOfHosts in matched with VMs at index 1,2 & 5 in arrayOfVMs
		// if matching.get(0) = [] 
		//     this means that host at index 0 in arrayOfHosts does not matched with any VMs
		ArrayList<ArrayList<Integer>> matching = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i<numSPs; i++) {
			waitingQueue[i] = i;
			ArrayList<Integer> obj = new ArrayList<Integer>();
			matching.add(obj);
		}
		int capacity = numSPs; 
		while(capacity>0) {
			int sp = this.popArray(waitingQueue);
			//System.out.println(Arrays.toString(waitingQueue)); 
			capacity -= 1;
			if(sp == -1) {
				break;
			}
			// engage method will return an array of SPs which lost matched VMs
			int[] SPsWhichLostPairedVMs = this.engage(sp, arrayOfSPs[sp], arrayOfVMs, matching, numSPs, numVMs, arrayOfSPs); 
			if(SPsWhichLostPairedVMs[0] != -1) { 
				int counter = 0;
				for(int l = capacity; l<waitingQueue.length; l++) {
					if(SPsWhichLostPairedVMs[counter] == -1) { 
						break;
					}
					waitingQueue[l] = SPsWhichLostPairedVMs[counter];
					counter++;
				}
				capacity += counter;
			}	
		}
		for(int i = 0; i<matching.size(); i++) {
			System.out.println("Host at index "+i+" contains: "+matching.get(i));
		}
		//This section print the satisfaction factor of all the VM
		// if the VM is not placed then the satisfaction factor of that VM is 0
		System.out.println("This is satisfaction factor for VMs");
		int[] factor = this.satisfactionFactor(arrayOfVMs, numSPs); 
		System.out.println(Arrays.toString(factor)); 
		System.out.print("Average satisfaction factor for VMs: ");
		int avg = 0;
		for(int i = 0; i<factor.length; i++) {
			avg += factor[i];
		}
		avg = avg/factor.length;
		System.out.print(avg+"% \n");
		System.out.println("This is satisfaction factor for SPs");
		int[] fact = this.satisfactionFactorHost(arrayOfSPs, numVMs, numSPs);
		System.out.println(Arrays.toString(fact)); 
		System.out.print("Average satisfaction factor for SPs: ");
		avg = 0;
		for(int i = 0; i<fact.length; i++) {
			avg += fact[i];
		}
		avg = avg/fact.length;
		System.out.print(avg+"%\n");
		//System.out.println(Arrays.toString(fact));  
		System.out.println("This is revenue for SPs");
		int[] rev = this.revenueSP(arrayOfSPs, arrayOfVMs);
		System.out.println(Arrays.toString(rev));
		int totalRev = 0;
		System.out.print("Total revenue of SPs: ");
		for(int i = 0; i<rev.length; i++) { 
			totalRev += rev[i];
		}
		System.out.print(totalRev+"$\n");

		 
	}
	 
	
	
	private int pivotSP(int[] sortedSPs, SP[] arrayOfSPs, int start, int end) {
        int temp = sortedSPs[start];
        int turtle = start+1;
        for(int i = start+1; i < end; i++){
            if(arrayOfSPs[sortedSPs[i]].cost<arrayOfSPs[sortedSPs[start]].cost){
                int temp1 = sortedSPs[turtle];
                sortedSPs[turtle] = sortedSPs[i];
                sortedSPs[i] = temp1;
                turtle++;
            }
        }
        sortedSPs[start] = sortedSPs[turtle-1];
        sortedSPs[turtle-1] = temp;
        return turtle-1;
	}
	 
	
	private void quickSortSPMain(int[] sortedSPs, SP[] arrayOfSPs, int start, int end) {
		if(start <= end-1) {
			int pivot = this.pivotSP(sortedSPs, arrayOfSPs, start, end);
			this.quickSortSPMain(sortedSPs, arrayOfSPs, start, pivot);
			this.quickSortSPMain(sortedSPs, arrayOfSPs, pivot+1, end);
			
		}
	}
	
	
	private int[] quickSortSP(SP[] arrayOfSPs) {
		int[] sortedSPs = new int[arrayOfSPs.length];
		
		for(int i = 0; i<arrayOfSPs.length; i++) {
			sortedSPs[i] = i;
		}
		this.quickSortSPMain(sortedSPs, arrayOfSPs, 0, arrayOfSPs.length);
		return sortedSPs;
	}
	
	
	
	private int pivotVM(int[] sortedVMs, VM[] arrayOfVMs, int start, int end) {
        int temp = sortedVMs[start];
        int turtle = start+1;
        for(int i = start+1; i < end; i++){
            if(arrayOfVMs[sortedVMs[i]].requirement > arrayOfVMs[sortedVMs[start]].requirement){
                int temp1 = sortedVMs[turtle];
                sortedVMs[turtle] = sortedVMs[i];
                sortedVMs[i] = temp1;
                turtle++;
            }
        }
        sortedVMs[start] = sortedVMs[turtle-1];
        sortedVMs[turtle-1] = temp;
        return turtle-1;
	}
	 
	private void quickSortVMmain(int[] sortedVMs, VM[] arrayOfVMs, int start, int end) {
		if(start <= end-1) {
			int pivot = this.pivotVM(sortedVMs, arrayOfVMs, start, end);
			this.quickSortVMmain(sortedVMs, arrayOfVMs, start, pivot);
			this.quickSortVMmain(sortedVMs, arrayOfVMs, pivot+1, end);
			
		}
	}
	 
	
	private int[] quickSortVM(VM[] arrayOfVMs) {
		int[] sortedVMs = new int[arrayOfVMs.length];
		
		for(int i = 0; i<arrayOfVMs.length; i++) {
			sortedVMs[i] = i;
		}
		this.quickSortVMmain(sortedVMs, arrayOfVMs, 0, arrayOfVMs.length);
		return sortedVMs;
	}
	 
	public static void main(String[] args) throws IOException {
		
		//In this section we take the number of VM and Host 
		// and then we create that much VM and host and store them in arrayOfVMs and arrayOfHosts respectively
		// arrayOfHosts and arrayOfVMs contains the objects of Host and VM class.
		Scanner sc = new Scanner(System.in); 
		RDAHostSpecific obj = new RDAHostSpecific(); 
		int[] capacitiesOfSPs = {2000,1000,3000,4000};
		int[] priceOfSPs = {8,7,12,9}; 
		System.out.println("Enter the number of VMs");
		int numberOfVMs = sc.nextInt();
		System.out.println("Enter the number of SPs");
		int numberOfSPs = sc.nextInt();
		VM[] arrayOfVMs = new VM[numberOfVMs];
		for(int i = 0; i<numberOfVMs; i++) {
			arrayOfVMs[i] = new VM(numberOfSPs);
		}
		SP[] arrayOfSPs = new SP[numberOfSPs];
		for(int i = 0; i<numberOfSPs; i++) {
			arrayOfSPs[i] = new SP(numberOfVMs);
			arrayOfSPs[i].capacity = capacitiesOfSPs[i];
			arrayOfSPs[i].cost = priceOfSPs[i];
		}
		int[] sortedSPs = obj.quickSortSP(arrayOfSPs);
		int[] sortedVMs = obj.quickSortVM(arrayOfVMs); 
		System.out.println("Sorted");
		// In this section we create the object of GeneratePriorityList
		// and then generate the priority list and store that priority in Prioritylist.txt file 
		GeneratePriorityList gn = new GeneratePriorityList();
		gn.generatePriorityList(numberOfVMs, numberOfSPs, sortedSPs, sortedVMs, arrayOfSPs, arrayOfVMs);
		System.out.println("File Written");
		File file = new File("Prioritylist.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String st;
		int count = 0;
		while((st = br.readLine())!= null) {
			if(count<numberOfVMs) {
				 int i=0;
				 for(String temp:st.split(" ")) {
					 if(temp.length() == 0) {
						 break;
					 }
					 arrayOfVMs[count].priorityListOfSPs[i]=Integer.parseInt(temp);
					 i+=1;
				 }
			}
			else {
				int i =0;
				for(String temp:st.split(" ")) {
					if(temp.length() == 0) {
						 break;
					 }
					arrayOfSPs[count-numberOfVMs].priorityListOfVMs[i]=Integer.parseInt(temp);
					i+=1;
				}
			}
			count+=1;
		}
		obj.rDA(numberOfVMs, numberOfSPs, arrayOfVMs, arrayOfSPs);
		System.out.println("successfull");
		sc.close();
		br.close();
	}
}

