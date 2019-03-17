package tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Convert {

    /**
     * 取到小數點第二位
     * @param data
     * @return
     */
    public static String DecimalPoint(Double data) {
        return DecimalPoint(data, "#.##");
    }

    /**
     * Double轉換成指定格式字串
     * @param data
     * @param format
     * @return
     */
    public static String DecimalPoint(Double data, String format) {
        DecimalFormat df=new DecimalFormat(format);
        return df.format(data);
    }


    /**
     * Convert to HaspMap form JSONObject
     * @param object
     * @return
     * @throws JSONException
     */
    public static HashMap<String, Object> toMap(JSONObject object) throws JSONException {
        HashMap<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    /**
     * Convert to ArrayList form JSONOArray
     * @param array
     * @return
     * @throws JSONException
     */
    public static ArrayList<Object> toList(JSONArray array) throws JSONException {
        ArrayList<Object> list = new ArrayList<>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
}
