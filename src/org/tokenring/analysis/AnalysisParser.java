package org.tokenring.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.tokenring.db.MySqlTrail;

class InnerCount {
	public int wins;
	public int losts;

	public InnerCount() {
		wins = 0;
		losts = 0;
	}
}

public class AnalysisParser {
	Logger log = Logger.getLogger(AnalysisParser.class);
	StockHistory stockHistory;

	public StockHistory getStockHistory() {
		return stockHistory;
	}

	List<AnalyzeHistory> analysisChain;
	List<AssertForcast> assertChain;
	List<Event> events;
	List<AssertEvent> assertEvents;
	List<ExDate> exDates;

	public AnalysisParser(String StockID, String StockBelong) throws SQLException {
		stockHistory = new StockHistory(StockID, StockBelong);

		// ����������
		analysisChain = new ArrayList<AnalyzeHistory>();

		// ������֤��
		assertChain = new ArrayList<AssertForcast>();

		// ��ʼ���¼�������
		events = new ArrayList<Event>();

		// ��ʼ����֤�¼�������
		assertEvents = new ArrayList<AssertEvent>();

		// ��ʼ�����������б�
		exDates = new ArrayList<ExDate>();
		int hisSize = stockHistory.getHisData().size();

		List<StockExchangeData> sedList = stockHistory.getHisData();
		ExDate ed;
		for (int idx = 0; idx < hisSize; idx++) {
			// String stockID,String stockBelong,String stockName,String
			// exDate,int idx
			StockExchangeData sed = sedList.get(idx);
			ed = new ExDate(stockHistory.getStockID(), stockHistory.getStockBelong(), stockHistory.getStockName(),
					sed.getExDate(), idx);
			exDates.add(ed);
		}
	}

	public void addParser(AnalyzeHistory ah) {
		if (!analysisChain.contains(ah)) {
			analysisChain.add(ah);
		}
	}

	public void prepareParser() {
		AnalyzeHistory fiveDaysPriceRiseTwentyPcts = new AnalyzePricePastDaysRiseRate(5, 20, stockHistory);
		analysisChain.add(fiveDaysPriceRiseTwentyPcts);

		AnalyzeHistory fiveDaysPriceFallTwentyPcts = new AnalyzePricePastDaysRiseRate(5, -20, stockHistory);
		analysisChain.add(fiveDaysPriceFallTwentyPcts);

		AnalyzeHistory threeDaysAmountRiseTenPcts = new AnalyzeAmountPastDaysRiseRate(3, 10, stockHistory);
		analysisChain.add(threeDaysAmountRiseTenPcts);

		AnalyzeHistory threeDaysAmountFallTenPcts = new AnalyzeAmountPastDaysRiseRate(3, -10, stockHistory);
		analysisChain.add(threeDaysAmountFallTenPcts);

		AnalyzeHistory fiveDaysPriceAndAmountRise = new AnalyzePriceAndAmountPastDaysContinueRiseRate(5, 1,
				stockHistory);
		analysisChain.add(fiveDaysPriceAndAmountRise);

		AnalyzeHistory fiveDaysPriceAndAmountFall = new AnalyzePriceAndAmountPastDaysContinueRiseRate(5, -1,
				stockHistory);
		analysisChain.add(fiveDaysPriceAndAmountFall);

		AnalyzeHistory fiveDaysAbove60AveragePrice = new AnalyzeAveragePrice(60, 5, false, stockHistory);
		analysisChain.add(fiveDaysAbove60AveragePrice);

		AnalyzeHistory fiveDaysBelow60AveragePrice = new AnalyzeAveragePrice(60, 5, true, stockHistory);
		analysisChain.add(fiveDaysBelow60AveragePrice);

		AnalyzeHistory fiveDaysAbove60AverageAmount = new AnalyzeAverageAmount(60, 5, false, stockHistory);
		analysisChain.add(fiveDaysAbove60AverageAmount);

		AnalyzeHistory fiveDaysBelow60AverageAmount = new AnalyzeAverageAmount(60, 5, true, stockHistory);
		analysisChain.add(fiveDaysBelow60AverageAmount);
	}

	public void addAssert(AssertForcast af) {
		if (!this.assertChain.contains(af)) {
			assertChain.add(af);
		}
	}

	public void prepareAssertForcast() {
		// int days, int rate, int max
		// AssertForcastNextNDaysMaxMRiseR nextDaysRise = new
		// AssertForcastNextNDaysMaxMRiseR(1, 1, 1);
		// assertChain.add(nextDaysRise);

		AssertForcastNextNDaysMaxMRiseR next3DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(3, 10, 1);
		assertChain.add(next3DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next5DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(5, 10, 1);
		assertChain.add(next5DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next10DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(10, 10, 1);
		assertChain.add(next10DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next20DaysRise10 = new AssertForcastNextNDaysMaxMRiseR(20, 15, 2);
		assertChain.add(next20DaysRise10);

		AssertForcastNextNDaysMaxMRiseR next30DaysRise20 = new AssertForcastNextNDaysMaxMRiseR(30, 20, 2);
		assertChain.add(next30DaysRise20);

		AssertForcastNextNDaysMaxMRiseR next60DaysRise20 = new AssertForcastNextNDaysMaxMRiseR(60, 20, 2);
		assertChain.add(next60DaysRise20);

		AssertForcastNextNDaysMaxMRiseR next90DaysRise30 = new AssertForcastNextNDaysMaxMRiseR(90, 30, 3);
		assertChain.add(next90DaysRise30);
	}

	public void doAnalyze() {
		int hisSize = stockHistory.getHisData().size();
		// List<StockExchangeData> sedList = stockHistory.getHisData();
		Event e;
		for (int idx = 0; idx < hisSize; idx++) {

			Iterator<AnalyzeHistory> iter = analysisChain.iterator();
			while (iter.hasNext()) {
				AnalyzeHistory ah = iter.next();
				e = ah.doAnalzy(idx);
				if (e != null) {
					events.add(e);
					exDates.get(idx).getEvents().add(e);
				}
			}
		}
		// log.error("events = " + events.size());
	}

	public void doAnalyzeToday() {

		Event e;

		Iterator<AnalyzeHistory> iter = analysisChain.iterator();
		while (iter.hasNext()) {
			AnalyzeHistory ah = iter.next();
			e = ah.doAnalzy(0);
			if (e != null) {
				events.add(e);
				exDates.get(0).getEvents().add(e);
			}
		}

		// log.error("events = " + events.size());
	}

	public void doAssert() {
		ExDate e;
		AssertEvent ae;
		AssertForcast af;

		// ����֤�¼�������
		Iterator<ExDate> iterExDate = exDates.iterator();
		while (iterExDate.hasNext()) {
			// ����֤����ʷ�¼�
			e = iterExDate.next();

			// ��֤��������
			Iterator<AssertForcast> iterAssert = assertChain.iterator();
			while (iterAssert.hasNext()) {
				af = iterAssert.next();
				// ��֤һ����ʷ�¼�
				ae = af.doAssert(e, this.stockHistory);
				if (ae != null) {
					assertEvents.add(ae);
					e.getAssertEvents().add(ae);
					e.setWin(true);
				}
			}
		}
		// log.error("assertChain = " + assertChain.size());
		// log.error("assertEvents = " + assertEvents.size());

	}
	public void printAll() {
		Map<String, InnerCount> mapAH = new HashMap<String, InnerCount>();

		Iterator<ExDate> iterEx = exDates.iterator();
		ExDate ex;
		Event e;
		
		while (iterEx.hasNext()) {
			ex = iterEx.next();
			Iterator<Event> iterEvent = ex.events.iterator();
			while (iterEvent.hasNext()) {
				e = iterEvent.next();
				
				StringBuffer sb = new StringBuffer();
				sb.append("[");
				sb.append(ex.stockID);
				sb.append("][");
				sb.append(e.eventMsg);
				sb.append("][");
				sb.append(e.exDate);
				sb.append("]");
				//log.info(sb.toString());

				if (mapAH.containsKey(e.eventMsg)) {
					InnerCount ic = (InnerCount) mapAH.get(e.eventMsg);
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				} else {
					InnerCount ic = new InnerCount();
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				}
			}
		}

		// print mapAH msg;
		Iterator entries = mapAH.entrySet().iterator();
		DecimalFormat df = new DecimalFormat("######0.00");
		
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			String key = (String) entry.getKey();

			InnerCount value = (InnerCount) entry.getValue();

			StringBuffer sb = new StringBuffer();
			double winRate = (double) value.wins / (value.wins + value.losts);
			double rate = (double) (value.wins + value.losts) / this.exDates.size();
			
				sb.append("[");
				sb.append(key);
				sb.append("]");
				sb.append("[");
				sb.append("exDates.size()= ");
				sb.append(exDates.size());
				sb.append("]");
				sb.append("[wins = ");
				sb.append(value.wins);
				sb.append("][losts = ");
				sb.append(value.losts);
				sb.append("][winrate = ");
				if (value.losts > 0) {
					sb.append(df.format(winRate * 100));
				} else {
					sb.append(100);
				}
				sb.append("%]");

				log.info(sb.toString());
			
		}
	}
	public void printAnalyze() {
		Map<String, InnerCount> mapAH = new HashMap<String, InnerCount>();

		Iterator<ExDate> iterEx = exDates.iterator();
		ExDate ex;
		Event e;
		while (iterEx.hasNext()) {
			ex = iterEx.next();
			Iterator<Event> iterEvent = ex.events.iterator();
			while (iterEvent.hasNext()) {
				e = iterEvent.next();

				if (mapAH.containsKey(e.eventMsg)) {
					InnerCount ic = (InnerCount) mapAH.get(e.eventMsg);
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				} else {
					InnerCount ic = new InnerCount();
					if (ex.isWin) {
						ic.wins++;
					} else {
						ic.losts++;
					}
					mapAH.put(e.eventMsg, ic);
				}
			}
		}

		// print mapAH msg;
		Iterator entries = mapAH.entrySet().iterator();
		DecimalFormat df = new DecimalFormat("######0.00");
		TreeMap<Double, String> mapTopWinRate = new TreeMap<Double, String>(new Comparator<Double>() {

			/*
			 * int compare(Object o1, Object o2) ����һ���������͵����ͣ� ���ظ�����ʾ��o1 С��o2�� o1
			 * ��ǰ�� ����0 ��ʾ��o1��o2��ȣ� ����������ʾ��o1����o2�� o2 ��ǰ��
			 */
			public int compare(Double o1, Double o2) {

				// ָ�����������ս�������
				int iret;
				if (o2 == o1) {
					iret = 0;
				} else {
					iret = (o2 - o1) > 0 ? 1 : -1;
				}
				return iret;
			}
		});
		;
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry) entries.next();
			String key = (String) entry.getKey();

			InnerCount value = (InnerCount) entry.getValue();

			StringBuffer sb = new StringBuffer();
			double winRate = (double) value.wins / (value.wins + value.losts);
			double rate = (double) (value.wins + value.losts) / this.exDates.size();
			mapTopWinRate.put(winRate, key);
/*
			if ((winRate > 0.7) && (rate > 0.1)) {

				sb.append("[");
				sb.append(key);
				sb.append("]");

				sb.append("[wins = ");
				sb.append(value.wins);
				sb.append("][losts = ");
				sb.append(value.losts);
				sb.append("][winrate = ");
				if (value.losts > 0) {
					sb.append(df.format(winRate * 100));
				} else {
					sb.append(100);
				}
				sb.append("%]");

				log.info(sb.toString());
			}
*/
		}
		entries = mapTopWinRate.entrySet().iterator();
		int i = 0;
		
		MySqlTrail mySQL = new MySqlTrail();
		boolean b = mySQL.init();
		//while (entries.hasNext() && i < 3) {
		while (entries.hasNext() ) {
			Map.Entry entry = (Map.Entry) entries.next();
			Double key = (Double) entry.getKey();
			String value = (String) entry.getValue();
			StringBuffer sb = new StringBuffer();
			sb.append("[");
			sb.append(value);
			sb.append("=");
			sb.append(df.format(key * 100));
			sb.append("%]");
			log.info(sb.toString());
			
			
			
			StringBuffer sbSQL = new StringBuffer();
			sbSQL.append("insert into T_HisWinRate(StockID,StockName,StockBelong,EventMsg,WinRate) values ('");
			sbSQL.append(this.stockHistory.StockID);
			sbSQL.append("','");
			sbSQL.append(this.stockHistory.StockName);
			sbSQL.append("','");
			sbSQL.append(this.stockHistory.StockBelong);
			sbSQL.append("','");
			sbSQL.append(value);
			sbSQL.append("',");
			sbSQL.append(df.format(key * 100));
			sbSQL.append(")");
			mySQL.executeSQL(sbSQL.toString());
			
			i++;
		}
		mySQL.destroy();

	}

	public List<AssertEvent> getAssertEvents() {
		return assertEvents;
	}

	public void setAssertEvents(List<AssertEvent> assertEvents) {
		this.assertEvents = assertEvents;
	}

	public List<ExDate> getExDates() {
		return exDates;
	}

	public void setExDates(List<ExDate> exDates) {
		this.exDates = exDates;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Logger log = Logger.getLogger(AnalysisParser.class);

			MySqlTrail mySQL = new MySqlTrail();
			boolean b = mySQL.init();
			String strSQL = "select StockID,StockBelong,StockName from T_StockBaseInfo where StockName != '��ָ֤��'";
			ResultSet rs = mySQL.QueryBySQL(strSQL);

			while (rs.next()) {
				AnalysisParser ap = new AnalysisParser(rs.getString("StockID"), rs.getString("StockBelong"));
				System.out.println("[StockID = " + rs.getString("StockID") + "][StockName = " + rs.getString("StockName") + "]");
				log.fatal("[StockID = " + rs.getString("StockID") + "][StockName = " + rs.getString("StockName") + "]");
				// AnalysisParser ap = new AnalysisParser("600570", "SH");

				// AnalyzeHistory ah = new AnalyzeAverageAmount(60, 5, true);
				AnalyzeHistory ah;
				ah = new AnalyzeMACD(ap.getStockHistory());
				ap.addParser(ah);
				
				//ah = new AnalyzeBTest(ap.getStockHistory());
				//ap.addParser(ah);
				
				/*
				for (int i = 10; i <= 60; i++) {
					for (int j = 3; j < 15; j++) {
						ah = new AnalyzeAverageAmount(i, j, true, ap.getStockHistory());
						ap.addParser(ah);
						ah = new AnalyzeAverageAmount(i, j, false, ap.getStockHistory());
						ap.addParser(ah);

						ah = new AnalyzeAveragePrice(i, j, true, ap.getStockHistory());
						ap.addParser(ah);
						ah = new AnalyzeAveragePrice(i, j, false, ap.getStockHistory());
						ap.addParser(ah);

					}
				}
				*/
				ap.prepareParser();
				ap.prepareAssertForcast();
				ap.doAnalyze();
				ap.doAssert();
				ap.printAnalyze();
				//ap.printAll();
				
				DecisionTree dt = new DecisionTree(ap.exDates);
				dt.printTree();

			}
			rs.close();
			mySQL.destroy();
			 //DecisionTree dt = new DecisionTree(ap.exDates);
			 //dt.printTree();

			/*
			 * List<Event> events = ap.getEvents(); List<Event> rightEvents =
			 * new ArrayList<Event> (); List<Event> leftEvents = new
			 * ArrayList<Event> (); Iterator<Event> iterEvent =
			 * events.iterator(); Event e;
			 * 
			 * while (iterEvent.hasNext()) { // ����֤����ʷ�¼� e = iterEvent.next();
			 * 
			 * }
			 */
			/*
			 * MySqlTrail mySQL = new MySqlTrail(); boolean b = mySQL.init();
			 * String sql;
			 * 
			 * Iterator<AssertEvent> itr = ap.getAssertEvents().iterator(); sql
			 * = "truncate table t_stock_assert_event"; mySQL.executeSQL(sql);
			 * 
			 * while (itr.hasNext()) { AssertEvent e = itr.next();
			 * 
			 * StringBuffer sbSQL = new StringBuffer(); sbSQL.append(
			 * "insert into t_stock_assert_event(StockID,StockName,StockBelong,ExDate,EventMsg,AssertMsg) values ( '"
			 * ); sbSQL.append(e.getEvent().getStockID()); sbSQL.append("','");
			 * sbSQL.append(e.getEvent().getStockName()); sbSQL.append("','");
			 * sbSQL.append(e.getEvent().getStockBelong()); sbSQL.append("','");
			 * sbSQL.append(e.getEvent().getExDate()); sbSQL.append("','");
			 * sbSQL.append(e.getEvent().getEventMsg()); sbSQL.append("','");
			 * sbSQL.append(e.getAssertMsg()); sbSQL.append("')");
			 * 
			 * mySQL.executeSQL(sbSQL.toString()); }
			 */
			/*
			 * Iterator<Event> itrEvent = ap.getEvents().iterator(); sql =
			 * "truncate table t_stock_event"; mySQL.executeSQL(sql);
			 * 
			 * while (itrEvent.hasNext()) { Event e = itrEvent.next();
			 * 
			 * StringBuffer sbSQL = new StringBuffer(); sbSQL.append(
			 * "insert into t_stock_event(StockID,StockName,StockBelong,ExDate,EventMsg) values ( '"
			 * ); sbSQL.append(e.getStockID()); sbSQL.append("','");
			 * sbSQL.append(e.getStockName()); sbSQL.append("','");
			 * sbSQL.append(e.getStockBelong()); sbSQL.append("','");
			 * sbSQL.append(e.getExDate()); sbSQL.append("','");
			 * sbSQL.append(e.getEventMsg()); sbSQL.append("')");
			 * 
			 * mySQL.executeSQL(sbSQL.toString()); }
			 * 
			 * mySQL.destroy();
			 */
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

}
