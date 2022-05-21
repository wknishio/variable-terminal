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
package com.offbynull.portmapper.gateways.process.internalmessages;

/**
 * Process exited.
 * @author Kasra Faghihi
 */
public final class ExitProcessNotification extends IdentifiableProcessResponse {
    private final Integer exitCode;

    /**
     * Constructs a {@link ExitProcessNotification} object.
     * @param id id of process
     * @param exitCode exit code ({@code null} if not available)
     */
    public ExitProcessNotification(int id, Integer exitCode) {
        super(id);
        this.exitCode = exitCode;
    }

    /**
     * Get exit code of process.
     * @return exit code of process ({@code null} if not available)
     */
    public Integer getExitCode() { // null if not available
        return exitCode;
    }

    
    public String toString() {
        return "ExitProcessNotification{super=" + super.toString() + "exitCode=" + exitCode + '}';
    }
    
}
