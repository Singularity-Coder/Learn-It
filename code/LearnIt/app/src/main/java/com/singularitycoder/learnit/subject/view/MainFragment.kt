package com.singularitycoder.learnit.subject.view

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentMainBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.askAlarmPermission
import com.singularitycoder.learnit.helpers.canScheduleAlarms
import com.singularitycoder.learnit.helpers.clipboard
import com.singularitycoder.learnit.helpers.collectLatestLifecycleFlow
import com.singularitycoder.learnit.helpers.constants.FragmentResultBundleKey
import com.singularitycoder.learnit.helpers.constants.FragmentResultKey
import com.singularitycoder.learnit.helpers.constants.FragmentsTag
import com.singularitycoder.learnit.helpers.constants.WakeLockKey
import com.singularitycoder.learnit.helpers.constants.WorkerData
import com.singularitycoder.learnit.helpers.constants.WorkerTag
import com.singularitycoder.learnit.helpers.constants.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.hasNotificationsPermission
import com.singularitycoder.learnit.helpers.hasStoragePermissionApi30
import com.singularitycoder.learnit.helpers.hideKeyboard
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.onImeClick
import com.singularitycoder.learnit.helpers.onSafeClick
import com.singularitycoder.learnit.helpers.requestStoragePermissionApi30
import com.singularitycoder.learnit.helpers.shouldShowRationaleFor
import com.singularitycoder.learnit.helpers.showAlertDialog
import com.singularitycoder.learnit.helpers.showAppSettings
import com.singularitycoder.learnit.helpers.showPopupMenuWithIcons
import com.singularitycoder.learnit.helpers.showScreen
import com.singularitycoder.learnit.helpers.showSnackBar
import com.singularitycoder.learnit.helpers.showToast
import com.singularitycoder.learnit.tutorial.TutorialFragment
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subject.viewmodel.SubjectViewModel
import com.singularitycoder.learnit.subject.worker.ExportDataWorker
import com.singularitycoder.learnit.topic.view.TopicFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

    private var wakeLock: PowerManager.WakeLock? = null

    private val viewModel by viewModels<SubjectViewModel>()

    private var filePicker = registerForActivityResult<String, Uri>(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri ?: return@registerForActivityResult

        // TODO validate
        // content://com.android.providers.downloads.documents/document/raw%3A%2Fstorage%2Femulated%2F0%2FDownload%2Flearn_it_export_22_Sep_2024_11_19_51_PM.txt
        println("uri.authority: " + uri.authority) // com.android.providers.downloads.documents
        println("uri.scheme: " + uri.scheme) // content

        startImportExportDataWorker(isImportData = true, uri = uri)
    }

    @SuppressLint("InlinedApi")
    private val notificationPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? ->
        isGranted ?: return@registerForActivityResult

        fun showRationale() {
            requireContext().showAlertDialog(
                title = "Grant permission",
                message = "You must grant notification permission to use this App.",
                positiveBtnText = if (AppPreferences.getInstance().notifPermissionDeniedCount >= 1) {
                    "Settings"
                } else "Grant",
                negativeBtnText = "Cancel",
                positiveAction = {
                    if (AppPreferences.getInstance().notifPermissionDeniedCount >= 1) {
                        activity?.showAppSettings()
                    } else {
                        AppPreferences.getInstance().notifPermissionDeniedCount += 1
                        askNotificationPermission()
                    }
                },
                negativeAction = {
                    AppPreferences.getInstance().notifPermissionDeniedCount += 1
                }
            )
        }

        val isDeniedSoShowRationale = activity?.shouldShowRationaleFor(android.Manifest.permission.POST_NOTIFICATIONS) == true
        if (isDeniedSoShowRationale) {
            showRationale()
            return@registerForActivityResult
        }

        if (isGranted.not()) {
            if (AppPreferences.getInstance().notifPermissionDeniedCount >= 1) {
                showRationale()
            } else {
                askNotificationPermission()
            }
            return@registerForActivityResult
        }

        AppPreferences.getInstance().hasNotificationPermission = true
        AppPreferences.getInstance().hasAlarmPermission = context?.canScheduleAlarms() == true
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

    override fun onPause() {
        super.onPause()
        releaseWakeLock()
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
                Pair("Show Tutorial", R.drawable.outline_help_outline_24),
                Pair("Import Data", R.drawable.round_south_west_24),
                Pair("Export Data", R.drawable.round_north_east_24),
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
                        (activity as MainActivity).showScreen(
                            fragment = TutorialFragment.newInstance(),
                            tag = FragmentsTag.TUTORIAL,
                            isAdd = true,
                            isAddToBackStack = true
                        )
                    }

                    optionsList[1].first -> {
                        if (activity?.hasStoragePermissionApi30()?.not() == true) {
                            showStoragePermissionPopup()
                            return@showPopupMenuWithIcons
                        }

                        requireContext().showAlertDialog(
                            title = "Import Data",
                            message = "Importing data will replace all existing data. You cannot undo this action.",
                            positiveBtnText = "Import",
                            negativeBtnText = "Cancel",
                            positiveBtnColor = R.color.md_red_700,
                            positiveAction = {
                                filePicker.launch("text/plain")
                            }
                        )
                    }

                    optionsList[2].first -> {
                        if (activity?.hasStoragePermissionApi30()?.not() == true) {
                            showStoragePermissionPopup()
                            return@showPopupMenuWithIcons
                        }

                        lifecycleScope.launch {
                            if (viewModel.hasSubjects().not()) {
                                context?.showToast("Nothing to export.")
                                return@launch
                            }

                            withContext(Dispatchers.Main) {
                                startImportExportDataWorker(isImportData = false)
                            }
                        }
                    }

                    optionsList[3].first -> {
                        requireContext().showAlertDialog(
                            message = "Delete all Subjects, Topics and Sub-Topics? You cannot undo this action.",
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
                    AppPreferences.getInstance().hasNotificationPermission = false
                    askNotificationPermission()
                    return@setOnItemClickListener
                }
            }

            if (context?.canScheduleAlarms()?.not() == true) {
                AppPreferences.getInstance().hasAlarmPermission = false
                context?.askAlarmPermission()
                return@setOnItemClickListener
            }

            AppPreferences.getInstance().hasNotificationPermission = true
            AppPreferences.getInstance().hasAlarmPermission = true

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

    private fun showStoragePermissionPopup() {
        requireContext().showAlertDialog(
            title = "Grant Storage Permission",
            message = "Please grant storage permission for \"Downloads\" folder to import or export data.",
            positiveBtnText = "Grant",
            negativeBtnText = "Cancel",
            positiveAction = {
                requireActivity().requestStoragePermissionApi30()
            }
        )
    }

    @SuppressLint("InlinedApi")
    private fun askNotificationPermission() {
        notificationPermissionResult.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun askPermissions() {
        if (AndroidVersions.isTiramisu().not()) return
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

    @SuppressLint("WakelockTimeout")
    private fun startImportExportDataWorker(isImportData: Boolean, uri: Uri? = null) {
        /** This is to make sure books are loading even if screen is turned off. Keeps CPU awake. */
        wakeLock = (requireContext().getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakeLockKey.IMPORT_EXPORT_DATA).apply {
                acquire()
            }
        }

        val data = Data.Builder()
            .putBoolean(WorkerData.IS_IMPORT_DATA, isImportData)
            .putString(WorkerData.URI, uri.toString())
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ExportDataWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setInputData(data)
            .build()
        WorkManager.getInstance(requireContext()).enqueueUniqueWork(WorkerTag.IMPORT_EXPORT_DATA, ExistingWorkPolicy.KEEP, workRequest)
        WorkManager.getInstance(requireContext()).getWorkInfoByIdLiveData(workRequest.id).observe(viewLifecycleOwner) { workInfo: WorkInfo? ->
            when (workInfo?.state) {
                WorkInfo.State.RUNNING -> Unit
                WorkInfo.State.ENQUEUED -> Unit
                WorkInfo.State.SUCCEEDED -> {
                    releaseWakeLock()
                    val isExport = workInfo.outputData.getBoolean(WorkerData.IS_EXPORT, false)
                    val fileName = workInfo.outputData.getString(WorkerData.FILE_NAME)
                    if (isExport) {
                        requireContext().showAlertDialog(
                            title = "Export Complete",
                            message = "Exported data to \"Downloads\" folder.\n\nFile name: $fileName",
                            positiveBtnText = "Okay",
                            negativeBtnText = "Copy",
                            negativeAction = {
                                context?.clipboard()?.text = fileName
                            }
                        )
                    } else {
                        requireContext().showAlertDialog(
                            message = "Import Complete",
                            positiveBtnText = "Okay"
                        )
                    }
                }

                WorkInfo.State.FAILED -> {
                    releaseWakeLock()
                    binding.root.showSnackBar("Something went wrong. Please try again.")
                }

                WorkInfo.State.BLOCKED -> Unit
                WorkInfo.State.CANCELLED -> {
                    releaseWakeLock()
                }

                else -> Unit
            }
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }
}