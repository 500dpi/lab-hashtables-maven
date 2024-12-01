package edu.grinnell.csc207.experiments;

import edu.grinnell.csc207.util.HashTable;

import java.io.PrintWriter;

/**
 * A simple set of experiments using our new hash tables.
 *
 * @author Sheilla Muligande
 * @author Sara Jaljaa
 * @author Samuel A. Rebelsky
 */
public class HashTableExperiments {

  // +--------+------------------------------------------------------
  // | Fields |
  // +--------+

  /**
   * A word list stolen from some tests that SamR wrote in the distant past.
   */
  static String[] words = {"aardvark", "anteater", "antelope", "bear", "bison",
      "buffalo", "chinchilla", "cat", "dingo", "elephant", "eel",
      "flying squirrel", "fox", "goat", "gnu", "goose", "hippo", "horse",
      "iguana", "jackalope", "kestrel", "llama", "moose", "mongoose", "nilgai",
      "orangutan", "opossum", "red fox", "snake", "tarantula", "tiger",
      "vicuna", "vulture", "wombat", "yak", "zebra", "zorilla"};

  // +-------------+-----------------------------------------------------
  // | Experiments |
  // +-------------+

  /**
   * A short experiment with getting a value from the hash table.
   *
   * @param pen
   *   Where to print messages.
   * @param htab
   *   The table to use.
   * @param key
   *   The key to search for.
   */
  public static void checkGet(PrintWriter pen, HashTable<String, String> htab,
      String key) {
    pen.print("Getting " + key + " ... ");
    pen.flush();
    try {
      pen.println(htab.get(key));
    } catch (Exception e) {
      pen.println("Failed because " + e);
    } // try/catch
  } // checkGet(PrintWriter, HashTable<String, String>, String)

  /**
   * Explore what happens when we use set with a repeated key.
   *
   * @param pen
   *   Where to print messages.
   * @param htab
   *   The table to use.
   */
  public static void repeatedSetExpt(PrintWriter pen,
      HashTable<String, String> htab) {
    
        htab.reportBasicCalls(true);
        htab.set("alpha", "alpha");
        htab.dump(pen);
        htab.set("beta", "beta");
        htab.dump(pen);
        htab.set("bravo", "bravo");
        htab.dump(pen);
        htab.set("beta", "max");
        htab.dump(pen);
        htab.reportBasicCalls(false);
        checkGet(pen, htab, "bravo");
        pen.println();
  } // repeatedSetExpt(PrintWriter, HashTable)

  /**
   * Explore what happens when we use two keys that map to the same location.
   *
   * @param pen
   *   Where to print messages.
   * @param htab
   *   The table to use.
   */
  public static void matchingKeysExpt(PrintWriter pen,
      HashTable<String, String> htab) {
    pen.println("Setting anteater");
    htab.set("anteater", "anteater");
    checkGet(pen, htab, "anteater");
    checkGet(pen, htab, "buffalo");
    htab.dump(pen);
    pen.println();
  } // matchingKeysExpt(PrintWriter, HashTable)

  /**
   * Explore what happens when we use two keys that map to the same location.
   *
   * @param pen
   *   Where to print messages.
   * @param htab
   *   The table to use.
   */
  public static void matchingSetExpt(PrintWriter pen,
      HashTable<String, String> htab) {
    pen.println("Setting anteater");
    htab.set("anteater", "anteater");
    htab.set("buffalo", "buffalo");
    checkGet(pen, htab, "anteater");
    checkGet(pen, htab, "buffalo");
    htab.dump(pen);
    pen.println();
  } // matchingSetExpt(PrintWriter, HashTable)

  /**
   * Explore what happens when we use set with a wide variety of key/value
   * pairs.
   *
   * @param pen
   *   Where to print messages.
   * @param htab
   *   The table to use.
   */
  public static void multipleSetExpt(PrintWriter pen,
      HashTable<String, String> htab) {
    int numwords = words.length;
    for (int i = 0; i < numwords; i++) {
      // htab.dump(pen);
      htab.set(words[i], words[i]);
      for (int j = 0; j <= i; j++) {
        try {
          String str = htab.get(words[j]);
          if (!str.equals(words[j])) {
            pen.println("After setting " + words[i] + ", " + words[j]
                + " no longer yields itself.");
            htab.dump(pen);
            return;
          } // if we didn't get the expected value.
        } catch (Exception e) {
          pen.println("After setting " + words[i] + ", " + words[j]
              + " is no longer in the table.");
          htab.dump(pen);
          return;
        } // try/catch
      } // for j
    } // for i
    htab.dump(pen);
    pen.println();
  } // multipleSetExpt(PrintWriter, HashTable)

  /**
   * Explore what happens when we remove elements.
   *
   * @param pen
   *   Where to print messages.
   * @param htab
   *   The table to use.
   */
  public static void removeExpt(PrintWriter pen,
      HashTable<String, String> htab) {
    // STUB
  } // removeExpt(PrintWriter, HashTable)

} // class HashTableExperiments
