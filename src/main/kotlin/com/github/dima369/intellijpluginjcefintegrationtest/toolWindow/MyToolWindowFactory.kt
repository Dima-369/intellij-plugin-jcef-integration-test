package com.github.dima369.intellijpluginjcefintegrationtest.toolWindow

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowser
import com.intellij.ui.jcef.JBCefJSQuery
import com.github.dima369.intellijpluginjcefintegrationtest.MyBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.ide.CopyPasteManager
import java.awt.datatransfer.StringSelection

class MyToolWindowFactory : ToolWindowFactory {

    init {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val myToolWindow = MyToolWindow(project, toolWindow)
        val content = ContentFactory.getInstance().createContent(myToolWindow.getContent(), null, false)
        toolWindow.contentManager.addContent(content)
    }

    override fun shouldBeAvailable(project: Project) = true

    class MyToolWindow(private val project: Project, toolWindow: ToolWindow) {

        private val browser = JBCefBrowser()

        init {
            // Create HTML content with a button
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 20px;
                            background-color: var(--background-color);
                            color: var(--text-color);
                        }
                        button {
                            padding: 10px 20px;
                            font-size: 16px;
                            cursor: pointer;
                            background-color: var(--button-background);
                            color: var(--button-text);
                            border: 1px solid var(--button-border);
                            border-radius: 4px;
                        }
                        button:hover {
                            background-color: var(--button-hover-background);
                        }
                    </style>
                </head>
                <body>
                    <button id="helloButton">${MyBundle.message("helloButton")}</button>

                    <script>
                        // Will be replaced with the actual query code
                        let copyToClipboard;

                        document.getElementById('helloButton').addEventListener('click', function() {
                            copyToClipboard('hello');
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            // Create a JavaScript query to handle the button click
            val copyToClipboardQuery = JBCefJSQuery.create(browser)
            copyToClipboardQuery.addHandler { text ->
                // Copy the text to clipboard
                CopyPasteManager.getInstance().setContents(StringSelection(text))

                // Show notification
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("JCEF Integration")
                    .createNotification(
                        MyBundle.message("copiedToClipboard", text),
                        NotificationType.INFORMATION
                    )
                    .notify(project)

                null
            }

            // Inject the JavaScript query into the HTML
            val jsCode = """window.copyToClipboard = function(text) {
                ${copyToClipboardQuery.inject("text")}
            };""".trimIndent()

            // Load the HTML content
            browser.loadHTML(htmlContent)

            // Execute the JavaScript after the page is loaded
            browser.jbCefClient.addLoadHandler(object : org.cef.handler.CefLoadHandlerAdapter() {
                override fun onLoadEnd(browser: org.cef.browser.CefBrowser?, frame: org.cef.browser.CefFrame?, httpStatusCode: Int) {
                    browser?.executeJavaScript(jsCode, browser.url, 0)
                }
            }, browser.cefBrowser)
        }

        fun getContent() = browser.component
    }
}
