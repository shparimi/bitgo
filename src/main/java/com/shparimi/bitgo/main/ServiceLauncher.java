package com.shparimi.bitgo.main;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.shparimi.bitgo.fetch.GetTransactionData;
import com.shparimi.bitgo.models.TransactionData;
import com.shparimi.bitgo.process.ProcessTransactionAncestry;

public class ServiceLauncher {

	private static final Logger logger = LogManager.getLogger(ServiceLauncher.class);

	public static void main(String[] args) throws StreamReadException, DatabindException, IOException {
		GetTransactionData gtd = new GetTransactionData();
		String input = "";
		if (args.length == 0) {
			input = "680000";
		} else {
			input = args[0];
		}
		List<TransactionData> tds = gtd.getTransactionDataFromFile(Integer.parseInt(input));
		ProcessTransactionAncestry pta = new ProcessTransactionAncestry();
		pta.processTransactionData(tds);
	}

}
