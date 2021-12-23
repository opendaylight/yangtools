/*
 * Copyright (c) 2021 Vratko Polak and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.testing;

import org.opendaylight.yangtools.concepts.Identifiable;

/**
 * IndexedList requires an identifiable element, so here is one.
 * The identifier is String, as Identifiable does not really support int.
 * The payload is integer, identifier is its string form.
 */
public class StringedTestingElement implements Identifiable<String> {

    private int payload;

    /**
     * Construct the instance.
     */
    public StringedTestingElement(final int input) {
        payload = input;
    }

    /**
     * Return the numeric payload.
     */
    public int getPayload() {
        return payload;
    }

    /**
     * Return the sequential-friendly identifier.
     */
    public String getIdentifier() {
        return Integer.toString(payload);
    }

}
