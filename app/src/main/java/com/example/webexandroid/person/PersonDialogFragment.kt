package com.example.webexandroid.person

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import com.example.webexandroid.databinding.FragmentDialogPersonBinding
import com.example.webexandroid.utils.Constants
import org.koin.android.ext.android.inject

class PersonDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(personId: String) : PersonDialogFragment {
            val args = Bundle()
            args.putString(Constants.Bundle.PERSON_ID, personId)

            val fragment = PersonDialogFragment()
            fragment.arguments = args

            return fragment
        }
    }

    private val personViewModel : PersonViewModel by inject()
    private lateinit var personId : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        personId = arguments?.getString(Constants.Bundle.PERSON_ID) ?: ""

        return FragmentDialogPersonBinding.inflate(inflater, container, false)
                .apply {
                    progressLayout.visibility = View.VISIBLE

                    personViewModel.person.observe(this@PersonDialogFragment, Observer { model ->
                        model?.let {
                            progressLayout.visibility = View.GONE
                            person = it
                        }
                    })

                    dialogOk.setOnClickListener { dismiss() }
                }.root
    }

    override fun onResume() {
        super.onResume()
        if(personId.isEmpty()) {
            personViewModel.getMe()
        } else {
            personViewModel.getPersonDetail(personId)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}