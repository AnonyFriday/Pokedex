package edu.harvard.cs50.pokedex.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.harvard.cs50.pokedex.R;
import edu.harvard.cs50.pokedex.activities.PokemonActivity;
import edu.harvard.cs50.pokedex.model.Pokemon;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {
    public static class PokedexViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView textView;

        PokedexViewHolder(View view) {
            super(view);

            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_text_view);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("name", current.getName());
                    intent.putExtra("url", current.getUrl());

                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    private class PokemonFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            // implement your search here!

            List<Pokemon> equalsList = new ArrayList<>();
            List<Pokemon> startsList = new ArrayList<>();
            List<Pokemon> containsList = new ArrayList<>();

            for (int i = 0 ; i < pokemon.size() ; i++) {
                Pokemon next = pokemon.get(i);

                if (next.getName().toLowerCase().equals(constraint.toString().toLowerCase())) {
                    equalsList.add(next);
                } else if (next.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                    startsList.add(next);
                } else if (next.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                    containsList.add(next);
                }
            }

            List<Pokemon> pokemonList = new ArrayList<>();
            pokemonList.addAll(equalsList);
            pokemonList.addAll(startsList);
            pokemonList.addAll(containsList);

            FilterResults results = new FilterResults();
            results.values = pokemonList; // you need to create this variable!
            results.count = pokemonList.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (List<Pokemon>) results.values;
            notifyDataSetChanged();
        }
    }

    private List<Pokemon> pokemon = new ArrayList<>();
    private List<Pokemon> filtered = new ArrayList<>();

    private RequestQueue requestQueue;

    public PokedexAdapter(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        loadPokemon();
    }

    public void loadPokemon() {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            try {
                JSONArray results = response.getJSONArray("results");
                Log.e("cs50", "Got JSON response successfully");
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    String name = result.getString("name");
                    pokemon.add(new Pokemon(
                        name.substring(0, 1).toUpperCase() + name.substring(1),
                        result.getString("url")
                    ));
                }

                notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("cs50", "Json error", e);
            }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon list error", error);
            }
        });

        requestQueue.add(request);
    }

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon current = null;
        if (!filtered.isEmpty() && filtered.size() > position) {
            current = filtered.get(position);
        } else if (filtered.isEmpty()) {
            current = pokemon.get(position);
        }

        if (current != null) {
            holder.textView.setText(current.getName());
            holder.containerView.setTag(current);
        }
    }

    @Override
    public int getItemCount() {
        return pokemon.size();
    }

    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }
}
