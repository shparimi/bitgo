package com.shparimi.bitgo.fetch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shparimi.bitgo.models.TransactionData;

public class GetTransactionData {

	private HttpClient httpClient;
	private ObjectMapper objectMapper;
	private Map<String, Set<String>> transactionsPerBlock;
	private List<TransactionData> tds;

	public GetTransactionData() {
		httpClient = new DefaultHttpClient();
		objectMapper = new ObjectMapper();
		transactionsPerBlock = new HashMap<>();
		tds = new ArrayList<>();
		
	}

	private int getTransactionsCount(String transactionHash) throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet("https://blockstream.info/api/block/" + transactionHash + "/txids");
		httpget.addHeader("accept", "application/json");
		HttpResponse response = httpClient.execute(httpget);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
		}
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			throw new RuntimeException("No data found");
		} else {
			String responseData = EntityUtils.toString(entity);
			Set<String> transactions = objectMapper.readValue(responseData, new TypeReference<Set<String>>() {
			});
			transactionsPerBlock.put(transactionHash, transactions);
			return transactions.size();
		}
	}

	private void printAllTransactionHistory(String transactionHash) throws ClientProtocolException, IOException {
		File file = new File("src/main/resources/" + transactionHash + ".txt");
		if (!file.exists()) {
			file.createNewFile();
			int totalCount = getTransactionsCount(transactionHash);
			int count = 0;
			FileWriter fw = new FileWriter(file);
			try (BufferedWriter bw = new BufferedWriter(fw)) {
				while (count < totalCount - 1) {
					HttpGet httpget = new HttpGet(
							"https://blockstream.info/api/block/" + transactionHash + "/txs/" + count);
					httpget.addHeader("accept", "application/json");
					HttpResponse response = httpClient.execute(httpget);
					if (response.getStatusLine().getStatusCode() != 200) {
						throw new RuntimeException(
								"Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
					}
					HttpEntity entity = response.getEntity();
					if (entity == null) {
						throw new RuntimeException("No data found");
					} else {
						String responseData = EntityUtils.toString(entity);
						bw.append(responseData);
						bw.flush();
						List<TransactionData> result = objectMapper.readValue(responseData, new TypeReference<List<TransactionData>>() {
						});
						tds.addAll(result);
						
					}
					count += 25;
				}
			}
		}

	}

	public List<TransactionData> getTransactionDataFromFile(String transactionHash)
			throws StreamReadException, DatabindException, IOException {
		File file = new File("src/main/resources/" + transactionHash + ".txt");
		/*if (!file.exists()) {
			printAllTransactionHistory(transactionHash);
		}
		List<TransactionData> result = objectMapper.readValue(file, new TypeReference<List<TransactionData>>() {
		});*/
		printAllTransactionHistory(transactionHash);
		return tds;
	}

}
