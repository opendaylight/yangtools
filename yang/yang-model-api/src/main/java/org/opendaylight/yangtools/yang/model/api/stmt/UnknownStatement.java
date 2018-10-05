/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * A statement not covered by the base metamodel, e.g. it is not expressed directly in terms of concrete statement
 * interfaces in this package. This interface is meant to be specialized by external semantic plugins, such that they
 * are properly anchored in the metamodel.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 */
public interface UnknownStatement<A> extends DeclaredStatement<A> {
    default @Nullable A getArgument() {
        return argument();
    }
}
