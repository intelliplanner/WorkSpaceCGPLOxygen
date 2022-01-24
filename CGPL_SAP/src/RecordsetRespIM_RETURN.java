/**
 * RecordsetRespIM_RETURN.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

public class RecordsetRespIM_RETURN  implements java.io.Serializable {
    private java.lang.String EX_INVOICE;

    private java.lang.String TYPE;

    private java.lang.String MESSAGE;

    public RecordsetRespIM_RETURN() {
    }

    public RecordsetRespIM_RETURN(
           java.lang.String EX_INVOICE,
           java.lang.String TYPE,
           java.lang.String MESSAGE) {
           this.EX_INVOICE = EX_INVOICE;
           this.TYPE = TYPE;
           this.MESSAGE = MESSAGE;
    }


    /**
     * Gets the EX_INVOICE value for this RecordsetRespIM_RETURN.
     * 
     * @return EX_INVOICE
     */
    public java.lang.String getEX_INVOICE() {
        return EX_INVOICE;
    }


    /**
     * Sets the EX_INVOICE value for this RecordsetRespIM_RETURN.
     * 
     * @param EX_INVOICE
     */
    public void setEX_INVOICE(java.lang.String EX_INVOICE) {
        this.EX_INVOICE = EX_INVOICE;
    }


    /**
     * Gets the TYPE value for this RecordsetRespIM_RETURN.
     * 
     * @return TYPE
     */
    public java.lang.String getTYPE() {
        return TYPE;
    }


    /**
     * Sets the TYPE value for this RecordsetRespIM_RETURN.
     * 
     * @param TYPE
     */
    public void setTYPE(java.lang.String TYPE) {
        this.TYPE = TYPE;
    }


    /**
     * Gets the MESSAGE value for this RecordsetRespIM_RETURN.
     * 
     * @return MESSAGE
     */
    public java.lang.String getMESSAGE() {
        return MESSAGE;
    }


    /**
     * Sets the MESSAGE value for this RecordsetRespIM_RETURN.
     * 
     * @param MESSAGE
     */
    public void setMESSAGE(java.lang.String MESSAGE) {
        this.MESSAGE = MESSAGE;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RecordsetRespIM_RETURN)) return false;
        RecordsetRespIM_RETURN other = (RecordsetRespIM_RETURN) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.EX_INVOICE==null && other.getEX_INVOICE()==null) || 
             (this.EX_INVOICE!=null &&
              this.EX_INVOICE.equals(other.getEX_INVOICE()))) &&
            ((this.TYPE==null && other.getTYPE()==null) || 
             (this.TYPE!=null &&
              this.TYPE.equals(other.getTYPE()))) &&
            ((this.MESSAGE==null && other.getMESSAGE()==null) || 
             (this.MESSAGE!=null &&
              this.MESSAGE.equals(other.getMESSAGE())));
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
        if (getEX_INVOICE() != null) {
            _hashCode += getEX_INVOICE().hashCode();
        }
        if (getTYPE() != null) {
            _hashCode += getTYPE().hashCode();
        }
        if (getMESSAGE() != null) {
            _hashCode += getMESSAGE().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RecordsetRespIM_RETURN.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://vts.com:SD:VTS:FlyAshInvoice", ">RecordsetResp>IM_RETURN"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("EX_INVOICE");
        elemField.setXmlName(new javax.xml.namespace.QName("", "EX_INVOICE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("TYPE");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TYPE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MESSAGE");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MESSAGE"));
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
