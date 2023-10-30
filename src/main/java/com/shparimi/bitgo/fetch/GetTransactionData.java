package com.shparimi.bitgo.fetch;

import java.io.File;
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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shparimi.bitgo.models.TransactionData;

public class GetTransactionData {

	private HttpClient httpClient;
	private ObjectMapper objectMapper;
	private Map<Integer, TransactionDetails> transactionMetaData;

	public GetTransactionData() {
		httpClient = new DefaultHttpClient();
		objectMapper = new ObjectMapper();
		transactionMetaData = new HashMap<>();
	}

	private String getHashForBlock(int blocknumber) throws ClientProtocolException, IOException {
		return doGet("https://blockstream.info/api/block-height/" + blocknumber);
	}

	private int getTransactionsCount(String transactionHash) throws ClientProtocolException, IOException {
		String responseData = doGet("https://blockstream.info/api/block/" + transactionHash + "/txids");
		Set<String> transactions = objectMapper.readValue(responseData, new TypeReference<Set<String>>() {
		});
		return transactions.size();
	}

	private void printAllTransactionHistory(String transactionHash, int transactionSize)
			throws ClientProtocolException, IOException {
		File file = new File("src/main/resources/" + transactionHash + ".txt");
		if (!file.exists()) {
			file.createNewFile();
			int count = 0;
			try (JsonGenerator jGenerator = objectMapper.getFactory().createGenerator(file, JsonEncoding.UTF8)) {
				jGenerator.writeStartArray();
				while (count < transactionSize - 1) {
					String responseData = doGet(
							"https://blockstream.info/api/block/" + transactionHash + "/txs/" + count);
					List<TransactionData> responseList = objectMapper.readValue(responseData,
							new TypeReference<List<TransactionData>>() {
							});
					for (TransactionData td : responseList) {
						jGenerator.writeStartObject();
						jGenerator.writeStringField("txid", td.getTransactionId());
						jGenerator.writeFieldName("vin");
						jGenerator.writeStartArray();
						for (TransactionData vtd : td.getVin()) {
							jGenerator.writeStartObject();
							jGenerator.writeStringField("txid", vtd.getTransactionId());
							jGenerator.writeEndObject();
						}
						jGenerator.writeEndArray();
						jGenerator.writeEndObject();
					}
					jGenerator.flush();
					count += 25;
				}
				jGenerator.writeEndArray();
				jGenerator.flush();
			}
		}

	}

	public List<TransactionData> getTransactionDataFromFile(int transactionBlock)
			throws StreamReadException, DatabindException, IOException {
		TransactionDetails tdetails = this.transactionMetaData.get(transactionBlock);
		if (tdetails == null) {
			String hash = getHashForBlock(transactionBlock);
			int size = getTransactionsCount(hash);
			tdetails = new TransactionDetails(hash, size);
			this.transactionMetaData.put(transactionBlock, tdetails);
		}
		List<TransactionData> result = new ArrayList<>();
		File file = new File("src/main/resources/" + tdetails.getHash() + ".txt");
		if (!file.exists()) {
			printAllTransactionHistory(tdetails.getHash(), tdetails.getInputTransactionSize());
		}
		try (JsonParser jParser = new JsonFactory().createParser(file)) {
			while (jParser.nextToken() != JsonToken.END_ARRAY) {
				String lastProperty = "", id = "";
				List<String> vin = new ArrayList<>();
				while (jParser.nextToken() != JsonToken.END_OBJECT) {
					lastProperty = jParser.getCurrentName() == null ? "" : jParser.getCurrentName();
					if (lastProperty.equals("txid")) {
						jParser.nextToken();
						id = jParser.getText();
					} else if (lastProperty.equals("vin")) {
						while (jParser.nextToken() != JsonToken.END_ARRAY) {
							lastProperty = jParser.getCurrentName() == null ? "" : jParser.getCurrentName();
							if (lastProperty.equals("txid")) {
								jParser.nextToken();
								String vId = jParser.getText();
								vin.add(vId);
							}
						}
					}
				}
				TransactionData td = new TransactionData(id, vin);
				result.add(td);
			}

		}

		// List<TransactionData> result = objectMapper.readValue(file, new
		// TypeReference<List<TransactionData>>() {
		// });
		// printAllTransactionHistory(transactionHash);
		return result;// tds;
	}

	private String doGet(String url) throws ClientProtocolException, IOException {
		HttpGet httpget = new HttpGet(url);
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
			return responseData;
		}
	}

	static class TransactionDetails {
		private String hash;

		private int inputTransactionSize;

		public TransactionDetails(String hash, int inputSize) {
			this.hash = hash;
			this.inputTransactionSize = inputSize;
		}

		public String getHash() {
			return hash;
		}

		public int getInputTransactionSize() {
			return inputTransactionSize;
		}
	}

}
