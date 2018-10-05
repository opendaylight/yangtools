/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective model statement which should be used to derive application behaviour related to typedefs.
 */
public interface TypedefEffectiveStatement extends EffectiveStatement<QName, TypedefStatement>, TypeDefinitionAware {

    /**
     * Return this type definition as an effective type statement.
     *
     * @return Effective type statement.
     */
    @Beta
    @NonNull TypeEffectiveStatement<TypeStatement> asTypeEffectiveStatement();
}
