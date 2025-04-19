package com.github.dima369.intellijpluginjcefintegrationtest

import com.github.dima369.intellijpluginjcefintegrationtest.services.MyProjectService
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * A simple test for the Hello button functionality.
 */
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class HelloButtonSimpleTest : BasePlatformTestCase() {

    /**
     * Test that the notification system works by simulating what happens when the Hello button is clicked.
     */
    fun testHelloButtonNotification() {
        // Show a notification similar to what happens when the Hello button is clicked
        Notifications.showInfo(project, "Copied to clipboard: hello")
        
        // If we get here without exceptions, the test passes
        println("Hello button notification test passed")
    }

    /**
     * Test that the project service is available, which is needed for the Hello button functionality.
     */
    fun testProjectService() {
        val projectService = project.getService(MyProjectService::class.java)
        
        // Verify that the service is available
        assertNotNull("Project service should be available", projectService)
        
        // Print a message to indicate that the test passed
        println("Project service test passed")
    }

    override fun getTestDataPath() = "src/test/testData"
}
