package ThinningBigData;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;

// this class will write the first set of cluster heads in a file
// it also updates the query files by adding the winner and rival representatives for each query along
// with the prediction errors for each case

public class DistanceMeasure {

		public static ArrayList<ArrayList<Double>> SortedCentroids      = new ArrayList<ArrayList<Double>>() ;
		public static ArrayList<ArrayList<Double>> testQueries          = new ArrayList<ArrayList<Double>>() ;
		public static ArrayList<ArrayList<Double>> trainQueries         = new ArrayList<ArrayList<Double>>() ;
		public static ArrayList<ArrayList<Double>> distances            = new ArrayList<ArrayList<Double>>() ;
		public static ArrayList<ArrayList<Double>> rivalDistances       = new ArrayList<ArrayList<Double>>() ;
		public static ArrayList<Integer>           rivalRepresentative  = new ArrayList<Integer>() ;
		public static int                          winnerIndex          = 0                  ;
		public static ArrayList<Integer>           winnerRepresentative = new ArrayList<Integer>() ;
		public static ArrayList<Double>            winnerPredictionError= new ArrayList<Double>() ;
		public static ArrayList<Double>            rivalPredictionError = new ArrayList<Double>() ;
		public static int                          k                    = 5                  ; // set value of k for k-means
		public static int                          n                    = 5                  ; // set n, the number of dimensions

		public static void main(String args[]) throws Exception {

		            // this code is for the first part of our solution
                    // Frirst it generates the traininf and testing set
			        queryMatching.DatasetAndQueryToFiles(0.6,0.4);
			        csvToArrayList("test.csv",testQueries,1);
			        csvToArrayList("train.csv",trainQueries,1);
			        // it then performs kmeans using our training set to form the clusterheads
					Kmeans.executeKmeans(k,n);    
					SortedCentroids=Kmeans.sortCentroidsOnScore(Kmeans.Centroids,k, n);
					// it then determines the distance from each clusterhead for each query
					distances=addDistances(distanceBetweenQueryAndCentroid(testQueries,SortedCentroids));
					// it then determines based on the distances the winner and rival representatives
				    winnerRepresentative=theWinnerRepresentative(distances);
					rivalRepresentative=theRivalRepresentative(distances,winnerRepresentative);
					// after the representatives have been selected, their predicted errors are calculated
					winnerPredictionError=winnerPredictionError(winnerRepresentative, testQueries, SortedCentroids);
					rivalPredictionError=winnerPredictionError(rivalRepresentative, testQueries, SortedCentroids);
					// the above information is blended for each query, with its existing contents
                    // these details are then written to file
					convertQueries(testQueries);
					writeConvertedQueries(testQueries,1);
					writeCentroids(SortedCentroids,1);
		}

		
		// a small function for reading a csv file into an arraylists of arraylists of doubles
		// lastElement is used to remove ";" from the generated test and train queries, when a newly generated
		// ste of queries is created lastElement is 1, after the distances are added lastElement becomes 0
		public static ArrayList<ArrayList<Double>> csvToArrayList(String filename, ArrayList<ArrayList<Double>> queryList, int lastElement ){
			 String csvFile = "C:/Users/pigko/Desktop/ResearchProject/"+filename;
		     BufferedReader br = null;
		     String line = "";
		     String cvsSplitBy = ",";
		        try {
		            br = new BufferedReader(new FileReader(csvFile));
		            int i=0;
		            while ((line = br.readLine()) != null) {
		                // use comma as separator
		                String[] attribute = line.split(cvsSplitBy);
		                ArrayList<Double> attributeValue= new ArrayList<Double>();
		                for (int x=0; x<attribute.length-lastElement;x++) {
		                	attributeValue.add(Double.parseDouble(attribute[x]));
		                }
		                queryList.add(attributeValue);
		                i++;
		            }
		        } catch (FileNotFoundException e) {
		            e.printStackTrace();
		        } catch (IOException e) {
		            e.printStackTrace();
		        } finally {
		            if (br != null) {
		                try {
		                    br.close();
		                } catch (IOException e) {
		                    e.printStackTrace();
		                }}}
			return queryList;
		}
		
		// this function takes a query and calculates the distances between itself and each centroid
		// it will build an array list of arraylist that calculate the difference between the query's attributes
		// and the centroid attributes
		public static ArrayList<ArrayList<ArrayList<Double>>> distanceBetweenQueryAndCentroid(ArrayList<ArrayList<Double>> testQueries,ArrayList<ArrayList<Double>> Centroids ) {	
			ArrayList<ArrayList<ArrayList<Double>>> distances= new ArrayList<ArrayList<ArrayList<Double>>>(); // list of lists of distances between all queries and all centroids 
			ArrayList<Double> distance1 = new ArrayList<Double>();                                   // distances between a single query and single centroid
			ArrayList<ArrayList<Double>> distance2 = new ArrayList<ArrayList<Double>>();            // list of distances between a single query and all centroids
				for (int q=0; q<testQueries.size();q++){	                                        // for every query				
						for (int c=0; c<Centroids.size(); c++) {									// for every centroid							
							for(int a=0; a<testQueries.get(q).size()-1;a++) {                   	// for every query attribute 
								double queryAttribute=testQueries.get(q).get(a);                    // get the query attribute x value
								double centroidAttribute=Centroids.get(c).get(a);				    // get the centroid attribute x value
								double difference= Math.abs(queryAttribute-centroidAttribute);      // calculate the distance between them
								distance1.add(difference);											// add the distance to the difference vector
					}	
						distance2.add(distance1);  													// add all the difference vectors for a query in the distances ArrayList
						
						distance1 = new ArrayList<Double>();
					}
						distances.add(distance2);	
						distance2=	new ArrayList<ArrayList<Double>>();	
				}
				
				return distances;
			}	
		
		// takes the output of the above functions and adds up the distances in each array list
		// this will result in one single unit of distance that will make the process of comparison easier afterwards
		public static ArrayList<ArrayList<Double>> addDistances(ArrayList<ArrayList<ArrayList<Double>>> distances1){
			ArrayList<ArrayList<Double>> distances2= new ArrayList<ArrayList<Double>>();
			double difference1=0;
			for (int i=0; i<distances1.size();i++) {
				ArrayList<Double> difference2 = new ArrayList<Double>();
				for (int z=0; z<distances1.get(i).size();z++) {
					for (int y=0;y<distances1.get(i).get(z).size();y++) {
						difference1+=distances1.get(i).get(z).get(y);
					}
				 difference2.add(difference1);
				 difference1=0;
				}distances2.add(difference2);
			}
			
			return distances2;
		}
		
	// a function that copies an array to a new array, in a way such they will be referred to
	// as different objects
	public static ArrayList<ArrayList<Double>> copyArrayToArray(ArrayList<ArrayList<Double>> array2 ){
		ArrayList<ArrayList<Double>> array1=new ArrayList<ArrayList<Double>>();
		for (int i=0; i<array2.size();i++) {
			ArrayList<Double> list  = array2.get(i);
			ArrayList<Double> list2 = new ArrayList<Double>(); 
			for (int x=0;x<list.size();x++) {
				double element=0.0+list.get(x);
				list2.add(element);
			}
			array1.add(list2);	
			}
		return array1;
	}
				
	// this function uses the above function to find for each query the centroid with minimum distance, it returns an arrayList of integers
	// each entry corresponds to a query, the integer displays the number of the centroid that is closest to the given query
	// [0,2,5] would mean there are three queries, first query foes to centroid 0, second query goes to centroid 2 and so on	
	public static ArrayList<Integer> theWinnerRepresentative(ArrayList<ArrayList<Double>> Distances) {
			ArrayList<Integer> winnerRepresentatives= new ArrayList<Integer>();
			rivalDistances= copyArrayToArray(Distances);
			winnerIndex                             = 0;
			for (int i=0; i<Distances.size();i++) {
				double min=9*10^10;
				for(int z=0;z<Distances.get(i).size();z++) {
					if(Distances.get(i).get(z)<=min) {
						min=Distances.get(i).get(z);
						winnerIndex=z;
					}
				}
				rivalDistances.get(i).remove(winnerIndex);                             // remove the winner index, so the rival can be located
				winnerRepresentatives.add(winnerIndex);
			}
			return winnerRepresentatives;
		}		
	
	// this function behaves as the function above, although it takes as input the winnerRepresentative list
	// it will use it to ignore the centroid that has been previously selected, hence selecting the next closest
	// centroid, the rival Representative
	public static ArrayList<Integer> theRivalRepresentative(ArrayList<ArrayList<Double>> Distances, ArrayList<Integer> winnerRepresentatives) {
		ArrayList<Integer> rivalRepresentatives= new ArrayList<Integer>();
		rivalDistances= copyArrayToArray(Distances);
		winnerIndex                             = 0;
		for (int i=0; i<Distances.size();i++) {
			double min=9*10^10;
			for(int z=0;z<Distances.get(i).size();z++) {
				if(z!=winnerRepresentatives.get(i)) {
				if(Distances.get(i).get(z)<=min) {
					min=Distances.get(i).get(z);
					winnerIndex=z;
				}}
			}
			rivalDistances.get(i).remove(winnerIndex);                             // remove the winner index, so the rival can be located
			rivalRepresentatives.add(winnerIndex);
		}
		return rivalRepresentatives;
	}	
	
	
	// this function takes  the winnerRepresentatives as inputs and uses it to find the corresponding centroid
	// for each cluster, it then calculates the difference between their scores to calculate the winnerPrediction error
	public static ArrayList<Double> winnerPredictionError(ArrayList<Integer> winnerRepresentatives, ArrayList<ArrayList<Double>> testQueries,ArrayList<ArrayList<Double>> SortedCentroids){
		ArrayList<Double> winnersPredictionErrors = new ArrayList<Double>();
		for (int i=0; i<testQueries.size();i++) {
			int    desiredCentroidIndex = winnerRepresentatives.get(i);
			int    desiredIndex         = testQueries.get(i).size()-1; 
			double queryScore           = testQueries.get(i).get(desiredIndex);
			double centroidScore        = SortedCentroids.get(desiredCentroidIndex).get(desiredIndex);
			double winnerPredictionError= Math.abs((queryScore-centroidScore));
			winnersPredictionErrors.add(winnerPredictionError);
		}	
		return winnersPredictionErrors;
	}

	
	// this is a function that adds to the test queries the winner representative along with its error
	// and the rival representative along with the error, therefore the output is as such:
	// attribute1 attribute2.. attributex score winnerRepresentative winnerError rivalRepresentative rivalError
	public static void convertQueries(ArrayList<ArrayList<Double>> oldQuery){
		DecimalFormat df = new DecimalFormat("#.######");
		df.setRoundingMode(RoundingMode.CEILING);
		for(int x=0; x<oldQuery.size();x++) {
			double wr= 0.0+ winnerRepresentative.get(x);
			double rr= 0.0+rivalRepresentative.get(x);
			oldQuery.get(x).add(wr);
			double winError= winnerPredictionError.get(x);
			String convertWinError= df.format(winError);
			winError=Double.parseDouble(convertWinError);
			oldQuery.get(x).add(winError);
			oldQuery.get(x).add( rr);
			double rivError= rivalPredictionError.get(x);
			String convertRivError= df.format(rivError);
			rivError=Double.parseDouble(convertRivError);
			oldQuery.get(x).add(rivError);
		}}
	
	
	public static void writeConvertedQueries(ArrayList<ArrayList<Double>> newQuery, int version) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("testQueriesv"+version+"k"+k+".csv"));
		for (int i=0; i<newQuery.size();i++) {
			//	System.out.println(i);
			String toWrite="";
			toWrite= ""+newQuery.get(i);
			toWrite=toWrite.replace("[", "");
			toWrite=toWrite.replace("]", "");
			writer.write(toWrite);
			writer.newLine();
		}
		writer.close();
	}

	public static void writeCentroids(ArrayList<ArrayList<Double>> newQuery, int version) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter("Centroidsv"+version+"k"+k+".csv"));
		for (int i=0; i<newQuery.size();i++) {
			//	System.out.println(i);
			String toWrite="";
			toWrite= ""+newQuery.get(i);
			toWrite=toWrite.replace("[", "");
			toWrite=toWrite.replace("]", "");
			writer.write(toWrite);
			writer.newLine();
		}
		writer.close();
	}

	// this function returns an integer array list, that contains either a 0 or a 1 for each index
	// 0 means rival > winner and 1 rival < winner
	public static ArrayList<Integer> winnerGreaterThanRival(ArrayList<ArrayList<Double>> testQueries){
	ArrayList<Integer> refinementQueries= new ArrayList<Integer>();
	int querySize=testQueries.get(0).size();
	// the format of a query is as such: min, max, min2, max2, Score,winnerID, winnerScore,rivalID, rivalScore
	// the variables below are the indexes for the winner and rival representative score, for a query of x dimensions
	int rivalPredictionErrorIndex  = querySize-1;
	int winnerPredictionErrorIndex = rivalPredictionErrorIndex-2;
	for (int i=0; i<testQueries.size();i++){
		double rivalPredictionError   = testQueries.get(i).get(rivalPredictionErrorIndex);
		double winnerPredictionError = testQueries.get(i).get(winnerPredictionErrorIndex);
		if(rivalPredictionError<=winnerPredictionError){
			refinementQueries.add(1); }
		else{
			refinementQueries.add(0); } }
			int queries=0                              ;
		for (int i=0; i<refinementQueries.size();i++ ){
			if( refinementQueries.get(i)==1){
				queries++;
			}
		}
		System.out.println(queries +" Queries where the Winner Prediction Error is higher than the Rival Prediction Error.");
		return refinementQueries;
	}

		
}
