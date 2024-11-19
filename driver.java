import java.io.FileNotFoundException;

/**
 * The driver class for the cache simulator. It creates a new cache object then calls
 * its go() method.
 */
public class driver {

    /**
     * The main method that initializes and executes Cache.
     * Takes a file path as a command-line argument, initializes the Cache with it,
     * and invokes the {@code go()} method to process the graph data.
     * <p>
     * If the specified file cannot be found, a {@link FileNotFoundException} is caught, and
     * an error message is printed to the console.
     *
     * @param args Command-line arguments; {@code args[0]} should contain the file path for the graph data.
     * @throws ArrayIndexOutOfBoundsException if no arguments are provided.
     */
    public static void main(String[] args) {
        CacheSimulator myCache = new CacheSimulator(args[0]);
        try {
            myCache.go();
        } catch (FileNotFoundException e) {
           System.out.println(e.getMessage());
        }
    }
}