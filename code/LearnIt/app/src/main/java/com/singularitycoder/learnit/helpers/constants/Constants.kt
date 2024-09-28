package com.singularitycoder.learnit.helpers.constants

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.singularitycoder.learnit.BuildConfig
import com.singularitycoder.learnit.R
import com.singularitycoder.learnit.intro.TutorialFragment
import com.singularitycoder.learnit.subject.view.MainFragment
import com.singularitycoder.learnit.subtopic.view.AddSubTopicFragment
import com.singularitycoder.learnit.topic.view.TopicFragment
import kotlinx.parcelize.Parcelize

const val FILE_PROVIDER = "${BuildConfig.APPLICATION_ID}.fileprovider"
const val TEXT_FILE_ITEM_DIVIDER = "--------iiiiiiii--------"
const val TEXT_FILE_TABLE_DIVIDER = "--------tttttttt--------"

val globalLayoutAnimation = R.anim.layout_animation_fall_down
val globalSlideToBottomAnimation = R.anim.layout_animation_fall_down
val globalSlideToTopAnimation = R.anim.layout_animation_slide_from_bottom

object FragmentResultKey {
    const val ADD_SUBJECT = "ADD_SUBJECT"
    const val ADD_TOPIC = "ADD_TOPIC"
    const val SHOW_KONFETTI = "SHOW_KONFETTI"
}

object FragmentResultBundleKey {
    const val SUBJECT = "SUBJECT"
    const val TOPIC = "TOPIC"
    const val SUB_TOPIC = "SUB_TOPIC"
}

object WakeLockKey {
    const val IMPORT_EXPORT_DATA = "${BuildConfig.APPLICATION_ID}:EXPORT_DATA"
    const val IMPORT_DATA = "${BuildConfig.APPLICATION_ID}:IMPORT_DATA"
}

enum class NotificationAction {
    PREVIOUS_SENTENCE,
    NEXT_SENTENCE,
    PLAY_PAUSE,
    PREVIOUS_PAGE,
    NEXT_PAGE
}

object IntentKey {
    const val NOTIF_BTN_CLICK_BROADCAST = "NOTIF_BTN_CLICK_BROADCAST"
    const val NOTIF_BTN_CLICK_BROADCAST_2 = "NOTIF_BTN_CLICK_BROADCAST_2"
    const val MAIN_BROADCAST_FROM_SERVICE = "MAIN_BROADCAST_FROM_SERVICE"
    const val ALARM_SETTINGS_BROADCAST = "ALARM_SETTINGS_BROADCAST"
    const val REVISION_ALARM = "${BuildConfig.APPLICATION_ID}.revision_alarm"
}

object IntentExtraKey {
    const val NOTIF_BTN_CLICK_TYPE = "NOTIF_BTN_CLICK_TYPE"
    const val NOTIF_BTN_CLICK_TYPE_2 = "NOTIF_BTN_CLICK_TYPE_2"
    const val SHOW_ALARM_VIEW = "SHOW_ALARM_VIEW"
    const val TOPIC_ID = "TOPIC_ID"
    const val TOPIC_ID_2 = "TOPIC_ID_2"
    const val TOPIC_ID_3 = "TOPIC_ID_3"
    const val CANNOT_SET_ALARM = "CANNOT_SET_ALARM"
}

object IntentExtraValue {
    const val SHOW_ALARM_VIEW = "SHOW_ALARM_VIEW"
    const val UNBIND = "UNBIND"
    const val FOREGROUND_SERVICE_READY = "UPDATE_PROGRESS"
}

object Db {
    const val LEARN_IT = "db_learn_it"
}

object DbTable {
    const val SUBJECT = "table_subject"
    const val TOPIC = "table_topic"
    const val SUB_TOPIC = "table_sub_topic"
}

object BroadcastKey {
}

object FragmentsTag {
    val MAIN: String = MainFragment::class.java.simpleName
    val TUTORIAL: String = TutorialFragment::class.java.simpleName
    val TOPIC: String = TopicFragment::class.java.simpleName
    val ADD_SUB_TOPIC: String = AddSubTopicFragment::class.java.simpleName
}

object TtsTag {
    const val UID_SPEAK: String = "UID_SPEAK"
}

object BottomSheetTag {
    const val TAG_EDIT = "TAG_EDIT_BOTTOM_SHEET"
    const val TAG_SUB_TOPICS = "TAG_SUB_TOPICS_BOTTOM_SHEET"
}

object WorkerData {
    const val IS_IMPORT_DATA = "IS_EXPORT"
    const val URI = "URI"
    const val FILE_NAME = "FILE_NAME"
    const val IS_EXPORT = "IS_EXPORT"
}

object WorkerTag {
    const val IMPORT_EXPORT_DATA = "EXPORT_DATA"
}

@Parcelize
enum class EditEvent : Parcelable {
    ADD_TOPIC,
    UPDATE_TOPIC,
}

val gifList = listOf(
    R.drawable.gif1,
    R.drawable.gif2,
    R.drawable.gif3,
    R.drawable.gif4
)

enum class Tutorial(
    @DrawableRes val image: Int,
    val title: String,
    @StringRes val subTitle: Int
) {
    WELCOME(image = R.drawable.tut0, title = "Hello", subTitle = R.string.tut1),
    HOME(image = R.drawable.tut1, title = "Subjects", subTitle = R.string.tut2),
    TOPIC(image = R.drawable.tut2, title = "Topics", subTitle = R.string.tut3),
    SUB_TOPIC(image = R.drawable.tut3, title = "Sub-Topics", subTitle = R.string.tut4),
    ALARM(image = R.drawable.tut4, title = "Reminders", subTitle = R.string.tut5),
}