# Distributed Transaction Settlement System

This project use a variety of the latest distributed computing technologies to build and implement a comprehensive system on cloud server, including **Kubernetes**, **Spark Streaming**, **Kafka**, **MySQL**, and **Zookeeper**.

## Application Scenario

Suppose that there is a hot international shopping platform, which always needs to handle high concurrency purchase orders. Since it is designed for users around the world, it should be able to support different currency settlement. When a user buys a commodity, the system will exchange the original price to the price of target currency, according to the current exchange rate. We design and implement the distributed transaction settlement system, receiving and processing trade orders, and recording all the transaction results and the total transaction amount.

## Application Details

### Receiveing Order 

The distributed transaction settlement system receives trade order in real time with JSON format as follows:

```json
{
    "user_id": "123456",
    "initiator": "CNY",
    "time": 1558868400000,
    "items": [
        {"id": "1", "number": "2"},
        {"id": "3", "number": "1"} 
    ]
}
```

### Processing Order

After receiving an order, the system will access the detail information of commodities contained in the order and exchange rate to calculate the transaction result. The detail information of commodity is stored in table `commodity` in MySQL, adn the details of exchange rate is stored in Zookeeper.

### Processing Result

The transaction reslut will be stored in result table in MySQL. The result can be queried by a later request. The total transaction amount is managed by Zookeeper. The system provides an interface to access the total transaction amount in real time.
