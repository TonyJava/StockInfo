package org.tokenring.analysis;

public class AnalyzeMyMACD implements AnalyzeHistory {
	StockHistory stockHistory;

	public AnalyzeMyMACD(StockHistory StockHistory) {
		stockHistory = StockHistory;
		stockHistory.calcMyMACD();
	}

	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		StockExchangeData sedToday = stockHistory.getHisDataByExDate(idx);
		StockExchangeData sedYesterday = stockHistory.getHisDataByExDate(idx + 1);

		if ((sedYesterday != null) && (sedToday != null)) {
			Double deltaDIF = sedToday.getMyDIF() - sedYesterday.getMyDIF();
			Double deltaDEA = sedToday.getMyDEA() - sedYesterday.getMyDEA();

			boolean isCrossed = ((sedToday.getMyDIF() - sedToday.getMyDEA())
					* (sedYesterday.getMyDIF() - sedYesterday.getMyDEA())) < 0;

			if (isCrossed) {
				if (deltaDIF > deltaDEA) {
					// ����
					// if ((sedToday.getDIF() > 0) && (sedToday.getDEA() > 0) &&
					// ((Math.atan(deltaDIF) -
					// Math.atan(deltaDEA))>(15*Math.PI/180) )){
					/*
					 * if ((Math.atan(deltaDIF) -
					 * Math.atan(deltaDEA))>(15*Math.PI/180)) { return new
					 * Event(stockHistory.getStockID(),
					 * stockHistory.getStockBelong(),
					 * stockHistory.getStockName(), "MACD ����ͻ�� 15%", idx,
					 * stockHistory.getHisDataByExDate(idx).getExDate()); }
					 */
					// ����ͻ��
					return new Event(stockHistory.getStockID(), stockHistory.getStockBelong(),
							stockHistory.getStockName(), "MyMACD ����ͻ��", idx,
							stockHistory.getHisDataByExDate(idx).getExDate());
				} else {
					// ����ͻ��
					return new Event(stockHistory.getStockID(), stockHistory.getStockBelong(),
							stockHistory.getStockName(), "MyMACD ����ͻ��", idx,
							stockHistory.getHisDataByExDate(idx).getExDate());
				}
			}
		}

		return null;
	}

}
