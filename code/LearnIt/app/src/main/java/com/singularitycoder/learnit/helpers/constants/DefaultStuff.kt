package com.singularitycoder.learnit.helpers.constants

import com.singularitycoder.learnit.subject.model.Subject
import com.singularitycoder.learnit.subtopic.model.SubTopic
import com.singularitycoder.learnit.topic.model.Topic

val DEFAULT_SUBJECTS = listOf(
    Subject(
        id = 1,
        title = "Data Science 🧑🏻‍💻"
    )
)

val DEFAULT_TOPICS = listOf(
    Topic(
        id = 1,
        title = "Numpy 💯",
        subjectId = 1,
        studyMaterial = "W3 Schools",
        dateStarted = 0L,
        nextSessionDate = 0L,
        finishedSessions = 0,
    ),
    Topic(
        id = 2,
        title = "Pandas 🐼",
        subjectId = 1,
        studyMaterial = "Scaler Academy",
        dateStarted = 0L,
        nextSessionDate = 0L,
        finishedSessions = 0,
    ),
    Topic(
        id = 3,
        title = "Matplotlib 📈",
        subjectId = 1,
        studyMaterial = "Vector School",
        dateStarted = 0L,
        nextSessionDate = 0L,
        finishedSessions = 0,
    )
)

val DEFAULT_SUB_TOPICS = listOf(
    SubTopic(
        id = 1,
        topicId = 1,
        subjectId = 1,
        title = "Create Array",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 2,
        topicId = 1,
        subjectId = 1,
        title = "Array Indexing",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 3,
        topicId = 1,
        subjectId = 1,
        title = "Array Slicing",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 4,
        topicId = 1,
        subjectId = 1,
        title = "Array Reshape",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 5,
        topicId = 1,
        subjectId = 1,
        title = "Array Sort",
        isCorrectRecall = false
    ),

    SubTopic(
        id = 6,
        topicId = 2,
        subjectId = 1,
        title = "Create Series",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 7,
        topicId = 2,
        subjectId = 1,
        title = "Create DataFrame",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 8,
        topicId = 2,
        subjectId = 1,
        title = "Read CSV",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 9,
        topicId = 2,
        subjectId = 1,
        title = "Removing Duplicates",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 10,
        topicId = 2,
        subjectId = 1,
        title = "Remove Empty Cells",
        isCorrectRecall = false
    ),

    SubTopic(
        id = 11,
        topicId = 3,
        subjectId = 1,
        title = "Create Bar Plot",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 12,
        topicId = 3,
        subjectId = 1,
        title = "Create Histogram",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 13,
        topicId = 3,
        subjectId = 1,
        title = "Create Box Plot",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 14,
        topicId = 3,
        subjectId = 1,
        title = "Create Pie Chart",
        isCorrectRecall = false
    ),
    SubTopic(
        id = 15,
        topicId = 3,
        subjectId = 1,
        title = "Create Scatter Plot",
        isCorrectRecall = false
    )
)