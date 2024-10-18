package soufix.game;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

@SuppressWarnings("hiding")
public class RandomCollection<Integer> {
    private final NavigableMap<Double, Integer> map = new TreeMap<Double, Integer>();
    private final Random random;
    private double total = 0;

    public RandomCollection() {
        this(new Random());
    }

    public RandomCollection(Random random) {
        this.random = random;
    }

    public void add(double weight, Integer result) {
        if (weight <= 0) return;
        total += weight;
        map.put(total, result);
    }

    public Integer next() {
        double value = random.nextDouble() * total;
        return map.ceilingEntry(value).getValue();
    }
}
