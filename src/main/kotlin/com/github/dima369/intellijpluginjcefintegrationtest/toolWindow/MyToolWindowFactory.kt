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
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLoadHandler
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
                    <button id="errorButton" style="margin-left: 10px;">Test JS Error</button>
                    <button id="uncaughtButton" style="margin-left: 10px;">Test Uncaught Error</button>
                    <button id="promiseButton" style="margin-left: 10px;">Test Promise Rejection</button>
                    <div id="notification" class="notification">Copied to clipboard: <span id="copied-text"></span></div>

                    <script>
                        let reportJsError;
                        let copyToClipboard;
                        const notification = document.getElementById('notification');

                        // Global error handler
                        window.onerror = function(message, source, lineno, colno, error) {
                            const errorDetails = {
                                message: message,
                                source: source,
                                lineno: lineno,
                                colno: colno,
                                stack: error ? error.stack : 'No stack trace available'
                            };
                            reportJsError(JSON.stringify(errorDetails));
                            return true; // Prevents the default browser error handling
                        };

                        // Catch unhandled promise rejections
                        window.addEventListener('unhandledrejection', function(event) {
                            const errorDetails = {
                                message: 'Unhandled Promise Rejection',
                                reason: event.reason ? event.reason.toString() : 'Unknown reason',
                                stack: event.reason && event.reason.stack ? event.reason.stack : 'No stack trace available'
                            };
                            reportJsError(JSON.stringify(errorDetails));
                        });

                        document.getElementById('helloButton').addEventListener('click', function() {
                            try {
                                const textToCopy = 'hello';
                                copyToClipboard(textToCopy);

                                // Update and show notification
                                document.getElementById('copied-text').textContent = textToCopy;
                                notification.classList.add('show');

                                // Hide notification after 2 seconds
                                setTimeout(function() {
                                    notification.classList.remove('show');
                                }, 2000);
                            } catch (error) {
                                reportJsError(JSON.stringify({
                                    message: error.message,
                                    stack: error.stack || 'No stack trace available'
                                }));
                            }
                        });

                        // Add event listener for the error button to deliberately trigger an error
                        document.getElementById('errorButton').addEventListener('click', function() {
                            // Deliberately trigger different types of errors
                            try {
                                // Throw a simple error
                                throw new Error('This is a test error from the error button');

                                // The following code will never execute due to the error above,
                                // but these are examples of other errors that could be triggered:

                                // Reference error (undefined variable)
                                // console.log(undefinedVariable);

                                // Type error
                                // const num = 42;
                                // num.toLowerCase();

                                // Syntax error (can't be caught in try/catch)
                                // eval('if (true) {');
                            } catch (error) {
                                reportJsError(JSON.stringify({
                                    message: error.message,
                                    stack: error.stack || 'No stack trace available'
                                }));
                            }
                        });

                        // Add event listener for the uncaught error button
                        document.getElementById('uncaughtButton').addEventListener('click', function() {
                            // This will trigger an uncaught error that will be caught by the global error handler
                            setTimeout(function() {
                                // This will cause an uncaught reference error
                                undefinedVariable.someMethod();
                            }, 100);
                        });

                        // Add event listener for the promise rejection button
                        document.getElementById('promiseButton').addEventListener('click', function() {
                            // Create a promise that will be rejected
                            new Promise(function(resolve, reject) {
                                // Reject the promise with an error
                                reject(new Error('This is a test promise rejection'));
                            });
                            // Note: We're not catching the rejection with .catch(), so it will be unhandled
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

            // Create a JavaScript query to handle JavaScript errors
            val jsErrorQuery = JBCefJSQuery.create(browser)
            jsErrorQuery.addHandler { errorJson ->
                try {
                    // Log the error to the console
                    println("JavaScript Error: $errorJson")

                    // Parse the JSON to extract error details
                    val errorMap = try {
                        // Simple parsing to extract message and stack
                        val jsonStr = errorJson.trim()
                        if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                            val pairs = jsonStr.substring(1, jsonStr.length - 1)
                                .split(",")
                                .map { it.trim() }
                                .filter { it.contains(":") }
                                .associate { pair ->
                                    val keyValue = pair.split(":", limit = 2)
                                    val key = keyValue[0].trim().trim('"')
                                    val value = keyValue[1].trim().trim('"')
                                    key to value
                                }
                            pairs
                        } else {
                            mapOf("error" to errorJson)
                        }
                    } catch (e: Exception) {
                        mapOf("error" to errorJson, "parse_error" to e.message.toString())
                    }

                    // Format a user-friendly error message
                    val errorMessage = buildString {
                        append("JavaScript Error")
                        errorMap["message"]?.let { append(": $it") }
                        errorMap["lineno"]?.let { line ->
                            errorMap["colno"]?.let { col ->
                                append(" at line $line, column $col")
                            } ?: append(" at line $line")
                        }
                        errorMap["source"]?.let { append("\nSource: $it") }
                        errorMap["stack"]?.let {
                            if (it.length > 100) {
                                append("\nStack: ${it.substring(0, 100)}...")
                            } else {
                                append("\nStack: $it")
                            }
                        }
                    }

                    // Show a notification with the formatted error details
                    Notifications.showError(project, errorMessage)
                } catch (e: Exception) {
                    println("Error handling JavaScript error: ${e.message}")
                    Notifications.showError(project, "Error handling JavaScript error: ${e.message}")
                }
                null
            }

            // Inject the JavaScript queries into the HTML
            val jsCode = """window.copyToClipboard = function(text) {
                ${copyToClipboardQuery.inject("text")}
            };

            window.reportJsError = function(errorJson) {
                ${jsErrorQuery.inject("errorJson")}
            };""".trimIndent()

            // Load the HTML content
            println(htmlContent)
            browser.loadHTML(htmlContent)
//            browser.loadURL("http://localhost:1337/")

            // Execute the JavaScript after the page is loaded
            browser.jbCefClient.addLoadHandler(object : org.cef.handler.CefLoadHandlerAdapter() {

                override fun onLoadError(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    errorCode: CefLoadHandler.ErrorCode?,
                    errorText: String?,
                    failedUrl: String?
                ) {
                    Notifications.showError(project, "Error loading URL: $failedUrl")
                }

                override fun onLoadEnd(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    httpStatusCode: Int
                ) {
                    browser?.executeJavaScript(jsCode, browser.url, 0)
                }
            }, browser.cefBrowser)
        }

        fun getContent() = browser.component
    }
}
