package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.db.Item;
import com.kiwi.dslab.dto.db.OrderForm;
import com.kiwi.dslab.dto.db.OrderResponse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// TODO: acquire lock before access `commodity`
// TODO: update total transaction in zookeeper

public class MysqlDaoImpl implements MysqlDao {
    private static final String DBPATH = "jdbc:mysql://202.120.40.8:30706/dslab";
    private static final String USER = "root";
    private static final String PASSWD = "root";

    @Override
    public boolean buyItem(OrderForm order) {
        try {
            Connection connection = getConnection();
            for (Item item : order.getItems()) {
                PreparedStatement ps = connection.prepareStatement("SELECT inventory FROM commodity WHERE id = ?");
                ps.setInt(1, Integer.valueOf(item.getId()));
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt("inventory") < Integer.valueOf(item.getNumber())) {
                    connection.close();
                    return false;
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
            return false;
        }
        return true;
    }

    @Override
    public boolean storeResult(String id, String user_id, String initiator, boolean success, double paid) {
        try {
            Connection connection = getConnection();
            connection.prepareStatement("INSERT result values (id, user_id, initiator, success, paid) ")
                    .executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public OrderResponse getResultById(String id) {
        OrderResponse response = null;

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM result WHERE id = ?");
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                response = new OrderResponse();
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
    public List<OrderResponse> getResultByUserId(String userId) {
        List<OrderResponse> response = new ArrayList<>();

        try {
            Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM result WHERE user_id = ?");
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderResponse or = new OrderResponse();
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
