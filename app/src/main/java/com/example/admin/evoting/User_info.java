package com.example.admin.evoting;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User_info extends Activity {
    TextView first_name;
    ArrayList candidate_name = new ArrayList();
    ArrayList party_name = new ArrayList();
    ArrayList description = new ArrayList();
    ArrayList logos = new ArrayList();
    ListView listView;
    ImageButton info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        first_name = (TextView) findViewById(R.id.first_name);
        info = (ImageButton) findViewById(R.id.info);
        Intent intent = getIntent();
        String scanContent = intent.getStringExtra("VoterId");
        JSONParser jParser = new JSONParser();
        String firstName;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        JSONObject candidate_info;
        JSONArray json = jParser.makeHttpRequest("http://172.31.24.43/php/myphpfile.php?Voter_id=" + scanContent,
                "GET", null);
        try {
            JSONObject user_info = json.getJSONObject(0);
            firstName = user_info.getString("first_name");
            first_name.setText("Hello " + firstName);
            for(int i = 1; i < json.length(); i++) {
                candidate_info = json.getJSONObject(i);
                candidate_name.add(candidate_info.getString("Candidate"));
                party_name.add(candidate_info.getString("Name"));
                description.add(candidate_info.getString("Description"));
                logos.add(candidate_info.getString("Logo"));
            }
            List<HashMap<String, Object>> aList = new ArrayList<>();
            for(int i = 0;i < candidate_name.size();i++){
                HashMap<String, Object> hm = new HashMap<>();
                hm.put("cand", "" + candidate_name.get(i));
                hm.put("party","" + party_name.get(i));
                hm.put("logo", logos.get(i));
                aList.add(hm);
            }
            String[] from = { "logo","cand","party" };
            int[] to = { R.id.logo,R.id.cand,R.id.party};
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), aList, R.layout.candidate_list, from, to);
            listView = ( ListView ) findViewById(R.id.listview);
            listView.setAdapter(adapter);
            for(int i=0;i<adapter.getCount();i++){
                String imgUrl = "http://172.31.24.43/php"+logos.get(i);
                ImageLoaderTask imageLoaderTask = new ImageLoaderTask();
                HashMap<String, Object> hmDownload = new HashMap<>();
                hmDownload.put("flag_path",imgUrl);
                hmDownload.put("position", i);
                imageLoaderTask.execute(hmDownload);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class ImageLoaderTask extends AsyncTask<HashMap<String, Object>, Void, HashMap<String, Object>>{

        @Override
        protected HashMap<String, Object> doInBackground(HashMap<String, Object>... hm) {

            InputStream iStream=null;
            String imgUrl = (String) hm[0].get("flag_path");
            int position = (Integer) hm[0].get("position");
            URL url;
            try {
                url = new URL(imgUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                iStream = urlConnection.getInputStream();
                File cacheDirectory = getBaseContext().getCacheDir();
                File tmpFile = new File(cacheDirectory.getPath() + "/wpta_"+position+".png");
                FileOutputStream fOutStream = new FileOutputStream(tmpFile);
                Bitmap b = BitmapFactory.decodeStream(iStream);
                b.compress(Bitmap.CompressFormat.PNG,100, fOutStream);
                fOutStream.flush();
                fOutStream.close();
                HashMap<String, Object> hmBitmap = new HashMap<String, Object>();
                hmBitmap.put("flag",tmpFile.getPath());
                hmBitmap.put("position",position);
                return hmBitmap;
            }catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> result) {
            String path = (String) result.get("flag");
            int position = (Integer) result.get("position");
            SimpleAdapter adapter = (SimpleAdapter ) listView.getAdapter();
            HashMap<String, Object> hm = (HashMap<String, Object>) adapter.getItem(position);
            hm.put("logo",path);
            adapter.notifyDataSetChanged();
        }
    }
    public void showInfo(View view)
    {
        LinearLayout parentRow = (LinearLayout) view.getParent();
        RelativeLayout rl = (RelativeLayout) parentRow.getParent();
        ListView listView = (ListView) rl.getParent();
        final int position = listView.getPositionForView(parentRow);
        Intent intent = new Intent(this, Info.class);
        intent.putExtra("Description", description.get(position).toString());
        startActivity(intent);
    }
}

