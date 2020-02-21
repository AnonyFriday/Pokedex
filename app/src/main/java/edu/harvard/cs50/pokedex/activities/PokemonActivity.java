package edu.harvard.cs50.pokedex.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import edu.harvard.cs50.pokedex.R;

public class PokemonActivity extends AppCompatActivity {

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            } catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            pokemonPic.setImageBitmap(bitmap);
        }
    }

    private ImageView pokemonPic;
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView catchButtonText;
    private TextView pokemonDesc;

    private String name;
    private String url;
    private String urlPic;
    private String urlSpec;
    private Boolean catched;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        sharedPreferences = getApplicationContext().getSharedPreferences("PokemonPull", 0);
        editor = sharedPreferences.edit();

        name = getIntent().getStringExtra("name").toLowerCase();
        url = getIntent().getStringExtra("url");
        urlSpec = "https://pokeapi.co/api/v2/pokemon-species/".concat(name);
        catched = false;

        pokemonPic = findViewById(R.id.pokemon_pic);

        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        catchButtonText = findViewById(R.id.catch_button);
        pokemonDesc = findViewById(R.id.pokemon_desc);

        load();
    }

    private void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest requestPokemonData = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    nameTextView.setText(name.substring(0, 1).toUpperCase() + name.substring(1));
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }

                    if (sharedPreferences.getBoolean(response.getString("name"), false)) {
                        catchPokemon();
                    } else {
                        releasePokemon();
                    }

                    JSONObject sprities = response.getJSONObject("sprites");
                    urlPic = sprities.getString("front_default");

                    new DownloadSpriteTask().execute(urlPic);

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        JsonObjectRequest requestPokemonDescription = new JsonObjectRequest(Request.Method.GET, urlSpec, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // TODO
                    JSONArray descList = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < descList.length(); i++) {
                        JSONObject langObj = (JSONObject) descList.get(i);
                        String lang = langObj.getJSONObject("language").getString("name");

                        if (lang.equals("en")) {
                            pokemonDesc.setText(langObj.getString("flavor_text"));
                        }
                    }

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(requestPokemonData);
        requestQueue.add(requestPokemonDescription);
    }

    public void toggleCatch(View view) {
        if (catched) {
            releasePokemon();
        } else {
            catchPokemon();
        }
    }

    private void catchPokemon() {
        catched = true;
        catchButtonText.setText("Release");

        editor.putBoolean(nameTextView.getText().toString(), true);
        editor.commit();
    }

    private void releasePokemon() {
        catched = false;
        catchButtonText.setText("Catch");

        editor.remove(nameTextView.getText().toString());
        editor.commit();
    }
}
