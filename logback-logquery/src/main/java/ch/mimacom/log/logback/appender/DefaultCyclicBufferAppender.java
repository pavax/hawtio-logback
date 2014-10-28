package ch.mimacom.log.logback.appender;

import ch.mimacom.log.logback.LogQueryAwareAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.CyclicBufferAppender;

import java.util.ArrayList;
import java.util.List;

public class DefaultCyclicBufferAppender extends CyclicBufferAppender<ILoggingEvent> implements LogQueryAwareAppender {

    public static final int DEFAULT_MAX_BUFFER_SIZE = 512;

    public DefaultCyclicBufferAppender() {
        this(DEFAULT_MAX_BUFFER_SIZE);
    }

    public DefaultCyclicBufferAppender(int maxBufferSize) {
        super();
        setMaxSize(maxBufferSize);
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        super.append(eventObject);
        // This will make sure the caller-data is initialized in-memory for later usage
        eventObject.getCallerData();
    }

    @Override
    public List<ILoggingEvent> getAllEvents() {
        int length = super.getLength();
        List<ILoggingEvent> resultList = new ArrayList<ILoggingEvent>(length);
        for (int i = 0; i < length; i++) {
            resultList.add(super.get(i));
        }
        return resultList;
    }
}
