package com.foreseer.erate.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.foreseer.erate.BuildConfig;
import com.foreseer.erate.Currency.AbstractCurrency;
import com.foreseer.erate.R;
import com.foreseer.erate.SQL.FragmentTableHandler;


public class RateFragment extends Fragment {

    private int sqlID;

    private double firstToSecondRate;
    private double secondToFirstRate;

    private AbstractCurrency firstCurrency;
    private AbstractCurrency secondCurrency;

    private double rate;

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

    public int getSqlID() {
        return sqlID;
    }

    public void setSqlID(int sqlID) {
        this.sqlID = sqlID;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement onDialogInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void changeCurrency(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency, double rate) {
        TextView firstCurrencyTextView = (TextView) getView().findViewById(R.id.textView_firstCurrency);
        TextView secondCurrencyTextView = (TextView) getView().findViewById(R.id.textView_secondCurrency);
        firstCurrencyTextView.setText(firstCurrency.getCurrencyCode());
        secondCurrencyTextView.setText(secondCurrency.getCurrencyCode());

        TextView firstCurrencyHint = (TextView) getView().findViewById(R.id.textView_firstCurrencyHint);
        TextView secondCurrencyHint = (TextView) getView().findViewById(R.id.textView_secondCurrencyHint);
        firstCurrencyHint.setText(firstCurrency.getCurrencyName());
        secondCurrencyHint.setText(secondCurrency.getCurrencyName());

        ImageView firstCurrencyImage = (ImageView) getView().findViewById(R.id.imageView_firstCurrency);
        ImageView secondCurrencyImage = (ImageView) getView().findViewById(R.id.imageView_secondCurrency);
        String firstDrawable = firstCurrency.getCurrencyImageName();
        String secondDrawable = secondCurrency.getCurrencyImageName();

        /*firstCurrencyImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), getResources()
                .getIdentifier(firstDrawable, "drawable", getActivity().getPackageName())));
        secondCurrencyImage.setImageDrawable(ContextCompat.getDrawable(getActivity(), getResources()
                .getIdentifier(secondDrawable, "drawable", BuildConfig.APPLICATION_ID)));*/
        Glide.with(this)
                .load(getResources().getIdentifier(firstDrawable, "drawable", getActivity().getPackageName()))
                .into(firstCurrencyImage);
        Glide.with(this)
                .load(getResources().getIdentifier(secondDrawable, "drawable", getActivity().getPackageName()))
                .into(secondCurrencyImage);


        firstToSecondRate = rate;
        secondToFirstRate = (1 / rate);

        String rateString = "1 = " + String.valueOf(rate).substring(0, 5);
        TextView rateTextView = (TextView) getView().findViewById(R.id.textView_currencyAmount);
        rateTextView.setText(rateString);
    }

    public void swap(View view) {
        setCurrency(secondCurrency, firstCurrency, secondToFirstRate);
        changeCurrency(firstCurrency, secondCurrency, secondToFirstRate);
        FragmentTableHandler handler = FragmentTableHandler.getInstance(getContext());
        handler.swapFragment(sqlID);
    }

    public void setCurrency(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency, double rate) {
        this.firstCurrency = firstCurrency;
        this.secondCurrency = secondCurrency;
        this.rate = rate;
    }

    @Override
    public void onStart() {
        changeCurrency(firstCurrency, secondCurrency, rate);
        ImageButton button = (ImageButton) getView().findViewById(R.id.imageButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onFragmentDeletion(sqlID);
            }
        });

        TextView textView = (TextView) getView().findViewById(R.id.textView_currencyAmount);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swap(v);
            }
        });
        super.onStart();
    }

    public void updateRate(double rate) {
        this.rate = rate;
        class MyRunnable implements Runnable {
            private AbstractCurrency firstCurrency;
            private AbstractCurrency secondCurrency;
            private double rate;

            public MyRunnable(AbstractCurrency firstCurrency, AbstractCurrency secondCurrency, double rate) {
                this.firstCurrency = firstCurrency;
                this.secondCurrency = secondCurrency;
                this.rate = rate;
            }

            @Override
            public void run() {
                changeCurrency(this.firstCurrency, this.secondCurrency, this.rate);
            }
        }
        getActivity().runOnUiThread(new MyRunnable(this.firstCurrency, this.secondCurrency, this.rate));
    }

    public AbstractCurrency getFirstCurrency() {
        return firstCurrency;
    }

    public AbstractCurrency getSecondCurrency() {
        return secondCurrency;
    }

    public double getRate() {
        return rate;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentDeletion(int id);
    }
}
