package com.ipssi.rfid.beans;

import com.ipssi.gen.utils.Misc;

public class TprChallanData {
	private int materialCat = Misc.getUndefInt();
	private int doId = Misc.getUndefInt();
	private String doNumber;
	private String minesCode;
	private String rfMinesCode;
	private String gradeCode;
	private String rfGradeCode;
	private String productCode;
	private String rfProductCode;
	private String transporterCode;
	private String rfTransporterCode;
	private String washeryCode;
	private String destinationCode;
	private String lrNo;
	private String rfLrNo;
	private String rfDestinationCode;
	private String customerCode;
	private String invoiceNumber;
	
	public TprChallanData() {
		super();
	}
	
	public TprChallanData(int materialCat, int doId, String doNumber, String minesCode, String rfMinesCode,
			String gradeCode, String rfGradeCode, String productCode, String rfProductCode, String transporterCode,
			String rfTransporterCode, String washeryCode, String destinationCode, String lrNo, String rfLrNo,
			String rfDestinationCode, String customerCode,String invoiceNumber) {
		super();
		this.materialCat = materialCat;
		this.doId = doId;
		this.doNumber = doNumber;
		this.minesCode = minesCode;
		this.rfMinesCode = rfMinesCode;
		this.gradeCode = gradeCode;
		this.rfGradeCode = rfGradeCode;
		this.productCode = productCode;
		this.rfProductCode = rfProductCode;
		this.transporterCode = transporterCode;
		this.rfTransporterCode = rfTransporterCode;
		this.washeryCode = washeryCode;
		this.destinationCode = destinationCode;
		this.lrNo = lrNo;
		this.rfLrNo = rfLrNo;
		this.rfDestinationCode = rfDestinationCode;
		this.customerCode = customerCode;
		this.invoiceNumber = invoiceNumber;
	}

	public int getMaterialCat() {
		return materialCat;
	}
	public void setMaterialCat(int materialCat) {
		this.materialCat = materialCat;
	}
	public int getDoId() {
		return doId;
	}
	public void setDoId(int doId) {
		this.doId = doId;
	}
	public String getDoNumber() {
		return doNumber;
	}
	public void setDoNumber(String doNumber) {
		this.doNumber = doNumber;
	}
	public String getMinesCode() {
		return minesCode;
	}
	public void setMinesCode(String minesCode) {
		this.minesCode = minesCode;
	}
	public String getRfMinesCode() {
		return rfMinesCode;
	}
	public void setRfMinesCode(String rfMinesCode) {
		this.rfMinesCode = rfMinesCode;
	}
	public String getGradeCode() {
		return gradeCode;
	}
	public void setGradeCode(String gradeCode) {
		this.gradeCode = gradeCode;
	}
	public String getRfGradeCode() {
		return rfGradeCode;
	}
	public void setRfGradeCode(String rfGradeCode) {
		this.rfGradeCode = rfGradeCode;
	}
	public String getProductCode() {
		return productCode;
	}
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
	public String getRfProductCode() {
		return rfProductCode;
	}
	public void setRfProductCode(String rfProductCode) {
		this.rfProductCode = rfProductCode;
	}
	public String getTransporterCode() {
		return transporterCode;
	}
	public void setTransporterCode(String transporterCode) {
		this.transporterCode = transporterCode;
	}
	public String getRfTransporterCode() {
		return rfTransporterCode;
	}
	public void setRfTransporterCode(String rfTransporterCode) {
		this.rfTransporterCode = rfTransporterCode;
	}
	public String getWasheryCode() {
		return washeryCode;
	}
	public void setWasheryCode(String washeryCode) {
		this.washeryCode = washeryCode;
	}
	public String getDestinationCode() {
		return destinationCode;
	}
	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}
	public String getLrNo() {
		return lrNo;
	}
	public void setLrNo(String lrNo) {
		this.lrNo = lrNo;
	}
	public String getRfLrNo() {
		return rfLrNo;
	}
	public void setRfLrNo(String rfLrNo) {
		this.rfLrNo = rfLrNo;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destinationCode == null) ? 0 : destinationCode.hashCode());
		result = prime * result + doId;
		result = prime * result + ((doNumber == null) ? 0 : doNumber.hashCode());
		result = prime * result + ((gradeCode == null) ? 0 : gradeCode.hashCode());
		result = prime * result + ((lrNo == null) ? 0 : lrNo.hashCode());
		result = prime * result + materialCat;
		result = prime * result + ((minesCode == null) ? 0 : minesCode.hashCode());
		result = prime * result + ((productCode == null) ? 0 : productCode.hashCode());
		result = prime * result + ((rfGradeCode == null) ? 0 : rfGradeCode.hashCode());
		result = prime * result + ((rfLrNo == null) ? 0 : rfLrNo.hashCode());
		result = prime * result + ((rfMinesCode == null) ? 0 : rfMinesCode.hashCode());
		result = prime * result + ((rfProductCode == null) ? 0 : rfProductCode.hashCode());
		result = prime * result + ((rfTransporterCode == null) ? 0 : rfTransporterCode.hashCode());
		result = prime * result + ((transporterCode == null) ? 0 : transporterCode.hashCode());
		result = prime * result + ((washeryCode == null) ? 0 : washeryCode.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TprChallanData other = (TprChallanData) obj;
		if (destinationCode == null) {
			if (other.destinationCode != null)
				return false;
		} else if (!destinationCode.equals(other.destinationCode))
			return false;
		if (doId != other.doId)
			return false;
		if (doNumber == null) {
			if (other.doNumber != null)
				return false;
		} else if (!doNumber.equals(other.doNumber))
			return false;
		if (gradeCode == null) {
			if (other.gradeCode != null)
				return false;
		} else if (!gradeCode.equals(other.gradeCode))
			return false;
		if (lrNo == null) {
			if (other.lrNo != null)
				return false;
		} else if (!lrNo.equals(other.lrNo))
			return false;
		if (materialCat != other.materialCat)
			return false;
		if (minesCode == null) {
			if (other.minesCode != null)
				return false;
		} else if (!minesCode.equals(other.minesCode))
			return false;
		if (productCode == null) {
			if (other.productCode != null)
				return false;
		} else if (!productCode.equals(other.productCode))
			return false;
		if (rfGradeCode == null) {
			if (other.rfGradeCode != null)
				return false;
		} else if (!rfGradeCode.equals(other.rfGradeCode))
			return false;
		if (rfLrNo == null) {
			if (other.rfLrNo != null)
				return false;
		} else if (!rfLrNo.equals(other.rfLrNo))
			return false;
		if (rfMinesCode == null) {
			if (other.rfMinesCode != null)
				return false;
		} else if (!rfMinesCode.equals(other.rfMinesCode))
			return false;
		if (rfProductCode == null) {
			if (other.rfProductCode != null)
				return false;
		} else if (!rfProductCode.equals(other.rfProductCode))
			return false;
		if (rfTransporterCode == null) {
			if (other.rfTransporterCode != null)
				return false;
		} else if (!rfTransporterCode.equals(other.rfTransporterCode))
			return false;
		if (transporterCode == null) {
			if (other.transporterCode != null)
				return false;
		} else if (!transporterCode.equals(other.transporterCode))
			return false;
		if (washeryCode == null) {
			if (other.washeryCode != null)
				return false;
		} else if (!washeryCode.equals(other.washeryCode))
			return false;
		return true;
	}

	public String getRfDestinationCode() {
		return rfDestinationCode;
	}

	public void setRfDestinationCode(String rfDestinationCode) {
		this.rfDestinationCode = rfDestinationCode;
	}

	public String getCustomerCode() {
		return customerCode;
	}

	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}

	public String getInvoiceNumber() {
		return invoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		this.invoiceNumber = invoiceNumber;
	}
	
}
