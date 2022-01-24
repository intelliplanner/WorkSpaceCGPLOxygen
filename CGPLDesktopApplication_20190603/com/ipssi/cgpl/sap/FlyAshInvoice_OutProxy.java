package com.ipssi.cgpl.sap;

public class FlyAshInvoice_OutProxy implements FlyAshInvoice_Out {
  private String _endpoint = null;
  private FlyAshInvoice_Out flyAshInvoice_Out = null;
  
  public FlyAshInvoice_OutProxy() {
    _initFlyAshInvoice_OutProxy();
  }
  
  public FlyAshInvoice_OutProxy(String endpoint) {
    _endpoint = endpoint;
    _initFlyAshInvoice_OutProxy();
  }
  
  private void _initFlyAshInvoice_OutProxy() {
    try {
      flyAshInvoice_Out = (new FlyAshInvoice_OutServiceLocator()).getHTTPS_Port();
      if (flyAshInvoice_Out != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)flyAshInvoice_Out)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)flyAshInvoice_Out)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (flyAshInvoice_Out != null)
      ((javax.xml.rpc.Stub)flyAshInvoice_Out)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public FlyAshInvoice_Out getFlyAshInvoice_Out() {
    if (flyAshInvoice_Out == null)
      _initFlyAshInvoice_OutProxy();
    return flyAshInvoice_Out;
  }
  
  public RecordsetResp flyAshInvoice_Out(Recordset recordsetRequest) throws java.rmi.RemoteException{
    if (flyAshInvoice_Out == null)
      _initFlyAshInvoice_OutProxy();
    return flyAshInvoice_Out.flyAshInvoice_Out(recordsetRequest);
  }
  
  
}