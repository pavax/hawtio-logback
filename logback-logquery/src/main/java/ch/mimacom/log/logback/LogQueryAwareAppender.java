package ch.mimacom.log.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

import java.util.List;

public interface LogQueryAwareAppender extends Appender<ILoggingEvent> {
    List<ILoggingEvent> getAllEvents();

    int getLength();
}
