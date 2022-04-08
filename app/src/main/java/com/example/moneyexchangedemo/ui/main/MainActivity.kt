package com.example.moneyexchangedemo.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneyexchangedemo.R
import com.example.moneyexchangedemo.databinding.ActivityMainBinding
import com.example.moneyexchangedemo.model.CurrencyRate
import com.example.moneyexchangedemo.ui.main.MainViewModel.Companion.COMMISSION_FREE_LIMIT
import com.example.moneyexchangedemo.ui.main.MainViewModel.Companion.COMMISSION_RATE
import com.example.moneyexchangedemo.ui.search.SearchDialogHelper
import com.example.moneyexchangedemo.util.showSnackbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
    SearchDialogHelper.SearchDialogListener {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onRateSelected(item: CurrencyRate) {
        viewModel.onRateSelected(item)
        binding.sellEt.setText("")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpGradientBGColor()

        binding.apply {
            sellCurrencyTv.setOnClickListener {
                viewModel.rateDestination = MainViewModel.RateDestination.SELL
                if (viewModel.allUserBalance.value?.size == 1) {
                    showSnackbar(
                        message = getString(R.string.only_have_one_currency),
                        view = binding.root
                    )
                } else {
                    proceedToSearchUI(
                        ArrayList(
                            viewModel.allUserBalance.value?.flatMap { userBalance ->
                                viewModel.allRatesState.value!!.filter {
                                    userBalance.currency == it.currency
                                }
                            }
                        )
                    )
                }
            }

            recvCurrencyTv.setOnClickListener {
                viewModel.rateDestination = MainViewModel.RateDestination.RECEIVE
                proceedToSearchUI(
                    ArrayList(
                        viewModel.allRatesState.value?.filter {
                            it.currency != viewModel.recvCurrencyStateFlow.value.currency
                        }
                    )
                )
            }

            userBalanceRv.apply {
                layoutManager =
                    LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
                setHasFixedSize(true)
                itemAnimator?.changeDuration = 100
            }

            sellEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    try{
                        //computes value
                        if (s.toString().isNotBlank()){
                            if(viewModel.isInputValueLessThanBalance(s.toString().toDouble())) {
                                viewModel.computeRecvAmount(s.toString().toDouble())
                            } else {
                                showSnackbar(
                                    message = getString(R.string.cannot_input_greater_value),
                                    view = binding.root
                                )

                                sellEt.setText("")
                                viewModel.computeRecvAmount(0.0)
                            }
                        } else {
                            viewModel.computeRecvAmount(0.0)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

            binding.submitBtn.setOnClickListener {

                if(binding.sellEt.text.isNullOrBlank() || binding.sellEt.text.equals("0"))
                    return@setOnClickListener

                if(viewModel.userCommissionRate.value?.transactions_done?:0 >= COMMISSION_FREE_LIMIT){
                    if(!viewModel.isInputValueLessThanBalance(viewModel.sellAmount.value + (viewModel.sellAmount.value* COMMISSION_RATE))) {
                        showSnackbar(
                            message = "Please consider paying ${viewModel.sellAmount.value* COMMISSION_RATE} ${viewModel.sellCurrencyStateFlow.value.currency} commission fee.",
                            view = binding.root
                        )
                        return@setOnClickListener
                    }
                }

                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(getString(R.string.currency_converted))
                    .setMessage(
                        getString(
                            R.string.currency_converted_dialog_body,
                            viewModel.sellAmount.value.toString() + " " + viewModel.sellCurrencyStateFlow.value.currency,
                            viewModel.recvAmount.value.toString() + " " + viewModel.recvCurrencyStateFlow.value.currency,
                            if(viewModel.userCommissionRate.value?.transactions_done ?: 0 < MainViewModel.COMMISSION_FREE_LIMIT) "0"
                            else (viewModel.sellAmount.value * COMMISSION_RATE).toString()
                                    + " " + viewModel.sellCurrencyStateFlow.value.currency
                        ))
                    .setPositiveButton("Done") { dialog, which ->
                        // Respond to positive button press
                    }
                    .show()

                lifecycleScope.launch {
                    viewModel.convert()
                    binding.sellEt.setText("")
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.sellCurrencyStateFlow.collect {

                binding.sellCurrencyTv.text = it.currency
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.recvCurrencyStateFlow.collect {
                binding.recvCurrencyTv.text = it.currency
                viewModel.computeRecvAmount(viewModel.sellAmount.value)
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.allUserBalance.collect {
                binding.userBalanceRv.apply {
                    adapter = UserBalanceRVAdapter(
                        it ?: emptyList()
                    )
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.events.collect { event ->
                when (event) {
                    is MainViewModel.Event.ShowErrorMessage -> {
                        showSnackbar(
                            message = getString(
                                R.string.error_occurred,
                                event.error.localizedMessage
                                    ?: getString(R.string.unknown_error_occurred),
                            ),
                            view = binding.root
                        )
                    }
                }
            }
        }

        lifecycleScope.launchWhenCreated {
            viewModel.recvAmount.collect {
                if (it != 0.0)
                    binding.recvTv.text = "+ ${"%.2f".format(it)}"
                else
                    binding.recvTv.text = it.toString()
            }
        }


        viewModel.userCommissionRate.observe(this, Observer {
                if(it == null) {
                    lifecycleScope.launch {
                        viewModel.initCommissionRate()
                    }
                }
            })


        viewModel.allRatesState.observe(this, Observer {
            viewModel.fetchRates()
        })

        viewModel.fetchRates()
    }

    private fun proceedToSearchUI(ratesList: ArrayList<CurrencyRate>) {

        val searchDialog = SearchDialogHelper()
        val args = Bundle()
        args.putParcelableArrayList("ratesList", ratesList)

        searchDialog.arguments = args
        searchDialog.show(supportFragmentManager, "searchDialog")
    }

    private fun setUpGradientBGColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        window.setBackgroundDrawableResource(R.drawable.bg_main_header)
    }

//    enum class RateDesitination {
//        SELL, RECEIVE
//    }
}