package ch.mimacom.log.logback;

import ch.mimacom.log.logback.appender.CyclicBufferAppenderWrapper;
import ch.mimacom.log.logback.appender.LevelBasedCyclicBufferAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.fabric8.insight.log.LogEvent;
import io.fabric8.insight.log.LogFilter;
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
        createLogStatement(20, Level.DEBUG);
        createLogStatement(20, Level.INFO);
        createLogStatement(20, Level.WARN);
        createLogStatement(20, Level.ERROR);
        LogResults logResults = logQuerySupport.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(40, logResults.getEvents().size());
        Assert.assertEquals(10, Lists.newArrayList(findLogEventsByLevel(logResults, Level.DEBUG.toString())).size());
        Assert.assertEquals(10, Lists.newArrayList(findLogEventsByLevel(logResults, Level.INFO.toString())).size());
        Assert.assertEquals(10, Lists.newArrayList(findLogEventsByLevel(logResults, Level.WARN.toString())).size());
        Assert.assertEquals(10, Lists.newArrayList(findLogEventsByLevel(logResults, Level.ERROR.toString())).size());
    }

    @Test
    public void testIndividualLevelBasedCyclicBufferAppender() throws Exception {
        final int maxDebugLogs = 5;
        final int maxInfoLogs = 6;
        final int maxWarnLogs = 7;
        final int maxErrorLogs = 8;
        int maxTotalLogs = maxDebugLogs + maxInfoLogs + maxWarnLogs + maxErrorLogs;
        logQuerySupport = new LogbackLogQuery(
                new LevelBasedCyclicBufferAppender(
                        ImmutableMap.<String, Integer>builder()
                                .put("TRACE", 5)
                                .put("DEBUG", maxDebugLogs)
                                .put("INFO", maxInfoLogs)
                                .put("WARN", maxWarnLogs)
                                .put("ERROR", maxErrorLogs)
                                .build())
        );
        logQuerySupport.start();
        createLogStatement(20, Level.DEBUG);
        createLogStatement(20, Level.INFO);
        createLogStatement(20, Level.WARN);
        createLogStatement(20, Level.ERROR);
        LogResults logResults = logQuerySupport.getLogResults(ALL_LOG_RESULTS);
        Assert.assertEquals(maxTotalLogs, logResults.getEvents().size());
        Assert.assertEquals(maxDebugLogs, Lists.newArrayList(findLogEventsByLevel(logResults, Level.DEBUG.toString())).size());
        Assert.assertEquals(maxInfoLogs, Lists.newArrayList(findLogEventsByLevel(logResults, Level.INFO.toString())).size());
        Assert.assertEquals(maxWarnLogs, Lists.newArrayList(findLogEventsByLevel(logResults, Level.WARN.toString())).size());
        Assert.assertEquals(maxErrorLogs, Lists.newArrayList(findLogEventsByLevel(logResults, Level.ERROR.toString())).size());
    }

    @Test
    public void testCyclicBufferAppender() throws Exception {
        logQuerySupport = new LogbackLogQuery(
                new CyclicBufferAppenderWrapper(new CyclicBufferAppender<ILoggingEvent>(), 10)
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

    @Test
    public void testFilterEventByLevels() throws Exception {
        logQuerySupport = new LogbackLogQuery(
                new CyclicBufferAppenderWrapper(
                        new CyclicBufferAppender<ILoggingEvent>(), 100)
        );
        logQuerySupport.start();
        createLogStatement(8, Level.DEBUG);
        createLogStatement(9, Level.WARN);
        LogFilter filter = new LogFilter();
        filter.setCount(999);
        filter.setLevels(new String[]{Level.ERROR.toString(), Level.DEBUG.toString()});
        LogResults queryLogResults = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(8, queryLogResults.getEvents().size());
    }

    @Test
    public void testFilterEventByTextMatch() throws Exception {
        logQuerySupport = new LogbackLogQuery(
                new CyclicBufferAppenderWrapper(
                        new CyclicBufferAppender<ILoggingEvent>(), 100)
        );
        logQuerySupport.start();
        createLogStatement(8, Level.DEBUG);
        createLogStatement(9, Level.WARN);
        LogFilter filter = new LogFilter();
        filter.setCount(999);

        filter.setMatchesText("Debug-TEST");
        LogResults queryLogResults = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(8, queryLogResults.getEvents().size());

        filter.setMatchesText("-TEST");
        LogResults queryLogResults2 = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(8+9, queryLogResults2.getEvents().size());

        filter.setMatchesText("Debug-XXX");
        LogResults queryLogResults3 = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(0, queryLogResults3.getEvents().size());
    }

    @Test
    public void testFilterDate() throws Exception {
        logQuerySupport = new LogbackLogQuery(
                new CyclicBufferAppenderWrapper(
                        new CyclicBufferAppender<ILoggingEvent>(), 100)
        );
        logQuerySupport.start();

        long currentTime1 = System.currentTimeMillis();
        Thread.sleep(1);
        createLogStatement(8, Level.DEBUG);
        Thread.sleep(1);
        long currentTime2 = System.currentTimeMillis();
        Thread.sleep(1);
        createLogStatement(5, Level.WARN);
        Thread.sleep(1);
        long currentTime3 = System.currentTimeMillis();

        LogFilter filter = new LogFilter();
        filter.setCount(999);

        filter.setAfterTimestamp(currentTime1);
        LogResults queryLogResults = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(8+5, queryLogResults.getEvents().size());

        filter.setAfterTimestamp(currentTime2);
        LogResults queryLogResults2 = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(5, queryLogResults2.getEvents().size());

        filter.setAfterTimestamp(currentTime3);
        LogResults queryLogResults3 = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(0, queryLogResults3.getEvents().size());

        filter.setAfterTimestamp(currentTime1);
        filter.setBeforeTimestamp(currentTime2);
        LogResults queryLogResults4 = logQuerySupport.queryLogResults(filter);
        Assert.assertEquals(8, queryLogResults4.getEvents().size());
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
            } else if (level.equals(Level.TRACE)) {
                LOGGER.trace(msg);
            }
        }
    }

    private Iterable<LogEvent> findLogEventsByLevel(LogResults logResults, final String level) {
        return Iterables.filter(logResults.getEvents(), new Predicate<LogEvent>() {
            @Override
            public boolean apply(LogEvent input) {
                return input.getLevel().equals(level);
            }
        });
    }
}