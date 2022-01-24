-- ========================================================

DELIMITER $$

DROP PROCEDURE IF EXISTS `ipssi_cgpl`.`InsertOrUpdateSalesOrder`$$

CREATE DEFINER=`root`@`localhost` PROCEDURE `InsertOrUpdateSalesOrder`(IN _sap_sales_order VARCHAR(64), IN _sap_sales_order_creation_date TIMESTAMP,  
IN _sap_customer_sap_code VARCHAR(64),  IN _sap_customer_name VARCHAR(64), IN _sap_customer_address VARCHAR(512),IN _sap_line_item INT(11),
IN _sap_material VARCHAR(64), IN _transporter_sap_code VARCHAR(64),  IN _sap_order_quantity DOUBLE,  IN _sap_order_unit VARCHAR(64), 
IN _sap_sale_order_status VARCHAR(64))

BEGIN
INSERT INTO cgpl_sales_order (sap_sales_order, sap_sales_order_creation_date,  sap_customer_sap_code,  sap_customer_name, sap_customer_address,sap_line_item ,sap_material, transporter_sap_code,  sap_order_quantity,  sap_order_unit,  sap_sale_order_status) 
VALUES (_sap_sales_order, _sap_sales_order_creation_date,  _sap_customer_sap_code,  _sap_customer_name, _sap_customer_address, _sap_line_item , _sap_material, _transporter_sap_code,  _sap_order_quantity,  _sap_order_unit,  _sap_sale_order_status) ON DUPLICATE KEY UPDATE 
sap_sales_order_creation_date=VALUES(sap_sales_order_creation_date), sap_customer_sap_code=VALUES(sap_customer_sap_code),sap_customer_name=VALUES(sap_customer_name), sap_customer_address=VALUES(sap_customer_address), sap_material=VALUES(sap_material), transporter_sap_code= VALUES(transporter_sap_code),  sap_order_quantity=VALUES(sap_order_quantity),  sap_order_unit=VALUES(sap_order_unit), sap_sale_order_status=VALUES(sap_sale_order_status);
END$$

DELIMITER ;

-- ===================================

DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`upd_upo_ins_customer_details`$$

CREATE TRIGGER `ipssi_cgpl_dev`.`upd_upo_ins_customer_details` BEFORE INSERT ON `ipssi_cgpl_dev`.`customer_details` 
FOR EACH ROW BEGIN
IF (new.gst_no IS NOT NULL AND new.gst_no <> '') THEN
  SET new.state_gst_code = LEFT(new.gst_no,2);
END IF;
SET new.updated_on = NOW();
END;
$$

DELIMITER ;

================================

DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`upd_upo_upd_customer_details`$$

CREATE TRIGGER `ipssi_cgpl_dev`.`upd_upo_upd_customer_details` BEFORE UPDATE ON `ipssi_cgpl_dev`.`customer_details` 
FOR EACH ROW BEGIN
IF (new.gst_no IS NOT NULL AND new.gst_no <> '') THEN
  SET new.state_gst_code = LEFT(new.gst_no,2);
END IF;
SET new.updated_on = NOW();
END;
$$

DELIMITER ;

-- ==============================

ALTER TABLE tp_record ADD COLUMN ex_invoice VARCHAR(255);
ALTER TABLE tp_record ADD COLUMN message VARCHAR(255);

ALTER TABLE tp_record_apprvd ADD COLUMN ex_invoice VARCHAR(255);
ALTER TABLE tp_record_apprvd ADD COLUMN message VARCHAR(255);


ALTER TABLE driver_details ADD CONSTRAINT driver_dl_number UNIQUE
(driver_dl_number);

-- ===============================================================

DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`BeforeInsertCGPLSales`$$

CREATE TRIGGER `ipssi_cgpl`.`BeforeInsertCGPLSales` BEFORE INSERT ON `ipssi_cgpl`.`cgpl_sales_order`
FOR EACH ROW BEGIN

DECLARE sap_codeq VARCHAR(128);
IF (new.status  IS NULL) THEN
SET new.status = 1;
END IF;
IF (new.port_node_id  IS NULL) THEN
SET new.port_node_id = 463;
END IF;	

IF (new.sap_customer_sap_code IS NOT NULL) THEN
  SET sap_codeq = (SELECT sap_code FROM customer_details  WHERE sap_code = new.sap_customer_sap_code LIMIT 1);

IF (sap_codeq IS NULL) THEN
    INSERT INTO customer_details (NAME, sap_code,STATUS,port_node_id) VALUE (new.sap_customer_name,new.sap_customer_sap_code,1,463);
  END IF;
END IF;

END$$

DELIMITER ;

-- === ------------------------------------------------------------------------------


DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`upd_upo_ins_customer_details`$$

CREATE TRIGGER `ipssi_cgpl`.`upd_upo_ins_customer_details` BEFORE INSERT ON `ipssi_cgpl`.`customer_details` 
FOR EACH ROW BEGIN
IF (new.gst_no IS NOT NULL AND new.gst_no <> '') THEN
  SET new.state_gst_code = LEFT(new.gst_no,2);
END IF;
SET new.updated_on = NOW();
END;
$$

DELIMITER ;

-------------------------------------------

DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`updateAfterInsertCGPL`$$

CREATE TRIGGER `ipssi_cgpl`.`updateAfterInsertCGPL` BEFORE INSERT ON `ipssi_cgpl`.`cgpl_sales_order` 
FOR EACH ROW BEGIN
DECLARE sap_codeq VARCHAR(128);
IF (new.status  IS NULL) THEN
SET new.status = 1;
END IF;
IF (new.port_node_id  IS NULL) THEN
SET new.port_node_id = 463;
END IF;	
IF (new.sap_customer_sap_code IS NOT NULL) THEN
  SET sap_codeq = (SELECT sap_code FROM customer_details  WHERE sap_code = new.sap_customer_sap_code LIMIT 1);
IF (sap_codeq IS NULL) THEN
    INSERT INTO customer_details (NAME, sap_code,STATUS,port_node_id) VALUE (new.sap_customer_name,new.sap_customer_sap_code,1,463);
  END IF;
END IF;
 
  SET new.customer_id = (SELECT id FROM customer_details WHERE sap_code = new.sap_customer_sap_code);
END;
$$

DELIMITER ;

---------------------------------------------------
DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`updateAfterInsertCGPL`$$

CREATE TRIGGER `ipssi_cgpl`.`updateAfterInsertCGPL` BEFORE INSERT ON `ipssi_cgpl`.`cgpl_sales_order` 
FOR EACH ROW BEGIN
DECLARE sap_codeq VARCHAR(128);
IF (new.status  IS NULL) THEN
SET new.status = 1;
END IF;
IF (new.port_node_id  IS NULL) THEN
SET new.port_node_id = 463;
END IF;	
IF (new.sap_customer_sap_code IS NOT NULL) THEN
  SET sap_codeq = (SELECT sap_code FROM customer_details  WHERE sap_code = new.sap_customer_sap_code LIMIT 1);
IF (sap_codeq IS NULL) THEN
    INSERT INTO customer_details (NAME, sap_code,STATUS,port_node_id) VALUE (new.sap_customer_name,new.sap_customer_sap_code,1,463);
  END IF;
END IF;
 
  SET new.customer_id = (SELECT id FROM customer_details WHERE sap_code = new.sap_customer_sap_code);
END;
$$

DELIMITER ;

===============================================================

DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`TPRecordAfterInsert`$$

CREATE TRIGGER `ipssi_cgpl`.`TPRecordAfterInsert` BEFORE INSERT ON `ipssi_cgpl`.`tp_record` 
FOR EACH ROW BEGIN
SET new.supplier_id = (SELECT seller FROM do_rr_details WHERE id = new.do_id);
IF (new.port_node_id IS NULL) THEN
   SET new.port_node_id=463;
END IF;
IF (new.material_cat IS NULL) THEN
   SET new.material_cat = 0;
END IF;
IF (new.load_tare > 1000) THEN
   SET new.load_tare = new.load_tare/1000;
END IF;
IF (new.load_gross > 1000) THEN
   SET new.load_gross = new.load_gross/1000;
END IF;
IF (new.unload_tare > 1000) THEN
   SET new.unload_tare = new.unload_tare/1000;
END IF;
IF (new.unload_gross > 1000) THEN
   SET new.unload_gross = new.unload_gross/1000;
END IF;
IF (new.rf_load_tare > 1000) THEN
   SET new.rf_load_tare = new.rf_load_tare/1000;
END IF;
IF (new.rf_load_gross > 1000) THEN
   SET new.rf_load_gross = new.rf_load_gross/1000;
END IF;
IF (new.material_cat = 0) THEN
	SET new.combo_start = LEAST(
	 COALESCE(new.earliest_unload_gate_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_gate_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)) 
	 );
	SET new.combo_end = GREATEST(
	 COALESCE(new.earliest_unload_gate_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_gate_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)) 
	 );
        IF (new.combo_start > new.combo_end) THEN
           SET new.combo_start = NULL;
           SET new.combo_end = NULL;
         END IF;
ELSE
	SET new.combo_start = LEAST(
	 COALESCE(new.earliest_load_gate_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_gate_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_out_out,CAST('2027-01-01 00:00:00' AS DATETIME))
	 );
	SET new.combo_end = GREATEST(
	 COALESCE(new.earliest_load_gate_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_gate_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)) 
	 );
	IF (new.combo_start > new.combo_end) THEN
           SET new.combo_start = NULL;
           SET new.combo_end = NULL;
	 END IF;
END IF;
IF (new.material_cat = 0) THEN
  IF (new.latest_unload_gate_out_out IS NOT NULL) THEN
     SET new.is_latest = 1;
  END IF;
ELSE
  IF (new.latest_load_gate_out_out IS NOT NULL) THEN
     SET new.tpr_status = 2;
  END IF;
END IF;
IF (new.combo_start IS NOT NULL AND new.reporting_status = 1) THEN
  SET new.reporting_status = 2;
END IF;
IF (new.tpr_status = 2 AND new.reporting_status IN (1,2)) THEN
  SET new.reporting_status = 3;
END IF;
IF (new.vehicle_name IS NULL) THEN
  SET new.vehicle_name =(SELECT NAME FROM vehicle WHERE STATUS=1 AND vehicle.id=new.vehicle_id);
END IF;
IF (new.washery_code IS NULL) THEN
  SET new.washery_code = '26219000';
END IF;
IF (new.consignee_name IS NULL) THEN
  SET new.consignee_name = (SELECT NAME FROM customer_details WHERE id = new.consignee_id);
END IF;
IF (new.transporter_code IS NULL) THEN
  SET new.transporter_code = (SELECT NAME FROM transporter_details WHERE STATUS=1 AND id = new.transporter_id);
END IF;
IF (new.tpr_status = 0 AND new.is_latest=1) THEN
	INSERT INTO current_vehicle_tpr (vehicle_id,tpr_id) VALUES (NEW.vehicle_id, NEW.tpr_id) ON DUPLICATE KEY UPDATE current_vehicle_tpr.tpr_id=NEW.tpr_id;
END IF;
SET @tprID := (SELECT AUTO_INCREMENT FROM information_schema.tables WHERE table_schema = 'ipssi_maithon' AND table_name = 'tp_record');
IF (new.material_cat = 2) THEN
                SET new.challan_no = CONCAT('MPF-',@tprID);
ELSEIF (new.material_cat = 1) THEN
                SET new.challan_no = CONCAT('MPS-',@tprID);
ELSEIF (new.material_cat = 3) THEN
                SET new.challan_no = CONCAT('MP0-',@tprID);
END IF;
IF (new.material_cat <> 0 AND new.challan_date IS NULL ) THEN 
	SET new.challan_date = NOW();
END IF;
END;
$$

DELIMITER ;

===========================================================

DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`updateCurrentVehicleTPR`$$

CREATE TRIGGER `ipssi_cgpl`.`updateCurrentVehicleTPR` BEFORE UPDATE ON `ipssi_cgpl`.`tp_record` 
FOR EACH ROW BEGIN
IF (new.material_cat = 8) THEN
	SET new.combo_start = LEAST(
	 COALESCE(new.earliest_load_gate_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_gate_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_out_out,CAST('2027-01-01 00:00:00' AS DATETIME))
	 );
	SET new.combo_end = GREATEST(
	 COALESCE(new.earliest_load_gate_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_yard_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_yard_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_wb_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_wb_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_load_gate_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_load_gate_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)) 
	 );
       IF (new.combo_start > new.combo_end) THEN
           SET new.combo_start = NULL;
           SET new.combo_end = NULL;
         END IF;
ELSE
SET new.combo_start = LEAST(
	 COALESCE(new.earliest_unload_gate_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_in_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_in_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_gate_out_in,CAST('2027-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_out_out,CAST('2027-01-01 00:00:00' AS DATETIME)) 
	 );
	SET new.combo_end = GREATEST(
	 COALESCE(new.earliest_unload_gate_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_reg_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_reg_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_in_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_in_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_yard_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_yard_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_wb_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_wb_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.earliest_unload_gate_out_in,CAST('1990-01-01 00:00:00' AS DATETIME)), 
	 COALESCE(new.latest_unload_gate_out_out,CAST('1990-01-01 00:00:00' AS DATETIME)) 
	 );
	IF (new.combo_start > new.combo_end) THEN
           SET new.combo_start = NULL;
           SET new.combo_end = NULL;
         END IF;
END IF;
IF (new.tpr_status = 0 AND new.is_latest=1) THEN
	INSERT INTO current_vehicle_tpr (vehicle_id,tpr_id) VALUES (NEW.vehicle_id, NEW.tpr_id) ON DUPLICATE KEY UPDATE current_vehicle_tpr.tpr_id=NEW.tpr_id;
END IF;
IF(new.combo_start  IS NULL) THEN	
	   SET new.combo_start = (NOW()-INTERVAL 10 MINUTE);
END IF;
IF(new.combo_end IS NULL) THEN	
           SET new.combo_end = NOW();	 
END IF;
END;
$$

DELIMITER ;