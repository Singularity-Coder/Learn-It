package com.singularitycoder.learnit

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.singularitycoder.learnit.databinding.FragmentEditBottomSheetBinding
import com.singularitycoder.learnit.helpers.EditEvent
import com.singularitycoder.learnit.helpers.FragmentResultBundleKey
import com.singularitycoder.learnit.helpers.FragmentResultKey
import com.singularitycoder.learnit.helpers.dpToPx
import com.singularitycoder.learnit.helpers.enableSoftInput
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.setTransparentBackground
import com.singularitycoder.learnit.helpers.showKeyboard
import dagger.hilt.android.AndroidEntryPoint

private const val ARG_PARAM_EDIT_EVENT_TYPE = "ARG_PARAM_EDIT_EVENT_TYPE"

@AndroidEntryPoint
class EditBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        @JvmStatic
        fun newInstance(
            eventType: EditEvent
        ) = EditBottomSheetFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PARAM_EDIT_EVENT_TYPE, eventType)
            }
        }
    }

    private lateinit var binding: FragmentEditBottomSheetBinding

    private var eventType: EditEvent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_PARAM_EDIT_EVENT_TYPE, EditEvent::class.java)
        } else {
            arguments?.getParcelable(ARG_PARAM_EDIT_EVENT_TYPE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    private fun FragmentEditBottomSheetBinding.setupUI() {
        enableSoftInput()

        setTransparentBackground()

        when (eventType) {
            EditEvent.RENAME_DOWNLOAD_FILE -> {
                tvHeader.text = "Rename file"
                etEdit.hint = "File name"
            }
            EditEvent.CREATE_NEW_DOWNLOAD_FOLDER -> {
                tvHeader.text = "Create new folder"
                etEdit.hint = "Folder name"
            }
            else -> Unit
        }

        etEdit.editText?.showKeyboard()
    }

    private fun FragmentEditBottomSheetBinding.setupUserActionListeners() {
        etEdit.editText?.doAfterTextChanged { it: Editable? ->
            if (etEdit.editText?.text.isNullOrBlank()) {
                etEdit.error = "This is required!"
            } else {
                etEdit.error = null
            }
        }

        etEdit.editText?.setOnFocusChangeListener { view, isFocused ->
            etEdit.boxStrokeWidth = if (etEdit.editText?.text.isNullOrBlank().not()) 2.dpToPx().toInt() else 0
        }

        btnDone.onSafeClick {
            etEdit.error = null
            if (etEdit.isVisible) {
                if (etEdit.editText?.text.isNullOrBlank()) {
                    etEdit.boxStrokeWidth = 2.dpToPx().toInt()
                    etEdit.error = "This is required!"
                    return@onSafeClick
                }
            }
            when (eventType) {
                EditEvent.RENAME_DOWNLOAD_FILE -> {
                    parentFragmentManager.setFragmentResult(
                        /* requestKey = */ FragmentResultKey.RENAME_DOWNLOAD_FILE,
                        /* result = */ bundleOf(FragmentResultBundleKey.RENAME_DOWNLOAD_FILE to etEdit.editText?.text.toString())
                    )
                }
                EditEvent.CREATE_NEW_DOWNLOAD_FOLDER -> {
                    parentFragmentManager.setFragmentResult(
                        /* requestKey = */ FragmentResultKey.CREATE_NEW_DOWNLOAD_FOLDER,
                        /* result = */ bundleOf(FragmentResultBundleKey.CREATE_NEW_DOWNLOAD_FOLDER to etEdit.editText?.text.toString())
                    )
                }
                else -> Unit
            }
            btnDone.isVisible = false
            progressCircular.isVisible = true
            dismiss()
        }
    }
}