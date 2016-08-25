/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Contains methods for getting data from the <code>instance-identifier</code>
 * YANG built-in type.
 */
@Value.Immutable
public interface InstanceIdentifierTypeDefinition extends TypeDefinition<InstanceIdentifierTypeDefinition> {
    /**
     * Returns true|false which represents argument of <code>require-instance</code> statement. This statement is the
     * substatement of the <code>type</code> statement.
     *
     * @return boolean value which is true if the <code>require-instance</code> statement is true and vice versa
     */
    boolean requireInstance();
}
