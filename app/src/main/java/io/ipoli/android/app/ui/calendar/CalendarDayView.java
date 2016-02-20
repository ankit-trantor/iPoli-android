package io.ipoli.android.app.ui.calendar;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.ipoli.android.R;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 2/6/16.
 */
public class CalendarDayView extends FrameLayout {

    public static final int HOURS_PER_SCREEN = 5;
    public static final int HOURS_IN_A_DAY = 24;

    private float minuteHeight;
    private RelativeLayout eventsContainer;
    private int hourHeight;
    private ObservableScrollView scrollView;
    private View timeLine;

    private BaseCalendarAdapter adapter;

    public CalendarDayView(Context context) {
        super(context);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CalendarDayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    public CalendarDayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (!isInEditMode()) {
            initUI(context);
        }
    }


    public CalendarDayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            initUI(context);
        }
    }

    private void initUI(Context context) {

        hourHeight = getScreenHeight(context) / HOURS_PER_SCREEN;
        minuteHeight = hourHeight / 60.0f;

        LayoutInflater inflater = LayoutInflater.from(context);

        FrameLayout mainContainer = initMainContainer(context);
        mainContainer.addView(initHourCellsContainer(context, inflater));
        mainContainer.addView(initEventsContainer(context));
        mainContainer.addView(initTimeLineContainer(context, inflater));

        addView(initScrollView(context, mainContainer));
    }

    private ObservableScrollView initScrollView(Context context, FrameLayout mainContainer) {
        scrollView = new ObservableScrollView(context);
        scrollView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        scrollView.setVerticalScrollBarEnabled(true);
        scrollView.addView(mainContainer);
        return scrollView;
    }

    @NonNull
    private FrameLayout initMainContainer(Context context) {
        FrameLayout mainContainer = new FrameLayout(context);
        mainContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return mainContainer;
    }

    @NonNull
    private LinearLayout initHourCellsContainer(Context context, LayoutInflater inflater) {
        LinearLayout hourCellsContainer = new LinearLayout(context);
        hourCellsContainer.setOrientation(LinearLayout.VERTICAL);
        hourCellsContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        for (int i = 0; i < HOURS_IN_A_DAY; i++) {
            hourCellsContainer.addView(initHourCell(i, hourCellsContainer, inflater));
        }
        return hourCellsContainer;
    }

    @NonNull
    private View initHourCell(int hour, LinearLayout hourCellsContainer, LayoutInflater inflater) {
        View hourCell = inflater.inflate(R.layout.calendar_hour_cell, hourCellsContainer, false);
        if (hour > 0) {
            TextView tv = (TextView) hourCell.findViewById(R.id.time_label);
            tv.setText(String.format(Locale.getDefault(), "%02d:00", hour));
        }
        ViewGroup.LayoutParams hcp = hourCell.getLayoutParams();
        hcp.height = hourHeight;
        hourCell.setLayoutParams(hcp);
        return hourCell;
    }

    private RelativeLayout initEventsContainer(Context context) {
        eventsContainer = new RelativeLayout(context);
        eventsContainer.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return eventsContainer;
    }

    @NonNull
    private RelativeLayout initTimeLineContainer(Context context, LayoutInflater inflater) {
        RelativeLayout timeRL = new RelativeLayout(context);
        timeRL.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        timeLine = inflater.inflate(R.layout.calendar_time_line, timeRL, false);

        RelativeLayout.LayoutParams lPTime = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();
        lPTime.topMargin = getCurrentTimeYPosition();
        timeRL.addView(timeLine, lPTime);
        return timeRL;
    }

    private int getScreenHeight(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        return metrics.heightPixels;
    }

    public void setAdapter(BaseCalendarAdapter<? extends CalendarEvent> adapter) {
        this.adapter = adapter;
        adapter.setCalendarDayView(this);
        adapter.notifyDataSetChanged();
    }

    <E extends CalendarEvent> void addEvent(final E calendarEvent, int position) {
        final View eventView = adapter.getView(eventsContainer, position);
        RelativeLayout.LayoutParams qlp = (RelativeLayout.LayoutParams) eventView.getLayoutParams();
        Calendar c = Calendar.getInstance();
        c.setTime(calendarEvent.getStartTime());
        qlp.topMargin = getYPositionFor(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        qlp.height = getHeightFor(calendarEvent.getDuration());
        eventsContainer.addView(eventView, qlp);

        final GestureDetectorCompat gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent moveEvent, float v, float v1) {
                int h = getHoursFor(moveEvent.getRawY() - getHeightFor(getDurationOffset(calendarEvent.getDuration())));
                int m = getMinutesFor(moveEvent.getRawY() - getHeightFor(getDurationOffset(calendarEvent.getDuration())), 5);

                int touchYPos = getYPositionFor(h, m);

                RelativeLayout.LayoutParams qlp = (RelativeLayout.LayoutParams) eventView.getLayoutParams();
                qlp.topMargin = touchYPos;
                eventView.setLayoutParams(qlp);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, h);
                c.set(Calendar.MINUTE, m);
                eventView.setTag(c.getTime());

                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                eventView.callOnClick();
                return true;
            }

        });

        eventView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {

                switch (MotionEventCompat.getActionMasked(event)) {

                    case MotionEvent.ACTION_DOWN:
                        eventView.setTag(null);
                        scrollView.setEnableScrolling(false);
                        break;

                    case MotionEvent.ACTION_UP:
                        if (eventView.getTag() != null) {
                            Date oldStartTime = calendarEvent.getStartTime();
                            calendarEvent.setStartTime((Date) eventView.getTag());
                            adapter.onStartTimeUpdated(calendarEvent, oldStartTime);
                        }
                        scrollView.setEnableScrolling(true);
                        break;
                }
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    public void onMinuteChanged() {

        RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams) timeLine.getLayoutParams();
        p.topMargin = getCurrentTimeYPosition();
        timeLine.setLayoutParams(p);
        // @TODO consider date change
    }

    int getHoursFor(float y) {
        float h = toLocalY((int) y) / hourHeight;
        return Math.min(Math.round(h), 23);
    }

    int getMinutesFor(float y, int rangeLength) {
        int minutes = (int) ((toLocalY((int) y) % hourHeight) / minuteHeight);
        minutes = Math.max(0, minutes);
        List<Integer> bounds = new ArrayList<>();
        int rangeStart = 0;
        for (int min = 0; min < 60; min++) {

            if (min % rangeLength == 0) {
                rangeStart = min;
            }
            bounds.add(rangeStart);
        }
        return bounds.get(minutes);
    }

    private int getDurationOffset(int duration) {
        int mid = duration / 2;
        int remainder = mid % 5;
        return mid + (5 - remainder);
    }

    int getYPositionFor(int hours, int minutes) {
        int y = hours * hourHeight;
        y += getMinutesHeight(minutes);
        return y;
    }

    public int getHeightFor(int duration) {
        return (int) getMinutesHeight(duration);
    }

    private float getMinutesHeight(int minutes) {
        return minuteHeight * minutes;
    }


    public int getCurrentTimeYPosition() {
        return getCurrentTimeYPosition(0);
    }

    public int getCurrentTimeYPosition(int hourOffset) {
        Calendar c = Calendar.getInstance();
        int hour = Math.max(0, c.get(Calendar.HOUR_OF_DAY) - hourOffset);
        int minutes = c.get(Calendar.MINUTE);
        return getYPositionFor(hour, minutes);
    }

    private int toLocalY(int y) {
        int offsets[] = new int[2];
        getLocationOnScreen(offsets);
        return scrollView.getScrollY() + y - offsets[1];
    }

    public void scrollToNow() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int pHours = 2;
                hour = Math.max(0, hour - pHours);
                if (hour == 0) {
                    scrollView.scrollTo(scrollView.getScrollX(), 0);
                } else {
                    int minutes = c.get(Calendar.MINUTE);
                    scrollView.scrollTo(scrollView.getScrollX(), getYPositionFor(hour, minutes));
                }
            }
        });
    }

    public void removeAllEvents() {
        eventsContainer.removeAllViews();
    }
}