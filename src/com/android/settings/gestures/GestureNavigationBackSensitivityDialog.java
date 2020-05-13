/*
 * Copyright (C) 2019 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.settings.gestures;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.settings.SettingsEnums;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.ServiceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;

import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

/**
 * Dialog to set the back gesture's sensitivity in Gesture navigation mode.
 */
public class GestureNavigationBackSensitivityDialog extends InstrumentedDialogFragment {

    private boolean mArrowSwitchChecked;
    private boolean mGestureHapticChecked;
    private boolean mBlockImeChecked;

    private static final String TAG = "GestureNavigationBackSensitivityDialog";
    private static final String KEY_BACK_SENSITIVITY = "back_sensitivity";
    private static final String KEY_BACK_HEIGHT = "back_height";
    private static final String KEY_BACK_BLOCK_IME = "back_block_ime";
    private static final String KEY_HOME_HANDLE_SIZE = "home_handle_width";

    public static void show(SystemNavigationGestureSettings parent, int sensitivity, int height,
            boolean blockIme, int length) {
        if (!parent.isAdded()) {
            return;
        }

        final GestureNavigationBackSensitivityDialog dialog =
                new GestureNavigationBackSensitivityDialog();
        final Bundle bundle = new Bundle();
        bundle.putInt(KEY_BACK_SENSITIVITY, sensitivity);
        bundle.putInt(KEY_BACK_HEIGHT, height);
        bundle.putBoolean(KEY_BACK_BLOCK_IME, blockIme);
        bundle.putInt(KEY_HOME_HANDLE_SIZE, length);
        dialog.setArguments(bundle);
        dialog.setTargetFragment(parent, 0);
        dialog.show(parent.getFragmentManager(), TAG);
    }

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_GESTURE_NAV_BACK_SENSITIVITY_DLG;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(
                R.layout.dialog_back_gesture_options, null);
        final SeekBar seekBarSensitivity = view.findViewById(R.id.back_sensitivity_seekbar);
        seekBarSensitivity.setProgress(getArguments().getInt(KEY_BACK_SENSITIVITY));
        final SeekBar seekBarHeight = view.findViewById(R.id.back_height_seekbar);
        seekBarHeight.setProgress(getArguments().getInt(KEY_BACK_HEIGHT));
        final SeekBar seekBarHandleSize = view.findViewById(R.id.home_handle_seekbar);
        seekBarHandleSize.setProgress(getArguments().getInt(KEY_HOME_HANDLE_SIZE));
        final Switch arrowSwitch = view.findViewById(R.id.back_arrow_gesture_switch);
        mArrowSwitchChecked = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.SHOW_BACK_ARROW_GESTURE, 1) == 1;
        arrowSwitch.setChecked(mArrowSwitchChecked);
        arrowSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrowSwitchChecked = arrowSwitch.isChecked() ? true : false;
            }
        });
        final Switch GestureHapticSwitch = view.findViewById(R.id.back_gesture_haptic);
        mGestureHapticChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BACK_GESTURE_HAPTIC, 0) == 1;
        GestureHapticSwitch.setChecked(mGestureHapticChecked);
        GestureHapticSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGestureHapticChecked = GestureHapticSwitch.isChecked() ? true : false;
            }
        });
        final Switch blockImeSwitch = view.findViewById(R.id.back_block_ime);
        mBlockImeChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BACK_GESTURE_BLOCK_IME, 0) == 1;
        blockImeSwitch.setChecked(mBlockImeChecked);
        blockImeSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBlockImeChecked = blockImeSwitch.isChecked() ? true : false;
            }
        });

        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.back_options_dialog_title)
                .setMessage(R.string.back_sensitivity_dialog_message)
                .setView(view)
                .setPositiveButton(R.string.okay, (dialog, which) -> {
                    int sensitivity = seekBarSensitivity.getProgress();
                    getArguments().putInt(KEY_BACK_SENSITIVITY, sensitivity);
                    int height = seekBarHeight.getProgress();
                    getArguments().putInt(KEY_BACK_HEIGHT, height);
                    boolean blockIme = blockImeSwitch.isChecked();
                    getArguments().putBoolean(KEY_BACK_BLOCK_IME, blockIme);
                    int length = seekBarHandleSize.getProgress();
                    getArguments().putInt(KEY_HOME_HANDLE_SIZE, length);
                    Settings.Secure.putInt(getActivity().getContentResolver(),
                            Settings.Secure.SHOW_BACK_ARROW_GESTURE, mArrowSwitchChecked ? 1 : 0);
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.BACK_GESTURE_HAPTIC, mGestureHapticChecked ? 1 : 0);
                    Settings.System.putInt(getActivity().getContentResolver(),
                            Settings.System.BACK_GESTURE_BLOCK_IME, mBlockImeChecked ? 1 : 0);
                    SystemNavigationGestureSettings.setBackHeight(getActivity(), height);
                    SystemNavigationGestureSettings.setBackSensitivity(getActivity(),
                            getOverlayManager(), sensitivity);
                    SystemNavigationGestureSettings.setBackBlockIme(getActivity(), blockIme);
                    SystemNavigationGestureSettings.setHomeHandleSize(getActivity(), length);
                })
                .create();
    }

    private IOverlayManager getOverlayManager() {
        return IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
    }
}
