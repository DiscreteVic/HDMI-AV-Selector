package com.discretevic.avselector;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.tv.TvContract;
import android.media.tv.TvInputInfo;
import android.media.tv.TvInputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Popup listing the TV's physical inputs (HDMI, AV, ...). Picking one asks the
 * system TV app to switch to that passthrough input.
 */
public class InputSelectorActivity extends Activity {

    private TvInputManager tvInputManager;
    private ViewGroup inputsView;
    private View emptyView;

    private final TvInputManager.TvInputCallback inputCallback = new TvInputManager.TvInputCallback() {
        @Override
        public void onInputAdded(String inputId) {
            refresh();
        }

        @Override
        public void onInputRemoved(String inputId) {
            refresh();
        }

        @Override
        public void onInputStateChanged(String inputId, int state) {
            refresh();
        }

        @Override
        public void onInputUpdated(String inputId) {
            refresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_selector);
        inputsView = findViewById(R.id.inputs);
        emptyView = findViewById(R.id.empty);
        tvInputManager = (TvInputManager) getSystemService(TV_INPUT_SERVICE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        refresh();
        if (tvInputManager != null) {
            tvInputManager.registerCallback(inputCallback, new Handler(Looper.getMainLooper()));
        }
    }

    @Override
    protected void onStop() {
        if (tvInputManager != null) {
            tvInputManager.unregisterCallback(inputCallback);
        }
        super.onStop();
    }

    private void refresh() {
        List<TvInputInfo> inputs = loadPhysicalInputs();
        inputsView.removeAllViews();
        emptyView.setVisibility(inputs.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = getLayoutInflater();
        for (TvInputInfo input : inputs) {
            View row = inflater.inflate(R.layout.item_input, inputsView, false);
            CharSequence label = labelFor(input);
            ((TextView) row.findViewById(R.id.label)).setText(label);

            String status = statusFor(input);
            TextView statusView = row.findViewById(R.id.status);
            if (status != null) {
                statusView.setText(status);
                statusView.setVisibility(View.VISIBLE);
            }

            row.setOnClickListener(v -> switchTo(input, label));
            inputsView.addView(row);
        }
        if (inputsView.getChildCount() > 0) {
            inputsView.getChildAt(0).requestFocus();
        }
    }

    /** Top-level hardware inputs only; HDMI-CEC devices hang off their HDMI port as children. */
    private List<TvInputInfo> loadPhysicalInputs() {
        List<TvInputInfo> result = new ArrayList<>();
        if (tvInputManager == null) {
            return result;
        }
        for (TvInputInfo input : tvInputManager.getTvInputList()) {
            if (input.isPassthroughInput()
                    && input.getParentId() == null
                    && !input.isHidden(this)
                    && typeOrder(input.getType()) >= 0) {
                result.add(input);
            }
        }
        result.sort(Comparator
                .comparingInt((TvInputInfo i) -> typeOrder(i.getType()))
                .thenComparing(i -> labelFor(i).toString(), String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    /** Display order of input types; -1 for types that aren't a physical HDMI/AV-style input. */
    private static int typeOrder(int type) {
        switch (type) {
            case TvInputInfo.TYPE_HDMI: return 0;
            case TvInputInfo.TYPE_COMPOSITE: return 1;
            case TvInputInfo.TYPE_COMPONENT: return 2;
            case TvInputInfo.TYPE_SVIDEO: return 3;
            case TvInputInfo.TYPE_SCART: return 4;
            case TvInputInfo.TYPE_VGA: return 5;
            case TvInputInfo.TYPE_DVI: return 6;
            case TvInputInfo.TYPE_DISPLAY_PORT: return 7;
            default: return -1;
        }
    }

    private CharSequence labelFor(TvInputInfo input) {
        CharSequence custom = input.loadCustomLabel(this);
        return TextUtils.isEmpty(custom) ? input.loadLabel(this) : custom;
    }

    private String statusFor(TvInputInfo input) {
        switch (tvInputManager.getInputState(input.getId())) {
            case TvInputManager.INPUT_STATE_DISCONNECTED:
                return getString(R.string.not_connected);
            case TvInputManager.INPUT_STATE_CONNECTED_STANDBY:
                return getString(R.string.standby);
            default:
                return null;
        }
    }

    private void switchTo(TvInputInfo input, CharSequence label) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                TvContract.buildChannelUriForPassthroughInput(input.getId()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
            finish();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.switch_failed, label), Toast.LENGTH_LONG).show();
        }
    }
}
