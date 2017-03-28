package com.foreseer.erate.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.ColorUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.R;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class AddDialog extends DialogFragment {

    final String TAG = "addDialog";
    private String previousSelection = "";

    private ArrayList<String> currencies;

    private onDialogInteractionListener listener;

    public AddDialog() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_dialog, container, false);
    }

    @Override
    public void onStart() {
        initializeWheels();



        Button addButton = (Button) getView().findViewById(R.id.button_add);
        addButton.setOnClickListener(v -> {
            listener.onAddFragment();
            dismiss();
        });

        Button cancelButton = (Button) getView().findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(v -> {
            listener.onCancel();
            dismiss();
        });
        super.onStart();
    }

    public void onAddButtonPressed(View view) {
        if (listener != null) {
            listener.onAddFragment();
        }
        dismiss();
    }

    private void initializeWheels(){
        currencies = new ArrayList<>();
        for (AbstractCurrency currency : CurrencyHelper.getCurrencyList()){
            currencies.add(currency.getCurrencyName() + " (" + currency.getCurrencyCode() + ")");
        }
        Collections.sort(currencies);

        WheelView wheelView = (WheelView) getView().findViewById(R.id.wheelview_firstCurrency);
        wheelView.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        wheelView.setSkin(WheelView.Skin.Holo);

        ArrayList<String> initial = new ArrayList<>();
        initial.addAll(currencies);

        wheelView.setWheelData(initial);

        WheelView wheelView2 = (WheelView) getView().findViewById(R.id.wheelview_secondCurrency);
        wheelView2.setWheelAdapter(new ArrayWheelAdapter(getContext()));
        wheelView2.setSkin(WheelView.Skin.Holo);
        wheelView2.setWheelData(currencies);

        previousSelection = wheelView.getSelectionItem().toString();
        currencies.remove(previousSelection);
        wheelView2.setWheelData(currencies);

        wheelView.setOnWheelItemSelectedListener((position, o) -> {
            String newSelection = o.toString();
            currencies.add(previousSelection);
            previousSelection = newSelection;
            currencies.remove(previousSelection);
            Collections.sort(currencies);
            wheelView2.setWheelData(currencies);
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        listener.onCancel();
        super.onCancel(dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onDialogInteractionListener) {
            listener = (onDialogInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onDialogInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface onDialogInteractionListener {
        void onCancel();
        void onAddFragment();
    }

    private void unbindDrawables(View view){
        if (view.getBackground() != null){
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)){
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++){
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
