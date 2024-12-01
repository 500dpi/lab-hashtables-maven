package edu.grinnell.csc207.util;

import java.io.PrintWriter;

/**
 * Simple hash tables.
 *
 * @author Samuel A. Rebelsky
 *
 * @param <K>
 *   The type for keys.
 * @param <V>
 *   The type for values.
 */
public interface HashTable<K, V> extends SimpleMap<K, V> {

  /**
   * Clear the whole table.
   */
  public void clear();

  /**
   * Dump the hash table.
   *
   * @param pen
   *   Where to dump the table.
   */
  public void dump(PrintWriter pen);

  /**
   * Should we report basic calls? Intended mostly for tracing.
   *
   * @param report
   *   Use true if you want basic calls reported and false otherwise.
   */
  public void reportBasicCalls(boolean report);

} // interface HashTable<K, V>
