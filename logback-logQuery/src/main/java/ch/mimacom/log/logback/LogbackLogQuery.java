package ch.mimacom.log.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import io.fabric8.insight.log.LogEvent;
import io.fabric8.insight.log.LogResults;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class LogbackLogQuery extends AbstractLogQuerySupport {

    private static final transient org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogbackLogQuery.class);

    protected static final String APPENDER_NAME = "LogQueryAppender";

    private final LogQueryAwareAppender logQueryAwareAppender;

    private final static Ordering<LogEvent> LOG_EVENT_TIMESTAMP_ORDERING = new Ordering<LogEvent>() {
        @Override
        public int compare(LogEvent left, LogEvent right) {
            return left.getTimestamp().compareTo(right.getTimestamp());
        }
    };

    public LogbackLogQuery(LogQueryAwareAppender logQueryAwareAppender) {
        super();
        if (logQueryAwareAppender == null) {
            throw new IllegalArgumentException("A 'logQueryAwareAppender' must be set");
        }
        this.logQueryAwareAppender = logQueryAwareAppender;
    }

    @Override
    public void start() {
        super.start();
        attachAppender();
    }

    @Override
    public void stop() {
        super.stop();
        removeAppender();
    }

    private void removeAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        Appender<ILoggingEvent> appender = logger.getAppender(APPENDER_NAME);
        if (appender == null) {
            LOGGER.warn("No Appender named '" + APPENDER_NAME + "' was found");
        } else {
            appender.stop();
            logger.detachAppender(APPENDER_NAME);
        }
    }

    private void attachAppender() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger.getAppender(APPENDER_NAME) != null) {
            LOGGER.warn("An Appender named '" + APPENDER_NAME + "' is already registered");
        } else {
            logQueryAwareAppender.setName(APPENDER_NAME);
            logQueryAwareAppender.start();
            logger.addAppender(logQueryAwareAppender);
        }
    }

    @Override
    protected LogResults filterLogResults(Predicate<LogEvent> predicate, int count) {
        final List<ILoggingEvent> allEvents = logQueryAwareAppender.getAllEvents();

        final List<LogEvent> allLogEvents = LogEventAssembler.toLogEvents(allEvents, getHostName());

        final List<LogEvent> filteredLogEvents = filterLogEvents(predicate, allLogEvents, count);

        return toLogResult(filteredLogEvents);
    }

    private List<LogEvent> filterLogEvents(Predicate<LogEvent> predicate, List<LogEvent> allLogEvents, int count) {
        final List<LogEvent> filteredLogEvents;
        if (predicate == null) {
            filteredLogEvents = allLogEvents;
        } else {
            filteredLogEvents = Lists.newArrayList(Iterables.filter(allLogEvents, predicate));
        }
        if (count > -1 && filteredLogEvents.size() > count) {
            return filteredLogEvents.subList(0, count);

        }
        return filteredLogEvents;

    }

    private LogResults toLogResult(List<LogEvent> logEventArrayList) {
        final LogResults logResults = new LogResults();
        if (logEventArrayList.size() > 0) {
            final LogEvent minLogEvent = LOG_EVENT_TIMESTAMP_ORDERING.min(logEventArrayList);
            final LogEvent maxLogEvent = LOG_EVENT_TIMESTAMP_ORDERING.max(logEventArrayList);

            logResults.setEvents(logEventArrayList);
            logResults.setFromTimestamp(minLogEvent.getTimestamp().getTime());
            logResults.setToTimestamp(maxLogEvent.getTimestamp().getTime());
            return logResults;
        }
        logResults.setEvents(Collections.<LogEvent>emptyList());
        return logResults;
    }

}
