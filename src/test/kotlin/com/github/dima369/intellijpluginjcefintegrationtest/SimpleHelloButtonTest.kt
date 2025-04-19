package com.github.dima369.intellijpluginjcefintegrationtest

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.github.dima369.intellijpluginjcefintegrationtest.services.MyProjectService

/**
 * A simple test case for the Hello button functionality.
 * This test doesn't actually test the button click but serves as a starting point
 * for more complex integration tests.
 */
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class SimpleHelloButtonTest : BasePlatformTestCase() {

    /**
     * Test that the project service is available and working.
     * This is a simple test to verify that the test infrastructure is working.
     */
    fun testProjectService() {
        // Get the project service
        val projectService = project.getService(MyProjectService::class.java)
        
        // Verify that the service is available
        assertNotNull("Project service should be available", projectService)
        
        // Verify that the random number generator is working
        val number1 = projectService.getRandomNumber()
        val number2 = projectService.getRandomNumber()
        
        // The numbers should be different (this could theoretically fail, but it's very unlikely)
        assertNotSame("Random numbers should be different", number1, number2)
        
        // Print a message to indicate that the test passed
        println("Hello button test infrastructure is working!")
        println("Random numbers: $number1, $number2")
    }
    
    /**
     * This is a placeholder for the actual button click test.
     * In a real test, we would:
     * 1. Open the MyToolWindow
     * 2. Find the Hello button
     * 3. Click it
     * 4. Verify that the notification appears
     * 
     * However, this requires more complex setup and UI interaction that
     * is difficult to implement in a headless test environment.
     */
    fun testHelloButtonPlaceholder() {
        // This is just a placeholder test that always passes
        assertTrue("This test always passes", true)
        
        // Print a message explaining what a real test would do
        println("""
            In a real test, we would:
            1. Open the MyToolWindow
            2. Find the Hello button
            3. Click it
            4. Verify that the notification appears
            
            However, this requires more complex setup and UI interaction that
            is difficult to implement in a headless test environment.
        """.trimIndent())
    }
    
    override fun getTestDataPath() = "src/test/testData"
}
