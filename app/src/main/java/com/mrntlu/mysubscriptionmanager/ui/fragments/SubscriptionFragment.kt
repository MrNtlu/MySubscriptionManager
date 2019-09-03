package com.mrntlu.mysubscriptionmanager.ui.fragments

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.mrntlu.mysubscriptionmanager.R
import com.mrntlu.mysubscriptionmanager.interfaces.CoroutinesHandler
import com.mrntlu.mysubscriptionmanager.models.Currency
import com.mrntlu.mysubscriptionmanager.models.FrequencyType
import com.mrntlu.mysubscriptionmanager.models.Subscription
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState
import com.mrntlu.mysubscriptionmanager.models.SubscriptionViewState.*
import com.mrntlu.mysubscriptionmanager.utils.*
import com.mrntlu.mysubscriptionmanager.viewmodels.SubscriptionViewModel
import kotlinx.android.synthetic.main.fragment_subscription.*
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("DefaultLocale")
class SubscriptionFragment : Fragment(), DatePickerDialog.OnDateSetListener, CoroutinesHandler {

    private var mSubscription: Subscription? = null
    private lateinit var viewModel: SubscriptionViewModel
    private lateinit var viewState: SubscriptionViewState
    private lateinit var navController: NavController

    private lateinit var paymentDate:Date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mSubscription = it.getParcelable("subscription")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_subscription, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(context as AppCompatActivity).get(SubscriptionViewModel::class.java)
        navController= Navigation.findNavController(view)

        setupObservers()
        setClickListeners()
        setSpinners(view)
    }

    private fun setSpinners(view: View) {
        currencySpinner.let {
            val arrayAdapter = ArrayAdapter<String>(
                view.context,
                android.R.layout.simple_spinner_item,
                Currency.values().map { currency -> currency.name })
            it.adapter = arrayAdapter
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

    private fun setupObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            viewState = it
            viewStateController(it)
        })
    }

    private fun setClickListeners() {
        saveFab.setOnClickListener {
            if (viewState == INSERT || viewState == UPDATE) {
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
                    showToast(it.context,checkRules().values.toTypedArray()[0])
                }
            } else {
                viewModel.setViewState(UPDATE)
            }
        }

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

        deleteFab.setOnClickListener {
            viewModel.deleteSubscription(mSubscription!!,this)
        }

        cancelFab.setOnClickListener {
            //TODO Show dialog are you sure
            when(viewState){
                UPDATE,INSERT->{
                    val dialogBuilder = createDialog(it.context,"Do you want to discard changes?")

                    dialogBuilder.setPositiveButton("Yes") { _, _ ->
                        if (viewState==UPDATE) viewModel.setViewState(VIEW) else navController.navigate(R.id.action_sub_to_subsList)
                    }

                    val alert = dialogBuilder.create()
                    alert.show()
                }
                VIEW -> {
                    navController.navigate(R.id.action_sub_to_subsList)
                }
            }
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

    private fun viewStateController(viewState: SubscriptionViewState) {
        when (viewState) {
            VIEW -> {
                setViews(false)
                setData()
                saveFab.setImageDrawable(
                    resources.getDrawable(
                        R.drawable.ic_edit_black_24dp,
                        context?.theme
                    )
                )
            }
            UPDATE -> {
                setViews(true)
                setData()
            }
            INSERT -> {
                setViews(true)
            }
        }
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun setData() {
        mSubscription?.let {
            priceText.text = it.price.toEditable()
            nameText.text = it.name.toEditable()
            frequencyText.text = it.frequency.toEditable()
            it.description?.let { desc -> descriptionText.text = desc.toEditable() }

            colorPicker.setCardBackgroundColor(it.color)
            firstPaymentText.text = it.paymentDate.dateFormatLong().toEditable()
            paymentDate=it.paymentDate
            it.paymentMethod?.let { method -> paymentMethodText.text = method.toEditable() }

            when(viewState){
                UPDATE ->{
                    currencySpinner.setSelection(it.currency.code)
                    frequencyTypeSpinner.setSelection(it.frequencyType.code)
                }
                VIEW ->{
                    currencyText.text=it.currency.name
                    frequencyTypeText.text=it.frequencyType.name.toLowerCase(Locale.getDefault())
                }
            }
        }
    }

    private fun setViews(isEditing: Boolean) {
        priceText.isEnabled = isEditing
        nameText.isEnabled = isEditing
        descriptionText.isEnabled = isEditing
        frequencyText.isEnabled = isEditing
        firstPaymentText.isEnabled = isEditing
        colorPicker.isEnabled = isEditing
        paymentMethodText.isEnabled = isEditing
        nameText.isEnabled = isEditing

        firstPaymentText.isClickable = isEditing
        colorPicker.isClickable = isEditing

        if (isEditing) saveFab.setImageDrawable(
            resources.getDrawable(
                R.drawable.ic_save_black_24dp,
                context?.theme
            )
        )
        else saveFab.setImageDrawable(
            resources.getDrawable(
                R.drawable.ic_edit_black_24dp,
                context?.theme
            )
        )

        if (isEditing) currencyText.setGone() else currencyText.setVisible()
        if (isEditing) currencySpinner.setVisible() else currencySpinner.setGone()
        if (isEditing) frequencyTypeText.setGone() else frequencyTypeText.setVisible()
        if (isEditing) frequencyTypeSpinner.setVisible() else frequencyTypeSpinner.setGone()
        if (isEditing) deleteFab.setGone() else deleteFab.setVisible()

    }

    private fun showDatePickerDialog(view: View) {
        val datePickerDialog = DatePickerDialog(
            view.context, this::onDateSet,
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )

        //todo datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 2000

        datePickerDialog.show()
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        val dateString = "$day ${month + 1} $year"
        val date = SimpleDateFormat("dd MM yyyy", Locale.ENGLISH).parse(dateString)
        date?.let {
            firstPaymentText.text = it.dateFormatLong().toEditable()
            paymentDate=it
        }
    }

    override fun onSuccess(message:String) {
        progressbarLayout.setGone()
        context?.let {
            showToast(it, message)
            when(viewState){
                UPDATE -> viewModel.setViewState(VIEW)
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

    override fun onStop() {
        viewModel.cancelJobs()
        super.onStop()
    }
}