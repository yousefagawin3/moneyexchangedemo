package com.example.moneyexchangedemo.ui.search

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneyexchangedemo.databinding.DialogSelectRateBinding
import com.example.moneyexchangedemo.model.CurrencyRate
import com.example.moneyexchangedemo.ui.RecycleViewCountryCodeAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class SearchDialogHelper : DialogFragment() {

    private var binding: DialogSelectRateBinding? = null
    private var mListener: SearchDialogListener? = null

    private var ratesList: ArrayList<CurrencyRate>? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mListener =  context as SearchDialogListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSelectRateBinding.inflate(LayoutInflater.from(context))

        ratesList = arguments?.getParcelableArrayList("ratesList")

        binding?.closeBtn?.setOnClickListener {
            dismiss()
        }

        binding?.searchEt?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val filteredObjs = ArrayList<CurrencyRate>()

                for (rate in ratesList!!) {
                    //checks if the currency contains the entered text string
                    if (rate.currency.lowercase(Locale.getDefault())
                            .contains(s.toString().lowercase(Locale.getDefault()))
                    ) {
                        filteredObjs.add(rate)
                    }
                }

                val adapter = RecycleViewCountryCodeAdapter(
                    filteredObjs,
                    object : RecycleViewCountryCodeAdapter.ItemClickListener {
                        override fun itemClicked(item: CurrencyRate) {
                            mListener?.onRateSelected(item)
                            dismiss()
                        }
                    })
                binding?.ratesRv?.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        })

        binding?.ratesRv?.layoutManager = LinearLayoutManager(context)
        binding?.ratesRv?.adapter = RecycleViewCountryCodeAdapter(
            ratesList!!,
            object : RecycleViewCountryCodeAdapter.ItemClickListener {
                override fun itemClicked(item: CurrencyRate) {
                    mListener?.onRateSelected(item)
                    dismiss()
                }
            })

        return AlertDialog.Builder(requireActivity())
            .setView(binding?.root)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    interface SearchDialogListener {
        fun onRateSelected(item: CurrencyRate)
    }
}