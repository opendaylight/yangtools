/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import javax.annotation.Nonnull;

/**
 * A generalized contract of an object which has an identifier. The identifier, unlike the object carrying it, must
 * generally comply to the interface contract outlined in {@link Identifier}, but this is not enforced in this API's
 * design, as there are external classes, such as {@link String}, which comply to the contract without implementing it.
 *
 * @param <T> Identifier class, must conform to API contract outlined by {@link Identifier}, even if it does not
 *            implement it.
 */
// FIXME: 3.0.0: consider requiring "T extends Serializable"
public interface Identifiable<T> {
    /**
     * Return this objects Identifier.
     *
     * @return Object's identifier, must not be null.
     */
    @Nonnull T getIdentifier();
}
