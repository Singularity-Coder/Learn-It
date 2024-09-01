package com.singularitycoder.learnit

import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.databinding.FragmentBookReaderFiltersBottomSheetBinding
import com.singularitycoder.learnit.helpers.dpToPx
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.setTransparentBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddSubjectBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = AddSubjectBottomSheetFragment()
    }

    private lateinit var binding: FragmentBookReaderFiltersBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookReaderFiltersBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTransparentBackground()
        binding.setupUserActionListeners()
    }

    private fun FragmentBookReaderFiltersBottomSheetBinding.setupUserActionListeners() {
        etApiKey.editText?.doAfterTextChanged { it: Editable? ->
            if (etApiKey.editText?.text.isNullOrBlank()) {
                etApiKey.error = "This is required!"
            } else {
                etApiKey.error = null
            }
        }

        etApiKey.editText?.setOnFocusChangeListener { view, isFocused ->
            etApiKey.boxStrokeWidth = if (etApiKey.editText?.text.isNullOrBlank().not()) 2.dpToPx().toInt() else 0
        }

        btnDone.onSafeClick {
            if (etApiKey.editText?.text.isNullOrBlank()) {
                etApiKey.boxStrokeWidth = 2.dpToPx().toInt()
                etApiKey.error = "This is required!"
                return@onSafeClick
            }
            if ((etApiKey.editText?.text?.length ?: 0) < 10 || (etApiKey.editText?.text?.length ?: 0) > 100) {
                etApiKey.boxStrokeWidth = 2.dpToPx().toInt()
                etApiKey.error = "Invalid API Key"
                return@onSafeClick
            }
            dismiss()
        }

//        ibPasteApiKey.onSafeClick {
//            if (context.clipboard()?.text.isNullOrBlank().not()) {
//                etApiKey.boxStrokeWidth = 2.dpToPx().toInt()
//                binding.etApiKey.editText?.setText(context.clipboard()?.text?.trim())
//            }
//        }
    }
}