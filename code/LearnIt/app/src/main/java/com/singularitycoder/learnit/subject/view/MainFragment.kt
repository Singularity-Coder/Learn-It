package com.singularitycoder.learnit.subject.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentMainBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.askAlarmPermission
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.constants.FragmentResultBundleKey
import com.singularitycoder.learnit.helpers.constants.FragmentResultKey
import com.singularitycoder.learnit.helpers.constants.FragmentsTag
import com.singularitycoder.learnit.helpers.constants.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.hasNotificationsPermission
import com.singularitycoder.learnit.helpers.hideKeyboard
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onImeClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.shouldShowRationaleFor
import com.singularitycoder.learnit.helpers.showAlertDialog
import com.singularitycoder.learnit.helpers.showAppSettings
import com.singularitycoder.learnit.helpers.showPopupMenuWithIcons
import com.singularitycoder.learnit.helpers.showScreen
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subject.viewmodel.SubjectViewModel
import com.singularitycoder.learnit.topic.view.TopicFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * android:fitsSystemWindows="true" adds weird padding on top and bottom.
 * android:fitsSystemWindows="true" along with manifest activity flag android:windowSoftInputMode="stateAlwaysVisible" is necessary for bottom text field to show be visible when keyboard is up
 * */

@AndroidEntryPoint
class MainFragment : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private lateinit var binding: FragmentMainBinding

    private val subjectsAdapter = SubjectsAdapter()

    private var subjectList = listOf<Subject?>()

    private val viewModel by viewModels<SubjectViewModel>()

    private var notificationPermissionCount = 0

    private val notificationPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? ->
        isGranted ?: return@registerForActivityResult

        fun showRationale() {
            requireContext().showAlertDialog(
                title = "Grant permission",
                message = "You must grant notification permission to use this App.",
                positiveBtnText = "Settings",
                negativeBtnText = "Cancel",
                positiveAction = {
                    activity?.showAppSettings()
                }
            )
        }

        val isDeniedShowRationale = activity?.shouldShowRationaleFor(android.Manifest.permission.POST_NOTIFICATIONS) == true
        if (isDeniedShowRationale) {
            notificationPermissionCount++
            showRationale()
            return@registerForActivityResult
        }

        if (isGranted.not()) {
            if (notificationPermissionCount >= 1) {
                showRationale()
            } else {
                askNotificationPermission()
            }
            return@registerForActivityResult
        }

        AppPreferences.getInstance().hasNotificationPermission = true
        AppPreferences.getInstance().hasAlarmPermission = (context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupUI()
        binding.setupUserActionListeners()
        observeForData()
    }

    private fun FragmentMainBinding.setupUI() {
        askPermissions()
        activity?.window?.navigationBarColor = requireContext().getColor(R.color.white)
        rvSubjects.apply {
            layoutAnimation = rvSubjects.context.layoutAnimationController(globalLayoutAnimation)
            layoutManager = LinearLayoutManager(context)
            adapter = subjectsAdapter
        }
        layoutSearch.etSearch.hint = "Search subjects"
        layoutAddItem.etItem.hint = "Add subject"
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentMainBinding.setupUserActionListeners() {
        root.setOnClickListener {}

        ivHeaderMore.onSafeClick { pair: Pair<View?, Boolean> ->
            val optionsList = listOf(
                Pair("Delete All", R.drawable.outline_delete_24),
            )
            requireContext().showPopupMenuWithIcons(
                view = pair.first,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItemText = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        requireContext().showAlertDialog(
                            message = "Delete all subjects? You cannot undo this action.",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveBtnColor = R.color.md_red_700,
                            positiveAction = {
                                viewModel.deleteAllSubjects()
                            }
                        )
                    }
                }
            }
        }

        subjectsAdapter.setOnItemClickListener { subject, position ->
            if (AndroidVersions.isTiramisu()) {
                if (activity?.hasNotificationsPermission()?.not() == true) {
                    askNotificationPermission()
                    return@setOnItemClickListener
                }
            }

            if ((context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager).canScheduleExactAlarms().not()) {
                context?.askAlarmPermission()
                return@setOnItemClickListener
            }

            if (layoutAddItem.etItem.isFocused) {
                layoutAddItem.etItem.hideKeyboard()
            }
            (requireActivity() as MainActivity).showScreen(
                fragment = TopicFragment.newInstance(subject),
                tag = FragmentsTag.TOPIC,
                isAdd = true,
                enterAnim = R.anim.slide_to_left,
                exitAnim = R.anim.slide_to_right,
                popEnterAnim = R.anim.slide_to_left,
                popExitAnim = R.anim.slide_to_right,
            )
        }

        subjectsAdapter.setOnItemLongClickListener { subject, view, position ->
            val optionsList = listOf(
                Pair("Edit", R.drawable.outline_edit_24),
                Pair("Delete", R.drawable.outline_delete_24)
            )
            requireContext().showPopupMenuWithIcons(
                view = view,
                menuList = optionsList,
                customColor = R.color.md_red_700,
                customColorItemText = optionsList.last().first
            ) { it: MenuItem? ->
                when (it?.title?.toString()?.trim()) {
                    optionsList[0].first -> {
                        subjectsAdapter.showEditView(
                            recyclerView = rvSubjects,
                            adapterPosition = position
                        )
                    }

                    optionsList[1].first -> {
                        requireContext().showAlertDialog(
                            title = "Delete item",
                            message = "Subject \"${subject?.title}\" along with its Topics and Sub-Topics will be deleted permanently.",
                            positiveBtnText = "Delete",
                            negativeBtnText = "Cancel",
                            positiveBtnColor = R.color.md_red_700,
                            positiveAction = {
                                viewModel.deleteSubjectItem(subject)
                            }
                        )
                    }
                }
            }
        }

        subjectsAdapter.setOnApproveUpdateClickListener { subject, position ->
            subject ?: return@setOnApproveUpdateClickListener
            viewModel.updateSubjectItem(subject)
        }

        layoutGrantPermission.btnGivePermission.onSafeClick {
        }

        layoutSearch.etSearch.onImeClick {
            layoutSearch.etSearch.hideKeyboard()
        }

        layoutAddItem.etItem.onImeClick {
            layoutAddItem.etItem.hideKeyboard()
        }

        layoutSearch.ibClearSearch.onSafeClick {
            layoutSearch.etSearch.setText("")
        }

        layoutSearch.etSearch.doAfterTextChanged { query: Editable? ->
            layoutSearch.ibClearSearch.isVisible = query.isNullOrBlank().not()
            if (query.isNullOrBlank()) {
                subjectsAdapter.subjectList = subjectList
                subjectsAdapter.notifyDataSetChanged()
                return@doAfterTextChanged
            }
            subjectsAdapter.subjectList = subjectList.filter { it?.title?.contains(other = query, ignoreCase = true) == true }
            subjectsAdapter.notifyDataSetChanged()
        }

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                activity?.onBackPressedDispatcher?.onBackPressed()
//            }
//        })

        parentFragmentManager.setFragmentResultListener(
            /* requestKey = */ FragmentResultKey.ADD_SUBJECT,
            /* lifecycleOwner = */ viewLifecycleOwner
        ) { _, bundle: Bundle ->
            val subject = bundle.getString(FragmentResultBundleKey.SUBJECT)
            viewModel.addSubjectItem(Subject(title = subject ?: ""))
        }

        layoutAddItem.ibAddItem.onSafeClick {
            val subjectTitle = layoutAddItem.etItem.text.toString().trim()
            if (subjectTitle.isBlank()) return@onSafeClick
            viewModel.addSubjectItem(Subject(title = subjectTitle))
            layoutAddItem.etItem.setText("")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        (activity as? MainActivity)?.collectLatestLifecycleFlow(flow = viewModel.getAllSubjectItemsFlow()) { list: List<Subject?> ->
            this.subjectList = list
            subjectsAdapter.subjectList = subjectList
            subjectsAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() {
        notificationPermissionResult.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun askPermissions() {
        if (AppPreferences.getInstance().hasNotificationPermission && AppPreferences.getInstance().hasAlarmPermission) return
        requireContext().showAlertDialog(
            title = "Grant permissions",
            message = "Please grant permission to \n* Show Notifications and \n* Schedule Alarms",
            positiveBtnText = "Grant",
            positiveAction = {
                askNotificationPermission()
            }
        )
    }
}