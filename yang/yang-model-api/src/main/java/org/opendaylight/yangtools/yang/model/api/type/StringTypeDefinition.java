/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.List;

/**
 * Contains method for getting data from the <code>string</code> YANG built-in type.
 */
public interface StringTypeDefinition extends LengthRestrictedTypeDefinition<StringTypeDefinition> {
    /**
     * Returns patterns specified in the string.
     *
     * @return list of pattern constraints which are specified in the
     *         <code>pattern</code> substatement of the <code>type</code>
     *         statement
     */
    List<PatternConstraint> getPatternConstraints();
}
