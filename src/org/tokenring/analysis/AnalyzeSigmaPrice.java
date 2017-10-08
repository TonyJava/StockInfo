package org.tokenring.analysis;

public class AnalyzeSigmaPrice implements AnalyzeHistory {
	StockHistory stockHistory;
	
	public AnalyzeSigmaPrice(StockHistory StockHistory){
		stockHistory = StockHistory;
		stockHistory.calcAllSigma();
	}

	@Override
	public Event doAnalzy(int idx) {
		// TODO Auto-generated method stub
		StockExchangeData sedToday = stockHistory.getHisDataByExDate(idx);
		if (sedToday.getPriceType() == 1){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"Sigma���� ���۴���",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
			
		}else if (sedToday.getPriceType() == -1){
			return new Event(stockHistory.getStockID(),
					stockHistory.getStockBelong(),
					stockHistory.getStockName(),
					"Sigma���� ���۴��",
					idx,
					stockHistory.getHisDataByExDate(idx).getExDate());
		}
		return null;
	}

}
