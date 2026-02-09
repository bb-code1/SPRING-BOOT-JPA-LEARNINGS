# Day 9: Proxy Objects & LazyInitializationException

## A. Concept
Lazy-loaded entities and collections are loaded using dynamic Proxy Objects (Byte Buddy subclasses).

## B. The Exception
If you access a lazy collection (e.g. order.getItems()) after the database session closes, the proxy fails to connect to the database and throws LazyInitializationException.
