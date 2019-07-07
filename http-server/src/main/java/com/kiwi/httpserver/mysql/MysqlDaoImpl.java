package com.kiwi.httpserver.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class MysqlDaoImpl implements MysqlDao {
    private static final String DBPATH = "jdbc:mysql://202.120.40.8:30706/dslab";
    private static final String USER = "root";
    private static final String PASSWD = "root";

    public MysqlDaoImpl() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find JDBC driver!");
            e.printStackTrace();
        }
    }

    @Override
    public ResultResponse getResultById(String id) {
        ResultResponse response = null;

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM result WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                response = new ResultResponse();
                response.setOrder_id(rs.getString("id"));
                response.setUser_id(rs.getString("user_id"));
                response.setInitiator(rs.getString("initiator"));
                response.setSuccess(rs.getBoolean("success"));
                response.setPaid(rs.getDouble("paid"));
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    @Override
    public List<ResultResponse> getResultByUserId(String userId) {
        List<ResultResponse> response = new ArrayList<>();

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM result WHERE user_id = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ResultResponse or = new ResultResponse();
                or.setOrder_id(rs.getString("id"));
                or.setUser_id(rs.getString("user_id"));
                or.setInitiator(rs.getString("initiator"));
                or.setSuccess(rs.getBoolean("success"));
                or.setPaid(rs.getDouble("paid"));
                response.add(or);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return response;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBPATH, USER, PASSWD);
    }

}
