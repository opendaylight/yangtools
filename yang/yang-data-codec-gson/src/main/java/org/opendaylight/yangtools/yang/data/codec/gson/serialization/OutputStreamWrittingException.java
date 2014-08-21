/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.serialization;

import java.io.IOException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;

public class OutputStreamWrittingException extends IllegalStateException {

    /**
     *
     */
    private static final long serialVersionUID = -8888728245871302176L;


    public OutputStreamWrittingException(final NodeIdentifier nodeIdentifier,final IOException e) {
        super("It wasn't possible to write to output stream identifier "+nodeIdentifier, e);
    }

    public OutputStreamWrittingException(final NodeIdentifierWithPredicates nodeIdentifier,final IOException e) {
        super("It wasn't possible to write to output stream identifier "+nodeIdentifier, e);
    }

    public OutputStreamWrittingException(final String message,final IOException e) {
        super(message, e);
    }

    public OutputStreamWrittingException(final Object obj,final IOException e) {
        super("It wasn't possible to write to output stream object"+obj, e);
    }

}
