package com.llui.idrink.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.llui.idrink.R;
/**
 * InstructionFragment2 displays the second instruction fragment in the ViewPager
 * of the InstructionActivity.
 * This fragment inflates a layout defined in fragment_instruction_2.xml.
 */
public class InstructionFragment2 extends Fragment {

    public InstructionFragment2() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_instruction_2, container, false);
    }
}
