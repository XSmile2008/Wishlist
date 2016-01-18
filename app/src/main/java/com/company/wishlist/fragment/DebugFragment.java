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
        Log.i(LOG_TAG, getId() + ".onAttach()");
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, getId() + ".onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(LOG_TAG, getId() + ".onCreateView()");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Log.i(LOG_TAG, getId() + ".onViewCreated()");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        Log.i(LOG_TAG, getId() + ".onPause()");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(LOG_TAG, getId() + ".onStop()");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, getId() + ".onDestroy()");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.i(LOG_TAG, getId() + ".onDetach()");
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        Log.i(LOG_TAG, getId() + ".onDestroyView()");
        super.onDestroyView();
    }

    @Override
    public void onLowMemory() {
        Log.i(LOG_TAG, getId() + ".onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i(LOG_TAG, getId() + ".onConfigurationChanged()");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, getId() + ".onSaveInstanceState()");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.i(LOG_TAG, getId() + ".onResume()");
        super.onResume();
    }

    @Override
    public void onStart() {
        Log.i(LOG_TAG, getId() + ".onStart()");
        super.onStart();
    }
}
