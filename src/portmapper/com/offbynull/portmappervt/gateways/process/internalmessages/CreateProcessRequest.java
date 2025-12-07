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
package com.offbynull.portmappervt.gateways.process.internalmessages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3vt.Validate;

import com.offbynull.portmappervt.gateway.Bus;

/**
 * Create a process. Possible responses are {@link CreateProcessResponse} and {@link IdentifiableErrorProcessResponse}).
 * @author Kasra Faghihi
 */
public final class CreateProcessRequest extends IdentifiableProcessRequest {

    private Bus responseBus;
    private String executable;
    private List<String> parameters;

    /**
     * Constructs a {@link CreateProcessRequest} object.
     * @param id of process
     * @param responseBus bus to send responses/notifications to for the created process 
     * @param executable executable to run
     * @param parameters parameters to use when running {@code executable}
     * @throws NullPointerException if any argument is {@code null}, or contains {@code null}
     */
    public CreateProcessRequest(int id, Bus responseBus, String executable, String ... parameters) {
        super(id);
        Validate.notNull(responseBus);
        Validate.notNull(executable);
        Validate.notNull(parameters);
        Validate.noNullElements(parameters);

        this.responseBus = responseBus;
        this.executable = executable;
        this.parameters = (List<String>) Collections.unmodifiableList(new ArrayList<String>(Arrays.asList(parameters)));
    }

    /**
     * Bus to send responses/notifications to for the created process.
     * @return response bus
     */
    public Bus getResponseBus() {
        return responseBus;
    }

    /**
     * Get executable to run.
     * @return executable
     */
    public String getExecutable() {
        return executable;
    }

    /**
     * Get parameters to use when running executables.
     * @return parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    
    public String toString() {
        return "CreateProcessRequest{" + "responseBus=" + responseBus + ", executable=" + executable
                + ", parameters=" + parameters + '}';
    }

}
