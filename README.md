hawtio-logback
==============

Quick-And-Dirty approach in order to provivde 'LogQuery'-Support (see: https://github.com/fabric8io/fabric8/tree/master/insight/insight-log-core) for Logback.

The Approach is mainly influenced on the insight-log4j project (see: https://github.com/fabric8io/fabric8/tree/master/insight/insight-log4j)

<h1>Why</h1>
We simply wanted to access our most-recent (Logback-Based) Logs from within <a href="http://hawt.io" >'Hawtio'</a>

<h1>Howto Integrate</h1>
<h2>Spring-Annotation-Config</h2>

```java
@Configuration
public class LogbackLogQueryConfig {
    @Bean
    public LogbackAwareLogQueryMBeanImpl createLogbackAwareLogQueryMBeanImpl() {
        return new LogbackAwareLogQueryMBeanImpl(1000);
    }
}
```
<h2>Spring-XML-Config</h2>
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean class="ch.mimacom.log.logback.LogbackAwareLogQueryMBeanImpl"
          init-method="start"
          destroy-method="stop">
        <constructor-arg name="maxLogsBufferSize" value="1000"/>
    </bean>
</beans>
```

