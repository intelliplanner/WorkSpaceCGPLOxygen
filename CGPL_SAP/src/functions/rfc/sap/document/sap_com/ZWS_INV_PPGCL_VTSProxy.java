package functions.rfc.sap.document.sap_com;

public class ZWS_INV_PPGCL_VTSProxy implements functions.rfc.sap.document.sap_com.ZWS_INV_PPGCL_VTS_PortType {
  private String _endpoint = null;
  private functions.rfc.sap.document.sap_com.ZWS_INV_PPGCL_VTS_PortType zWS_INV_PPGCL_VTS_PortType = null;
  
  public ZWS_INV_PPGCL_VTSProxy() {
    _initZWS_INV_PPGCL_VTSProxy();
  }
  
  public ZWS_INV_PPGCL_VTSProxy(String endpoint) {
    _endpoint = endpoint;
    _initZWS_INV_PPGCL_VTSProxy();
  }
  
  private void _initZWS_INV_PPGCL_VTSProxy() {
    try {
      zWS_INV_PPGCL_VTS_PortType = (new functions.rfc.sap.document.sap_com.ZWS_INV_PPGCL_VTS_ServiceLocator()).getZVTS_SERV_BINDING();
      if (zWS_INV_PPGCL_VTS_PortType != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)zWS_INV_PPGCL_VTS_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)zWS_INV_PPGCL_VTS_PortType)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (zWS_INV_PPGCL_VTS_PortType != null)
      ((javax.xml.rpc.Stub)zWS_INV_PPGCL_VTS_PortType)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public functions.rfc.sap.document.sap_com.ZWS_INV_PPGCL_VTS_PortType getZWS_INV_PPGCL_VTS_PortType() {
    if (zWS_INV_PPGCL_VTS_PortType == null)
      _initZWS_INV_PPGCL_VTSProxy();
    return zWS_INV_PPGCL_VTS_PortType;
  }
  
  public void ZFM_CREATE_INV_PPGCL_VTS(functions.rfc.sap.document.sap_com.ZST_INV_DETAILS_PPGCL_VTS IM_INVOICE_DETAILS, java.lang.String IM_TPRID, functions.rfc.sap.document.sap_com.holders.BAPIRET1_TABHolder ET_RETURN, javax.xml.rpc.holders.StringHolder EX_INVOICE) throws java.rmi.RemoteException{
    if (zWS_INV_PPGCL_VTS_PortType == null)
      _initZWS_INV_PPGCL_VTSProxy();
    zWS_INV_PPGCL_VTS_PortType.ZFM_CREATE_INV_PPGCL_VTS(IM_INVOICE_DETAILS, IM_TPRID, ET_RETURN, EX_INVOICE);
  }
  
  
}