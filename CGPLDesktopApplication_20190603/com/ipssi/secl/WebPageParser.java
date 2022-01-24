package com.ipssi.secl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebPageParser {

    public static void main(String[] args) throws IOException {
        ArrayList<CoalTripInfo> bcclTruckWiseData = getBCCLDataBySONumber("18701011524");
        for(int i=0,is=bcclTruckWiseData == null ? 0 :bcclTruckWiseData.size();i<is;i++){
        	System.out.println(bcclTruckWiseData.get(i));
        }
        ArrayList<CoalTripInfo> eclTruckWiseData = getECLDataBySONumber("2017098847");
        for(int i=0,is=eclTruckWiseData == null ? 0 :eclTruckWiseData.size();i<is;i++){
        	System.out.println(eclTruckWiseData.get(i));
        }
    }
    public static final SimpleDateFormat eclDateFormat = new SimpleDateFormat("dd-MMM-yy");
    public static ArrayList<CoalTripInfo> getECLDataBySONumber(String soNumber){
    	ArrayList<CoalTripInfo> retval = null;
    	try{
    		Document doc = 
            		Jsoup.connect("http://112.133.239.50:8099/ecl_road_sale/despatch_against_so.php?so_no="+soNumber+"&submit=")
            		.get();
            Element table = doc.select("table#example").get(0); 
            Element t1 = table.children().get(1);
            Elements rows = t1.children();
            for (int i = 0; i < rows.size(); i++) { 
                Element row = rows.get(i);
                Elements cols = row.select("td");
                CoalTripInfo trip = null;
                for(int j=0,js=cols==null?0:cols.size();j<js;j++){
                	Date tripDate = eclDateFormat.parse(cols.get(3).text());
                	if(tripDate == null)
                		continue;
                	trip = new CoalTripInfo(soNumber, cols.get(5).text(), cols.get(1).text(), tripDate, cols.get(2).text(), cols.get(6).text(), Double.parseDouble(cols.get(4).text()));
                }
                if(trip != null){
                	if(retval == null)
                		retval = new ArrayList<CoalTripInfo>();
                	retval.add(trip);
                }
            }
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}finally{
    		if(retval != null)
    			Collections.sort(retval,new CoalTripInfo());
    	}
    	return retval;
    }
    public static final SimpleDateFormat bcclDateFormat = new SimpleDateFormat("dd-MM-yyyy");
    public static ArrayList<CoalTripInfo> getBCCLDataBySONumber(String soNumber){
    	ArrayList<CoalTripInfo> retval = null;
    	try{
    		Response response = 
            		Jsoup.connect("http://www.bcclweb.in/reports/despatch_daily_against_sales_order.php")
            		.data("sales_order_no", soNumber)
            		.data("submit","Search")
            		.method(Method.POST)
            		.execute();
    		Document doc = response.parse();
            Element table = doc.select("table#example").get(0); 
            Element t1 = table.children().get(1);
            Elements rows = t1.children();
            for (int i = 0; i < rows.size(); i++) { 
                Element row = rows.get(i);
                Elements cols = row.select("td");
                CoalTripInfo trip = null;
                for(int j=0,js=cols==null?0:cols.size();j<js;j++){
                	Date tripDate = bcclDateFormat.parse(cols.get(2).text());
                	if(tripDate == null)
                		continue;
                	trip = new CoalTripInfo(soNumber, cols.get(4).text(), cols.get(0).text(), tripDate, cols.get(1).text(), "", Double.parseDouble(cols.get(3).text()));
                }
                if(trip != null){
                	if(retval == null)
                		retval = new ArrayList<CoalTripInfo>();
                	retval.add(trip);
                }
            }
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}finally{
    		if(retval != null)
    			Collections.sort(retval,new CoalTripInfo());
    	}
    	return retval;
    }
    public static class CoalTripInfo implements Comparator<CoalTripInfo>{
    	private String serialNo;
    	private String truckNo;
    	private String consignee;
    	private Date tripDate;
    	private String grade;
    	private String heapId;
    	private double net;
		public CoalTripInfo() {
			super();
		}
		
		public CoalTripInfo(String serialNo, String truckNo, String consignee, Date tripDate, String grade,
				String heapId, double net) {
			super();
			this.serialNo = serialNo;
			this.truckNo = truckNo;
			this.consignee = consignee;
			this.tripDate = tripDate;
			this.grade = grade;
			this.heapId = heapId;
			this.net = net;
		}
		
		public String getSerialNo() {
			return serialNo;
		}

		public void setSerialNo(String serialNo) {
			this.serialNo = serialNo;
		}

		public String getTruckNo() {
			return truckNo;
		}

		public void setTruckNo(String truckNo) {
			this.truckNo = truckNo;
		}

		public String getConsignee() {
			return consignee;
		}

		public void setConsignee(String consignee) {
			this.consignee = consignee;
		}

		public Date getTripDate() {
			return tripDate;
		}

		public void setTripDate(Date tripDate) {
			this.tripDate = tripDate;
		}

		public String getGrade() {
			return grade;
		}

		public void setGrade(String grade) {
			this.grade = grade;
		}

		public String getHeapId() {
			return heapId;
		}

		public void setHeapId(String heapId) {
			this.heapId = heapId;
		}

		public double getNet() {
			return net;
		}

		public void setNet(double net) {
			this.net = net;
		}

		@Override
		public String toString() {
			return "CoalTripInfo [serialNo=" + serialNo + ", truckNo=" + truckNo + ", consignee=" + consignee
					+ ", tripDate=" + (tripDate == null ? "" : bcclDateFormat.format(tripDate)) + ", grade=" + grade + ", heapId=" + heapId + ", net=" + net + "]";
		}

		@Override
		public int compare(CoalTripInfo o1, CoalTripInfo o2) {
			// TODO Auto-generated method stub
			return (int)(o2.getTripDate().getTime() - o1.getTripDate().getTime());
		}
		
    }
}
