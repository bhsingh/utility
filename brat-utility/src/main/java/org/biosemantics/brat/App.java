package org.biosemantics.brat;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import au.com.bytecode.opencsv.CSVWriter;

public class App {
	public static void main(String[] args) throws IOException {
		File folder = new File("/Users/bhsingh/Annotation-Gina");
		File[] annotationFiles = folder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".ann");
			}
		});
		AnnotationReader annotationReader = new AnnotationReader();
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File("/Users/bhsingh/code/data/EVIPED/annotation-all.txt")));
		for (File annotationFile : annotationFiles) {
			Collection<BratAnnotation> bratAnnotations = annotationReader.readAll(annotationFile);
			for (BratAnnotation bratAnnotation : bratAnnotations) {
				csvWriter.writeNext(bratAnnotation.toStringArray());
			}
		}
		csvWriter.flush();
		csvWriter.close();
	}
}
