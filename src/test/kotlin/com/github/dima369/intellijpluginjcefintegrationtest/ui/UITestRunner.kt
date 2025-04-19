package com.github.dima369.intellijpluginjcefintegrationtest.ui

import org.junit.platform.launcher.Launcher
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.junit.platform.launcher.listeners.TestExecutionSummary
import org.junit.platform.engine.discovery.DiscoverySelectors
import java.io.PrintWriter

/**
 * Simple runner for UI tests.
 * This can be used to run the UI tests manually.
 */
object UITestRunner {
    @JvmStatic
    fun main(args: Array<String>) {
        val request: LauncherDiscoveryRequest = LauncherDiscoveryRequestBuilder.request()
            .selectors(DiscoverySelectors.selectClass(HelloButtonUITest::class.java))
            .build()

        val launcher: Launcher = LauncherFactory.create()
        val listener = SummaryGeneratingListener()

        launcher.registerTestExecutionListeners(listener)
        launcher.execute(request)

        val summary: TestExecutionSummary = listener.summary
        summary.printTo(PrintWriter(System.out))
    }
}
