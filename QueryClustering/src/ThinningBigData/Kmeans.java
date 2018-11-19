package ThinningBigData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

// implementation of the Kmeans algorithm
	 
	public class Kmeans {
		static ArrayList<ArrayList<Double>> Centroids= new  ArrayList<ArrayList<Double>>();
	 
		public static BufferedReader readDataFile(String filename) {
			BufferedReader inputReader = null;
	 
			try {
				inputReader = new BufferedReader(new FileReader(filename));
			} catch (FileNotFoundException ex) {
				System.err.println("File not found: " + filename);
			}
	 
			return inputReader;
		}
		
		// k reprsents the value of in k-means
		// dimensionNum includes the number of dimensions in the query (including the score)
		public static void executeKmeans(int k, int dimensionNum) throws Exception {
			SimpleKMeans kmeans = new SimpleKMeans();
			kmeans.setSeed(10);
	 
			//important parameter to set: preserver order, number of cluster.
			kmeans.setPreserveInstancesOrder(true);
			kmeans.setNumClusters(k);
	 
			BufferedReader datafile = readDataFile("C:/Users/pigko/OneDrive/Desktop/ThinningBigData/QueryClustering/TrainScoreSet.arff"); 
			Instances data = new Instances(datafile);
			kmeans.buildClusterer(data);
	 
			Instances text=kmeans.getClusterCentroids(); // Cluster centroids and their values
			ArrayList<Double> Centroid=new ArrayList<Double>();
			
			int a=0;
			int x=0;
			int parameter =dimensionNum;
			int numberOfClusters=kmeans.getNumClusters();
			
			//pass the centroid data into Centroids ArrayList
			while(a < numberOfClusters) {
			for (x=0; x<parameter;x++) {
				double[] cluster =text.attributeToDoubleArray(x);
				Centroid.add(cluster[a]);
			}		
			Centroids.add(Centroid);
			Centroid= new ArrayList<Double>();
			a++;
			}	
		}
		
		// sorts an array of centroids from maximum to minimum score
		// n is the number of dimensions that make up each centroid
		public static ArrayList<ArrayList<Double>> sortCentroidsOnScore( ArrayList<ArrayList<Double>> Centroids, int k, int n) throws IOException {
			n=n-1;                                                                            //  decrease n by 1, avoid errors
			ArrayList<ArrayList<Double>> sortedCentroids= new ArrayList<ArrayList<Double>>(); // the sorted array that will be returned
			sortedCentroids.add(Centroids.get(0));                                            // add an entry to sorted Centroids list
			double maxScore= Centroids.get(0).get(n);   									  // set a variable for keeping a max score	
			int    c       = 0;																  // an index for the sorted Centroid list	
			int z=0;
			
			ArrayList<Integer> checklist=new ArrayList<Integer>();							  // a list to check values from centroids are added only once to stored centroids
			while(Centroids.size()!= sortedCentroids.size()) {                                // do not terminate the loop if not all centroids have been sorted!
				for (int i=0; i< Centroids.size();i++) {     							      // loop through Centroids list
					if(Centroids.get(i).get(n)==0.0){
						Centroids.get(i).set(n, 0.0000001);                                  // error with score 0.0, assign a small value to avoid error: 0.0000001
					}
					if (Centroids.get(i).get(n)>maxScore && !checklist.contains(i)) {         // if an an entry has a higher max score
						maxScore=Centroids.get(i).get(n);                                     // replace valeu of max score
						if (sortedCentroids.size()<=c) {
							sortedCentroids.add(c, Centroids.get(i)); z=i;
						}else {
							sortedCentroids.set(c, Centroids.get(i)); z=i;}                      // place new value in centroids
					}
					} 
					checklist.add(z);
					maxScore=0.0; 
					c++; 	
			}
			// we have managed to sort the list
			// now let's write it down in a file
			int minn=1;
			int maxn=1;
			int parameter =n;
			double queryID=0.0;
			@SuppressWarnings("resource")
			BufferedWriter writer = new BufferedWriter(new FileWriter("KmeansSortedResults"+k+".arff"));
			//set the file relation title
			String title = "@relation Sorted_Query_Centroids";
			writer.write(title);
			writer.newLine();
			//set the attribute names
			for (int y=0; y<parameter; y++) {
				if (Queries.isEven(y) || y==0) {
					String attribute1= "@attribute"+" min"+minn + " numeric";
					minn++;
					writer.write(attribute1);
					writer.newLine();
				}
				else {
					String attribute1="@attribute"+" max" +maxn + " numeric";
					maxn++;
					writer.write(attribute1);
					writer.newLine();
				}}
			
			//set score attribute name
			String attribute1 ="@attribute"+" score" + " numeric";
			writer.write(attribute1);
			writer.newLine();
			writer.newLine();
			// centroid data
			writer.write("@DATA");
			writer.newLine();
			// write the sorted data dowwwwwn
	
			for (int i =0; i<sortedCentroids.size();i++) {
				String line=" ";
				for(n=0; n<sortedCentroids.get(i).size(); n++) {
					line+=sortedCentroids.get(i).get(n)+", ";
				}
				writer.write(line);
				writer.newLine();
				queryID=queryID+1.0;
			}
			writer.close();
			return sortedCentroids;
			
		}
		
		public static void main(String[] args) throws Exception {
			executeKmeans(15,5); 
			sortCentroidsOnScore(Centroids,15,4);
		}
	}

