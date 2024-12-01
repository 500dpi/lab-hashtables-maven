package edu.grinnell.csc207.util;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * A simple implementation of probed hash tables.
 *
 * @author Sheilla Muligande
 * @author Sara Jaljaa
 * @author Samuel A. Rebelsky
 *
 * @param <K>
 *   The type of keys.
 * @param <V>
 *   The type of values.
 */
public class ProbedHashTable<K, V> implements HashTable<K, V> {

  // +-------+-----------------------------------------------------------
  // | Notes |
  // +-------+

  /*
   * Our hash table is stored as an array of key/value pairs. Because of the
   * design of Java arrays, we declare that as type Object[] and cast whenever
   * we remove an element. (SamR needs to find a better way to deal with this
   * issue; using ArrayLists doesn't seem like the best idea.)
   *
   * We use linear probing to handle collisions. (Well, we *will* use linear
   * probing, once the table is finished.)
   *
   * We expand the hash table when the load factor is greater than LOAD_FACTOR
   * (see constants below).
   *
   * Since some combinations of data and hash function may lead to a situation
   * in which we get a surprising relationship between values (e.g., all the
   * hash values are 0 mod 32), when expanding the hash table, we incorporate a
   * random number. (Is this likely to make a big difference? Who knows. But
   * it's likely to be fun.)
   *
   * For experimentation and such, we allow the client to supply a Reporter that
   * is used to report behind-the-scenes work, such as calls to expand the
   * table.
   *
   * Bugs to squash.
   *
   * [X] Checks for repeated keys in set.
   *
   * [X] Checks for matching key in get.
   *
   * [X] Handles collisions.
   *
   * [X] The `expand` method is implemented.
   *
   * [ ] The `remove` method is not implemented.
   *
   * Features to add.
   *
   * [ ] A full implementation of `containsKey`.
   *
   * [ ] An iterator.
   */

  // +-----------+-------------------------------------------------------
  // | Constants |
  // +-----------+

  /**
   * The load factor for expanding the table.
   */
  static final double LOAD_FACTOR = 0.5;

  /**
   * The offset to use in linear probes. (We choose a prime because that helps
   * ensure that we cover all of the spaces.)
   */
  static final int PROBE_OFFSET = 17;

  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of values currently stored in the hash table. We use this to
   * determine when to expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the key/value pairs. (We use an array,
   * rather than an ArrayList, because we want to control expansion.)
   */
  Object[] pairs;

  /**
   * An optional reporter to let us observe what the hash table is doing.
   */
  Reporter reporter;

  /**
   * Do we report basic calls?
   */
  boolean REPORT_BASIC_CALLS = false;

  /**
   * Our helpful random number generator, used primarily when expanding the size
   * of the table..
   */
  Random rand;

  // +--------------+----------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new hash table.
   */
  public ProbedHashTable() {
    this.rand = new Random();
    this.clear();
    this.reporter = null;
  } // ProbedHashTable

  /**
   * Create a new hash table that reports activities using a reporter.
   *
   * @param report
   *   The object used to report activities.
   */
  public ProbedHashTable(Reporter report) {
    this();
    this.reporter = report;
  } // ProbedHashTable(Reporter)

  // +-------------------+-------------------------------------------
  // | SimpleMap methods |
  // +-------------------+

  /**
   * Determine if the hash table contains a particular key.
   *
   * @param key
   *   The key to look for.
   *
   * @return true if the key is in the table and false otherwise.
   */
  @Override
  public boolean containsKey(K key) {
    // STUB/HACK
    try {
      get(key);
      return true;
    } catch (Exception e) {
      return false;
    } // try/catch
  } // containsKey(K)

  /**
   * Apply a function to each key/value pair in the table.
   *
   * @param action
   *   The function to apply. Takes a key and a value as parameters.
   */
  public void forEach(BiConsumer<? super K, ? super V> action) {
    for (Pair<K, V> pair : this) {
      action.accept(pair.key(), pair.value());
    } // for
  } // forEach(BiConsumer)

  /**
   * Get the value for a particular key.
   *
   * @param key
   *   The key to search for.
   *
   * @return the corresponding value.
   *
   * @throws IndexOutOfBoundsException
   *   when the key is not in the table.
   */
  @Override
  public V get(K key) {
    int index = find(key);
    @SuppressWarnings("unchecked")
    Pair<K, V> pair = (Pair<K, V>) pairs[index];
    if (pair == null) {
      if (REPORT_BASIC_CALLS && (reporter != null)) {
        reporter.report("get(" + key + ") failed");
      } // if reporter != null
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } else {
      if (pair.key().equals(key) && REPORT_BASIC_CALLS && (reporter != null)) {
        reporter.report("get(" + key + ") => " + pair.value());
      } // if reporter != null
      return pair.value();
    } // get
  } // get(K)

  /**
   * Iterate the keys in some order.
   *
   * @return
   *   An iterator for the keys.
   */
  public Iterator<K> keys() {
    return MiscUtils.transform(this.iterator(), (pair) -> pair.key());
  } // keys()

  /**
   * Remove a key/value pair.
   *
   * @param key
   *   The key of the key/value pair.
   *
   * @return
   *   The corresponding value.
   */
  @Override
  public V remove(K key) {
    // STUB
    return null;
  } // remove(K)

  /**
   * Set a value.
   *
   * @param key
   *   The key to set.
   * @param value
   *   The value to set.
   *
   * @return the prior value for the key, if there was one, or null.
   */
  @SuppressWarnings("unchecked")
  public V set(K key, V value) {
    V result = null;
    // If there are too many entries, expand the table.
    if (this.size > (this.pairs.length * LOAD_FACTOR)) {
      expand();
    } // if there are too many entries
    // Find out where the key belongs and put the pair there.
    int index = find(key);
    /* If the keys are the same, check if the value to be is set is the
    same as the pre-existing value. */
    if (this.pairs[index] != null && ((Pair) this.pairs[index]).key().equals(key)) {
      result = ((Pair<K, V>) this.pairs[index]).value();
      /* If the value to be set is different, then set the value at the key and return
      the previous value. Otherwise, return null since the value remains unchanged. */
      if (result != value) {
        this.pairs[index] = new Pair<K, V>(key, value);
        return result;
      } else {
        return null;
      } // elif
    } // if
    this.pairs[index] = new Pair<K, V>(key, value);
    // Report activity, if appropriate
    if (REPORT_BASIC_CALLS && (reporter != null)) {
      reporter.report("pairs[" + index + "] = " + key + ":" + value);
    } // if reporter != null
    // Note that we've incremented the size.
    ++this.size;
    // And we're done
    return result;
  } // set(K, V)

  /**
   * Get the size of the dictionary - the number of key/value pairs
   * stored in the dictionary.
   *
   * @return the number of key/value pairs in the dictionary.
   */
  @Override
  public int size() {
    return this.size;
  } // size()

  /**
   * Iterate the values in some order.
   *
   * @return an iterator for the values.
   */
  public Iterator<V> values() {
    return MiscUtils.transform(this.iterator(), (pair) -> pair.value());
  } // values()

  // +------------------+--------------------------------------------
  // | Iterator methods |
  // +------------------+

  /**
   * Iterate the key/value pairs in some order.
   *
   * @return an iterator for the key/value pairs.
   */
  public Iterator<Pair<K, V>> iterator() {
    return new Iterator<Pair<K, V>>() {
      public boolean hasNext() {
        // STUB
        return false;
      } // hasNext()

      public Pair<K, V> next() {
        // STUB
        return null;
      } // next()
    }; // new Iterator
  } // iterator()

  // +-------------------+-------------------------------------------
  // | HashTable methods |
  // +-------------------+

  /**
   * Clear the whole table.
   */
  @Override
  public void clear() {
    this.pairs = new Object[41];
    this.size = 0;
  } // clear()

  /**
   * Dump the hash table.
   *
   * @param pen
   *   Where to dump the table.
   */
  @Override
  public void dump(PrintWriter pen) {
    pen.print("{");
    int printed = 0; // Number of elements printed
    for (int i = 0; i < this.pairs.length; i++) {
      @SuppressWarnings("unchecked")
      Pair<K, V> pair = (Pair<K, V>) this.pairs[i];
      if (pair != null) {
        pen.print(i + ":" + pair.key() + "(" + pair.key().hashCode() + "):"
            + pair.value());
        if (++printed < this.size) {
          pen.print(", ");
        } // if
      } // if the current element is not null
    } // for
    pen.println("}");
  } // dump(PrintWriter)

  // +------+------------------------------------------------------------
  // | Misc |
  // +------+

  /**
   * Should we report basic calls? Intended mostly for tracing.
   *
   * @param report
   *   Use true if you want basic calls reported and false otherwise.
   */
  public void reportBasicCalls(boolean report) {
    REPORT_BASIC_CALLS = report;
  } // reportBasicCalls

  // +---------+---------------------------------------------------------
  // | Helpers |
  // +---------+

  /**
   * Expand the size of the table.
   */
  void expand() {
    // Figure out the size of the new table.
    int newSize = 2 * this.pairs.length + rand.nextInt(20);
    /* Check if the size is a multiple of the probe offset. */
    if (newSize % PROBE_OFFSET == 0) {
      newSize++;
    } // if
    if (REPORT_BASIC_CALLS && (reporter != null)) {
      reporter.report("Expanding to " + newSize + " elements.");
    } // if reporter != null
    // Create a new table of that size.
    Object[] newPairs = new Object[newSize];
    /* Copy over the values from the original pairs, skipping the null elements. */
    for (int i = 0; i < this.pairs.length; i++) {
      if (this.pairs[i] != null) {
        newPairs[i] = this.pairs[i];
      } // if
    } // for
    this.pairs = newPairs;
  } // expand()

  /**
   * Find the index of the entry with a given key. If there is no such entry,
   * return the index of an entry we can use to store that key.
   *
   * @param key
   *   The key we're searching for.
   *
   * @return the aforementioned index.
   */
  @SuppressWarnings({"rawtypes"})
  int find(K key) {
    /* The starting index. */
    int index = Math.abs(key.hashCode()) % this.pairs.length;
    /* Checks first that the pair is not null, then if the pair
    matches the target key. */
    while (this.pairs[index] != null
        && !((Pair) this.pairs[index]).key().equals(key)) {
      index = (index + PROBE_OFFSET) % this.pairs.length;
    } // while
    /* If pair is found, it will be returned; otherwise, the last
    index must be a null value to exit the loop. */
    return index;
  } // find(K)
} // class ProbedHashTable<K, V>

