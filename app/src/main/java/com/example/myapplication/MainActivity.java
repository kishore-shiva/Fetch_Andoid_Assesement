package com.example.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "https://fetch-hiring.s3.amazonaws.com/hiring.json";
    private RecyclerView recyclerView;
    private ItemAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchData();
    }

    private void fetchData() {
        new FetchDataTask().execute(URL);
    }

    private class FetchDataTask extends AsyncTask<String, Void, List<Item>> {

        @Override
        protected List<Item> doInBackground(String... urls) {
            List<Item> items = new ArrayList<>();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONArray response = new JSONArray(result.toString());
                HashMap<Integer, List<Item>> groupedItems = new HashMap<>();

                for (int i = 0; i < response.length(); i++) {
                    JSONObject obj = response.getJSONObject(i);
                    int listId = obj.getInt("listId");
                    String name = obj.optString("name");

                    if (!name.equals("null") && !name.isEmpty()) {
                        Item item = new Item(listId, obj.getInt("id"), name);
                        if (!groupedItems.containsKey(listId)) {
                            groupedItems.put(listId, new ArrayList<>());
                        }
                        groupedItems.get(listId).add(item);
                    }
                }

                List<Item> sortedItems = new ArrayList<>();
                for (int key : groupedItems.keySet()) {
                    List<Item> itemList = groupedItems.get(key);
                    Collections.sort(itemList, new Comparator<Item>() {
                        @Override
                        public int compare(Item o1, Item o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    });
                    sortedItems.addAll(itemList);
                }

                Collections.sort(sortedItems, new Comparator<Item>() {
                    @Override
                    public int compare(Item o1, Item o2) {
                        return Integer.compare(o1.getListId(), o2.getListId());
                    }
                });

                return sortedItems;

            } catch (Exception e) {
                Log.e("FetchItems", "Error fetching data", e);
            }
            return items;
        }

        @Override
        protected void onPostExecute(List<Item> items) {
            if (items.isEmpty()) {
                Toast.makeText(MainActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            } else {
                adapter = new ItemAdapter(items);
                recyclerView.setAdapter(adapter);
            }
        }
    }
}