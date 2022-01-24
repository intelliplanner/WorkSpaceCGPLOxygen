
// Copyright (c) 2000 IntelliPlanner Software Systems,  Inc.
package com.ipssi.gen.utils;

import com.ipssi.shift.ShiftBean;
import com.ipssi.shift.ShiftInformation;


public class TimePeriodHelper extends Object {
      static boolean g_doBMSHack = false;
      public java.util.Date m_date = Misc.getCurrentDate(); //the date indicated in the periodstring, if relative to startDate then will have the date/mon of the startDate
      public int m_scope = Misc.SCOPE_ANNUAL;
      public int m_origScope = m_scope;
      public boolean m_doPartial = false; //if the data is for a larger scope and there are some partialities implied then will do so
//      public int m_startIndex;
//      public int m_lastIndex;
//      private int m_supplementalStartIndex;
//      private int m_supplementalLastIndex;
      private boolean m_doingRelative = false;
      public int m_numPortions = 1;
      public int m_numDaysInPeriod = 30;
      public int m_numDaysInPeriodForFteMult = 30; //rajeev 062506
      double m_valPortions[] = null;

      public static int getDurationSmart(long timeId, int scope) { //rajeev 062506

          java.util.Date dateBeg = getDateFor((int)timeId);
          java.util.Date dateEnd = new java.util.Date(dateBeg.getTime());

          if (scope == Misc.SCOPE_ANNUAL) {
             Misc.addMonths(dateEnd, 12);
          }
          else if (scope == Misc.SCOPE_QTR) {
             Misc.addMonths(dateEnd, 3);
          }
          else if (scope == Misc.SCOPE_MONTH) {
             Misc.addMonths(dateEnd, 1);
          }
          else if (scope == Misc.SCOPE_WEEK)
             Misc.addDays(dateEnd, 7);
          else
             Misc.addDays(dateEnd,1);
          return Misc.getDurationExcl(dateEnd, dateBeg);
      }

      public static int getDuration(long timeId, int scope) { //rajeev 060905 .. updated
          if (scope == Misc.SCOPE_ANNUAL || scope == Misc.SCOPE_QTR || scope == Misc.SCOPE_MONTH){
             java.util.Date dateBeg = getDateFor((int)timeId);
             java.util.Date dateEnd = new java.util.Date(dateBeg.getTime());


             if (scope == Misc.SCOPE_ANNUAL) {
                 Misc.addMonths(dateEnd, 12);
             }
             else if (scope == Misc.SCOPE_QTR) {
                 Misc.addMonths(dateEnd, 3);
             }
             if (scope == Misc.SCOPE_MONTH) {
                 Misc.addMonths(dateEnd, 1);
             }
             return Misc.getDaysDiff(dateEnd, dateBeg);
          }
          else if (scope == Misc.SCOPE_WEEK) {
             return 7;
          }
          else
             return 1;

      }
      public static void addScopedDur(java.util.Date stDate, int scope, int numVals) {
          if (scope == Misc.SCOPE_ANNUAL) {
             Misc.addMonths(stDate, 12*numVals);
          }
          else if (scope == Misc.SCOPE_QTR) {
             Misc.addMonths(stDate, 3*numVals);
          }
          else if (scope == Misc.SCOPE_MONTH) {
             Misc.addMonths(stDate, numVals);
          }
          else if (scope == Misc.SCOPE_WEEK) {
             Misc.addDays(stDate, 7*numVals);
          }
          else if (scope == Misc.SCOPE_HOUR || scope == Misc.SCOPE_HOUR_RELATIVE){
        	  stDate.setTime(stDate.getTime()+numVals*60*1000);
          }
          else {
             Misc.addDays(stDate, numVals);
          }
      }
      public static void addScopedDur(java.util.Date stDate, int scope, double numVals) {
          if (scope == Misc.SCOPE_ANNUAL) {
             Misc.addMonths(stDate, 12*numVals);
          }
          else if (scope == Misc.SCOPE_QTR) {
             Misc.addMonths(stDate, 3*numVals);
          }
          else if (scope == Misc.SCOPE_MONTH) {
             Misc.addMonths(stDate, numVals);
          }
          else if (scope == Misc.SCOPE_WEEK) {
             Misc.addDays(stDate, 7*numVals);
          }
          else if (scope == Misc.SCOPE_HOUR || scope == Misc.SCOPE_HOUR_RELATIVE){
        	  stDate.setTime(stDate.getTime()+(((int)(numVals*60*60))*1000));
          }
          else if (scope == Misc.SCOPE_MTD) {
              Misc.addMonths(stDate, numVals);
           }
          else {
             Misc.addDays(stDate, numVals);
          }
      }
      public static int getTimeIdIncr(long timeId, int scope, int numVals) {
          if (scope == Misc.SCOPE_ANNUAL)
             return 12*35*numVals;
          else if (scope == Misc.SCOPE_QTR)
             return 3*35*numVals;
          else if (scope == Misc.SCOPE_MONTH)
             return 35*numVals;
          else if (scope == Misc.SCOPE_WEEK) {
             java.util.Date dt = TimePeriodHelper.getDateFor((int)timeId);
             Misc.addDays(dt, 7*numVals);
             return (int)(TimePeriodHelper.getTimeId(dt)-timeId);
          }
          else
             return 1;
      }

      public static String getAppropriateDate(int scope, int timeId) {
          if (scope == Misc.SCOPE_ANNUAL) {
              int val = timeId/(12*35);
              return Integer.toString(val+1900);
          }
          else if (scope == Misc.SCOPE_QTR) {
             int numQtr = timeId/(3*35);
             int year = numQtr/4;
             int qtr = (numQtr%4)+1;
             return Integer.toString(qtr)+"Q'"+Integer.toString(year+1900);
          }
          else if (scope == Misc.SCOPE_MONTH) {
             int numMon = timeId/35;
             int year = numMon/12;
             int mon = (numMon%12) + 1;
             return Integer.toString(mon)+"/"+Integer.toString(year+1900);
          }
          else {
             return "";
          }
      }

      public static int getNextLowerLevel(int scope) {
          if (scope == Misc.SCOPE_ANNUAL)
             if (g_doBMSHack)
                return Misc.SCOPE_QTR; //will go down to month //CHANGED SCHERING
             else
                return Misc.SCOPE_MONTH;
          else if (scope == Misc.SCOPE_QTR)
             return Misc.SCOPE_MONTH;
          else if (scope == Misc.SCOPE_MONTH)
             return Misc.SCOPE_CUSTOM;
          else //if (scope == Misc.SCOPE_WEEK)
             return Misc.SCOPE_DAY;
      }


      public static int getTimeVal(int scope, int timeId) {
          if (scope == Misc.SCOPE_ANNUAL) {
              return timeId/(12*35);
          }
          else if (scope == Misc.SCOPE_QTR) {
              return timeId/(3*35);
          }
          else if (scope == Misc.SCOPE_MONTH) {
              return timeId/35;
          }
          else if (scope == Misc.SCOPE_WEEK) {
              java.util.Date dt = TimePeriodHelper.getDateFor(timeId);
              int diff = Misc.getDaysDiff(dt, Misc.G_REF_DATE_FOR_WEEK);
              return diff/7;
          }
          else {
              return timeId;
          }
      }

      public static int getTimeId(int scope, int timeVal) {
         if (scope == Misc.SCOPE_ANNUAL)
            return timeVal*12*35;
         else if (scope == Misc.SCOPE_QTR)
            return timeVal*3*35;
         else if (scope == Misc.SCOPE_MONTH)
            return timeVal*35;
         else if (scope == Misc.SCOPE_WEEK) {
            java.util.Date dt = new java.util.Date(Misc.G_REF_DATE_FOR_WEEK.getTime());
            Misc.addDays(dt, 7*timeVal);
            return getTimeId(dt);
         }
         else
            return timeVal;
      }

      public static int getTimeId(java.util.Date dt) {
         return dt.getYear()*12*35 + dt.getMonth()*35+dt.getDate()-1;
      }

      public static java.util.Date getDateFor(int timeId) {
         int year = timeId/(12*35);
         int mon  = timeId/35 - year*12;
         int day  = timeId%35;
         return new java.util.Date(year, mon, day+1);
      }




      public TimePeriodHelper() {//CHANGES THE START/END DATE PASSED
         m_date           = new java.util.Date();
         m_scope          = 0;
         m_doPartial      = false;
//         m_startIndex     = 0;
//         m_lastIndex      = 1;
//         m_supplementalStartIndex = 0;
//         m_supplementalLastIndex = 1;
         m_doingRelative = false;
      }

      public static int getFullYearFrom2Digit(int year) {
           if (year < 100) { // is two year digit
                 java.util.Date currDate = new java.util.Date();
                 int currYear = currDate.getYear()+1900;
                 int currCentury = (currYear/100)*100;
                 int tempYear = currCentury+year;
                 if (tempYear >= currYear)
                     if (tempYear < currYear + 60)
                        year = tempYear;
                    else
                        year = tempYear - 100;
                 else if (tempYear < currYear)
                    if  (tempYear < currYear - 40)
                        year = tempYear+100;
                    else
                        year = tempYear;
          }
          return year;
      }

      public int getNumDaysInPeriod() {
          return m_numDaysInPeriod;
      }

      public int getNumDaysInPeriodForFteMult() { //rajeev 062506 .. new func
         return m_numDaysInPeriodForFteMult;
      }

      public int getNumPortions() {
          return m_numPortions;
      }
      public java.util.Date getEndDateFromScopeBeg() {
         return getEndDateFromScopeBeg(false, false);
      }
      public static double getPropIncluded(java.util.Date rangeBeg,java.util.Date rangeEnd, java.util.Date dataBeg, java.util.Date dataEnd) {
         return getPropIncluded(rangeBeg, rangeEnd, dataBeg, dataEnd, true, false);
      }
      public static double getPropIncluded(java.util.Date rangeBeg,java.util.Date rangeEnd, java.util.Date dataBeg, java.util.Date dataEnd, boolean rangeEndIsIncl, boolean dataEndIsIncl) {
          int gapRangeEndDataBeg =rangeEndIsIncl ? Misc.getDaysDiffIncl(rangeEnd, dataBeg) : Misc.getDaysDiff(rangeEnd, dataBeg);
          int gapRangeBegDataEnd =dataEndIsIncl ? Misc.getDaysDiffIncl(rangeBeg, dataEnd) : Misc.getDaysDiff(rangeBeg, dataEnd);
          int gapRangeBegDataBeg = Misc.getDaysDiff(rangeBeg, dataBeg);
          int gapRangeEndDataEnd = (rangeEndIsIncl && !dataEndIsIncl) ? Misc.getDaysDiffIncl(rangeEnd,dataEnd)
                                  :(!rangeEndIsIncl && dataEndIsIncl) ? -1*Misc.getDaysDiffIncl(dataEnd, rangeEnd)
                                  :Misc.getDaysDiff(rangeEnd,dataEnd);
          double gapRangeEndRangeBeg = rangeEndIsIncl ? Misc.getDaysDiffIncl(rangeEnd, rangeBeg) : Misc.getDaysDiff(rangeEnd, rangeBeg);
          double gapDataEndDataBeg = dataEndIsIncl ? Misc.getDaysDiffIncl(dataEnd, dataBeg) : Misc.getDaysDiff(dataEnd, dataBeg);

          if (gapRangeEndDataBeg <= 0 /*range to left of data */|| gapRangeBegDataEnd >= 0 /* range to right of data*/)
             return 0;
          if (gapRangeBegDataBeg <= 0 && gapRangeEndDataEnd >= 0 /*data in range */)
             return 1;
          if (gapRangeBegDataBeg >= 0 && gapRangeEndDataEnd <= 0 /* range in data*/)
             return gapRangeEndRangeBeg/gapDataEndDataBeg;
          if (gapRangeBegDataBeg <= 0 /* range to left of data because of 2nd cond range end before data end*/)
             return (double)gapRangeEndDataBeg/gapDataEndDataBeg;
          // range to right of data but data end comes sooner
          return (double)gapRangeBegDataEnd/gapDataEndDataBeg * (-1.0F);

//         	if (rangeEnd.before(dataBeg) || Misc.getDaysDiff(rangeEnd,dataBeg) == 0 || rangeBeg.after(dataEnd) || Misc.getDaysDiff(rangeBeg, dataEnd) == 0)
//             return 0;
//          if ((rangeBeg.before(dataBeg) || Misc.getDaysDiff(rangeBeg, dataBeg) == 0) && (rangeEnd.after(dataEnd) || Misc.getDaysDiff(rangeEnd, dataEnd) == 0))
//   		       return 1;
//   	      double dataRangeDur = Misc.getDaysDiff(dataEnd, dataBeg);
//	        if ((dataBeg.before(rangeBeg) || Misc.getDaysDiff(dataBeg, rangeBeg) == 0) && (dataEnd.after(rangeEnd) || Misc.getDaysDiff(dataEnd, rangeEnd) == 0))
//		         return (double)(Misc.getDaysDiff(rangeEnd, rangeBeg))/dataRangeDur;
//          if ((rangeBeg.before(dataBeg) || Misc.getDaysDiff(dataBeg, rangeBeg) == 0))
//		         return (double)(Misc.getDaysDiff(rangeEnd, dataBeg))/dataRangeDur;
//          return (double) (Misc.getDaysDiff(dataEnd, rangeBeg))/dataRangeDur;
      }
      public java.util.Date getEndDateFromScopeBeg(boolean useOrigScope, boolean numPortionAdjusted) {

          java.util.Date dateEnd = new java.util.Date(m_date.getTime());
          int numPortion = numPortionAdjusted ? this.m_numPortions:1;
          int scope = useOrigScope ? m_origScope : m_scope;
          if (scope == Misc.SCOPE_QTR) { //check if start's quarter lies in the quarter for timePeriod
              dateEnd.setDate(1);
              dateEnd.setMonth(dateEnd.getMonth()/3 * 3);
              Misc.addMonths(dateEnd,3*numPortion);
//              dateEnd.setMonth(dateEnd.getMonth()+3);
          }
          else if (scope == Misc.SCOPE_ANNUAL) { //yearly
              dateEnd.setDate(1);
              dateEnd.setMonth(0);
              Misc.addMonths(dateEnd, 12*numPortion);
          }
          else if (scope == Misc.SCOPE_MONTH) { //monthly
              dateEnd.setDate(1);
              Misc.addMonths(dateEnd, 1*numPortion);
//              dateEnd.setMonth(dateEnd.getMonth()+1);
          }
          else if (scope == Misc.SCOPE_WEEK) {
              Misc.addDays(dateEnd, 7*numPortion);
          }
          else if (scope == Misc.SCOPE_DAY) { //monthly
//              Misc.addDays(dateEnd, 1);
              Misc.addDays(dateEnd, 1*numPortion);
          }
          return dateEnd;
      }

      public double getValPortion(int addLowerLevelIndex) {
         if (!this.m_doPartial || m_valPortions == null || addLowerLevelIndex < 0 || addLowerLevelIndex >= m_valPortions.length)
            return 1;
         else
            return m_valPortions[addLowerLevelIndex];
      }

      public long getTimeId(int addLowerLevelIndex) { //TODO dayAdd to implement
         if (addLowerLevelIndex == 0)
            return getTimeId(m_date);
         java.util.Date dt = new java.util.Date(m_date.getTime());
         int lower_level_scope = TimePeriodHelper.getNextLowerLevel(m_scope);
         if (lower_level_scope == Misc.SCOPE_QTR) {
             Misc.addMonths(dt, 3*addLowerLevelIndex);
         }
         else if (lower_level_scope == Misc.SCOPE_MONTH) {
             Misc.addMonths(dt, addLowerLevelIndex);
         }
         else if (lower_level_scope == Misc.SCOPE_WEEK) {
             Misc.addDays(dt, 7*addLowerLevelIndex);
         }
         return getTimeId(dt);
      }
     //CHANGES THE START/END DATE PASSED .. end date is inclusive
      public TimePeriodHelper(String str, java.util.Date startDate, java.util.Date pEndDateIncl) throws Exception {//CHANGES THE START/END DATE PASSED
     //CHANGES THE START/END DATE PASSED
         m_date           = new java.util.Date();
         m_scope          = 0;
         m_doPartial      = false;
//         m_startIndex     = 0;
//         m_lastIndex      = 1;
         m_doingRelative = false;


         int year    = Misc.getUndefInt();  //generally absolute unless was partial specified -
         int month   = Misc.getUndefInt(); //generally absolute and relative to 1
         int day     = Misc.getUndefInt();   //generally absolute and relative to 1
         int quarter = Misc.getUndefInt();   //absolute and relative to 1


         if (str == null || str.length() == 0)
             return;
//092106         if (startDate == null)
//092106             startDate = new java.util.Date(); //initialize to current time if not provided


         java.util.Date endDate = null;
         if (pEndDateIncl != null) {
            endDate = new java.util.Date(pEndDateIncl.getTime());
            Misc.addDays(endDate,1);
         }
         boolean isZeroDay = startDate == null || (startDate != null && endDate != null && Misc.getDaysDiff(endDate, startDate) <= 1);
         try {
             char strch[] = str.toCharArray();

             //check if the format is mm/dd/yyyy or mm/yyyy or isAllDigit or ..
             boolean ofMMDDformat = true;
             int slashPosn[] = {-1, -1};
             boolean isAllDigit = true;
             int size = strch.length;
             int slashCount = 0;
             for (int i=0;i<size;i++) {
                char ch = strch[i];
                if (!Character.isDigit(ch)) {
                   if (ch == '/' || ch == '.' || ch == '-') {
                      if (slashCount >= 2) {
                         ofMMDDformat = false;
                         isAllDigit = false;
                      }
                      slashPosn[slashCount++] = i;
                   }
                   else {
                      ofMMDDformat = false;
                      isAllDigit = false;
                   }
                }
                if (!ofMMDDformat)
                   break;
             }
             if (slashCount == 0) {
                ofMMDDformat = false;
             }
             if (ofMMDDformat) {
                String tempStr = new String(strch, 0, slashPosn[0]);
                month = Misc.getParamAsInt(tempStr);
                tempStr = new String(strch, slashPosn[slashCount-1]+1, strch.length-slashPosn[slashCount-1]-1);
                year = Misc.getParamAsInt(tempStr);
                year = getFullYearFrom2Digit(year);
                m_scope = Misc.SCOPE_MONTH;
            }
            else if (isAllDigit) {
                String tempStr = new String(strch);
                year = Integer.parseInt(tempStr);
                m_scope = Misc.SCOPE_ANNUAL;
            }
            else {
                //the format could by (f)Y(ear)nnnnQ(tr|uarter)
                //or it could be Q()nnn(f)Y()nnn
                //or it could be nnnnQnumber
                //or it could be Q1 '04
                //or it could be w23, m43, q12
                //Same also for month
                int startPosn[] = {-1,-1,-1,-1}; //0 is year, 1 is qtr, 2 is mon
                int endPosn[] = {-1,-1,-1,-1};

                //first get the general idea of what is there in the string
                for (int i=0;i<size;i++) {
                   char ch = strch[i];
                   if (Character.isDigit(ch) && startPosn[0] == -1 && startPosn[1] == -1 && startPosn[2] == -1)
                      startPosn[0] = i;
                   else if (ch == 'y' || ch == 'Y' || ch =='\'')
                      startPosn[0] = i;
                   else if (ch == 'Q' || ch == 'q')
                      startPosn[1] = i;
                   else if (ch == 'm' || ch == 'M')
                      startPosn[2] = i;
                   else if (ch == 'w' || ch == 'W')
                      startPosn[3] = i;
                }
                if (startPosn[2] >= 0 && startPosn[1] >= 0)
                    startPosn[2] = -1;

                //now get the posn where the digits start and digits end
                boolean foundData[] = {false, false, false,false};
                int numFoundData = 0;
                int lastVal = 0;
                for (int art=0;art < 4; art++) {
                   if (startPosn[art] != -1) {
                      int temp;
                      for (temp=startPosn[art];temp<size;temp++)
                         if (Character.isDigit(strch[temp])) {
                            break;
                         }
                      startPosn[art] = temp;
                      for (temp=startPosn[art];temp<size;temp++)
                         if (!Character.isDigit(strch[temp])) {
                            break;
                         }
                      if (temp-startPosn[art] > 0) {
                         String tempStr = new String(strch, startPosn[art], temp-startPosn[art]);
                         int val = Integer.parseInt(tempStr);
                         foundData[art] = true;
                         numFoundData++;
                         if (art == 0) {
                            year = val;
                         }
                         else if (art == 1) {
                            quarter = val;
                         }
                         else if (art == 2) {
                            month = val;
                         }
                         lastVal = val;
                      }
                  } //if there was a valid start
               }//looped through all artificial loops
               if (numFoundData == 1 && !foundData[0] && lastVal > 280) { //know for sure that this is timeVal 70 qtr past 1900
                  if (foundData[1]) {
                      quarter = lastVal%4 +1;
                      year = lastVal/4+1900;
                  }
                  else if (foundData[2]) {
                      month = lastVal%12 +1;
                      year = lastVal/12+1900;
                  }
                  else if (foundData[3]) {
                      java.util.Date dt = getDateFor((int)getTimeId(Misc.SCOPE_WEEK, lastVal));
                      year = dt.getYear()+1900;
                      month = dt.getMonth()+1;
                      day = dt.getDate();
                  }
               }
               if (!Misc.isUndef(year) && (!Misc.isUndef(quarter) || !Misc.isUndef(month)))
                  year = getFullYearFrom2Digit(year);
               if (Misc.G_BD_DOING && !Misc.isUndef(year))
                  year = getFullYearFrom2Digit(year);

               if (foundData[2]) {
                   m_scope = Misc.SCOPE_MONTH;
               }
               else if (foundData[1]) {
                   m_scope = Misc.SCOPE_QTR;
               }
               else if (foundData[3]){
                   m_scope = Misc.SCOPE_WEEK;
               }
               else {
                   m_scope = Misc.SCOPE_ANNUAL;
               }
            }//is not of 02/04 OR 2002 format
            //DO OTHER processing
            m_origScope = m_scope;
            if (!Misc.isUndef(year) && year < 1900) { //the years are relatively numbered
//              year = startDate.getYear()+year+1900;
                java.util.Date tempDate = startDate == null ? Misc.getCurrentDate() : new java.util.Date(startDate.getTime());
                tempDate.setYear(tempDate.getYear()+year-1);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                day = tempDate.getDate();
                quarter = (month-1)/3+1;
                m_doingRelative = true;
            }
            if (!Misc.isUndef(quarter) && Misc.isUndef(year) && Misc.isUndef(day) && Misc.isUndef(month)) {
//              relatively numbered quarter
//              int numYear = quarter/4;
//              year = startDate.getYear()+1900+numYear;
//              quarter = quarter % 4+1; //the quarters are numbered relative to 1 in TimePeriodHelper

                java.util.Date tempDate = startDate == null ? Misc.getCurrentDate() : new java.util.Date(startDate.getTime());
                Misc.addMonths(tempDate, (quarter-1)*3);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                quarter = (month-1)/3+1;
                day = tempDate.getDate();
                m_doingRelative = true;
            }
            else if (Misc.isUndef(quarter) && Misc.isUndef(year) && Misc.isUndef(day) && !Misc.isUndef(month)) {
//              relatively numbered months
//              int numYear = month/12;
//              year = startDate.getYear()+1900+numYear;
//              month = month % 12+startDate.getMonth(); //temporaruly switched to 0 based ..
//              if (month > 11) {
//                 year++;
//                 month -= 12;
//              }
//              month++;

                java.util.Date tempDate = startDate == null ? Misc.getCurrentDate() : new java.util.Date(startDate.getTime());
                Misc.addMonths(tempDate, month-1);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                quarter = (month-1)/3+1;
                day = tempDate.getDate();
                m_doingRelative = true;
            }
            else if (Misc.isUndef(quarter) && Misc.isUndef(year) && !Misc.isUndef(day) && Misc.isUndef(month)) {
                //relatively numbered days
                java.util.Date tempDate = startDate == null ? Misc.getCurrentDate() : new java.util.Date(startDate.getTime());
                Misc.addDays(tempDate, day-1);
                tempDate.setDate(tempDate.getDate()+day-1);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                quarter = (month-1)/3+1;
                day = tempDate.getDate();
                m_doingRelative = true;
            }

            //fill in missing values
            if (!Misc.isUndef(year)) {
                if (!Misc.isUndef(month) && Misc.isUndef(quarter))
                    quarter = ((month-1)/3) + 1;
                else if (Misc.isUndef(month) && !Misc.isUndef(quarter))
                    month = (quarter-1)*3+1;
                else if (Misc.isUndef(month) && Misc.isUndef(quarter)) {
                    month = 1;
                    quarter = 1;
                }
            }
            if (Misc.isUndef(day)) {
                day = 1;
            }
            if (m_doingRelative) {
                m_date = new java.util.Date(year-1900, month-1, day);
                java.util.Date tempDate = new java.util.Date(m_date.getTime());
                if (m_scope == Misc.SCOPE_ANNUAL)
                   Misc.addMonths(tempDate, 12);
                else if (m_scope == Misc.SCOPE_QTR)
                   Misc.addMonths(tempDate, 3);
                else if (m_scope == Misc.SCOPE_MONTH)
                   Misc.addMonths(tempDate, 1);
                else if (m_scope == Misc.SCOPE_WEEK)
                   Misc.addDays(tempDate, 7);
                else
                   Misc.addDays(tempDate,1);

                this.m_numDaysInPeriod = Misc.getDaysDiff(tempDate, m_date);
                if (m_numDaysInPeriod == 0)
                   m_numDaysInPeriod = 1;
                this.m_numDaysInPeriodForFteMult = Misc.getDurationExcl(tempDate, m_date);//rajeev 062506 .. 3 lines
                if (m_numDaysInPeriodForFteMult == 0)
                   m_numDaysInPeriodForFteMult = 1;

            }
            else if (!m_doingRelative) {

              if (isZeroDay) {
                 if (Misc.isUndef(month))
                    month = 1;
                 if (Misc.isUndef(day))
                    day = 1;
                 m_date = new java.util.Date(year-1900, month-1, day);
              }
              else { //only in this case do we have to worry about partials
                if (m_scope == Misc.SCOPE_ANNUAL) { //we know the year
                    java.util.Date tempDate = new java.util.Date(year-1900,0,1);
                    if (g_doBMSHack)
                       m_doPartial = true;
                    if (startDate != null && startDate.after(tempDate)) {

                        if (g_doBMSHack) {
                           month = startDate.getMonth()/3*3+1;
                           m_doPartial = true; //will go qtr level only
                        }
                        else {
//                         if (startDate.getYear()+1900 <= year) //condition not needed ... already true
                          { //do partial only in this case
                             m_doPartial = true; //will go qtr level only
                             month = startDate.getMonth() + 1;
                             if (startDate.getDate() >=15)
                                month++;
                          }
                        }
                    }
                }
                else if (m_scope == Misc.SCOPE_QTR) { //don't do partially for quarterly data in case of BMS
                   if (!g_doBMSHack) { //CHECK HERE
//                   if (!true) {

                      java.util.Date tempDate = new java.util.Date(year-1900, month-1,1);
                      if (startDate != null && startDate.after(tempDate)) {
                         //if (startDate.getYear()+1900 <= year) //not needed
                         { //TO CHECK borrowed for SCOPE_ANNUAL where it is correct
                             m_doPartial = true;
                             month = startDate.getMonth() + 1;
                             if (startDate.getDate() >=  15)
                                month++;
                             day = 1;
                          }
                      }
                   }
                }
                else if (m_scope == Misc.SCOPE_MONTH) {
                    java.util.Date tempDate = new java.util.Date(year-1900, month-1,1);
                    if (startDate != null && startDate.after(tempDate)) {
//                      if (startDate.getYear()+1900 <= year)  //not needed
                      {
                         m_doPartial = true;
                         day = startDate.getDate();
                      }
                    }
                }
//END BMS HACK
                m_date = new java.util.Date(year-1900, month-1, day);
//@@@@
                if (endDate != null && endDate.before(m_date)) {
                    endDate.setTime(m_date.getTime());
                    Misc.addMonths(endDate, 12);
                }
                if (startDate != null && startDate.after(m_date)) {
                    startDate = new java.util.Date(m_date.getTime());
//                    startDate.setTime(m_date.getTime());
                }
               //check if finish for the scope would make it go beyond
               java.util.Date scopeEndDate = getEndDateFromScopeBeg();
               java.util.Date useDateForCheckingNumDays = scopeEndDate;
               if (endDate != null && endDate.before(scopeEndDate)) {
//                  if (m_scope == Misc.SCOPE_ANNUAL) //BMS HACK
                    m_doPartial = true;
                    useDateForCheckingNumDays = endDate;

               }
               //BMS HACK
               //this.m_numDaysInPeriod = Misc.getDaysDiff(endDate != null && endDate.before(scopeEndDate) ? endDate : scopeEndDate, m_date);
               this.m_numDaysInPeriod = Misc.getDaysDiff(useDateForCheckingNumDays, m_date);
               if (m_numDaysInPeriod == 0)
                  m_numDaysInPeriod = 1;
               this.m_numDaysInPeriodForFteMult = Misc.getDurationExcl(useDateForCheckingNumDays, m_date);//rajeev 062506
                if (m_numDaysInPeriodForFteMult == 0)
                   m_numDaysInPeriodForFteMult = 1;

               //some other potential adjustment for start and how we want to calc week
               //basically if the number of days in the month > 28 then make life simpler
               //go for full month scope
               //If end date is beyond scopeEnd then go for reverse counting
               //else check if making the weeks whole will cause things go in reverse

               if (m_doPartial) {
                 if (m_scope == Misc.SCOPE_MONTH) {
                     m_scope = Misc.SCOPE_CUSTOM;
                     m_doPartial = false;
                 }
                 else if (m_scope == Misc.SCOPE_QTR) {
                    int dur = Misc.getMonthsDiff(endDate != null && endDate.before(scopeEndDate) ? endDate : scopeEndDate, m_date);

                       if (dur <= 0)
                         dur = 1;
                       this.m_numPortions = dur;
                       m_valPortions = new double[dur];
                       for (int t=0;t<dur;t++)
                          m_valPortions[t] = (double)1.0/(double)dur;
                 }//end of scope_qtr
                 else if (m_scope == Misc.SCOPE_ANNUAL) {
                    //BMS HACK ..
                    //m_date.start is beginning of qtr appropriately ..
                    int dur = Misc.getMonthsDiff(endDate != null && endDate.before(scopeEndDate) ? endDate : scopeEndDate, m_date);

                    if (g_doBMSHack) {
                       int numQtrs = (int) Math.round((double)dur/(double)3.0);
                       if (numQtrs <= 0)
                       {
                         numQtrs = 1;
                       }
                       this.m_numPortions = numQtrs;
                       m_valPortions = new double[numQtrs];
                       for (int t=0;t<numQtrs;t++) {
                          m_valPortions[t] = (double)1.0/(double)numQtrs;
                       }
                    }                    
                    else {
                       if (dur <= 0)
                         dur = 1;
                       this.m_numPortions = dur;
                       m_valPortions = new double[dur];
                       for (int t=0;t<dur;t++)
                          m_valPortions[t] = (double)1.0/(double)dur;
                    }

                 } //end of annual
                }
              } //end of partial
           } //end of relative
        }//end of try block of catch ...
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
      }

      public static MiscInner.Pair getLowerLevelTimeValBound(int upperScope, int upperTimeVal, int lowerScope, int numItems) { //doesn't handle weeks
         int lo = 0;
         int hi = 0;
         if (upperScope == Misc.SCOPE_ANNUAL) {
            if (lowerScope == Misc.SCOPE_QTR) {
              lo = upperTimeVal * 4;
              hi = (upperTimeVal+numItems)*4;
            }
            else if (lowerScope == Misc.SCOPE_MONTH) {
              lo = upperTimeVal * 12;
              hi = (upperTimeVal+numItems)*12;
            }
            else if (upperScope == Misc.SCOPE_ANNUAL) {
               lo = upperTimeVal;
               hi = upperTimeVal+numItems;
            }

         }
         else if (upperScope == Misc.SCOPE_QTR) {
            if (lowerScope == Misc.SCOPE_MONTH) {
              lo = upperTimeVal * 3;
              hi = (upperTimeVal+numItems)*3;
            }
            else if (upperScope == Misc.SCOPE_QTR) {
               lo = upperTimeVal;
               hi = upperTimeVal+numItems;
            }

         }
         else if (upperScope == Misc.SCOPE_MONTH) {
            if (upperScope == Misc.SCOPE_MONTH) {
               lo = upperTimeVal;
               hi = upperTimeVal+numItems;
            }

         }
         return new MiscInner.Pair(lo, hi);
      }
      
      public static void setTimeBegOfDate(java.util.Date dt) {
          dt.setHours(0);
          dt.setMinutes(0);
          dt.setSeconds(0);
       }
      
      public static void addSeconds(java.util.Date dt, int seconds) {
          dt = new java.util.Date(dt.getTime() + seconds*1000L);
       }
      public static java.util.Date getBegOfDate(java.util.Date dt, int scope, ShiftBean shiftBean) {
    	  if (dt == null)
    		  return null;
    	  java.util.Date retval = new java.util.Date(dt.getTime()/1000*1000); //to get rid of partial millis
    	  setBegOfDate(retval, scope, shiftBean);
    	  return retval;
      }
      public static java.util.Date getBegOfDate(java.util.Date dt, int scope) {
    	  if (dt == null)
    		  return null;
    	  java.util.Date retval = new java.util.Date(dt.getTime()/1000*1000); //to get rid of partial millis
    	  setBegOfDate(retval, scope);
    	  return retval;
      }
      public static long getBegOfDate(long dt, int scope) {
    	  if (Misc.isUndef(dt))
    		  return Misc.getUndefInt();
    	  java.util.Date retval = new java.util.Date(dt/1000*1000); //to get rid of partial millis
    	  setBegOfDate(retval, scope);
    	  return retval.getTime();
      }
      public static void setBegOfDate(java.util.Date dt, int scope, ShiftBean shiftBean ) {
    	  
    	  if (scope != Misc.SCOPE_HOUR && scope != Misc.SCOPE_HOUR_RELATIVE && scope != Misc.SCOPE_USER_PERIOD) {
    		  java.util.Date tempDate = new java.util.Date(dt.getTime());
	    	  tempDate.setHours(shiftBean.getStartHour());
	    	  tempDate.setMinutes(shiftBean.getStartMin());
	    	  if(tempDate.after(dt))
	    		  Misc.addDays(dt, -1);
    	  }
    	  
    	  if (scope != Misc.SCOPE_HOUR && scope != Misc.SCOPE_HOUR_RELATIVE) {
	         dt.setHours(shiftBean.getStartHour());
	         dt.setMinutes(shiftBean.getStartMin());
    	  }
    	  dt.setSeconds(0);
         if (scope == Misc.SCOPE_ANNUAL) {
            dt.setMonth(0);
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_QTR) {
            dt.setMonth((dt.getMonth()/3) * 3);
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_MONTH) {
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_WEEK) {
        	 int day = dt.getDay();
        	 if (day == 0)
        		 Misc.addDays(dt, -6);
        	 else
        		 Misc.addDays(dt, 1-day);
         }
         else if ( scope == Misc.SCOPE_DAY){
           //do nothing .. it is done at beg itself
         }
         else if (scope == Misc.SCOPE_HOUR) {
        	 dt.setMinutes(0);
         }
         else if (scope == Misc.SCOPE_MTD){
        	 Misc.addDays(dt, -1.0);
        	 dt.setDate(1);
         }
      }
      
      public static void setBegOfDate(java.util.Date dt, int scope) {
    	 
    	 if (scope != Misc.SCOPE_HOUR && scope != Misc.SCOPE_HOUR_RELATIVE) {
	         dt.setHours(0);
	         dt.setMinutes(0);
	         
    	 }
    	 dt.setSeconds(0);
         if (scope == Misc.SCOPE_ANNUAL) {
            dt.setMonth(0);
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_QTR) {
            dt.setMonth((dt.getMonth()/3) * 3);
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_MONTH) {
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_WEEK) {
        	 int day = dt.getDay();
        	 if (day == 0)
        		 Misc.addDays(dt, -6);
        	 else
        		 Misc.addDays(dt, 1-day);
         }
         else if ( scope == Misc.SCOPE_DAY){
           //do nothing .. it is done at beg itself
         }
         else if (scope == Misc.SCOPE_HOUR) {
        	 dt.setMinutes(0);
        	 dt.setSeconds(0);
         }
         else if (scope == Misc.SCOPE_MTD) {
        	 Misc.addDays(dt, -1.0);
             dt.setDate(1);
          }
      }
      
      public static void setBegOfDate(java.util.Date dt,int portNodeId, int scope, int shiftIdAsked ,SessionManager session) throws Exception {
    	 ShiftBean shiftBean = ShiftInformation.getShiftById(portNodeId, shiftIdAsked, session.getConnection());
    	 int minStartHour = shiftBean.getStartHour();
    	 int minStartMin = shiftBean.getStartMin();
    	 java.util.Date tempDate = new java.util.Date();
    	 tempDate.setHours(minStartHour);
    	 tempDate.setMinutes(minStartMin);
    	 if(tempDate.after(dt))
    	 {
    		 Misc.addDays(dt, -1);
    	 }
    	  if (scope != Misc.SCOPE_HOUR)
    		  dt.setHours(minStartHour);
              dt.setMinutes(minStartMin);
              dt.setSeconds(0);
         if (scope == Misc.SCOPE_ANNUAL) {
            dt.setMonth(0);
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_QTR) {
            dt.setMonth((dt.getMonth()/3) * 3);
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_MONTH) {
            dt.setDate(1);
         }
         else if (scope == Misc.SCOPE_WEEK) {
        	 int day = dt.getDay();
        	 if (day == 0)
        		 Misc.addDays(dt, -6);
        	 else
        		 Misc.addDays(dt, 1-day);
         }
         else if ( scope == Misc.SCOPE_DAY){
           //do nothing .. it is done at beg itself
         }
      }
      
/*
      public TimePeriodHelper(String str, java.util.Date startDate, java.util.Date finishDate) {
         m_date           = new java.util.Date();
         m_scope          = 0;
         m_doPartial      = false;
         m_startIndex     = 0;
         m_lastIndex      = 1;
         m_doingRelative = false;


         int year    = Misc.getUndefInt();  //generally absolute unless was partial specified -
         int month   = Misc.getUndefInt(); //generally absolute and relative to 1
         int day     = Misc.getUndefInt();   //generally absolute and relative to 1
         int quarter = Misc.getUndefInt();   //absolute and relative to 1


         if (str == null || str.length() == 0)
             return;
         if (startDate == null)
             startDate = new java.util.Date(); //initialize to current time if not provided

         try {
             char strch[] = str.toCharArray();

             //check if the format is mm/dd/yyyy or mm/yyyy or isAllDigit or ..
             boolean ofMMDDformat = true;
             int slashPosn[] = {-1, -1};
             boolean isAllDigit = true;
             int size = strch.length;
             int slashCount = 0;
             for (int i=0;i<size;i++) {
                char ch = strch[i];
                if (!Character.isDigit(ch)) {
                   if (ch == '/' || ch == '.' || ch == '-') {
                      if (slashCount >= 2) {
                         ofMMDDformat = false;
                         isAllDigit = false;
                      }
                      slashPosn[slashCount++] = i;
                   }
                   else {
                      ofMMDDformat = false;
                      isAllDigit = false;
                   }
                }
                if (!ofMMDDformat)
                   break;
             }
             if (slashCount == 0) {
                ofMMDDformat = false;
             }
             if (ofMMDDformat) {
                String tempStr = new String(strch, 0, slashPosn[slashCount == 1 || MiscInner.PortInfo.G_DEFAULT_LOCALEID == 0 ? 0 : 1]);
                month = Misc.getParamAsInt(tempStr);
                tempStr = new String(strch, slashPosn[slashCount-1]+1, strch.length-slashPosn[slashCount-1]-1);
                year = Misc.getParamAsInt(tempStr);
                year = getFullYearFrom2Digit(year);
                m_scope = Misc.SCOPE_MONTH;
                if (startDate.getYear() >= (year-1900) && startDate.getMonth() >= (month-1)) {
                   day = startDate.getDate();
                }
                else
                   day = 1;
             //   if (slashCount == 2) {
             //       tempStr = new String(strch, slashPosn[0]+1, slashPosn[1]-slashPosn[0]-1);
             //       day = 1;
             //       m_scope = Misc.SCOPE_MONTH;
             //   }
            }
            else if (isAllDigit) {
                String tempStr = new String(strch);
                year = Integer.parseInt(tempStr);
                m_scope = Misc.SCOPE_ANNUAL;
                if ((year-1900) <= startDate.getYear()) {
                    day = startDate.getDate();
                    month = startDate.getMonth()+1;
                }
                else {
                    day = 1;
                    month = 1;
                }
            }
            else {

                //the format could by (f)Y(ear)nnnnQ(tr|uarter)
                //or it could be Q()nnn(f)Y()nnn
                //or it could be nnnnQnumber
                //or it could be Q1 '04
                //Same also for month
                int startPosn[] = {-1,-1,-1}; //0 is year, 1 is qtr, 2 is mon
                int endPosn[] = {-1,-1,-1};

                //first get the general idea of what is there in the string
                for (int i=0;i<size;i++) {
                   char ch = strch[i];
                   if (Character.isDigit(ch) && startPosn[0] == -1 && startPosn[1] == -1 && startPosn[2] == -1)
                      startPosn[0] = i;
                   else if (ch == 'y' || ch == 'Y' || ch =='\'')
                      startPosn[0] = i;
                   else if (ch == 'Q' || ch == 'q')
                      startPosn[1] = i;
                   else if (ch == 'm' || ch == 'M')
                      startPosn[2] = i;
                }

                //now get the posn where the digits start and digits end
                boolean foundData[] = {false, false, false};
                for (int art=0;art < 3; art++) {
                   if (startPosn[art] != -1) {
                      int temp;
                      for (temp=startPosn[art];temp<size;temp++)
                         if (Character.isDigit(strch[temp])) {
                            break;
                         }
                      startPosn[art] = temp;
                      for (temp=startPosn[art];temp<size;temp++)
                         if (!Character.isDigit(strch[temp])) {
                            break;
                         }
                      if (temp-startPosn[art] > 0) {
                         String tempStr = new String(strch, startPosn[art], temp-startPosn[art]);
                         int val = Integer.parseInt(tempStr);
                         foundData[art] = true;
                         if (art == 0) {
                            year = val;
                         }
                         else if (art == 1) {
                            quarter = val;
                         }
                         else if (art == 2) {
                            month = val;
                         }
                      }
                  } //if there was a valid start
               }//looped through all artificial loops
               if (!Misc.isUndef(year) && (!Misc.isUndef(quarter) || !Misc.isUndef(month)))
                  year = getFullYearFrom2Digit(year);
               if (foundData[2]) {
                   m_scope = Misc.SCOPE_MONTH;
                   if (startDate.getYear() >= (year-1900) && startDate.getMonth() >= (month-1)) {
                      day = startDate.getDate();
                   }
                   else
                      day = 1;
               }
               else if (foundData[1]) {
                   m_scope = Misc.SCOPE_QTR;
               }
               else {
                   m_scope = Misc.SCOPE_ANNUAL;
                   if ((year-1900) <= startDate.getYear()) {
                     day = startDate.getDate();
                     month = startDate.getMonth()+1;
                   }
                   else {
                     day = 1;
                     month = 1;
                   }
               }


            }//is not of 02/04 OR 2002 format
            //DO OTHER processing

            if (!Misc.isUndef(year) && year < 1900) { //the years are relatively numbered
//              year = startDate.getYear()+year+1900;
                java.util.Date tempDate = new java.util.Date(startDate.getTime());
                tempDate.setYear(tempDate.getYear()+year-1);
                year = tempDate.getYear()+1900;
                m_doingRelative = true;
                month = startDate.getMonth()+1;
                day = startDate.getDate();
            }
            if (!Misc.isUndef(quarter) && Misc.isUndef(year) && Misc.isUndef(day) && Misc.isUndef(month)) {
//              relatively numbered quarter
//              int numYear = quarter/4;
//              year = startDate.getYear()+1900+numYear;
//              quarter = quarter % 4+1; //the quarters are numbered relative to 1 in TimePeriodHelper

                java.util.Date tempDate = new java.util.Date(startDate.getTime());
                tempDate.setMonth(tempDate.getMonth()+(quarter-1)*3);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                quarter = (month-1)/3+1;
                day = tempDate.getDate();
                m_doingRelative = true;
            }
            else if (Misc.isUndef(quarter) && Misc.isUndef(year) && Misc.isUndef(day) && !Misc.isUndef(month)) {
//              relatively numbered months
//              int numYear = month/12;
//              year = startDate.getYear()+1900+numYear;
//              month = month % 12+startDate.getMonth(); //temporaruly switched to 0 based ..
//              if (month > 11) {
//                 year++;
//                 month -= 12;
//              }
//              month++;

                java.util.Date tempDate = new java.util.Date(startDate.getTime());
                tempDate.setMonth(tempDate.getMonth()+month-1);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                quarter = (month-1)/3+1;
                day = tempDate.getDate();
                m_doingRelative = true;
            }
            else if (Misc.isUndef(quarter) && Misc.isUndef(year) && !Misc.isUndef(day) && Misc.isUndef(month)) {
                //relatively numbered days
                java.util.Date tempDate = new java.util.Date(startDate.getTime());
                tempDate.setDate(tempDate.getDate()+day-1);
                year = tempDate.getYear()+1900;
                month = tempDate.getMonth()+1;
                quarter = (month-1)/3+1;
                day = tempDate.getDate();
                m_doingRelative = true;
            }

            //Fill missing values - note that year is all filled up unless there was a mjor issue
            if (!Misc.isUndef(year)) {
                if (!Misc.isUndef(month) && Misc.isUndef(quarter))
                    quarter = ((month-1)/3) + 1;
                else if (Misc.isUndef(month) && !Misc.isUndef(quarter))
                    month = (quarter-1)*3+1;
                else if (Misc.isUndef(month) && Misc.isUndef(quarter)) {
                   if (!m_doingRelative) {
                      month = 1;
                      quarter = 1;
                   }
                   else if (startDate != null) {
                      month = startDate.getMonth()+1;
                      quarter = (month-1)/3+1;
                   }
                   else {
                      month = 1;
                      quarter = 1;
                   }
                }
                if (Misc.isUndef(day)) {
                 // if (startDate != null) {
                 //    day = startDate.getDate();
                 //  }
                 //  else {
                 if (!m_doingRelative) {
                      day = 1;
                 }
                 else {
                    day = statDate.getDate();
                 }
                 //  }
                }

                m_date = new java.util.Date(year-1900, month-1, day);
                if (m_date.before(startDate)) {
                    m_date.setDate(startDate.getDate());
                }
                m_lineItemStartDate = startDate;
                m_lineItemEndDate = endDate;
                if (m_doingRelative)
                   m_doPartial = true;
                if (m_date.after(
                if (date.after(m_date) && date.before(dateEnd) && !date.equals(m_date)) {
                   m_doPartial = true;
                }


                helperAdjustForStartFin(startDate, finishDate);
            }

        }//end of try block of catch ...
        catch (Exception e) {
            e.printStackTrace();
        }
      }

            public void setStartIndices(java.util.Date date) { //if relative then 0 and will always go to lower level
         java.util.Date dateEnd = getEndDate();
         m_startIndex = 0;
//         m_supplementalStartIndex = 0;
         int lower_level_scope = TimePeriodHelper.getNextLowerLevel(m_scope);

         if (m_doPartial && !m_doingRelative) {
             if (lower_level_scope == Misc.SCOPE_QTR) { //means upper level was annual
                 m_startIndex = date.getMonth()/3;
             }
             else if (lower_level_scope == Misc.SCOPE_MONTH) { //upper level was Qtr
                    m_startIndex = date.getMonth()%3;
             }
             else if (lower_level_scope == Misc.SCOPE_WEEK) { //upper level was month
                    //m_startIndex = (date.getDate()-1)/7;
                    m_startIndex = 0;
             }
             else if (lower_level_scope == Misc.SCOPE_DAY) { //upper level was week
                 m_startIndex = (date.getDate()-1)%7;
             }
         }
      }

      public void setLastIndices(java.util.Date date) {
         java.util.Date dateEnd = getEndDate();
         m_lastIndex = 1;
//         m_supplementalLastIndex = 1;
         int lower_level_scope = TimePeriodHelper.getNextLowerLevel(m_scope);


         if (date != null && date.before(dateEnd) && date.after(m_date) && !date.equals(m_date)) {
             m_doPartial = true;
             if (m_doingRelative) { //TODO - only works till SCOPE_MONTH
                int monDiff = Misc.getMonthsDiff(dateEnd, date);

                if (lower_level_scope == Misc.SCOPE_QTR) {
                    m_lastIndex = 4 - monDiff/3;
                }
                else if (lower_level_scope == Misc.SCOPE_MONTH) {
                   if (m_scope == Misc.SCOPE_ANNUAL)
                      m_lastIndex = 12 - monDiff;
                   else
                      m_lastIndex = 3 - monDiff;
                }
             }
             else {
                if (lower_level_scope == Misc.SCOPE_QTR) { //means upper level was annual
                    m_lastIndex = date.getMonth()/3+1;
                }
                else if (lower_level_scope == Misc.SCOPE_MONTH) { //upper level was Qtr
                    m_lastIndex = date.getMonth()%3+1;
                }
                else if (lower_level_scope == Misc.SCOPE_WEEK) { //upper level was month
                    //m_lastIndex = (date.getDate()-1)/7+1;

                }
                else if (lower_level_scope == Misc.SCOPE_DAY) { //upper level was week
                    m_lastIndex = (date.getDate()-1)%7+1;
                }
             }
         }
         else if (m_doPartial) {
             if (lower_level_scope == Misc.SCOPE_QTR) { //means upper level was annual
                 m_lastIndex = 4;
             }
             else if (lower_level_scope == Misc.SCOPE_MONTH) { //upper was SCOPE_ANNUAL
                 m_lastIndex = 3;
             }
             else if (lower_level_scope == Misc.SCOPE_WEEK) {
                 m_lastIndex = 5;
             }
             else if (lower_level_scope == Misc.SCOPE_DAY) {
                 m_lastIndex = 7;
             }
         }
      }



      public void helperAdjustForStartFin(java.util.Date startDate, java.util.Date finishDate) {
        //NOT CORRECT FOR FISCAL YEAR adjustments
        //           m_year = Misc.getAbsFiscalYearFromRelToStart(m_year, startDate);

//        boolean needToDoAdjust = (m_scope == 1 && Misc.PARTIAL_DATA_SCOPE != 1) ||
//               (m_scope == 0 && (Misc.PARTIAL_DATA_SCOPE == 2 || Misc.PARTIAL_DATA_SCOPE == 3)) ||
//               (m_scope == 2 && Misc.PARTIAL_DATA_SCOPE == 3);

//        if (needToDoAdjust) {
            setStartIndices(startDate);
            setLastIndices(finishDate);
//        }
      }


      public static long getBegTimeId(int scope, int timeId) {
         return getTimeId(scope, getTimeVal(scope, timeId));
      }
      public static long getNextTimeId(int scope, int timeId) {
          return getBegTimeId(scope, timeId) + getTimeId(scope, 1);
      }



*/
}
