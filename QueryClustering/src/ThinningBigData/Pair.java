package ThinningBigData;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Pair {

	// A pair should be in the format (min, max)
	public static void main (String args[]) {}
	
	public static ArrayList<Double> createPair(int min, int max){
		ArrayList<Double> pair= new ArrayList<Double>();
		DecimalFormat df = new DecimalFormat("#.######");
		df.setRoundingMode(RoundingMode.CEILING);
		double a = ThreadLocalRandom.current().nextDouble(0, 1-0.01);
		String aConvert=df.format(a);
		a=Double.parseDouble(aConvert);
		double b = ThreadLocalRandom.current().nextDouble(a , 1);
		String bConvert= df.format(b);
		b=Double.parseDouble(bConvert);
		pair.add(a);
		pair.add(b);
		return pair;	
	}
}