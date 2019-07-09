package com.kiwi.dslab.db;

public class DBTest {
    public static void main(String[] args) {
        MysqlDao dao = new MysqlDaoImpl();
//        ResultResponse response = dao.getResultById("1");
//        if (response == null) {
//            System.out.println("null object!");
//        } else {
//            response.print();
//        }
//
//        List<ResultResponse> rs = dao.getResultByUserId("2");
//        for (ResultResponse r : rs) {
//            r.print();
//        }
//
//        OrderForm form = new OrderForm();
//        form.setUser_id("2");
//        form.setInitiator("RMB");
//        form.setTime((long) 0);
//        Item item1 = new Item();
//        item1.setId("1");
//        item1.setNumber("2");
//        Item item2 = new Item();
//        item2.setId("3");
//        item2.setNumber("1");
//        List<Item> items = new ArrayList<>();
//        items.add(item1);
//        items.add(item2);
//        form.setItems(items);
//        OrderResponse response = dao.buyItem(form);
//        System.out.println(response);

//        dao.storeResult("1", "CNY", true, 33.5);
        dao.initCommodity();
    }

}
