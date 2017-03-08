package com.foreseer.dynamicuilist;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements RateFragment.OnFragmentInteractionListener{

    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //RateFragment fragment = (RateFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_rate);
//        RateFragment fragment = (RateFragment) getSupportFragmentManager().findFragmentByTag("RateFragment"+0);
//        fragment.changeCurrency("USD", "EUR", 1.2);
        count = 1;

    }

    public void addItem(View view){

    }

    public void addFragment(View view){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RateFragment rateFragment = new RateFragment();
        fragmentTransaction.add(R.id.linearLayout, rateFragment, "RateFragment"+count);
        fragmentTransaction.commit();
        count++;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
