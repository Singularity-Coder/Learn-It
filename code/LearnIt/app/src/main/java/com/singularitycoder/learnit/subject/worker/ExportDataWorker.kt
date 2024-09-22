package com.singularitycoder.learnit.subject.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.singularitycoder.learnit.helpers.constants.TEXT_FILE_TABLE_DIVIDER
import com.singularitycoder.learnit.helpers.constants.WorkerData
import com.singularitycoder.learnit.helpers.currentTimeMillis
import com.singularitycoder.learnit.helpers.db.LearnItDatabase
import com.singularitycoder.learnit.helpers.getDownloadDirectory
import com.singularitycoder.learnit.helpers.listToString
import com.singularitycoder.learnit.helpers.readFromTextFile
import com.singularitycoder.learnit.helpers.toDateTime
import com.singularitycoder.learnit.helpers.writeToTextFile
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

/** For setting progress - https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/observe */
class ExportDataWorker(val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ThisEntryPoint {
        fun db(): LearnItDatabase
    }

    override suspend fun doWork(): Result {
        return withContext(IO) {
            val appContext = context.applicationContext ?: throw IllegalStateException()
            val dbEntryPoint = EntryPointAccessors.fromApplication(appContext, ThisEntryPoint::class.java)

            val subjectDao = dbEntryPoint.db().subjectDao()
            val topicDao = dbEntryPoint.db().topicDao()
            val subTopicDao = dbEntryPoint.db().subTopicDao()

            val isImportData = inputData.getBoolean(WorkerData.IS_IMPORT_DATA, false)

            // TODO show notification on start

            try {
                val subjects = listToString(ArrayList(subjectDao.getAll()))
                val topics = listToString(ArrayList(topicDao.getAll()))
                val subTopics = listToString(ArrayList(subTopicDao.getAll()))

                if (isImportData) {
                    // TODO file selection
//                    readFromTextFile(
//                        inputFile = File("${getDownloadDirectory().absolutePath}/")
//                    )
                } else {
                    val text = "$subjects$TEXT_FILE_TABLE_DIVIDER$topics$TEXT_FILE_TABLE_DIVIDER$subTopics"
                    writeToTextFile(
                        outputFile = getDownloadDirectory(),
                        text = text,
                        fileNameWithExtension = "learn_it_export_${currentTimeMillis.toDateTime(type = "dd_MMM_yyyy_h_mm_ss_a")}.txt"
                    )
                }

                // TODO show notification on completion

                Result.success()
            } catch (_: Exception) {
                Result.failure()
            }
        }
    }

}