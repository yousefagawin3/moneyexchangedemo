package com.example.moneyexchangedemo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyexchangedemo.databinding.ListItemRateBinding
import com.example.moneyexchangedemo.model.CurrencyRate

class RecycleViewCountryCodeAdapter(
    private val currencyRates: ArrayList<CurrencyRate>,
    private val clickListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return currencyRates.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewItem = currencyRates[position]
        viewItem.let {
            (holder as ViewHolderMain).apply {
                bind(clickListener, viewItem)
                itemView.tag = viewItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderMain(
            ListItemRateBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(listener: ItemClickListener, currencyRate: CurrencyRate)
    }

    class ViewHolderMain(private val binding: ListItemRateBinding) :
        ViewHolder(binding.root) {

        override fun bind(listener: ItemClickListener, currencyRate: CurrencyRate) {

            binding.currencyTv.text = currencyRate.currency
            binding.valueTv.text = currencyRate.value.toString()

            itemView.setOnClickListener {
                listener.itemClicked(currencyRate)
            }
        }
    }

    interface ItemClickListener {
        fun itemClicked(item: CurrencyRate)
    }
}