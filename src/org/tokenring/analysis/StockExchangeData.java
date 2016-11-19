package org.tokenring.analysis;

public class StockExchangeData {
	String ExDate;
	Double BeginPrice;
	Double HighestPrice;
	Double EndPrice;
	Double LowestPrice;
	int ExQuantity;
	int ExAmount;
	Double averagePrice;
	Double averageQuantity;
	Double averageAmount;
	
	//EMA��12�� = ǰһ��EMA��12�� X 11/13 + �������̼� X 2/13
	Double EMA12;
	//EMA��26�� = ǰһ��EMA��26�� X 25/27 + �������̼� X 2/27
	Double EMA26;
	//DIF = EMA��12�� - EMA��26��
	Double DIF;
	//DEA = ��ǰһ��DEA X 8/10 + ����DIF X 2/10��
	Double DEA;
	//��DIF-DEA��*2��ΪMACD��״ͼ
	Double MACD;
	/*
	 * Ͷ���߲ο���
1.��DIF��MACD������0(����ͼ���ϱ�ʾΪ���Ǵ�����������)�������ƶ�ʱ��һ���ʾΪ���鴦�ڶ�ͷ�����У��������뿪�ֻ��ͷ�ֲ֣�
2.��DIF��MACD��С��0(����ͼ���ϱ�ʾΪ���Ǵ�����������)�������ƶ�ʱ��һ���ʾΪ���鴦�ڿ�ͷ�����У������������ֻ������
3.��DIF��MACD������0(����ͼ���ϱ�ʾΪ���Ǵ�����������)���������ƶ�ʱ��һ���ʾΪ���鴦���µ��׶Σ������������ֺ͹�����
4.��DIF��MACD��С��0ʱ(����ͼ���ϱ�ʾΪ���Ǵ�����������)�������ƶ�ʱ��һ���ʾΪ���鼴�����ǣ���Ʊ�����ǣ��������뿪�ֻ��ͷ�ֲ֡�

������ԭ��Ϊ��
1.DIF��DEA��Ϊ����DIF����ͻ��DEA�������źŲο���
2.DIF��DEA��Ϊ����DIF���µ���DEA�������źŲο���
3.DIF����K�߷������룬������ܳ��ַ�ת�źš�
4.DIF��DEA��ֵ��������ɸ��������ߴӸ���������������ǽ����źţ���Ϊ����������г���
	*/
	 
	Double myEMA12;
	Double myEMA26;
	Double myDIF;
	Double myDEA;
	Double myMACD;
	
	public Double getAveragePrice() {
		return averagePrice;
	}
	public void setAveragePrice(Double averagePrice) {
		this.averagePrice = averagePrice;
	}
	public Double getAverageQuantity() {
		return averageQuantity;
	}
	public void setAverageQuantity(Double averageQuantity) {
		this.averageQuantity = averageQuantity;
	}
	public Double getAverageAmount() {
		return averageAmount;
	}
	public void setAverageAmount(Double averageAmount) {
		this.averageAmount = averageAmount;
	}
	public String getExDate() {
		return ExDate;
	}
	public void setExDate(String exDate) {
		ExDate = exDate;
	}
	public Double getBeginPrice() {
		return BeginPrice;
	}
	public void setBeginPrice(Double beginPrice) {
		BeginPrice = beginPrice;
	}
	public Double getHighestPrice() {
		return HighestPrice;
	}
	public void setHighestPrice(Double highestPrice) {
		HighestPrice = highestPrice;
	}
	public Double getEndPrice() {
		return EndPrice;
	}
	public void setEndPrice(Double endPrice) {
		EndPrice = endPrice;
	}
	public Double getLowestPrice() {
		return LowestPrice;
	}
	public void setLowestPrice(Double lowestPrice) {
		LowestPrice = lowestPrice;
	}
	public int getExQuantity() {
		return ExQuantity;
	}
	public void setExQuantity(int exQuantity) {
		ExQuantity = exQuantity;
	}
	public int getExAmount() {
		return ExAmount;
	}
	public void setExAmount(int exAmount) {
		ExAmount = exAmount;
	}
	public Double getEMA12() {
		return EMA12;
	}
	public void setEMA12(Double eMA12) {
		EMA12 = eMA12;
	}
	public Double getEMA26() {
		return EMA26;
	}
	public void setEMA26(Double eMA26) {
		EMA26 = eMA26;
	}
	public Double getDIF() {
		return DIF;
	}
	public void setDIF(Double dIF) {
		DIF = dIF;
	}
	public Double getDEA() {
		return DEA;
	}
	public void setDEA(Double dEA) {
		DEA = dEA;
	}
	public Double getMACD() {
		return MACD;
	}
	public void setMACD(Double mACD) {
		MACD = mACD;
	}

	public StockExchangeData(String ExDate,Double BeginPrice,Double HighestPrice,Double EndPrice,Double LowestPrice,int ExQuantity,int ExAmount){
		this.ExDate = ExDate;
		this.BeginPrice = BeginPrice;
		this.HighestPrice = HighestPrice;
		this.EndPrice = EndPrice;
		this.LowestPrice = LowestPrice;
		this.ExQuantity = ExQuantity;
		this.ExAmount = ExAmount;
	}
	public Double getMyEMA12() {
		return myEMA12;
	}
	public void setMyEMA12(Double myEMA12) {
		this.myEMA12 = myEMA12;
	}
	public Double getMyEMA26() {
		return myEMA26;
	}
	public void setMyEMA26(Double myEMA26) {
		this.myEMA26 = myEMA26;
	}
	public Double getMyDIF() {
		return myDIF;
	}
	public void setMyDIF(Double myDIF) {
		this.myDIF = myDIF;
	}
	public Double getMyDEA() {
		return myDEA;
	}
	public void setMyDEA(Double myDEA) {
		this.myDEA = myDEA;
	}
	public Double getMyMACD() {
		return myMACD;
	}
	public void setMyMACD(Double myMACD) {
		this.myMACD = myMACD;
	}
}
