/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

/**
 * Immutable Object - object does not change its externally-observable state during its lifetime.
 *
 * <p>
 * Marker interface for objects which are immutable. This interface should be used directly on objects, preferably
 * final, which are eligible for the JSR-305 {@code javax.annotation.concurrent.Immutable} annotation and objects
 * implementing this interface are required to abide to interface contract specified by {@code @Immutable}.
 *
 * <p>
 * The reason for the existence of this interface is twofold: unlike {@code @Immutable}, it is visible at runtime and
 * objects can be quickly checked for compliance using an 'instanceof' check. This is useful for code which needs to
 * capture a point-in-time snapshot of otherwise unknown objects -- a typical example being logging/tracing systems.
 * Such systems would normally have to rely on serializing the object to get a stable checkpoint. Objects marked with
 * this interface are guaranteed to remain stable, thus already being a checkpoint for all intents and purposes, so
 * aside from retaining a reference no further action on them is necessary.
 *
 * <p>
 * Implementations of this interface must not change any public state during their entire lifetime.
 *
 * <p>
 * This interface is mutually exclusive with {@link Mutable} and other {@link MutationBehaviour}s.
 *
 * @author Robert Varga
 * @author Tony Tkacik
 */
public interface Immutable extends MutationBehaviour<Immutable> {

}
