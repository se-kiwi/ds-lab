package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.db.Item;
import com.kiwi.dslab.dto.db.OrderForm;
import com.kiwi.dslab.dto.db.OrderResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBTest {
//    static Connection connection = null;
//    static PreparedStatement preparedStatement = null;
//    public static final String DBPATH = "jdbc:mysql://202.120.40.8:30706/lab4";
//    public static final String USER = "root";
//    public static final String PASSWD = "root";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find JDBC driver!");
            e.printStackTrace();
            return;
        }

        MysqlDao dao = new MysqlDaoImpl();
//        OrderResponse response = dao.getResultById("1");
//        if (response == null) {
//            System.out.println("null object!");
//        } else {
//            response.print();
//        }
//
//        List<OrderResponse> rs = dao.getResultByUserId("2");
//        for (OrderResponse r : rs) {
//            r.print();
//        }
        OrderForm form = new OrderForm();
        form.setUser_id("2");
        form.setInitiator("RMB");
        form.setTime((long) 0);
        Item item1 = new Item();
        item1.setId("1");
        item1.setNumber("2");
        Item item2 = new Item();
        item2.setId("3");
        item2.setNumber("1");
        List<Item> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        form.setItems(items);
        System.out.println(dao.buyItem(form));

    }
//
//    private static void fetchOperation() throws SQLException {
//        preparedStatement = connection.prepareStatement("SELECT  * FROM visitor");
//        ResultSet rs = preparedStatement.executeQuery();  // need lock!!!
//
//        while (rs.next()) {
//            System.out.println(rs.getString("username"));
//        }
//    }
//
//    private static void addOperation() throws SQLException {
//        preparedStatement = connection.prepareStatement("INSERT visitor VALUES (?, ?, ?, ?)");
//        preparedStatement.setString(1, "51");
//        preparedStatement.setString(2, "1");
//        preparedStatement.setString(3, "1");
//        preparedStatement.setString(4, "1");
//
//        preparedStatement.executeUpdate();
//    }
}
