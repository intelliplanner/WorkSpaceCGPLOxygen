DELIMITER $$

DROP PROCEDURE IF EXISTS `ipssi_cgpl`.`InsertOrUpdateSalesOrder`$$

CREATE PROCEDURE `ipssi_cgpl`.`InsertOrUpdateSalesOrder`(IN _sap_sales_order VARCHAR(64), IN _sap_sales_order_creation_date TIMESTAMP,  IN _sap_customer_sap_code VARCHAR(64),  IN _sap_customer_name VARCHAR(64), IN _sap_customer_address VARCHAR(512),IN _sap_line_item int(11),IN _sap_material VARCHAR(64), IN _transporter_sap_code VARCHAR(64),  IN _sap_order_quantity double,  IN _sap_order_unit VARCHAR(64),  IN _sap_sale_order_status VARCHAR(64))
BEGIN
insert into cgpl_sales_order (sap_sales_order, sap_sales_order_creation_date,  sap_customer_sap_code,  sap_customer_name, sap_customer_address,sap_line_item ,sap_material, transporter_sap_code,  sap_order_quantity,  sap_order_unit,  sap_sale_order_status) 

values (_sap_sales_order, _sap_sales_order_creation_date,  _sap_customer_sap_code,  _sap_customer_name, _sap_customer_address, _sap_line_item , _sap_material, _transporter_sap_code,  _sap_order_quantity,  _sap_order_unit,  _sap_sale_order_status) ON DUPLICATE KEY UPDATE 
sap_sales_order_creation_date=values(sap_sales_order_creation_date), sap_customer_sap_code=values(sap_customer_sap_code),sap_customer_name=values(sap_customer_name), sap_customer_address=values(sap_customer_address), sap_material=values(sap_material), transporter_sap_code= values(transporter_sap_code),  sap_order_quantity=values(sap_order_quantity),  sap_order_unit=values(sap_order_unit), sap_sale_order_status=values(sap_sale_order_status);

END$$

DELIMITER ;


========================================================


alter table tp_record add column ex_invoice varchar(255);
alter table tp_record add column message varchar(255);

alter table tp_record_apprvd add column ex_invoice varchar(255);
alter table tp_record_apprvd add column message varchar(255);



alter table driver_details ADD CONSTRAINT driver_dl_number UNIQUE
(driver_dl_number);


===============================================================


DELIMITER $$

DROP TRIGGER `ipssi_cgpl`.`BeforeInsertCGPLSales`$$

CREATE TRIGGER `ipssi_cgpl`.`BeforeInsertCGPLSales` BEFORE INSERT on `ipssi_cgpl`.`cgpl_sales_order`
FOR EACH ROW BEGIN

declare sap_codeq varchar(128);
if (new.status  is null) then
set new.status = 1;
end if;
if (new.port_node_id  is null) then
set new.port_node_id = 463;
end if;	

if (new.sap_customer_sap_code is not null) then
  set sap_codeq = (select sap_code from customer_details  where sap_code = new.sap_customer_sap_code limit 1);

if (sap_codeq is null) then
    insert into customer_details (name, sap_code,status,port_node_id) value (new.sap_customer_name,new.sap_customer_sap_code,1,463);
  end if;
end if;

END$$

DELIMITER ;