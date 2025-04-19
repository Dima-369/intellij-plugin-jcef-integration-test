package com.github.dima369.intellijpluginjcefintegrationtest

import com.github.dima369.intellijpluginjcefintegrationtest.Notifications
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.junit.Test

/**
 * Test for the Hello button functionality using JUnit 4 annotations.
 */
class JUnit4HelloButtonTest : BasePlatformTestCase() {

    /**
     * Test that the notification system works correctly.
     * This simulates what happens when the Hello button is clicked.
     */
    @Test
    fun testNotification() {
        // Show a notification similar to what happens when the Hello button is clicked
        Notifications.showInfo(project, "Copied to clipboard: hello")
        
        // If we get here without exceptions, the test passes
        println("Notification test passed")
    }
}
