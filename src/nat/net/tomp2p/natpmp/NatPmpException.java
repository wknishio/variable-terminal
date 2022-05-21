/*******************************************************************************
 * Copyright 2012 Thomas Bocek
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *  
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
/*
 * This file is part of jNAT-PMPlib.
 *
 * jNAT-PMPlib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jNAT-PMPlib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jNAT-PMPlib.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tomp2p.natpmp;

/**
 * This class is used for exceptions specific to jNAT-PMPlib. Refer to the
 * message and stack trace for information specific to the exception.
 * @author flszen
 */
public class NatPmpException extends Exception {

	private static final long serialVersionUID = 6619242406146562923L;

	/**
     * Constructs a new NatPmpException with a specific message.
     * @param message The message of the exception.
     */
    NatPmpException(String message) {
        super(message);
    }

    /**
     * Constructs a new NatPmpException with a specific message and cause.
     * @param message The message of the exception.
     * @param cause The cause of the exception.
     */
    NatPmpException(String message, Throwable cause) {
        super(message, cause);
    }
}
