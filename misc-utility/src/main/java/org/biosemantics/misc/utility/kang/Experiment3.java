package org.biosemantics.misc.utility.kang;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biosemantics.conceptstore.domain.Concept;
import org.biosemantics.conceptstore.domain.Label;
import org.biosemantics.conceptstore.domain.impl.ConceptType;
import org.biosemantics.conceptstore.repository.ConceptRepository;
import org.biosemantics.conceptstore.repository.impl.ConceptRepositoryImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class Experiment3 {

	private static final String DB_PATH = "/Users/bhsingh/code/neo4j-community-1.8/data/graph.db";
	private static final String TP_FILE = "/Users/bhsingh/code/data/kang-rlsp-mining/exp2/tp400.csv";
	private static final String FP_FILE = "/Users/bhsingh/code/data/kang-rlsp-mining/exp2/fp400.csv";
	private CSVWriter csvWriter;
	private File file = null;
	private GraphDatabaseService graphDb;
	private ConceptRepository conceptRepository;
	private Map<Long, Long> tpMap = new HashMap<Long, Long>();
	private Map<Long, Long> fpMap = new HashMap<Long, Long>();

	public void init(String dbPath, String tpFile, String fpFile) throws IOException {
		file = new File("/Users/bhsingh/Desktop/predicates.csv");
		csvWriter = new CSVWriter(new FileWriter(file));
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook(graphDb);
		this.conceptRepository = new ConceptRepositoryImpl(graphDb);
		CSVReader tpReader = new CSVReader(new FileReader(tpFile));
		List<String[]> tpLines = tpReader.readAll();
		for (String[] columns : tpLines) {
			tpMap.put(Long.valueOf(columns[0].trim()), Long.valueOf(columns[1].trim()));
		}
		tpReader.close();

		CSVReader fpreader = new CSVReader(new FileReader(fpFile), ',');
		List<String[]> fpLines = fpreader.readAll();
		for (String[] columns : fpLines) {
			fpMap.put(Long.valueOf(columns[0].trim()), Long.valueOf(columns[1].trim()));

		}
		fpreader.close();
	}

	public void writeOut() throws IOException {
		Collection<Concept> predicates = conceptRepository.getByType(ConceptType.PREDICATE);
		for (Concept predicate : predicates) {
			String labelTxt = null;
			for (Label label : predicate.getLabels()) {
				labelTxt = label.getText();
				break;
			}
			if(predicate.getId() == 1110L || predicate.getId() ==1104L){
				System.out.println("here");
			}
			Collection<Long> children = conceptRepository.getAllChildPredicates(predicate.getId());
			Long aggregatedTp = 0L;
			Long myTp = tpMap.get(predicate.getId());
			if (myTp != null) {
				aggregatedTp += myTp;
			} else {
				myTp = 0L;
			}
			Long aggregatedFp = 0L;
			Long myFp = fpMap.get(predicate.getId());
			if (myFp != null) {
				aggregatedFp += myFp;
			} else {
				myFp = 0L;
			}
			for (Long childId : children) {
				Long tp = tpMap.get(childId);
				if (tp != null) {
					aggregatedTp += tp;
				}
				Long fp = fpMap.get(childId);
				if (fp != null) {
					aggregatedFp += fp;
				}
			}
			csvWriter.writeNext(new String[] { "" + predicate.getId(), labelTxt, "" + children.size(), "" + myTp,
					"" + aggregatedTp, "" + myFp, "" + aggregatedFp });
		}
		csvWriter.flush();
		csvWriter.close();
	}

	public void destroy() {
		graphDb.shutdown();
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	public static void main(String[] args) throws IOException {
		Experiment3 experiment = new Experiment3();
		experiment.init(DB_PATH, TP_FILE, FP_FILE);
		experiment.writeOut();
		experiment.destroy();
	}
}
