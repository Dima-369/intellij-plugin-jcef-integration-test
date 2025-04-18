package com.github.dima369.intellijpluginjcefintegrationtest.toolWindow

import com.github.dima369.intellijpluginjcefintegrationtest.MyBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefBrowserBase
import com.intellij.ui.jcef.JBCefBrowserBuilder
import com.intellij.ui.jcef.JBCefJSQuery
import java.io.IOException
import java.util.concurrent.TimeUnit

class MyToolWindowFactory : ToolWindowFactory, DumbAware {

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

        private val browser = JBCefBrowserBuilder().build() as JBCefBrowserBase

        init {
            // Create HTML content with a button
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        :root {
                            --background-color: #f2f2f2;
                            --text-color: #000000;
                            --button-background: #e1e1e1;
                            --button-text: #000000;
                            --button-border: #c1c1c1;
                            --button-hover-background: #d1d1d1;
                        }
                        @media (prefers-color-scheme: dark) {
                            :root {
                                --background-color: #2b2b2b;
                                --text-color: #bbbbbb;
                                --button-background: #3c3f41;
                                --button-text: #bbbbbb;
                                --button-border: #5e6060;
                                --button-hover-background: #494d4f;
                            }
                        }
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
                        .notification {
                            display: none;
                            margin-top: 10px;
                            padding: 10px;
                            background-color: #4CAF50;
                            color: white;
                            border-radius: 4px;
                        }
                        .notification.show {
                            display: block;
                        }
                    </style>
                </head>
                <body>
                    <button id="helloButton">${MyBundle.message("helloButton")}</button>
                    <div id="notification" class="notification">Copied to clipboard: <span id="copied-text"></span></div>

                    <script>
                        // Will be replaced with the actual query code
                        let copyToClipboard;
                        const notification = document.getElementById('notification');

                        document.getElementById('helloButton').addEventListener('click', function() {
                            alert('hi');
                            const textToCopy = 'hello';
                            copyToClipboard(textToCopy);

                            // Update and show notification
                            document.getElementById('copied-text').textContent = textToCopy;
                            notification.classList.add('show');

                            // Hide notification after 2 seconds
                            setTimeout(function() {
                                notification.classList.remove('show');
                            }, 2000);
                        });
                    </script>
                </body>
                </html>
            """.trimIndent()

            // Create a JavaScript query to handle the button click
            val copyToClipboardQuery = JBCefJSQuery.create(browser)
            copyToClipboardQuery.addHandler { text ->
                // Copy the text to clipboard using pbcopy
                try {
//                    val process = ProcessBuilder("pbcopy").start()
//                    process.outputStream.use { output ->
//                        output.write(text.toByteArray())
//                    }
//                    process.waitFor(5, TimeUnit.SECONDS)

                    // Show notification
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("JCEF Integration")
                        .createNotification(
                            MyBundle.message("copiedToClipboard", text),
                            NotificationType.INFORMATION
                        )
                        .notify(project)
                } catch (e: IOException) {
                    // Show error notification
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("JCEF Integration")
                        .createNotification(
                            "Failed to copy to clipboard: ${e.message}",
                            NotificationType.ERROR
                        )
                        .notify(project)
                    thisLogger().warn("Failed to copy to clipboard", e)
                } catch (e: InterruptedException) {
                    // Show error notification
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("JCEF Integration")
                        .createNotification(
                            "Clipboard operation timed out: ${e.message}",
                            NotificationType.ERROR
                        )
                        .notify(project)
                    thisLogger().warn("Clipboard operation timed out", e)
                }

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
                override fun onLoadEnd(
                    browser: org.cef.browser.CefBrowser?,
                    frame: org.cef.browser.CefFrame?,
                    httpStatusCode: Int
                ) {
                    browser?.executeJavaScript(jsCode, browser.url, 0)
                }
            }, browser.cefBrowser)
        }

        fun getContent() = browser.component
    }
}
