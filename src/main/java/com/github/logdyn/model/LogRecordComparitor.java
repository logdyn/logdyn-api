package com.github.logdyn.model;

import java.util.Comparator;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 02/06/2017.
 */
public class LogRecordComparitor implements Comparator<LogRecord>
{
    public static final LogRecordComparitor COMPARITOR = new LogRecordComparitor();

    private LogRecordComparitor()
    {
        super();
    }

    @Override
    public int compare(LogRecord o1, LogRecord o2)
    {
        if (o1 == o2)
        {
            return 0;
        }
        else if (null == o1)
        {
            return -1;
        }
        else if(null == o2)
        {
            return 1;
        }
        int result = Long.compare(o1.getMillis(), o2.getMillis());
        if (result == 0)
        {
            result = Integer.compare(o1.getLevel().intValue(), o2.getLevel().intValue());
            if (result == 0)
            {
                final String o1SessionId = o1 instanceof LogMessage ? ((LogMessage) o1).getSessionId() : null;
                final String o2SessionId = o2 instanceof LogMessage ? ((LogMessage) o2).getSessionId() : null;
                result = NullComparitor.NULL_FIRST.compare(o1SessionId, o2SessionId);
                if (result == 0)
                {
                    result = o1.getMessage().compareTo(o2.getMessage());
                }
            }
        }
        return result;
    }

    private static class NullComparitor<V extends Comparable<V>> implements Comparator<V>
    {
        public static final NullComparitor NULL_FIRST = new NullComparitor(true);

        public static final NullComparitor NULL_LAST = new NullComparitor(false);

        final boolean nullFirst;

        private NullComparitor(final boolean nullFirst)
        {
            this.nullFirst = nullFirst;
        }

        @Override
        public int compare(V o1, V o2) {
            if (o1 == null)
            {
                return (o2 == null) ? 0 : (this.nullFirst ? -1 : 1);
            }
            else if (o2 == null)
            {
                return this.nullFirst ? 1: -1;
            }
            else
            {
                return o1.compareTo(o2);
            }
        }
    }
}
