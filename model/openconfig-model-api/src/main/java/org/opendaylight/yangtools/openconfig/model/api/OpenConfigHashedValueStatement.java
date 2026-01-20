/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement corresponding to config's "openconfig-hashed-value" (new name) or "openconfig-encrypted-value"
 * (old name).
 */
@NonNullByDefault
public interface OpenConfigHashedValueStatement extends UnknownStatement<Empty> {
    /**
     * The definition of {@code oc-ext:openconfig-encrypted-value} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition ENCRYPTED_DEFINITION = StatementDefinition.of(
        OpenConfigHashedValueStatement.class, OpenConfigHashedValueEffectiveStatement.class,
        OpenConfigConstants.ENCRYPTED_VALUE_MODULE, "openconfig-encrypted-value");
    /**
     * The definition of {@code oc-ext:openconfig-hashed-value} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition HASHED_DEFINITION = StatementDefinition.of(
        OpenConfigHashedValueStatement.class, OpenConfigHashedValueEffectiveStatement.class,
        OpenConfigConstants.HASHED_VALUE_MODULE, "openconfig-hashed-value");
}
