package com.singularitycoder.learnit.intro

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentTutorialBinding
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.color
import com.singularitycoder.learnit.helpers.constants.FragmentsTag
import com.singularitycoder.learnit.helpers.constants.Tutorial
import com.singularitycoder.learnit.helpers.drawable
import com.singularitycoder.learnit.helpers.setNavigationBarColor
import com.singularitycoder.learnit.helpers.showScreen
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subject.view.MainFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TutorialFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = TutorialFragment()
    }

    private lateinit var binding: FragmentTutorialBinding

    private val viewPager2PageChangeListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            println("viewpager2: onPageScrollStateChanged")
        }

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            println("viewpager2: onPageSelected")
            updateBottomDotColors(position)
            binding.btnPrevious.isVisible = position != 0
            binding.btnSkip.isVisible = position != Tutorial.entries.size - 1
            binding.btnNext.icon = context?.drawable(
                if (position != Tutorial.entries.size - 1) {
                    R.drawable.round_arrow_forward_24
                } else {
                    R.drawable.ic_round_check_24
                }
            )
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            println("viewpager2: onPageScrolled")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentTutorialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.setNavigationBarColor(R.color.white)
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.viewPager.unregisterOnPageChangeCallback(viewPager2PageChangeListener)
    }

    private fun FragmentTutorialBinding.setupUI() {
        addBottomDots(currentPage = 0)
        viewPager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            adapter = MainViewPagerAdapter(fragmentManager = parentFragmentManager, lifecycle = lifecycle)
            setPageTransformer(DepthPageTransformer())
            registerOnPageChangeCallback(viewPager2PageChangeListener)
            currentItem = 0
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentTutorialBinding.setupUserActionListeners() {
        btnSkip.setOnClickListener { showHome() }
        btnNext.setOnClickListener { btnNext() }
        btnPrevious.setOnClickListener { btnPrevious() }
    }

    private fun addBottomDots(currentPage: Int) {
        val tvDotsArray = arrayOfNulls<TextView>(Tutorial.entries.size)

        binding.linLayDots.removeAllViews()
        for (i in tvDotsArray.indices) {
            tvDotsArray[i] = TextView(context)
            tvDotsArray[i]?.text = Html.fromHtml("&#8226;", HtmlCompat.FROM_HTML_MODE_LEGACY)
            tvDotsArray[i]?.textSize = 35f
            tvDotsArray[i]?.setTextColor(requireContext().color(R.color.purple_50))
            binding.linLayDots.addView(tvDotsArray[i])
        }

        if (tvDotsArray.isNotEmpty()) {
            tvDotsArray[currentPage]?.setTextColor(requireContext().color(R.color.purple_500))
        }
    }

    private fun updateBottomDotColors(currentPage: Int) {
        binding.linLayDots.forEachIndexed { index, view ->
            (view as TextView).setTextColor(
                requireContext().color(
                    if (index == currentPage) {
                        R.color.purple_500
                    } else {
                        R.color.purple_50
                    }
                )
            )
        }
    }

    private fun btnNext() {
        val current = binding.viewPager.currentItem + 1 // checking for last page if last page home screen will be launched
        if (current < Tutorial.entries.size) {
            binding.viewPager.currentItem = current // Show next screen
        } else {
            showHome()
        }
    }

    private fun btnPrevious() {
        val current = binding.viewPager.currentItem - 1
        if (current >= 0) {
            binding.viewPager.currentItem = current
        }
    }

    private fun showHome() {
        AppPreferences.getInstance().hasCompletedTutorial = true
        (activity as MainActivity).showScreen(
            fragment = MainFragment.newInstance(),
            tag = FragmentsTag.MAIN,
            isAdd = true,
            isAddToBackStack = false
        )
    }

    inner class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
        override fun getItemCount(): Int = Tutorial.entries.size
        override fun createFragment(position: Int): Fragment = TutorialItemFragment.newInstance(position)
    }
}