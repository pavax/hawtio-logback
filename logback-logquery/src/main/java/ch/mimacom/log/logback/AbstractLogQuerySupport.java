package ch.mimacom.log.logback;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import io.fabric8.insight.log.LogEvent;
import io.fabric8.insight.log.LogFilter;
import io.fabric8.insight.log.LogResults;
import io.fabric8.insight.log.support.LogQuerySupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

abstract class AbstractLogQuerySupport extends LogQuerySupport {

    protected AbstractLogQuerySupport() {
        super();
    }

    @Override
    public LogResults getLogResults(int count) throws IOException {
        return filterLogResults(null, count);
    }

    @Override
    public LogResults queryLogResults(LogFilter filter) {
        Predicate<LogEvent> logEventPredicate = GuavaBasedLogFilterPredicates.buildPredicate(filter);
        int maxCount = -1;
        if (filter != null) {
            maxCount = filter.getCount();
        }
        return filterLogResults(logEventPredicate, maxCount);
    }

    protected abstract LogResults filterLogResults(Predicate<LogEvent> predicate, int count);

    public static class GuavaBasedLogFilterPredicates {

        public static Predicate<LogEvent> buildPredicate(LogFilter logFilter) {
            List<Predicate<LogEvent>> allPredicates = new ArrayList<>();
            if (logFilter.getAfterTimestamp() != null) {
                allPredicates.add(buildIsAfterPredicate(new Date(logFilter.getAfterTimestamp())));
            }
            if (logFilter.getBeforeTimestamp() != null) {
                allPredicates.add(buildIsBeforePredicate(new Date(logFilter.getBeforeTimestamp())));
            }
            if (logFilter.getLevels() != null) {
                allPredicates.add(buildLevelBasedPredicate(logFilter.getLevelsSet()));
            }
            if (logFilter.getMatchesText() != null) {
                allPredicates.add(buildTextMatcherPredicate(logFilter.getMatchesText()));
            }
            return Predicates.and(allPredicates);
        }

        private static Predicate<LogEvent> buildIsAfterPredicate(final Date date) {
            return new Predicate<LogEvent>() {
                public boolean apply(LogEvent logEvent) {
                    Date time = logEvent.getTimestamp();
                    return time != null && time.after(date);
                }
            };
        }

        private static Predicate<LogEvent> buildIsBeforePredicate(final Date date) {
            return new Predicate<LogEvent>() {
                public boolean apply(LogEvent logEvent) {
                    Date time = logEvent.getTimestamp();
                    return time != null && time.before(date);
                }
            };
        }

        private static Predicate<LogEvent> buildTextMatcherPredicate(final String queryTerm) {
            final String lcQueryTerm = queryTerm.toLowerCase();
            return new Predicate<LogEvent>() {
                public boolean apply(LogEvent logEvent) {
                    return logEvent.getMessage().toLowerCase().contains(lcQueryTerm) ||
                            logEvent.getClassName().toLowerCase().contains(lcQueryTerm) ||
                            logEvent.getMethodName().toLowerCase().contains(lcQueryTerm);
                }
            };
        }

        private static Predicate<LogEvent> buildLevelBasedPredicate(final Set<String> levels) {
            return new Predicate<LogEvent>() {
                public boolean apply(LogEvent logEvent) {
                    String level = logEvent.getLevel();
                    return level != null && levels.contains(level);
                }
            };
        }
    }
}
