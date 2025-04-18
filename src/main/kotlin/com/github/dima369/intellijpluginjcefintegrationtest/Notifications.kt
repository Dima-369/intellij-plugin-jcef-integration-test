package com.github.dima369.intellijpluginjcefintegrationtest

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

object Notifications {
    private const val GROUP_ID = "Dima AI Notifications"

    fun showInfo(project: Project?, content: String, title: String = "Dima AI") {
        notify(project, content, title, NotificationType.INFORMATION)
    }

    fun showWarning(project: Project?, content: String, title: String = "Dima AI") {
        notify(project, content, title, NotificationType.WARNING)
    }

    fun showError(project: Project?, content: String, title: String = "Dima AI") {
        notify(project, content, title, NotificationType.ERROR)
    }

    private fun notify(
        project: Project?,
        content: String,
        title: String,
        type: NotificationType
    ) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(GROUP_ID)
            .createNotification(content, type)
            .setTitle(title)
            .notify(project)
    }
}
