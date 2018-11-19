package ThinningBigData;

import java.io.IOException;
import java.util.ArrayList;

public class QueryRefinement {

    public static ArrayList<ArrayList<ArrayList<Double>>> testQueries = new ArrayList<ArrayList<ArrayList<Double>>>();
    public static ArrayList<ArrayList<Double>> formerTestQueries      = new ArrayList<ArrayList<Double>>();
    public static ArrayList<ArrayList<Double>> testQueries2           = new ArrayList<ArrayList<Double>>();
    public static ArrayList<ArrayList<Double>> clusterHeads           = new ArrayList<ArrayList<Double>>();
    public static int k                                               = DistanceMeasure.k;
    public static ArrayList<Double>           aCollection             = new ArrayList<Double>();
    public static ArrayList<Integer>          winnerOrRival           = new ArrayList<Integer>();


    public static void main(String[] args) throws IOException {

        // this is the first set of test queries, along with the clusterheads that have been formed during training
        DistanceMeasure.csvToArrayList("testQueriesv1k" + k + ".csv", formerTestQueries, 0);
        DistanceMeasure.csvToArrayList("CentroidsV" + 1+"k"+k+ ".csv", clusterHeads, 0);
        DistanceMeasure.winnerGreaterThanRival(formerTestQueries);

        // generate a new testing set, keep it as a file and convert it to an array list
        //  AnotherTestQueryToFile(2);

        // load the test and clusterHeads to the corresponding arraylists
        DistanceMeasure.csvToArrayList("test" + 2 + ".csv", testQueries2, 1);


        // measure the distances and error
        DistanceMeasure.distances = DistanceMeasure.addDistances(DistanceMeasure.distanceBetweenQueryAndCentroid(testQueries2, clusterHeads));

        // it then determines based on the distances the winner and rival representatives
        DistanceMeasure.winnerRepresentative = DistanceMeasure.theWinnerRepresentative(DistanceMeasure.distances);
        DistanceMeasure.rivalRepresentative = DistanceMeasure.theRivalRepresentative(DistanceMeasure.distances, DistanceMeasure.winnerRepresentative);

        // after the representatives have been selected, their predicted errors are calculated
        DistanceMeasure.winnerPredictionError = DistanceMeasure.winnerPredictionError(DistanceMeasure.winnerRepresentative, testQueries2, clusterHeads);
        DistanceMeasure.rivalPredictionError = DistanceMeasure.winnerPredictionError(DistanceMeasure.rivalRepresentative, testQueries2, clusterHeads);

        // the above information is blended for each query, with its existing contents
        // these details are then written to file
        // determine the cases where the winner error is greater than the rival error
        DistanceMeasure.convertQueries(testQueries2);
        DistanceMeasure.writeConvertedQueries(testQueries2, 2);
        winnerOrRival=DistanceMeasure.winnerGreaterThanRival(testQueries2);
        aCollection=determineA(testQueries2,clusterHeads);
    }


    // generate another test query
    public static void AnotherTestQueryToFile(int version) throws IOException {
        ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch1.dat");
        ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch2.dat");
        ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch3.dat");
        ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch4.dat");
        ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch5.dat");
        ReadInDataset.readDATFile("C:/Users/pigko/Downloads/driftdataset/batch6.dat");
        ReadInDataset.findMinAndMax();
        ReadInDataset.finalizeDataset();
        ReadInDataset.writeToFile2("FinalDataset" + ".arff", ReadInDataset.finalDataset); //write down the used dataset
        queryMatching.query = queryMatching.assignScoreToQueries();   //assign the scores to queries
        testQueries = queryMatching.takeTestingQuerySet(queryMatching.query, 1.0, 0.0);
        queryMatching.scoreToFile(Queries.NumberOfDimensions, testQueries, "TestScoreSet" + version + ".arff"); //write down the queries with the scores
        //   queryMatching. queryAndDataToCSV();
        queryMatching.writeQueriesToCSV(testQueries, "test" + version);
    }


    // This formula will determine the value of a, where a appears in
    // The reward formula will be w=w+a(q-w), where the penalty formula is w=w-a(q-w).
    // The value of a (can vary due to difference between query and its winner score) is determined as follows:
    // Dw = difference between the score of the query and the score of its winner representative.
    // Dr = difference between the score of the query and the score if its rival representative.
    // Function f for query q: f(q)=1-e^(-Dw/Dw+Dr)
    // value of a will be a=min(0.5, f(q))
    public static ArrayList<Double> determineA( ArrayList<ArrayList<Double>> testQuery, ArrayList<ArrayList<Double>> clusterHeads){
        ArrayList<Double> aCollection = new ArrayList<Double>();
        int queryScoreIndex           = testQuery.get(0).size()-5;                                               // get the index of the query score
        int queryWinnerIndex          = testQuery.get(0).size()-4;                                               // get the index of the winner representative
        int queryRivalIndex           = testQuery.get(0).size()-2;                                               // get the index of the rival  representative
        int clusterScore              = clusterHeads.get(0).size()-1;
        for (int i=0; i< testQuery.size(); i++){
            double testQueryScore            = testQuery.get(i).get(queryScoreIndex);
            int currentQueryWinnerIndex      = (int) (0.0+(testQuery.get(i).get(queryWinnerIndex)));
            double winningClusterScore       = clusterHeads.get(currentQueryWinnerIndex).get(clusterScore);
            int currentQueryRivalIndex       = (int) (0.0+(testQuery.get(i).get(queryRivalIndex)));
            double rivalClusterScore         = clusterHeads.get(currentQueryRivalIndex).get(clusterScore);
            double Dw                        = Math.abs(testQueryScore - winningClusterScore);
            double Dr                        = Math.abs(testQueryScore - rivalClusterScore);
            double f                         = 1 - (Math.exp(-Dw/(Dw+Dr)));
            double a                         = Math.min(0.5,f);
            aCollection.add(a);
    }
       return aCollection;
    }

    // this function is used to readjust the values of the cluster head attributes
    // reward  = it brings the clusterhead closer to the query if the winner prediction error is lower than the rival prediction error
    // penalty = it takes the clusterhead further from the query if the opposite occurs
    // reward  : w=w+a(w-q)
    // penalty : w=w-a(w-q)
    public static ArrayList<ArrayList<Double>> newClusterHeads(ArrayList<ArrayList<Double>> oldClusterHeads, ArrayList<Integer> winnerOrRival,
                                                               ArrayList<Double> aCollection, ArrayList<ArrayList<Double>> testQueries){
        ArrayList<ArrayList<Double>> newCentroids= DistanceMeasure.copyArrayToArray(oldClusterHeads);
        int queryWinnerIndex          = testQueries.get(0).size()-4;                                       // get the index of the winner representative
        for (int i=0; i<testQueries.size(); i++){                                                          // for all queries
            int currentQueryWinnerIndex      = (int) (0.0+(newCentroids.get(i).get(queryWinnerIndex)));    // get the winner index for this query
            ArrayList<Double> thisCluster    = newCentroids.get(currentQueryWinnerIndex);                  // get the cluster for this query
            double a                         = aCollection.get(i);                                         // get the a value for this query
            int    rewardOrPenalty           = winnerOrRival.get(i);                                       // 0 or 1 value to impose reward or penalty
            for(int x=0; x<newCentroids.size()-1; x++){                                                    // for each query attribute except Score, since queries have some extra attributes, winner representative, predictions errors etc..
                double w= thisCluster.get(x);                                                              // w is a query attributes

            }
        }
        return newCentroids;
    }
}
