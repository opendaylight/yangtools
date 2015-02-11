/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

public class SomeModifiersUnresolvedException extends ReactorException {

    private static final long serialVersionUID = 1L;

    public SomeModifiersUnresolvedException(ModelProcessingPhase phase) {
        super(phase,"Some of " + phase + " modifiers for statements were not resolved.");
    }

}
