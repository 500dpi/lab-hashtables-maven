package edu.grinnell.csc207.util;

/**
 * Simple, immutable, key/value pairs.
 *
 * @author Samuel A. Rebelsky
 *
 *
 * @param <K>
 *   The type for keys.
 * @param <V>
 *   The type for values.
 */
public class Pair<K, V> {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * The key. May not be null.
   */
  private K key;

  /**
   * The associated value.
   */
  private V value;

  // +--------------+------------------------------------------------
  // | Constructors |
  // +--------------+

  /**
   * Create a new key/value pair.
   *
   * @param k
   *   The key.
   * @param v
   *   The value.
   */
  public Pair(K k, V v) {
    this.key = k;
    this.value = v;
  } // Pair(K, V)

  // +------------------+--------------------------------------------
  // | Standard methods |
  // +------------------+

  /**
   * Compute a hash code.
   *
   * @return the hash code.
   */
  public int hashCode() {
    return this.toString().hashCode();
  } // hashCode()

  /**
   * Compare for equality.
   *
   * @param other
   *   The object we're comparing to.
   *
   * @return true if the other object is a pair that is equal and false
   *    otherwise.
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object other) {
    return ((other instanceof Pair) && (this.equals((Pair<K, V>) other)));
  } // equals(Object)

  /**
   * Compare for equality.
   *
   * @param other
   *   The pair we're comparing to.
   *
   * @return true if the keys and values are equal and false otherwise.
   */
  public boolean equals(Pair<K, V> other) {
    return ((this.key.equals(other.key)) && (this.value.equals(other.value)));
  } // equals(Pair<K, V>)

  /**
   * Convert to string form.
   *
   * @return the corresponding string.
   */
  @Override
  public String toString() {
    return "<" + key + ":" + value + ">";
  } // toString()

  // +---------+-----------------------------------------------------
  // | Methods |
  // +---------+

  /**
   * Get the key.
   *
   * @return the key.
   */
  public K key() {
    return this.key;
  } // key()

  /**
   * Get the value.
   *
   * @return the value.
   */
  public V value() {
    return this.value;
  } // value()
} // Pair<K, V>
