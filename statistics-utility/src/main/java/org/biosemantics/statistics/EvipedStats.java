package org.biosemantics.statistics;

import java.text.DecimalFormat;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvipedStats {

	private static double[] a = new double[] { 0.34d, 0.43d, 0.46d, 0.71d, 0.39d, 0.73d, 0.51d, 0.68d, 0.61d, 0.41d };
	private static double[] b = new double[] { 0.08d, 0.16d, 0.21d, 0.23d, 0.11d, 0.33d, 0.02d, 0.18d, 0.18d, 0.08d };

	public static void main(String[] args) {
		mattWhitney();
	}

	public static void mattWhitney() {
		MannWhitneyUTest mwutest = new MannWhitneyUTest();
		
		double d = mwutest.mannWhitneyU(a, b);
		d = (double)Math.round(d * 100000) / 100000;
		System.out.println(d);
		logger.info("{}", d);
	}

	private static final Logger logger = LoggerFactory.getLogger(EvipedStats.class);

}
