package com.ipssi.gen.utils;
//import com.ipssi.gen.cache.*;
import java.sql.*;
import java.util.*;
import java.io.*;
import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.text.*;
//import oracle.xml.parser.v2.*;
import org.xml.sax.*;
import org.w3c.dom.*;


  class PortVersionHelper { //helps in keeping tack of portfolio Versions
     int maj = 0;
     int min = 0;
     PortVersionHelper (int pMaj, int pMin) {
        maj = pMaj;
        min = pMin;
     }
     public boolean equals(Object rhs) {
        //TODO rhs is of same type
        if (rhs == null)
           return false;
        PortVersionHelper temp = (PortVersionHelper) rhs;
        return (maj == temp.maj) && (min == temp.min);
     }
     public int hashCode() {
        return min < 0 ? min - 1000000 : (maj-50000) * 1000 + min - 50000;
//        return maj*100+min;
     }
  }
