package com.foreseer.dynamicuilist;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;


public class RateFragment extends Fragment {

    private TextView firstCurrencyTextView;
    private TextView secondCurrencyTextView;

    private double firstToSecondRate;
    private double secondToFirstRate;

    private OnFragmentInteractionListener mListener;

    public RateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rate, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void changeCurrency(String firstCurrency, String secondCurrency, double rate){
        if (firstCurrencyTextView == null) {
            firstCurrencyTextView = (TextView) getView().findViewById(R.id.textView_firstCurrency);
            secondCurrencyTextView = (TextView) getView().findViewById(R.id.textView_secondCurrency);
        }

        firstCurrencyTextView.setText(firstCurrency);
        secondCurrencyTextView.setText(secondCurrency);
        firstToSecondRate = rate;
        secondToFirstRate = (1 / rate);

        String rateString = "1 = " + String.valueOf(rate);
        TextView rateTextView = (TextView) getView().findViewById(R.id.textView_currencyAmount);
        rateTextView.setText(rateString);
    }
}
