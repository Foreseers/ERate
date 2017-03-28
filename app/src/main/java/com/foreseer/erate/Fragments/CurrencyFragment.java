package com.foreseer.erate.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.R;

public class CurrencyFragment extends Fragment {

    private OnCurrencySelection listener;
    private AbstractCurrency currency;

    public CurrencyFragment() {
        // Required empty public constructor
    }

    public void setCurrency(AbstractCurrency currency) {
        this.currency = currency;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_currency, container, false);
    }

    @Override
    public void onStart() {
        ImageView imageView = (ImageView) getView().findViewById(R.id.imageView_currency);
        Glide.with(this)
                .load(getResources().getIdentifier(currency.getCurrencyImageName(), "drawable", getActivity().getPackageName()))
                .skipMemoryCache(true)
                .into(imageView);

        TextView textView = (TextView) getView().findViewById(R.id.textView_currency);
        textView.setText(currency.getCurrencyName());

        super.onStart();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onClicked(View view) {
        if (listener != null) {
            listener.onCurrencySelection(currency);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCurrencySelection) {
            listener = (OnCurrencySelection) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnCurrencySelection");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }


    public interface OnCurrencySelection {
        // TODO: Update argument type and name
        void onCurrencySelection(AbstractCurrency currency);
    }
}
