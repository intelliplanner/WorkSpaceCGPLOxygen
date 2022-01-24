-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE vehicle_extended (
  [vehicle_id] int NOT NULL DEFAULT '0',
  [capacity_sec] int DEFAULT NULL,
  [registeration_number] varchar(128) DEFAULT NULL,
  [registeration_number_expiry] datetime2(0) NULL DEFAULT NULL,
  [insurance_number] varchar(128) DEFAULT NULL,
  [insurance_number_expiry] datetime2(0) NULL DEFAULT NULL,
  [permit1_number] varchar(128) DEFAULT NULL,
  [permit1_number_expiry] datetime2(0) NULL DEFAULT NULL,
  [permit1_desc] varchar(512) DEFAULT NULL,
  [permit2_number] varchar(128) DEFAULT NULL,
  [permit2_number_expiry] datetime2(0) NULL DEFAULT NULL,
  [permit2_desc] varchar(512) DEFAULT NULL,
  [working_hrs] float DEFAULT NULL,
  [hired_from] int DEFAULT NULL,
  [rental_rate_usage] float DEFAULT NULL,
  [rental_rate_retainer] float DEFAULT NULL,
  [acquisition_date] datetime2(0) NULL DEFAULT NULL,
  [release_date] datetime2(0) NULL DEFAULT NULL,
  [notes] varchar(512) DEFAULT NULL,
  [lov_field1] int DEFAULT NULL,
  [lov_field2] int DEFAULT NULL,
  [lov_field3] int DEFAULT NULL,
  [lov_field4] int DEFAULT NULL,
  [lov_field5] int DEFAULT NULL,
  [lov_field6] int DEFAULT NULL,
  [lov_field7] int DEFAULT NULL,
  [lov_field8] int DEFAULT NULL,
  [lov_field9] int DEFAULT NULL,
  [lov_field10] int DEFAULT NULL,
  [double_field1] float DEFAULT NULL,
  [double_field2] float DEFAULT NULL,
  [double_field3] float DEFAULT NULL,
  [double_field4] float DEFAULT NULL,
  [double_field5] float DEFAULT NULL,
  [double_field6] float DEFAULT NULL,
  [double_field7] float DEFAULT NULL,
  [double_field8] float DEFAULT NULL,
  [double_field9] float DEFAULT NULL,
  [double_field10] float DEFAULT NULL,
  [str_field1] varchar(512) DEFAULT NULL,
  [str_field2] varchar(512) DEFAULT NULL,
  [str_field3] varchar(512) DEFAULT NULL,
  [str_field4] varchar(512) DEFAULT NULL,
  [str_field5] varchar(512) DEFAULT NULL,
  [str_field6] varchar(512) DEFAULT NULL,
  [str_field7] varchar(512) DEFAULT NULL,
  [str_field8] varchar(512) DEFAULT NULL,
  [str_field9] varchar(512) DEFAULT NULL,
  [str_field10] varchar(512) DEFAULT NULL,
  [date_field1] datetime2(0) NULL DEFAULT NULL,
  [date_field2] datetime2(0) NULL DEFAULT NULL,
  [date_field3] datetime2(0) NULL DEFAULT NULL,
  [date_field4] datetime2(0) NULL DEFAULT NULL,
  [date_field5] datetime2(0) NULL DEFAULT NULL,
  [date_field6] datetime2(0) NULL DEFAULT NULL,
  [date_field7] datetime2(0) NULL DEFAULT NULL,
  [date_field8] datetime2(0) NULL DEFAULT NULL,
  [date_field9] datetime2(0) NULL DEFAULT NULL,
  [date_field10] datetime2(0) NULL DEFAULT NULL,
  [plant] int DEFAULT NULL,
  [purpose] int DEFAULT NULL,
  [transporter_id] int DEFAULT NULL,
  [extended_status] int DEFAULT '1',
  [other_vehicle_id] int DEFAULT NULL,
  [str_field11] varchar(512) DEFAULT NULL,
  [mark_for_qc] int DEFAULT NULL,
  [mark_for_qc_reason] varchar(256) DEFAULT NULL,
  [mark_for_gps_repair] int DEFAULT NULL,
  [mark_for_gps_repair_reason] varchar(256) DEFAULT NULL,
  [transporter_code] varchar(20) DEFAULT NULL,
  PRIMARY KEY ([vehicle_id])
) ;
CREATE TABLE users (
  [PJ_ANALYSIS_FILEID] varchar(240) DEFAULT NULL,
  [PJ_STATUS_FILEID] varchar(240) DEFAULT NULL,
  [PASSWORD] varchar(240) DEFAULT NULL,
  [PRT_STATUS_FILEID] varchar(240) DEFAULT NULL,
  [NAME] varchar(240) DEFAULT NULL,
  [id] int NOT NULL IDENTITY,
  [USERNAME] varchar(240) DEFAULT NULL,
  [EMAIL] varchar(240) DEFAULT NULL,
  [PRT_ANALYSIS_FILEID] decimal(38,0) DEFAULT NULL,
  [PHONE] varchar(256) DEFAULT NULL,
  [ISACTIVE] decimal(2,0) NOT NULL,
  [port_node_id] int DEFAULT NULL,
  [home_server] varchar(24) DEFAULT NULL,
  [home_port] varchar(24) DEFAULT NULL,
  [home_uid] int DEFAULT NULL,
  [temp_status] int DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [msg_status] int DEFAULT NULL,
  [custom_msg] varchar(255) DEFAULT NULL,
  [last_password_change] datetime2(0) NULL DEFAULT NULL,
  [allow_next_login] int DEFAULT NULL,
  [fcm_id] varchar(512) DEFAULT NULL,
  [password_field] varchar(240) DEFAULT NULL,
  [salted_password] varbinary(max),
  [fail_counter] int DEFAULT '0',
  PRIMARY KEY ([id])
);

CREATE INDEX [username] ON users ([USERNAME],[PASSWORD]);
CREATE INDEX [name] ON users ([NAME],[PASSWORD]);

CREATE TABLE customer_details (
  [id] int NOT NULL IDENTITY,
  [name] varchar(64) DEFAULT NULL,
  [sap_code] varchar(64) DEFAULT NULL,
  [address] varchar(64) DEFAULT NULL,
  [status] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  PRIMARY KEY ([id])
);  

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE cgpl_sales_order (
  [id] int NOT NULL IDENTITY,
  [sap_sales_order] varchar(64) DEFAULT NULL,
  [sap_sales_order_creation_date] date DEFAULT NULL,
  [customer_id] float DEFAULT NULL,
  [sap_customer_sap_code] varchar(64) DEFAULT NULL,
  [sap_customer_name] varchar(64) DEFAULT NULL,
  [sap_customer_address] varchar(512) DEFAULT NULL,
  [sap_material] varchar(64) DEFAULT NULL,
  [sap_line_item] int DEFAULT NULL,
  [transporter_id] float DEFAULT NULL,
  [transporter_sap_code] varchar(64) DEFAULT NULL,
  [basic_price] float DEFAULT NULL,
  [created_on] datetime2(0) NOT NULL DEFAULT '0000-00-00 00:00:00',
  [created_by] float DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT '0000-00-00 00:00:00',
  [updated_by] float DEFAULT NULL,
  [status] float DEFAULT NULL,
  [port_node_id] float DEFAULT NULL,
  [tracking_no] varchar(128) DEFAULT NULL,
  [sap_order_quantity] float DEFAULT NULL,
  [sap_order_unit] varchar(64) DEFAULT NULL,
  [sap_sale_order_status] varchar(64) DEFAULT NULL,
  [sap_order_lapse_quantity] float DEFAULT NULL,
  PRIMARY KEY ([id]),
  CONSTRAINT [sap_sales_order] UNIQUE  ([sap_sales_order],[sap_line_item])
);  
-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE user_roles (
  [id] int NOT NULL IDENTITY,
  [USER_1_ID] decimal(38,0) NOT NULL,
  [ROLE_ID] decimal(38,0) NOT NULL,
  PRIMARY KEY ([id])
)

CREATE INDEX [user_roles] ON user_roles ([USER_1_ID],[ROLE_ID]); 


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE role_privs (
  [role_id] int NOT NULL,
  [priv_id] int NOT NULL,
  PRIMARY KEY ([role_id],[priv_id])
);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE transporter_details (
  [id] int NOT NULL IDENTITY,
  [name] varchar(80) DEFAULT NULL,
  [status] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  [material_code] int DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [type] int DEFAULT NULL,
  [sap_code] varchar(64) DEFAULT NULL,
  [lr_prefix] varchar(12) DEFAULT NULL,
  [supervisor] varchar(64) DEFAULT NULL,
  [supervisor_mobile] varchar(64) DEFAULT NULL,
  [supervisor_address] varchar(256) DEFAULT NULL,
  [comments] varchar(512) DEFAULT NULL,
  [full_name] varchar(200) DEFAULT NULL,
  [active_upto] datetime2(0) DEFAULT NULL,
  [active_from] datetime2(0) DEFAULT NULL,
  [material_cat] int DEFAULT NULL,
  [tare_freq] int DEFAULT NULL,
  [sn] varchar(64) DEFAULT NULL,
  PRIMARY KEY ([id])
) ;

-- SQLINES LICENSE FOR EVALUATION USE ONLY
-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE reason_codes (
  [id] int NOT NULL IDENTITY,
  [reason_code] varchar(255) DEFAULT NULL,
  PRIMARY KEY ([id])
);


CREATE TABLE vehicle (
  [id] int NOT NULL IDENTITY,
  [customer_id] int DEFAULT NULL,
  [name] varchar(255) DEFAULT NULL,
  [type] int DEFAULT NULL,
  [device_serial_number] varchar(255) DEFAULT NULL,
  [device_model_info_id] varchar(255) DEFAULT NULL,
  [device_internal_id] varchar(255) DEFAULT NULL,
  [sim] varchar(255) DEFAULT NULL,
  [device_change_reason_code] int DEFAULT NULL,
  [device_change_reason] varchar(512) DEFAULT NULL,
  [status] int DEFAULT NULL,
  [install_date] datetime2(0) DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [io_set_id] int DEFAULT NULL,
  [sim_number] varchar(255) DEFAULT NULL,
  [service_provider_id] int DEFAULT NULL,
  [redirect_url] varchar(255) DEFAULT NULL,
  [miscellaneous] varchar(255) DEFAULT NULL,
  [fieldone] varchar(255) DEFAULT NULL,
  [fieldtwo] varchar(255) DEFAULT NULL,
  [is_template] int DEFAULT '0',
  [detailed_status] int DEFAULT '1',
  [info_complete] int DEFAULT '0',
  [updated_by] int DEFAULT NULL,
  [last_update_comment] varchar(255) DEFAULT NULL,
  [std_name] varchar(255) DEFAULT NULL,
  [ignore_after] datetime2(0) NULL DEFAULT NULL,
  [ignore_before] datetime2(0) NULL DEFAULT NULL,
  [cumm_base] float DEFAULT '0',
  [temp_status] int DEFAULT NULL,
  [fieldthree] varchar(255) DEFAULT NULL,
  [fieldfour] varchar(255) DEFAULT NULL,
  [driver] varchar(255) DEFAULT NULL,
  [do_rule] int DEFAULT NULL,
  [do_trip] int DEFAULT NULL,
  [reboot_freq] int DEFAULT NULL,
  [drop_conn_freq] int DEFAULT NULL,
  [work_area] int DEFAULT NULL,
  [fieldfive] varchar(255) DEFAULT NULL,
  [fieldsix] varchar(255) DEFAULT NULL,
  [fieldseven] varchar(255) DEFAULT NULL,
  [fieldeight] varchar(255) DEFAULT NULL,
  [back_points] int DEFAULT '0',
  [manual_adj_tz_min] int DEFAULT NULL,
  [make] int DEFAULT NULL,
  [model] int DEFAULT NULL,
  [capacity] int DEFAULT NULL,
  [fieldnine] varchar(255) DEFAULT NULL,
  [fieldten] varchar(255) DEFAULT NULL,
  [fieldeleven] varchar(255) DEFAULT NULL,
  [fieldtwelve] varchar(255) DEFAULT NULL,
  [fieldthirteen] varchar(255) DEFAULT NULL,
  [fieldfourteen] varchar(255) DEFAULT NULL,
  [home_branch_id] int DEFAULT NULL,
  [plant] int DEFAULT NULL,
  [last_index] int DEFAULT NULL,
  [server_ip_1] varchar(20) DEFAULT NULL,
  [server_ip_2] varchar(20) DEFAULT NULL,
  [server_port_1] varchar(6) DEFAULT NULL,
  [server_port_2] varchar(6) DEFAULT NULL,
  [calc_flag] int DEFAULT NULL,
  [tripparam_profile_id] int DEFAULT NULL,
  [is_controlled] int DEFAULT NULL,
  [is_weighed] int DEFAULT NULL,
  [rfid_generated] int DEFAULT NULL,
  [alert_mail_id] varchar(64) DEFAULT NULL,
  [alert_phone] varchar(64) DEFAULT NULL,
  [is_notification_hack] int DEFAULT NULL,
  [flag] int DEFAULT NULL,
  [working_status] int DEFAULT NULL,
  [working_detailed_status] int DEFAULT NULL,
  [cust_op_mngr_one] int DEFAULT NULL,
  [cust_op_mngr_two] int DEFAULT NULL,
  [op_mngr_one] int DEFAULT NULL,
  [op_mngr_two] int DEFAULT NULL,
  [stock_owner_id] int DEFAULT NULL,
  [bill_owner_id] int DEFAULT NULL,
  [tare] float DEFAULT NULL,
  [gross] float DEFAULT NULL,
  [rfid_epc] varchar(50) DEFAULT NULL,
  [rfid_issue_date] datetime2(0) DEFAULT NULL,
  [dist_adj_factor] float DEFAULT NULL,
  [cumm_dist] int DEFAULT NULL,
  [use_cumm_dist] int DEFAULT NULL,
  [cumm_data_to_meter] float DEFAULT NULL,
  [orig_status] int DEFAULT NULL,
  [strict_forwarding] int DEFAULT '0',
  [strict_ackback] int DEFAULT '0',
  [flyash_tare] float DEFAULT NULL,
  [flyash_tare_time] datetime2(0) DEFAULT NULL,
  [last_epc] varchar(50) DEFAULT NULL,
  [rfid_temp_status] int DEFAULT NULL,
  [unload_tare_time] datetime2(0) DEFAULT NULL,
  [unload_tare] float DEFAULT NULL,
  [load_tare_freq] int DEFAULT NULL,
  [unload_tare_freq] int DEFAULT NULL,
  [card_type] int DEFAULT NULL,
  [card_purpose] int DEFAULT NULL,
  [do_assigned] varchar(100) DEFAULT NULL,
  [card_validity_type] int DEFAULT NULL,
  [card_expiary_date] datetime2(0) DEFAULT NULL,
  [card_init] int DEFAULT NULL,
  [prefered_mines] int DEFAULT NULL,
  [prefered_driver] int DEFAULT NULL,
  [rfid_info_id] int DEFAULT NULL,
  [card_init_date] datetime2(0) DEFAULT NULL,
  [is_vehicle_on_gate] int DEFAULT NULL,
  [prefered_mines_code] varchar(20) DEFAULT NULL,
  [last_tare_tpr] int DEFAULT NULL,
  [min_tare] float DEFAULT NULL,
  [min_gross] float DEFAULT NULL,
  [mdt_flag] int DEFAULT NULL,
  [preferred_driver] int DEFAULT NULL,
  [rf_updated_on] datetime2(0) DEFAULT NULL,
  [tag_init_challan] varchar(128) DEFAULT NULL,
  [gate_pass_number] varchar(128) DEFAULT NULL,
  [record_src] int DEFAULT NULL,
  [src_record_time] datetime2(0) DEFAULT NULL,
  [driver_name] varchar(100) DEFAULT NULL,
  [driver_dl_no] varchar(50) DEFAULT NULL,
  [driver_phone] varchar(20) DEFAULT NULL,
  [transporter_code] varchar(50) DEFAULT NULL,
  [driver_expiary] datetime2(0) DEFAULT NULL,
  [card_expiry] datetime2(0) NULL DEFAULT NULL,
  [load_tare_time] datetime2(0) NULL DEFAULT NULL,
  [sub_type] int DEFAULT NULL,
  [khanij_reg_no] varchar(128) DEFAULT NULL,
  [khanij_reg_expiry] datetime2(0) DEFAULT NULL,
  [temp_remote_id] int DEFAULT NULL,
  [gate_pass_no] varchar(44) DEFAULT NULL,
  [vehicle_quality] int DEFAULT NULL,
  PRIMARY KEY ([id]),
  CONSTRAINT [uc_vehicle_std_name] UNIQUE  ([std_name])
)

CREATE INDEX [device_change_reason_code] ON vehicle ([device_change_reason_code]);
CREATE INDEX [name] ON vehicle ([name]);
CREATE INDEX [device_serial_number] ON vehicle ([device_serial_number]);
CREATE INDEX [device_internal_id] ON vehicle ([device_internal_id]);
CREATE INDEX [std_name] ON vehicle ([std_name]);
CREATE INDEX [by_updated_on] ON vehicle ([updated_on],[record_src]);
CREATE INDEX [epc_veh] ON vehicle ([rfid_epc],[status]);

-- SQLINES LICENSE FOR EVALUATION USE ONLY



======================================================

CREATE TABLE tp_record (
  [tpr_id] int NOT NULL IDENTITY,
  [vehicle_id] int DEFAULT NULL,
  [vehicle_name] varchar(64) DEFAULT NULL,
  [transporter_id] int DEFAULT NULL,
  [do_id] int DEFAULT NULL,
  [material_grade_id] int DEFAULT NULL,
  [plant_id] int DEFAULT NULL,
  [mines_id] int DEFAULT NULL,
  [hh_device_id] int DEFAULT NULL,
  [challan_no] varchar(64) DEFAULT NULL,
  [challan_date] datetime2(0) DEFAULT NULL,
  [tpr_create_date] datetime2(0) DEFAULT NULL,
  [lr_no] varchar(64) DEFAULT NULL,
  [dispatch_permit_no] varchar(64) DEFAULT NULL,
  [load_tare] float DEFAULT NULL,
  [load_gross] float DEFAULT NULL,
  [unload_tare] float DEFAULT NULL,
  [unload_gross] float DEFAULT NULL,
  [earliest_load_gate_in_in] datetime2(0) DEFAULT NULL,
  [latest_load_gate_in_out] datetime2(0) DEFAULT NULL,
  [load_gate_in_name] varchar(128) DEFAULT NULL,
  [earliest_load_wb_in_in] datetime2(0) DEFAULT NULL,
  [latest_load_wb_in_out] datetime2(0) DEFAULT NULL,
  [load_wb_in_name] varchar(128) DEFAULT NULL,
  [earliest_load_yard_in_in] datetime2(0) DEFAULT NULL,
  [latest_load_yard_in_out] datetime2(0) DEFAULT NULL,
  [load_yard_in_name] varchar(128) DEFAULT NULL,
  [earliest_load_yard_out_in] datetime2(0) DEFAULT NULL,
  [latest_load_yard_out_out] datetime2(0) DEFAULT NULL,
  [earliest_load_wb_out_in] datetime2(0) DEFAULT NULL,
  [latest_load_wb_out_out] datetime2(0) DEFAULT NULL,
  [load_wb_out_name] varchar(128) DEFAULT NULL,
  [earliest_load_gate_out_in] datetime2(0) DEFAULT NULL,
  [latest_load_gate_out_out] datetime2(0) DEFAULT NULL,
  [load_gate_out_name] varchar(128) DEFAULT NULL,
  [earliest_unload_gate_in_in] datetime2(0) DEFAULT NULL,
  [latest_unload_gate_in_out] datetime2(0) DEFAULT NULL,
  [unload_gate_in_name] varchar(128) DEFAULT NULL,
  [earliest_unload_wb_in_in] datetime2(0) DEFAULT NULL,
  [latest_unload_wb_in_out] datetime2(0) DEFAULT NULL,
  [unload_wb_in_name] varchar(128) DEFAULT NULL,
  [earliest_unload_yard_in_in] datetime2(0) DEFAULT NULL,
  [latest_unload_yard_in_out] datetime2(0) DEFAULT NULL,
  [unload_yard_in_name] varchar(128) DEFAULT NULL,
  [earliest_unload_yard_out_in] datetime2(0) DEFAULT NULL,
  [latest_unload_yard_out_out] datetime2(0) DEFAULT NULL,
  [earliest_unload_wb_out_in] datetime2(0) DEFAULT NULL,
  [latest_unload_wb_out_out] datetime2(0) DEFAULT NULL,
  [unload_wb_out_name] varchar(128) DEFAULT NULL,
  [earliest_unload_gate_out_in] datetime2(0) DEFAULT NULL,
  [latest_unload_gate_out_out] datetime2(0) DEFAULT NULL,
  [unload_gate_out_name] varchar(128) DEFAULT NULL,
  [bed_assigned] varchar(64) DEFAULT NULL,
  [driver_id] int DEFAULT NULL,
  [dl_no] varchar(64) DEFAULT NULL,
  [driver_name] varchar(64) DEFAULT NULL,
  [mines_trip_id] int DEFAULT NULL,
  [hh_tpr_merged_time] datetime2(0) DEFAULT NULL,
  [old_trip_id] int DEFAULT NULL,
  [tpr_status] int DEFAULT NULL,
  [status_reason] varchar(1024) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [user_by] int DEFAULT NULL,
  [prev_tp_step] int DEFAULT NULL,
  [next_tp_step] int DEFAULT NULL,
  [confirm_time] datetime2(0) DEFAULT NULL,
  [combo_start] datetime2(0) NULL DEFAULT NULL,
  [combo_end] datetime2(0) NULL DEFAULT NULL,
  [next_trip_id] int DEFAULT NULL,
  [m_trip_id] int DEFAULT NULL,
  [is_rfid_trip_close] int DEFAULT NULL,
  [is_merged_with_hh_tpr] int DEFAULT NULL,
  [lr_date] datetime2(0) DEFAULT NULL,
  [rf_lr_date] datetime2(0) DEFAULT NULL,
  [rf_transporter_id] int DEFAULT NULL,
  [rf_mines_id] int DEFAULT NULL,
  [rf_grade] int DEFAULT NULL,
  [rf_challan_date] datetime2(0) DEFAULT NULL,
  [rf_challan_id] varchar(100) DEFAULT NULL,
  [rf_lr_id] varchar(100) DEFAULT NULL,
  [rf_load_tare] float DEFAULT NULL,
  [rf_load_gross] float DEFAULT NULL,
  [rf_device_id] int DEFAULT NULL,
  [rf_do_id] int DEFAULT NULL,
  [rf_record_id] int DEFAULT NULL,
  [rf_record_key] varchar(100) DEFAULT NULL,
  [material_code_id] int DEFAULT NULL,
  [mpl_ref_doc] int DEFAULT NULL,
  [consignee_ref_doc] varchar(512) DEFAULT NULL,
  [material_description] varchar(512) DEFAULT NULL,
  [consignee_address] varchar(512) DEFAULT NULL,
  [consignee_notes] varchar(512) DEFAULT NULL,
  [consignor_name] varchar(512) DEFAULT NULL,
  [consignor_ref_doc] varchar(512) DEFAULT NULL,
  [consignor_address] varchar(512) DEFAULT NULL,
  [consignor_notes] varchar(512) DEFAULT NULL,
  [carrying_transporter_id] int DEFAULT NULL,
  [consignor_id] int DEFAULT NULL,
  [consignee_id] int DEFAULT NULL,
  [consignee_name] varchar(512) DEFAULT NULL,
  [reporting_status] int DEFAULT '1',
  [cancellation_reason] int DEFAULT NULL,
  [mark_for_qc] int DEFAULT NULL,
  [permit_no] varchar(64) DEFAULT NULL,
  [pre_step_type] int DEFAULT NULL,
  [pre_step_date] datetime2(0) DEFAULT NULL,
  [next_step_type] int DEFAULT NULL,
  [blocked_step_type] int DEFAULT NULL,
  [blocked_step_id] int DEFAULT NULL,
  [blocked_step_date] datetime2(0) DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT '1',
  [comment] varchar(64) DEFAULT NULL,
  [is_latest] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [driver_src] int DEFAULT NULL,
  [vehicle_src] int DEFAULT NULL,
  [mark_for_gps] int DEFAULT NULL,
  [mark_for_qc_reason] varchar(255) DEFAULT NULL,
  [ext_doc_ref] varchar(64) DEFAULT NULL,
  [ext_doc_date] datetime2(0) NULL DEFAULT NULL,
  [src_device_log_id] int DEFAULT NULL,
  [rf_also_on_card] int DEFAULT NULL,
  [is_new_vehicle] int DEFAULT NULL,
  [material_cat] int DEFAULT '0',
  [stone_lift_area_id] int DEFAULT NULL,
  [stone_of_transporter_id] int DEFAULT NULL,
  [challan_data_edit_at_reg] smallint DEFAULT NULL,
  [challan_data_edit_at_wb] smallint DEFAULT NULL,
  [challan_data_edit_at_preaudit] smallint DEFAULT NULL,
  [challan_data_edit_at_audit] smallint DEFAULT NULL,
  [earliest_reg_in] datetime2(0) NULL DEFAULT NULL,
  [latest_reg_in] datetime2(0) NULL DEFAULT NULL,
  [latest_reg_out] datetime2(0) NULL DEFAULT NULL,
  [ref_tpr_id_if_cancelled] int DEFAULT NULL,
  [wbin_station_id] int DEFAULT NULL,
  [wbout_station_id] int DEFAULT NULL,
  [verification_status] int DEFAULT '3',
  [material_notes_first] varchar(255) DEFAULT NULL,
  [material_notes_second] varchar(255) DEFAULT NULL,
  [mpl_reference_doc] varchar(255) DEFAULT NULL,
  [rf_vehicle_id] int DEFAULT NULL,
  [rf_vehicle_name] varchar(40) DEFAULT NULL,
  [other_material_description] varchar(100) DEFAULT NULL,
  [grn_id] int DEFAULT NULL,
  [supplier_id] int DEFAULT NULL,
  [transhipped_vehicle] int DEFAULT NULL,
  [transhipped_remarks] varchar(128) DEFAULT NULL,
  [rf_card_data_merge_time] datetime2(0) NULL DEFAULT NULL,
  [material_sub_cat] int DEFAULT NULL,
  [material_sub_cat_id] int DEFAULT NULL,
  [unload_yard_out_name] varchar(64) DEFAULT NULL,
  [load_yard_out_name] varchar(64) DEFAULT NULL,
  [load_flyash_tare_name] varchar(128) DEFAULT NULL,
  [load_flyash_gross_name] varchar(128) DEFAULT NULL,
  [tpr_type] int DEFAULT '0',
  [washery_id] int DEFAULT '0',
  [rfid_info_id] int DEFAULT NULL,
  [mines_code] varchar(20) DEFAULT NULL,
  [transporter_code] varchar(20) DEFAULT NULL,
  [destination_code] varchar(20) DEFAULT NULL,
  [washery_code] varchar(20) DEFAULT NULL,
  [grade_code] varchar(20) DEFAULT NULL,
  [product_code] varchar(20) DEFAULT NULL,
  [rf_mines_code] varchar(20) DEFAULT NULL,
  [rf_transporter_code] varchar(20) DEFAULT NULL,
  [rf_destination_code] varchar(20) DEFAULT NULL,
  [rf_washery_code] varchar(20) DEFAULT NULL,
  [rf_grade_code] varchar(20) DEFAULT NULL,
  [rf_product_code] varchar(20) DEFAULT NULL,
  [do_number] varchar(64) DEFAULT NULL,
  [invoice_number]  VARCHAR(255),
[destination_state_code] VARCHAR(255),
[load_gross_time] datetime2(0) NULL DEFAULT NULL,
[load_tare_time] datetime2(0) NULL DEFAULT NULL,
[destination] VARCHAR(255),
[remote_tpr_id] int DEFAULT NULL,
[allow_gross_tare_diff_wb] int DEFAULT NULL,
[allowed_by] VARCHAR(255),
[allowed_reason] VARCHAR(255),
[record_src] int DEFAULT NULL,
[server_code]  VARCHAR(255),
[ex_invoice]  VARCHAR(255),
[wb_challan_no] VARCHAR(255),
[message] VARCHAR(255),
  PRIMARY KEY ([tpr_id])
) ;

CREATE INDEX [tpr_veh] ON tp_record ([vehicle_id],[challan_date]);
CREATE INDEX [veh_combo] ON tp_record ([vehicle_id],[combo_start]);
CREATE INDEX [vehicle_challan] ON tp_record ([vehicle_id],[challan_no]);
CREATE INDEX [challan_no_challan_date] ON tp_record ([challan_no],[challan_date]);
CREATE INDEX [driver_id] ON tp_record ([driver_id],[combo_start]);
CREATE INDEX [rf_challan_date] ON tp_record ([rf_device_id],[rf_record_id],[rf_challan_date]);
CREATE INDEX [challan_no] ON tp_record ([mines_id],[challan_no]);
CREATE INDEX [rf_record_key] ON tp_record ([rf_record_key]);
CREATE INDEX [material_cat] ON tp_record ([tpr_status],[is_latest],[material_cat],[status]);

=============================


CREATE TABLE tp_record_apprvd (
  [tpr_id] int NOT NULL ,
  [vehicle_id] int DEFAULT NULL,
  [vehicle_name] varchar(64) DEFAULT NULL,
  [transporter_id] int DEFAULT NULL,
  [do_id] int DEFAULT NULL,
  [material_grade_id] int DEFAULT NULL,
  [plant_id] int DEFAULT NULL,
  [mines_id] int DEFAULT NULL,
  [hh_device_id] int DEFAULT NULL,
  [challan_no] varchar(64) DEFAULT NULL,
  [challan_date] datetime2(0) DEFAULT NULL,
  [tpr_create_date] datetime2(0) DEFAULT NULL,
  [lr_no] varchar(64) DEFAULT NULL,
  [dispatch_permit_no] varchar(64) DEFAULT NULL,
  [load_tare] float DEFAULT NULL,
  [load_gross] float DEFAULT NULL,
  [unload_tare] float DEFAULT NULL,
  [unload_gross] float DEFAULT NULL,
  [earliest_load_gate_in_in] datetime2(0) DEFAULT NULL,
  [latest_load_gate_in_out] datetime2(0) DEFAULT NULL,
  [load_gate_in_name] varchar(128) DEFAULT NULL,
  [earliest_load_wb_in_in] datetime2(0) DEFAULT NULL,
  [latest_load_wb_in_out] datetime2(0) DEFAULT NULL,
  [load_wb_in_name] varchar(128) DEFAULT NULL,
  [earliest_load_yard_in_in] datetime2(0) DEFAULT NULL,
  [latest_load_yard_in_out] datetime2(0) DEFAULT NULL,
  [load_yard_in_name] varchar(128) DEFAULT NULL,
  [earliest_load_yard_out_in] datetime2(0) DEFAULT NULL,
  [latest_load_yard_out_out] datetime2(0) DEFAULT NULL,
  [earliest_load_wb_out_in] datetime2(0) DEFAULT NULL,
  [latest_load_wb_out_out] datetime2(0) DEFAULT NULL,
  [load_wb_out_name] varchar(128) DEFAULT NULL,
  [earliest_load_gate_out_in] datetime2(0) DEFAULT NULL,
  [latest_load_gate_out_out] datetime2(0) DEFAULT NULL,
  [load_gate_out_name] varchar(128) DEFAULT NULL,
  [earliest_unload_gate_in_in] datetime2(0) DEFAULT NULL,
  [latest_unload_gate_in_out] datetime2(0) DEFAULT NULL,
  [unload_gate_in_name] varchar(128) DEFAULT NULL,
  [earliest_unload_wb_in_in] datetime2(0) DEFAULT NULL,
  [latest_unload_wb_in_out] datetime2(0) DEFAULT NULL,
  [unload_wb_in_name] varchar(128) DEFAULT NULL,
  [earliest_unload_yard_in_in] datetime2(0) DEFAULT NULL,
  [latest_unload_yard_in_out] datetime2(0) DEFAULT NULL,
  [unload_yard_in_name] varchar(128) DEFAULT NULL,
  [earliest_unload_yard_out_in] datetime2(0) DEFAULT NULL,
  [latest_unload_yard_out_out] datetime2(0) DEFAULT NULL,
  [earliest_unload_wb_out_in] datetime2(0) DEFAULT NULL,
  [latest_unload_wb_out_out] datetime2(0) DEFAULT NULL,
  [unload_wb_out_name] varchar(128) DEFAULT NULL,
  [earliest_unload_gate_out_in] datetime2(0) DEFAULT NULL,
  [latest_unload_gate_out_out] datetime2(0) DEFAULT NULL,
  [unload_gate_out_name] varchar(128) DEFAULT NULL,
  [bed_assigned] varchar(64) DEFAULT NULL,
  [driver_id] int DEFAULT NULL,
  [dl_no] varchar(64) DEFAULT NULL,
  [driver_name] varchar(64) DEFAULT NULL,
  [mines_trip_id] int DEFAULT NULL,
  [hh_tpr_merged_time] datetime2(0) DEFAULT NULL,
  [old_trip_id] int DEFAULT NULL,
  [tpr_status] int DEFAULT NULL,
  [status_reason] varchar(1024) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [user_by] int DEFAULT NULL,
  [prev_tp_step] int DEFAULT NULL,
  [next_tp_step] int DEFAULT NULL,
  [confirm_time] datetime2(0) DEFAULT NULL,
  [combo_start] datetime2(0) NULL DEFAULT NULL,
  [combo_end] datetime2(0) NULL DEFAULT NULL,
  [next_trip_id] int DEFAULT NULL,
  [m_trip_id] int DEFAULT NULL,
  [is_rfid_trip_close] int DEFAULT NULL,
  [is_merged_with_hh_tpr] int DEFAULT NULL,
  [lr_date] datetime2(0) DEFAULT NULL,
  [rf_lr_date] datetime2(0) DEFAULT NULL,
  [rf_transporter_id] int DEFAULT NULL,
  [rf_mines_id] int DEFAULT NULL,
  [rf_grade] int DEFAULT NULL,
  [rf_challan_date] datetime2(0) DEFAULT NULL,
  [rf_challan_id] varchar(100) DEFAULT NULL,
  [rf_lr_id] varchar(100) DEFAULT NULL,
  [rf_load_tare] float DEFAULT NULL,
  [rf_load_gross] float DEFAULT NULL,
  [rf_device_id] int DEFAULT NULL,
  [rf_do_id] int DEFAULT NULL,
  [rf_record_id] int DEFAULT NULL,
  [rf_record_key] varchar(100) DEFAULT NULL,
  [material_code_id] int DEFAULT NULL,
  [mpl_ref_doc] int DEFAULT NULL,
  [consignee_ref_doc] varchar(512) DEFAULT NULL,
  [material_description] varchar(512) DEFAULT NULL,
  [consignee_address] varchar(512) DEFAULT NULL,
  [consignee_notes] varchar(512) DEFAULT NULL,
  [consignor_name] varchar(512) DEFAULT NULL,
  [consignor_ref_doc] varchar(512) DEFAULT NULL,
  [consignor_address] varchar(512) DEFAULT NULL,
  [consignor_notes] varchar(512) DEFAULT NULL,
  [carrying_transporter_id] int DEFAULT NULL,
  [consignor_id] int DEFAULT NULL,
  [consignee_id] int DEFAULT NULL,
  [consignee_name] varchar(512) DEFAULT NULL,
  [reporting_status] int DEFAULT '1',
  [cancellation_reason] int DEFAULT NULL,
  [mark_for_qc] int DEFAULT NULL,
  [permit_no] varchar(64) DEFAULT NULL,
  [pre_step_type] int DEFAULT NULL,
  [pre_step_date] datetime2(0) DEFAULT NULL,
  [next_step_type] int DEFAULT NULL,
  [blocked_step_type] int DEFAULT NULL,
  [blocked_step_id] int DEFAULT NULL,
  [blocked_step_date] datetime2(0) DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT '1',
  [comment] varchar(64) DEFAULT NULL,
  [is_latest] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [driver_src] int DEFAULT NULL,
  [vehicle_src] int DEFAULT NULL,
  [mark_for_gps] int DEFAULT NULL,
  [mark_for_qc_reason] varchar(255) DEFAULT NULL,
  [ext_doc_ref] varchar(64) DEFAULT NULL,
  [ext_doc_date] datetime2(0) NULL DEFAULT NULL,
  [src_device_log_id] int DEFAULT NULL,
  [rf_also_on_card] int DEFAULT NULL,
  [is_new_vehicle] int DEFAULT NULL,
  [material_cat] int DEFAULT '0',
  [stone_lift_area_id] int DEFAULT NULL,
  [stone_of_transporter_id] int DEFAULT NULL,
  [challan_data_edit_at_reg] smallint DEFAULT NULL,
  [challan_data_edit_at_wb] smallint DEFAULT NULL,
  [challan_data_edit_at_preaudit] smallint DEFAULT NULL,
  [challan_data_edit_at_audit] smallint DEFAULT NULL,
  [earliest_reg_in] datetime2(0) NULL DEFAULT NULL,
  [latest_reg_in] datetime2(0) NULL DEFAULT NULL,
  [latest_reg_out] datetime2(0) NULL DEFAULT NULL,
  [ref_tpr_id_if_cancelled] int DEFAULT NULL,
  [wbin_station_id] int DEFAULT NULL,
  [wbout_station_id] int DEFAULT NULL,
  [verification_status] int DEFAULT '3',
  [material_notes_first] varchar(255) DEFAULT NULL,
  [material_notes_second] varchar(255) DEFAULT NULL,
  [mpl_reference_doc] varchar(255) DEFAULT NULL,
  [rf_vehicle_id] int DEFAULT NULL,
  [rf_vehicle_name] varchar(40) DEFAULT NULL,
  [other_material_description] varchar(100) DEFAULT NULL,
  [grn_id] int DEFAULT NULL,
  [supplier_id] int DEFAULT NULL,
  [transhipped_vehicle] int DEFAULT NULL,
  [transhipped_remarks] varchar(128) DEFAULT NULL,
  [rf_card_data_merge_time] datetime2(0) NULL DEFAULT NULL,
  [material_sub_cat] int DEFAULT NULL,
  [material_sub_cat_id] int DEFAULT NULL,
  [unload_yard_out_name] varchar(64) DEFAULT NULL,
  [load_yard_out_name] varchar(64) DEFAULT NULL,
  [load_flyash_tare_name] varchar(128) DEFAULT NULL,
  [load_flyash_gross_name] varchar(128) DEFAULT NULL,
  [tpr_type] int DEFAULT '0',
  [washery_id] int DEFAULT '0',
  [rfid_info_id] int DEFAULT NULL,
  [mines_code] varchar(20) DEFAULT NULL,
  [transporter_code] varchar(20) DEFAULT NULL,
  [destination_code] varchar(20) DEFAULT NULL,
  [washery_code] varchar(20) DEFAULT NULL,
  [grade_code] varchar(20) DEFAULT NULL,
  [product_code] varchar(20) DEFAULT NULL,
  [rf_mines_code] varchar(20) DEFAULT NULL,
  [rf_transporter_code] varchar(20) DEFAULT NULL,
  [rf_destination_code] varchar(20) DEFAULT NULL,
  [rf_washery_code] varchar(20) DEFAULT NULL,
  [rf_grade_code] varchar(20) DEFAULT NULL,
  [rf_product_code] varchar(20) DEFAULT NULL,
  [do_number] varchar(64) DEFAULT NULL,
  [invoice_number]  VARCHAR(255),
[destination_state_code] VARCHAR(255),
[load_gross_time] datetime2(0) NULL DEFAULT NULL,
[load_tare_time] datetime2(0) NULL DEFAULT NULL,
[destination] VARCHAR(255),
[remote_tpr_id] int DEFAULT NULL,
[allow_gross_tare_diff_wb] int DEFAULT NULL,
[allowed_by] VARCHAR(255),
[allowed_reason] VARCHAR(255),
[record_src] int DEFAULT NULL,
[server_code]  VARCHAR(255),
[ex_invoice]  VARCHAR(255),
[wb_challan_no] VARCHAR(255),
[message] VARCHAR(255)
) ;

CREATE INDEX [tpr_veh] ON tp_record_apprvd ([vehicle_id],[challan_date]);
CREATE INDEX [veh_combo] ON tp_record_apprvd ([vehicle_id],[combo_start]);
CREATE INDEX [vehicle_challan] ON tp_record_apprvd ([vehicle_id],[challan_no]);
CREATE INDEX [challan_no_challan_date] ON tp_record_apprvd ([challan_no],[challan_date]);
CREATE INDEX [driver_id] ON tp_record_apprvd ([driver_id],[combo_start]);
CREATE INDEX [rf_challan_date] ON tp_record_apprvd ([rf_device_id],[rf_record_id],[rf_challan_date]);
CREATE INDEX [challan_no] ON tp_record_apprvd ([mines_id],[challan_no]);
CREATE INDEX [rf_record_key] ON tp_record_apprvd ([rf_record_key]);
CREATE INDEX [material_cat] ON tp_record_apprvd ([tpr_status],[is_latest],[material_cat],[status]);



====================================


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE merge_process_log (
  tpr_id int DEFAULT NULL,
  vehicle_id int DEFAULT NULL,
  trace varchar(2048) DEFAULT NULL,
  updated_on datetime2(0) DEFAULT NULL
) ;
CREATE TABLE merge_debug_flag (
  [flag] int DEFAULT NULL
);






=============================Dummy Data=================================

INSERT INTO  transporter_details  ( NAME ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  material_code ,  port_node_id ,  created_by ,  TYPE ,  sap_code ,  lr_prefix ,  supervisor ,  supervisor_mobile ,  supervisor_address ,  comments ,  full_name ,  active_upto ,  active_from ,  material_cat ,  tare_freq ,  sn ) VALUES('AKAL','1','2016-12-01 13:35:25','2016-12-01 14:18:52','1',NULL,'3','1',NULL,'AK',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL);
INSERT INTO  transporter_details  ( NAME ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  material_code ,  port_node_id ,  created_by ,  TYPE ,  sap_code ,  lr_prefix ,  supervisor ,  supervisor_mobile ,  supervisor_address ,  comments ,  full_name ,  active_upto ,  active_from ,  material_cat ,  tare_freq ,  sn ) VALUES('BKB','1','2016-12-01 13:35:25','2016-12-01 14:18:52','1',NULL,'3','1',NULL,'BK',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL);
INSERT INTO  transporter_details  ( NAME ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  material_code ,  port_node_id ,  created_by ,  TYPE ,  sap_code ,  lr_prefix ,  supervisor ,  supervisor_mobile ,  supervisor_address ,  comments ,  full_name ,  active_upto ,  active_from ,  material_cat ,  tare_freq ,  sn ) VALUES('akal','2','2016-12-01 14:14:48','2016-12-01 14:17:46','1',NULL,'3','1',NULL,'AKA',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL);
INSERT INTO  transporter_details  ( NAME ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  material_code ,  port_node_id ,  created_by ,  TYPE ,  sap_code ,  lr_prefix ,  supervisor ,  supervisor_mobile ,  supervisor_address ,  comments ,  full_name ,  active_upto ,  active_from ,  material_cat ,  tare_freq ,  sn ) VALUES('AKAL','2','2016-12-01 14:15:39','2016-12-01 14:17:46','1',NULL,'3','1',NULL,'AKA',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL);
INSERT INTO  transporter_details  ( NAME ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  material_code ,  port_node_id ,  created_by ,  TYPE ,  sap_code ,  lr_prefix ,  supervisor ,  supervisor_mobile ,  supervisor_address ,  comments ,  full_name ,  active_upto ,  active_from ,  material_cat ,  tare_freq ,  sn ) VALUES('AKAL','2','2016-12-01 14:16:28','2016-12-01 14:17:46','1',NULL,'3','1',NULL,'AK',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL);
INSERT INTO  transporter_details  ( NAME ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  material_code ,  port_node_id ,  created_by ,  TYPE ,  sap_code ,  lr_prefix ,  supervisor ,  supervisor_mobile ,  supervisor_address ,  comments ,  full_name ,  active_upto ,  active_from ,  material_cat ,  tare_freq ,  sn ) VALUES('Ipssi','1','2016-12-01 14:18:52','2016-12-01 14:18:52','1',NULL,'3','1',NULL,'IP',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'0',NULL,NULL);


INSERT INTO  customer_details  ( NAME ,  sap_code ,  address ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  port_node_id ,  created_by ) VALUES('Maithon power ltd','MPL','JHARKHAND','1','2016-11-30 19:09:23','2016-11-30 19:11:31','1','3','1');
INSERT INTO  customer_details  ( NAME ,  sap_code ,  address ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  port_node_id ,  created_by ) VALUES('Adani Power ltd','APL','CHATTISGARH','1','2016-11-30 19:09:23','2016-11-30 19:11:32','1','3','1');
INSERT INTO  customer_details  ( NAME ,  sap_code ,  address ,  STATUS ,  created_on ,  updated_on ,  updated_by ,  port_node_id ,  created_by ) VALUES('Triveni','TEM','ORISSA','1','2016-11-30 19:09:23','2016-11-30 19:11:32','1','3','1');


=================================================================================

CREATE TABLE block_instruction (
  [id] int NOT NULL IDENTITY,
  [vehicle_id] int DEFAULT NULL,
  [type] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  [block_from] datetime2(0) DEFAULT NULL,
  [block_to] datetime2(0) DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [notes] varchar(255) DEFAULT NULL,
  [material_cat] int DEFAULT NULL,
  PRIMARY KEY ([id])
);

CREATE TABLE block_instruction_apprvd()
  [id] int NOT NULL IDENTITY,
  [vehicle_id] int DEFAULT NULL,
  [type] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  [block_from] datetime2(0) DEFAULT NULL,
  [block_to] datetime2(0) DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [notes] varchar(255) DEFAULT NULL,
  [material_cat] int DEFAULT NULL,
  PRIMARY KEY ([id])
);



CREATE TABLE tp_step (
  [tps_id] int NOT NULL IDENTITY,
  [tpr_id] int DEFAULT NULL,
  [vehicle_id] int DEFAULT NULL,
  [work_station_type] int DEFAULT NULL,
  [work_station_id] int DEFAULT NULL,
  [has_valid_rf] int DEFAULT NULL,
  [in_time] datetime2(0) DEFAULT NULL,
  [out_time] datetime2(0) DEFAULT NULL,
  [tare_wt] float DEFAULT NULL,
  [gross_wt] float DEFAULT NULL,
  [short_wt] float DEFAULT NULL,
  [mark_for_qc] int DEFAULT NULL,
  [qc_id] int DEFAULT NULL,
  [dispatch_permit_no] varchar(64) DEFAULT NULL,
  [mineral_challan_no] varchar(64) DEFAULT NULL,
  [iia_receipt_no] varchar(64) DEFAULT NULL,
  [has_gps_violations] int DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [user_by] int DEFAULT NULL,
  [notes] varchar(255) DEFAULT NULL,
  [result_code] int DEFAULT NULL,
  [for_block] int DEFAULT NULL,
  [save_status] smallint DEFAULT NULL,
  [material_cat] int DEFAULT NULL,
  [is_centered] int DEFAULT NULL,
  PRIMARY KEY ([tps_id])
)

CREATE INDEX [tps_tpr_work_station] ON tp_step ([tpr_id],[work_station_id]);



drop table tp_step_apprvd;

CREATE TABLE tp_step_apprvd (
  [tps_id] int NOT NULL,
  [tpr_id] int DEFAULT NULL,
  [vehicle_id] int DEFAULT NULL,
  [work_station_type] int DEFAULT NULL,
  [work_station_id] int DEFAULT NULL,
  [has_valid_rf] int DEFAULT NULL,
  [in_time] datetime2(0) DEFAULT NULL,
  [out_time] datetime2(0) DEFAULT NULL,
  [tare_wt] float DEFAULT NULL,
  [gross_wt] float DEFAULT NULL,
  [short_wt] float DEFAULT NULL,
  [mark_for_qc] int DEFAULT NULL,
  [qc_id] int DEFAULT NULL,
  [dispatch_permit_no] varchar(64) DEFAULT NULL,
  [mineral_challan_no] varchar(64) DEFAULT NULL,
  [iia_receipt_no] varchar(64) DEFAULT NULL,
  [has_gps_violations] int DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [user_by] int DEFAULT NULL,
  [notes] varchar(255) DEFAULT NULL,
  [result_code] int DEFAULT NULL,
  [for_block] int DEFAULT NULL,
  [save_status] smallint DEFAULT NULL,
  [material_cat] int DEFAULT NULL,
  [is_centered] int DEFAULT NULL
);

CREATE INDEX [tps_tpr_work_station] ON tp_step_apprvd ([tpr_id],[work_station_id]);




-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE tps_question_detail (
  [tps_question_id] int NOT NULL IDENTITY,
  [tps_id] int DEFAULT NULL,
  [tpr_id] int DEFAULT NULL,
  [question_id] int DEFAULT NULL,
  [answer_id] int DEFAULT NULL,
  [action_required] int DEFAULT NULL,
  [block_next_step] int DEFAULT NULL,
  [block_trip] int DEFAULT NULL,
  [block_vehicle] int DEFAULT NULL,
  [tpr_block_id] int DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [user_by] int DEFAULT NULL,
  PRIMARY KEY ([tps_question_id])
)

CREATE INDEX [tpr_id] ON tps_question_detail ([tpr_id],[question_id]);



CREATE TABLE tps_question_detail_apprvd (
  [tps_question_id] int NOT NULL,
  [tps_id] int DEFAULT NULL,
  [tpr_id] int DEFAULT NULL,
  [question_id] int DEFAULT NULL,
  [answer_id] int DEFAULT NULL,
  [action_required] int DEFAULT NULL,
  [block_next_step] int DEFAULT NULL,
  [block_trip] int DEFAULT NULL,
  [block_vehicle] int DEFAULT NULL,
  [tpr_block_id] int DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [user_by] int DEFAULT NULL
)

CREATE INDEX [tpr_id] ON tps_question_detail_apprvd ([tpr_id],[question_id]);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE tpr_block_status (
  [tpr_id] int DEFAULT NULL,
  [workstation_type_id] int DEFAULT NULL,
  [system_cause_id] int DEFAULT NULL,
  [instruction_id] int DEFAULT NULL,
  [override_step_only] int DEFAULT NULL,
  [override_tpr_only] int DEFAULT NULL,
  [override_status] int DEFAULT NULL,
  [override_notes] varchar(255) DEFAULT NULL,
  [override_date] datetime2(0) DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  [override_ste] int DEFAULT NULL,
  [override_workstation_type_id] int DEFAULT NULL,
  [type] int DEFAULT NULL,
  [id] int NOT NULL IDENTITY,
  [status] int DEFAULT '1',
  [override_step] int DEFAULT NULL,
  [create_type] int DEFAULT NULL,
  [skipped_step_id] int DEFAULT NULL,
  PRIMARY KEY ([id])
)

CREATE INDEX [tpr_id] ON tpr_block_status ([tpr_id],[workstation_type_id],[status],[override_status],[instruction_id]);



==============================LOcTracker tables==============================
create table users_flex_priv (user_id int, name varchar(64), val varchar(64));


CREATE TABLE user_session (
  user_id int DEFAULT NULL,
  start datetime2 DEFAULT NULL,
  ends datetime2 DEFAULT NULL
) ;

insert  into user_session(user_id,start,ends) values 
(1,'2016-11-30 23:30:44','2016-11-30 23:35:47');



CREATE TABLE singleton (
  dummy int DEFAULT NULL
);

insert into singleton (dummy) values(1);


####################  21 june ######################################

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE device_model_info (
  [id] int NOT NULL IDENTITY,
  [name] varchar(255) DEFAULT NULL,
  [internal_battery] char(1) DEFAULT NULL,
  [ignition_on_off] char(1) DEFAULT NULL,
  [voice] char(1) DEFAULT NULL,
  [buzzer] char(1) DEFAULT NULL,
  [io_count] int DEFAULT NULL,
  [display] char(1) DEFAULT NULL,
  [status] int DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [device_model] int DEFAULT NULL,
  [has_cumm_dist] int DEFAULT '0',
  [has_speed] int DEFAULT '0',
  [has_orientation] int DEFAULT '0',
  [dist_unit] float DEFAULT '1000',
  [device_version] int DEFAULT NULL,
  [command_word] varchar(500) DEFAULT NULL,
  [cumm_dist] int DEFAULT NULL,
  [use_cumm_dist] int DEFAULT NULL,
  [manual_adj_tz_min] int DEFAULT NULL,
  [cumm_data_to_meter] float DEFAULT NULL,
  [dist_adj_factor] float DEFAULT '1',
  PRIMARY KEY ([id])
);

CREATE TABLE eta_alert_ty_lookup (
  [id] int NOT NULL DEFAULT '0',
  [name] varchar(128) DEFAULT NULL,
  PRIMARY KEY ([id])
);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE secl_workstation_details (
  [id] int NOT NULL IDENTITY,
  [uid] varchar(128) DEFAULT NULL,
  [name] varchar(64) DEFAULT NULL,
  [code] varchar(64) DEFAULT NULL,
  [workstation_profile_id] int DEFAULT NULL,
  [mines_id] int DEFAULT NULL,
  [challan_series] int DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  [notes] varchar(255) DEFAULT NULL,
  [mines_code] varchar(20) DEFAULT NULL,
  [gate_reader_one_type] int DEFAULT NULL,
  [gate_reader_two_type] int DEFAULT NULL,
  [idle_threshold_sec] int DEFAULT NULL,
  [server_conn_threshold_sec] int DEFAULT NULL,
  [prefered_product] varchar(64) DEFAULT NULL,
  [prefered_grade] varchar(64) DEFAULT NULL,
  [no_remote] int DEFAULT NULL,
  [type] int DEFAULT NULL,
  [road_lr_prefix_first] varchar(10) DEFAULT NULL,
  [road_lr_prefix_second] varchar(10) DEFAULT NULL,
  [washery_lr_prefix_first] varchar(10) DEFAULT NULL,
  [washery_lr_prefix_second] varchar(10) DEFAULT NULL,
  [other_lr_prefix_first] varchar(10) DEFAULT NULL,
  [other_lr_prefix_second] varchar(10) DEFAULT NULL,
  [str_field1] varchar(64) DEFAULT NULL,
  [str_field2] varchar(64) DEFAULT NULL,
  [str_field3] varchar(64) DEFAULT NULL,
  [sap_code] varchar(24) DEFAULT NULL,
  PRIMARY KEY ([id])
) ;


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE spn1939 (
  [id] int NOT NULL IDENTITY,
  [spn_id] int NOT NULL,
  [spn_name] varchar(255) DEFAULT NULL,
  [spn_desc] varchar(255) DEFAULT NULL,
  [spn_len] int DEFAULT NULL,
  [spn_start_byte] smallint DEFAULT '0',
  [spn_end_byte] smallint DEFAULT '0',
  [spn_start_bit] smallint DEFAULT '0',
  [spn_end_bit] smallint DEFAULT '0',
  [spn_resolution] float DEFAULT '0',
  [spn_offset] int DEFAULT NULL,
  [spn_threshold_ischanged] smallint DEFAULT '0',
  [spn_lower_threshold] int DEFAULT '0',
  [spn_upper_threshold] int DEFAULT '0',
  [spn_percent_threshold] int DEFAULT '0',
  [pgn_id] int DEFAULT NULL,
  [spn_time_threshold] int DEFAULT '0',
  PRIMARY KEY ([id])
);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE org_lov_params (
  [port_node_id] int NOT NULL DEFAULT '0',
  [param_id] int NOT NULL DEFAULT '0',
  [param_val] int DEFAULT NULL,
  [seq] int NOT NULL DEFAULT '0',
  PRIMARY KEY ([port_node_id],[param_id],[seq])
) ;


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE org_string_params (
  [port_node_id] int NOT NULL DEFAULT '0',
  [param_id] int NOT NULL DEFAULT '0',
  [param_val] varchar(128) DEFAULT NULL,
  [seq] int NOT NULL DEFAULT '0',
  PRIMARY KEY ([port_node_id],[param_id],[seq])
) ;


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE org_double_params (
  [port_node_id] int NOT NULL DEFAULT '0',
  [param_id] int NOT NULL DEFAULT '0',
  [param_val] float DEFAULT NULL,
  [seq] int NOT NULL DEFAULT '0',
  PRIMARY KEY ([port_node_id],[param_id],[seq])
);



-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE user_roles_scope (
  [PRJ_ID] decimal(38,0) DEFAULT NULL,
  [PORT_NODE_ID] decimal(38,0) DEFAULT NULL,
  [WSPACE_ID] decimal(38,0) DEFAULT NULL,
  [ALL_SCOPE] decimal(38,0) DEFAULT NULL,
  [USER_ROLE_ID] decimal(38,0) NOT NULL,
  [grantable] int DEFAULT NULL
);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE INDEX [user_roles_scope] ON user_roles_scope ([USER_ROLE_ID],[PORT_NODE_ID],[grantable]);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE report_definitions (
  [id] int NOT NULL IDENTITY,
  [page_context] varchar(255) DEFAULT NULL,
  [optional_Menu_Name] varchar(255) DEFAULT NULL,
  [name] varchar(255) DEFAULT NULL,
  [title] varchar(255) DEFAULT NULL,
  [help] varchar(1024) DEFAULT NULL,
  [type] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [for_port_node_id] int DEFAULT NULL,
  [for_user_id] int DEFAULT NULL,
  [master_menu_id] int DEFAULT NULL,
  [no_data] int DEFAULT '0',
  [org_mailing_id] int DEFAULT '1',
  [template_id] int DEFAULT NULL,
  PRIMARY KEY ([id])
);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE menu_report_definition (
  [menu_placeholder_id] int DEFAULT NULL,
  [report_definition_id] int DEFAULT NULL
);



-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE menu_master_report_definition (
  [report_definition_id] int DEFAULT NULL,
  [menu_master_id] int DEFAULT NULL
) ;


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE user_preferences (
  [user_1_id] int NOT NULL DEFAULT '0',
  [name] varchar(256) NOT NULL DEFAULT '',
  [value] varchar(1000) NOT NULL,
  PRIMARY KEY ([user_1_id],[name])
);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE object_role_assignment (
  [user_1_id] int NOT NULL DEFAULT '0',
  [role_id] int NOT NULL DEFAULT '0',
  [object_type] int NOT NULL DEFAULT '0',
  [object_id] int NOT NULL DEFAULT '0',
  PRIMARY KEY ([user_1_id],[role_id],[object_type],[object_id])
);

CREATE TABLE actions_master (
  [id] int NOT NULL IDENTITY,
  [action] varchar(255) DEFAULT NULL,
  PRIMARY KEY ([id])
);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE port_nodes (
  [NAME] varchar(240) DEFAULT NULL,
  [ID] int NOT NULL IDENTITY,
  [PORT_NODE_DESC] varchar(240) DEFAULT NULL,
  [PORT_NODE_ID] decimal(38,0) DEFAULT NULL,
  [FULL_NAME] varchar(1024) DEFAULT NULL,
  [STATUS] decimal(38,0) DEFAULT NULL,
  [DEFAULT_CURRENCY] decimal(38,0) DEFAULT NULL,
  [DEFAULT_DATE_FORMAT] varchar(128) DEFAULT NULL,
  [DEFAULT_REPORTING_CURRENCY] decimal(38,0) DEFAULT NULL,
  [DEFAULT_CURRENCY_SCALE] decimal(38,0) DEFAULT NULL,
  [ORG_TYPE] int DEFAULT NULL,
  [DEFAULT_GROUP_THRESHOLD] float DEFAULT NULL,
  [DEFAULT_THRESHOLD1] float DEFAULT NULL,
  [DEFAULT_THRESHOLD2] float DEFAULT NULL,
  [DEFAULT_THRESHOLD3] float DEFAULT NULL,
  [DEFAULT_THRESHOLD4] float DEFAULT NULL,
  [EXTERNAL_CODE] varchar(128) DEFAULT NULL,
  [hier_level] decimal(38,0) DEFAULT NULL,
  [COUNTRY_CODE] int DEFAULT NULL,
  [CONSOLIDATION_STATUS] int DEFAULT NULL,
  [classify1] int DEFAULT NULL,
  [classify2] int DEFAULT NULL,
  [classify3] int DEFAULT NULL,
  [classify4] int DEFAULT NULL,
  [classify5] int DEFAULT NULL,
  [def_currency_calc] int DEFAULT NULL,
  [def_rep_currency_calc] int DEFAULT NULL,
  [locale_id] int DEFAULT NULL,
  [STR_FIELD1] varchar(240) DEFAULT NULL,
  [STR_FIELD2] varchar(240) DEFAULT NULL,
  [STR_FIELD3] varchar(240) DEFAULT NULL,
  [STR_FIELD4] varchar(240) DEFAULT NULL,
  [STR_FIELD5] varchar(240) DEFAULT NULL,
  [lhs_number] int DEFAULT NULL,
  [rhs_number] int DEFAULT NULL,
  PRIMARY KEY ([ID])
)

CREATE INDEX [rhs_number] ON port_nodes ([rhs_number]);
CREATE INDEX [lhs_number] ON port_nodes ([lhs_number],[ID]);
CREATE INDEX [port_org_type] ON port_nodes ([ORG_TYPE]);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE menu_master_report_definition (
  [report_definition_id] int DEFAULT NULL,
  [menu_master_id] int DEFAULT NULL
)

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE menu_master (
  [id] int NOT NULL IDENTITY,
  [port_node_id] int DEFAULT NULL,
  [user_id] int DEFAULT NULL,
  [menu_tag] varchar(255) DEFAULT NULL,
  [component_file] varchar(255) DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [row_table] int DEFAULT NULL,
  [column_table] int DEFAULT NULL,
  [temp] int DEFAULT NULL,
  [temp2] int DEFAULT NULL,
  [is_mobile] int DEFAULT NULL,
  PRIMARY KEY ([id])
);

CREATE INDEX [port_node_id] ON menu_master ([port_node_id]);
CREATE INDEX [menu_user_tag] ON menu_master ([user_id],[menu_tag],[component_file]);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE user_preferences (
  [user_1_id] int NOT NULL DEFAULT '0',
  [name] varchar(256) NOT NULL DEFAULT '',
  [value] varchar(1000) NOT NULL,
  PRIMARY KEY ([user_1_id],[name])
) ;



-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE object_role_assignment (
  [user_1_id] int NOT NULL DEFAULT '0',
  [role_id] int NOT NULL DEFAULT '0',
  [object_type] int NOT NULL DEFAULT '0',
  [object_id] int NOT NULL DEFAULT '0',
  PRIMARY KEY ([user_1_id],[role_id],[object_type],[object_id])
);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE user_login_track (
  [user_id] int DEFAULT NULL,
  [ts] datetime2(0) DEFAULT NULL,
  [host_ip] varchar(500) DEFAULT NULL
);

CREATE INDEX [user_id_ts] ON user_login_track ([user_id],[ts]);
CREATE INDEX [login_track_ts] ON user_login_track ([ts]);
CREATE INDEX [login_track_uidts] ON user_login_track ([user_id],[ts]);


-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE work_station_details (
  [id] int NOT NULL IDENTITY,
  [workstation_name] varchar(255) DEFAULT NULL,
  [workstation_type] varchar(255) DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [created_by] int DEFAULT NULL,
  [updated_by] int DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [comments] varchar(512) DEFAULT NULL,
  PRIMARY KEY ([id])
);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE work_station_details (
  [id] int NOT NULL IDENTITY,
  [workstation_name] varchar(255) DEFAULT NULL,
  [workstation_type] varchar(255) DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [created_by] int DEFAULT NULL,
  [updated_by] int DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [comments] varchar(512) DEFAULT NULL,
  PRIMARY KEY ([id])
);


CREATE TABLE role (
  [ROLE_DESC] varchar(240) DEFAULT NULL,
  [id] int NOT NULL IDENTITY,
  [NAME] varchar(240) DEFAULT NULL,
  [SCOPE] decimal(10,0) DEFAULT NULL,
  [external_code] varchar(256) DEFAULT NULL,
  [temp] int DEFAULT NULL,
  PRIMARY KEY ([id])
);

insert into users (PJ_ANALYSIS_FILEID, PJ_STATUS_FILEID, PASSWORD, PRT_STATUS_FILEID, NAME, USERNAME, EMAIL, PRT_ANALYSIS_FILEID, PHONE, ISACTIVE, port_node_id, home_server, home_port, home_uid, temp_status, updated_on, created_on, msg_status, custom_msg, last_password_change, allow_next_login, fcm_id, password_field, salted_password, fail_counter) values(NULL,NULL,'test@123',NULL,'TEST','test','test@ipssi.com',NULL,'','1',NULL,NULL,NULL,NULL,NULL,'2016-02-09 00:00:00','2016-02-09 00:00:00',NULL,NULL,'2017-08-30 13:22:35','0',NULL,NULL,NULL,'0');
insert into users ( PASSWORD,  NAME, USERNAME, EMAIL,   ISACTIVE, port_node_id) values('ppgcl@123','ppgcl','ppgcl','ppgcl@gmail.com',1,3);
insert into users ( PASSWORD,  NAME, USERNAME, EMAIL,   ISACTIVE, port_node_id) values('chandra@123','Chandra Prakash Jain','Chandra Prakash','ppgcl@gmail.com',1,3);
insert into users ( PASSWORD,  NAME, USERNAME, EMAIL,   ISACTIVE, port_node_id) values('santosh@123','Santosh Tiwari','Santosh Tiwari','ppgcl@gmail.com',1,3);
insert into users ( PASSWORD,  NAME, USERNAME, EMAIL,   ISACTIVE, port_node_id) values('admin@123','admin','admin','admin@gmail.com',1,3);
insert into users ( PASSWORD,  NAME, USERNAME, EMAIL,   ISACTIVE, port_node_id) values('Indresh@123','Indresh Dubey','Indresh Dubey','admin@gmail.com',1,3);
insert  into cgpl_sales_order(sap_sales_order,sap_sales_order_creation_date,customer_id,sap_customer_sap_code,sap_customer_name,sap_customer_address,sap_material,sap_line_item,transporter_id,transporter_sap_code,basic_price,created_on,created_by,updated_on,updated_by,status,port_node_id,tracking_no,sap_order_quantity,sap_order_unit,sap_sale_order_status,sap_order_lapse_quantity) values 
('0000000003','2021-06-12',2,'0200000000','Anirudh','String 5','String 7',10,NULL,NULL,NULL,'2021-06-12 16:44:17',NULL,'2021-06-12 16:44:17',NULL,1,3,NULL,10000,'TO','1',NULL);



=======================USER PRIVILEDGE============================

SELECT * FROM user_roles;

SELECT * FROM role_privs;

DESC role_privs;

DESC user_roles;

TRUNCATE TABLE user_roles;
// GATE_IN = 80101;
// GATE_OUT = 80102;
// TARE_WB = 80103;
// GROSS_WB = 80104;
// SETTING = 80105;
// TRANPORTER_WINDOW = 80106;
// READ_TAG_WINDOW = 80107;
// TPR_DETAILS_WINDOW = 80108;
// ISSUE_TAG_WINDOW = 80109;
// SALES_ORDER_WINDOW = 80110;
//ALLOW_CREATE_INVOICE = 80111;
INSERT INTO role_privs VALUES (1,80101);
INSERT INTO role_privs VALUES (2,80102);
INSERT INTO role_privs VALUES (3,80103);
INSERT INTO role_privs VALUES (4,80104);
INSERT INTO role_privs VALUES (5,80105);
INSERT INTO role_privs VALUES (6,80106);
INSERT INTO role_privs VALUES (7,80107);
INSERT INTO role_privs VALUES (8,80108);
INSERT INTO role_privs VALUES (9,80109);
INSERT INTO role_privs VALUES (10,80110);
INSERT INTO role_privs VALUES (11,80111);

INSERT INTO user_roles VALUES (3,1);
INSERT INTO user_roles VALUES (3,2);
INSERT INTO user_roles VALUES (3,3);
INSERT INTO user_roles VALUES (3,4);
-- INSERT INTO user_roles VALUES (3,5);
INSERT INTO user_roles VALUES (3,6);
INSERT INTO user_roles VALUES (3,7);
INSERT INTO user_roles VALUES (3,8);
INSERT INTO user_roles VALUES (3,9);
INSERT INTO user_roles VALUES (3,10);
INSERT INTO user_roles VALUES (3,11);





====================Blocking Instructions=====================
CREATE TABLE block_reason_text (
  id INT(11) NOT NULL DEFAULT '0',
  NAME VARCHAR(128) DEFAULT NULL,
  PRIMARY KEY (id)
)
INSERT  INTO block_reason_text(id,NAME) VALUES (2001,'Skipped Step');


INSERT  INTO block_instruction(TYPE,STATUS,port_node_id,material_cat) values (2001,1,3,8);


INSERT INTO user_roles VALUES (2,1);
INSERT INTO user_roles VALUES (2,2);
INSERT INTO user_roles VALUES (2,3);
INSERT INTO user_roles VALUES (2,4);



INSERT INTO user_roles VALUES (3,3);
INSERT INTO user_roles VALUES (3,4);
INSERT INTO user_roles VALUES (3,8);
INSERT INTO user_roles VALUES (3,11);


INSERT INTO user_roles VALUES (4,3);
INSERT INTO user_roles VALUES (4,4);
INSERT INTO user_roles VALUES (4,8);
INSERT INTO user_roles VALUES (4,11);

INSERT INTO user_roles VALUES (5,3);
INSERT INTO user_roles VALUES (5,4);
INSERT INTO user_roles VALUES (5,8);
INSERT INTO user_roles VALUES (5,11);

INSERT INTO user_roles VALUES (6,3);
INSERT INTO user_roles VALUES (6,4);
INSERT INTO user_roles VALUES (6,8);
INSERT INTO user_roles VALUES (6,11);
 -- =========================

INSERT INTO user_roles VALUES (7,1);
INSERT INTO user_roles VALUES (7,2);
INSERT INTO user_roles VALUES (7,8);

INSERT INTO user_roles VALUES (8,1);
INSERT INTO user_roles VALUES (8,2);
INSERT INTO user_roles VALUES (8,8);

INSERT INTO user_roles VALUES (9,1);
INSERT INTO user_roles VALUES (9,2);
INSERT INTO user_roles VALUES (9,8);

INSERT INTO user_roles VALUES (10,1);
INSERT INTO user_roles VALUES (10,2);
INSERT INTO user_roles VALUES (10,8);

INSERT INTO user_roles VALUES (11,1);
INSERT INTO user_roles VALUES (11,2);
INSERT INTO user_roles VALUES (11,8);

-- truncate table vehicle;
-- truncate table vehicle_extended;
-- truncate table tp_record;
-- truncate table tp_record_apprvd;
-- truncate table tp_step;
-- truncate table tp_step_apprvd;
-- truncate table transporter_details;
-- truncate table customer_details;
-- truncate table cgpl_sales_order;
select * from tp_record where tpr_id=2101;
select * from cgpl_sales_order;
-- update cgpl_sales_order set sap_order_lapse_quantity= 125.56 where id=16;

==============================================18 Dec================================================


create table filter_prefs (id int not null IDENTITY, menu_tag varchar(128), user_1_id int , primary key(id));
create table filter_pref_items (filter_pref_id int, varname varchar(28), val varchar(256));
alter table generic_params add column int_val1 int;


CREATE TABLE shift (
  id int NOT NULL IDENTITY,
  name varchar(255) DEFAULT NULL,
  port_node_id int DEFAULT NULL,
  updated_on dateTime NOT NULL DEFAULT  GETDATE(),
  shift_type int DEFAULT NULL,
  temp int DEFAULT NULL,
  prod_hrs float DEFAULT NULL,
   PRIMARY KEY ([id]),
);

CREATE TABLE mines_details (
  id int NOT NULL IDENTITY,
  name varchar(80) DEFAULT NULL,
  status int DEFAULT NULL,
  created_on datetime DEFAULT NULL,
  updated_on datetime DEFAULT NULL,
  updated_by int DEFAULT NULL,
  port_node_id int DEFAULT NULL,
  created_by int DEFAULT NULL,
  supplier_id int DEFAULT NULL,
  sap_code varchar(64) DEFAULT NULL,
  comments varchar(256) DEFAULT NULL,
  challan_prefix varchar(12) DEFAULT NULL,
  opstation_id int DEFAULT NULL,
  sn varchar(20) DEFAULT NULL,
  type int DEFAULT '0',
  parent_mines int DEFAULT NULL,
  parent_sub_area int DEFAULT NULL,
  parent_area int DEFAULT NULL,
  parent_area_code varchar(20) DEFAULT NULL,
  parent_sub_area_code varchar(20) DEFAULT NULL,
  parent_mines_code varchar(20) DEFAULT NULL,
  address varchar(255) DEFAULT NULL,
  tin_number varchar(64) DEFAULT NULL,
  central_excise_reg_no varchar(64) DEFAULT NULL,
  assessee varchar(64) DEFAULT NULL,
  cst_no varchar(64) DEFAULT NULL,
  vat_no varchar(64) DEFAULT NULL,
  project_name varchar(64) DEFAULT NULL,
  address_range varchar(64) DEFAULT NULL,
  address_division varchar(64) DEFAULT NULL,
  commissionerate varchar(64) DEFAULT NULL,
  central_excise_goods varchar DEFAULT NULL,
  dmf_rate float DEFAULT NULL,
  nmet_rate float DEFAULT NULL,
  excise_duty_rate float DEFAULT NULL,
  education_cess_rate float DEFAULT NULL,
  higher_education_cess_rate float DEFAULT NULL,
  min_retry_hours float DEFAULT NULL,
  area_desc varchar DEFAULT NULL,
  gst_no varchar DEFAULT NULL,
  str_field1 varchar DEFAULT NULL,
  str_field2 varchar DEFAULT NULL,
  state varchar DEFAULT NULL,
  state_gst_code varchar DEFAULT NULL,
  unit_code varchar DEFAULT NULL,
  mines_address varchar DEFAULT NULL,
  tare_freq_int float DEFAULT NULL,
  closing_step int DEFAULT NULL,
  src_record_time datetime DEFAULT NULL,
  temp_remote_id int DEFAULT NULL,
  record_src int DEFAULT NULL,
  PRIMARY KEY (id),

) 
INSERT  INTO port_nodes(NAME,PORT_NODE_DESC,PORT_NODE_ID,FULL_NAME,STATUS,DEFAULT_CURRENCY,DEFAULT_DATE_FORMAT,DEFAULT_REPORTING_CURRENCY,DEFAULT_CURRENCY_SCALE,ORG_TYPE,DEFAULT_GROUP_THRESHOLD,DEFAULT_THRESHOLD1,DEFAULT_THRESHOLD2,DEFAULT_THRESHOLD3,DEFAULT_THRESHOLD4,EXTERNAL_CODE,hier_level,COUNTRY_CODE,CONSOLIDATION_STATUS,classify1,classify2,classify3,classify4,classify5,def_currency_calc,def_rep_currency_calc,locale_id,STR_FIELD1,STR_FIELD2,STR_FIELD3,STR_FIELD4,STR_FIELD5,lhs_number,rhs_number) VALUES 
('Unspecified',NULL,2,'Unspecified',1,NULL,'dd/MM/yy',NULL,1,1,1000000,NULL,NULL,NULL,NULL,'UNS',0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'','null','','','',110,2221);

INSERT  INTO port_nodes(NAME,PORT_NODE_DESC,PORT_NODE_ID,FULL_NAME,STATUS,DEFAULT_CURRENCY,DEFAULT_DATE_FORMAT,DEFAULT_REPORTING_CURRENCY,DEFAULT_CURRENCY_SCALE,ORG_TYPE,DEFAULT_GROUP_THRESHOLD,DEFAULT_THRESHOLD1,DEFAULT_THRESHOLD2,DEFAULT_THRESHOLD3,DEFAULT_THRESHOLD4,EXTERNAL_CODE,hier_level,COUNTRY_CODE,CONSOLIDATION_STATUS,classify1,classify2,classify3,classify4,classify5,def_currency_calc,def_rep_currency_calc,locale_id,STR_FIELD1,STR_FIELD2,STR_FIELD3,STR_FIELD4,STR_FIELD5,lhs_number,rhs_number) VALUES 
('ALL',NULL,NULL,'All',1,NULL,'dd/MM/yy',NULL,1,2,1000000,NULL,NULL,NULL,NULL,'All',0,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0,'CBSE','null','','','',1,2222);

INSERT  INTO port_nodes(NAME,PORT_NODE_DESC,PORT_NODE_ID,FULL_NAME,STATUS,DEFAULT_CURRENCY,DEFAULT_DATE_FORMAT,DEFAULT_REPORTING_CURRENCY,DEFAULT_CURRENCY_SCALE,ORG_TYPE,DEFAULT_GROUP_THRESHOLD,DEFAULT_THRESHOLD1,DEFAULT_THRESHOLD2,DEFAULT_THRESHOLD3,DEFAULT_THRESHOLD4,EXTERNAL_CODE,hier_level,COUNTRY_CODE,CONSOLIDATION_STATUS,classify1,classify2,classify3,classify4,classify5,def_currency_calc,def_rep_currency_calc,locale_id,STR_FIELD1,STR_FIELD2,STR_FIELD3,STR_FIELD4,STR_FIELD5,lhs_number,rhs_number) VALUES 
('Cust1','',2,'Cust1',0,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,NULL,'C1',1,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,662,663);
  update [port_nodes] set name = 'Unspecified' , port_node_id=2 ,PORT_NODE_DESC=2  where ID=1;
  update [port_nodes] set name = 'All' , port_node_id=3 ,PORT_NODE_DESC=3  where ID=2;
  update [port_nodes] set name = 'PPGCL' , port_node_id=463 ,PORT_NODE_DESC=463  where ID=3;

  
  CREATE TABLE shift_timings (
  shift_id int DEFAULT NULL,
  valid_start date DEFAULT NULL,
  valid_end date DEFAULT NULL,
  start_hour int DEFAULT NULL,
  start_min int DEFAULT NULL,
  stop_hour int DEFAULT NULL,
  stop_min int DEFAULT NULL
) ;

CREATE TABLE day_table (
  id int NOT NULL IDENTITY,
  label varchar DEFAULT NULL,
  start_time datetime DEFAULT NULL,
  end_time datetime DEFAULT NULL,
  port_node_id int DEFAULT NULL,
  PRIMARY KEY (id),
);

CREATE TABLE supplier_details (
  id int NOT NULL IDENTITY,
  name varchar DEFAULT NULL,
  status int DEFAULT NULL,
  created_on datetime DEFAULT NULL,
  updated_on dateTime NOT NULL DEFAULT GETDATE(),
  updated_by int DEFAULT NULL,
  port_node_id int DEFAULT NULL,
  sap_code varchar DEFAULT NULL,
  comments varchar DEFAULT NULL,
  created_by int DEFAULT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE ui_column (
  [menu_id] int DEFAULT NULL,
  [column_name] varchar(50) DEFAULT NULL,
  [attribute_name] varchar(50) DEFAULT NULL,
  [attribute_value] varchar(50) DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [rollup] smallint DEFAULT NULL,
  [card_index] int DEFAULT NULL,
  [action_index] int DEFAULT NULL,
  [status_index] int DEFAULT NULL,
  [config_str] varchar(512) DEFAULT NULL
)

CREATE INDEX [menu_id] ON ui_column ([menu_id]);

CREATE TABLE ui_parameter (
  [menu_id] int DEFAULT NULL,
  [param_name] varchar(50) DEFAULT NULL,
  [param_value] varchar(50) DEFAULT NULL,
  [updated_on] datetime2(0) DEFAULT NULL,
  [operator] varchar(24) DEFAULT NULL,
  [right_operand] varchar(256) DEFAULT NULL
)

CREATE INDEX [menu_id] ON ui_parameter ([menu_id]);




-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE workstation_types (
  [id] int NOT NULL IDENTITY,
  [name] varchar(128) DEFAULT NULL,
  [comments] varchar(256) DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [created_by] int DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [updated_by] int DEFAULT NULL,
  PRIMARY KEY ([id])
);

-- SQLINES LICENSE FOR EVALUATION USE ONLY
CREATE TABLE colorcode_detail (
  [colorcode_id] int DEFAULT NULL,
  [column_id] char(255) DEFAULT NULL,
  [oder] int DEFAULT NULL,
  [thresholdone] int DEFAULT NULL,
  [thresholdtwo] int DEFAULT NULL,
  [check_for_all] int DEFAULT NULL
) ;

delete from port_nodes where ID >=1;
DBCC CHECKIDENT ('[port_nodes]', RESEED, 0);

update port_nodes set NAME='PPGCL',FULL_NAME='PPGCL',STATUS=1,PORT_NODE_ID=2 where ID=3;
delete from port_nodes where ID >=1;

CREATE TABLE work_station_details (
  [id] int NOT NULL IDENTITY,
  [workstation_name] varchar(255) DEFAULT NULL,
  [workstation_type] varchar(255) DEFAULT NULL,
  [created_on] datetime2(0) DEFAULT NULL,
  [updated_on] datetime2(0) NOT NULL DEFAULT GETDATE(),
  [created_by] int DEFAULT NULL,
  [updated_by] int DEFAULT NULL,
  [port_node_id] int DEFAULT NULL,
  [status] int DEFAULT NULL,
  [comments] varchar(512) DEFAULT NULL,
  PRIMARY KEY ([id])
);