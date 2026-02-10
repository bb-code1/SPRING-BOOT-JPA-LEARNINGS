# Day 10: The N+1 SELECT Query Problem

## A. Concept
Occurs when you load N parent records, and accessing their lazy child associations triggers N additional queries.

## B. Example
List<Order> orders = orderRepository.findAll();
orders.forEach(o -> o.getItems().size());
