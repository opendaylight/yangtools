/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * Mutation behavior. This interface is used to prevent same class extends multiple types of MutationBehaviour
 * such as {@link Immutable} and {@link Mutable} which are mutually exclusive.
 *
 * @author Tony Tkacik
 *
 * @param <T> Mutation Type
 */
// FIXME: sealed to allow Mutable and Immutable only when we have JDK17+
// FIXME: consider hiding this interface afterwards
public interface MutationBehaviour<T extends MutationBehaviour<T>> {
    // Marker interface only
}
