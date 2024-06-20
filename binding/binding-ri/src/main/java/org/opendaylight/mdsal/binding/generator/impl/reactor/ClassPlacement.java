/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * Enumeration of possible placements for a particular type. This provides a tie-in between the {@link Generator} tree
 * and the layout of resulting Java classes as dictated by {@link JavaTypeName} and {@link CollisionDomain}.
 */
enum ClassPlacement {
    /**
     * Generated class is a
     * <a href="https://docs.oracle.com/javase/specs/jls/se11/html/jls-7.html#jls-7.6">top level type declaration</a>.
     */
    TOP_LEVEL,
    /**
     * Generated class is a
     * <a href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-8.html#jls-8.5">member type declaration</a>.
     */
    MEMBER,
    /**
     * There is no class being generated, hence placement is irrelevant.
     */
    NONE,
    /**
     * There is no class being generated, just as with {@link #NONE}, but there is at least one {@link #TOP_LEVEL}
     * generated class which derives its name from this {@link Generator} and is participating in the same
     * {@link CollisionDomain}.
     */
    PHANTOM;
}
