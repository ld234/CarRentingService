package car_service;

public class Pair<K,V> {

    private final K x;
    private final V y;

    public Pair(final K x, final V y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pair<?,?>)) {
            return false;
        }

        final Pair<?,?> pair = (Pair<?, ?>) o;

        if (x.equals(pair.x)) {
            return false;
        }
        if (y.equals(pair.y)) {
            return false;
        }
        return true;
    }
    
    public K getKey() {
    	return x;
    }
    
    public V getValue() {
    	return y;
    }
    
    @Override
    public int hashCode() {
        int result = x.hashCode() * y.hashCode();
        return result;
    }
}