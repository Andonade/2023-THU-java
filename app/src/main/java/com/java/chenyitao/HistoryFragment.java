package com.java.chenyitao;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
public class HistoryFragment extends Fragment {

    private CardView viewed_history_cardview, stared_history_cardview;
    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_history, container, false);

        viewed_history_cardview = view.findViewById(R.id.viewedHistoryCardView);
        stared_history_cardview = view.findViewById(R.id.staredHistoryCardView);

        viewed_history_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ViewedActivity.class));
            }
        });

        stared_history_cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), StaredActivity.class));
            }
        });

        return view;
    }
}