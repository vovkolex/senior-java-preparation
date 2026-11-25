# LeetCode — Java

Java 21, Maven, JUnit 5. Run from the repository root:

```bash
mvn -f leetcode/pom.xml test
```

## Progress

| Problem | Pattern | Time (average) | Auxiliary space | Status | Next review |
|---|---|---|---|---|---|
| [#1 Two Sum](https://leetcode.com/problems/two-sum/) | One-pass HashMap | O(n) | O(n) | Accepted (66/66) | 2026-11-26 |
| [#3 Longest Substring Without Repeating Characters](https://leetcode.com/problems/longest-substring-without-repeating-characters/) | Sliding Window | O(n) | O(min(n, k)) | Accepted (1036/1036) | 2026-11-26 |

Both solutions accepted on 2026-11-25. Review from scratch after 1, 3, 7, 14 and 30 days.

## #1 Two Sum

[Solution](src/main/java/hashmap/TwoSum.java) · [Tests](src/test/java/hashmap/TwoSumTest.java)

Store previously processed numbers and their indices. Look up the complement before inserting the current number. Return immediately when a match is found.

Interview reminders:
- Checking before inserting prevents using the same array element twice, including when the complement equals the current number.
- A two-pass map that stores only the last index can lose duplicate indices.
- A missing map entry returns null; assigning it to int compiles but throws NullPointerException during unboxing. Comparing a primitive int with null does not compile.
- HashMap operations take O(1) on average; brute force trades O(n²) time for O(1) auxiliary space.

## #3 Longest Substring Without Repeating Characters

[Solution](src/main/java/slidingwindow/LongestSubstring.java) · [Tests](src/test/java/slidingwindow/LongestSubstringTest.java)

Maintain an inclusive window [left, right] and store each character's last occurrence index. On a repeated character, move left to max(left, previousIndex + 1). Update the map and maximum window length.

Interview reminders:
- Invariant: after each iteration, the window contains unique characters.
- Never move left backward: test with "abba".
- Inclusive window length is right - left + 1.
- HashMap stores indices, allowing a direct jump. HashSet also works by removing characters from the left until the duplicate is gone.
- Amortized analysis: each position enters and leaves the HashSet window at most once, so total pointer movements are O(n), even with a nested while loop.
- charAt avoids the O(n) extra array allocated by toCharArray.
- Space is O(min(n, k)), where k is the number of possible characters (Java char units); for a fixed alphabet this is O(1).

## Review focus

Reimplement both algorithms without hints. Explain the invariant, amortized analysis, HashMap vs HashSet and unboxing in English.
