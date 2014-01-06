package org.biosemantics.misc.utility.herman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestComandLine {

	public static void main(String[] args) throws IOException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("python /Users/bhsingh/Desktop/scholar.py -c 10 --txt --author einstein quantum");
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		// read the output from the command
		String s;
		System.out.println("Here is the standard output of the command:\n");
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}

		// read any errors from the attempted command
		System.out.println("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
			System.out.println(s);
		}
	}

}
