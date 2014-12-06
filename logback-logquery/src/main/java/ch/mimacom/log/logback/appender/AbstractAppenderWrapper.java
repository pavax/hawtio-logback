package ch.mimacom.log.logback.appender;

import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;

import java.util.List;

public abstract class AbstractAppenderWrapper<APPENDER extends Appender<E>, E> implements Appender<E> {

    private final APPENDER delegateAppender;

    public AbstractAppenderWrapper(APPENDER delegateAppender) {
        if (delegateAppender == null) {
            throw new IllegalArgumentException("'delegateAppender' must not be null");
        }
        this.delegateAppender = delegateAppender;
    }

    protected APPENDER getDelegateAppender() {
        return delegateAppender;
    }

    public String getName() {
        return delegateAppender.getName();
    }

    public void doAppend(E event) throws LogbackException {
        delegateAppender.doAppend(event);
    }

    public void setName(String name) {
        delegateAppender.setName(name);
    }

    public void start() {
        delegateAppender.start();
    }

    public void stop() {
        delegateAppender.stop();
    }

    public boolean isStarted() {
        return delegateAppender.isStarted();
    }


    public void setContext(Context context) {
        delegateAppender.setContext(context);
    }

    public Context getContext() {
        return delegateAppender.getContext();
    }

    public void addStatus(Status status) {
        delegateAppender.addStatus(status);
    }

    public void addInfo(String msg) {
        delegateAppender.addInfo(msg);
    }

    @Override
    public void addInfo(String msg, Throwable ex) {
        delegateAppender.addInfo(msg, ex);
    }

    @Override
    public void addWarn(String msg) {
        delegateAppender.addWarn(msg);
    }

    @Override
    public void addWarn(String msg, Throwable ex) {
        delegateAppender.addWarn(msg, ex);
    }

    @Override
    public void addError(String msg) {
        delegateAppender.addError(msg);
    }

    @Override
    public void addError(String msg, Throwable ex) {
        delegateAppender.addError(msg, ex);
    }

    @Override
    public void addFilter(Filter<E> newFilter) {
        delegateAppender.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        delegateAppender.clearAllFilters();
    }

    @Override
    public List<Filter<E>> getCopyOfAttachedFiltersList() {
        return delegateAppender.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(E event) {
        return delegateAppender.getFilterChainDecision(event);
    }
}
