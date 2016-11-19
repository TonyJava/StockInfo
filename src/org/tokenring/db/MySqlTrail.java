package org.tokenring.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlTrail {
	 boolean isInit = false;
	 Connection conn = null;

	public  boolean init() {
		if (isInit) {
			return isInit;
		}
		// ����������
		String driver = "com.mysql.jdbc.Driver";

		// URLָ��Ҫ���ʵ����ݿ���StockInfoDB
		//String url = "jdbc:mysql://tokenring.jios.org:3306/StockInfoDB?autoReconnect=true";
		String url = "jdbc:mysql://192.168.199.90:3306/StockInfoDB?autoReconnect=true";

		// MySQL����ʱ���û���
		String user = "stockinfo";

		// MySQL����ʱ������
		String password = "stock@info";

		try {
			// ������������
			Class.forName(driver);

			// �������ݿ�
			conn = DriverManager.getConnection(url, user, password);
			isInit = !conn.isClosed();
			return isInit;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public  boolean destroy() {
		if (conn != null) {
			try {
				conn.close();
				isInit = false;
				conn = null;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public  boolean executeSQL(String sql) {
		try {
			if (!conn.isClosed()) {
				// statement����ִ��SQL���
				Statement statement = conn.createStatement();

				// ִ��ҵ�����
				return statement.execute(sql);
			} else {
				System.out.println("conn is closed.");
				return false;
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	public  ResultSet QueryBySQL(String sql) {
		try {
			if (!conn.isClosed()) {
				// statement����ִ��SQL���
				Statement statement = conn.createStatement();
				// ִ��ҵ�����
				return statement.executeQuery(sql);
			} else {
				System.out.println("Conn is closed.");
				return null;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

	}
	public static void main(String[] args) throws SQLException {
		MySqlTrail mst = new MySqlTrail();
		boolean b = mst.init();
		
		String sql = "Select StockID,StockBelong FROM T_StockBaseInfo where NOT (StockID = '000001' and StockBelong = 'SH')  AND NOT (StockID = '399001' and StockBelong = 'SZ')";
		ResultSet rs = mst.QueryBySQL(sql);
		
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
		mst.destroy();
	}
}
