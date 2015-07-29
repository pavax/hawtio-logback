package ch.mimacom.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextListener;
import ch.qos.logback.core.Appender;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Ordering;
import io.fabric8.insight.log.LogEvent;
import io.fabric8.insight.log.LogResults;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;

public class LogbackLogQuery extends AbstractLogQuerySupport implements LoggerContextListener {

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
    @PostConstruct
    public void start() {
        super.start();
        attachAppender();
        attachLoggerContextListener();
    }


    @Override
    @PreDestroy
    public void stop() {
        super.stop();
        removeAppender();
    }

    private void removeAppender() {
        LoggerContext loggerContext = getLoggerContext();
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
        LoggerContext loggerContext = getLoggerContext();
        Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger.getAppender(APPENDER_NAME) != null) {
            LOGGER.warn("An Appender named '" + APPENDER_NAME + "' is already registered");
        } else {
            logQueryAwareAppender.setName(APPENDER_NAME);
            logQueryAwareAppender.start();
            logger.addAppender(logQueryAwareAppender);
        }
    }

    private void attachLoggerContextListener() {
        LoggerContext loggerContext = getLoggerContext();
        loggerContext.addListener(this);
    }

    @Override
    protected LogResults filterLogResults(Predicate<LogEvent> predicate, int count) {
        final List<ILoggingEvent> allEvents = logQueryAwareAppender.getAllEvents();

        final List<LogEvent> allLogEvents = LogEventAssembler.toLogEvents(allEvents, getHostName());

        final List<LogEvent> filteredLogEvents = filterLogEvents(predicate, allLogEvents, count);

        return toLogResult(filteredLogEvents);
    }

    private List<LogEvent> filterLogEvents(Predicate<LogEvent> predicate, List<LogEvent> allLogEvents, int count) {
        FluentIterable<LogEvent> fluentIterable = FluentIterable.from(allLogEvents);
        if (predicate != null) {
            fluentIterable = fluentIterable.filter(predicate);
        }
        if (count > -1) {
            fluentIterable = fluentIterable.limit(count);
        }
        return fluentIterable.toList();
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    private LogResults toLogResult(List<LogEvent> logEvents) {
        final LogResults logResults = new LogResults();
        if (logEvents.size() > 0) {
            final LogEvent minLogEvent = LOG_EVENT_TIMESTAMP_ORDERING.min(logEvents);
            final LogEvent maxLogEvent = LOG_EVENT_TIMESTAMP_ORDERING.max(logEvents);

            logResults.setEvents(logEvents);
            logResults.setFromTimestamp(minLogEvent.getTimestamp().getTime());
            logResults.setToTimestamp(maxLogEvent.getTimestamp().getTime());
            return logResults;
        }
        logResults.setEvents(Collections.<LogEvent>emptyList());
        return logResults;
    }

    @Override
    public boolean isResetResistant() {
        return true;
    }

    @Override
    public void onStart(LoggerContext loggerContext) {
    }

    @Override
    public void onReset(LoggerContext loggerContext) {
        attachAppender();
    }

    @Override
    public void onStop(LoggerContext loggerContext) {
        removeAppender();
    }

    @Override
    public void onLevelChange(Logger logger, Level level) {

    }
}
