package io.ipoli.android.app.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.SparseBooleanArray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.ipoli.android.R;
import io.ipoli.android.app.TimeOfDay;
import io.ipoli.android.app.utils.StringUtils;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 6/17/16.
 */
public class TimeOfDayPickerFragment extends DialogFragment {

    private static final String TAG = "time-of-day-picker-dialog";
    private static final String SELECTED_TIMES = "selected_times";
    private static final String TITLE = "title";

    private OnTimesOfDayPickedListener timesOfDayPickedListener;

    private List<String> preSelectedTimes;
    private AlertDialog alertDialog;

    @StringRes
    private int title;

    public static TimeOfDayPickerFragment newInstance(@StringRes int title, OnTimesOfDayPickedListener listener) {
        return newInstance(title, new HashSet<>(), listener);
    }

    public static TimeOfDayPickerFragment newInstance(@StringRes int title, Set<TimeOfDay> selectedTimesOfDay, OnTimesOfDayPickedListener listener) {
        TimeOfDayPickerFragment fragment = new TimeOfDayPickerFragment();
        Bundle args = new Bundle();
        ArrayList<String> selectedTimes = new ArrayList<>();
        for (TimeOfDay timeOfDay : selectedTimesOfDay) {
            selectedTimes.add(timeOfDay.name());
        }
        args.putStringArrayList(SELECTED_TIMES, selectedTimes);
        args.putInt(TITLE, title);
        fragment.setArguments(args);
        fragment.timesOfDayPickedListener = listener;
        return fragment;
    }

    public interface OnTimesOfDayPickedListener {
        void onTimesOfDayPicked(Set<TimeOfDay> selectedTimes);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preSelectedTimes = getArguments().getStringArrayList(SELECTED_TIMES);
        title = getArguments().getInt(TITLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        boolean[] checkedDays = new boolean[TimeOfDay.values().length];
        String[] timesOfDay = new String[TimeOfDay.values().length];

        for (int i = 0; i < TimeOfDay.values().length; i++) {
            TimeOfDay timeOfDay = TimeOfDay.values()[i];
            if (preSelectedTimes.contains(timeOfDay.name())) {
                checkedDays[i] = true;
            }
            timesOfDay[i] = getString(TimeOfDay.getNameRes(timeOfDay));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(R.drawable.logo)
                .setMultiChoiceItems(timesOfDay, checkedDays, null)
                .setTitle(title)
                .setPositiveButton(R.string.help_dialog_ok, (dialog, which) -> {
                    SparseBooleanArray selectedPositions = alertDialog.getListView().getCheckedItemPositions();
                    Set<TimeOfDay> selectedTimes = new HashSet<>();
                    for (int i = 0; i < alertDialog.getListView().getAdapter().getCount(); i++) {
                        if (selectedPositions.get(i)) {
                            selectedTimes.add(TimeOfDay.values()[i]);
                        }
                    }
                    timesOfDayPickedListener.onTimesOfDayPicked(selectedTimes);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {

                });
        alertDialog = builder.create();
        return alertDialog;
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }
}
