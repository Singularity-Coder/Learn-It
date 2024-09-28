package com.singularitycoder.learnit.permissions

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentPermissionsBinding
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.constants.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.setNavigationBarColor
import com.singularitycoder.learnit.subject.view.MainActivity
import dagger.hilt.android.AndroidEntryPoint

/** Referred https://github.com/WrichikBasu/ShakeAlarmClock */

@AndroidEntryPoint
class PermissionsFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = PermissionsFragment()
    }

    private lateinit var binding: FragmentPermissionsBinding

    private val permissionsAdapter = PermissionsAdapter()

    private var permissionList = listOf<Permission>()

    private val viewModel by viewModels<PermissionsViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentPermissionsBinding.setupUI() {
        activity?.setNavigationBarColor(R.color.white)
        layoutCustomToolbar.apply {
            ibBack.isVisible = false
            ivMore.isVisible = false
            tvTitle.text = "Grant Permissions"
        }
        rvPermissions.apply {
            layoutAnimation = rvPermissions.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = permissionsAdapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentPermissionsBinding.setupUserActionListeners() {
        root.setOnClickListener {}

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = viewModel.permissionsList) { list: List<Permission> ->
            this.permissionList = list
            permissionsAdapter.permissionList = permissionList
            permissionsAdapter.notifyDataSetChanged()
            binding.layoutCustomToolbar.tvCount.text = "${list.filter { it.isGranted }.size}/${list.size} granted"
        }
    }
}