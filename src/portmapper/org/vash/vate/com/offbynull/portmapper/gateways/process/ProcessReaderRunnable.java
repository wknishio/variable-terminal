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
package org.vash.vate.com.offbynull.portmapper.gateways.process;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vash.vate.com.offbynull.portmapper.gateway.Bus;
import org.vash.vate.com.offbynull.portmapper.gateways.process.internalmessages.ReadType;
import org.vash.vate.compatibility.VTArrays;
import org.vash.vate.org.apache.commons.io.IOUtils;
import org.vash.vate.org.apache.commons.lang3.Validate;

final class ProcessReaderRunnable implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessReaderRunnable.class);

    private int id;
    private final InputStream inputStream;
    private final Bus processBus;
    private final ReadType readType;

    ProcessReaderRunnable(int id, InputStream inputStream, Bus processBus, ReadType readType) {
        Validate.notNull(inputStream);
        Validate.notNull(processBus);
        Validate.notNull(readType);
        
        this.id = id;
        this.inputStream = inputStream;
        this.processBus = processBus;
        this.readType = readType;
    }
    
    
    public void run() {
        LOG.debug("{} Starting up reader {}", id, readType);
        
        byte[] buffer = new byte[8192];
        try {
            while (true) {
                int count = inputStream.read(buffer);
                if (count == -1) {
                    LOG.debug("{} {} ended", id, readType);
                    break;
                }
                
                LOG.debug("{} Read {} bytes from {}", id, count, readType);
                
                processBus.send(new ReadMessage(id, VTArrays.copyOf(buffer, count), readType));
            }
        } catch (RuntimeException ioe) {
            LOG.debug(id + " " + readType + " encountered exception", ioe);
        } catch (IOException ioe) {
            LOG.debug(id + " " + readType + " encountered exception", ioe);
        } catch (Throwable t) {
            LOG.debug(id + " " + readType + " encountered exception", t);
        }
        finally {
            IOUtils.closeQuietly(inputStream);
            LOG.debug("{} Shutting down reader {}", id, readType);
        }
    }
    
}
