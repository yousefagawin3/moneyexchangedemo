package com.example.moneyexchangedemo.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyexchangedemo.databinding.ListItemUserBalanceBinding
import com.example.moneyexchangedemo.db.UserBalance

@SuppressLint("SetTextI18n")
class UserBalanceRVAdapter(
    private val userBalance: List<UserBalance>,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int {
        return userBalance.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewItem = userBalance[position]
        viewItem.let {
            (holder as ViewHolderMain).apply {
                bind(viewItem)
                itemView.tag = viewItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolderMain(
            ListItemUserBalanceBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    abstract class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(userBalance: UserBalance)
    }

    class ViewHolderMain(private val binding: ListItemUserBalanceBinding) :
        ViewHolder(binding.root) {

        override fun bind(userBalance: UserBalance) {

            binding.amountTv.text = "%.2f".format(userBalance.amount) + " " + userBalance.currency
        }
    }
}