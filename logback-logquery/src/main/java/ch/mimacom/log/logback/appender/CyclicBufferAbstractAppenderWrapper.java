package ch.mimacom.log.logback.appender;

import ch.mimacom.log.logback.LogQueryAwareAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.read.CyclicBufferAppender;

import java.util.ArrayList;
import java.util.List;

public class CyclicBufferAbstractAppenderWrapper extends AbstractAppenderWrapper<CyclicBufferAppender<ILoggingEvent>, ILoggingEvent> implements LogQueryAwareAppender {

    public CyclicBufferAbstractAppenderWrapper(CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender) {
        super(cyclicBufferAppender);
    }

    public CyclicBufferAbstractAppenderWrapper(CyclicBufferAppender<ILoggingEvent> cyclicBufferAppender, int i) {
        this(cyclicBufferAppender);
        cyclicBufferAppender.setMaxSize(i);
    }

    @Override
    public void doAppend(ILoggingEvent event) throws LogbackException {
        super.doAppend(event);
        // This will make sure the caller-data is initialized in-memory for later usage
        event.getCallerData();
    }

    @Override
    public List<ILoggingEvent> getAllEvents() {
        CyclicBufferAppender<ILoggingEvent> appender = super.getDelegateAppender();
        int length = appender.getLength();
        List<ILoggingEvent> resultList = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            resultList.add(appender.get(i));
        }
        return resultList;
    }

    @Override
    public int getLength() {
        CyclicBufferAppender<ILoggingEvent> appender = super.getDelegateAppender();
        return appender.getLength();
    }
}
