package RDAImplementation;

import java.util.*;
import java.io.*;

public class RandomisedRDA {
	
	// This method will return the index of element in array arr[]
	private int index(int[] arr, int element) {
		int ans = 100;
		for(int i=0; i<arr.length; i++) {
			if(arr[i]==element) {
				ans = i;
				break;
			}
		}
		return ans;
	}
	
	
	//this method returns the index of VM in arrayOfVMs that is rejected by the host
	//this is the utility method for engage method
	public int[] match(int vm, int[] matching, VM[] arrayOfVMs, SP sp) {
		
		VM currVM = arrayOfVMs[vm];
		int priorityOfCurrVM = this.index(sp.priorityListOfVMs, vm);
		// breject is used to update the Host.bestRejected attribute
		int breject = arrayOfVMs.length; 
		//temp will store all the VM that has priority less than currVM
		int[] temp = new int[arrayOfVMs.length];
		Arrays.fill(temp, -1);
		int count = 0;  
		//This loop will add all the VMs that has less priority than currVM in temp[] array
		for(int i = 0; i<sp.currentlyMatched.size(); i++) {
			int prorityOfVM = this.index(sp.priorityListOfVMs, sp.currentlyMatched.get(i));
			// Here I took greater than because we take index of VM as their priority and the VM whose index is low has high priority
			if(prorityOfVM > priorityOfCurrVM) {
				temp[count]=sp.currentlyMatched.get(i);
				count += 1;
			}	
		} 
		
		int requiredCapacity = 0;
		count = 0;
		// here cpu is the need of currVM and same for memory and I take count<100 as this is the maximum count value possible
		// This loop will check that adding the cpu and memory requirement of all the VMs that has less priority than the current VM
		// and count is used to mark that upto what index of temp[] array the requirement is fulfilled
		while(requiredCapacity < currVM.requirement && count<arrayOfVMs.length) {
			if(temp[count]==-1) {
				//this condition means that we just go through all the vm that has less priority than currVM
				break;
			}
			int priorityOfVm = this.index(sp.priorityListOfVMs, temp[count]);
			//This condition is to check that the Host.bestRejected is greater than the rejected VMs due to currentVM
			if(priorityOfVm < breject) {
				breject = priorityOfVm;
			}
			requiredCapacity += arrayOfVMs[temp[count]].requirement;
			count += 1;
		}
		
		// flag1 is to check that whether or not currentVM can be placed or not in the Host
		boolean flag1 = false;
		// if after rejecting some VMs we can acquire the currentVM then we enter this condition
		if(requiredCapacity >= currVM.requirement) {
			// here we set attributes of host and currentVM
			if(breject< sp.bestRejected) {
				sp.bestRejected = breject;
			}
			flag1 = true;
			sp.capacity += requiredCapacity - currVM.requirement;
			for(int i = 0; i<sp.currentlyMatched.size(); i++) {
				boolean flag = true;
				for(int j = 0; j<count; j++) {
					if(temp[j]==sp.currentlyMatched.get(i)) {
						flag = false;
						break;
					} 
				}
				if(!flag) {
					sp.currentlyMatched.remove(i);
					i--;
				}
			} 
			sp.currentlyMatched.add(vm);
			 
		}
		int[] ans = new int[arrayOfVMs.length]; 
		Arrays.fill(ans, -1);
		// This condition means that some VM is rejected and we have to add those VMs in the ans[] array and return this array.
		if(flag1) {
			for(int i =0; i<arrayOfVMs.length; i++) {
				if(i<count) {
					ans[i]=temp[i];
				}
				else {
					ans[i]=-1;
				}
			}	
	  }
	  return ans;
	} 
	
	//This method is to free the State of VM that currently Says that it is matched
	private void freeVM(int[] vmThatIsRejectedByTheCurrentSP, VM[] arrayOfVMs, int[] matching) {
		for(int k=0; k<vmThatIsRejectedByTheCurrentSP.length; k++) {
			if(vmThatIsRejectedByTheCurrentSP[k]==-1) {
				break;
			}
			else {
				arrayOfVMs[vmThatIsRejectedByTheCurrentSP[k]].SP=-1;
				arrayOfVMs[vmThatIsRejectedByTheCurrentSP[k]].currentlyMatched = false;
				matching[vmThatIsRejectedByTheCurrentSP[k]]=-1;
			}
		}
	}
	
	
	// This method will take the object of Host
	// and then reject all the VM that has priority less the Host.bestRejected
	// this method will return the array of VMs that is rejected by the host.
	private int[] reject(SP sp, VM[] arrayOfVMs) {
		int[] ans = new int[sp.currentlyMatched.size()];
		Arrays.fill(ans, -1);
		int counter = 0;
		for(int i = 0; i < sp.currentlyMatched.size(); i++) {
			int temp = this.index(sp.priorityListOfVMs, sp.currentlyMatched.get(i));
			if(temp > sp.bestRejected) {
				ans[counter] = sp.currentlyMatched.get(i);
				sp.capacity += arrayOfVMs[ans[counter]].requirement;
				//sp.currentlyMatched.remove(i);
				counter++;
			}
		}
		for(int i = 0; i<counter; i++) {
			sp.currentlyMatched.remove(new Integer(ans[i]));
		}
		return ans;
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
	
	
	// This is the utility method for the rDA 
	// Its function is that it will place the VM in the appropriate host and return the array of VM that are rejected by the Host after placing the current VM.
	public int[] engage(int vm, int[] matching, VM[] arrayOfVMs, SP[] arrayOfSPs) {
		// vmThatIsRejectedByTheCurrentHost array will contain all the VMs that is rejected by the host on placing the currentVM and also due to best rejected attribute of host
		int vmThatIsRejectedByTheCurrentHost[] = new int[arrayOfVMs.length];
		// vmThatIsRejectedBecauseOfBestRejected array contain all the VMs that is rejected because of bestRejected attribute of host
		// This array is in the end will be merged with vmThatIsRejectedByTheCurrentHost and returned
		int[] vmThatIsRejectedBecauseOfBestRejected = new int[arrayOfVMs.length];
		// tempArray is used to store rejectedVMs temporary and then merged with vmThatIsRejectedBecauseOfBestRejected
		int[] tempArray = new int[arrayOfVMs.length]; 
		int counter = 0;
		//initially all the array is filled with -1 to marks these arrays empty
		Arrays.fill(vmThatIsRejectedByTheCurrentHost, -1);
		Arrays.fill(vmThatIsRejectedBecauseOfBestRejected, -1);
		Arrays.fill(tempArray, -1);
		VM currVM = arrayOfVMs[vm];
		currVM.count += 1; 
		//This loop will iterate over the priorityListOfHosts of currentVM and find the appropriate host for placement
		while(true) {
			if(arrayOfVMs[vm].h.size() >= arrayOfSPs.length) {
				break;
			}
			int j;
			// In this section we choose a random host to propose.
			while(true) {
				
				j = (int)(arrayOfSPs.length*Math.random());
				if(arrayOfVMs[vm].h.contains(j)) {
					continue;
				}
				else {
					arrayOfVMs[vm].h.add(j);
					break;
				} 
				
			}
			// i is the index of host in arrayOfHosts[]
			int i = currVM.priorityListOfSPs[j];
			// This condition checks that whether or not we came to the end of priorityListOfHosts as it is possible that a VM priority list may or may not contain all the host
			// but as the priorityListOfHosts length is fixed for all the VMs so -1 specifies no host or empty position
			if(currVM.count > arrayOfSPs.length) {
				break;
			}
			currVM.count += 1;
			if(i == -1) {
				continue; 
			}
			// This condition means that the currentHost has required cpu capacity and memory capacity to acquire currentVM
			// if this condition satisfies the currentVM is placed in the host and no other VMs are rejected so we break the loop set all the appropriate attributes
			// and then returns an empty vmThatIsRejectedByTheCurrentHost
			if(arrayOfSPs[i].capacity >= currVM.requirement) {
				arrayOfSPs[i].capacity -= currVM.requirement;
				arrayOfSPs[i].currentlyMatched.add(vm);
				currVM.currentlyMatched = true;
				currVM.SP = i;
				currVM.pointer = j+1;
				matching[vm] = i;
				break;
			}  
			// This condition means that the host bestRejected is higher than the priorityOfVM so we just continue the iteration 
			// and propose the next host in the priority list with next iteration
			else if(arrayOfSPs[i].bestRejected < this.index(arrayOfSPs[i].priorityListOfVMs, vm)){
				continue;
			}
			// This condition means that the host does not have capacity to acquire the currentVM
			// so  in this condition we call the match(currentVMIndex, matching, arrayOfVMs, hostToPropose)
			// which the check that whether or not currentVM can be placed or not in the host by removing some VMs with less priority
			else {
				continue;
//				vmThatIsRejectedByTheCurrentHost = this.match(vm, matching, arrayOfVMs, arrayOfSPs[i]);
//				// In this condition we check the array vmThatIsRejectedByTheCurrentHost index 0 
//				// if that index does not equal to -1 it means that some VMs are rejected and the match method will return array with rejected VMs
//				// only when the currentVM is placed otherwise not.
//				if(vmThatIsRejectedByTheCurrentHost[0]!=-1) {
//					// freeVM method will free the VMs that is rejected by setting the attributes VM.currentlyMatched = false and VM.host = -1
//					this.freeVM(vmThatIsRejectedByTheCurrentHost, arrayOfVMs, matching);
//					currVM.pointer = j+1;
//					currVM.currentlyMatched = true;
//					matching[vm]=i;
//					// we call the reject method to get all the VMs that has priority less than Host.bestRejected
//					// we use break statement because the current VM is placed
//					tempArray = this.reject(arrayOfSPs[i],arrayOfVMs);
//					break;
//				}
//				// In this condition the currentVM is not placed in the proposed host so we update the Host.bestRejected and then call the reject method 
//				// which then reject all the VMs that has less priority than Host.bestRejected
//				else{
//						int tempBestRejected = this.index(arrayOfSPs[i].priorityListOfVMs, vm);
//						if(arrayOfSPs[i].bestRejected > tempBestRejected) {
//						arrayOfSPs[i].bestRejected = tempBestRejected;
//						tempArray = this.reject(arrayOfSPs[i],arrayOfVMs);
//					}
//				}
//				// freeVM method will free the VMs that is rejected and stored in tempArray
//				this.freeVM(tempArray, arrayOfVMs, matching);
//				// Here we just add all the rejected VMs in the tempArray and store those in vmThatIsRejectedBecauseOfBestRejected
//				for(int l = 0; l < tempArray.length; l++) { 
//					if(tempArray[l] == -1) {
//						break;
//					}
//					else {
//						vmThatIsRejectedBecauseOfBestRejected[counter] = tempArray[l];
//						counter++;
//					}
//						
//				}
			}				 
		}
			
		int count = 0;
		// Here we count that upto which index the vmThatIsRejectedByTheCurrentHost which is filled by the VMs that is removed to place the currentVM in the Host
		for(int l = 0; l < vmThatIsRejectedByTheCurrentHost.length; l++) {
			if(vmThatIsRejectedByTheCurrentHost[l] == -1) {
				break;
			}
			else {
				count += 1;
			}
		}
		// Here we add all the VMs that is rejected because of bestRejected  in the vmThatIsRejectedByTheCurrentHost
		for(int l = 0; l<vmThatIsRejectedBecauseOfBestRejected.length; l++) {
			if(vmThatIsRejectedBecauseOfBestRejected[l] == -1) {
				break;
			}
			else {
				vmThatIsRejectedByTheCurrentHost[count] = vmThatIsRejectedBecauseOfBestRejected[l];
				count++;
			}
		}
		// All the VMs in the vmThatIsRejectedByTheCurrentHost is then return to the rDA method which will then added to the waitingQueue.
		return vmThatIsRejectedByTheCurrentHost;
	}
	
	
	
	// this method will pop the first element of the array and return that element 
	// and also shift all the element one index toward left
	private int popArray(int[] arr) {
		int item = arr[0];
		
		for(int i = 1; i<arr.length; i++) {
			arr[i-1] = arr[i]; 
		}
		arr[arr.length-1]=-1;
		
		return item;
		
	} 
	
	
	// this method will calculate the satisfaction factor of the VMs based on the formula for satisfaction factor
	public int[] satisfactionFactor(VM[] arrayOfVMs, int[] matching, int numSP) {
		int[] factor = new int[matching.length];
		//Arrays.fill(factor, -1);
		for(int i = 0; i<matching.length; i++) {
			if(matching[i]==-1) {
				continue;
			}
			int j = this.index(arrayOfVMs[i].priorityListOfSPs, matching[i]);
			float sfactor = ((numSP - j)*100)/numSP; 
			int sfact = (int)sfactor;
			factor[i] = sfact;
		}
		
		return factor;
	}
	
	// this method will calculate the satisfaction factor of the Hosts based on the formula for the satisfaction factor.
	public int[] satisfactionFactorHost(SP[] arrayOfSPs, int[] matching, int numVM, int numSP) {
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
	
	
	private void rDA(int numVM, int numSPs, VM[] arrayOfVMs, SP[] arrayOfSPs) {
		// the waitingQueue initially contains all the VMs and it sends the VM for allocation in FIFO manner
		// waitingQueue store the result as follows:
		// if waitingQueue[2] = 4 (This means that VM at index 4 in arrayOfVMs is waiting for allocation
		// if waitingQueue[2] = -1 (This means that no VM is waiting at that index and after that index)
		int[] waitingQueue = new int[numVM];
		int[] matching = new int[numVM];
		for(int i = 0; i<numVM; i++) {
			waitingQueue[i] = i;
			matching[i] = -1;
		}
				
		// capacity tells us the number of VMs in waitingQueue
		int capacity = numVM;
		while(capacity > 0) { 
			//System.out.println("This is waitingQueue "+Arrays.toString(waitingQueue));
			// The popArray method will remove the element at index 0 and shift all the other elements one position left and fill the last index of array as -1 
			// to show that, this position is empty. and return the element at index 0 that is removed
			int vm = this.popArray(waitingQueue);
			capacity -= 1;
			//this if condition is only to check that the popArray method does not pop the empty waitingQueue 
			if(vm == -1) {
				break;
			}
			//The engage method will find the appropriate matching for the vm and returns the array of VMs that are rejected by placing the current VM in the host
			int[] tempVM = this.engage(vm, matching, arrayOfVMs, arrayOfSPs);
			// if the tempVM[0]!=-1, it means that by placing the current VM in the host some VMs are rejected
			// we then just add those VMs in the waitingQueue and also increase the capacity of the waitingQueue
			// if the tempVM[0] = -1 then no VM is deallocated by allocating the current VM
			if(tempVM[0]!=-1) {
				int counter = 0;
				for(int i = capacity; i<numVM; i++) {
					if(tempVM[counter]==-1) {
						break;
					}
					waitingQueue[i] = tempVM[counter];
					counter += 1;
				}
				capacity += counter;
			}		
		}      
		System.out.println("This is matching");
		for(int i = 0; i<numVM; i++) {
			System.out.print(matching[i]+" ");
		}
		System.out.println();
		//This section print the satisfaction factor of all the VM
		// if the VM is not placed then the satisfaction factor of that VM is 0
		System.out.println("This is satisfaction factor for VMs");
		int[] factor = this.satisfactionFactor(arrayOfVMs, matching, numSPs); 
		System.out.println(Arrays.toString(factor)); 
		System.out.print("Average satisfaction factor for VMs: ");
		int avg = 0;
		for(int i = 0; i<factor.length; i++) {
			avg += factor[i];
		}
		avg = avg/factor.length;
		System.out.print(avg+"% \n");
		System.out.println("This is satisfaction factor for SPs");
		int[] fact = this.satisfactionFactorHost(arrayOfSPs, matching, numVM, numSPs);
		System.out.println(Arrays.toString(fact)); 
		System.out.print("Average satisfaction factor for SPs: ");
		avg = 0;
		for(int i = 0; i<fact.length; i++) {
			avg += fact[i];
		}
		avg = avg/fact.length;
		System.out.print(avg+"%\n");
		System.out.println(Arrays.toString(fact));  
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
		RandomisedRDA obj = new RandomisedRDA();
		int[] capacitiesOfSPs = {3000,2000,2500,2500};
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
					 arrayOfVMs[count].priorityListOfSPs[i]=Integer.parseInt(temp);
					 i+=1;
				 }
			}
			else { 
				int i =0;
				for(String temp:st.split(" ")) {
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

 
 