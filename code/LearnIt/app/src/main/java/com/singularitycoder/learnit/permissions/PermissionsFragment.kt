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
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.databinding.FragmentPermissionsBinding
import com.singularitycoder.learnit.helpers.AndroidVersions
import com.singularitycoder.learnit.helpers.AppPreferences
import com.singularitycoder.learnit.helpers.canScheduleAlarms
import com.singularitycoder.learnit.helpers.constants.Permission
import com.singularitycoder.learnit.helpers.constants.FragmentsTag
import com.singularitycoder.learnit.helpers.hasNotificationsPermission
import com.singularitycoder.learnit.helpers.onSafeClick
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

        doWhenNotificationPermissionGranted()
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
            doWhenNotificationPermissionGranted()
        }

        if (context?.canScheduleAlarms() == true
            && AppPreferences.getInstance().hasAlarmPermission.not()
        ) {
            AppPreferences.getInstance().hasAlarmPermission = true
            binding.layoutAlarm.root.isVisible = false
            viewModel.setPermissionCount((viewModel.permissionCount.value ?: 0) + 1)
        }

        binding.layoutNotification.root.isVisible = AppPreferences.getInstance().hasNotificationPermission.not()
        binding.layoutAlarm.root.isVisible = AppPreferences.getInstance().hasAlarmPermission.not()
        binding.layoutBattery.root.isVisible = AppPreferences.getInstance().hasBatteryOptimisePermission.not()
        binding.layoutDnd.root.isVisible = AppPreferences.getInstance().hasDndPermission.not()
        binding.layoutStorage.root.isVisible = AppPreferences.getInstance().hasStoragePermission.not()
    }

    private fun FragmentPermissionsBinding.setupUI() {
        activity?.setNavigationBarColor(R.color.white)

        layoutCustomToolbar.apply {
            ibBack.isVisible = false
            ivMore.isVisible = false
            tvTitle.text = "Grant Permissions"
            tvCount.text = "This App requires these permissions to work properly."
        }

        if (AndroidVersions.isTiramisu().not()) {
            layoutNotification.root.isVisible = false
            AppPreferences.getInstance().hasNotificationPermission = true
        }

        layoutNotification.btnLater.isVisible = false
        layoutAlarm.btnLater.isVisible = false

        val layoutList = listOf(
            layoutNotification,
            layoutAlarm,
            layoutBattery,
            layoutDnd,
            layoutStorage
        )

        Permission.entries.forEachIndexed { index, permission ->
            layoutList.get(index).apply {
                tvTitle.text = root.context.getString(permission.title)
                tvSubtitle.text = root.context.getString(permission.subtitle)
                tvSubtitle2.text = root.context.getString(permission.requirementType)
            }
        }

//        layoutNotification.apply {
//            tvTitle.text = root.context.getString(R.string.perm_title_post_notif)
//            tvSubtitle.text = root.context.getString(R.string.perm_exp_post_notif)
//            tvSubtitle2.text = root.context.getString(R.string.essential)
//            btnLater.isVisible = false
//        }
//
//        layoutAlarm.apply {
//            tvTitle.text = root.context.getString(R.string.perm_title_exact_alarms)
//            tvSubtitle.text = root.context.getString(R.string.perm_expln_exact_alarms)
//            tvSubtitle2.text = root.context.getString(R.string.essential)
//            btnLater.isVisible = false
//        }
//
//        layoutBattery.apply {
//            tvTitle.text = root.context.getString(R.string.perm_title_ign_bat_optim)
//            tvSubtitle.text = root.context.getString(R.string.perm_exp_ign_bat_optim)
//            tvSubtitle2.text = root.context.getString(R.string.highly_recommended)
//        }
//
//        layoutDnd.apply {
//            tvTitle.text = root.context.getString(R.string.perm_title_notif_policy)
//            tvSubtitle.text = root.context.getString(R.string.perm_exp_notif_policy)
//            tvSubtitle2.text = root.context.getString(R.string.highly_recommended)
//        }
//
//        layoutStorage.apply {
//            tvTitle.text = root.context.getString(R.string.perm_title_storage_access)
//            tvSubtitle.text = root.context.getString(R.string.perm_exp_storage_access)
//            tvSubtitle2.text = root.context.getString(R.string.optional)
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun FragmentPermissionsBinding.setupUserActionListeners() {
        root.setOnClickListener {}

        layoutNotification.btnGrant.onSafeClick {
            askNotificationPermission()
        }

        layoutAlarm.btnGrant.onSafeClick {
            requestAlarmPermission()
        }

        layoutBattery.apply {
            btnGrant.onSafeClick {
                requestIgnoreBatteryOptimisePermission()
            }
            btnLater.onSafeClick {

            }
        }

        layoutDnd.apply {
            btnGrant.onSafeClick {
                requestNotificationPolicyPermission()
            }
            btnLater.onSafeClick {

            }
        }

        layoutStorage.apply {
            btnGrant.onSafeClick {

            }
            btnLater.onSafeClick {

            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeForData() {
        viewModel.permissionCount.observe(viewLifecycleOwner) { it: Int? ->
            if (it == 5) {
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

    private fun doWhenNotificationPermissionGranted() {
        AppPreferences.getInstance().hasNotificationPermission = true
        binding.layoutNotification.root.isVisible = false
        viewModel.setPermissionCount((viewModel.permissionCount.value ?: 0) + 1)
    }
}