/*
 * Copyright 2013-2016, Kasra Faghihi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vate.com.offbynull.portmapper.gateways.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vate.com.offbynull.portmapper.gateway.Bus;
import vate.org.apache.commons.lang3.Validate;

final class ProcessMonitorRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessMonitorRunnable.class);

    private final int id;
    private final Process process;
    private final Bus processBus;
    private final Thread stdoutThread;
    private final Thread stderrThread;

    ProcessMonitorRunnable(int id, Process process, Bus processBus, Thread stdoutThread, Thread stderrThread) {
        Validate.notNull(process);
        Validate.notNull(processBus);
        Validate.notNull(stdoutThread);
        Validate.notNull(stderrThread);
        
        this.id = id;
        this.process = process;
        this.processBus = processBus;
        this.stdoutThread = stdoutThread;
        this.stderrThread = stderrThread;
    }
    
    
    public void run() {
        LOG.debug("{} Starting up monitor", id);
        
        try {
            int exitCode = process.waitFor();
            
            LOG.debug("{} Process closed with exit code {}", id, exitCode);
            
            // before sending terminated message, make sure that the stdout/stderr processing threads are done...
            // we do this because we don't want to deal with a race condition where the term message is before
            // stdout/stderr content messages
            
            stdoutThread.join();
            stderrThread.join();
        }
         catch (InterruptedException ie) {
            Thread.interrupted();
            LOG.debug(id + " Interrupted", ie);
        }
        catch (Throwable e) {
            LOG.debug(id + " Encountered exception", e);
        }
         finally {
            // make sure it's destroyed
            try {
                process.destroy();
            } catch (RuntimeException re) {
                // do nothing
            }
            
            // make sure a message goes out saying that it's destroyed
            try {
                processBus.send(new TerminatedMessage(id, null));
            } catch (RuntimeException re) {
                // do nothing
            }

            LOG.debug("{} Shutting down monitor", id);
        }
    }
    
}
