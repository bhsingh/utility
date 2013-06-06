package org.biosemantics.misc.utility.kang;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.biosemantics.conceptstore.domain.Concept;
import org.biosemantics.conceptstore.domain.Notation;
import org.biosemantics.conceptstore.domain.impl.ConceptType;
import org.biosemantics.conceptstore.domain.impl.NotationSourceConstant;
import org.biosemantics.conceptstore.repository.ConceptRepository;
import org.biosemantics.conceptstore.repository.LabelRepository;
import org.biosemantics.conceptstore.repository.NotationRepository;
import org.biosemantics.conceptstore.repository.TraversalRepository;
import org.biosemantics.conceptstore.repository.impl.ConceptRepositoryImpl;
import org.biosemantics.conceptstore.repository.impl.LabelRepositoryImpl;
import org.biosemantics.conceptstore.repository.impl.NotationRepositoryImpl;
import org.biosemantics.conceptstore.repository.impl.TraversalRepositoryImpl;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

import scala.actors.threadpool.Arrays;

public class Experiment1 {

	public final static String[] filterSemanticTypesDrugArray = new String[] { "T002", "T028", "T044", "T059", "T061",
			"T109", "T110", "T111", "T114", "T115", "T116", "T118", "T119", "T121", "T123", "T124", "T125", "T126",
			"T127", "T129", "T130", "T131", "T195", "T196", "T197" };
	public final static String[] filterSemanticTypesDiseaseArray = new String[] { "T004", "T019", "T020", "T031",
			"T033", "T034", "T037", "T039", "T040", "T042", "T046", "T047", "T048", "T061" };

	private List<String[]> getSrcAndTarget(String inputFile) throws IOException {
		List<String> lines = FileUtils.readLines(new File(inputFile));
		List<String[]> io = new ArrayList<String[]>();
		for (String line : lines) {
			String[] columns = line.split(";");
			String type = columns[0];
			String source = columns[2].split("\\|")[2];
			String target = columns[3].split("\\|")[2];
			String[] out = new String[] { type, source, target };
			io.add(out);
			logger.info("{} {} {}", new Object[] { type, source, target });
		}
		return io;
	}

	private Set<Long> getDrugConceptId(String cui) {
		Collection<Notation> notations = notationRepository.getByCode(cui);
		Set<Long> conceptIds = new HashSet<Long>();
		for (Notation notation : notations) {
			for (Concept concept : notation.getRelatedConcepts()) {
				if (concept.getType() == ConceptType.CONCEPT) {
					for (Concept scheme : concept.getInSchemes()) {
						for (Notation schemeNot : scheme.getNotations()) {
							if (schemeNot.getSource().equalsIgnoreCase(NotationSourceConstant.UMLS.toString())
									&& Arrays.asList(filterSemanticTypesDrugArray).contains(schemeNot.getCode())) {
								conceptIds.add(concept.getId());
							}
						}
					}
				}
			}
		}
		return conceptIds;
	}

	private Set<Long> getDiseaseConceptId(String cui) {
		Collection<Notation> notations = notationRepository.getByCode(cui);
		Set<Long> conceptIds = new HashSet<Long>();
		for (Notation notation : notations) {
			for (Concept concept : notation.getRelatedConcepts()) {
				if (concept.getType() == ConceptType.CONCEPT) {
					for (Concept scheme : concept.getInSchemes()) {
						for (Notation schemeNot : scheme.getNotations()) {
							if (schemeNot.getSource().equalsIgnoreCase(NotationSourceConstant.UMLS.toString())
									&& Arrays.asList(filterSemanticTypesDiseaseArray).contains(schemeNot.getCode())) {
								conceptIds.add(concept.getId());
							}
						}
					}
				}
			}
		}
		return conceptIds;
	}

	private static final String DB_PATH = "/Users/bhsingh/code/neo4j-community-1.8/data/graph.db";
	private static final String FOLDER = null;
	private GraphDatabaseService graphDb;
	private ConceptRepository conceptRepository;
	private LabelRepository labelRepository;
	private NotationRepository notationRepository;
	private TraversalRepository traversalRepository;

	public void init(String dbPath) {
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		registerShutdownHook(graphDb);
		this.conceptRepository = new ConceptRepositoryImpl(graphDb);
		this.labelRepository = new LabelRepositoryImpl(graphDb);
		this.notationRepository = new NotationRepositoryImpl(graphDb);
		this.traversalRepository = new TraversalRepositoryImpl(graphDb, this.conceptRepository);
	}

	public void initWithConfig(String dbPath) {
		Map<String, String> config = MapUtil.stringMap("neostore.propertystore.db.index.keys.mapped_memory", "5M",
				"neostore.propertystore.db.index.mapped_memory", "5M", "neostore.nodestore.db.mapped_memory", "200M",
				"neostore.relationshipstore.db.mapped_memory", "1000M", "neostore.propertystore.db.mapped_memory",
				"1000M", "neostore.propertystore.db.strings.mapped_memory", "200M");
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbPath).setConfig(config).newGraphDatabase();
		registerShutdownHook(graphDb);
		this.conceptRepository = new ConceptRepositoryImpl(graphDb);
		this.labelRepository = new LabelRepositoryImpl(graphDb);
		this.notationRepository = new NotationRepositoryImpl(graphDb);
		this.traversalRepository = new TraversalRepositoryImpl(graphDb, this.conceptRepository);
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	private Iterable<Path> getPaths(Long src, Long tgt, int max) {
		return traversalRepository.findShortestPath(src, tgt, max);
	}

	public static void main(String[] args) throws IOException {
		Experiment1 experiment = new Experiment1();
		experiment.initWithConfig(DB_PATH);
		List<String[]> srcTargets = experiment
				.getSrcAndTarget("/Users/bhsingh/code/data/kang-rlsp-mining/exp1/input/kang-relation-mining-training-set.csv");
		int ctr = 1;
		for (String[] srcTarget : srcTargets) {
			Set<Long> drugIds = experiment.getDrugConceptId(srcTarget[1]);
			Long drugId = null;
			for (Long long1 : drugIds) {
				drugId = long1;
			}
			Set<Long> diseaseIds = experiment.getDiseaseConceptId(srcTarget[2]);
			Long diseaseId = null;
			for (Long long1 : diseaseIds) {
				diseaseId = long1;
			}
			logger.info("{} {}", new Object[] { drugId, diseaseId });
			Iterable<Path> paths = experiment.getPaths(drugId, diseaseId, 4);
			CSVWriter pathWriter = new CSVWriter(new FileWriter(new File(FOLDER, ctr + "_PATH")));
			for (Path path : paths) {
				pathWriter.writeNext(new String[] { path.toString() });
			}
			pathWriter.flush();
			pathWriter.close();

		}
	}

	private static final Logger logger = LoggerFactory.getLogger(Experiment1.class);
}
