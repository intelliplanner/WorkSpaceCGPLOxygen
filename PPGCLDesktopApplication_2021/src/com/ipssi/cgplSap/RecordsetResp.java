package com.ipssi.cgplSap;
/**
 * RecordsetResp.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */


/**
 * Datatype for Recordset
 */
public class RecordsetResp  implements java.io.Serializable {
    private RecordsetRespIM_RETURN IM_RETURN;

    public RecordsetResp() {
    }

    public RecordsetResp(
           RecordsetRespIM_RETURN IM_RETURN) {
           this.IM_RETURN = IM_RETURN;
    }


    /**
     * Gets the IM_RETURN value for this RecordsetResp.
     * 
     * @return IM_RETURN
     */
    public RecordsetRespIM_RETURN getIM_RETURN() {
        return IM_RETURN;
    }


    /**
     * Sets the IM_RETURN value for this RecordsetResp.
     * 
     * @param IM_RETURN
     */
    public void setIM_RETURN(RecordsetRespIM_RETURN IM_RETURN) {
        this.IM_RETURN = IM_RETURN;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RecordsetResp)) return false;
        RecordsetResp other = (RecordsetResp) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.IM_RETURN==null && other.getIM_RETURN()==null) || 
             (this.IM_RETURN!=null &&
              this.IM_RETURN.equals(other.getIM_RETURN())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getIM_RETURN() != null) {
            _hashCode += getIM_RETURN().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RecordsetResp.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vts.com:SD:VTS:FlyAshInvoice", "RecordsetResp"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IM_RETURN");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IM_RETURN"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vts.com:SD:VTS:FlyAshInvoice", ">RecordsetResp>IM_RETURN"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
