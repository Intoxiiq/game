package soufix.other;

public class SecureParser {

    public final static Interval INT_POSITIVE = new Interval(0, Integer.MAX_VALUE);
    public final static Interval INT_NEGATIVE = new Interval(Integer.MIN_VALUE, 0);
    public final static Interval PERCENTAGE = new Interval(0, 100);
    public final static Interval ITEM_QUANTITY = new Interval(0, 65535);

    public static class Interval {

        public Interval(long MIN_VALUE, long MAX_VALUE) {
            this(MIN_VALUE, MAX_VALUE, 0);
        }

        public Interval(long MIN_VALUE, long MAX_VALUE, long DEFAULT_VALUE) {
            this.MIN_VALUE = MIN_VALUE;
            this.MAX_VALUE = MAX_VALUE;
            this.DEFAULT_VALUE = DEFAULT_VALUE;
        }
        public final long DEFAULT_VALUE;
        public final long MIN_VALUE;
        public final long MAX_VALUE;
    }

    public static int Integer(String value, Interval interval) {
        try {
            int intVal = Integer.parseInt(value);
            if (intVal < interval.MIN_VALUE) {
                return (int) interval.MIN_VALUE;
            } else if (intVal > interval.MAX_VALUE) {
                return (int) interval.MAX_VALUE;
            } else {
                return intVal;
            }
        } catch (Throwable e) {
            return (int) interval.DEFAULT_VALUE;
        }
    }
    public static int Integer(String value) {
        return Integer(value, INT);
    }
    public final static Interval INT = new Interval(Integer.MIN_VALUE, Integer.MAX_VALUE);

    public static int PositiveInteger(String value) {
        return Integer(value, INT_POSITIVE);
    }

    public static int ItemQuantity(String value) {
        return Integer(value, ITEM_QUANTITY);
    }

    public static int Percentage(String value) {
        return Integer(value, PERCENTAGE);
    }
}

