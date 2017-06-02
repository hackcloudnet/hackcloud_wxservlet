package com.hackcloud.xjzwx;
import java.sql.*;



public class WxDTO {
	private DTO dbm;
	public  WxDTO()
	{
	dbm = new DTO();	
		
	}
	public WxDTO(String mysqlHost, String mysqlDatabase, String user, String password)
	{
		dbm = new DTO(mysqlHost,mysqlDatabase,user,password) ;
	}
	public Connection wxGetconn()
	{
		return dbm.getCon();
	}
	public ResultSet wxExecuteQuery(String sql) {
		return dbm.executeQuery(sql);
	}
	public boolean wxExecuteUpdate(String sql){
		return dbm.executeUpdate(sql);
	}
	public void closeWxDTO()
	{
		dbm.closeDTO();
	}
}
