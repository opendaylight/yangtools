/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Interface for {@link TypeDefinition}s which can be restricted through a require-instance statement.
 *
 * @param <T> Concrete {@link TypeDefinition} subinterface
 */
public interface RequireInstanceRestrictedTypeDefinition<T extends TypeDefinition<T>> extends TypeDefinition<T> {
    /**
     * Returns true or false which represents argument of <code>require-instance</code> statement. This statement is
     * the substatement of the <code>type</code> statement.
     *
     * @return boolean value which is true if the <code>require-instance</code> statement is true and vice versa
     */
    boolean requireInstance();
}
