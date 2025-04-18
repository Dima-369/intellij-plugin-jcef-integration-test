package com.github.dima369.intellijpluginjcefintegrationtest.toolWindow

import com.github.dima369.intellijpluginjcefintegrationtest.MyBundle
import com.github.dima369.intellijpluginjcefintegrationtest.Notifications
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
            // Load CSS content directly
            val cssContent = javaClass.classLoader.getResourceAsStream("css/styles.css")?.bufferedReader()?.readText() ?: ""

            // Create HTML content with a button
            val htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                    ${cssContent.replace("\n", "\n                    ")}
                    </style>
                </head>
                <body>
                    <button id="helloButton">${MyBundle.message("helloButton")}</button>
                    <div id="notification" class="notification">Copied to clipboard: <span id="copied-text"></span></div>

                    <script>
                        const notification = document.getElementById('notification');

                        document.getElementById('helloButton').addEventListener('click', function() {
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
                println("DUDEEEE")
                // Copy the text to clipboard using pbcopy
                try {
//                    val process = ProcessBuilder("pbcopy").start()
//                    process.outputStream.use { output ->
//                        output.write(text.toByteArray())
//                    }
//                    process.waitFor(5, TimeUnit.SECONDS)

                    Notifications.showInfo(project, MyBundle.message("copiedToClipboard", text))
                } catch (e: IOException) {
                    Notifications.showError(project, "Failed to copy to clipboard: ${e.message}")
                } catch (e: InterruptedException) {
                    Notifications.showError(project, "Clipboard operation timed out: ${e.message}")
                }
                null
            }

            // Inject the JavaScript query into the HTML
            val jsCode = """window.copyToClipboard = function(text) {
                ${copyToClipboardQuery.inject("text")}
            };""".trimIndent()

            // Load the HTML content
            println(htmlContent)
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
