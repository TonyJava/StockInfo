package org.tokenring.analysis;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.milkyway.vo.StockVO;
import org.tokenring.db.MyBatis;
import org.tokenring.db.MySqlTrail;

import com.google.gson.Gson;

public class StockHistory {
	String StockID;
	String StockBelong;
	String StockName;
	List HisData;   //HisData�б���������������Ϊ0��Խ�������idԽ��
	boolean isCaclMACD = false;
	boolean isCaclMyMACD = false;

	boolean isCaclKDJ = false;
	boolean isCaclSigma = false;
	
	public void calcAllSigma(){
		if (isCaclSigma) return;
		
		int idxLast = HisData.size() - 1;
		for (int idx = idxLast ; idx >= 0; idx--) {
			calcSigma(idx,idx + 40,3);
		}
		isCaclSigma = true;
	}
	
	/*
	 * ���ҹ�ȥN�������ֵ��һ�죬����Ϊ����idx
	 * @fromId <= @toId    
	 *
	 * @return idx: �۸���ߵ����idx
	 * 			-2 : exception
	 */
	public int searchHigh(int fromId,int toId){
		if (fromId <0) return -2;
		
		
		if (toId > HisData.size() - 1){
			toId = HisData.size() - 1;
		}
		
		int retIdx = fromId;
		Double maxPrice = getHisDataByExDate(retIdx).getEndPrice();
		for (int i = fromId;i < toId;i++){
			if (getHisDataByExDate(i).getEndPrice() > maxPrice){
				retIdx = i;
				maxPrice = getHisDataByExDate(i).getEndPrice();
			}
		}
		return retIdx;
	}
	
	/*
	 * ���ҹ�ȥN�������ֵ��һ�죬����Ϊ����idx
	 * @fromId <= @toId    
	 *
	 * @return idx: �۸���͵����idx
	 * 			-2 : exception
	 */
	public int searchLow(int fromId,int toId){
		if (fromId <0) return -2;
		
		
		if (toId > HisData.size() - 1){
			toId = HisData.size() - 1;
		}
		
		int retIdx = fromId;
		Double minPrice = getHisDataByExDate(retIdx).getEndPrice();
		for (int i = fromId;i < toId;i++){
			if (getHisDataByExDate(i).getEndPrice() < minPrice){
				retIdx = i;
				minPrice = getHisDataByExDate(i).getEndPrice();
			}
		}
		return retIdx;
	}
	
	/*
	 * ���ݹ�ȥN������ߺ���ͷ�����IDX��������������
	 * �߼��������ߵ�������͵㣬����͵㲻�ǽ��죬������
	 * 						 ����͵��ǽ��죬�½���
	 * 	    �����ߵ�������͵㣬����ߵ㲻�ǽ��죬�½���
	 * 					     ����ߵ��ǽ��죬������   
	 *
	 * @return 1 ��
	 * 		   -1 ��
	 * 		   0 ��ƽ
	 */
	public int calcTrend(int highestIdx,int lowestIdx,int todayIdx){
		int ret = 0;
		if (highestIdx >= lowestIdx){
			if (lowestIdx != todayIdx){
				ret = 1;
			}else{
				ret = -1;
			}
		}else{
			if (highestIdx != todayIdx){
				ret = -1;
			}else{
				ret = 1;
			}
		}
		return ret;
	}
	/*
	 * �˺��������ڼ������������������м���
	 * 
	 * //new algorithm
//case1
Total count:117
Beyond75:27
Beyond60:33
Beyond50:32
Beyond30:13
Below30:12

//case1 ||case2
Total count:117
Beyond75:6
Beyond60:27
Beyond50:35
Beyond30:32
Below30:17


//old algorithm
Total count:117
Beyond75:0
Beyond60:34
Beyond50:47
Beyond30:29
Below30:7
	 */
	public int calcSituation(int idx){
		
		StockExchangeData sedToday = getHisDataByExDate(idx);
		StockExchangeData sedYesterday = getHisDataByExDate(idx + 1); 
		
		//Ĭ��Ϊ��ƽ
		sedToday.setSituation(0);
		if (null == sedYesterday) return 0;
		
		//new algorithm
		
		//calcAveragePrice(idx,20);
		
		//���ݹ�ȥ5���10�����ߡ���͵���㵱�������
		//�������һ�£���������ȷ����������߲�һ�£�ȡ��ȥ20�����ݽ������չ���
		int highest5 = searchHigh(idx,idx + 5);
		int highest10 = searchHigh(idx,idx + 10);
		int lowest5 = searchLow(idx,idx + 5);
		int lowest10 = searchLow(idx,idx + 10);
		
		int trend5 = calcTrend(highest5,lowest5,idx);
		int trend10 = calcTrend(highest10,lowest10,idx);
		int trend;
		
		if (trend5 != trend10){
			int highest20 = searchHigh(idx,idx + 20);
			int lowest20 = searchLow(idx,idx + 20);
			trend = calcTrend(highest20,lowest20,idx);
		}else{
			trend = trend5;
		}
		sedToday.setSituation(trend);
		
		//�����Ƿ�յ�
		//calcAverageAmount(idx,20);
		if ((highest5 == idx) && (sedToday.getQuantityType() == 1) && (hasDownShadow(idx))){
			sedToday.setSituation(-2);
		}
		
		//
		
		 // Total count:117
     	 //Beyond75:27
         //Beyond60:13
         //Beyond50:26
         //Beyond30:25
         //Below30:26
		boolean bCase1 =  (sedToday.getQuantityType() == 1)&& (hasUpShadow(idx));  // (lowest5 == idx) &&
		boolean bCase2 = (sedToday.getExAmount() >= 3* sedYesterday.getExAmount()) 
				&& (sedYesterday.getSituation() == -1)&& (hasUpShadow(idx));
		boolean bCase3 = false;
		
		
		if (bCase1 || bCase1 || bCase3 ){
			sedToday.setSituation(2);
		}
		
		
		/*
		 * 116ֻ��Ʊ�����ʴ���70% 14ֻ�����ʴ���50%С��70% 59ֻ�����ʴ���30%С��50% 37ֻ��ʣ��6ֻ
		 * 
		 */
		/*
		if(sedYesterday.getSituation() == -1){
			calcAveragePrice(idx,20);
			if (sedToday.EndPrice <= sedYesterday.EndPrice){
				sedToday.setSituation(-1);
			}else if (sedToday.getPriceType() == 1){
				sedToday.setSituation(2);
			}else if (sedToday.EndPrice <= sedToday.averagePrice){
				sedToday.setSituation(0);
			}else{
				sedToday.setSituation(1);
			}
		}else if(sedYesterday.getSituation() == 0){
			if (sedToday.getPriceType() == 0){
				
				if (sedToday.EndPrice > sedYesterday.EndPrice){
					sedToday.setSituation(1);
				}else{
					sedToday.setSituation(-1);
				}
			}else{
				sedToday.setSituation(2);
			}
			
		}else if(sedYesterday.getSituation() == 1){
			calcAveragePrice(idx,20);
			if (sedToday.EndPrice >= sedYesterday.EndPrice){
				sedToday.setSituation(1);
			}else if (sedToday.getPriceType() == -1){
				sedToday.setSituation(2);
			}else if (sedToday.EndPrice >= sedToday.averagePrice){
				sedToday.setSituation(0);
			}else{
				sedToday.setSituation(-1);
			}
			
		}else if(sedYesterday.getSituation() == 2){
			StockExchangeData sedBeforeYesterday = getHisDataByExDate(idx + 2); 
			if (null == sedBeforeYesterday){
				sedToday.setSituation(0);
			}
			
			if((sedToday.getEndPrice() - sedYesterday.getEndPrice()) * (sedYesterday.getEndPrice() - sedBeforeYesterday.getEndPrice()) < 0){
				if (sedToday.getEndPrice()  > sedYesterday.getEndPrice()){
					sedToday.setSituation(1);
				}else if(sedToday.getEndPrice() < sedYesterday.getEndPrice()){
					sedToday.setSituation(-1);
				}else{
					sedToday.setSituation(0);
				}
				
			}else {
				if (sedToday.getEndPrice()  > sedYesterday.getEndPrice()){
					sedYesterday.setSituation(1);
					sedToday.setSituation(2);
				}else if(sedToday.getEndPrice() < sedYesterday.getEndPrice()){
					sedYesterday.setSituation(-1);
					sedToday.setSituation(2);
				}
			}
			
		}
		*/
		
		return sedToday.getSituation();
	}
	private boolean hasDownShadow(int idx) {
		StockExchangeData sed = getHisDataByExDate(idx);
		
		if((Math.min(sed.getBeginPrice(), sed.getEndPrice()) - sed.getLowestPrice())/sed.getEndPrice() > 0.015){
			return true;
		}
		
		if(sed.getBeginPrice() - sed.getEndPrice() >= 0){
			return (sed.getEndPrice() - sed.getLowestPrice()) > 0.6*(sed.getBeginPrice() - sed.getEndPrice());
		}else{
			return (sed.getBeginPrice() - sed.getLowestPrice() ) > 0.6*(sed.getEndPrice() - sed.getBeginPrice());
		}
	}

	private boolean hasUpShadow(int idx) {
		StockExchangeData sed = getHisDataByExDate(idx);
		if((sed.getHighestPrice() - Math.max(sed.getBeginPrice(), sed.getEndPrice()))/sed.getEndPrice() > 0.015){
			return true;
		}
				
		if(sed.getBeginPrice() - sed.getEndPrice() >= 0){
			return (sed.getHighestPrice() - sed.getBeginPrice() ) > (sed.getBeginPrice() - sed.getEndPrice());
		}else{
			return (sed.getHighestPrice() - sed.getEndPrice() ) > (sed.getEndPrice() - sed.getBeginPrice());
		}
		
	}

	/*
	 * @fromId <= @toId    
	 *
	 * @return 1: ��ʾfromId�����������nSigma,
	 * 		   -1:��ʾfromId�����������nSigma
	 * 			0:����
	 * 			-2 : exception
	 */
	public int calcSigma(int fromId,int toId,double nSigma){
		if (fromId <0) return -2;
		
		
		if (toId > HisData.size() - 1){
			toId = HisData.size() - 1;
		}
		
		StockExchangeData sedToday = getHisDataByExDate(fromId);
		StockExchangeData sedYesterday = getHisDataByExDate(fromId + 1);
		
		double quantitis=0;
		double daverage=0;
		int count = 0;
		
		
		for (int i = fromId;i < toId;i++){
			count ++;
			quantitis += ((StockExchangeData)HisData.get(i)).ExQuantity;
		}
		daverage = quantitis/count;
		double dsigma = 0;

		for (int i = fromId;i < toId;i++){

			dsigma += Math.pow( (((StockExchangeData)HisData.get(i)).ExQuantity - daverage), 2) ;
		}
		dsigma /= count;
		dsigma = Math.sqrt(dsigma);
		

		sedToday.setAverage(daverage);
		sedToday.setSigma(dsigma);
		//
		double prices=0;
		double pricesAverage=0;
		count = 0;
		for (int i = fromId;i < toId;i++){
			count ++;
			prices += ((StockExchangeData)HisData.get(i)).getEndPrice();
		}
		pricesAverage = prices/count;
		double dsigmaPrice = 0;

		for (int i = fromId;i < toId;i++){

			dsigmaPrice += Math.pow( (((StockExchangeData)HisData.get(i)).getEndPrice() - pricesAverage), 2) ;
		}
		dsigmaPrice /= count;
		dsigmaPrice = Math.sqrt(dsigmaPrice);
		

		sedToday.setPriceAverage(pricesAverage);
		sedToday.setPriceSigma(dsigmaPrice);
		
		if (null == sedYesterday){
			//��һ��
			sedToday.setSituation(0);
			sedToday.setPriceType(0);
			sedToday.setQuantityType(0);
			sedToday.setSituation(0);
		}else{
			if (sedToday.ExQuantity > (daverage + nSigma * dsigma)){
				sedToday.setQuantityType(1);
			}else if (sedToday.ExQuantity < (daverage - nSigma * dsigma)){
				sedToday.setQuantityType(-1);
			}else{
				sedToday.setQuantityType(0);
			}
			
			if (sedToday.EndPrice > sedYesterday.EndPrice * 1.06){
				sedToday.setPriceType(1);
			}else if (sedToday.EndPrice < sedYesterday.EndPrice * 0.94){
				sedToday.setPriceType(-1);
			}else{
				sedToday.setPriceType(0);
			}
		}
		
		calcSituation(fromId);
		return sedToday.getQuantityType();
	}
	

	public void calcMyMACD() {
		// todo
		if (isCaclMyMACD) return;
		
		int idxLast = HisData.size() - 1;
		for (int idx = idxLast ; idx >= 0; idx--) {
			StockExchangeData sedToday = getHisDataByExDate(idx);
			StockExchangeData sedYesterday = getHisDataByExDate(idx + 1);

			if (sedYesterday != null) {
				calcAverageQuantity(idx,11);
				Double newMyEMA12 = sedYesterday.getMyEMA12() * 11 / 13 + (double)sedToday.getExAmount() / sedToday.getExQuantity() * 2 / 13 * sedToday.getExQuantity() / sedToday.getAverageQuantity();
				calcAverageQuantity(idx,25);
				Double newMyEMA26 = sedYesterday.getMyEMA26() * 25 / 27 + (double)sedToday.getExAmount() / sedToday.getExQuantity() * 2 / 27 * sedToday.getExQuantity() / sedToday.getAverageQuantity();
				Double newMyDIF = newMyEMA12 - newMyEMA26;
				Double newMyDEA = sedYesterday.getMyDEA() * 8 / 10 + newMyDIF * 2 / 10;
				Double newMyMACD = (newMyDIF - newMyDEA) * 2;

				sedToday.setMyEMA12(newMyEMA12);
				sedToday.setMyEMA26(newMyEMA26);
				sedToday.setMyDIF(newMyDIF);
				sedToday.setMyDEA(newMyDEA);
				sedToday.setMyMACD(newMyMACD);
			} else {
				// sedYesterday == null ��ʾ�ǵ�һ��
				// DIFF = 0,DEA = 0,MACD = 0,EMA12 = EMA26 = ���̼�
				sedToday.setMyDIF(0.0);
				sedToday.setMyDEA(0.0);
				sedToday.setMyMACD(0.0);
				sedToday.setMyEMA12((double)sedToday.getExAmount() / sedToday.getExQuantity());
				sedToday.setMyEMA26((double)sedToday.getExAmount() / sedToday.getExQuantity());
			}

		}
		isCaclMyMACD = true;
	}
	
	public void calcMACD() {
		// todo
		if (isCaclMACD) return;
		
		int idxLast = HisData.size() - 1;
		for (int idx = idxLast ; idx >= 0; idx--) {
			StockExchangeData sedToday = getHisDataByExDate(idx);
			StockExchangeData sedYesterday = getHisDataByExDate(idx + 1);

			if (sedYesterday != null) {
				Double newEMA12 = sedYesterday.getEMA12() * 11 / 13 + sedToday.getEndPrice() * 2 / 13;
				Double newEMA26 = sedYesterday.getEMA26() * 25 / 27 + sedToday.getEndPrice() * 2 / 27;
				Double newDIF = newEMA12 - newEMA26;
				Double newDEA = sedYesterday.getDEA() * 8 / 10 + newDIF * 2 / 10;
				Double newMACD = (newDIF - newDEA) * 2;

				sedToday.setEMA12(newEMA12);
				sedToday.setEMA26(newEMA26);
				sedToday.setDIF(newDIF);
				sedToday.setDEA(newDEA);
				sedToday.setMACD(newMACD);
			} else {
				// sedYesterday == null ��ʾ�ǵ�һ��
				// DIFF = 0,DEA = 0,MACD = 0,EMA12 = EMA26 = ���̼�
				sedToday.setDIF(0.0);
				sedToday.setDEA(0.0);
				sedToday.setMACD(0.0);
				sedToday.setEMA12(sedToday.getEndPrice());
				sedToday.setEMA26(sedToday.getEndPrice());
			}

		}
		isCaclMACD = true;
	}
	
	/*
	 * KDJ�ļ���Ƚϸ��ӣ�����Ҫ�������ڣ�n�ա�n�ܵȣ���RSVֵ����δ�������ָ��ֵ��Ȼ���ټ���Kֵ��Dֵ��Jֵ�ȡ���n��KDJ��ֵ�ļ���Ϊ��������㹫ʽΪ
		n��RSV=��Cn��Ln��/��Hn��Ln����100
		��ʽ�У�CnΪ��n�����̼ۣ�LnΪn���ڵ���ͼۣ�HnΪn���ڵ���߼ۡ�
		��Σ�����Kֵ��Dֵ��
		����Kֵ=2/3��ǰһ��Kֵ+1/3������RSV
		����Dֵ=2/3��ǰһ��Dֵ+1/3������Kֵ
		����ǰһ��K ֵ��Dֵ����ɷֱ���50�����档
		Jֵ=3*����Kֵ-2*����Dֵ
		��9��Ϊ���ڵ�KD��Ϊ������δ�������ֵ�����㹫ʽΪ
		9��RSV=��C��L9���£�H9��L9����100
		��ʽ�У�CΪ��9�յ����̼ۣ�L9Ϊ9���ڵ���ͼۣ�H9Ϊ9���ڵ���߼ۡ�
		Kֵ=2/3����8��Kֵ+1/3����9��RSV
		Dֵ=2/3����8��Dֵ+1/3����9��Kֵ
		Jֵ=3*��9��Kֵ-2*��9��Dֵ
		����ǰһ��Kֵ��Dֵ������Էֱ���50���档
	 */
	public void calcKDJ() {
		// todo
		if (isCaclKDJ) return;
		
		int idxLast = HisData.size() - 1;
		for (int idx = idxLast ; idx >= 0; idx--) {
			StockExchangeData sedToday = getHisDataByExDate(idx);
			StockExchangeData sedYesterday = getHisDataByExDate(idx + 1);

			double h = getHisDataByExDate(searchHigh(idx,idx + 9)).getEndPrice();
			double l = getHisDataByExDate(searchLow(idx,idx + 9)).getEndPrice();
			double k,d,j,rsv;
			if ((sedYesterday == null) ||(h == l)){
				rsv = 50.0;
				k = 2.0 / 3 * 50 + 1.0 / 3 * rsv;
				d = 2.0 / 3 * 50 + 1.0 / 3 * k;
				j = 3.0 * k - 2.0 * d;
			} else {
				rsv = 1.0 * (sedToday.getEndPrice() - l) / (h - l) * 100;
				k = 2.0 / 3 * sedYesterday.getK() + 1.0 / 3 * rsv;
				d = 2.0 / 3 * sedYesterday.getD() + 1.0 / 3 * k;
				j = 3.0 * k - 2.0 * d;						
			}
			sedToday.setRSV(rsv);
			sedToday.setK(k);
			sedToday.setD(d);
			sedToday.setJ(j);
		}
		isCaclKDJ = true;
	}

	public void calcAveragePrice(int idx, int days) {
		StockExchangeData sedLast = getHisDataByExDate(idx + days - 1);
		if (sedLast != null) {
			Double sumPrice = (double) 0;
			for (int i = 0; i < days; i++) {
				sumPrice += getHisDataByExDate(idx + i).getEndPrice();
			}
			getHisDataByExDate(idx).setAveragePrice(sumPrice / days);
		} else {
			getHisDataByExDate(idx).setAveragePrice((double) -1);
		}
	}

	public void calcAverageQuantity(int idx, int days) {
		StockExchangeData sedLast = getHisDataByExDate(idx + days - 1);
		if (sedLast != null) {
			Double sumQuantity = (double) 0;
			for (int i = 0; i < days; i++) {
				sumQuantity += getHisDataByExDate(idx + i).getExQuantity();
			}
			getHisDataByExDate(idx).setAverageQuantity(sumQuantity / days);
		} else {
			getHisDataByExDate(idx).setAverageQuantity((double) -1);
		}
	}

	public void calcAverageAmount(int idx, int days) {
		StockExchangeData sedLast = getHisDataByExDate(idx + days - 1);
		if (sedLast != null) {
			Double sumAmount = (double) 0;
			for (int i = 0; i < days; i++) {
				sumAmount += getHisDataByExDate(idx + i).getExAmount();
			}
			getHisDataByExDate(idx).setAverageAmount(sumAmount / days);
		} else {
			getHisDataByExDate(idx).setAverageAmount((double) -1);
		}
	}

	public String getStockID() {
		return StockID;
	}

	public void setStockID(String stockID) {
		StockID = stockID;
	}

	public String getStockBelong() {
		return StockBelong;
	}

	public void setStockBelong(String stockBelong) {
		StockBelong = stockBelong;
	}

	public String getStockName() {
		return StockName;
	}

	public void setStockName(String stockName) {
		StockName = stockName;
	}

	public List getHisData() {
		return HisData;
	}

	public void setHisData(List hisData) {
		HisData = hisData;
	}

	public StockExchangeData getHisDataByExDate(String ExDate) {
		int isize = HisData.size();
		int i = 0;
		boolean bMatch = false;
		StockExchangeData sed = null;
		while ((i < isize) && !bMatch) {
			sed = (StockExchangeData) (HisData.get(i));
			bMatch = sed.getExDate().equals(ExDate);
			i++;
		}

		if (bMatch) {
			return sed;
		} else {
			return null;
		}
	}

	public int getIdxByExDate(String ExDate) {
		int isize = HisData.size();
		int i = 0;
		boolean bMatch = false;
		StockExchangeData sed = null;
		while ((i < isize) && !bMatch) {
			sed = (StockExchangeData) (HisData.get(i));
			bMatch = sed.getExDate().equals(ExDate);
			i++;
		}

		if (bMatch) {
			return i - 1;
		} else {
			return -1;
		}
	}

	public StockExchangeData getHisDataByExDate(int idx) {
		if ((idx >= 0) && (idx < HisData.size())) {
			return (StockExchangeData) HisData.get(idx);
		} else {
			return null;
		}

	}

	public StockHistory(String StockID, String StockBelong) throws SQLException {
		this.StockID = StockID;
		this.StockBelong = StockBelong;
		this.HisData = new ArrayList();
		
		MyBatis mb = MyBatis.getInstance();

		String strSQL = "select StockName from T_StockBaseInfo where StockID = '" + StockID + "' and StockBelong = '"
				+ StockBelong + "' limit 1";
		List<Map> lm = mb.queryBySQL(strSQL);
		Map m = lm.get(0);
		//ResultSet rs = mySQL.QueryBySQL(strSQL);

		if ((m != null) && (m.get("StockName") != null)) {
			this.StockName = (String) m.get("StockName");
		}
		
		// initHisData
		// strSQL = "select
		// ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity,ExAmount
		// from t_stockhis_sina where StockID = '" + StockID + "' and
		// StockBelong = '" + StockBelong + "' order by ExDate desc";
		strSQL = "select ExDate,BeginPrice,HighestPrice,EndPrice,LowestPrice,ExQuantity/10000 NewExQ,ExAmount/10000 NewExA from t_stockadjhis_sina where StockID = '"
				+ StockID + "' and StockBelong = '" + StockBelong + "' order by ExDate desc";
		lm = mb.queryBySQL(strSQL);
		Iterator itr = lm.iterator();

		while (itr.hasNext()) {
			m = (Map) itr.next();
			StockExchangeData sed = new StockExchangeData((String)m.get("ExDate"), 
					((BigDecimal) m.get("BeginPrice")).doubleValue(),//Double.valueOf((String)m.get("BeginPrice")),
					((BigDecimal) m.get("HighestPrice")).doubleValue(),
					((BigDecimal) m.get("EndPrice")).doubleValue(),
					((BigDecimal) m.get("LowestPrice")).doubleValue(),
					((BigDecimal) m.get("NewExQ")).intValue(),
					((BigDecimal) m.get("NewExA")).intValue()
					);

			HisData.add(sed);
		}

	}

	public static void main(String[] args) throws ParseException {
		// TODO Auto-generated method stub
		Logger log = Logger.getLogger(StockHistory.class);
		try {
			StockHistory st = new StockHistory("601012", "SH");

			System.out.println("StockID:" + st.getStockID());
			System.out.println("StockName:" + st.getStockName());
			System.out.println("StockBelong:" + st.getStockBelong());
			System.out.println("20150407 EndPrice :" + st.getHisDataByExDate("2015-04-07").getEndPrice());
			System.out.println("20160405 EndPrice :" + st.getHisDataByExDate(1).getEndPrice());
			st.calcAllSigma();
			StockExchangeData sed;
			
			Gson gson = new Gson();
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
			List<StockVO> stockVOs = new ArrayList<StockVO>();
			StockVO svo = new StockVO();
			
			Iterator<StockExchangeData> iter = st.HisData.iterator();
			while (iter.hasNext()) {
				sed = iter.next();
				//log.info(String.format("%s,%s,%s,%s,%f,%f,%f,%f,%d",sed.getExDate(),sed.getSituation(),sed.getPriceType(),sed.getQuantityType(),sed.HighestPrice,sed.BeginPrice,sed.LowestPrice,sed.EndPrice,sed.ExQuantity)); 
				//log.info(String.format("[exdate=%s][situation=%d][pricetype=%d][QuantityType=%d]",sed.getExDate(),sed.getSituation(),sed.getPriceType(),sed.getQuantityType()));
				svo = new StockVO();
				svo.setExDate(sdf.parse(sed.getExDate()));
				svo.setExDateTs(svo.getExDate().getTime());
				svo.setHigh(sed.getHighestPrice());
				svo.setLow(sed.getLowestPrice());
				svo.setOpen(sed.getBeginPrice());
				svo.setClose(sed.getEndPrice());
				svo.setQuantity(sed.getExQuantity());
				svo.setSituation(sed.getSituation());
				
				stockVOs.add(svo);
				
			}
			log.error(gson.toJson(stockVOs));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
