package com.foreseer.erate.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.Currency.CurrencyHelper;
import com.foreseer.erate.R;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import io.blackbox_vision.wheelview.view.WheelView;


public class AddDialog extends DialogFragment {

    final String TAG = "addDialog";

    private String firstSelected;
    private String secondSelected;

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
        firstSelected = "";
        secondSelected = "";

        initializeWheels();

        super.onStart();
    }

    private void initializeWheels(){
        currencies = new ArrayList<>();
        for (AbstractCurrency currency : CurrencyHelper.getCurrencyList()){
            currencies.add(currency.getCurrencyName() + " (" + currency.getCurrencyCode() + ")");
        }
        Collections.sort(currencies);

        ArrayList<String> initial = new ArrayList<>();
        initial.addAll(currencies);

        WheelView wheelView = (WheelView) getView().findViewById(R.id.wheelview_firstCurrency);
        wheelView.setTextSize(17);
        wheelView.setItems(currencies);
        wheelView.setCanLoop(false);
        wheelView.setInitPosition(2);
        initializeDoubleTapDetectors();

    }

    private void initializeDoubleTapDetectors(){
        WheelView wheelView = (WheelView) getView().findViewById(R.id.wheelview_firstCurrency);
        final GestureDetector wheelDoubleTapDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (firstSelected.equals("")){
                    int index = wheelView.getSelectedItem();
                    firstSelected = currencies.get(index);
                    currencies.remove(index);
                    ((TextView) getView().findViewById(R.id.textView_firstCurrency)).setText("Selected: " + firstSelected);
                    wheelView.setItems(currencies);
                } else {
                    int index = wheelView.getSelectedItem();
                    secondSelected = currencies.get(index);
                    finishAddition();
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return super.onDoubleTapEvent(e);
            }
        });
        wheelView.setOnTouchListener((v, event) -> wheelDoubleTapDetector.onTouchEvent(event));

        TextView textView = (TextView) getView().findViewById(R.id.textView_firstCurrency);
        final GestureDetector currencyTextViewDoubleTapDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!firstSelected.equals("")) {
                    currencies.add(firstSelected);
                    Collections.sort(currencies);
                    firstSelected = "";
                    wheelView.setItems(currencies);
                    textView.setText("Selected: ");
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return super.onDoubleTapEvent(e);
            }
        });

        textView.setOnTouchListener((v, event) -> currencyTextViewDoubleTapDetector.onTouchEvent(event));



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

    private void finishAddition(){
        String first = firstSelected.split("\\(")[0].trim();
        String second = secondSelected.split("\\(")[0].trim();
        listener.onAddFragment(CurrencyHelper.getCurrencyByName(first), CurrencyHelper.getCurrencyByName(second));
        dismiss();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface onDialogInteractionListener {
        void onAddFragment(AbstractCurrency first, AbstractCurrency second);
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
