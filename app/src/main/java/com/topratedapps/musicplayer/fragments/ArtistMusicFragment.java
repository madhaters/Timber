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

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topratedapps.musicplayer.R;
import com.topratedapps.musicplayer.adapters.AlbumAdapter;
import com.topratedapps.musicplayer.dataloaders.ArtistAlbumLoader;
import com.topratedapps.musicplayer.models.Album;
import com.topratedapps.musicplayer.utils.Constants;

import java.util.ArrayList;

public class ArtistMusicFragment extends Fragment {

    public static RecyclerView songsRecyclerview;
    long artistID = -1;
    AlbumAdapter mSongAdapter;

    public static ArtistMusicFragment newInstance(long id) {
        ArtistMusicFragment fragment = new ArtistMusicFragment();
        Bundle args = new Bundle();
        args.putLong(Constants.ARTIST_ID, id);
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
                R.layout.fragment_artist_music, container, false);

        songsRecyclerview = (RecyclerView) rootView.findViewById(R.id.recycler_view_songs);

        setUpSongs();


        return rootView;
    }


    private void setUpSongs() {
        songsRecyclerview.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        ArrayList<Album> albums;
        albums = ArtistAlbumLoader.getAlbumsForArtist(getActivity(), artistID);


        mSongAdapter = new AlbumAdapter(getActivity(), albums);
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing_card);
        songsRecyclerview.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
        songsRecyclerview.setAdapter(mSongAdapter);
    }


    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {

            //the padding from left
            outRect.left = space;


        }
    }


}
