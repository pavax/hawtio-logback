hawtio-logback
==============

Quick-And-Dirty approach in order to provide 'LogQuery'-Support (see: https://github.com/fabric8io/fabric8/tree/master/insight/insight-log-core) for Logback.

The Approach is mainly influenced by the insight-log4j project (see: https://github.com/fabric8io/fabric8/tree/master/insight/insight-log4j)

<h1>Why</h1>
In our project we simply wanted to access our most-recent (Logback-Based) log outputs from within the <a href="http://hawt.io" >'Hawtio'</a> app

<h1>Open Items</h1>
* Add Maven Coordinates Support 
* Add Better Predicate Filtering Support
* CallerData is not always determined correclty

<h1>Howto Integrate</h1>

<h2>DefaultCyclicBufferAppender</h2>
The DefaultCyclicBufferAppender keeps N-Log-Events in-memory.

<h3>Spring-Annotation-Config</h3>

```java
@Configuration
public class LogbackLogQueryConfig {
    @Bean
    public LogbackAwareLogQueryMBeanImpl createLogbackAwareLogQueryMBeanImpl() {
        return new LogbackAwareLogQueryMBeanImpl(new DefaultCyclicBufferAppender(10));
    }
}
```
<h3>Spring-XML-Config</h3>
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean class="ch.mimacom.log.logback.LogbackAwareLogQueryMBeanImpl"
          init-method="start"
          destroy-method="stop">
        <constructor-arg name="logQueryAwareAppender">
            <bean class="ch.mimacom.log.logback.appender.DefaultCyclicBufferAppender">
                <constructor-arg name="maxBufferSize" value="1000"/>
            </bean>
        </constructor-arg>
    </bean>
</beans>
```

<h2>LevelBasedCyclicBufferAppender</h2>
Allows to define a buffer-size for each level. Thus N-Log-Events for every Level are keept in-memory
<h3>Spring-Annotation-Config</h3>
```java
@Configuration
public class LogbackLogQueryConfig {
  @Bean
    public LogbackAwareLogQueryMBeanImpl createLogbackAwareLogQueryMBeanImpl() {
        return new LogbackAwareLogQueryMBeanImpl(
                new LevelBasedCyclicBufferAppender(
                        ImmutableMap.<String, Integer>builder()
                                .put("TRACE", 5)
                                .put("DEBUG", 5)
                                .put("INFO", 5)
                                .put("WARN", 10)
                                .put("ERROR", 10)
                                .build()
                )
        );
    }
}
```
