package com.shparimi.bitgo.process;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.shparimi.bitgo.models.TransactionData;

public class ProcessTransactionAncestry {

	private Map<String, Integer> ancestryCount = new TreeMap<>(Collections.reverseOrder());

	public void processTransactionData(List<TransactionData> inputData) {
		Map<String, TransactionData> inputTransactions = getAllInputTransactions(inputData);
		for (TransactionData td : inputData) {
			String tid = td.getTransactionId();
			int count = 0;
			for (TransactionData vin : td.getVin()) {
				count+=doDepthSearchForId(inputTransactions, vin.getTransactionId());
			}
			ancestryCount.put(tid, count);
		}
		int printSize = 10;
		for (Entry<String, Integer> me : ancestryCount.entrySet()) {
			if (printSize > 0) {
				System.out.println(me.getKey() + "->" + me.getValue());
			} else {
				break;
			}
			printSize--;
		}
	}

	private Map<String, TransactionData> getAllInputTransactions(List<TransactionData> inputData) {
		Map<String, TransactionData> result = new HashMap<>();
		for (TransactionData td : inputData) {
			result.put(td.getTransactionId(), td);
		}
		return result;
	}

	private int doDepthSearchForId(Map<String, TransactionData> inputTransactions, String id) {
		int count = 0;
		if (inputTransactions.containsKey(id)) {
			if (ancestryCount.containsKey(id)) {
				return ancestryCount.get(id);
			} else {
				TransactionData tds = inputTransactions.get(id);
				for (TransactionData vinIds : tds.getVin()) {
					int lcount = 1+doDepthSearchForId(inputTransactions, vinIds.getTransactionId());
					ancestryCount.put(vinIds.getTransactionId(), lcount);
				}
			}

		}
		return count;
	}
}
