package com.example.travel.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.travel.MainActivity
import com.example.travel.R
import com.example.travel.`interface`.OnBottomSheetCallbacks
import com.example.travel.adapters.PlacesAdapter
import com.example.travel.helpers.DatabaseHandler
import com.example.travel.models.PlacesModelClass
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_window.*

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

        textResult.setOnClickListener {
            (activity as MainActivity).openBottomSheet()
        }

        filterImage.setOnClickListener {
            if (currentState == BottomSheetBehavior.STATE_EXPANDED) {
                (activity as MainActivity).closeBottomSheet()
            } else  {
                (activity as MainActivity).openBottomSheet()
            }
        }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        currentState = newState
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                textResult.text = "0 results"
                filterImage.setImageResource(R.drawable.ic_baseline_filter_list_24)
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                textResult.text = "See the results"
                filterImage.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            }
        }
    }
}