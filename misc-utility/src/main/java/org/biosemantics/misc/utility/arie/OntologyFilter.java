package org.biosemantics.misc.utility.arie;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class OntologyFilter {

	private static final String SPLIT = "--";

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("/home/bhsingh/Public/UMLS2010ABHomologeneJochemToxV1_6.ontology"));
		File outfile = new File("/home/bhsingh/Public/gene.ontology");
		outfile.createNewFile();
		StringBuilder out = new StringBuilder();
		int ctr = 0;
		for (String line : lines) {
			out.append(line);
			out.append("\n");
			if (line.equals(SPLIT)) {
				String outString = out.toString();
				String[] fragments = outString.split("\n");
				boolean vo = outString.contains("VO GENE");
				if (fragments.length < 5 || vo) {
					FileUtils.writeStringToFile(outfile, outString, "UTF-8", true);
					ctr++;
				}
				out.setLength(0);
			}

		}
		System.out.println(ctr);
	}
}
