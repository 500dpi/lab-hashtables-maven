package edu.grinnell.csc207.util;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * A simple implementation of chained hash tables.
 *
 * @author Sheilla Muligande
 * @author Sara Jaljaa
 * @author Samuel A. Rebelsky
 *
 * @param <K>
 *   The type of keys in the table.
 * @param <V>
 *   The type of values in the table.
 */
public class ChainedHashTable<K, V> implements HashTable<K, V> {

  // +-------+-----------------------------------------------------------
  // | Notes |
  // +-------+

  /*
   * Our hash table is stored as an array of ArrayLists of key/value pairs.
   * Because of the design of Java arrays, we declare that as type Object[]
   * rather than ArrayList<Pair<K, V>>[] and cast whenever we extract an an
   * element. (SamR needs to find a better way to deal with this issue; using
   * ArrayLists doesn't seem like the best idea.)
   *
   * We use chaining to handle collisions. (Well, we *will* use chaining once
   * the table is finished.)
   *
   * We expand the hash table when the load factor is greater than LOAD_FACTOR
   * (see constants below).
   *
   * Since some combinations of data and hash function may lead to a situation
   * in which we get a surprising relationship between values (e.g., all the
   * hash values are 0 mod 32), when expanding the hash table, we incorporate a
   * random number. (Is this likely to make a big difference? Who knows. But
   * it's likely to be fun to explore.)
   *
   * For experimentation and such, we allow the client to supply a Reporter that
   * is used to report behind-the-scenes work, such as calls to expand the
   * table.
   *
   * Bugs to squash.
   *
   * [ ] Doesn't check for repeated keys in `set`.
   *
   * [ ] Doesn't look for matching key in `get`.
   *
   * [ ] Doesn't handle collisions.
   *
   * Other features to add.
   *
   * [ ] The `expand` method is not completely implemented.
   *
   * [ ] The `remove` method is not implemented.
   *
   * [ ] The `containsKey` method is not implemented.
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

  // +--------+----------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The number of values currently stored in the hash table. We use this to
   * determine when to expand the hash table.
   */
  int size = 0;

  /**
   * The array that we use to store the ArrayList of key/value pairs. (We use an
   * array, rather than an ArrayList, because we want to control expansion and
   * ArrayLists of ArrayLists are just weird.)
   */
  Object[] buckets;

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
  public ChainedHashTable() {
    this.rand = new Random();
    this.clear();
    this.reporter = null;
  } // ChainedHashTable

  /**
   * Create a new hash table that reports activities using a reporter.
   *
   * @param report
   *   The object used to report activities.
   */
  public ChainedHashTable(Reporter report) {
    this();
    this.reporter = report;
  } // ChainedHashTable(Reporter)

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
   * Apply a function to each key/value pair.
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
    ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) buckets[index];
    if (alist == null) {
      if (REPORT_BASIC_CALLS && (reporter != null)) {
        reporter.report("get(" + key + ") failed");
      } // if reporter != null
      throw new IndexOutOfBoundsException("Invalid key: " + key);
    } else {
      Pair<K, V> pair = alist.get(0);
      if (REPORT_BASIC_CALLS && (reporter != null)) {
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
    if (this.size > (this.buckets.length * LOAD_FACTOR)) {
      expand();
    } // if there are too many entries

    // Find out where the key belongs and put the pair there.
    int index = find(key);
    ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) this.buckets[index];
    // Special case: Nothing there yet
    if (alist == null) {
      alist = new ArrayList<Pair<K, V>>();
      this.buckets[index] = alist;
    } // if
    alist.add(new Pair<K, V>(key, value));
    //++this.size;

    // Report activity, if appropriate
    if (REPORT_BASIC_CALLS && (reporter != null)) {
      reporter.report("adding '" + key + ":" + value + "' to bucket " + index);
    } // if reporter != null

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
    this.buckets = new Object[41];
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
    pen.println("Capacity: " + this.buckets.length + ", Size: " + this.size);
    for (int i = 0; i < this.buckets.length; i++) {
      @SuppressWarnings("unchecked")
      ArrayList<Pair<K, V>> alist = (ArrayList<Pair<K, V>>) this.buckets[i];
      if (alist != null) {
        for (Pair<K, V> pair : alist) {
          pen.println("  " + i + ": <" + pair.key() + "(" + pair.key().hashCode()
              + "):" + pair.value() + ">");
        } // for each pair in the bucket
      } // if the current bucket is not null
    } // for each bucket
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
    // Figure out the size of the new table
    int newSize = 2 * this.buckets.length + rand.nextInt(10);
    if (REPORT_BASIC_CALLS && (reporter != null)) {
      reporter.report("Expanding to " + newSize + " elements.");
    } // if reporter != null
    // STUB
  } // expand()

  /**
   * Find the index of the bucket for objects with a given key. If there is no
   * such entry, return the index of a bucket we can use for that key.
   *
   * @param key
   *   The key we're searching for.
   *
   * @return the aforementioned index.
   */
  int find(K key) {
    return Math.abs(key.hashCode()) % this.buckets.length;
  } // find(K)

} // class ChainedHashTable<K, V>

