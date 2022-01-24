package com.ipssi.android;

public class InspectionQuestion {
	public InspectionQuestion(int queryId, String queryText,
			String querySubText, boolean isMandatory, boolean isPhoto) {
		this.queryText = queryText;
		this.querySubText = querySubText;
		this.isMandatory = isMandatory;
		this.isPhoto = isPhoto;
		this.queryId = queryId;
	}

	public InspectionQuestion() {
	}

	public String getQueryText() {
		return queryText;
	}

	public String getQuerySubText() {
		return querySubText;
	}

	public boolean isMandatory() {
		return isMandatory;
	}

	public boolean isPhoto() {
		return isPhoto;
	}

	public int getQueryId() {
		return queryId;
	}

	public void setQueryId(int id) {
		queryId = id;
	}

	public void setInspectionVehicleDetailId(String vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getInspectionVehicleDetailId() {
		return vehicleId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public String getObservation() {
		return observation;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setMandatoryToComplete(String mandatoryToComplete) {
		this.mandatoryToComplete = mandatoryToComplete;
	}

	public String getMandatoryToComplete() {
		return mandatoryToComplete;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public String getExplanation() {
		return explanation;
	}

	String queryText, querySubText;
	boolean isMandatory, isPhoto;
	int queryId;
	private String vehicleId, type, result, observation, explanation,
			mandatoryToComplete;
	private byte[] photo;

}
