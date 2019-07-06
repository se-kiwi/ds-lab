package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.db.Item;
import com.kiwi.dslab.dto.db.OrderForm;
import com.kiwi.dslab.dto.db.OrderResponse;
import com.kiwi.dslab.dto.db.ResultResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// TODO: acquire lock before access `commodity`
// TODO: update total transaction in zookeeper

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
    public OrderResponse buyItem(OrderForm order) {
        OrderResponse response = new OrderResponse(false, null, null);
        List<Double> prices = new ArrayList<>();
        List<String> currencies = new ArrayList<>();
        try {
            Connection connection = getConnection();
            for (Item item : order.getItems()) {
                PreparedStatement ps = connection.prepareStatement("SELECT * FROM commodity WHERE id = ?");
                ps.setInt(1, Integer.valueOf(item.getId()));
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    prices.add(rs.getDouble("price"));
                    currencies.add(rs.getString("currency"));
                    if (rs.getInt("inventory") < Integer.valueOf(item.getNumber())) {
                        connection.close();
                        return response;
                    }
                }
            }
            for (Item item : order.getItems()) {
                PreparedStatement ps = connection.prepareStatement("UPDATE commodity SET inventory = inventory - ? where id = ?");
                ps.setInt(1, Integer.valueOf(item.getNumber()));
                ps.setInt(2, Integer.valueOf(item.getId()));
                ps.executeUpdate();
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return response;
        }
        response.setSuccess(true);
        response.setCurrencies(currencies);
        response.setPrices(prices);
        return response;
    }

    @Override
    public boolean storeResult(String user_id, String initiator, boolean success, double paid) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement
                    = connection.prepareStatement("INSERT result (user_id, initiator, success, paid) VALUES (?, ?, ?, ?)");
            preparedStatement.setInt(1, Integer.valueOf(user_id));
            preparedStatement.setString(2, initiator);
            preparedStatement.setBoolean(3, success);
            preparedStatement.setDouble(4, paid);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
