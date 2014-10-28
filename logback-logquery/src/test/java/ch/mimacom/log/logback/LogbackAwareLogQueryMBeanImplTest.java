package ch.mimacom.log.logback;

import ch.mimacom.log.logback.appender.DefaultCyclicBufferAppender;
import ch.mimacom.log.logback.appender.LevelBasedCyclicBufferAppender;
import ch.qos.logback.classic.Level;
import io.fabric8.insight.log.LogEvent;
import io.fabric8.insight.log.LogResults;
import io.fabric8.insight.log.support.LogQuerySupport;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogbackAwareLogQueryMBeanImplTest {

    private static final int ALL_LOG_RESULTS = -1;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackAwareLogQueryMBeanImplTest.class);

    @Test
    public void testGetEventsUsingDefaultCyclicBufferAppender() throws Exception {
        LogQuerySupport logbackAwareLogQueryMBean = new LogbackAwareLogQueryMBeanImpl(
                new DefaultCyclicBufferAppender(10)
        );
        logbackAwareLogQueryMBean.start();
        createLogStatement(10, Level.INFO);
        createLogStatement(10, Level.ERROR);
        createLogStatement(10, Level.DEBUG);
        LogResults logResults = logbackAwareLogQueryMBean.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(10, logResults.getEvents().size());
    }

    @Test
    public void testGetEventsUsingLevelBasedCyclicBufferAppender() throws Exception {
        LogQuerySupport logbackAwareLogQueryMBean = new LogbackAwareLogQueryMBeanImpl(
                new LevelBasedCyclicBufferAppender(100)
        );
        logbackAwareLogQueryMBean.start();
        createLogStatement(10, Level.INFO);
        createLogStatement(10, Level.ERROR);
        createLogStatement(10, Level.DEBUG);
        LogResults logResults = logbackAwareLogQueryMBean.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(30, logResults.getEvents().size());
    }

    @Test
    public void testEventData() throws Exception {
        LogQuerySupport logbackAwareLogQueryMBean = new LogbackAwareLogQueryMBeanImpl(
                new DefaultCyclicBufferAppender(10)
        );
        logbackAwareLogQueryMBean.start();
        createLogStatement(1, Level.INFO);
        LogResults logResults = logbackAwareLogQueryMBean.getLogResults(1);
        Assert.assertEquals(1, logResults.getEvents().size());
        LogEvent logEvent = logResults.getEvents().get(0);
        Assert.assertEquals("LOG-INFO-TEST: 0", logEvent.getMessage());
    }

    private void createLogStatement(int counts, Level level) {
        for (int i = 0; i < counts; i++) {
            String msg = "LOG-" + level.toString() + "-TEST: " + i;
            if (level.equals(Level.INFO)) {
                LOGGER.info(msg);
            } else if (level.equals(Level.DEBUG)) {
                LOGGER.debug(msg);
            } else if (level.equals(Level.WARN)) {
                LOGGER.warn(msg);
            } else if (level.equals(Level.ERROR)) {
                LOGGER.error(msg);
            }
        }
    }
}