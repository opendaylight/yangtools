/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;

/**
 * Namespace of {@code data node}s. This is a subtree of {@link SchemaTreeEffectiveStatementNamespace} in that all
 * data nodes are also schema nodes. The structure of the tree is different, though, as {@code choice} and {@code case}
 * statements are glossed over and they do not contribute to the tree hierarchy -- only their children do.

 * <p>
 * This corresponds to the {@code data tree} view of a YANG-defined data.
 *
 * @param <T> Child statement type
 * @author Robert Varga
 */
@Beta
public abstract class DataTreeEffectiveStatementNamespace<T extends SchemaTreeEffectiveStatement<?>>
        extends EffectiveStatementNamespace<T> {
    private DataTreeEffectiveStatementNamespace() {
        // Should never be instantiated
    }
}
