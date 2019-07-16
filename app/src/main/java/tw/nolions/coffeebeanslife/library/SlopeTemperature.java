package tw.nolions.coffeebeanslife.library;

import android.util.Log;

import java.util.HashMap;

public class SlopeTemperature {

    /**
     * 計算兩點之間的斜率
     *
     * @param timeDiff
     * @param tempDiff
     * @return
     */
    public float slope(int timeDiff, int tempDiff) {
        Log.e("test", "test slope:" + tempDiff / timeDiff);
        return (float) tempDiff / timeDiff;
    }

    /**
     * 取得目標溫度
     *
     * @param slope
     * @param timeDiff
     * @param oldTemp
     * @return
     */
    public int temperature(float slope, int oldTemp, int timeDiff) {
        return (int) (slope * timeDiff + oldTemp);
    }

    public HashMap<Integer, Integer> allTemperature(float slope, int startTime, int endTime, int range, int oldTemp, int newTemp) {
        HashMap<Integer, Integer> map = new HashMap<>();
        int timeRange = endTime - startTime;
        for (int i = 1; i < timeRange; i += range) {
            int time = startTime + i;
            int timeDiff = time - startTime;
            int temp = this.temperature(slope, oldTemp, timeDiff);
            temp = temp > newTemp?newTemp:temp;
            map.put(time, temp);
        }

        return map;
    }
}
