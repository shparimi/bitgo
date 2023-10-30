package com.shparimi.bitgo.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionData {

	@JsonProperty("txid")
	private String transactionId;

	@JsonProperty("vin")
	private List<TransactionData> vin;

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public List<TransactionData> getVin() {
		return vin;
	}

	public void setVin(List<TransactionData> vin) {
		this.vin = vin;
	}
}
