package org.biosemantics.common.utility.saber;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class DiffUtility {

	public static void main(String[] args) throws IOException {
		writeMap(checkFrequency("/media/ssd/bhsingh/results2/saber-simple.txt"),"/media/ssd/bhsingh/results2/saber-simple-freq.txt");
		writeMap(checkFrequency("/media/ssd/bhsingh/results2/saber-umls.txt"),"/media/ssd/bhsingh/results2/saber-umls-freq.txt");
		writeMap(checkFrequency("/media/ssd/bhsingh/results2/saber-sbd.txt"),"/media/ssd/bhsingh/results2/saber-sbd-freq.txt");
	}

	public static Map<Integer, Integer> checkFrequency(String file) throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(file));
		List<String[]> umlsLines = csvReader.readAll();
		Map<Integer, Integer> conceptIdMap = new HashMap<Integer, Integer>();
		ValueComparator bvc = new ValueComparator(conceptIdMap);
		TreeMap<Integer, Integer> sorted_map = new TreeMap<Integer, Integer>(bvc);
		for (String[] columns : umlsLines) {
			Integer conceptId = Integer.valueOf(columns[5]);
			int frequency = 0;
			if (conceptIdMap.containsKey(conceptId)) {
				frequency = conceptIdMap.get(conceptId);
			}
			frequency++;
			conceptIdMap.put(conceptId, frequency);
			termsMap.put(conceptId, columns[4]);
		}
		csvReader.close();
		sorted_map.putAll(conceptIdMap);
		return sorted_map;
	}

	public static void writeMap(Map<Integer, Integer> map, String outFile) throws IOException {
		CSVWriter csvWriter = new CSVWriter(new FileWriter(outFile));
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			csvWriter
					.writeNext(new String[] { "" + entry.getKey(), termsMap.get(entry.getKey()), "" + entry.getValue() });

		}

		csvWriter.flush();
		csvWriter.close();
	}

	private static Map<Integer, String> termsMap = new HashMap<Integer, String>();

}

class ValueComparator implements Comparator<Integer> {
	Map<Integer, Integer> base;

	public ValueComparator(Map<Integer, Integer> base) {
		this.base = base;
	}

	// Note: this comparator imposes orderings that are inconsistent with
	// equals.
	public int compare(Integer a, Integer b) {
		if (base.get(a) <= base.get(b)) {
			return -1;
		} else {
			return 1;
		} // returning 0 would merge keys
	}
}
