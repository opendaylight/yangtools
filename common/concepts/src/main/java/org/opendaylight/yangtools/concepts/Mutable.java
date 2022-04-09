/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * Mutable object - object may change it's state during lifecycle. This interface is mutually exclusive
 * with {@link Immutable} and other {@link MutationBehaviour}s.
 *
 * @author Tony Tkacik
 */
public non-sealed interface Mutable extends MutationBehaviour<Mutable> {
    // Marker interface only
}
