package com.ipostu.androidjettycontainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation


class FirstFragment : Fragment() {

    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_first, container, false)

        textView = view.findViewById(R.id.textView1)
        textView.setOnClickListener {
            val action = FirstFragmentDirections.navigateToSecondFragment()
                .setNum(99)
            Navigation.findNavController(view).navigate(action)
        }

        return view
    }

}