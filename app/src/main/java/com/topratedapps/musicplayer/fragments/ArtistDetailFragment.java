/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.topratedapps.musicplayer.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.appthemeengine.Config;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.topratedapps.musicplayer.MusicPlayer;
import com.topratedapps.musicplayer.R;
import com.topratedapps.musicplayer.dataloaders.ArtistLoader;
import com.topratedapps.musicplayer.lastfmapi.LastFmClient;
import com.topratedapps.musicplayer.lastfmapi.callbacks.ArtistInfoListener;
import com.topratedapps.musicplayer.lastfmapi.models.ArtistQuery;
import com.topratedapps.musicplayer.lastfmapi.models.LastfmArtist;
import com.topratedapps.musicplayer.models.Artist;
import com.topratedapps.musicplayer.utils.ATEUtils;
import com.topratedapps.musicplayer.utils.Constants;
import com.topratedapps.musicplayer.utils.Helpers;
import com.topratedapps.musicplayer.utils.ImageUtils;
import com.topratedapps.musicplayer.utils.NavigationUtils;
import com.topratedapps.musicplayer.utils.TimberUtils;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

public class ArtistDetailFragment extends Fragment {

    long artistID = -1;

    ImageView artistArt;

    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    AppBarLayout appBarLayout;
    boolean largeImageLoaded = false;
    int primaryColor = -1;
    FloatingActionButton fab;

    public static ArtistDetailFragment newInstance(long id, boolean useTransition, String transitionName) {
        ArtistDetailFragment fragment = new ArtistDetailFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ID, id);
        args.putBoolean("transition", useTransition);
        if (useTransition)
            args.putString("transition_name", transitionName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            artistID = getArguments().getLong(Constants.ARTIST_ID);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_artist_detail, container, false);

        artistArt = (ImageView) rootView.findViewById(R.id.artist_art);

        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.app_bar);

        if (getArguments().getBoolean("transition")) {
            artistArt.setTransitionName(getArguments().getString("transition_name"));
        }

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        fab = rootView.findViewById(R.id.fab);
        setupFab();
        setupToolbar();
        setUpArtistDetails();

        getChildFragmentManager().beginTransaction().replace(R.id.container, ArtistMusicFragment.newInstance(artistID)).commit();


        return rootView;
    }

    private void setupFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MusicPlayer.playArtist(getActivity(), artistID, 0, true);
                        NavigationUtils.navigateToNowplaying(getActivity(), false);
                    }
                }, 150);
            }
        });
        Context context = getContext();
        MaterialDrawableBuilder builder = MaterialDrawableBuilder.with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.PLAY)
                .setColor(TimberUtils.getBlackWhiteColor(Config.accentColor(context, Helpers.getATEKey(context))));
        ATEUtils.setFabBackgroundTint(fab, Config.accentColor(context, Helpers.getATEKey(context)));
        fab.setImageDrawable(builder.build());
    }
    private void setupToolbar() {

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    private void setUpArtistDetails() {

        final Artist artist = ArtistLoader.getArtist(getActivity(), artistID);

        collapsingToolbarLayout.setTitle(artist.name);

        LastFmClient.getInstance(getActivity()).getArtistInfo(new ArtistQuery(artist.name), new ArtistInfoListener() {
            @Override
            public void artistInfoSucess(final LastfmArtist artist) {
                if (artist != null) {

                    ImageLoader.getInstance().displayImage(artist.mArtwork.get(4).mUrl, artistArt,
                            new DisplayImageOptions.Builder().cacheInMemory(true)
                                    .cacheOnDisk(true)
                                    .showImageOnFail(R.drawable.ic_empty_music2)
                                    .build(), new SimpleImageLoadingListener() {
                                @Override
                                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                    largeImageLoaded = true;

                                }
                            });
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setBlurredPlaceholder(artist);
                        }
                    }, 100);

                }
            }

            @Override
            public void artistInfoFailed() {

            }
        });

    }

    private void setBlurredPlaceholder(LastfmArtist artist) {
        ImageLoader.getInstance().loadImage(artist.mArtwork.get(1).mUrl, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (getActivity() != null && !largeImageLoaded)
                    new setBlurredAlbumArt().execute(loadedImage);

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        toolbar.setBackgroundColor(Color.TRANSPARENT);

    }

    private class setBlurredAlbumArt extends AsyncTask<Bitmap, Void, Drawable> {

        @Override
        protected Drawable doInBackground(Bitmap... loadedImage) {
            Drawable drawable = null;
            try {
                drawable = ImageUtils.createBlurredImageFromBitmap(loadedImage[0], getActivity(), 3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return drawable;
        }

        @Override
        protected void onPostExecute(Drawable result) {
            if (result != null && !largeImageLoaded) {
                artistArt.setImageDrawable(result);
            }
        }

        @Override
        protected void onPreExecute() {
        }
    }

}