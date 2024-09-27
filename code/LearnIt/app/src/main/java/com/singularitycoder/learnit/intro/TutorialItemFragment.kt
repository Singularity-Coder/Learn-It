package com.singularitycoder.learnit.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.singularitycoder.learnit.databinding.FragmentTutorialItemBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.constants.Tutorial
import com.singularitycoder.learnit.helpers.deviceHeight
import com.singularitycoder.learnit.helpers.drawable
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialItemFragment : Fragment() {

    companion object {
        private const val KEY_POSITION = "KEY_POSITION"

        @JvmStatic
        fun newInstance(position: Int = 0) = TutorialItemFragment().apply {
            arguments = Bundle().apply {
                putInt(KEY_POSITION, position)
            }
        }
    }

    private lateinit var binding: FragmentTutorialItemBinding

    private var position: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (AndroidVersions.isTiramisu()) {
            position = arguments?.getInt(KEY_POSITION, 0)
        } else {
            position = arguments?.getInt(KEY_POSITION, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTutorialItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
    }

    private fun FragmentTutorialItemBinding.setupUI() {
        ivImage.layoutParams.height = (deviceHeight() / 2.5).toInt()
        val tutorial = Tutorial.entries.getOrNull(position ?: 0) ?: return
        ivImage.setImageDrawable(requireContext().drawable(tutorial.image))
        tvTitle.text = tutorial.title
        tvSubtitle.text = getString(tutorial.subTitle)
    }
}