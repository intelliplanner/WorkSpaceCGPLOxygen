package com.ipssi.reporting.trip;

import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.ipssi.android.InspectionQuestion;
import com.itextpdf.text.pdf.codec.Base64;

public class JsonParser {
	public static ArrayList<InspectionQuestion> parseJsonArray(String result) {
		ArrayList<InspectionQuestion> list = new ArrayList<InspectionQuestion>();
		if (result != null && result.length() > 0) {
			try {
				JSONArray array = new JSONArray(result);
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = array.getJSONObject(i);
					int queryId = object.optInt("id");
//					String type = object.optString("type_of_inspection");
					String answer = object.optString("result");
					
					String photo = object.optString("photo");
					String vehicleId = object.optString("inspection_vehicle_detail_id");
				    String explanation=object.optString("explanation");
                    String mandatoryToComplete=object.optString("mandatory_to_complete");
					InspectionQuestion question = new InspectionQuestion();
					question.setExplanation(explanation);
					question.setMandatoryToComplete(mandatoryToComplete);
					question.setQueryId(queryId);
//					question.setObservation(observation);
					question.setPhoto(Base64.decode(photo));
					question.setResult(answer);
//					question.setType(type);
					question.setInspectionVehicleDetailId(vehicleId);
					list.add(question);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return list;
	}
}
