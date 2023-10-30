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
		List<TransactionData> tds = gtd
				.getTransactionDataFromFile("000000000000000000076c036ff5119e5a5a74df77abf64203473364509f7732");
		ProcessTransactionAncestry pta = new ProcessTransactionAncestry();
		pta.processTransactionData(tds);
	}

}
