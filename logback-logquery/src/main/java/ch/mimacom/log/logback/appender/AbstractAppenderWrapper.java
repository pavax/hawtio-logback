package ch.mimacom.log.logback.appender;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

import java.util.List;

public abstract class AbstractAppenderWrapper<APPENDER extends Appender<E>, E> implements Appender<E> {

    private final APPENDER wrappedAppender;

    public AbstractAppenderWrapper(APPENDER wrappedAppender) {
        if (wrappedAppender == null) {
            throw new IllegalArgumentException("'delegateAppender' must not be null");
        }
        this.wrappedAppender = wrappedAppender;
    }

    protected APPENDER getWrappedAppender() {
        return wrappedAppender;
    }

    public String getName() {
        return wrappedAppender.getName();
    }

    public void doAppend(E event) throws LogbackException {
        wrappedAppender.doAppend(event);
    }

    public void setName(String name) {
        wrappedAppender.setName(name);
    }

    public void start() {
        wrappedAppender.start();
    }

    public void stop() {
        wrappedAppender.stop();
    }

    public boolean isStarted() {
        return wrappedAppender.isStarted();
    }


    public void setContext(Context context) {
        wrappedAppender.setContext(context);
    }

    public Context getContext() {
        return wrappedAppender.getContext();
    }

    public void addStatus(Status status) {
        wrappedAppender.addStatus(status);
    }

    public void addInfo(String msg) {
        wrappedAppender.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        wrappedAppender.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        wrappedAppender.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        wrappedAppender.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        wrappedAppender.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        wrappedAppender.addError(msg, ex);
    }

    @Override
    public void addFilter(Filter<E> newFilter) {
        wrappedAppender.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        wrappedAppender.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return wrappedAppender.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event) {
        return wrappedAppender.getFilterChainDecision(event);
    }
}
