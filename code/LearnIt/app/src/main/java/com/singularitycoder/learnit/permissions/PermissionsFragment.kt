package com.singularitycoder.learnit.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentPermissionsBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.canScheduleAlarms
import com.singularitycoder.learnit.helpers.constants.FragmentsTag
import com.singularitycoder.learnit.helpers.constants.globalLayoutAnimation
import com.singularitycoder.learnit.helpers.hasNotificationsPermission
import com.singularitycoder.learnit.helpers.layoutAnimationController
import com.singularitycoder.learnit.helpers.setNavigationBarColor
import com.singularitycoder.learnit.helpers.shouldShowRationaleFor
import com.singularitycoder.learnit.helpers.showNotificationPermissionRationalePopup
import com.singularitycoder.learnit.helpers.showNotificationSettingsPopup
import com.singularitycoder.learnit.helpers.showScreen
import com.singularitycoder.learnit.subject.view.MainActivity
import com.singularitycoder.learnit.subject.view.MainFragment
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

    @SuppressLint("InlinedApi")
    private val notificationPermissionResult = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean? ->
        isGranted ?: return@registerForActivityResult

        val isDeniedSoShowRationale = activity?.shouldShowRationaleFor(android.Manifest.permission.POST_NOTIFICATIONS) == true
        if (isDeniedSoShowRationale) {
            AppPreferences.getInstance().notifPermissionDeniedCount += 1
            activity?.showNotificationPermissionRationalePopup {
                askNotificationPermission()
            }
            return@registerForActivityResult
        }

        if (isGranted.not()) {
            if (AppPreferences.getInstance().notifPermissionDeniedCount >= 1) {
                activity?.showNotificationSettingsPopup()
            } else {
                askNotificationPermission()
            }
            return@registerForActivityResult
        }

        AppPreferences.getInstance().hasNotificationPermission = true
        AppPreferences.getInstance().hasAlarmPermission = context?.canScheduleAlarms() == true
        viewModel.updateList(position = 0)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (
                intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
                || intent.action == NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED
            ) {
//                if (!Objects.isNull(viewModel.getCurrentPermission())) {
//                    onPermissionGranted()
//                }
            }
        }
    }

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

    override fun onResume() {
        super.onResume()
        if (AndroidVersions.isTiramisu()
            && activity?.hasNotificationsPermission() == true
            && AppPreferences.getInstance().hasNotificationPermission.not()
        ) {
            askNotificationPermission()
        }
    }

    private fun FragmentPermissionsBinding.setupUI() {
        activity?.setNavigationBarColor(R.color.white)
        layoutCustomToolbar.apply {
            ibBack.isVisible = false
            ivMore.isVisible = false
            tvTitle.text = "Grant Permissions"
            tvCount.text = "This App requires these permissions to work properly."
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

        permissionsAdapter.setOnGrantClickListener { permission, position ->
            when (permission.permissionName) {
                Manifest.permission.POST_NOTIFICATIONS -> askNotificationPermission()
                Manifest.permission.SCHEDULE_EXACT_ALARM -> requestAlarmPermission()
                Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS -> requestIgnoreBatteryOptimisePermission()
                Manifest.permission.ACCESS_NOTIFICATION_POLICY -> requestNotificationPolicyPermission()
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION -> {}
            }
        }

        permissionsAdapter.setOnLaterClickListener { permission, position ->
            viewModel.updateList(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        viewModel.permissionsList.observe(viewLifecycleOwner) { list: List<Permission>? ->
            this.permissionList = list ?: emptyList()
            permissionsAdapter.permissionList = permissionList
            permissionsAdapter.notifyDataSetChanged()
            if (permissionList.isEmpty()) {
                (activity as MainActivity).showScreen(
                    fragment = MainFragment.newInstance(),
                    tag = FragmentsTag.MAIN,
                    isAdd = true,
                    isAddToBackStack = false
                )
            }
        }
    }

    private fun askNotificationPermission() {
        notificationPermissionResult.launch(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * Requests the [Manifest.permission.SCHEDULE_EXACT_ALARM] permission.
     * Has to be checked via [AlarmManager.canScheduleExactAlarms].
     * Can only be requested via Settings.
     * Broadcasts
     * [AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED] when
     * granted.
     */
    private fun requestAlarmPermission() {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            data = Uri.Builder()
                .scheme("package")
                .opaquePart(activity?.packageName)
                .build()
        }
        registerReceiver(
            requireContext(),
            broadcastReceiver,
            IntentFilter(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
        startActivity(intent)
    }

    /**
     * Requests the [Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS]
     * permission.
     * Has to be checked via [PowerManager.isIgnoringBatteryOptimizations].
     * Can only be requested via Settings. See docs of
     * [Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS].
     */
    private fun requestIgnoreBatteryOptimisePermission() {
        val intent = Intent().apply {
            action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//            setData(Uri.parse("package:${activity?.packageName}"))
        }
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            val intent2 = Intent().apply {
                action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
            }
            startActivity(intent2)
        }
    }

    /**
     * Requests the [Manifest.permission.ACCESS_NOTIFICATION_POLICY] permission
     * (DND
     * override permission).
     *
     * Has to be checked via
     * [NotificationManager.isNotificationPolicyAccessGranted].
     *
     * Can only be asked via Settings. For details, see docs of
     * [NotificationManager.isNotificationPolicyAccessGranted].
     *
     * Broadcasts
     * [NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED] when
     * granted.
     */
    private fun requestNotificationPolicyPermission() {
        val intent = Intent().apply {
            action = Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
        }
        registerReceiver(
            requireContext(),
            broadcastReceiver,
            IntentFilter(NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED),
            ContextCompat.RECEIVER_EXPORTED
        )
        startActivity(intent)
    }
}