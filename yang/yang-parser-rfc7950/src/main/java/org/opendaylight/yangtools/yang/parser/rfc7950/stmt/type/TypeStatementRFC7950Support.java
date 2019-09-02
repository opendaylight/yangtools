/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

/**
 * Class providing necessary support for processing YANG 1.1 Type statement.
 */
@Beta
public final class TypeStatementRFC7950Support extends AbstractTypeStatementSupport {
    private static final ImmutableMap<String, StatementSupport<?, ?, ?>> ARGUMENT_SPECIFIC_SUPPORTS = ImmutableMap.of(
        LEAF_REF, new LeafrefSpecificationRFC7950Support(),
        IDENTITY_REF, new IdentityrefSpecificationRFC7950Support());
    private static final TypeStatementRFC7950Support INSTANCE = new TypeStatementRFC7950Support();

    private TypeStatementRFC7950Support() {
        // Hidden
    }

    public static TypeStatementRFC7950Support getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean hasArgumentSpecificSupports() {
        return !ARGUMENT_SPECIFIC_SUPPORTS.isEmpty() || super.hasArgumentSpecificSupports();
    }

    @Override
    public StatementSupport<?, ?, ?> getSupportSpecificForArgument(final String argument) {
        final StatementSupport<?, ?, ?> potential = ARGUMENT_SPECIFIC_SUPPORTS.get(argument);
        return potential != null ? potential : super.getSupportSpecificForArgument(argument);
    }
}
