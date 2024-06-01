package com.ipostu.androidjettycontainer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs

class SecondFragment : Fragment() {
    private lateinit var textView: TextView
    private val args: SecondFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_second, container, false)
        val num = args.num

        textView = view.findViewById(R.id.textView1)
        textView.setText(textView.text.toString() + " - " + num.toString())
        textView.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.navigateToFirstFragment)
        }

        return view
    }
}