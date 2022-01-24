package com.ipssi.beans;

import java.util.Date;

import com.ipssi.gen.utils.Misc;
import com.ipssi.rfid.db.Table;
import com.ipssi.rfid.db.Table.Column;
import com.ipssi.rfid.db.Table.GENRATED;
import com.ipssi.rfid.db.Table.KEY;
import com.ipssi.rfid.db.Table.PRIMARY_KEY;

@Table("cgpl_sales_order")
public class SalesOrderTable {
	@KEY
	@GENRATED
	@PRIMARY_KEY
	@Column("id")
	private int id;
	@Column("sap_sales_order")
	private String sap_sales_order;
	@Column("sap_customer_sap_code")
	private String sap_customer_sap_code;
	@Column("sap_sales_order_creation_date")
	private Date sap_sales_order_creation_date;
	@Column("customer_id")
	private int customer_id;
	@Column("sap_customer_name")
	private String sap_customer_name;
	@Column("sap_customer_address")
	private String sap_customer_address;
	@Column("sap_material")
	private String sap_material;
	@Column("sap_line_item")
	private int sap_line_item;
	@Column("created_on")
	private Date created_on;
	@Column("updated_on")
	private Date updated_on;
	@Column("updated_by")
	private int updatedBy;
	@Column("created_by")
	private int createdBy;
	@Column("STATUS")
	private int STATUS;
	@Column("port_node_id")
	private int port_node_id;
	@Column("sap_order_quantity")
	private String sap_order_quantity;
	@Column("sap_order_unit")
	private String sap_order_unit;
	@Column("sap_sale_order_status")
	private int sap_sale_order_status;
	@Column("sap_order_lapse_quantity")
	private String sapOrderLapseQuantity;

	public String getSapOrderLapseQuantity() {
		return sapOrderLapseQuantity;
	}

	public void setSapOrderLapseQuantity(String sapOrderLapseQuantity) {
		this.sapOrderLapseQuantity = sapOrderLapseQuantity;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSap_sales_order() {
		return sap_sales_order;
	}

	public void setSap_sales_order(String sap_sales_order) {
		this.sap_sales_order = sap_sales_order;
	}

	public Date getSap_sales_order_creation_date() {
		return sap_sales_order_creation_date;
	}

	public void setSap_sales_order_creation_date(Date sap_sales_order_creation_date) {
		this.sap_sales_order_creation_date = sap_sales_order_creation_date;
	}

	public int getCustomer_id() {
		return customer_id;
	}

	public void setCustomer_id(int customer_id) {
		this.customer_id = customer_id;
	}

	public String getSap_customer_name() {
		return sap_customer_name;
	}

	public void setSap_customer_name(String sap_customer_name) {
		this.sap_customer_name = sap_customer_name;
	}

	public String getSap_customer_address() {
		return sap_customer_address;
	}

	public void setSap_customer_address(String sap_customer_address) {
		this.sap_customer_address = sap_customer_address;
	}

	public String getSap_material() {
		return sap_material;
	}

	public void setSap_material(String sap_material) {
		this.sap_material = sap_material;
	}

	public int getSap_line_item() {
		return sap_line_item;
	}

	public void setSap_line_item(int sap_line_item) {
		this.sap_line_item = sap_line_item;
	}

	public Date getCreated_on() {
		return created_on;
	}

	public void setCreated_on(Date created_on) {
		this.created_on = created_on;
	}

	public Date getUpdated_on() {
		return updated_on;
	}

	public void setUpdated_on(Date updated_on) {
		this.updated_on = updated_on;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public int getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}

	public int getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(int sTATUS) {
		STATUS = sTATUS;
	}

	public int getPort_node_id() {
		return port_node_id;
	}

	public void setPort_node_id(int port_node_id) {
		this.port_node_id = port_node_id;
	}

	public String getSap_order_quantity() {
		return sap_order_quantity;
	}

	public void setSap_order_quantity(String sap_order_quantity) {
		this.sap_order_quantity = sap_order_quantity;
	}

	public String getSap_order_unit() {
		return sap_order_unit;
	}

	public void setSap_order_unit(String sap_order_unit) {
		this.sap_order_unit = sap_order_unit;
	}

	public int getSap_sale_order_status() {
		return sap_sale_order_status;
	}

	public void setSap_sale_order_status(int sap_sale_order_status) {
		this.sap_sale_order_status = sap_sale_order_status;
	}

	public String getSap_customer_sap_code() {
		return sap_customer_sap_code;
	}

	public void setSap_customer_sap_code(String sap_customer_sap_code) {
		this.sap_customer_sap_code = sap_customer_sap_code;
	}

	@Override
	public String toString() {
		return "SalesOrderTable [id=" + id + ", sap_sales_order=" + sap_sales_order + ", sap_customer_sap_code="
				+ sap_customer_sap_code + ", sap_sales_order_creation_date=" + sap_sales_order_creation_date
				+ ", customer_id=" + customer_id + ", sap_customer_name=" + sap_customer_name
				+ ", sap_customer_address=" + sap_customer_address + ", sap_material=" + sap_material
				+ ", sap_line_item=" + sap_line_item + ", created_on=" + created_on + ", updated_on=" + updated_on
				+ ", updatedBy=" + updatedBy + ", createdBy=" + createdBy + ", STATUS=" + STATUS + ", port_node_id="
				+ port_node_id + ", sap_order_quantity=" + sap_order_quantity + ", sap_order_unit=" + sap_order_unit
				+ ", sap_sale_order_status=" + sap_sale_order_status + "]";
	}

}
