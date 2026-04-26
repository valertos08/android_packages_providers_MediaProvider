/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.providers.media.photopicker.ui;

import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.android.providers.media.R;
import com.android.providers.media.photopicker.viewmodel.PickerViewModel;

/**
 * The tab container fragment - simplified for Albums-only view
 */
public class TabContainerFragment extends Fragment {
    private static final String TAG = "TabContainerFragment";

    private PickerViewModel mPickerViewModel;

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_picker_tab_container, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ViewModelProvider viewModelProvider = new ViewModelProvider(requireActivity());
        mPickerViewModel = viewModelProvider.get(PickerViewModel.class);

        if (savedInstanceState == null) {
            final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            final Fragment fragment;
            if (mPickerViewModel.getPickerLaunchTab() == MediaStore.PICK_IMAGES_TAB_IMAGES) {
                fragment = PhotosTabFragment.newInstance();
            } else {
                fragment = AlbumsTabFragment.newInstance();
            }
            ft.replace(R.id.fragment_container, fragment);
            ft.commitAllowingStateLoss();
        }
    }

    /**
     * Create the fragment and add it into the FragmentManager
     *
     * @param fm the fragment manager
     */
    public static void show(FragmentManager fm) {
        final FragmentTransaction ft = fm.beginTransaction();
        final TabContainerFragment fragment = new TabContainerFragment();
        ft.replace(R.id.fragment_container, fragment, TAG);
        ft.commitAllowingStateLoss();
    }
}