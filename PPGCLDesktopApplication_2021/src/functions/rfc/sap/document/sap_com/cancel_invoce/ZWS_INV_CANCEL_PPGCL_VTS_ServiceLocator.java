/**
 * ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package functions.rfc.sap.document.sap_com.cancel_invoce;

public class ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator extends org.apache.axis.client.Service implements functions.rfc.sap.document.sap_com.cancel_invoce.ZWS_INV_CANCEL_PPGCL_VTS_Service {

    public ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator() {
    }


    public ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ZWS_INV_CANCEL_PPGCL_VTS_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ZVTS_SERV_BINDING
    private java.lang.String ZVTS_SERV_BINDING_address = "http://tpcped.tpc.co.in:8000/sap/bc/srt/rfc/sap/zws_inv_cancel_ppgcl_vts/400/zws_inv_cancel_ppgcl_vts/zvts_serv_binding";

    public java.lang.String getZVTS_SERV_BINDINGAddress() {
        return ZVTS_SERV_BINDING_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ZVTS_SERV_BINDINGWSDDServiceName = "ZVTS_SERV_BINDING";

    public java.lang.String getZVTS_SERV_BINDINGWSDDServiceName() {
        return ZVTS_SERV_BINDINGWSDDServiceName;
    }

    public void setZVTS_SERV_BINDINGWSDDServiceName(java.lang.String name) {
        ZVTS_SERV_BINDINGWSDDServiceName = name;
    }

    public functions.rfc.sap.document.sap_com.cancel_invoce.ZWS_INV_CANCEL_PPGCL_VTS_PortType getZVTS_SERV_BINDING() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ZVTS_SERV_BINDING_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getZVTS_SERV_BINDING(endpoint);
    }

    public functions.rfc.sap.document.sap_com.cancel_invoce.ZWS_INV_CANCEL_PPGCL_VTS_PortType getZVTS_SERV_BINDING(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            functions.rfc.sap.document.sap_com.cancel_invoce.ZVTS_SERV_BINDINGStub _stub = new functions.rfc.sap.document.sap_com.cancel_invoce.ZVTS_SERV_BINDINGStub(portAddress, this);
            _stub.setPortName(getZVTS_SERV_BINDINGWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setZVTS_SERV_BINDINGEndpointAddress(java.lang.String address) {
        ZVTS_SERV_BINDING_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (functions.rfc.sap.document.sap_com.cancel_invoce.ZWS_INV_CANCEL_PPGCL_VTS_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                functions.rfc.sap.document.sap_com.cancel_invoce.ZVTS_SERV_BINDINGStub _stub = new functions.rfc.sap.document.sap_com.cancel_invoce.ZVTS_SERV_BINDINGStub(new java.net.URL(ZVTS_SERV_BINDING_address), this);
                _stub.setPortName(getZVTS_SERV_BINDINGWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("ZVTS_SERV_BINDING".equals(inputPortName)) {
            return getZVTS_SERV_BINDING();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:sap-com:document:sap:rfc:functions", "ZWS_INV_CANCEL_PPGCL_VTS");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:sap-com:document:sap:rfc:functions", "ZVTS_SERV_BINDING"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ZVTS_SERV_BINDING".equals(portName)) {
            setZVTS_SERV_BINDINGEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
