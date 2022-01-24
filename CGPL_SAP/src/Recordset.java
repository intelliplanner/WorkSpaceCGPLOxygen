/**
 * Recordset.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */


/**
 * Datatype for Recordset
 */
public class Recordset  implements java.io.Serializable {
    private RecordsetIM_INVOICE_DETAILS IM_INVOICE_DETAILS;

    private java.lang.String IM_TPRID;

    public Recordset() {
    }

    public Recordset(
           RecordsetIM_INVOICE_DETAILS IM_INVOICE_DETAILS,
           java.lang.String IM_TPRID) {
           this.IM_INVOICE_DETAILS = IM_INVOICE_DETAILS;
           this.IM_TPRID = IM_TPRID;
    }


    /**
     * Gets the IM_INVOICE_DETAILS value for this Recordset.
     * 
     * @return IM_INVOICE_DETAILS
     */
    public RecordsetIM_INVOICE_DETAILS getIM_INVOICE_DETAILS() {
        return IM_INVOICE_DETAILS;
    }


    /**
     * Sets the IM_INVOICE_DETAILS value for this Recordset.
     * 
     * @param IM_INVOICE_DETAILS
     */
    public void setIM_INVOICE_DETAILS(RecordsetIM_INVOICE_DETAILS IM_INVOICE_DETAILS) {
        this.IM_INVOICE_DETAILS = IM_INVOICE_DETAILS;
    }


    /**
     * Gets the IM_TPRID value for this Recordset.
     * 
     * @return IM_TPRID
     */
    public java.lang.String getIM_TPRID() {
        return IM_TPRID;
    }


    /**
     * Sets the IM_TPRID value for this Recordset.
     * 
     * @param IM_TPRID
     */
    public void setIM_TPRID(java.lang.String IM_TPRID) {
        this.IM_TPRID = IM_TPRID;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Recordset)) return false;
        Recordset other = (Recordset) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.IM_INVOICE_DETAILS==null && other.getIM_INVOICE_DETAILS()==null) || 
             (this.IM_INVOICE_DETAILS!=null &&
              this.IM_INVOICE_DETAILS.equals(other.getIM_INVOICE_DETAILS()))) &&
            ((this.IM_TPRID==null && other.getIM_TPRID()==null) || 
             (this.IM_TPRID!=null &&
              this.IM_TPRID.equals(other.getIM_TPRID())));
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
        if (getIM_INVOICE_DETAILS() != null) {
            _hashCode += getIM_INVOICE_DETAILS().hashCode();
        }
        if (getIM_TPRID() != null) {
            _hashCode += getIM_TPRID().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Recordset.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vts.com:SD:VTS:FlyAshInvoice", "Recordset"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IM_INVOICE_DETAILS");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IM_INVOICE_DETAILS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://vts.com:SD:VTS:FlyAshInvoice", ">Recordset>IM_INVOICE_DETAILS"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IM_TPRID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "IM_TPRID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
