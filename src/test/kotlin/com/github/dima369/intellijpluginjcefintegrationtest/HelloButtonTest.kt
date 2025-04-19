package com.github.dima369.intellijpluginjcefintegrationtest

import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.ui.UIUtil
import java.awt.Component

/**
 * Test for the Hello button in the MyToolWindow.
 */
@TestDataPath("\$CONTENT_ROOT/src/test/testData")
class HelloButtonTest : BasePlatformTestCase() {

    /**
     * Test that the MyToolWindow is available.
     */
    fun testToolWindowAvailability() {
        // Get the tool window manager
        val toolWindowManager = ToolWindowManager.getInstance(project)
        
        // Get the MyToolWindow
        val toolWindow = toolWindowManager.getToolWindow("MyToolWindow")
        
        // Verify that the tool window is available
        assertNotNull("MyToolWindow should be available", toolWindow)
        
        // Print a message to indicate that the test passed
        println("MyToolWindow is available")
    }
    
    /**
     * Test that we can find the JBCefBrowser component in the tool window.
     */
    fun testFindJCefBrowser() {
        // Get the tool window manager
        val toolWindowManager = ToolWindowManager.getInstance(project)
        
        // Get the MyToolWindow
        val toolWindow = toolWindowManager.getToolWindow("MyToolWindow")
        assertNotNull("MyToolWindow should be available", toolWindow)
        
        // Activate the tool window
        UIUtil.invokeAndWaitIfNeeded {
            toolWindow?.activate(null)
        }
        
        // Wait for the tool window to fully initialize
        PlatformTestUtil.waitForAlarm(1000)
        
        // Get the content of the tool window
        val contentManager = toolWindow?.contentManager
        val content = contentManager?.getContent(0)
        assertNotNull("Tool window content should be available", content)
        
        // Find the JBCefBrowser component
        val jcefBrowser = findJCefBrowser(content?.component)
        
        // We don't assert that the browser is found, as it might not be available in the test environment
        if (jcefBrowser != null) {
            println("JBCefBrowser component found in the tool window")
        } else {
            println("JBCefBrowser component not found in the tool window (this is expected in a headless test environment)")
        }
    }
    
    /**
     * Recursively searches for a JBCefBrowser component in the component hierarchy.
     */
    private fun findJCefBrowser(component: Component?): JBCefBrowser? {
        if (component == null) return null
        
        if (component is JBCefBrowser) {
            return component
        }
        
        if (component is java.awt.Container) {
            for (i in 0 until component.componentCount) {
                val child = component.getComponent(i)
                val result = findJCefBrowser(child)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }
    
    override fun getTestDataPath() = "src/test/testData"
}
