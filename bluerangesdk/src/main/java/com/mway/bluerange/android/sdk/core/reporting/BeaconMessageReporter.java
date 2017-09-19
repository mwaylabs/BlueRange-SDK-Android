//
//  BeaconMessageReporter.java
//  BlueRangeSDK
//
// Copyright (c) 2016-2017, M-Way Solutions GmbH
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//     * Redistributions of source code must retain the above copyright
//       notice, this list of conditions and the following disclaimer.
//     * Redistributions in binary form must reproduce the above copyright
//       notice, this list of conditions and the following disclaimer in the
//       documentation and/or other materials provided with the distribution.
//     * Neither the name of the M-Way Solutions GmbH nor the
//       names of its contributors may be used to endorse or promote products
//       derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY M-Way Solutions GmbH ''AS IS'' AND ANY
// EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL M-Way Solutions GmbH BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

package com.mway.bluerange.android.sdk.core.reporting;

import com.mway.bluerange.android.sdk.core.logging.BeaconMessageLogger;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessagePassingStreamNode;
import com.mway.bluerange.android.sdk.core.streaming.BeaconMessageStreamNode;
import com.mway.bluerange.android.sdk.utils.logging.ITracer;
import com.mway.bluerange.android.sdk.utils.logging.Tracer;
import com.mway.bluerange.android.sdk.core.scanning.messages.BeaconMessage;

/**
 * A beacon message reporter periodically reads the log of a {@link BeaconMessageLogger} instance
 * and sends reports to a specified sender. The reports will be sent to the receiver
 * every {@link #timeBetweenReportsInMs} milliseconds.
 */
public class BeaconMessageReporter extends BeaconMessagePassingStreamNode {

    // Tracer
    private ITracer tracer = Tracer.getInstance();
    private final static String kLogTag = "BeaconMessageReporter";

    // Thread
    private Thread thread;

    // Logger
    private BeaconMessageLogger logger;

    // Internal state
    private Boolean reportingEnabled = false;

    // Configuration
    private static final long DEFAULT_WAIT_TIME_BETWEEN_REPORTS_IN_MS = 20000L;
    private long timeBetweenReportsInMs = DEFAULT_WAIT_TIME_BETWEEN_REPORTS_IN_MS;
    private static final long DEFAULT_POLLING_TIME_WAIT_FOR_RECEIVER_AVAILABLE_IN_MS = 60000L;
    private long pollingTimeWaitForReceiverAvailableInMs
            = DEFAULT_POLLING_TIME_WAIT_FOR_RECEIVER_AVAILABLE_IN_MS;
    // Report builder
    private BeaconMessageReportBuilder reportBuilder;
    // Report sender
    private BeaconMessageReportSender sender;

    /**
     * Creates a new instance using the preconfigured {@link BeaconMessageLogger},
     * {@link BeaconMessageReportBuilder} and {@link BeaconMessageReportSender}.
     * @param logger The preconfigured logger that is used for scanning and persistently
     *               logging {@link BeaconMessage}s.
     * @param reportBuilder A builder that transform a stream of {@link BeaconMessage}s to a
     *                      {@link BeaconMessageReport} object.
     * @param sender The {@link BeaconMessageReportSender} object which the reports will be sent to.
     */
    public BeaconMessageReporter(BeaconMessageLogger logger,
                                 BeaconMessageReportBuilder reportBuilder,
                                 BeaconMessageReportSender sender) {
        super(logger);
        this.logger = logger;
        this.reportBuilder = reportBuilder;
        this.sender = sender;
    }

    /**
     * Starts scanning, persistently logging and periodically
     * sending reports.
     */
    public void startReporting() {
        // Another should wait for report request.
        this.startReportingInBackground();
    }

    private void startReportingInBackground() {
        // We do not need to synchronize the threads
        // since the reporting thread does not change
        // this variable but only reads it.
        this.reportingEnabled = true;
        startReportingThread();
    }

    private void startReportingThread() {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    continuouslySendReports();
                } catch (InterruptedException e) {
                    // An interrupt should stop the thread.
                }
            }
        });
        this.thread.setName("BeaconMessageReporter");
        this.thread.start();
    }

    private void continuouslySendReports() throws InterruptedException {
        while(reportingEnabled) {
            // 1. Wait until next report request.
            tracer.logDebug(kLogTag, "Waiting for next report request.");
            waitUntilNextReportRequest();
            // 2. Wait until the report receiver is available.
            tracer.logDebug(kLogTag, "Waiting until report receiver is available");
            waitUntilReportReceiverIsAvailable();
            try {
                // 3. Try constructing report
                tracer.logDebug(kLogTag, "Trying to construct report.");
                BeaconMessageReport report = tryConstructingReport();
                // 4. Clear the log
                tracer.logDebug(kLogTag, "Clearing log.");
                clearLog();
                // 5. Send the report
                if (report != null) {
                    tracer.logDebug(kLogTag, "Sending report.");
                    sendReport(report);
                }
            } catch (Throwable t) {
                // If something happened while constructing or sending
                // the report, log this and just continue with the next report.
                tracer.logError(kLogTag, "An error in BeaconMessageReporter occurred: " + t.getMessage());
            }
        }
    }

    private void waitUntilNextReportRequest() throws InterruptedException {
        Thread.sleep(timeBetweenReportsInMs);
    }

    private void waitUntilReportReceiverIsAvailable() throws InterruptedException {
        while(!sender.receiverAvailable()) {
            Thread.sleep(pollingTimeWaitForReceiverAvailableInMs);
        }
    }

    private BeaconMessageReport tryConstructingReport() {
        BeaconMessageReport report = null;
        try {
             report = buildActivityReport();
        } catch (BeaconMessageReportBuilder.BuildException e) {
            // If an error occurred when building the
            // report we assume that this error could
            // not be fixed by repeating. Therefore,
            // in this case we throw the complete log away
            // in order to give the next scan report a chance.
            // Therefore: Empty catch implementation
            tracer.logWarning(kLogTag, "Failed constructing the status report!");
        } catch (BeaconMessageReportBuilder.NoMessagesException e) {
            // If we did not receive any messages since the
            // last report, we do not have to send anything
            // to the receiver.
            tracer.logDebug(kLogTag, "No status report was sent, because no messages were " +
                    "received " +
                    "since the last status report.");
        }
        return report;
    }

    private BeaconMessageReport buildActivityReport() throws BeaconMessageReportBuilder.BuildException,
            BeaconMessageReportBuilder.NoMessagesException {
        if (!logger.getLogIterator().hasNext()) {
            throw new BeaconMessageReportBuilder.NoMessagesException();
        }

        reportBuilder.newReport();
        for (BeaconMessage message : logger) {
            reportBuilder.addBeaconMessage(message);
        }
        BeaconMessageReport report = reportBuilder.buildReport();
        return report;
    }

    private void sendReport(BeaconMessageReport report) {
        boolean retrySendingReport = true;
        while (retrySendingReport) {
            try {
                // 3.2 Send the report
                sender.sendReport(report);
                // 3.3 Set the reporting sending as finished
                retrySendingReport = false;
            } catch(BeaconMessageReportSender.SendReportException e) {
                // If report cannot be sent, just log this information
                // and do not retry sending the report.
                tracer.logWarning(kLogTag, "Failed sending status report! Retry sending report");
                retrySendingReport = false;
            } catch (BeaconMessageReportSender.UnresolvableSendReportException e) {
                // If report cannot be sent, just log this information
                // and do not retry sending the report.
                tracer.logWarning(kLogTag, "Failed sending status report! Discard status report");
                retrySendingReport = false;
            }
        }
    }

    private void clearLog() {
        // Since the logger class is thread safe we do not need
        // to add a synchronized block.
        logger.clearLog();
    }

    public long getTimeBetweenReportsInMs() {
        return timeBetweenReportsInMs;
    }

    public void setTimeBetweenReportsInMs(long timeBetweenReportsInMs) {
        this.timeBetweenReportsInMs = timeBetweenReportsInMs;
    }

    /**
     * Stops scanning, logging and reporting.
     */
    public void stopReporting() {
        // Reporting should be turned off.
        this.stopReportingInBackground();
    }

    private void stopReportingInBackground() {
        // We do not need to synchronize the threads
        // since the reporting thread does not change
        // this variable but only reads it.
        this.reportingEnabled = false;
        this.thread.interrupt();
    }

    @Override
    protected void preprocessMessage(BeaconMessage message) {

    }

    @Override
    protected void postprocessMessage(BeaconMessage message) {

    }

    @Override
    public void onMeshInactive(BeaconMessageStreamNode senderNode) {
        super.onMeshInactive(senderNode);
    }

    public long getPollingTimeWaitForReceiverAvailableInMs() {
        return pollingTimeWaitForReceiverAvailableInMs;
    }

    public void setPollingTimeWaitForReceiverAvailableInMs(long pollingTimeWaitForReceiverAvailableInMs) {
        this.pollingTimeWaitForReceiverAvailableInMs = pollingTimeWaitForReceiverAvailableInMs;
    }

}
