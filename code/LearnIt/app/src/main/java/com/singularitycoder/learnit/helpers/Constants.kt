package com.singularitycoder.learnit.helpers

import com.singularitycoder.learnit.BuildConfig
import com.singularitycoder.learnit.MainFragment
import com.singularitycoder.learnit.R

const val FILE_PROVIDER = "${BuildConfig.APPLICATION_ID}.fileprovider"

val globalLayoutAnimation = R.anim.layout_animation_fall_down
val globalSlideToBottomAnimation = R.anim.layout_animation_fall_down
val globalSlideToTopAnimation = R.anim.layout_animation_slide_from_bottom

object FragmentResultKey {
}

object FragmentResultBundleKey {
}

object WakeLockKey {
    const val LOADING_BOOKS = "${BuildConfig.APPLICATION_ID}:LOADING_BOOKS"
    const val PLAYING_BOOK = "${BuildConfig.APPLICATION_ID}:PLAYING_BOOK"
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
}

object IntentExtraKey {
    const val NOTIF_BTN_CLICK_TYPE = "NOTIF_BTN_CLICK_TYPE"
    const val NOTIF_BTN_CLICK_TYPE_2 = "NOTIF_BTN_CLICK_TYPE_2"
}

object IntentExtraValue {
    const val UNBIND = "UNBIND"
    const val FOREGROUND_SERVICE_READY = "UPDATE_PROGRESS"
}

object Db {
    const val PLAY_BOOKS = "db_play_books"
}

object DbTable {
    const val BOOK = "table_book"
    const val BOOK_DATA = "table_book_data"
}

object BroadcastKey {
}

object FragmentsTag {
    val MAIN: String = MainFragment::class.java.simpleName
}

object TtsTag {
    const val UID_SPEAK: String = "UID_SPEAK"
}

object BottomSheetTag {
}

object WorkerData {
    const val KEY_PROGRESS = "KEY_PROGRESS"
}

object WorkerTag {
    const val PDF_TO_TEXT_CONVERTER = "PDF_TO_TEXT_CONVERTER"
}