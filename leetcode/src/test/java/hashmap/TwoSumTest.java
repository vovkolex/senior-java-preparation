package hashmap;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

class TwoSumTest {
    private final TwoSum solution = new TwoSum();

    private void check(int[] nums, int target, int... expected) {
        int[] actual = solution.twoSum(nums, target);
        Arrays.sort(actual);
        assertArrayEquals(expected, actual);
    }

    @Test void examples() {
        check(new int[]{2, 7, 11, 15}, 9, 0, 1);
        check(new int[]{3, 2, 4}, 6, 1, 2);
        check(new int[]{3, 3}, 6, 0, 1);
    }

    @Test void negativeAndZero() {
        check(new int[]{-3, 0, 4, 3}, 0, 0, 3);
        check(new int[]{0, 4, 3, 0}, 0, 0, 3);
    }

    @Test void noSolution() {
        assertThrows(IllegalArgumentException.class,
                () -> solution.twoSum(new int[]{1, 2}, 8));
    }
}
