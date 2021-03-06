package ru.whalemare.weather.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.ButterKnife;
import ru.whalemare.weather.R;
import ru.whalemare.weather.activity.ForecastActivity;
import ru.whalemare.weather.adapters.CityCursorAdapter;
import ru.whalemare.weather.database.CitiesProvider;
import ru.whalemare.weather.database.DatabaseHandler;
import ru.whalemare.weather.di.App;
import ru.whalemare.weather.tasks.CityLoader;

public class CityFragment extends Fragment implements SearchView.OnQueryTextListener, LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = getClass().getSimpleName();

    SearchView searchView;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CityCursorAdapter adapter;
    private SharedPreferences shared;

    @Inject DatabaseHandler database;

    public CityFragment() {
    }

    public static CityFragment newInstance() {
        return new CityFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shared = getContext().getSharedPreferences(CitiesProvider.CitiesMetaData.KEY_GISMETEO_CODE, Context.MODE_PRIVATE);
        final String gismeteoCode = shared.getString(CitiesProvider.CitiesMetaData.KEY_GISMETEO_CODE, null);
        final String cityName = shared.getString(CitiesProvider.CitiesMetaData.KEY_CITY_NAME, null);
        if (gismeteoCode != null && cityName != null) {
            Intent intent = new Intent(getActivity(), ForecastActivity.class);
            intent.putExtra(CitiesProvider.CitiesMetaData.KEY_GISMETEO_CODE, gismeteoCode);
            intent.putExtra(CitiesProvider.CitiesMetaData.KEY_CITY_NAME, cityName);
            startActivity(intent);
        }

        Bundle args = new Bundle();
        args.putInt("DO", -1);
        getLoaderManager().initLoader(0, args, this);

        App.get(getContext()).getComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_city, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView_city);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new CityCursorAdapter(getContext(), null);
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_sw, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        Bundle args = new Bundle();
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Bundle args = new Bundle();
        args.putString("query", query);
        getLoaderManager().restartLoader(0, args, this);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        database.close();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
        database.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CityLoader(getContext(), args);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }
}
