package slidingwindow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LongestSubstringTest {
    private final LongestSubstring solution = new LongestSubstring();

    @ParameterizedTest
    @CsvSource({"abcabcbb,3", "bbbbb,1", "pwwkew,3", "'',0",
                "a,1", "abba,2", "dvdf,3", "abcdef,6", "' ',1"})
    void examplesAndEdgeCases(String input, int expected) {
        assertEquals(expected, solution.lengthOfLongestSubstring(input));
    }
}
