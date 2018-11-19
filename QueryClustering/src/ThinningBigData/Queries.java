package ThinningBigData;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Queries {
	
	static int NumberOfQueries=0;
	static int NumberOfDimensions=0;
	//ArrayList<Pair> Queries=new ArrayList<Pair>();
	public static void main(String args[]) throws IOException {
		ArrayList<ArrayList<ArrayList<Double>>> allQueries=createQueries();	
		writeToFile(NumberOfDimensions, allQueries);

		}
	public static Boolean isEven (Integer i) {
        return (i % 2) == 0;
    }	
	
	// this is a function for taking the min and max parameters for input in the createPair function
	// the function will take as input the dimension number
	// eg. for 4 dimensions it will ask for 4 min values and 4 max values 
	public static ArrayList<Integer> takeMinAndMax(int dimensionNum){
		ArrayList<Integer> parameters=new ArrayList<Integer>();
		int n=0;
		int x=0;
		for (int i=0; i<dimensionNum; i++) {
			// in case the number is Even it means it belongs to a max value
			// therefore the parameter should be less than the min value
			if (!isEven(i) && i!=0) {
				n = 0; // Scans the next token of the input as an int.
				}		
			if (isEven(i) || i==0) { // this is the min value, no checks needed
				n = 1;			 
			}
				if (!isEven(i)) {
					x=x+1;
				}
				parameters.add(n);
			}	
		return parameters;	
}
	

	public static ArrayList<ArrayList<Double>> createQuery(int dimensionNum, ArrayList<Integer> parameterVector){
		ArrayList<ArrayList<Double>> minMax = new ArrayList<ArrayList<Double>>(); // the min max pair
		for (int i=0; i < dimensionNum ; i++) {
			minMax.add(Pair.createPair(parameterVector.get(i),parameterVector.get(i+1)));
			i=i+1;
		}	return minMax;}
	
	// create a given number x-dimensional queries, x is defined through user input
	public static ArrayList<ArrayList<ArrayList<Double>>> createQueries(){
	ArrayList<ArrayList<ArrayList<Double>>> allQueries= new ArrayList<ArrayList<ArrayList<Double>>>();
	int dimensionNum=0;
	int numOfQueries=0;
	Scanner reader= new Scanner(System.in);  // Reading from System.in
	System.out.println("Please enter the number of dimensions ( remember 2 dimensions in query correspond to 1 in dataset):");
	dimensionNum = reader.nextInt();
	while (!isEven(dimensionNum)) {
		System.out.println("Please enter an even number of dimensions:");
		dimensionNum = reader.nextInt();
	}
	NumberOfDimensions = dimensionNum;
	System.out.println("Please enter the total number of queries:");
	numOfQueries = reader.nextInt();
	NumberOfQueries =  numOfQueries;
	ArrayList<Integer> parameterVector=takeMinAndMax(dimensionNum); // store the vector of parameters
	for (int i=0; i<numOfQueries; i++) {
		ArrayList<ArrayList<Double>> query=createQuery(dimensionNum, parameterVector);
		allQueries.add(query);
	}
	reader.close();
	return allQueries;
	}
		
	// write to file for an x dimensional query
	public static void writeToFile(int x, ArrayList<ArrayList<ArrayList<Double>>> allQueries) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("Query.arff"));
		int minn=1;
		int maxn=1;
		String title = "@relation Query_Title";
		writer.write(title);
		writer.newLine();
		for (int i=0; i<NumberOfDimensions; i++) {
			if (isEven(i) || i==0) {
				String attribute= "@attribute"+" min"+minn + " numeric";
				minn++;
				writer.write(attribute);
				writer.newLine();
			}
			else {
				String attribute="@attribute"+" max" +maxn + " numeric";
				maxn++;
				writer.write(attribute);
				writer.newLine();
			}
		}
		writer.newLine();
		writer.write("@DATA");
		writer.newLine();		
		for (int i=0; i<allQueries.size(); i++) {
			String str="";
			int n=0;
			for(n=0; n<x/2; n++) {
				str += allQueries.get(i).get(n).get(0)+", " + allQueries.get(i).get(n).get(1)+", ";
			}
			str += allQueries.get(i).get(n).get(0); // add the score
			writer.write(str);
			writer.newLine();
		}
		writer.close();
	}

	public static int getDimensions() {
		return NumberOfDimensions;
	}
	
}
