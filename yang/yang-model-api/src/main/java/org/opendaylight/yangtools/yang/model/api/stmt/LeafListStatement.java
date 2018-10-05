/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

public interface LeafListStatement extends MultipleElementsDeclaredStatement, TypeAwareDeclaredStatement,
        ConfigStatementAwareDeclaredStatement<QName>, MustStatementAwareDeclaredStatement<QName> {
    /**
     * Return default statements defined in this leaf-list. For RFC6020 semantics, this method returns an empty
     * collection.
     *
     * @return collection of default statements
     */
    default @NonNull Collection<? extends DefaultStatement> getDefaults() {
        return declaredSubstatements(DefaultStatement.class);
    }
}
