package org.biosemantics.misc.utility.kang;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.biosemantics.common.utility.DatabaseUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class UmlsRlsp {
	private static final String SQL = "select CUI1, CUI2, RELA from MRREL where CUI1 != CUI2 AND RELA IS NOT NULL AND (CUI1= ? OR CUI2 = ?);";
	private static final String INPUT = "/Users/bhsingh/code/data/kang-rlsp-mining/uniqCUIs.txt";
	private static final String OUTPUT = "/Users/bhsingh/code/data/kang-rlsp-mining/uniqCuis-out.csv";
	private static final Logger logger = LoggerFactory.getLogger(UmlsRlsp.class);

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		DatabaseUtility dbUtility = new DatabaseUtility();
		Connection connection = dbUtility.getConnection();
		PreparedStatement pstmt = connection.prepareStatement(SQL);

		CSVReader csvReader = new CSVReader(new FileReader(INPUT));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(OUTPUT));
		List<String[]> rows = csvReader.readAll();
		for (String[] columns : rows) {
			Set<String> nodups = new HashSet<String>();
			String cui = columns[0];
			pstmt.setString(1, cui);
			pstmt.setString(2, cui);
			ResultSet rs = pstmt.executeQuery();
			int ctr = 0;
			try {
				while (rs.next()) {
					++ctr;
					String cui1 = rs.getString("CUI1");
					String cui2 = rs.getString("CUI2");
					String rela = rs.getString("RELA");
					nodups.add(cui1 + cui2 + rela);
				}
			} finally {
				rs.close();
			}
			logger.info("{} {} {}", new Object[] { cui, ctr, nodups.size() });
			csvWriter.writeNext(new String[] { cui, "" + nodups.size() });
		}
		csvWriter.flush();
		csvWriter.close();
		csvReader.close();
		connection.close();

	}
}
