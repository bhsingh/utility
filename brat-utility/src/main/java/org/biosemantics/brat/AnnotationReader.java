/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.biosemantics.brat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import au.com.bytecode.opencsv.CSVReader;

/**
 * 
 * @author bhsingh
 */
public class AnnotationReader {

	private static final String ENTITY_PREFIX = "T";
	private static final String RLSP_PREFIX = "R";
	private static final String NOTE_PREFIX = "#";

	Collection<BratAnnotation> readAll(File annotationFile) throws IOException {
		CSVReader csvReader = new CSVReader(new FileReader(annotationFile), '\t');
		Collection<BratAnnotation> bratAnnotations = new ArrayList<BratAnnotation>();
		List<String[]> lines = csvReader.readAll();
		try {
			for (String[] columns : lines) {
				if (columns[0].startsWith(ENTITY_PREFIX)) {
					String[] splits = columns[1].split("\\s+");
					String text = "";
					if (columns.length == 3) {
						text = columns[2];
					}
					String fileNameNoExten = FilenameUtils.removeExtension(annotationFile.getName());
					bratAnnotations.add(new BratEntityAnnotation(columns[0], splits[0], Integer.parseInt(splits[1]),
							Integer.parseInt(splits[2]), text, fileNameNoExten));
				}
			}
		} finally {
			csvReader.close();
		}
		return bratAnnotations;
	}
}
