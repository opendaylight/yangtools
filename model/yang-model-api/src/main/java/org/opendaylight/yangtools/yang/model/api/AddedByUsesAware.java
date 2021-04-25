/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;

/**
 * Trait interface for {@link SchemaNode}s, which have the {@link #isAddedByUses()} method.
 */
@Beta
//FIXME: 8.0.0: refactor this interface to take into account DerivableSchemaNode
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
