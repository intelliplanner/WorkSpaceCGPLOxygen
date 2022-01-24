package com.ipssi.modeler;

public class DBQueries {
	public static final String CLEAN_TEMP_ATTRIB_WITH_MODEL = "delete from temp_attrib_with_model";
	public static final String INSERT_IN_TEMP_ATTRIB_WITH_MODEL = "insert into temp_attrib_with_model (askdim_id, modeldim_id, getorder) values (?,?,?)";
	public static final String INSERT_MISSING_VEHICLE_MODEL = "insert into model_state (vehicle_id, attribute_id) (select distinct vehicle.id, temp_attrib_with_model.modeldim_id from vehicle join temp_attrib_with_model on (vehicle.status = 1) "+
	                                                                " left outer join model_state on (vehicle.id = model_state.vehicle_id and temp_attrib_with_model.modeldim_id = model_state.attribute_id) where model_state.vehicle_id is null) "
																	;
	public static final String GET_MODEL_STATE = "select vehicle_id, attribute_id, firstpoint, saved_at, object from model_state ";
	public static final String GET_GPS_DATA_REDO_1 = "select logged_data.vehicle_id, longitude, latitude, attribute_id, attribute_value, gps_record_time, source, name,updated_on,speed,gps_id from "+
	                                                                                  " (select model_state.vehicle_id, temp_attrib_with_model.askdim_id dim_id, min(model_state.firstpoint) ts, temp_attrib_with_model.getorder from model_state join temp_attrib_with_model on (model_state.attribute_id = temp_attrib_with_model.modeldim_id) ";
	public static final String GET_GPS_DATA_REDO_2 = ") need "+
	                                                                                  " join logged_data on (logged_data.vehicle_id = need.vehicle_id and logged_data.attribute_id = need.dim_id and logged_data.gps_record_time >= need.ts) order by logged_data.vehicle_id, logged_data.gps_record_time, need.getorder, logged_data.attribute_id "
	                                                                                  ; // 
	public static final String NEW_UPDATE_DATA_INFO = "update logged_data set attribute_value=?, speed=? where vehicle_id=? and gps_record_time=? and attribute_id=?";//must match DP.DBQueries.NEW_UPDATE_DATA_INFO
}
