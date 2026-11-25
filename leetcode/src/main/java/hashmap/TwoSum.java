package hashmap;

import java.util.HashMap;
import java.util.Map;

public class TwoSum {
    public int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> data = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer previousIndex = data.get(target - nums[i]);
            if (previousIndex != null) {
                return new int[]{i, previousIndex};
            }
            data.put(nums[i], i);
        }
        throw new IllegalArgumentException("No solution found");
    }
}
