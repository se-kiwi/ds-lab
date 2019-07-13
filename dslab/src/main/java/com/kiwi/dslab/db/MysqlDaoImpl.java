package com.kiwi.dslab.db;

import com.kiwi.dslab.dto.Item;
import com.kiwi.dslab.dto.OrderForm;
import com.kiwi.dslab.dto.OrderResponse;
import com.kiwi.dslab.dto.ResultResponse;
import com.kiwi.dslab.zk.DistributedLock;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.kiwi.dslab.ClusterConf.*;
import static com.kiwi.dslab.util.Utils.index2name;
import static com.kiwi.dslab.util.Utils.getRandInt;

public class MysqlDaoImpl implements MysqlDao {

    public MysqlDaoImpl() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find JDBC driver!");
            e.printStackTrace();
        }
    }

    @Override
    public OrderResponse buyItem(OrderForm order, ZooKeeper zooKeeper) {
        OrderResponse response = new OrderResponse(false, null, null);
        List<Double> prices = new ArrayList<>();
        List<String> currencies = new ArrayList<>();
        DistributedLock lock = new DistributedLock(zooKeeper);

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
//                        lock.unlock();
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
//            lock.unlock();
            return response;
        }

//        lock.unlock();

        response.setSuccess(true);
        response.setCurrencies(currencies);
        response.setPrices(prices);
        return response;
    }

    @Override
    public boolean storeResult(String order_id, String user_id, String initiator, boolean success, double paid) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement
                    = connection.prepareStatement("INSERT result (id, user_id, initiator, success, paid) VALUES (?, ?, ?, ?, ?)");
            preparedStatement.setString(1, order_id);
            preparedStatement.setInt(2, Integer.valueOf(user_id));
            preparedStatement.setString(3, initiator);
            preparedStatement.setBoolean(4, success);
            preparedStatement.setDouble(5, paid);
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean initCommodity() {
        try {
            Connection connection = getConnection();
            // drop table `commodity`
            PreparedStatement ps =
                    connection.prepareStatement("DROP TABLE commodity");
            ps.executeUpdate();

            // create new table `commodity`
            ps = connection.prepareStatement("create table commodity\n" +
                    "(\n" +
                    "    id int primary key auto_increment,\n" +
                    "    name varchar(18) not null,\n" +
                    "    price double not null,\n" +
                    "    currency varchar(8) not null,\n" +
                    "    inventory int not null\n" +
                    ")");
            ps.executeUpdate();

            for (int i = 0; i < 500; i++) {
                int randInt = getRandInt();
                String name = "item" + i;
                double price = randInt % 10000 / 100.0;
                String currency = index2name.get(randInt % 4);
                int inventory = getRandInt() % 500;

                ps = connection.prepareStatement("INSERT commodity (name, price, currency, inventory) VALUES (?, ?, ?, ?)");
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setString(3, currency);
                ps.setInt(4, inventory);
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
    public boolean initResult() {
        try {
            Connection connection = getConnection();
            // drop table `result`
            PreparedStatement ps =
                    connection.prepareStatement("DROP TABLE result");
            ps.executeUpdate();

            // create new table `result`
            ps = connection.prepareStatement("create table result\n" +
                    "(\n" +
                    "    id varchar(36) primary key,\n" +
                    "    user_id int not null,\n" +
                    "    initiator varchar(8) not null,\n" +
                    "    success bool not null,\n" +
                    "    paid double not null\n" +
                    ");");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL_PATH, MYSQL_USER, MYSQL_PASSWD);
    }

}
