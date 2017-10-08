package org.tokenring.analysis;

public class AnalyzeSigmaQuantity implements AnalyzeHistory {
	StockHistory stockHistory;
	
	public AnalyzeSigmaQuantity(StockHistory StockHistory){
		stockHistory = StockHistory;
		stockHistory.calcAllSigma();
	}
	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		StockExchangeData sedToday = stockHistory.getHisDataByExDate(idx);
		if (sedToday.getQuantityType() == 1){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"Sigma���� ��������",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
			
		}else if (sedToday.getQuantityType() == -1){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"Sigma���� �������",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
		}
		return null;
	}

}
