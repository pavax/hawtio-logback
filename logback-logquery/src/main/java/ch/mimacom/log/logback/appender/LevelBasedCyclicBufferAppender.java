package ch.mimacom.log.logback.appender;

import ch.mimacom.log.logback.LogQueryAwareAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.helpers.CyclicBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LevelBasedCyclicBufferAppender extends AppenderBase<ILoggingEvent> implements LogQueryAwareAppender {

    private static final Logger LOGGER = LoggerFactory.getLogger(LevelBasedCyclicBufferAppender.class);

    public static final int DEFAULT_SIZE = 64;

    private int maxBufferSizePerLevel = DEFAULT_SIZE;

    private Map<Level, CyclicBuffer<ILoggingEvent>> bufferMap = null;

    public LevelBasedCyclicBufferAppender(int maxBufferSizePerLevel) {
        this.maxBufferSizePerLevel = maxBufferSizePerLevel;
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (!isStarted()) {
            return;
        }
        // This will make sure the caller-data is initialized in-memory for latter usage
        eventObject.getCallerData();
        CyclicBuffer<ILoggingEvent> cyclicBuffer = bufferMap.get(eventObject.getLevel());
        if (cyclicBuffer != null) {
            cyclicBuffer.add(eventObject);
        } else {
            LOGGER.warn("Unknown Log-Level {}", eventObject.getLevel());
        }
    }

    public void start() {
        bufferMap = new HashMap<Level, CyclicBuffer<ILoggingEvent>>(5);
        bufferMap.put(Level.TRACE, new CyclicBuffer<ILoggingEvent>(maxBufferSizePerLevel));
        bufferMap.put(Level.DEBUG, new CyclicBuffer<ILoggingEvent>(maxBufferSizePerLevel));
        bufferMap.put(Level.INFO, new CyclicBuffer<ILoggingEvent>(maxBufferSizePerLevel));
        bufferMap.put(Level.WARN, new CyclicBuffer<ILoggingEvent>(maxBufferSizePerLevel));
        bufferMap.put(Level.ERROR, new CyclicBuffer<ILoggingEvent>(maxBufferSizePerLevel));
        super.start();
    }

    public void stop() {
        bufferMap = null;
        super.stop();
    }

    @Override
    public int getLength() {
        if (isStarted()) {
            int counter = 0;
            Iterable<CyclicBuffer<ILoggingEvent>> cyclicBuffers = bufferMap.values();
            for (CyclicBuffer<ILoggingEvent> cyclicBuffer : cyclicBuffers) {
                counter += cyclicBuffer.length();
            }
            return counter;
        } else {
            return 0;
        }
    }

    @Override
    public List<ILoggingEvent> getAllEvents() {
        Iterable<CyclicBuffer<ILoggingEvent>> cyclicBuffers = bufferMap.values();
        List<ILoggingEvent> resultList = new ArrayList<ILoggingEvent>(getLength());
        for (CyclicBuffer<ILoggingEvent> cyclicBuffer : cyclicBuffers) {
            for (int i = 0; i < cyclicBuffer.length(); i++) {
                resultList.add(cyclicBuffer.get(i));
            }
        }
        sortByTimeStamp(resultList);
        return resultList;
    }

    public void setMaxBufferSizePerLevel(int maxBufferSizePerLevel) {
        this.maxBufferSizePerLevel = maxBufferSizePerLevel;
    }

    private static void sortByTimeStamp(List<ILoggingEvent> resultList) {
        Collections.sort(resultList, new Comparator<ILoggingEvent>() {
            @Override
            public int compare(ILoggingEvent eventOne, ILoggingEvent eventTwo) {
                return Long.compare(eventOne.getTimeStamp(), eventTwo.getTimeStamp());
            }
        });
    }
}
