package com.quadcoder.coinpet.page.tutorial;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.quadcoder.coinpet.R;
import com.quadcoder.coinpet.page.signup.SignupActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class TutorialThirdFragment extends Fragment {


    public TutorialThirdFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tutorial_third, container, false);

        Button btn = (Button)rootView.findViewById(R.id.btnNext);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SignupActivity.class));
                getActivity().finish();
            }
        });

        return rootView;
    }


}