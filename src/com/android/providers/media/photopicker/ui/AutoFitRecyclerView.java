/*
 * Copyright (C) 2021 The Android Open Source Project
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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The AutoFitRecyclerView auto fits the column width to decide the span count
 */
public class AutoFitRecyclerView extends RecyclerView {

    private int mColumnWidth = -1;
    private int mMinimumSpanCount = 2;
    private boolean mIsGridLayout;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mPendingFocusSearch = false;
    private View mPendingFocusedView;
    private int mPendingDirection;

    public AutoFitRecyclerView(Context context) {
        super(context);
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mIsGridLayout && mColumnWidth > 0) {
            final int spanCount = Math.max(mMinimumSpanCount,
                    Math.round((float) getMeasuredWidth() / mColumnWidth));
            ((GridLayoutManager) getLayoutManager()).setSpanCount(spanCount);
        }
    }

    @Override
    public void setLayoutManager(@Nullable RecyclerView.LayoutManager layoutManager) {
        super.setLayoutManager(layoutManager);
        mIsGridLayout = (layoutManager instanceof GridLayoutManager);
    }

    public void setColumnWidth(int columnWidth) {
        mColumnWidth = columnWidth;
    }

    /**
     * Set the minimum span count for the recyclerView.
     * @param minimumSpanCount The default value is 2.
     */
    public void setMinimumSpanCount(int minimumSpanCount) {
        mMinimumSpanCount = minimumSpanCount;
    }

    @Override
    public View focusSearch(View focused, int direction) {
        if (isGridScrollingDirection(direction)) {
            View nextFocus = super.focusSearch(focused, direction);
            if (nextFocus != null && nextFocus != focused) {
                if (!isViewFullyVisible(nextFocus)) {
                    int scrollAmount = getScrollAmountForDirection(direction);
                    if (scrollAmount != 0) {
                        mPendingFocusSearch = true;
                        mPendingFocusedView = focused;
                        mPendingDirection = direction;
                        smoothScrollBy(0, scrollAmount);
                        mHandler.postDelayed(() -> {
                            if (mPendingFocusSearch) {
                                mPendingFocusSearch = false;
                                View v = super.focusSearch(mPendingFocusedView, mPendingDirection);
                                if (v != null && v != mPendingFocusedView) {
                                    v.requestFocus();
                                }
                            }
                        }, 100);
                        return focused;
                    }
                }
            }
            return nextFocus;
        }
        return super.focusSearch(focused, direction);
    }

    private boolean isViewFullyVisible(View view) {
        if (view == null) return false;
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewTop = location[1];
        int viewBottom = viewTop + view.getHeight();
        int[] recyclerLocation = new int[2];
        getLocationOnScreen(recyclerLocation);
        int recyclerTop = recyclerLocation[1];
        int recyclerBottom = recyclerTop + getHeight();
        return viewTop >= recyclerTop && viewBottom <= recyclerBottom;
    }

    private boolean isGridScrollingDirection(int direction) {
        return direction == FOCUS_DOWN || direction == FOCUS_UP;
    }

    private int getScrollAmountForDirection(int direction) {
        if (getChildCount() == 0) {
            return 0;
        }

        View lastVisibleChild = getChildAt(getChildCount() - 1);
        View firstVisibleChild = getChildAt(0);

        if (lastVisibleChild == null || firstVisibleChild == null) {
            return 0;
        }

        int itemHeight = lastVisibleChild.getHeight();
        int spacing = 0;
        try {
            spacing = getResources().getDimensionPixelSize(
                    com.android.providers.media.R.dimen.picker_album_item_spacing);
        } catch (Exception e) {
            try {
                spacing = getResources().getDimensionPixelSize(
                        com.android.providers.media.R.dimen.picker_photo_item_spacing);
            } catch (Exception e2) {
                spacing = 16;
            }
        }

        if (direction == FOCUS_DOWN) {
            return itemHeight + spacing;
        } else if (direction == FOCUS_UP) {
            return -(itemHeight + spacing);
        }
        return 0;
    }
}
