package com.example.travel.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel.MainActivity
import com.example.travel.R
import com.example.travel.`interface`.OnBottomSheetCallbacks
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WindowFragment : BottomSheetDialogFragment(), OnBottomSheetCallbacks {
    private var currentState: Int = BottomSheetBehavior.STATE_EXPANDED
    var thiscontext: Context? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        thiscontext = container?.getContext();
        // Inflate the layout for this fragment
        (activity as MainActivity).setOnBottomSheetCallbacks(this)

        return inflater.inflate(R.layout.fragment_window, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            if (currentState == BottomSheetBehavior.STATE_EXPANDED) {
                (activity as MainActivity).closeBottomSheet()
            } else  {
                (activity as MainActivity).openBottomSheet()
            }

    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        currentState = newState
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {

            }
            BottomSheetBehavior.STATE_COLLAPSED -> {

            }
        }
    }
}