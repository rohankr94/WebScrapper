package com.cermati.scrapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.cermati.scrapper.GetRoleDetailsParallel.mp;

public class BuildJson {
    public static void build() throws IOException {

        JSONObject jo = new JSONObject();
        for (Map.Entry<String, ArrayList<JSONObject>> entry : mp.entrySet()){
            JSONArray ja = new JSONArray();
            String key = entry.getKey();
            List<JSONObject> value = entry.getValue();
            for(int i=0;i<value.size();i++){
                ja.put(value.get(i));
            }
            jo.put(key, ja);
        }
        FileWriter file = new FileWriter("./solution.json");
        file.write(jo.toString(2));
        file.close();
    }
}
