package com.singularitycoder.learnit.subject.worker

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.singularitycoder.learnit.helpers.NotificationsHelper
import com.singularitycoder.learnit.helpers.constants.TEXT_FILE_TABLE_DIVIDER
import com.singularitycoder.learnit.helpers.constants.WorkerData
import com.singularitycoder.learnit.helpers.currentTimeMillis
import com.singularitycoder.learnit.helpers.db.LearnItDatabase
import com.singularitycoder.learnit.helpers.getDownloadDirectory
import com.singularitycoder.learnit.helpers.listToString
import com.singularitycoder.learnit.helpers.readFromTextFile
import com.singularitycoder.learnit.helpers.stringToList
import com.singularitycoder.learnit.helpers.toDateTime
import com.singularitycoder.learnit.helpers.writeToTextFile
import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.topic.model.Topic
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

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
            val uri = inputData.getString(WorkerData.URI)

            NotificationsHelper.createNotificationChannel(context)
            NotificationsHelper.createNotification(context = context, title = "Import Export Data")

            val successData: Data.Builder = Data.Builder()

            try {
                if (isImportData) {
                    NotificationsHelper.updateNotification(context = context, title = "Importing data")
                    val outputFile = File("${context.cacheDir}/learn_it_import_data.txt")
                    try {
                        context.contentResolver?.openInputStream(Uri.parse(uri)).use { inputStream ->
                            val fileOutputStream = FileOutputStream(outputFile)
                            fileOutputStream.write(inputStream?.readBytes())
                            fileOutputStream.flush()
                            fileOutputStream.close()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val data = readFromTextFile(inputFile = outputFile).split(TEXT_FILE_TABLE_DIVIDER)
                    val subjects = stringToList<Subject>(data.getOrNull(0))?.toList() ?: emptyList()
                    val topics = stringToList<Topic>(data.getOrNull(1))?.toList() ?: emptyList()
                    val subTopics = stringToList<SubTopic>(data.getOrNull(2))?.toList() ?: emptyList()
                    subjectDao.insertAll(subjects)
                    topicDao.insertAll(topics)
                    subTopicDao.insertAll(subTopics)
                    NotificationsHelper.updateNotification(context = context, title = "Finished Importing data")
                    successData.putBoolean(WorkerData.IS_EXPORT, false)
                } else {
                    NotificationsHelper.updateNotification(context = context, title = "Exporting data")
                    val subjects = listToString(ArrayList(subjectDao.getAll()))
                    val topics = listToString(ArrayList(topicDao.getAll()))
                    val subTopics = listToString(ArrayList(subTopicDao.getAll()))
                    val text = "$subjects\n$TEXT_FILE_TABLE_DIVIDER\n$topics\n$TEXT_FILE_TABLE_DIVIDER\n$subTopics"
                    val fileNameWithExtension = "learn_it_export_${currentTimeMillis.toDateTime(type = "dd_MMM_yyyy_h_mm_ss_a")}.txt"
                    writeToTextFile(
                        outputFile = getDownloadDirectory(),
                        text = text,
                        fileNameWithExtension = fileNameWithExtension
                    )
                    NotificationsHelper.updateNotification(
                        context = context,
                        title = "Exported data to \"Downloads\" folder.\n\nFile name: $fileNameWithExtension"
                    )
                    successData
                        .putBoolean(WorkerData.IS_EXPORT, true)
                        .putString(WorkerData.FILE_NAME, fileNameWithExtension)
                }

                Result.success(successData.build())
            } catch (_: Exception) {
                Result.failure()
            }
        }
    }

}