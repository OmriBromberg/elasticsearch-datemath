# elasticsearch-datemath
This is a datemath library for Elasticsearch written in Java  
It supports the datemath expression standard of Elasticsearch ***5.5***

## Specification
https://www.elastic.co/guide/en/elasticsearch/client/net-api/current/date-math-expressions.html

# Example
```java
DateMathParser dateMathParser = new DateMathBuilder()
                .pattern("yyyy-MM-dd HH-ss")
                .zone(ZoneId.of("America/New_York"))
                .build();
dateMathParser.resolveExpression("1998-09-18 16-43||-4d");
```
# API
## Parser
### DateMathParser
#### ZonedDateTime resolveExpression(String Expression)
```java
// gets the current datetime and subtracts 5 days
dateMathParser.resolveExpression("now-5d");
    
// gets the current datetime and substracts 6 months, adds 6 years and rounds the month
dateMathParser.resolveExpression("now-6M+6Y/M");
    
// gets the datetime of "2017-07-18" and adds 5 minutes
dateMathParser.resolveExpression("2017-07-18||+5m");
```
### DateMathBuilder
#### DateMathBuilder pattern(String pattern)
```java
// creates a new instance from the current with the new pattern
dateMathBuilder.pattern("yyyy-MM-dd");
```
#### DateMathBuilder zone(DateTimeZone zone)
```java
// creates a new instance from the current with the new zone
dateMathBuilder.zone(ZoneId.of("America/New_York"));
```
#### DateMathBuilder now(Supplier<ZonedDateTime> nowSupplier)
```java
// creates a new instance from the current with the new nowSupplier
dateMathBuilder.now(() -> ZonedDateTime.now(ZoneId.of("America/New_York")));
```
#### DateMathBuilder nowPattern(String nowPattern)
```java
// creates a new instance from the current with the new nowPattern
dateMathBuilder.nowPattern("now");
```
#### DateMathParser build()
```java
// creates a new DateMathParser instance and returns it
dateMathBuilder.build();
```