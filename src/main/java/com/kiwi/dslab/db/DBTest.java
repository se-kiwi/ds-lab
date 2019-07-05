package com.kiwi.dslab.db;

import java.sql.*;

public class DBTest {
    static Connection connection = null;
    static PreparedStatement preparedStatement = null;
    public static final String DBPATH = "jdbc:mysql://202.120.40.8:30706/lab4";
    public static final String USER = "root";
    public static final String PASSWD = "root";

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find JDBC driver!");
            e.printStackTrace();
            return;
        }

        connection = DriverManager.getConnection(DBPATH, USER, PASSWD);

        fetchOperation();
        addOperation();

        preparedStatement.close();
        connection.close();
    }

    private static void fetchOperation() throws SQLException {
        preparedStatement = connection.prepareStatement("SELECT  * FROM visitor");
        ResultSet rs = preparedStatement.executeQuery();  // need lock!!!

        while (rs.next()) {
            System.out.println(rs.getString("username"));
        }
    }

    private static void addOperation() throws SQLException {
        preparedStatement = connection.prepareStatement("INSERT visitor VALUES (?, ?, ?, ?)");
        preparedStatement.setString(1, "51");
        preparedStatement.setString(2, "1");
        preparedStatement.setString(3, "1");
        preparedStatement.setString(4, "1");

        preparedStatement.executeUpdate();
    }
}
