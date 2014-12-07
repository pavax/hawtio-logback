package ch.mimacom.log.logback;

import ch.mimacom.log.logback.appender.CyclicBufferAppenderWrapper;
import ch.mimacom.log.logback.appender.LevelBasedCyclicBufferAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;
import io.fabric8.insight.log.LogEvent;
import io.fabric8.insight.log.LogResults;
import io.fabric8.insight.log.support.LogQuerySupport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogbackLogQueryTest {

    private static final int ALL_LOG_RESULTS = -1;

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbackLogQueryTest.class);

    private LogQuerySupport logQuerySupport;

    @After
    public void tearDown() throws Exception {
        logQuerySupport.stop();
    }

    @Test
    public void testLevelBasedCyclicBufferAppender() throws Exception {
        logQuerySupport = new LogbackLogQuery(
                new LevelBasedCyclicBufferAppender(10)
        );
        logQuerySupport.start();
        createLogStatement(10, Level.DEBUG);
        createLogStatement(10, Level.INFO);
        createLogStatement(10, Level.ERROR);
        LogResults logResults = logQuerySupport.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(30, logResults.getEvents().size());
        createLogStatement(10, Level.ERROR);
        LogResults logResults2 = logQuerySupport.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(30, logResults2.getEvents().size());
    }

    @Test
    public void testCyclicBufferAppender() throws Exception {
        logQuerySupport = new LogbackLogQuery(
                new CyclicBufferAppenderWrapper(
                        new CyclicBufferAppender<ILoggingEvent>(), 10)
        );
        logQuerySupport.start();
        createLogStatement(10, Level.INFO);
        LogResults logResults = logQuerySupport.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(10, logResults.getEvents().size());
        LogEvent logEvent = logResults.getEvents().get(0);
        Assert.assertEquals("LOG-INFO-TEST: 0", logEvent.getMessage());
        Assert.assertNotNull(logEvent.getClassName());
        createLogStatement(10, Level.DEBUG);
        LogResults logResults2 = logQuerySupport.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(10, logResults2.getEvents().size());

    }

    private void createLogStatement(int counts, Level level) {
        for (int i = 0; i < counts; i++) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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