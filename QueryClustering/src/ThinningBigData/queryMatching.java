package ThinningBigData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class queryMatching {
	
	static ArrayList<ArrayList<ArrayList<Double>>> query= new ArrayList<ArrayList<ArrayList<Double>>>();
	static ArrayList<ArrayList<ArrayList<Double>>> trainQueries= new ArrayList<ArrayList<ArrayList<Double>>>();
	static ArrayList<ArrayList<ArrayList<Double>>> testQueries= new ArrayList<ArrayList<ArrayList<Double>>>();
	static int x                                        =	0;
	static int FileMarker                               =   0;  // this integer controls how many queries are written in the test and training files
	// it is used in the queryToFile and scoreToFile methods
	public static void main(String args[]) throws IOException {
		
	}

	// a function that performs query matching on the dataset
	// the function returns the queries as they were but with a final integer
	// indicating a Score
	// the Score = Cardinality
	public static ArrayList<ArrayList<ArrayList<Double>>> assignScoreToQueries() {
		
		ArrayList<ArrayList<ArrayList<Double>>> allQueries = Queries.createQueries();
		ArrayList<ArrayList<String>>            dataset    = ReadInDataset.finalDataset;
		int                                     Counter    = 0;
		Double                                  Score      = 0.0;
		DecimalFormat                           df         = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.CEILING);          // round up to 4 decimal places
		
		
		// initially assign to each query a score of 0
		
		for (int i = 0; i < allQueries.size(); i++) {
			ArrayList<Double> zero = new ArrayList<Double>();
			zero.add(0.0);
			allQueries.get(i).add(zero);}
		
		// go through each query and check how many entries of the dataset it matches
		// with each match increase the score
		
		for (int i = 0; i < allQueries.size(); i++) { // for each query
			for(int b=0;  b < dataset.size(); b++) {    // for each dataset
				Counter = 0; 
				for    (int a=0;  a < allQueries.get(i).size()-1; a++) { // for each query clause
				//check if the query criteria match the dataset and increase the Score accordingly
																		  //this counter ensures that all query requirements are met in order to increase the score
					if (a != allQueries.get(i).size() - 1) {						 // ensure that Score entry is not involved in Query Matching
						
						// take min and max from query along with an entry from the dataset, convert to a double of 4 decimal places
						double minPoint1  = allQueries.get(i).get(a).get(0);
						String minPoint2  = df.format(minPoint1);
						double minPoint   = Double.parseDouble(minPoint2);
						double maxPoint1  = allQueries.get(i).get(a).get(1);
						String maxPoint2  = df.format(maxPoint1);
						double maxPoint   = Double.parseDouble(maxPoint2);
						double dataPoint1 = Double.parseDouble(dataset.get(b).get(a+1));
						String dataPoint2 = df.format(dataPoint1);
						double dataPoint  = Double.parseDouble(dataPoint2);
							
						if (   dataPoint<= maxPoint && dataPoint >= minPoint) { Counter++; 
					//	System.out.println("min:" + minPoint+" max: "+maxPoint+" data: "+dataPoint+ " of Query: " + b+ "| Query Match!");
						}
						else {//System.out.println(minPoint+" "+maxPoint+" "+dataPoint+ " of " + b);
						}
					}
					
				if (Counter==(Queries.getDimensions())/2) {  // if counter equals the dimensions of the query then increase score
					Score     =  allQueries.get(i).get(allQueries.get(i).size()-1).get(0);
					allQueries.get(i).get(allQueries.get(i).size()-1).set(0, Score+1.00); 
					}}
				 
				}
		//	System.out.println("Score = " + allQueries.get(i).get(allQueries.get(i).size()-1).get(0));
		}	
		return allQueries;
	}
	
	// write to file for an x dimensional query
	// allQueries is the queries that will be written in the file
	// filename is what the name of the file will be
	// NumOfWrites is how many queries the file will contain
	// starting index indicates from which query should the method write to file
	// this is useful when separating the queries into training and test sets

		public static void scoreToFile(int x, ArrayList<ArrayList<ArrayList<Double>>> allQueries, String filename) throws IOException {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
			int minn=1;
			int maxn=1;
			String title = "@relation Query_Title";
			writer.write(title);
			writer.newLine();
			for (int i=0; i<Queries.NumberOfDimensions; i++) {
				if (Queries.isEven(i) || i==0) {
					String attribute= "@attribute"+" min"+minn + " numeric";
					writer.write(attribute);
					writer.newLine();
					minn++;
				}
				else {
					String attribute="@attribute"+" max" +maxn + " numeric";
					writer.write(attribute);
					writer.newLine();
					maxn++;
				}
	
			}
			String attribute ="@attribute"+" score" + " numeric";
			writer.write(attribute);
			writer.newLine();
			
			writer.newLine();
			writer.write("@DATA");
			writer.newLine();
			for (int i=0; i<allQueries.size(); i++) {
				String str="";
				int n=0;
				for(n=0; n<allQueries.get(i).size()-1; n++) {	str += allQueries.get(i).get(n).get(0)+", " + allQueries.get(i).get(n).get(1)+", ";
				}
				if(allQueries.get(i).get(n).get(0)==0) {
					str+="0.0";
				}else {
					str += allQueries.get(i).get(n).get(0); // add the score
				}
				writer.write(str);
				writer.newLine();
			}
			writer.close();
		}
	
	// produce a csv file where each row:
	// has two dimensions from the dataset to represent a point in graph
	public static void queryAndDataToCSV() throws IOException {
		@SuppressWarnings("resource")
		BufferedWriter writer = new BufferedWriter(new FileWriter("Graph"+x+".csv"));
		ArrayList<ArrayList<String>> dataset =ReadInDataset.finalDataset;
		for (int i=0; i< dataset.size(); i++) {
			String entry= dataset.get(i).get(1)+","+ dataset.get(i).get(2);
			if (i< Queries.NumberOfQueries) {
				for (int x=0; x<(Queries.NumberOfDimensions/2)-1;x++) {
				entry = entry+","+query.get(i).get(x).get(0) +"," +query.get(i).get(x).get(1);
				entry = entry+","+query.get(i).get(x+1).get(0) +"," +query.get(i).get(x+1).get(1);
				
			}}
			writer.write(entry);
			writer.newLine();
		}
		writer.close();
	}


	
	// this function takes a query and write it in a CSV file
	public static void writeQueriesToCSV(ArrayList<ArrayList<ArrayList<Double>>> query, String name) throws IOException {
		@SuppressWarnings("resource")
		BufferedWriter writer = new BufferedWriter(new FileWriter(name+".csv"));
		for (int i=0; i<query.size();i++){
			int a=0;
			String entry="";
			for(a=0; a<query.get(i).size()-1;a++) {
				entry+=query.get(i).get(a).get(0) +","+ query.get(i).get(a).get(1)+"," ;	
			}
			entry+= query.get(i).get(a).get(0)+"," +";";
			writer.write(entry);
			writer.newLine();

		} 			writer.flush();
					writer.close();}
	
	// writes a given dataset and queries with scores to a file
	// t1 and t2 are doubles representing percentages that represent how the
	// queries are divided into training and testing sets, t1 for training, t2 for testing
	public static void DatasetAndQueryToFiles(double d, double e) throws IOException {
		ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch1.dat");
		ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch2.dat");
		ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch3.dat");
		ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch4.dat");
		ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch5.dat");
		ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch6.dat");
		ReadInDataset.findMinAndMax();
		ReadInDataset.finalizeDataset();
		ReadInDataset.writeToFile2("FinalDataset"+".arff",ReadInDataset.finalDataset); //write down the used dataset
		query=assignScoreToQueries();   //assign the scores to queries
		trainQueries=takeTrainingQuerySet(query,d);
		testQueries=takeTestingQuerySet(query,e,d);
		scoreToFile(Queries.NumberOfDimensions, trainQueries, "TrainScoreSet.arff"); //write down the queries with the scores
		scoreToFile(Queries.NumberOfDimensions, testQueries, "TestScoreSet.arff"); //write down the queries with the scores
	//	queryAndDataToCSV();                                                               // this produces a graph with both queries and the dataset, not needded right now
		writeQueriesToCSV(testQueries, "test"); 
		writeQueriesToCSV(trainQueries, "train"); 
	}
	
	
	
	public static ArrayList<ArrayList<ArrayList<Double>>> takeTrainingQuerySet(ArrayList<ArrayList<ArrayList<Double>>> query, double z){
		int trainSetSize= (int) (query.size() * z); // size of training set will be the z total number of queries
		ArrayList<ArrayList<ArrayList<Double>>> trainSet= new ArrayList<ArrayList<ArrayList<Double>>>();
		for (int i=0; i<trainSetSize; i++) {
			trainSet.add(query.get(i));
		}
		return trainSet;
	}
	
	public static ArrayList<ArrayList<ArrayList<Double>>> takeTestingQuerySet(ArrayList<ArrayList<ArrayList<Double>>> query, double z, double n){
		ArrayList<ArrayList<ArrayList<Double>>> testSet= new ArrayList<ArrayList<ArrayList<Double>>>();
		for (int i=(int) (query.size()*n); i<query.size(); i++) {                       //index should start from point based on n, which should be the z value from the                                                                      // takeTrainingQuerySet function
			testSet.add(query.get(i));
		}
		return testSet;
	}
	
	
}
