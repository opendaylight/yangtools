/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 * Trait interface for {@link SchemaNode}s, which have the {@link #isAddedByUses()} method.
 *
 * @deprecated The only user of this trait is MD-SAL's binding component. As such, we provide enough information in
 *             EffectiveStatement tree to reconstruct this information.
 */
@Deprecated(since = "7.0.9", forRemoval = true)
public interface AddedByUsesAware {
    /**
     * Returns {@code true} if this node was added by uses statement, otherwise returns {@code false}.
     *
     * @return {@code true} if this node was added by uses statement, otherwise returns {@code false}
     */
    boolean isAddedByUses();
}
