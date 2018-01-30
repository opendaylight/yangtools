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
 * @deprecated This interface relates to declared model rather than to effective mode and as such should not
 *             exist. It is provided to provide common method definition and eash migration of users.
 */
@Deprecated
public interface AddedByUsesAware {
    /**
     * Returns <code>true</code> if this node was added by uses statement,
     * otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if this node was added by uses statement,
     *         otherwise returns <code>false</code>
     */
    boolean isAddedByUses();
}
