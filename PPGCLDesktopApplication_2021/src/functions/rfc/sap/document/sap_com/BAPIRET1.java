/**
 * BAPIRET1.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package functions.rfc.sap.document.sap_com;

public class BAPIRET1  implements java.io.Serializable {
    private java.lang.String TYPE;

    private java.lang.String ID;

    private java.lang.String NUMBER;

    private java.lang.String MESSAGE;

    private java.lang.String LOG_NO;

    private java.lang.String LOG_MSG_NO;

    private java.lang.String MESSAGE_V1;

    private java.lang.String MESSAGE_V2;

    private java.lang.String MESSAGE_V3;

    private java.lang.String MESSAGE_V4;

    public BAPIRET1() {
    }

    public BAPIRET1(
           java.lang.String TYPE,
           java.lang.String ID,
           java.lang.String NUMBER,
           java.lang.String MESSAGE,
           java.lang.String LOG_NO,
           java.lang.String LOG_MSG_NO,
           java.lang.String MESSAGE_V1,
           java.lang.String MESSAGE_V2,
           java.lang.String MESSAGE_V3,
           java.lang.String MESSAGE_V4) {
           this.TYPE = TYPE;
           this.ID = ID;
           this.NUMBER = NUMBER;
           this.MESSAGE = MESSAGE;
           this.LOG_NO = LOG_NO;
           this.LOG_MSG_NO = LOG_MSG_NO;
           this.MESSAGE_V1 = MESSAGE_V1;
           this.MESSAGE_V2 = MESSAGE_V2;
           this.MESSAGE_V3 = MESSAGE_V3;
           this.MESSAGE_V4 = MESSAGE_V4;
    }


    /**
     * Gets the TYPE value for this BAPIRET1.
     * 
     * @return TYPE
     */
    public java.lang.String getTYPE() {
        return TYPE;
    }


    /**
     * Sets the TYPE value for this BAPIRET1.
     * 
     * @param TYPE
     */
    public void setTYPE(java.lang.String TYPE) {
        this.TYPE = TYPE;
    }


    /**
     * Gets the ID value for this BAPIRET1.
     * 
     * @return ID
     */
    public java.lang.String getID() {
        return ID;
    }


    /**
     * Sets the ID value for this BAPIRET1.
     * 
     * @param ID
     */
    public void setID(java.lang.String ID) {
        this.ID = ID;
    }


    /**
     * Gets the NUMBER value for this BAPIRET1.
     * 
     * @return NUMBER
     */
    public java.lang.String getNUMBER() {
        return NUMBER;
    }


    /**
     * Sets the NUMBER value for this BAPIRET1.
     * 
     * @param NUMBER
     */
    public void setNUMBER(java.lang.String NUMBER) {
        this.NUMBER = NUMBER;
    }


    /**
     * Gets the MESSAGE value for this BAPIRET1.
     * 
     * @return MESSAGE
     */
    public java.lang.String getMESSAGE() {
        return MESSAGE;
    }


    /**
     * Sets the MESSAGE value for this BAPIRET1.
     * 
     * @param MESSAGE
     */
    public void setMESSAGE(java.lang.String MESSAGE) {
        this.MESSAGE = MESSAGE;
    }


    /**
     * Gets the LOG_NO value for this BAPIRET1.
     * 
     * @return LOG_NO
     */
    public java.lang.String getLOG_NO() {
        return LOG_NO;
    }


    /**
     * Sets the LOG_NO value for this BAPIRET1.
     * 
     * @param LOG_NO
     */
    public void setLOG_NO(java.lang.String LOG_NO) {
        this.LOG_NO = LOG_NO;
    }


    /**
     * Gets the LOG_MSG_NO value for this BAPIRET1.
     * 
     * @return LOG_MSG_NO
     */
    public java.lang.String getLOG_MSG_NO() {
        return LOG_MSG_NO;
    }


    /**
     * Sets the LOG_MSG_NO value for this BAPIRET1.
     * 
     * @param LOG_MSG_NO
     */
    public void setLOG_MSG_NO(java.lang.String LOG_MSG_NO) {
        this.LOG_MSG_NO = LOG_MSG_NO;
    }


    /**
     * Gets the MESSAGE_V1 value for this BAPIRET1.
     * 
     * @return MESSAGE_V1
     */
    public java.lang.String getMESSAGE_V1() {
        return MESSAGE_V1;
    }


    /**
     * Sets the MESSAGE_V1 value for this BAPIRET1.
     * 
     * @param MESSAGE_V1
     */
    public void setMESSAGE_V1(java.lang.String MESSAGE_V1) {
        this.MESSAGE_V1 = MESSAGE_V1;
    }


    /**
     * Gets the MESSAGE_V2 value for this BAPIRET1.
     * 
     * @return MESSAGE_V2
     */
    public java.lang.String getMESSAGE_V2() {
        return MESSAGE_V2;
    }


    /**
     * Sets the MESSAGE_V2 value for this BAPIRET1.
     * 
     * @param MESSAGE_V2
     */
    public void setMESSAGE_V2(java.lang.String MESSAGE_V2) {
        this.MESSAGE_V2 = MESSAGE_V2;
    }


    /**
     * Gets the MESSAGE_V3 value for this BAPIRET1.
     * 
     * @return MESSAGE_V3
     */
    public java.lang.String getMESSAGE_V3() {
        return MESSAGE_V3;
    }


    /**
     * Sets the MESSAGE_V3 value for this BAPIRET1.
     * 
     * @param MESSAGE_V3
     */
    public void setMESSAGE_V3(java.lang.String MESSAGE_V3) {
        this.MESSAGE_V3 = MESSAGE_V3;
    }


    /**
     * Gets the MESSAGE_V4 value for this BAPIRET1.
     * 
     * @return MESSAGE_V4
     */
    public java.lang.String getMESSAGE_V4() {
        return MESSAGE_V4;
    }


    /**
     * Sets the MESSAGE_V4 value for this BAPIRET1.
     * 
     * @param MESSAGE_V4
     */
    public void setMESSAGE_V4(java.lang.String MESSAGE_V4) {
        this.MESSAGE_V4 = MESSAGE_V4;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof BAPIRET1)) return false;
        BAPIRET1 other = (BAPIRET1) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.TYPE==null && other.getTYPE()==null) || 
             (this.TYPE!=null &&
              this.TYPE.equals(other.getTYPE()))) &&
            ((this.ID==null && other.getID()==null) || 
             (this.ID!=null &&
              this.ID.equals(other.getID()))) &&
            ((this.NUMBER==null && other.getNUMBER()==null) || 
             (this.NUMBER!=null &&
              this.NUMBER.equals(other.getNUMBER()))) &&
            ((this.MESSAGE==null && other.getMESSAGE()==null) || 
             (this.MESSAGE!=null &&
              this.MESSAGE.equals(other.getMESSAGE()))) &&
            ((this.LOG_NO==null && other.getLOG_NO()==null) || 
             (this.LOG_NO!=null &&
              this.LOG_NO.equals(other.getLOG_NO()))) &&
            ((this.LOG_MSG_NO==null && other.getLOG_MSG_NO()==null) || 
             (this.LOG_MSG_NO!=null &&
              this.LOG_MSG_NO.equals(other.getLOG_MSG_NO()))) &&
            ((this.MESSAGE_V1==null && other.getMESSAGE_V1()==null) || 
             (this.MESSAGE_V1!=null &&
              this.MESSAGE_V1.equals(other.getMESSAGE_V1()))) &&
            ((this.MESSAGE_V2==null && other.getMESSAGE_V2()==null) || 
             (this.MESSAGE_V2!=null &&
              this.MESSAGE_V2.equals(other.getMESSAGE_V2()))) &&
            ((this.MESSAGE_V3==null && other.getMESSAGE_V3()==null) || 
             (this.MESSAGE_V3!=null &&
              this.MESSAGE_V3.equals(other.getMESSAGE_V3()))) &&
            ((this.MESSAGE_V4==null && other.getMESSAGE_V4()==null) || 
             (this.MESSAGE_V4!=null &&
              this.MESSAGE_V4.equals(other.getMESSAGE_V4())));
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
        if (getTYPE() != null) {
            _hashCode += getTYPE().hashCode();
        }
        if (getID() != null) {
            _hashCode += getID().hashCode();
        }
        if (getNUMBER() != null) {
            _hashCode += getNUMBER().hashCode();
        }
        if (getMESSAGE() != null) {
            _hashCode += getMESSAGE().hashCode();
        }
        if (getLOG_NO() != null) {
            _hashCode += getLOG_NO().hashCode();
        }
        if (getLOG_MSG_NO() != null) {
            _hashCode += getLOG_MSG_NO().hashCode();
        }
        if (getMESSAGE_V1() != null) {
            _hashCode += getMESSAGE_V1().hashCode();
        }
        if (getMESSAGE_V2() != null) {
            _hashCode += getMESSAGE_V2().hashCode();
        }
        if (getMESSAGE_V3() != null) {
            _hashCode += getMESSAGE_V3().hashCode();
        }
        if (getMESSAGE_V4() != null) {
            _hashCode += getMESSAGE_V4().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(BAPIRET1.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:sap-com:document:sap:rfc:functions", "BAPIRET1"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("TYPE");
        elemField.setXmlName(new javax.xml.namespace.QName("", "TYPE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ID");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NUMBER");
        elemField.setXmlName(new javax.xml.namespace.QName("", "NUMBER"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MESSAGE");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MESSAGE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("LOG_NO");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LOG_NO"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("LOG_MSG_NO");
        elemField.setXmlName(new javax.xml.namespace.QName("", "LOG_MSG_NO"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MESSAGE_V1");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MESSAGE_V1"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MESSAGE_V2");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MESSAGE_V2"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MESSAGE_V3");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MESSAGE_V3"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("MESSAGE_V4");
        elemField.setXmlName(new javax.xml.namespace.QName("", "MESSAGE_V4"));
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
