package cn.demo.test.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBConnnection {
	 protected static final Logger logger = LoggerFactory.getLogger(DBConnnection.class);
	private static String driverName = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
	private static String dbURL = "jdbc:sqlserver://localhost:1433;DatabaseName=BookingImp";
	private static String userName = "sa";
	private static String userPwd = "123456";
	private static Connection conn = null;

	public static Connection getConnection() {
		Connection conn = null;
		try {
			Class.forName(driverName);
			conn = DriverManager.getConnection(dbURL, userName, userPwd);
			// System.out.println("连接数据库成功");
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.print("连接失败");
		}
		return conn;
	}
	
	public static ResultSet getResultSet(String sqlToExcute) {
		conn =  DBConnnection.getConnection();
		ResultSet rs = null;
		Statement statement = null;
		try {
			statement = conn.createStatement();
			rs = statement.executeQuery(sqlToExcute);
			// System.out.println("连接数据库成功");
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.print("连接失败");
		}
		return rs;
	}

	public static void execDelete(String deleteSQL){
		conn =  DBConnnection.getConnection();
		Statement statement = null;
		try {
			statement = conn.createStatement();
			 statement.executeUpdate(deleteSQL);
			 logger.info("the query: '"+deleteSQL+ "' was executed successfuly! ");
			// System.out.println("连接数据库成功");			
		} catch (Exception e) {
			e.printStackTrace();
			// System.out.print("连接失败");
		}finally{
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	}

	public static String columnResult(String SQL) {
		conn = DBConnnection.getConnection();
		String[] columns = parseSQL(SQL);
		logger.info("columns: "+Arrays.toString(columns));
		Statement statement = null;
		ResultSet rs = getResultSet(SQL);
		String result = null;
		String fieldName_currrent = null;
		try {
			// System.out.println("String columnResult(String SQL):
			// Arrays.toString(columns): "+Arrays.toString(columns));
			result = getResult(rs,columns);
		} catch (SQLException e) {
			System.out.println("SQLException! please either check your DB connection or DB value for the field: "
					+ fieldName_currrent);
			e.printStackTrace();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
				if (statement != null) {
					statement.close();
				}
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return result;
	}

	public static String[] parseSQL(String sql) {
		sql = sql.trim();
		int endIndex = sql.indexOf("from");
		String columns = sql.substring(6, endIndex).trim();
		String[] columArr = null;
		if (columns.equals("*")) {
			columArr = new String[] { "CardID", "CardNumber", "CardIssuerID", "CardHolderFirstName",
					"CardHolderLastName", "CardActiveDate", "CardExpireDate", "CardLimitAmt", "CreateDate",
					"CreateBy" };
		} else {
			columArr = columns.split(",");
		}
		return columArr;
	}

	public static String getResult(ResultSet rs, String[] columns) throws SQLException {
		String result = null;
		while (rs.next()) {
			StringBuilder resultSB = new StringBuilder("");
			String tempString = null;
		for (String fieldName : columns) {
			if (fieldName.equalsIgnoreCase("CardID")) {
				tempString = Integer.toString(rs.getInt(fieldName)) + ",";
			} else if (fieldName.equalsIgnoreCase("CardNumber") || fieldName.equalsIgnoreCase("CardHolderFirstName")
					|| fieldName.equalsIgnoreCase("CardHolderLastName") || fieldName.equalsIgnoreCase("CreateBy")) {
				tempString = rs.getString(fieldName) + ",";
			} else if (fieldName.equalsIgnoreCase("CardIssuerID")) {
				tempString = Byte.toString(rs.getByte(fieldName));
			} else if (fieldName.equalsIgnoreCase("CardActiveDate") || fieldName.equalsIgnoreCase("CardExpireDate")
					|| fieldName.equalsIgnoreCase("CreateDate")) {
				tempString = rs.getDate(fieldName).toString() + ",";
			} else if (fieldName.equalsIgnoreCase("CardLimitAmt")) {
				tempString = Double.toString(rs.getDouble(fieldName)) + ",";
			}
			logger.info(fieldName+": "+tempString);
			// System.out.println(fieldName+": "+tempString);
			resultSB.append(tempString);
		}
			result += resultSB.toString()+"\n";
		}
		
		return result.substring(4,result.length()-2);
	}

}
