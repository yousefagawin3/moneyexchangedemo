package com.example.moneyexchangedemo.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneyexchangedemo.db.Commission
import com.example.moneyexchangedemo.db.USER_COMMISSION_RATE_ID
import com.example.moneyexchangedemo.db.UserBalance
import com.example.moneyexchangedemo.model.CurrencyRate
import com.example.moneyexchangedemo.repo.MoneyExchangeRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MoneyExchangeRepo
) : ViewModel() {

    //we use Channel bec using MutableStateFlow will store the prev value of the error which will show the prev snackbar error when rotated the screen
    private val eventChannel = Channel<Event>()

    //we want to only expose the flow of (events) in our fragment for encapsulation purposes
    val events = eventChannel.receiveAsFlow()

    val sellCurrencyStateFlow = MutableStateFlow(CurrencyRate("", 0.0))
    val sellAmount = MutableStateFlow(0.0)

    val recvCurrencyStateFlow = MutableStateFlow(CurrencyRate("", 0.0))
    val recvAmount = MutableStateFlow(0.0)

    var rateDestination = RateDestination.SELL

    //    val allRatesState = MutableLiveData<Map<String, Double>>()
    val allRatesState = MutableLiveData<List<CurrencyRate>>()

    val userCommissionRate = repository.getCommissionRate()

    val allUserBalance = repository.getAllUserBalance()
        .flatMapLatest { allUserBalance ->
            if (allUserBalance.isNullOrEmpty()) {
                repository.setUserBalance(
                    UserBalance(
                        "EUR",
                        1000.0
                    )
                )
            } else {
                    //if the userbalance is no longer null,
                    // we make the first currency value as the base
                    if(allUserBalance.filter { it.currency == sellCurrencyStateFlow.value.currency }.isNullOrEmpty()) {
                        sellCurrencyStateFlow.value = (CurrencyRate(allUserBalance[0].currency, 0.0))
                    }

            }
            return@flatMapLatest repository.getAllUserBalance()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    fun fetchRates() {
        viewModelScope.launch {
            if (!allRatesState.value.isNullOrEmpty()) {
                delay(REFRESH_RATE)
            }
            repository.getAllRates(
                sellCurrencyStateFlow.value.currency,
                onAPISuccess = { apiResp ->
                    apiResp.rates?.let { rates ->
                        allRatesState.value = rates.toList().map {
                            CurrencyRate(it.first, it.second)
                        }.let {
                            //if the receive currency is empty, we will try to put up a new value
                            if(recvCurrencyStateFlow.value.currency == ""){
                                //if the current sell currency is the same with the first value from the list, we'll assign the second value
                                if (sellCurrencyStateFlow.value.currency == it[0].currency) {
                                    recvCurrencyStateFlow.value = it[1]
                                } else {
                                    recvCurrencyStateFlow.value = it[0]
                                }
                            }


                            it
                        }
                    }
                },
                onAPIFailed = {
                    viewModelScope.launch {
                        eventChannel.send(Event.ShowErrorMessage(it))
                    }
                }
            ).stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                null
            )
        }
    }

    fun onRateSelected (currencyRate: CurrencyRate) {
        when(rateDestination) {
            RateDestination.SELL -> {
                sellCurrencyStateFlow.value = currencyRate
            }
            RateDestination.RECEIVE -> {
                recvCurrencyStateFlow.value = currencyRate
            }
        }
        resetAmounts()
    }

    private fun resetAmounts() {
        sellAmount.value = 0.0
        recvAmount.value = 0.0
    }

    fun computeRecvAmount(sellInputValue: Double) {
        sellAmount.value = sellInputValue

        //updates the sell currency value
        allRatesState.value?.filter {
            it.currency == sellCurrencyStateFlow.value.currency
        }.also {
            sellCurrencyStateFlow.value.value = it?.get(0)?.value ?: 0.0
        }
        recvAmount.value =
            (recvCurrencyStateFlow.value.value * sellAmount.value / sellCurrencyStateFlow.value.value)
    }

    //checks if the user input is within its user balance
    fun isInputValueLessThanBalance(input: Double): Boolean {

        val userBalance = allUserBalance.value?.filter {
            it.currency == sellCurrencyStateFlow.value.currency
        }
        userBalance?.get(0)?.let {
            if (input <= it.amount)
                return true
        }
        return false
    }

    suspend fun convert() {

        userCommissionRate.value?.let { currentUserCommRate ->
            //free transactions
            if (currentUserCommRate.transactions_done < COMMISSION_FREE_LIMIT) {
                repository.insertOrAddUserBalance(
                    recvCurrencyStateFlow.value.currency,
                    recvAmount.value
                )

                repository.insertOrAddUserBalance(
                    sellCurrencyStateFlow.value.currency,
                    sellAmount.value * -1
                )
            }
            //non-free transactions
            else {
                repository.insertOrAddUserBalance(
                    recvCurrencyStateFlow.value.currency,
                    recvAmount.value
                )
                repository.insertOrAddUserBalance(
                    sellCurrencyStateFlow.value.currency,
                    (sellAmount.value + sellAmount.value * COMMISSION_RATE) * -1
                )
            }

            repository.upsertCommissionRate(
                Commission(
                    id = USER_COMMISSION_RATE_ID,
                    transactions_done = currentUserCommRate.transactions_done + 1
                )
            )
        }
    }

    suspend fun initCommissionRate() {
        repository.upsertCommissionRate(
            Commission(
                id = USER_COMMISSION_RATE_ID,
                transactions_done = 0
            )
        )
    }
    //with this kind of Event class we can send different kinds of event in our Channel
    //for example: we can also send navigation events here
    //we make this a sealed class bec the compiler knows that they are only the subclasses that we put directly into its body and no other ones.
    sealed class Event {
        data class ShowErrorMessage(val error: Throwable) : Event()
    }

    companion object {
        const val COMMISSION_FREE_LIMIT = 5
        const val COMMISSION_RATE = 0.007
        const val REFRESH_RATE = 500000L
    }

    enum class RateDestination {
        SELL, RECEIVE
    }
}