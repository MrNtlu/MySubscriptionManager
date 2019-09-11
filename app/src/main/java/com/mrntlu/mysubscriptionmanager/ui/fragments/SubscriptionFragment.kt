package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.adapters.CurrencySpinnerAdapter
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.FrequencyType
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState.*
import com.mrntlu.mysubscriptionmanager.ui.MainActivity
import com.mrntlu.mysubscriptionmanager.utils.*
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_subscription.*
import java.text.SimpleDateFormat
import java.util.*

interface BackPressedCallback{
    fun onBackPressed()
}

@SuppressLint("DefaultLocale")
class SubscriptionFragment : Fragment(), DatePickerDialog.OnDateSetListener, CoroutinesHandler,BackPressedCallback {

    private var mSubscription: Subscription? = null
    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var viewState: SubscriptionViewState
    private lateinit var defaultCurrency: Currency
    private lateinit var navController: NavController

    private lateinit var paymentDate:Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mSubscription = it.getParcelable("subscription")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subscription, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(context as AppCompatActivity).get(SubscriptionViewModel::class.java)
        navController= Navigation.findNavController(view)

        setupObservers(view)
        setClickListeners()

        initBottomAppBar(view.context as MainActivity)
    }

    private fun initBottomAppBar(activity: MainActivity) {
        activity.setBottomAppBar(this)
        setBottomAppbarMenuListeners(activity)
        activity.addFab.setOnClickListener {
            when(viewState){
                UPDATE,INSERT->{
                    val dialogBuilder = createDialog(it.context,"Do you want to discard changes?")
                    dialogBuilder.setPositiveButton("Yes") { _, _ ->
                        navController.popBackStack()
                        /*if (viewState==UPDATE) {
                            val bundle= bundleOf("subscription" to mSubscription)
                            navController.navigate(R.id.action_sub_to_subView,bundle)
                        } else navController.navigate(R.id.action_sub_to_subsList)*/
                    }
                    val alert = dialogBuilder.create()
                    alert.show()
                }
            }
        }
    }

    private fun setSpinners(view: View) {
        currencySpinner.let {
            val arrayAdapter = CurrencySpinnerAdapter(
                view.context,
                Currency.values().toList(),
                R.color.White,
                R.color.Black)
            it.adapter = arrayAdapter
            if (viewState!=UPDATE) it.setSelection(if (::defaultCurrency.isInitialized) defaultCurrency.code else Currency.USD.code)
            printLog(message = "$viewState ${it.selectedItemPosition}")
        }

        frequencyTypeSpinner.let {
            val arrayAdapter=ArrayAdapter<String>(
                view.context,
                android.R.layout.simple_spinner_item,
                FrequencyType.values().map { frequencyType -> frequencyType.name.toLowerCase(Locale.getDefault()).capitalize() }
            )
            it.adapter=arrayAdapter
        }

    }

    private fun setupObservers(view: View) {
        viewModel.defaultCurrency.observe(viewLifecycleOwner, Observer {
            defaultCurrency=it
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            viewState = it
            if (it==UPDATE) {
                setSpinners(view)
                setData()
            }
        })
    }

    private fun setBottomAppbarMenuListeners(activity: MainActivity){
        activity.bottomAppBar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.appbarSave -> {
                    if (checkRules().keys.toBooleanArray()[0]) {
                        val subscription = Subscription(
                            mSubscription?.id ?: UUID.randomUUID().toString(),
                            nameText.getAsString(),
                            descriptionText.getAsString(),
                            paymentDate,
                            frequencyText.getAsString().toInt(),
                            FrequencyType.valueOf(
                                frequencyTypeSpinner.selectedItem.toString().toUpperCase(
                                    Locale.getDefault()
                                )
                            ),
                            priceText.getAsString().toDouble(),
                            Currency.valueOf(currencySpinner.selectedItem.toString()),
                            colorPicker.cardBackgroundColor.defaultColor,
                            paymentMethodText.getAsString()
                        )

                        progressbarLayout.setVisible()
                        if (viewState == UPDATE) {
                            viewModel.updateSubscription(subscription, this)
                            mSubscription = subscription
                        } else viewModel.insertSubscription(subscription, this)
                    }else{
                        showToast(activity,checkRules().values.toTypedArray()[0])
                    }
                }
                R.id.appbarDelete ->{
                    val dialogBuilder = createDialog(activity,"Do you want to delete this item?")
                    dialogBuilder.setPositiveButton("Yes") { _, _ ->
                        viewModel.deleteSubscription(mSubscription!!,this)
                    }
                    val alert = dialogBuilder.create()
                    alert.show()
                }
                R.id.appbarEdit -> {
                    viewModel.setViewState(UPDATE)
                }
            }
            true
        }
    }

    private fun setClickListeners() {
        firstPaymentText.setOnClickListener {
            showDatePickerDialog(it)
        }

        colorPicker.setOnClickListener {
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose Color")
                .initialColor(colorPicker.cardBackgroundColor.defaultColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                .density(12)
                .setPositiveButton("Select") { _, lastSelectedColor, _ ->
                    colorPicker.setCardBackgroundColor(lastSelectedColor)
                }
                .build().show()
        }
    }

    private fun checkRules(): Map<Boolean,String> {
        return if (priceText.isEmptyOrBlank()){
            mapOf(false to "Price should't be empty.")
        }else if (nameText.isEmptyOrBlank()){
            mapOf(false to "Name should't be empty.")
        }else if(!::paymentDate.isInitialized){
            mapOf(false to "Payment Date should't be empty.")
        }else if (frequencyText.isEmptyOrBlank()){
            mapOf(false to "Payment frequency should't be empty.")
        }else mapOf(true to "")
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun setData() {
        mSubscription?.let {
            priceText.text = it.price.toEditable()
            nameText.text = it.name.toEditable()
            frequencyText.text = it.frequency.toEditable()
            descriptionText.text = it.description.toEditable()

            colorPicker.setCardBackgroundColor(it.color)
            firstPaymentText.text = it.paymentDate.dateFormatLong().toEditable()
            paymentDate=it.paymentDate
            paymentMethodText.text = it.paymentMethod.toEditable()

            printLog(message = "${it.currency} ${it.currency.code}")
            currencySpinner.setSelection(it.currency.code)
            printLog(message = currencySpinner.selectedItemPosition.toString())
            frequencyTypeSpinner.setSelection(it.frequencyType.code)
        }
    }

    private fun showDatePickerDialog(view: View) {
        val datePickerDialog = DatePickerDialog(
            view.context, this::onDateSet,
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 2000

        datePickerDialog.show()
    }
//Date Dialog
    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val dateString = "$day ${month + 1} $year"
        val date = SimpleDateFormat("dd MM yyyy", Locale.ENGLISH).parse(dateString)
        date?.let {
            firstPaymentText.text = it.dateFormatLong().toEditable()
            paymentDate=it
        }
    }
//Coroutines Handler
    override fun onSuccess(message:String) {
        progressbarLayout.setGone()
        context?.let {
            showToast(it, message)
            when(viewState){
                UPDATE -> {
                    val bundle= bundleOf("subscription" to mSubscription)
                    navController.navigate(R.id.action_sub_to_subView,bundle)
                }
                else -> navController.navigate(R.id.action_sub_to_subsList)
            }
        }
    }

    override fun onError(exception: String) {
        progressbarLayout.setGone()
        context?.let {
            showToast(it, exception)
            (it as AppCompatActivity).onBackPressed()
        }
    }

//OnBackPressed
    override fun onBackPressed() {
        context?.let {
            (it as MainActivity).addFab.performClick()
        }
    }

    override fun onStop() {
        viewModel.cancelJobs()
        super.onStop()
    }
}