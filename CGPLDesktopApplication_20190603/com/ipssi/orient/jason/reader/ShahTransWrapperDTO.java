package com.ipssi.orient.jason.reader;

import java.util.ArrayList;

public class ShahTransWrapperDTO {
	private String error;
	private ArrayList<ShahTransDataDTO> data;
	private ArrayList<String> error_list;
	
	public ShahTransWrapperDTO(String error,  ArrayList<ShahTransDataDTO> data, ArrayList<String> error_list) {
		 this.error=error;
		 this.data=data;
		 this.error_list=error_list;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public ArrayList<ShahTransDataDTO> getData() {
		return data;
	}

	public void setData(ArrayList<ShahTransDataDTO> data) {
		this.data = data;
	}

	public ArrayList<String> getError_list() {
		return error_list;
	}

	public void setError_list(ArrayList<String> error_list) {
		this.error_list = error_list;
	}
	

	
}
