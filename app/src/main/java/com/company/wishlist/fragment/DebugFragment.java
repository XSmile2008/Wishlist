package com.company.wishlist.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Starikov on 10.12.15.
 */
public abstract class DebugFragment extends Fragment {

    String LOG_TAG = this.getClass().getSimpleName();

    @Override
    public void onAttach(Context context) {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onAttach()");
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "Fragment" + getId() + ".onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onCreateView()");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onDetach()");
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onResume()");
        super.onResume();
    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, this.getClass().getSimpleName() + getId() + ".onStart()");
        super.onStart();
    }
}
