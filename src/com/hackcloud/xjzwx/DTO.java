package com.hackcloud.xjzwx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DTO {
	

		//用户名

		private String mysqlUser = "root";

		//密码

		private String mysqlPassword = "19781130qs";

		//主机

		private String mysqlHost = "127.0.0.1:3306";

		//数据库名字

		private String mysqlDatabase = "hqwx";

		 

		/*

		private String 
		url="jdbc:mysql://"+mysqlHost+"/"+"useUnicode=true&characterEncoding=GB2312";

		*/

		private String url ="";

		private Connection con = null;

		 

		Statement stmt;

		/**

		* 根据主机、数据库名称、数据库用户名、数据库用户密码取得连接。

		* @param mysqlHost String

		* @param mysqlDatabase String

		* @param user String

		* @param password String

		*/
		public DTO()
		{
			this.url = "jdbc:mysql://" + this.mysqlHost + "/" + this.mysqlDatabase ;
			
			 

			try {

			Class.forName("com.mysql.jdbc.Driver");

			}

			catch (ClassNotFoundException e) {

			System.err.println("class not found:" + e.getMessage());

			}

			 

			try {

			con = DriverManager.getConnection(this.url, this.mysqlUser, this.mysqlPassword);

			//连接类型为ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY

			stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

			}

			catch (SQLException a) {

			System.err.println("sql exception:" + a.getMessage());
			
			}	
		}
		public DTO(String mysqlHost, String mysqlDatabase, String user, String password) {

		 

		this.mysqlHost = mysqlHost;

		this.mysqlDatabase = mysqlDatabase;

		this.mysqlUser = user;

		this.mysqlPassword = password;

		//显示中文

		
		this.url = "jdbc:mysql://" + mysqlHost + "/" + mysqlDatabase ;

		 

		try {

		Class.forName("com.mysql.jdbc.Driver");

		}

		catch (ClassNotFoundException e) {

		System.err.println("class not found:" + e.getMessage());

		}

		 

		try {

		con = DriverManager.getConnection(this.url, this.mysqlUser, this.mysqlPassword);

		//连接类型为ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY

		stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

		}

		catch (SQLException a) {

		System.err.println("sql exception:" + a.getMessage());

		}

		}

		
		/**

		* 返回取得的连接

		*/
		public String getMysqlHost()
		{
			return this.mysqlHost;
		}
		public String getMysqlDatabase()
		{
			return this.mysqlDatabase;
		}
		public String getMysqlUser()
		{
			return this.mysqlUser;
		}
		
		public String getMysqlPassword()
		{
			return this.mysqlPassword;
		}
		public void setMysqlHost(String mysqlHost)
		{
			this.mysqlHost=mysqlHost;
			
		}
		public void setMysqlDatabass(String mysqlDatabase)
		{
			this.mysqlHost=mysqlDatabase;
			
		}
		public void setMysqlUser(String mysqlUser)
		{
			this.mysqlHost=mysqlUser;
			
		}
		public void setMysqlPassword(String mysqlPassword)
		{
			this.mysqlHost=mysqlPassword;
			
		}
		
		public Connection getCon() {

		return con;

		}

		/**

		* 执行一条简单的查询语句

		* 返回取得的结果集

		*/

		public ResultSet executeQuery(String sql) {

		ResultSet rs = null;

		try {

		rs = stmt.executeQuery(sql);

		}

		catch (SQLException e) {

		e.printStackTrace();

		}

		return rs;

		}

		/**

		* 执行一条简单的更新语句

		* 执行成功则返回true

		*/

		public boolean executeUpdate(String sql) {

			boolean v = false;

			try {

				v = stmt.executeUpdate(sql) > 0 ? true : false;

			}

			catch (SQLException e) {

				e.printStackTrace();

			}

		//	finally
		//	{

				return v;

		//	}

			}
		public void closeDTO ()
		{
			try {
				stmt.close();
				con.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
			
		}

		
}
