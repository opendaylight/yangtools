/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * Immutable Object - object does not change its state during lifecycle.
 * 
 * Implementations of this interface must not change any public state during
 * their whole lifecycle.
 * 
 * This interface is mutually exclusive with {@link Mutable} and other
 * {@link MutationBehaviour}s.
 * 
 * @author Tony Tkacik <ttkacik@cisco.com>
 * 
 */
public interface Immutable extends MutationBehaviour<Immutable> {

}
