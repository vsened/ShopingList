package com.example.shopinglist.presentation

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.shopinglist.R
import com.example.shopinglist.domain.ShopItem
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ShopItemFragment : Fragment() {
    private lateinit var viewModel: ShopItemViewModel
    lateinit var onEditingFinishListener: OnEditingFinishListener

    private lateinit var tilName: TextInputLayout
    private lateinit var tilCount: TextInputLayout
    private lateinit var tvName: TextInputEditText
    private lateinit var tvCount: TextInputEditText
    private lateinit var saveButton: Button

    private var screenMode: String = MODE_UNKNOWN
    private var shopItemId: Int = ShopItem.UNDEFINED_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseParams()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnEditingFinishListener) {
            onEditingFinishListener = context
        } else {
            throw RuntimeException("Activity must implement onEditingFinishListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_shop_item,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        viewModel = ViewModelProvider(this)[ShopItemViewModel::class.java]
        addTextChangeListeners()
        launchScreenMode()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.errorInputName.observe(viewLifecycleOwner) {
            if (it) {
                tilName.error = INCORRECT_NAME_MESSAGE
            } else {
                tilName.error = null
            }
        }
        viewModel.errorInputCount.observe(viewLifecycleOwner) {
            if (it) {
                tilName.error = INCORRECT_COUNT_MESSAGE
            } else {
                tilName.error = null
            }
        }
        viewModel.shouldCloseScreen.observe(viewLifecycleOwner) {
            onEditingFinishListener.onEditingFinish()
        }
    }

    private fun launchScreenMode() {
        when (screenMode) {
            MODE_EDIT -> launchEditMode()
            else -> launchAddMode()
        }
    }

    private fun addTextChangeListeners() {
        tvName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.resetErrorInputName()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
        tvCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                viewModel.resetErrorInputCount()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun parseParams() {
        val args = requireArguments()
        if (!args.containsKey(SCREEN_MODE)) {
            throw RuntimeException("Param screen mode is absent")
        }
        val mode = args.getString(SCREEN_MODE)
        if (mode != MODE_ADD && mode != MODE_EDIT) {
            throw RuntimeException("Unknown screen mode $mode")
        }
        screenMode = mode
        if (mode == MODE_EDIT) {
            if (!args.containsKey(SHOP_ITEM_ID)) {
                throw RuntimeException("Param shop item id is absent")
            }
            shopItemId = args.getInt(SHOP_ITEM_ID, ShopItem.UNDEFINED_ID)
        }
    }

    private fun initViews(view: View) {
        tilName = view.findViewById(R.id.til_name)
        tilCount = view.findViewById(R.id.til_count)
        tvName = view.findViewById(R.id.et_name)
        tvCount = view.findViewById(R.id.et_count)
        saveButton = view.findViewById(R.id.button_save_item)
    }

    private fun launchEditMode() {
        viewModel.getShopItem(shopItemId)
        viewModel.currentShopItem.observe(viewLifecycleOwner) {
            tvName.setText(it.name)
            tvCount.setText(it.count.toString())

        }
        saveButton.setOnClickListener {
            val name = tvName.text?.toString()
            val count = tvCount.text?.toString()
            viewModel.editShopItem(name, count)
        }
    }

    private fun launchAddMode() {
        saveButton.setOnClickListener {
            val name = tvName.text?.toString()
            val count = tvCount.text?.toString()
            viewModel.addShopItem(name, count)
        }
    }

    interface OnEditingFinishListener {
        fun onEditingFinish()
    }

    companion object {
        private const val SCREEN_MODE = "extra_mode"
        private const val SHOP_ITEM_ID = "extra_shop_item_id"
        private const val MODE_EDIT = "mode_edit"
        private const val MODE_ADD = "mode_add"
        private const val MODE_UNKNOWN = ""
        private const val INCORRECT_NAME_MESSAGE = "Name is incorrect! Please change."
        private const val INCORRECT_COUNT_MESSAGE = "Count is incorrect! Please change."

        fun newInstanceAddItem(): ShopItemFragment {
            return ShopItemFragment().apply {
                arguments = Bundle().apply {
                    putString(SCREEN_MODE, MODE_ADD)
                }
            }
        }

        fun newInstanceEditItem(shopItemId: Int): ShopItemFragment {
            return ShopItemFragment().apply {
                arguments = Bundle().apply {
                    putString(SCREEN_MODE, MODE_EDIT)
                    putInt(SHOP_ITEM_ID, shopItemId)
                }
            }

        }
    }
}