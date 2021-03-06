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
 * Write buffer for process is empty.
 * @author Kasra Faghihi
 */
public final class WriteEmptyProcessNotification extends IdentifiableProcessNotification {

    /**
     * Constructs a {@link WriteEmptyProcessNotification} object.
     * @param id id of process
     */
    public WriteEmptyProcessNotification(int id) {
        super(id);
    }

    
    public String toString() {
        return "WriteEmptyProcessNotification{super=" + super.toString() + '}';
    }

}
