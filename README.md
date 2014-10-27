hawtio-logback
==============

Quick-And-Diry Approach in order to provivde LogQuery-Support for Logback

The Approach is mainly based on the insight-log4j project (see: https://github.com/fabric8io/fabric8/tree/master/insight/insight-log4j)


Howto Integrate
==============

Spring-Config

@Configuration
public class LogbackLogQueryConfig {
    @Bean
    public LogbackAwareLogQueryMBeanImpl createLogbackAwareLogQueryMBeanImpl() {
        return new LogbackAwareLogQueryMBeanImpl(1000);
    }
}
