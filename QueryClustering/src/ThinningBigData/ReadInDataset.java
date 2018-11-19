package ThinningBigData;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;


public class ReadInDataset {
	
	static ArrayList<ArrayList<String>> dataset     = new ArrayList<ArrayList<String>>();
	static ArrayList<ArrayList<String>> finalDataset= new ArrayList<ArrayList<String>>();
	static ArrayList<ArrayList<Double>> minAndMax   = new ArrayList<ArrayList<Double>>();
	static ArrayList<String>            Maxs        = new   ArrayList<String>();
	static ArrayList<String>            Mins        = new   ArrayList<String>();
	static int                          gasID       = 0;
	@SuppressWarnings("resource")
	public static void main (String args[]) throws IOException {
	}
	
	// read  dat file into an ArrayList
	public static ArrayList<ArrayList<String>> readDATFile(String filename) throws FileNotFoundException {
		File file = new File(filename);
		@SuppressWarnings("resource")
		Scanner reader= new Scanner(System.in);  // Reading from System.in
		@SuppressWarnings("resource")
		Scanner scnr = new Scanner(file);
		if(gasID==0) {
		System.out.println("Please enter the gas ID: ");
		gasID= reader.nextInt();}
		while(scnr.hasNextLine()){
		   String line = scnr.nextLine();
		   String[] line1 = line.split("\\s+");
		   ArrayList<String> data = new ArrayList<String>(); 
		   String[] nameAndConcetration= line1[0].split(";");
		   String Concetration= nameAndConcetration[1];
		   int gasName=Integer.parseInt(nameAndConcetration[0]);
		   if(gasID==gasName) {
		   data.add(nameAndConcetration[0]);
		   data.add(Concetration);
		   for (int z=1; z<=128;z++) { 
			   String[] line0=line1[z].split(":",-2); // split to keep only the integer
			   data.add(line0[1]);	   
		   }
		   dataset.add(data);}
		}	
		return dataset;
	}
	

	// write to file for an x dimensional query
		public static void writeToFile2(String file, ArrayList<ArrayList<String>> dataset1) throws IOException {
			int NumberOfDimensions = dataset.get(0).size();
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			String title = "@relation Dataset_Title";
			writer.write(title);
			writer.newLine();
			int FeatureNumber=0; // a counter for counting every 16 sensors
			int sensorNumber=0 ; // a counter for the id of each sensor

			for (int y=0; y<NumberOfDimensions-1; y++) {
				// pass the attributes names					
				if (y==0) {
					String attribute="@attribute"+" GasConcetration" + " numeric";
					writer.write(attribute);
					writer.newLine();
				}			
				// there are 16 sensors with 8 features each, create an attribute for each one
				else if (FeatureNumber<8) {
					String attribute="@attribute"+" Sensor"+sensorNumber+"Feature"+FeatureNumber + " numeric";
					writer.write(attribute);
					writer.newLine();
					FeatureNumber++;
				}				
				else {
					sensorNumber++;
					FeatureNumber=0;
					String attribute="@attribute"+" Sensor"+sensorNumber+"Feature"+FeatureNumber + " numeric";
					writer.write(attribute);
					writer.newLine();
					FeatureNumber++;
				}
			}
			writer.newLine();
			writer.write("@DATA");
			writer.newLine();
			
			// pass the data into the file
			ArrayList <String> data= new ArrayList<String>();
				for (int i=0; i<dataset1.size(); i++) {
					data=dataset1.get(i);
					String s="";
					for (int z=1 ; z<data.size(); z++) {
						s +=data.get(z)+" "; 						
				}
						writer.write(s);
						writer.newLine();
				}			
				writer.close();
			}
		
		// function that produces an Array of doubles representing the min and max for each dimension of the vector
		// output is in the format [[max1,min1],...,[max128,min128]]
		public static void findMinAndMax(){
			// first initialize values in Maxs and Mins arrays
			for (int a=0; a<dataset.get(0).size();a++) {
				Maxs.add(dataset.get(0).get(a));
				Mins.add(dataset.get(0).get(a));
			}
			for (int i=0; i<dataset.size(); i++) { // go through each dataset entry
				for(int z=0; z<dataset.get(i).size();z++) { // examine entry and update max and min if necessary
					double currentEntry= Double.parseDouble(dataset.get(i).get(z));
					double currentMax  = Double.parseDouble(Maxs.get(z));
					double currentMin  = Double.parseDouble(Mins.get(z));
					if(currentEntry>currentMax) { // compare max
						Maxs.set(z,dataset.get(i).get(z));	
					}
					if(currentEntry<currentMin) { //compare min
						Mins.set(z,dataset.get(i).get(z));
					}}}}
		
		// function that takes in the produced dataset and converts it to a double between 0 and 1
		// max(x)-x/max(x)-min(x)
		public static ArrayList<ArrayList<String>> finalizeDataset() {
		for (int i=0; i< dataset.size();i++){
			ArrayList<String> dataEntry=new ArrayList<String>();
			for (int a=0; a< dataset.get(i).size();a++) {
				float X      = Float.parseFloat(dataset.get(i).get(a));
				float maxX   = Float.parseFloat(Maxs.get(a));
				float minX   = Float.parseFloat(Mins.get(a));
				float newX   = (maxX-X)/(maxX-minX);
				String newSX = Float.toString(newX);				
				dataEntry.add(newSX);
			}
			finalDataset.add(dataEntry);
		}return finalDataset;
		}
	}


