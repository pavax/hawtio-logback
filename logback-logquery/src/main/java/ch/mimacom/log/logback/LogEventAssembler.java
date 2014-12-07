package ch.mimacom.log.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import io.fabric8.insight.log.LogEvent;

import java.util.Date;
import java.util.List;

class LogEventAssembler {

    public static List<LogEvent> toLogEvents(List<ILoggingEvent> allEvents, final String hostName) {
        return Lists.transform(allEvents, new Function<ILoggingEvent, LogEvent>() {
            @Override
            public LogEvent apply(ILoggingEvent input) {
                return LogEventAssembler.toLogEvent(input, hostName);
            }
        });
    }

    public static LogEvent toLogEvent(ILoggingEvent element, String hostName) {
        LogEvent answer = new LogEvent();

        initExceptionData(answer, element);

        initCallerData(answer, element);

        initLogLevelData(answer, element);

        initMessage(answer, element);

        initMiscData(answer, element, hostName);

        return answer;
    }

    private static void initMiscData(LogEvent answer, ILoggingEvent element, String hostName) {
        answer.setLogger(element.getLoggerName());
        answer.setProperties(element.getMDCPropertyMap());
        answer.setSeq(element.getTimeStamp());
        answer.setTimestamp(new Date(element.getTimeStamp()));
        answer.setThread(element.getThreadName());
        answer.setHost(hostName);
    }

    private static void initMessage(LogEvent answer, ILoggingEvent element) {
        String message = element.getFormattedMessage();
        if (message != null) {
            answer.setMessage(message);
        }
    }

    private static void initLogLevelData(LogEvent answer, ILoggingEvent element) {
        Level level = element.getLevel();
        if (level != null) {
            answer.setLevel(level.toString());
        }
    }

    private static void initExceptionData(LogEvent answer, ILoggingEvent element) {
        answer.setException(extractExceptionInformation(element));
    }

    private static void initCallerData(LogEvent answer, ILoggingEvent loggingEvent) {
        StackTraceElement[] callerData = loggingEvent.getCallerData();
        if (callerData.length != 0) {
            // TODO figure out correct callerData index-position
            StackTraceElement stackTraceElement = callerData[0];
            answer.setClassName(stackTraceElement.getClassName());
            answer.setFileName(stackTraceElement.getFileName());
            answer.setClassName(stackTraceElement.getClassName());
            answer.setMethodName(stackTraceElement.getMethodName());
            answer.setLineNumber(Integer.toString(stackTraceElement.getLineNumber()));
        }
    }

    private static String[] extractExceptionInformation(ILoggingEvent loggingEvent) {
        IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
        if (throwableProxy != null && throwableProxy.getStackTraceElementProxyArray().length > 0) {
            StackTraceElementProxy[] stepArray = throwableProxy.getStackTraceElementProxyArray();
            String[] exceptionStringArray = new String[stepArray.length];
            for (int i = 0; i < stepArray.length; i++) {
                exceptionStringArray[i] = stepArray[i].toString();
            }
            return exceptionStringArray;

        }
        return null;
    }
}
