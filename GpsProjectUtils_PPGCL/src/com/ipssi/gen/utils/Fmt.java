package com.ipssi.gen.utils;
import java.sql.*;
import java.util.*;
import java.io.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.text.*;

public class Fmt {
   public static final int CURRENCY_DEFAULT_REPORTING = 0;
   public static final int CURRENCY_DEFAULT_BUDGET = 1;
   public static final int CURRENCY_USER_SPEC = 2;
   
   public static FmtI.Currency getCurrencyFormatter(Connection dbConn, Cache cache, int orgId, boolean doRepNotBudget) throws Exception {
      return getCurrencyFormatter(dbConn, cache, orgId, doRepNotBudget ? CURRENCY_DEFAULT_REPORTING : CURRENCY_DEFAULT_BUDGET, Misc.getUndefInt(), Misc.getUndefInt());
   }
   
   public static FmtI.Currency getCurrencyFormatter(Connection dbConn, Cache cache, int orgId, int currencyCode, int unitCode) throws Exception {
      return getCurrencyFormatter(dbConn, cache, orgId, CURRENCY_USER_SPEC, currencyCode, unitCode);
   }
   
   public static FmtI.Date getDateFormatter(Connection dbConn, Cache cache, int orgId) throws Exception {      
      MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, dbConn);
      Locale locale = helpGetLocale(cache, portInfo);      
      return new FmtI.Date(locale);
   }
   
   public static FmtI.Number getNumberFormatter(Connection dbConn, Cache cache, int orgId, int qtyType) throws Exception {
      return getNumberFormatter(dbConn, cache, orgId, qtyType, Misc.getUndefInt());
   }
   
   public static FmtI.Number getNumberFormatter(Connection dbConn, Cache cache, int orgId, int qtyType, int unitCode) throws Exception  {
      MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, dbConn);
      Locale locale = helpGetLocale(cache, portInfo);      
      MiscInner.UnitInfo unitInfo = cache.getUnitInfo(qtyType, unitCode);
      return new FmtI.Number(locale, unitInfo);
   }
   
   
   /////helper functions
   //// internal implementation
   private static Locale helpGetLocale(Cache cache, MiscInner.PortInfo portInfo) {
      if (portInfo != null) {
         return portInfo.m_locale;
      }
      else {
         return cache.getLocale(0);         
      }
   }
   
   private static FmtI.Currency getCurrencyFormatter(Connection dbConn, Cache cache, int orgId, int currGetterControl, int currencyCode, int unitCode) throws Exception {
      MiscInner.PortInfo portInfo = cache.getPortInfo(orgId, dbConn);
      Locale locale = helpGetLocale(cache, portInfo);
      MiscInner.CurrencyInfo currencyInfo = null;
      MiscInner.UnitInfo unitInfo = null;
      if (portInfo != null && currGetterControl == CURRENCY_DEFAULT_REPORTING) {
          currencyCode = portInfo.m_repCurrency;
          if (Misc.isUndef(unitCode))
             unitCode = portInfo.m_currUnitCode;
      }
      else if (portInfo != null && currGetterControl == CURRENCY_DEFAULT_BUDGET) {
          currencyCode = portInfo.m_budCurrency;
          if (Misc.isUndef(unitCode))
             unitCode = portInfo.m_currUnitCode;
      }
      else if (currGetterControl == CURRENCY_USER_SPEC) {
          if (Misc.isUndef(unitCode) && Misc.isUndef(currencyCode)) {
              currencyInfo = cache.getCurrencyInfo(unitCode);
              if (currencyInfo != null && Misc.isUndef(unitCode))
                  unitCode = currencyInfo.m_unitCode;
          }
      }
      if (Misc.isUndef(currencyCode))
          currencyCode = MiscInner.CurrencyInfo.g_defaultCurrencyCode;
      if (Misc.isUndef(unitCode)) {
          currencyInfo = cache.getCurrencyInfo(unitCode);
          if (currencyInfo != null)
              unitCode = currencyInfo.m_unitCode;
          else
              unitCode = 0;
      }
      unitInfo = cache.getUnitInfo(0, unitCode);
      if (currencyInfo == null)
          currencyInfo = cache.getCurrencyInfo(currencyCode);
         
      
      return new FmtI.Currency(locale, currencyInfo, unitInfo);
   }
   
   
}