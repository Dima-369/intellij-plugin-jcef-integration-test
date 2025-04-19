package com.github.dima369.intellijpluginjcefintegrationtest

import com.github.dima369.intellijpluginjcefintegrationtest.Notifications
import com.intellij.notification.NotificationType
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Test for the notification functionality used by the Hello button.
 */
class NotificationTest : BasePlatformTestCase() {

    /**
     * Test that the notification system works correctly.
     * This simulates what happens when the Hello button is clicked.
     */
    fun testNotification() {
        // Show a notification similar to what happens when the Hello button is clicked
        Notifications.showInfo(project, "Copied to clipboard: hello")
        
        // If we get here without exceptions, the test passes
        println("Notification test passed")
    }
}
