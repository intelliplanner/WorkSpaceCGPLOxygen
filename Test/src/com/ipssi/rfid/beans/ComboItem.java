
package com.ipssi.rfid.beans;

public class ComboItem {
int value;
    String label;
  
       public ComboItem(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String Label) {
        this.label = Label;
    }
    
    @Override
    public String toString() {
        return label;
    }
}
