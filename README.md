hawtio-logback
==============

Quick-And-Diry approach in order to provivde 'LogQuery'-Support for Logback.

The Approach is mainly based on the insight-log4j project (see: https://github.com/fabric8io/fabric8/tree/master/insight/insight-log4j)

<h1>Why</h1>
We simply wanted to access our most-recent (Logback-Based) Logs from within <a href="http://hawt.io" >'Hawtio'</a>

<h1>Howto Integrate</h1>
<h2>Spring-Config</h2>

```java
@Configuration
public class LogbackLogQueryConfig {
    @Bean
    public LogbackAwareLogQueryMBeanImpl createLogbackAwareLogQueryMBeanImpl() {
        return new LogbackAwareLogQueryMBeanImpl(1000);
    }
}
```
