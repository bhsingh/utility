package org.biosemantics.misc.utility.kang;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biosemantics.conceptstore.domain.Concept;
import org.biosemantics.conceptstore.domain.Label;
import org.biosemantics.conceptstore.domain.impl.ConceptImpl;
import org.biosemantics.conceptstore.domain.impl.ConceptType;
import org.biosemantics.conceptstore.repository.ConceptRepository;
import org.biosemantics.conceptstore.repository.impl.ConceptRepositoryImpl;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class Experiment2 {

	private static final String DB_PATH = "/Users/bhsingh/code/neo4j-community-1.8/data/graph.db";
	private static final String PRED_DB_PATH = "/Users/bhsingh/Desktop/graph.db";
	private static final String TP_FILE = "/Users/bhsingh/code/data/kang-rlsp-mining/exp2/tp.csv";
	private static final String FP_FILE = "/Users/bhsingh/code/data/kang-rlsp-mining/exp2/fp.csv";
	private CSVWriter csvWriter;
	private File file = null;
	private GraphDatabaseService graphDb;
	private ConceptRepository conceptRepository;
	private Map<Long, Long> tpMap = new HashMap<Long, Long>();
	private Map<Long, Long> fpMap = new HashMap<Long, Long>();
	private StringBuilder nodePath = new StringBuilder();

	private GraphDatabaseService predicateGraphDb;

	public void init(String dbPath, String predDbPath, String tpFile, String fpFile) throws IOException {
		file = new File("/Users/bhsingh/Desktop/predicates.txt");
		csvWriter = new CSVWriter(new FileWriter(file));
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook(graphDb);
		this.conceptRepository = new ConceptRepositoryImpl(graphDb);
		//
		predicateGraphDb = new GraphDatabaseFactory().newEmbeddedDatabase(predDbPath);
		registerShutdownHook(predicateGraphDb);
		//
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

	public void createAllPredicates() throws IOException {
		Map<Long, Long> idMap = new HashMap<Long, Long>();
		int ctr = 0;
		Collection<Concept> concepts = conceptRepository.getByType(ConceptType.PREDICATE);
		Transaction tx = predicateGraphDb.beginTx();
		logger.info("{}", concepts.size());
		for (Concept concept : concepts) {
			StringBuilder text = new StringBuilder();
			for (Label label : concept.getLabels()) {
				text.append(label.getText()).append("|");
			}
			Node node = predicateGraphDb.createNode();
			idMap.put(concept.getId(), node.getId());
			node.setProperty("id", concept.getId());
			node.setProperty("label", text.toString());
			long tp = 0;
			if (tpMap.containsKey(concept.getId())) {
				tp = tpMap.get(concept.getId());
			}
			long fp = 0;
			if (fpMap.containsKey(concept.getId())) {
				fp = fpMap.get(concept.getId());
			}
			node.setProperty("tp", tp);
			node.setProperty("fp", fp);
			node.setProperty("aggregateTp", 0);
			node.setProperty("aggregateFp", 0);
			logger.info("{}", ++ctr);
		}
		logger.info("{}", idMap);
		logger.info("creating rlsps");
		ctr = 0;
		for (Concept concept : concepts) {
			ConceptImpl conceptImpl = (ConceptImpl) concept;
			Node node = conceptImpl.getNode();
			Long predicateNodeId = idMap.get(node.getId());
			Node predicateNode = predicateGraphDb.getNodeById(predicateNodeId);
			Iterable<Relationship> incomingRelationships = node.getRelationships(Direction.INCOMING,
					DynamicRelationshipType.withName("578"));
			for (Relationship relationship : incomingRelationships) {
				Node otherNode = relationship.getOtherNode(node);
				Long otherPredicateNodeId = idMap.get(otherNode.getId());
				Node otherPredicateNode = predicateGraphDb.getNodeById(otherPredicateNodeId);
				Iterable<Relationship> existingRlsps = otherPredicateNode.getRelationships(Direction.OUTGOING);
				boolean rlspExists = false;
				for (Relationship existingRlsp : existingRlsps) {
					if (existingRlsp.getOtherNode(otherPredicateNode).equals(predicateNode)) {
						rlspExists = true;
						break;
					}
				}
				if (!rlspExists) {
					otherPredicateNode.createRelationshipTo(predicateNode, DynamicRelationshipType.withName("IS_A"));
				}
			}
			Iterable<Relationship> outgoingRelationships = node.getRelationships(Direction.OUTGOING,
					DynamicRelationshipType.withName("578"));
			for (Relationship relationship : outgoingRelationships) {
				Node otherNode = relationship.getOtherNode(node);
				Long otherPredicateNodeId = idMap.get(otherNode.getId());
				Node otherPredicateNode = predicateGraphDb.getNodeById(otherPredicateNodeId);
				Iterable<Relationship> existingRlsps = predicateNode.getRelationships(Direction.OUTGOING);
				boolean rlspExists = false;
				for (Relationship existingRlsp : existingRlsps) {
					if (existingRlsp.getOtherNode(predicateNode).equals(otherPredicateNode)) {
						rlspExists = true;
						break;
					}
				}
				if (!rlspExists) {
					predicateNode.createRelationshipTo(otherPredicateNode, DynamicRelationshipType.withName("IS_A"));
				}
			}
			logger.info("{}", ++ctr);
		}
		Set<Node> noRlspNodes = new HashSet<Node>();
		Iterable<Node> nodes = predicateGraphDb.getAllNodes();
		for (Node node : nodes) {
			if (node.getId() == 0L) {
				continue;
			}
			// int rlspCtr = 0;
			// Iterable<Relationship> rlsps =
			// node.getRelationships(Direction.INCOMING);
			// for (Relationship relationship : rlsps) {
			// Node otherNode = relationship.getOtherNode(node);
			// csvWriter.writeNext(new String[] {
			// String.valueOf(node.getProperty("id")), "IS_A",
			// String.valueOf(otherNode.getProperty("id")),
			// String.valueOf(node.getProperty("label")),
			// String.valueOf(otherNode.getProperty("label")) });
			// rlspCtr++;
			// }
			// if (rlspCtr == 0) {
			// Iterable<Relationship> outRlsps =
			// node.getRelationships(Direction.OUTGOING);
			// for (Relationship relationship : rlsps) {
			// rlspCtr++;
			// }
			// }
			// if (rlspCtr == 0) {
			// noRlspNodes.add(node);
			// }
			Long id = (Long) node.getProperty("id");
			String label = (String) node.getProperty("label");
			Long tp = 0L;
			if (tpMap.containsKey(id)) {
				tp = tpMap.get(id);
			}
			Long fp = 0L;
			if (fpMap.containsKey(id)) {
				fp = fpMap.get(id);
			}
			csvWriter.writeNext(new String[] { String.valueOf(id), label, String.valueOf(tp), String.valueOf(fp) });
			
		}

		for (Node node : noRlspNodes) {
			logger.info("{},{}", new Object[] { node.getProperty("id"), node.getProperty("label") });
		}
		logger.info("{}", noRlspNodes.size());
		csvWriter.flush();
		csvWriter.close();
		tx.success();
		tx.finish();
		graphDb.shutdown();
		predicateGraphDb.shutdown();

	}

	public void getChildren(Node node) {
		Iterable<Relationship> relatinships = node.getRelationships(Direction.INCOMING);
		Set<Node> childNodes = new HashSet<Node>();
		for (Relationship relationship : relatinships) {
			childNodes.add(relationship.getStartNode());
		}
		nodePath.append(node.getProperty("id")).append("|").append(node.getProperty("label")).append("tp:")
				.append(node.getProperty("tp")).append("fp:").append(node.getProperty("fp")).append("\t");
		for (Node node2 : childNodes) {
			getChildren(node2);
		}
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
		Experiment2 experiment = new Experiment2();
		experiment.init(DB_PATH, PRED_DB_PATH, TP_FILE, FP_FILE);
		experiment.createAllPredicates();
	}

	private static final Logger logger = LoggerFactory.getLogger(Experiment2.class);

}
