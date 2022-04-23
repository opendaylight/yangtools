/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;

/**
 * Interface describing YANG 'leaf-list' statement.
 */
public non-sealed interface LeafListSchemaNode extends TypedDataSchemaNode, MustConstraintAware,
        ElementCountConstraintAware, UserOrderedAware<LeafListEffectiveStatement> {
    @Override
    LeafListEffectiveStatement asEffectiveStatement();

    /**
     * Return the default value of this leaf-list, as per the rules outlined in
     * <a href="https://tools.ietf.org/html/rfc7950#section-7.7.4">Section 7.4.4 of RFC7950</a>. RFC6020 does not
     * allow for default value of leaf-list, hence the returned list will be empty.
     *
     * @return Ordered list of Strings which specify the default values of this leaf-list
     */
    @NonNull Collection<? extends @NonNull Object> getDefaults();
}
