package com.ipssi.inventory;

/**
 * @author balwant
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.Cache;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.SessionManager;
import com.ipssi.gen.utils.Triple;
import com.ipssi.tracker.common.db.DBQueries;
import com.ipssi.tracker.common.db.DBQueries.InventoryMng;
import com.itextpdf.text.pdf.PdfStructTreeController.returnType;

public class InventoryMngDao {


	public InventoryMngDao(SessionManager m_session) {
		this.m_session = m_session;
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
	public SessionManager m_session = null;

	public List<InventoryMngBean> parseProductData(String xmlData) {
		List<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();

		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xmlData);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();

		for (int i = 0; i < size; i++) {
			InventoryMngBean bean = new InventoryMngBean();
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;

			int catId = Misc.getParamAsInt(e.getAttribute("categoryCode"));
			bean.setCategoryId(catId);

			String itemName = Misc.getParamAsString(e.getAttribute("itemName"));
			if ("".equalsIgnoreCase(itemName)) {
				itemName = null;
			}
			bean.setItemName(itemName);
			String itemCode = Misc.getParamAsString(e.getAttribute("itemCode"));
			bean.setItemCode(itemCode);

			//itemLife  lifeUnit
			int itemLife = Misc.getParamAsInt(e.getAttribute("itemLife"));
			bean.setProductLife(itemLife);
			
			int lifeUnit = Misc.getParamAsInt(e.getAttribute("lifeUnit"));
			bean.setProductLifeUnit(lifeUnit);
			
			String Manufacturer = Misc.getParamAsString(e.getAttribute("manufacturer"));
			if ("".equalsIgnoreCase(Manufacturer)) {
				Manufacturer = null;
			}
			bean.setManufacturer(Manufacturer);
			String mfgCode = Misc.getParamAsString(e.getAttribute("manufacturerCode"));
			bean.setManufacturerCode(mfgCode);

			String notes = Misc.getParamAsString(e.getAttribute("itemNotes"));
			if ("".equalsIgnoreCase(notes)) {
				notes = null;
			}
			bean.setNotes(notes);
			dataList.add(bean);

		}
		return dataList;

	}
	public void saveNewCategoryData(InventoryMngBean bean) throws GenericException {
		Connection conn = null;
		PreparedStatement ps = null;
		if(bean == null)
			return;
			try {
				conn = m_session.getConnection();
				ps = conn.prepareStatement(DBQueries.InventoryMng.INSERT_INVENTORY_PRODUCT );
				Misc.setParamInt(ps, bean.getCategoryId(), 1);
				ps.setString(2, bean.getItemName());
				ps.setString(3, bean.getItemCode());
				ps.setString(4, bean.getManufacturer());
				ps.setString(5, bean.getManufacturerCode());
				ps.setString(6, bean.getNotes());
				ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
				Misc.setParamInt(ps, bean.getProductLife(), 8);
			    Misc.setParamInt(ps, bean.getProductLifeUnit(), 9);
			    Misc.setParamInt(ps, bean.getPortNodeId(), 10);
				ps.execute();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
	}

	public void saveNewCategoryData(String xmlData, int productId) throws GenericException {
		Connection conn = null;
		PreparedStatement ps = null;
		InventoryMngBean bean = null;
		List<InventoryMngBean> dataBean = parseProductData(xmlData);

		int size = Misc.getUndefInt();
		if (dataBean != null) {
			size = dataBean.size();
		}
		if (size > 0) {
			try {
				conn = m_session.getConnection();
				ps = conn.prepareStatement(Misc.isUndef(productId) ? DBQueries.InventoryMng.INSERT_INVENTORY_PRODUCT : DBQueries.InventoryMng.UPDATE_INVENTORY_PRODUCT);
				for (int i = 0; i < size; i++) {
					bean = dataBean.get(i);
					Misc.setParamInt(ps, bean.getCategoryId(), 1);
					ps.setString(2, bean.getItemName());
					ps.setString(3, bean.getItemCode());
					ps.setString(4, bean.getManufacturer());
					ps.setString(5, bean.getManufacturerCode());
					ps.setString(6, bean.getNotes());
					ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
					Misc.setParamInt(ps, bean.getProductLife(), 8);
					Misc.setParamInt(ps, bean.getProductLifeUnit(), 9);
			        if (Misc.isUndef(productId)) {
			        	Misc.setParamInt(ps, Misc.getParamAsInt(m_session.getAttribute("pv123")), 10);
					}
					if (!Misc.isUndef(productId)) {
						Misc.setParamInt(ps, productId, 10);
					}
					ps.addBatch();
				}
				ps.executeBatch();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (ps != null) {
					try {
						ps.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("The Method Is Executed Well");

	}

	public List<InventoryMngBean> parseStockData(String xmlData) {
		List<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();

		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xmlData);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();

		for (int i = 0; i < size; i++) {
			InventoryMngBean bean = new InventoryMngBean();
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;

			int id = Misc.getParamAsInt(e.getAttribute("stockId"));
			bean.setId(id);
            bean.setYetReleased(Misc.getParamAsInt(e.getAttribute("yetReleased"))==1);
           
			int itemCode = Misc.getParamAsInt(e.getAttribute("itemCode"));
			bean.setProductId(itemCode);

			String lotNumber = Misc.getParamAsString(e.getAttribute("lotNumber"));
			bean.setLotNumber(lotNumber);

			int qty = Misc.getParamAsInt(e.getAttribute("quantity"));
			bean.setQuantaty(qty);
            int oldQty = Misc.getParamAsInt(e.getAttribute("oldQuantity"));
            bean.setStockDifference(qty-oldQty);
            //"remainQuantity"
            int remainQty = Misc.getParamAsInt(e.getAttribute("remainQuantity"));
            bean.setRemainQty(remainQty);
			double price = Misc.getParamAsDouble(e.getAttribute("price"));
			bean.setPrice(price);

			int age = Misc.getParamAsInt(e.getAttribute("itemAge"));
			bean.setAge(age);

			String Manufacturer = Misc.getParamAsString(e.getAttribute("manufacturer"));
			if ("".equalsIgnoreCase(Manufacturer)) {
				Manufacturer = null;
			}
			bean.setManufacturer(Manufacturer);
			String supplier = Misc.getParamAsString(e.getAttribute("supplier"));
			if ("".equalsIgnoreCase(supplier)) {
				supplier = null;
			}
			bean.setSupplier(supplier);

			String itemPurchase = Misc.getParamAsString(e.getAttribute("itemPurchase"));
			if ("".equalsIgnoreCase(itemPurchase)) {
				itemPurchase = null;
			}
			bean.setPurchaseReceiver(itemPurchase);
			String deliveryReport = Misc.getParamAsString(e.getAttribute("deliveryReport"));
			if ("".equalsIgnoreCase(deliveryReport)) {
				deliveryReport = null;
			}
			bean.setDeliveryReport(deliveryReport);

			String notes = Misc.getParamAsString(e.getAttribute("itemNotes"));
			if ("".equalsIgnoreCase(notes)) {
				notes = null;
			}
			bean.setNotes(notes);

			String warrantyTill = e.getAttribute("warrantyTill");
			String dateOfMfg = e.getAttribute("dateOfMfg");
			String dateOfAcquisition = e.getAttribute("dateOfAcquisition");
			try {
				long warrantydate = Misc.getUndefInt();
				long mfgDate = Misc.getUndefInt();
				long acquisitonDate = Misc.getUndefInt();
				if (!"".equals(warrantyTill)) {
					warrantydate = dateFormat.parse(warrantyTill).getTime();
				}
				bean.setWarrantyTill(warrantydate);

				if (!"".equalsIgnoreCase(dateOfMfg)) {
					mfgDate = dateFormat.parse(dateOfMfg).getTime();
				}
				bean.setMfgDate(mfgDate);

				if (!"".equalsIgnoreCase(dateOfAcquisition)) {
					acquisitonDate = dateFormat.parse(dateOfAcquisition).getTime();
				}
				bean.setAcquisitionDate(acquisitonDate);

			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			dataList.add(bean);

		}
		return dataList;

	}

	public void saveNewStocksData(String xmlData) throws GenericException {
		Connection conn = m_session.getConnection();;
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		InventoryMngBean bean = null;
		List<InventoryMngBean> dataList = parseStockData(xmlData);
		boolean updateStock = false;
		boolean yetReleased = false;
		int size = Misc.getUndefInt();
		if (dataList != null) {
			size = dataList.size();
			updateStock = !Misc.isUndef(dataList.get(0).getId());
			
		}
		// insert into
		// inventory_product_detail(item_code,lot_number,qty,price,age,manufacturer,supplier,purchase_receive,delivery_report,notes,warrenty_date,mfg_date,acquisition_date)
		// values
		// (2222,4,500,45,'45','bk','bk','dd','notes','2013-02-21','2013-02-01','2013-02-22')
		if (size > 0) {
			try {
			
                if (!updateStock) {
                	ps = conn.prepareStatement(DBQueries.InventoryMng.INSERT_NEW_STOCK_DATA);
                	ps1 = conn.prepareStatement(DBQueries.InventoryMng.ADD_STOCK_HISTORY_DATA);
				}else {
					ps = conn.prepareStatement(DBQueries.InventoryMng.UPDATE_STOCK_RELEASED_DATA);
				}
		//		ps = conn.prepareStatement(!updateStock ? DBQueries.InventoryMng.INSERT_NEW_STOCK_DATA : DBQueries.InventoryMng.UPDATE_STOCK_DATA);
				for (int i = 0; i < size; i++) {
					bean = dataList.get(i);
			           if (updateStock) {
//inventory_product_id = ?,lot_number = ?,price = ?,age = ?,manufacturer = ?,supplier = ?,purchase_receive = ?,
			        	   //delivery_report = ?,notes = ?,warrenty_date = ?,mfg_date = ?,acquisition_date = ?,updated_on = ?,qty_1 = ? where id = ?			        	   
						//	ps.setInt(1, bean.getProductId());
							ps.setString(1, bean.getLotNumber());
							ps.setDouble(2, bean.getPrice());
							ps.setInt(3, bean.getAge());
							ps.setString(4, bean.getManufacturer());
							ps.setString(5, bean.getSupplier());
							ps.setString(6, bean.getPurchaseReceiver());
							ps.setString(7, bean.getDeliveryReport());
							ps.setString(8, bean.getNotes());
							ps.setTimestamp(9, new Timestamp(bean.getWarrantyTill()));
							ps.setTimestamp(10, new Timestamp(bean.getMfgDate()));
							ps.setTimestamp(11, new Timestamp(bean.getAcquisitionDate()));
							ps.setTimestamp(12, new Timestamp(System.currentTimeMillis()));
							ps.setInt(13, bean.getId());
							ps.addBatch();
				
					}else{
				    int qtyToAdd = bean.getQuantaty();
				    int productId = bean.getProductId();
					ps.setInt(1, productId);
					ps.setString(2, bean.getLotNumber());
					ps.setInt(3, qtyToAdd);
					ps.setDouble(4, bean.getPrice());
					ps.setInt(5, bean.getAge());
					ps.setString(6, bean.getManufacturer());
					ps.setString(7, bean.getSupplier());
					ps.setString(8, bean.getPurchaseReceiver());
					ps.setString(9, bean.getDeliveryReport());
					ps.setString(10, bean.getNotes());
					ps.setTimestamp(11, new Timestamp(bean.getWarrantyTill()));
					ps.setTimestamp(12, new Timestamp(bean.getMfgDate()));
					ps.setTimestamp(13, new Timestamp(bean.getAcquisitionDate()));
					ps.setTimestamp(14, new Timestamp(System.currentTimeMillis()));
					ps.setInt(15, bean.getQuantaty());
					ps.addBatch();
					//product_detail_id,qty_to_added,cummulative_qty_added,qty_to_release,cummulative_qty_released,created_on
					ps1.setInt(1, productId);
					ps1.setInt(2, qtyToAdd);
					ps1.setLong(3, 0);
					ps1.setTimestamp(4,new Timestamp(System.currentTimeMillis()));
					ps1.addBatch();
					}
				}
				ps.executeBatch();
				if (!updateStock) {
					ps1.executeBatch();	
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try{
				if (ps != null) {
						ps.close();
				}
				if (ps1 != null) {
					ps1.close();
			}
				}catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public List<InventoryMngBean> getInventoryList() throws GenericException {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();
		try {
			int catId = Misc.getParamAsInt(m_session.getParameter("categoryCode"),Misc.getUndefInt());
			String itemName = Misc.getParamAsString(m_session.getParameter("itemName"),null);
			String itemCode = Misc.getParamAsString(m_session.getParameter("itemCode"),null);
			int portNodeId = Misc.getParamAsInt(m_session.getParameter("pv123"),Misc.G_TOP_LEVEL_PORT);
			conn = m_session.getConnection();
			StringBuilder query = new StringBuilder(InventoryMng.GET_INVENTORY_PRODUCT_DATA);
			StringBuilder whereClause = new StringBuilder();
			if (!Misc.isUndef(catId) && catId >= 0) {
				if(whereClause.length() > 0)
					whereClause.append(" and ");
				whereClause.append(" categoryId = ").append(catId);
				m_session.setAttribute("categoryCode", catId+"", false);
			}
			if (itemCode != null && itemCode.length() > 0) {
				if(whereClause.length() > 0)
					whereClause.append(" and ");
				whereClause.append(" inventory_product.item_code like '%").append(itemCode).append("%' ");
				m_session.setAttribute("itemCode", itemCode, false);
			}
			if (itemName != null && itemName.length() > 0) {
				if(whereClause.length() > 0)
					whereClause.append(" and ");
				whereClause.append(" item_name like '%").append(itemName).append("%' ");
				m_session.setAttribute("itemName", itemName, false);
			}
			if(whereClause.length() > 0)
				query.append(" where ").append(whereClause.toString());
			m_session.setAttribute("pv123", portNodeId+"", false);
			query.append("  group by inventory_product.item_code,inventory_product.categoryId  order by inventory_product.categoryId,inventory_product.item_name,inventory_product.item_code");
			ps = conn.prepareStatement(query.toString());
			ps.setInt(1, portNodeId);
			rs = ps.executeQuery();
			while (rs.next()) {
				InventoryMngBean bean = new InventoryMngBean();
				bean.setCategoryId(rs.getInt(1));
				bean.setItemName(rs.getString(2));
				bean.setItemCode(rs.getString(3));
				bean.setQuantaty(rs.getInt(4));
				bean.setProductId(rs.getInt(5));
				bean.setNotes(rs.getString(6));
				bean.setProductLife(rs.getInt(7));
				bean.setProductLifeUnit(rs.getInt(8));
				dataList.add(bean);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dataList;

	}

	public List<InventoryMngBean> getDistinctItemCode() throws GenericException {
		List<InventoryMngBean> itemCode = new ArrayList<InventoryMngBean>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		InventoryMngBean bean = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.InventoryMng.GET_DISTINCT_ITEM_CODE);
			rs = ps.executeQuery();
			while (rs.next()) {
				bean = new InventoryMngBean();
				bean.setProductId(rs.getInt(1));
				bean.setItemCode(rs.getString(2));
				itemCode.add(bean);
			}
			return itemCode;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<InventoryMngBean> getDistinctItemCode(String productIds) throws GenericException {
		List<InventoryMngBean> itemCode = new ArrayList<InventoryMngBean>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		InventoryMngBean bean = null;
		try {
			conn = m_session.getConnection();
			StringBuilder query = new StringBuilder(DBQueries.InventoryMng.GET_DISTINCT_ITEM_CODE); 
			
			if (productIds != null && productIds.length() > 0) {
			 String[] pIds = productIds.split(",");
			 query.append(" where id in (");
			 Misc.convertInListToStr(pIds, query);
			 query.append(")");
			}
			ps = conn.prepareStatement(query.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				bean = new InventoryMngBean();
				bean.setProductId(rs.getInt(1));
				bean.setItemCode(rs.getString(2));
				itemCode.add(bean);
			}
			return itemCode;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public ArrayList<InventoryMngBean> searchDetailData(int productId, String lotNumber, String mfgName, String supplierName, String firstDate, String secondDate) throws GenericException{
		ArrayList<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean lotNumberPresent = false;
		boolean mfgNamePresent = false;
		boolean supplierNamePresent = false;
		boolean firstDatePresent = false;
		boolean secondDatePresent = false;

		SimpleDateFormat dateFormat = new SimpleDateFormat(Misc.G_DEFAULT_DATE_FORMAT);
		try {
			conn = m_session.getConnection();
			StringBuilder query = new StringBuilder(DBQueries.InventoryMng.GET_INVENTORY_DETAIL_DATA);
			query.append(" where ");
			if (lotNumber != null && lotNumber.length() > 0) {
				lotNumberPresent = true;
			}
			if (mfgName != null && mfgName.length() > 0) {
				mfgNamePresent = true;
			}
			if (supplierName != null && supplierName.length() > 0) {
				supplierNamePresent = true;
			}
			if (firstDate != null && firstDate.length() > 0) {
				firstDatePresent = true;
			}
			if (secondDate != null && secondDate.length() > 0) {
				secondDatePresent = true;
			}

			if (lotNumberPresent) {
				query.append("lot_number like '%" + lotNumber + "%' and ");
			}
			if (mfgNamePresent) {
				query.append("inventory_product_detail.manufacturer like '%" + mfgName + "%' and ");
			}
			if (supplierNamePresent) {
				query.append(" supplier like '%" + supplierName + "%' and ");
			}
			if (firstDatePresent && secondDatePresent) {
				query.append(" inventory_product_detail.created_on between '" + new Timestamp(dateFormat.parse(firstDate).getTime()) + "' and '"
						+ new Timestamp(dateFormat.parse(secondDate).getTime()) + "' and ");
			}
			if (firstDatePresent && !secondDatePresent) {
				query.append(" inventory_product_detail.created_on >= '" + new Timestamp(dateFormat.parse(firstDate).getTime()) + "' and ");
			}
			if (!firstDatePresent && secondDatePresent) {
				query.append(" inventory_product_detail.created_on <= '" + new Timestamp(dateFormat.parse(secondDate).getTime()) + "' and ");
			}

			query.append(" inventory_product_id= " + productId);

			ps = conn.prepareStatement(query.toString());
			rs = ps.executeQuery();
			while (rs.next()) {
				InventoryMngBean bean = new InventoryMngBean();

				bean.setLotNumber(rs.getString(1));
				bean.setQuantaty(rs.getInt(2));
				bean.setPrice(rs.getDouble(3));
				bean.setAge(rs.getInt(4));
				bean.setManufacturer(Misc.getParamAsString(rs.getString(5)));
				bean.setSupplier(Misc.getParamAsString(rs.getString(6)));
				bean.setPurchaseReceiver(Misc.getParamAsString(rs.getString(7)));
				bean.setDeliveryReport(Misc.getParamAsString(rs.getString(8)));
				bean.setNotes(Misc.getParamAsString(rs.getString(9)));
				rs.getTimestamp(10);
				if (rs.wasNull()) {
					bean.setWarrantyTill(Misc.getUndefInt());
				} else
					bean.setWarrantyTill(rs.getTimestamp(10).getTime());

				rs.getTimestamp(11);
				if (rs.wasNull()) {
					bean.setMfgDate(Misc.getUndefInt());
				} else
					bean.setMfgDate(rs.getTimestamp(11).getTime());

				rs.getTimestamp(12);
				if (rs.wasNull()) {
					bean.setAcquisitionDate(Misc.getUndefInt());
				} else
					bean.setAcquisitionDate(rs.getTimestamp(12).getTime());

				rs.getTimestamp(13);
				if (rs.wasNull()) {
					bean.setCreatedOn(Misc.getUndefInt());
				} else
					bean.setCreatedOn(rs.getTimestamp(13).getTime());

				bean.setItemName(Misc.getParamAsString(rs.getString(14)));
				bean.setId(rs.getInt(15));
				bean.setItemCode(rs.getString(16));
				dataList.add(bean);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dataList;
	}

	public ArrayList<InventoryMngBean> getInventoryDetailDataList(String itemCode, int productId, String[] stockIds) throws GenericException {
		ArrayList<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			conn = m_session.getConnection();
			StringBuilder query = new StringBuilder(DBQueries.InventoryMng.GET_INVENTORY_DETAIL_DATA);
			if (stockIds != null && Misc.isUndef(productId)) {

				query.append(" where inventory_product_detail.id in(");
				Misc.convertInListToStr(stockIds, query);
				query.append(")");
			} else if (stockIds == null && !Misc.isUndef(productId)) {

				query.append(" where inventory_product_id = ?");
			}
			ps = conn.prepareStatement(query.toString());
			if (stockIds == null && !Misc.isUndef(productId)) {
				ps.setInt(1, productId);
			}
			rs = ps.executeQuery();
			while (rs.next()) {
				InventoryMngBean bean = new InventoryMngBean();

				
				bean.setLotNumber(rs.getString(1));
				bean.setQuantaty(rs.getInt(2));
				bean.setPrice(rs.getDouble(3));
				bean.setAge(rs.getInt(4));
				bean.setManufacturer(Misc.getParamAsString(rs.getString(5)));
				bean.setSupplier(Misc.getParamAsString(rs.getString(6)));
				bean.setPurchaseReceiver(Misc.getParamAsString(rs.getString(7)));
				bean.setDeliveryReport(Misc.getParamAsString(rs.getString(8)));
				bean.setNotes(Misc.getParamAsString(rs.getString(9)));
				rs.getTimestamp(10);
				if (rs.wasNull()) {
					bean.setWarrantyTill(Misc.getUndefInt());
				} else
					bean.setWarrantyTill(rs.getTimestamp(10).getTime());

				rs.getTimestamp(11);
				if (rs.wasNull()) {
					bean.setMfgDate(Misc.getUndefInt());
				} else
					bean.setMfgDate(rs.getTimestamp(11).getTime());

				rs.getTimestamp(12);
				if (rs.wasNull()) {
					bean.setAcquisitionDate(Misc.getUndefInt());
				} else
					bean.setAcquisitionDate(rs.getTimestamp(12).getTime());

				rs.getTimestamp(13);
				if (rs.wasNull()) {
					bean.setCreatedOn(Misc.getUndefInt());
				} else
					bean.setCreatedOn(rs.getTimestamp(13).getTime());

				bean.setItemName(Misc.getParamAsString(rs.getString(14)));
				bean.setId(rs.getInt(15));
				bean.setItemCode(rs.getString(16));
				bean.setInitialQty(rs.getInt(17));				
                bean.setYetReleased(rs.getInt(18)== 1);
				dataList.add(bean);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return dataList;
	}

	public InventoryMngBean getProductDetail(int productId) throws GenericException{
		InventoryMngBean bean = new InventoryMngBean();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = m_session.getConnection();
			ps = conn.prepareStatement(DBQueries.InventoryMng.GET_PRODUCT_DETAIL_DATA);
			ps.setInt(1, productId);
			rs = ps.executeQuery();
			while (rs.next()) {
				// id,
				// categoryId,item_code,item_name,manufacturer,manufacturer_code,notes
				// from inventory_product
				bean.setProductId(rs.getInt(1));
				bean.setCategoryId(rs.getInt(2));
				bean.setItemCode(rs.getString(3));
				bean.setItemName(rs.getString(4));
				bean.setManufacturer(rs.getString(5));
				bean.setManufacturerCode(rs.getString(6));
				bean.setNotes(rs.getString(7));
				bean.setProductLife(rs.getInt(8));
				bean.setProductLifeUnit(rs.getInt(9));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return bean;
	}
	
	public List<Triple<Integer, String, InventoryMngBean>> getInventoryDataToRelease(String productIds) throws GenericException {
		List<Triple<Integer, String, InventoryMngBean>> dataList = new ArrayList<Triple<Integer, String, InventoryMngBean>>();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		String[] pIds = null;
		try {
			if (productIds != null && productIds.length() > 0) {
				conn = m_session.getConnection();
				StringBuilder query = new StringBuilder(DBQueries.InventoryMng.GET_INVENTORY_DATA_To_RELEASE);
				pIds = productIds.split(",");
				Misc.convertInListToStr(pIds, query);
				query.append(" )");
				query.append(" order by inventory_product_id");
				ps = conn.prepareStatement(query.toString());
				rs = ps.executeQuery();
				while (rs.next()) {
					InventoryMngBean bean = new InventoryMngBean();
					bean.setId(rs.getInt(1));
					int inventoryProductId = rs.getInt(2);
					bean.setProductId(inventoryProductId);
					bean.setLotNumber(rs.getString(3));
					bean.setQuantaty(rs.getInt(4));
					bean.setAge(rs.getInt(5));
					bean.setPrice(rs.getDouble(6));
					String itemName = rs.getString(7);
					bean.setItemName(itemName);
					bean.setCreatedOn(rs.getTimestamp(8).getTime());
					Triple<Integer, String, InventoryMngBean> triplet = new Triple<Integer, String, InventoryMngBean>(inventoryProductId, itemName, bean);
					dataList.add(triplet);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (ps != null) {
					ps.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return dataList;

	}
	
	public void saveReleaseStockData(String xmlData) throws GenericException{
		if (xmlData != null) {
			List<InventoryMngBean> dataList = parseReleaseStockData(xmlData);
			Connection conn = null;
			PreparedStatement ps = null;
			PreparedStatement ps1 = null;
			PreparedStatement ps2 = null;
			conn = m_session.getConnection();
			boolean execute = false;
				try {
				ps = conn.prepareStatement(DBQueries.InventoryMng.INSERT_RELEASE_DATA);
				ps1 = conn.prepareStatement(DBQueries.InventoryMng.ADD_STOCK_HISTORY_DATA);
				ps2 = null;//DEBUG13 conn.prepareStatement(DBQueries.InventoryMng.UPDATE_STOCK_QTY);
				int size = dataList.size();
				for (int i = 0; i < size; i++) {
					InventoryMngBean bean = dataList.get(i);
					int qtyToRelease = bean.getQuantaty();
					int productId = bean.getProductId();
					if(Misc.isUndef(qtyToRelease))
						continue;
					Misc.setParamInt(ps, bean.getProductId(), 1);
					Misc.setParamInt(ps, qtyToRelease, 2);
					ps.setString(3, bean.getTicketId());
					//prodcut_detail_id,qtytorelease,itemPurchase,deliveryReport,notes,released_on
					ps.setString(4, bean.getPurchaseReceiver());
					ps.setString(5, bean.getDeliveryReport());
					ps.setString(6,bean.getNotes());
					ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
					//product_detail_id,qty_to_added,cummulative_qty_added,qty_to_release,cummulative_qty_released,created_on
					Misc.setParamInt(ps1, productId, 1);
					Misc.setParamInt(ps1, 0, 2);
					Misc.setParamInt(ps1, qtyToRelease, 3);
					ps1.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
					ps.addBatch();
					ps1.addBatch();
					/*Misc.setParamInt(ps2,qtyToRelease, 1);
					Misc.setParamInt(ps2, bean.getId(), 2);
					ps2.addBatch();*/
					execute = true;
				}
				if (execute) {
					ps.executeBatch();
					ps1.executeBatch();
					//ps2.executeBatch();
				}
				
			} catch (Exception e) {
              e.printStackTrace();
			}finally{
				try{
				if (ps != null) {
					ps.close();
				}
				if (ps1 != null) {
					ps1.close();
				}
				}catch(Exception e2){
				e2.printStackTrace();
				
				}
				
			}
			
			
			
		}
		
	}
	
	public List<InventoryMngBean> parseReleaseStockData(String xmlData) {
		List<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();

		org.w3c.dom.Document xmlDoc = com.ipssi.gen.utils.MyXMLHelper.loadFromString(xmlData);
		org.w3c.dom.NodeList nList = xmlDoc.getElementsByTagName("MainTag");
		int size = nList.getLength();

		for (int i = 0; i < size; i++) {
			InventoryMngBean bean = new InventoryMngBean();
			org.w3c.dom.Node n = nList.item(i);
			org.w3c.dom.Element e = (org.w3c.dom.Element) n;

            int productId = Misc.getParamAsInt(e.getAttribute("productId"));
            bean.setProductId(productId);
			int Id = Misc.getParamAsInt(e.getAttribute("lotNumber"));
			bean.setId(Id);

			
			int  quantityRelease = Misc.getParamAsInt(e.getAttribute("qtyToRelease"));
			bean.setQuantaty(quantityRelease);
			bean.setTicketId(Misc.getParamAsString(e.getAttribute("ticketId")));
      
			String itemPurchase = Misc.getParamAsString(e.getAttribute("itemPurchase"));
			if ("".equalsIgnoreCase(itemPurchase)) {
				itemPurchase = null;
			}
			bean.setPurchaseReceiver(itemPurchase);

			String deliveryReport = Misc.getParamAsString(e.getAttribute("deliveryReport"));
			if ("".equalsIgnoreCase(deliveryReport)) {
				deliveryReport = null;
			}
			bean.setDeliveryReport(deliveryReport);

			String notes = Misc.getParamAsString(e.getAttribute("itemNotes"));
			if ("".equalsIgnoreCase(notes)) {
				notes = null;
			}
			bean.setNotes(notes);
			dataList.add(bean);

		}
		return dataList;

	}

public List<InventoryMngBean> getStockAcqReportData(int catId, String itemCode,String startDate,String endDate) throws GenericException{
	List<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	boolean whereClause = false;
	boolean catIdPresent = false;
	boolean itemCodePresent = false;
	boolean startDatePresent = false;
	boolean endDatePresent = false;
	try {
		StringBuilder query = new StringBuilder(DBQueries.InventoryMng.STOCK_ACQ_REPORT_DATA);
		if (catId >= 0) {
			catIdPresent = true;
		}
		if (itemCode != null && itemCode.length() >0) {
			itemCodePresent = true;
		}
		if (startDate != null && startDate.length() >0) {
			startDatePresent = true;
		}
		if (endDate != null && endDate.length() >0) {
			endDatePresent = true;
		}
		if (catIdPresent || itemCodePresent ||startDatePresent ||endDatePresent ) {
			whereClause = true;
			query.append(" where ");
		}
		if (catIdPresent) {
			query.append("categoryId = "+catId +" and ");
		}
		if (itemCodePresent) {
			query.append(" inventory_product.item_code like '%"+itemCode +"%' and ");
		}
		if (startDatePresent && endDatePresent) {
			query.append(" acquisition_date between '"+new Timestamp(dateFormat.parse(startDate).getTime()) +"' and '"+new Timestamp(dateFormat.parse(endDate).getTime())+"' and "); 
		}
		if (startDatePresent && !endDatePresent) {
			query.append(" acquisition_date > '"+new Timestamp(dateFormat.parse(startDate).getTime())+"' and "); 
		}
		if (!startDatePresent && endDatePresent) {
			query.append(" acquisition_date <= '"+new Timestamp(dateFormat.parse(endDate).getTime())+"' and "); 
		}
		if (whereClause) {
			query.append(" 1=1 ");	
		}
		query.append(" order by acquisition_date ");
		System.out.println("#### query ::"+query.toString());
		conn = m_session.getConnection();
		ps = conn.prepareStatement(query.toString());
		rs = ps.executeQuery();
		while (rs.next()) {
			InventoryMngBean bean = new InventoryMngBean();
			 bean.setId(rs.getInt(1));
			 bean.setItemCode(rs.getString(2));
			 bean.setItemName(rs.getString(3));
			 bean.setQuantaty(rs.getInt(4));
			 bean.setCreatedOn(rs.getTimestamp(5).getTime());
			 bean.setCategoryId(rs.getInt(6));
			 bean.setAcquisitionDate(rs.getTimestamp(7).getTime());
			 bean.setLotNumber(rs.getString(8));
			 dataList.add(bean);
			
		}
		
	} catch (Exception e) {
      e.printStackTrace();
	}finally{
		try{
		if (ps != null) {
			ps.close();
		}
		if (rs != null) {
			rs.close();
		}
		}catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	return dataList;
}
public List<InventoryMngBean> getStockReleaseData(int catId,String itemCode,String startDate, String endDate){
	List<InventoryMngBean> dataList = new ArrayList<InventoryMngBean>();
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	boolean whereClause = false;
	boolean catIdPresent = false;
	boolean itemCodePresent = false;
	boolean startDatePresent = false;
	boolean endDatePresent = false;
 
	try {
		StringBuilder query = new StringBuilder(DBQueries.InventoryMng.STOCK_RELEASE_REPORT_DATA);
		if (catId >= 0) {
			catIdPresent = true;
		}
		if (itemCode != null && itemCode.length() >0) {
			itemCodePresent = true;
		}
		if (startDate != null && startDate.length() >0) {
			startDatePresent = true;
		}
		if (endDate != null && endDate.length() >0) {
			endDatePresent = true;
		}
		if (catIdPresent || itemCodePresent ||startDatePresent ||endDatePresent ) {
			whereClause = true;
			query.append(" where ");
		}
		if (catIdPresent) {
			query.append("categoryId = "+catId +" and ");
		}
		if (itemCodePresent) {
			query.append(" inventory_product.item_code like '%"+itemCode +"%' and ");
		}
		if (startDatePresent && endDatePresent) {
			query.append(" released_on between '"+new Timestamp(dateFormat.parse(startDate).getTime()) +"' and '"+new Timestamp(dateFormat.parse(endDate).getTime())+"' and "); 
		}
		if (startDatePresent && !endDatePresent) {
			query.append(" released_on > '"+new Timestamp(dateFormat.parse(startDate).getTime())+"' and "); 
		}
		if (!startDatePresent && endDatePresent) {
			query.append(" released_on <= '"+new Timestamp(dateFormat.parse(endDate).getTime())+"' and "); 
		}
		if (whereClause) {
			query.append(" 1=1 ");	
		}
		
		query.append(" order by released_on ");
		System.out.println("#### query ::"+query.toString());
		conn = m_session.getConnection();
		ps = conn.prepareStatement(query.toString());
		rs = ps.executeQuery();
		while (rs.next()) {
			InventoryMngBean bean = new InventoryMngBean();
		
			 bean.setCreatedOn(rs.getTimestamp(4).getTime());
			 bean.setCategoryId(rs.getInt(1));
			 bean.setQuantaty(rs.getInt(3));
			 bean.setItemCode(rs.getString(5));
			 bean.setItemName(rs.getString(6));
			 bean.setLotNumber(rs.getString(7));
			 dataList.add(bean);
			
		}
		
		
	} catch (Exception e) {
		// TODO: handle exception
	}
	return dataList;
}

public List<String> getItemCodeBasedOnCategory(int catId) throws GenericException{
	List<String> dataList = new ArrayList<String>();
	Connection conn = null;
	PreparedStatement ps = null;
	ResultSet rs = null;
	try {
		conn = m_session.getConnection();
		ps = conn.prepareStatement("select item_code from inventory_product where categoryId= ?");
		ps.setInt(1, catId);
		rs = ps.executeQuery();
		while (rs.next()) {
			dataList.add(rs.getString(1));
			
		}
	} catch (Exception e) {
	 e.printStackTrace();
	}finally{
		try {
			if (ps != null) {
				ps.close();
			}
			if (rs != null) {
				rs.close();
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	return dataList;
}
}
