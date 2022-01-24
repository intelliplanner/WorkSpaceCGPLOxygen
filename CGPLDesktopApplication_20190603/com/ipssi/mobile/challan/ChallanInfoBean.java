package com.ipssi.mobile.challan;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.ipssi.gen.exception.GenericException;
import com.ipssi.gen.utils.DBConnectionPool;
import com.ipssi.gen.utils.Misc;
import com.ipssi.gen.utils.MyXMLHelper;

/**
 * Created by ipssi11 on 9/17/2015.
 */
public class ChallanInfoBean {

    LocationInfo source;
    LocationInfo vehicle;
    Date dispatchDate;
    ArrayList<DeliveryInfo> deliveries;
    ArrayList<MaterialInfo> materialInfo;
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public ChallanInfoBean(){

    }
    public static ArrayList<ChallanInfoBean> getChallanInfoList(String xmlStr) {
    	ArrayList<ChallanInfoBean> retval = null;
    	ChallanInfoBean challanInfoBean = null;
        try {
            org.w3c.dom.Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
            org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("challan");
            int length = nList.getLength();
            for ( int i=0; i<length ; i++){
                org.w3c.dom.Node node =  nList.item(i);
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                if(element != null){
                	if(retval == null)
                		retval = new ArrayList<ChallanInfoBean>();
                	challanInfoBean = new ChallanInfoBean();
                	load(element,challanInfoBean);
                	retval.add(challanInfoBean);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return retval;
    }
    public static StringBuilder getChallanInfoListXML(ArrayList<ChallanInfoBean> challanInfoList){
    	StringBuilder retval = null;
    	try {
            if(challanInfoList != null  && challanInfoList.size() > 0){
            	
            	for(ChallanInfoBean challan : challanInfoList){
            		if(retval == null){
            			retval = new StringBuilder();
            			retval.append("<root>");
            		}
            		retval.append(challan.toXMl().toString());
            	}
            	retval.append("</root>");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    	return retval;
    }

    public ChallanInfoBean(String xmlStr) {

        try {
            org.w3c.dom.Document xmlDoc = MyXMLHelper.loadFromString(xmlStr);
            org.w3c.dom.NodeList  nList= xmlDoc.getElementsByTagName("challan");
            org.w3c.dom.Node node =  nList.item(0);
            org.w3c.dom.Element element = (org.w3c.dom.Element) node;
            load(element,this);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void load(org.w3c.dom.Element element,ChallanInfoBean challanBean) {
        if (element != null) {
        	challanBean.source = new LocationInfo(Misc.getParamAsInt(element.getAttribute("source_id")), Misc.getParamAsString(element.getAttribute("source_name")), Misc.getParamAsDouble(element.getAttribute("source_lat")), Misc.getParamAsDouble(element.getAttribute("source_lon")));
        	challanBean.vehicle = new LocationInfo(Misc.getParamAsInt(element.getAttribute("vehicle_id")), Misc.getParamAsString(element.getAttribute("vehicle_name")), Misc.getParamAsDouble(element.getAttribute("vehicle_lat")), Misc.getParamAsDouble(element.getAttribute("vehicle_lon")));
            String dateStr = Misc.getParamAsString(element.getAttribute("date"));
            if(dateStr != null)
                try {
                	challanBean.dispatchDate = sdf.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            //to do for date
            challanBean.setDeliveries(challanBean.getDeliveries(element));
        }
    }
    public ArrayList<DeliveryInfo> getDeliveries(org.w3c.dom.Element element){
        ArrayList<DeliveryInfo> retval = null;
        DeliveryInfo deliveryInfo = null;
        if(element != null){
            org.w3c.dom.NodeList  deliveryNodes = element.getElementsByTagName("delivery");
            if(deliveryNodes != null && deliveryNodes.getLength() > 0){
                int deliverySize = deliveryNodes.getLength();
                for(int j=0;j<deliverySize;j++){
                    org.w3c.dom.Node deliveryNode =  deliveryNodes.item(j);
                    org.w3c.dom.Element deliveryElement = (org.w3c.dom.Element) deliveryNode;
                    if(deliveryElement != null){
                        if(retval == null)
                            retval = new ArrayList<DeliveryInfo>();
                        deliveryInfo = new DeliveryInfo(new LocationInfo(Misc.getParamAsInt(deliveryElement.getAttribute("id")), Misc.getParamAsString(deliveryElement.getAttribute("name")), Misc.getParamAsDouble(deliveryElement.getAttribute("lat")), Misc.getParamAsDouble(deliveryElement.getAttribute("lon"))));
                        deliveryInfo.setChallanId(Misc.getParamAsInt(deliveryElement.getAttribute("challan_id")));
                        deliveryInfo.setItems(getMaterialInfo(deliveryElement));
                        retval.add(deliveryInfo);
                    }

                }
            }
        }
        return retval;
    }
    public ArrayList<MaterialInfo> getMaterialInfo(org.w3c.dom.Element element){
        ArrayList<MaterialInfo> retval = null;
        MaterialInfo materialInfo = null;
        if(element != null){
            org.w3c.dom.NodeList  mateNodes = element.getElementsByTagName("item");
            if(mateNodes != null && mateNodes.getLength() > 0){
                int matSize = mateNodes.getLength();
                for(int j=0;j<matSize;j++){
                    org.w3c.dom.Node matNode =  mateNodes.item(j);
                    org.w3c.dom.Element matElement = (org.w3c.dom.Element) matNode;
                    if(matElement != null){
                        if(retval == null)
                            retval = new ArrayList<MaterialInfo>();
                        materialInfo = new MaterialInfo(Misc.getParamAsInt(matElement.getAttribute("id")), Misc.getParamAsString(matElement.getAttribute("code")), Misc.getParamAsDouble(matElement.getAttribute("dispatch_qty")), Misc.getParamAsDouble(matElement.getAttribute("received_qty")), Misc.getParamAsString(matElement.getAttribute("dispatch_notes")));
                        materialInfo.setRecivedNotes(Misc.getParamAsString(matElement.getAttribute("recived_notes")));
                        materialInfo.setRowId(Misc.getParamAsInt(matElement.getAttribute("row_id")));
                        retval.add(materialInfo);
                    }

                }
            }
        }
        return retval;
    }

    public StringBuilder toXMl(){
        StringBuilder retval = new StringBuilder();
        try{
            retval.append("<challan");
            if(source != null){
                retval.append(" source_id=\""+getAdjustedString(source.getId())+"\" ")
                        .append(" source_name=\""+getAdjustedString(source.getName())+"\" ")
                        .append(" source_lat=\""+getAdjustedString(source.getLat())+"\" ")
                        .append(" source_lon=\"" + getAdjustedString(source.getLon()) + "\" ");
            }
            if(vehicle != null){
                retval.append(" vehicle_id=\""+getAdjustedString(vehicle.getId())+"\" ")
                        .append(" vehicle_name=\""+getAdjustedString(vehicle.getName())+"\" ")
                        .append(" vehicle_lat=\""+getAdjustedString(vehicle.getLat())+"\" ")
                        .append(" vehicle_lon=\""+getAdjustedString(vehicle.getLon())+"\" ");
            }
            if(dispatchDate != null){
                retval.append(" date=\""+sdf.format(dispatchDate)+"\" ");
            }
            retval.append(">\n");
            if(deliveries != null && deliveries.size() > 0){
                for(int i=0;i<deliveries.size();i++){
                    retval.append("<delivery")
                            .append(" id=\""+getAdjustedString(deliveries.get(i).getDestination().getId())+"\" ")
                            .append(" name=\"" + getAdjustedString(deliveries.get(i).getDestination().getName()) + "\" ")
                            .append(" lat=\""+getAdjustedString(deliveries.get(i).getDestination().getLat())+"\" ")
                            .append(" lon=\"" + getAdjustedString(deliveries.get(i).getDestination().getLon()) + "\" ")
                    		.append(" challan_id=\"" + getAdjustedString(deliveries.get(i).getChallanId()) + "\" ");
                    retval.append(">\n");
                    if(deliveries.get(i).getItems() != null && deliveries.get(i).getItems().size() > 0){
                        for(int j=0;j<deliveries.get(i).getItems().size();j++){
                            retval.append("<item")
                                    .append(" row_id=\"" + getAdjustedString(deliveries.get(i).getItems().get(j).getRowId()) + "\" ")
                                    .append(" id=\"" + getAdjustedString(deliveries.get(i).getItems().get(j).getId()) + "\" ")
                                    .append(" code=\""+getAdjustedString(deliveries.get(i).getItems().get(j).getCode())+"\" ")
                                    .append(" dispatch_qty=\"" + getAdjustedString(deliveries.get(i).getItems().get(j).getQty()) + "\" ")
                                    .append(" received_qty=\""+getAdjustedString(deliveries.get(i).getItems().get(j).getRecivedQty())+"\" ")
                                    .append(" dispatch_notes=\"" + getAdjustedString(deliveries.get(i).getItems().get(j).getDispatchNotes()) + "\" ")
                                    .append(" recived_notes=\"" + getAdjustedString(deliveries.get(i).getItems().get(j).getRecivedNotes()) + "\" ")
                                    
                                    ;
                            retval.append("/>\n");
                        }
                    }
                    retval.append("</delivery>\n");
                }

            }
            retval.append("</challan>\n");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return retval;
    }

    public static class DeliveryInfo{
    	int challanId = Misc.getUndefInt();
    	LocationInfo destination;
        ArrayList<MaterialInfo> items;

        public DeliveryInfo(LocationInfo destination) {
            this.destination = destination;
        }

        public LocationInfo getDestination() {
            return destination;
        }

        public void setDestination(LocationInfo destination) {
            this.destination = destination;
        }

        public ArrayList<MaterialInfo> getItems() {
            return items;
        }

        public void setItems(ArrayList<MaterialInfo> items) {
            this.items = items;
        }

		public int getChallanId() {
			return challanId;
		}

		public void setChallanId(int challanId) {
			this.challanId = challanId;
		}
        
    }

    public static class MaterialInfo{
    	int rowId=Misc.getUndefInt();
    	int id;
        String code;
        double qty = Misc.getUndefDouble();
        double recivedQty = Misc.getUndefDouble();
        String dispatchNotes;
        String recivedNotes;

        public MaterialInfo(int id, String code, double qty) {
            this.id = id;
            this.code = code;
            this.qty = qty;
        }

        public MaterialInfo(int id, String code, double qty, double recivedQty, String comments) {
            this.id = id;
            this.code = code;
            this.qty = qty;
            this.recivedQty = recivedQty;
            this.dispatchNotes = comments;
        }
        
        public MaterialInfo(int id, String code, double qty, double recivedQty, String dispatchNotes, String recivedNotes) {
            this.id = id;
            this.code = code;
            this.qty = qty;
            this.recivedQty = recivedQty;
            this.dispatchNotes = dispatchNotes;
            this.recivedNotes = recivedNotes;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public double getQty() {
            return qty;
        }

        public void setQty(double qty) {
            this.qty = qty;
        }

        public double getRecivedQty() {
            return recivedQty;
        }

        public void setRecivedQty(double recivedQty) {
            this.recivedQty = recivedQty;
        }

        public String getDispatchNotes() {
            return dispatchNotes;
        }

        public void setDispatchNotes(String dispatchNotes) {
            this.dispatchNotes = dispatchNotes;
        }

        public String getRecivedNotes() {
            return recivedNotes;
        }

        public void setRecivedNotes(String recivedNotes) {
            this.recivedNotes = recivedNotes;
        }

		public int getRowId() {
			return rowId;
		}

		public void setRowId(int rowId) {
			this.rowId = rowId;
		}
    }

    public LocationInfo getSource() {
        return source;
    }

    public void setSource(LocationInfo source) {
        this.source = source;
    }

    public LocationInfo getVehicle() {
        return vehicle;
    }

    public void setVehicle(LocationInfo vehicle) {
        this.vehicle = vehicle;
    }

    public Date getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(Date dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public ArrayList<DeliveryInfo> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(ArrayList<DeliveryInfo> deliveries) {
        this.deliveries = deliveries;
    }
    public static void main(String[] arg){
    	ChallanInfoBean c = new ChallanInfoBean();
    	c.setSource(new LocationInfo(1, "d83, sector 6, noida"));
    	c.setVehicle(new LocationInfo(23455, "HR55AC3344"));
    	
    	MaterialInfo m1 = new MaterialInfo(1, "code1", 1.2);
    	MaterialInfo m2 = new MaterialInfo(2, "code2", 5);
    	MaterialInfo m3 = new MaterialInfo(3, "code3", 6);
    	MaterialInfo m4 = new MaterialInfo(4, "code4", 4);
    	MaterialInfo m5 = new MaterialInfo(5, "code5", 100);
    	MaterialInfo m6 = new MaterialInfo(6, "code6", 12);
    	MaterialInfo m7 = new MaterialInfo(7, "code7", 25);
    	
    	ArrayList<MaterialInfo> itemList1 = new ArrayList<ChallanInfoBean.MaterialInfo>(Arrays.asList( m1,m2,m3));
    	ArrayList<MaterialInfo> itemList2 = new ArrayList<ChallanInfoBean.MaterialInfo>(Arrays.asList( m4,m5));
    	ArrayList<MaterialInfo> itemList3 = new ArrayList<ChallanInfoBean.MaterialInfo>(Arrays.asList( m6,m7));
    	
    	
    	
    	ArrayList<DeliveryInfo> deliveries = new ArrayList<ChallanInfoBean.DeliveryInfo>();
    	DeliveryInfo d1 = new DeliveryInfo(new LocationInfo(1, "c93, sector 2, noida"));
    	d1.setItems(itemList1);
    	DeliveryInfo d2 = new DeliveryInfo(new LocationInfo(2, "c159, sector 15, noida"));
    	d2.setItems(itemList2);
    	DeliveryInfo d3 = new DeliveryInfo(new LocationInfo(3, "c122, sector 122,noida"));
    	d3.setItems(itemList3);
    	deliveries.add(d1);
    	deliveries.add(d2);
    	deliveries.add(d3);
    	c.setDeliveries(deliveries);
    	c.setDispatchDate(new Date());
    	/*System.out.println(c.toXMl());*/
    	ChallanInfoBean c1 = new ChallanInfoBean(c.toXMl().toString());
    	
    	/*System.out.println(c1.toXMl());
    	System.out.println(new LocationInfo( new LocationInfo(1,"test",1.74,2.5).toXMl().toString()).toXMl());*/
    	/*ArrayList<ChallanInfoBean> cl1 = new ArrayList<ChallanInfoBean>(Arrays.asList(c,c,c,c));
    	ArrayList<ChallanInfoBean> cl2 = ChallanInfoBean.getChallanInfoList(ChallanInfoBean.getChallanInfoListXML(cl1).toString());
    	System.out.println(ChallanInfoBean.getChallanInfoListXML(cl1));
    	System.out.println("###########################################");
    	System.out.println(ChallanInfoBean.getChallanInfoListXML(cl2));*/
    	Connection conn = null;
    	boolean destroyIt = false;
    	try{
    		conn = DBConnectionPool.getConnectionFromPoolNonWeb();
    		ArrayList<LocationInfo> loc1 =  ChallanInfoDao.getVehicle(conn, 628);
    		System.out.println(LocationInfo.getLocationInfoListXML(loc1));
    		ArrayList<LocationInfo> source =  ChallanInfoDao.getSource(conn, 628);
    		ArrayList<LocationInfo> destination =  ChallanInfoDao.getDestination(conn, 628);
    		ArrayList<ChallanInfoBean> challanList = ChallanInfoDao.getChallan(conn, 628, 10,Misc.getUndefInt(),Misc.getUndefInt());
    		System.out.println(challanList.get(0).toXMl().toString());
    		//ChallanInfoDao.insertChallan(conn, c1);
    	}catch(Exception ex){
    		destroyIt = true;
    		ex.printStackTrace();
    	}finally{
    		try {
				DBConnectionPool.returnConnectionToPoolNonWeb(conn,destroyIt);
			} catch (GenericException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    private static String getAdjustedString(String s) {
        if (s != null) {
            s = s.trim();
           /* s = s.replaceAll("@", "@@");
            s = s.replaceAll(",", "@comma");
            s = s.replaceAll("#", "@hash");*/
        }
        else {
            s = "";
        }
        return s;
    }
    private static String getAdjustedString(int v) {
        return Misc.isUndef(v) ? "" : Integer.toString(v);
    }
    private static String getAdjustedString(SimpleDateFormat sdf, long v) {//assumed to be date
        String s = "";
        if (v > 0) {
            Date dt = new Date(v);
            s = sdf.format(dt);
        }
        return s;
    }
    private static String getAdjustedString(double v) {
        return Misc.isUndef(v) ? "" : Double.toString(v);
    }
    private static String getAdjustedString(boolean v) {
        return v ? "1" : "0";
    }
    private static String getAdjustedString(SimpleDateFormat sdf, Date dt) {
        return dt == null ? "" : sdf.format(dt);
    }
	public ArrayList<MaterialInfo> getMaterialInfo() {
		return materialInfo;
	}
	public void setMaterialInfo(ArrayList<MaterialInfo> materialInfo) {
		this.materialInfo = materialInfo;
	}
}
