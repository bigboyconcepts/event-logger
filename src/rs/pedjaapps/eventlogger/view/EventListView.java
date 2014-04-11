/*
 * Copyright (C) 2008 The Android Open Source Project
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

package rs.pedjaapps.eventlogger.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public final class EventListView extends ListView
{
    private OnSizeChangedListener mOnSizeChangedListener;

    public EventListView(Context context)
    {
        super(context);
    }

    public EventListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mOnSizeChangedListener != null)
        {
            mOnSizeChangedListener.onSizeChanged(w, h, oldw, oldh);
        }
    }

    /**
     * Set the listener which will be triggered when the size of
     * the view is changed.
     */
    public void setOnSizeChangedListener(OnSizeChangedListener l)
    {
        mOnSizeChangedListener = l;
    }

    public interface OnSizeChangedListener
    {
        void onSizeChanged(int width, int height, int oldWidth, int oldHeight);
    }
}
