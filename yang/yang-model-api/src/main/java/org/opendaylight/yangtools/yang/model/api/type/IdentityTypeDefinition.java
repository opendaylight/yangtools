/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *
 * Contains the method for getting the details about YANG <code>identity</code>.
 */
public interface IdentityTypeDefinition extends TypeDefinition<IdentityTypeDefinition> {

    /**
     * Returns the name of the YANG identity
     *
     * @return QName of the YANG identity
     */
    QName getIdentityName();
}
