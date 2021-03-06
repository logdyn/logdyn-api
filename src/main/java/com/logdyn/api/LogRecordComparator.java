package com.logdyn.api;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Created by Matt on 02/06/2017.
 */
public class LogRecordComparator implements Comparator<LogRecord>
{
    public static final LogRecordComparator COMPARATOR = new LogRecordComparator();

    private LogRecordComparator()
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
            result = LevelComparator.compareTo(o1.getLevel(), o2.getLevel());
            if (result == 0)
            {
                final String o1Username = o1 instanceof LogMessage ? ((LogMessage) o1).getUsername() : null;
                final String o2Username = o2 instanceof LogMessage ? ((LogMessage) o2).getUsername() : null;
                result = NullComparator.compareTo(o1Username, o2Username);
                if (result == 0)
                {
                    final String o1SessionId = o1 instanceof LogMessage ? ((LogMessage) o1).getSessionId() : null;
                    final String o2SessionId = o2 instanceof LogMessage ? ((LogMessage) o2).getSessionId() : null;
                    result = NullComparator.compareTo(o1SessionId, o2SessionId);
                    if (result == 0)
                    {
                        result = o1.getMessage().compareTo(o2.getMessage());
                    }
                }
            }
        }
        return result;
    }

    public static int compareTo(LogRecord o1, LogRecord o2)
    {
        return LogRecordComparator.COMPARATOR.compare(o1, o2);
    }

    private static class LevelComparator implements Comparator<Level>
    {
        public static final LevelComparator COMPARATOR = new LevelComparator();

        private LevelComparator()
        {
            super();
        }

        public static int compareTo(Level o1, Level o2)
        {
            return COMPARATOR.compare(o1, o2);
        }

        @Override
        public int compare(Level o1, Level o2)
        {
            return Integer.compare(o1.intValue(), o2.intValue());
        }
    }

    private static class NullComparator implements Comparator<String>
    {
        public static final NullComparator NULL_FIRST = new NullComparator();

        private NullComparator()
        {
            super();
        }

        public static int compareTo(String o1, String o2)
        {
            return NULL_FIRST.compare(o1, o2);
        }

        @Override
        public int compare(String o1, String o2) {
            if (o1 == null)
            {
                return (o2 == null) ? 0 : -1 ;
            }
            else if (o2 == null)
            {
                return 1;
            }
            else
            {
                return o1.compareTo(o2);
            }
        }
    }
}
