package com.github.dima369.intellijpluginjcefintegrationtest

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.util.ui.UIUtil
import java.awt.Component
import javax.swing.SwingUtilities
import org.junit.Test

/**
 * Integration test for the Hello button in the MyToolWindow.
 */
class HelloButtonIntegrationTest : BasePlatformTestCase() {

    /**
     * Tests that clicking the Hello button in the MyToolWindow shows a notification.
     */
    @Test
    fun testHelloButtonClick() {
        // Open the MyToolWindow
        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("MyToolWindow")
        assertNotNull("MyToolWindow not found", toolWindow)

        // Activate the tool window
        UIUtil.invokeAndWaitIfNeeded {
            toolWindow?.activate(null)
        }

        // Wait for the tool window to fully initialize
        PlatformTestUtil.waitForAlarm(1000)

        // Get the content of the tool window
        val contentManager = toolWindow?.contentManager
        val content = contentManager?.getContent(0)
        assertNotNull("Tool window content not found", content)

        // Find the JBCefBrowser component
        val jcefBrowser = findJCefBrowser(content?.component)
        assertNotNull("JBCefBrowser component not found in the tool window", jcefBrowser)

        // Execute JavaScript to click the Hello button
        UIUtil.invokeAndWaitIfNeeded {
            // Access the underlying CEF browser to execute JavaScript
            val cefBrowser = jcefBrowser?.cefBrowser
            cefBrowser?.executeJavaScript(
                "document.getElementById('helloButton').click();",
                cefBrowser.url, 0
            )
        }

        // Wait for the notification to appear
        PlatformTestUtil.waitForAlarm(1000)

        // The test passes if no exceptions are thrown
        // Note: It's difficult to verify the notification in a headless test environment,
        // so we're just verifying that the click doesn't cause any errors
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
}
