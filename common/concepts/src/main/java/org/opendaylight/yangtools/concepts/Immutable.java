/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Marker interface for all classes which their {@code effective immutability} with regards to their
 * externally-observable state. This is a weaker version of
 * <a href="https://errorprone.info/bugpattern/Immutable">Error Prone's @Immutable</a>, where the contract cannot be
 * statically expressed.
 *
 * <p>As a direct consequence, this object is safe to use from multiple threads concurrently, without any unreasonable
 * overhead (such as {@code synchronized}).
 *
 * <p>The design as an explicit interface has further benefits in that objects can be efficiently queried for this
 * contract via a simple {@code instanceof} rather than having to do reflection. This is important for infrastructure
 * tasks such as formatting objects for logging purposes: {@link Immutable} objects can be retained and their
 * {@link Object#toString()} invoked at some later point in time, possibly in a completely different thread. This is,
 * by definition, guaranteed to be safe, as {@link #toString()} is part of an object's observable state.
 *
 * {@apiNote}
 *     This interface uses an internal modeling trick to make it impossible to implement {@link Immutable}
 *     and {@link Mutable} at the same time. Violations are expected to be flagged by {@code javac}, but we have not
 *     ascertained JVM would reject such classes.
 *
 * {@implSpec}
 *     Implementing class must abide the {@code @Immutable} contract just as if they were annotated as such, but may
 *     be implemented with deferred materialization. When implementing such a strategy, implementations should prefer
 *     concurrent computation and with first-wins memoization with {@code getAcuire()} and {@code setRelease()} memory
 *     effects.
 */
// FIXME: add a bytecode generator test which would attempt to load an interface that 'extends Mutable, Immutable' and
//        clarify above apiNote to provide exact (JVMS-rooted) semantics. If it becomes an absolute guarantee, promote
//        the apiNode to full API contract.
@NonNullByDefault
public non-sealed interface Immutable extends MutationBehaviour<Immutable> {
    /**
     * {@inheritDoc}
     *
     * <p>Any two invocations of this method are guaranteed to return a non-null {@link String#equals(Object)} value.
     */
    @Override
    String toString();
}
