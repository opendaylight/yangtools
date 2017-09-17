/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

/**
 * Class providing necessary support for processing YANG 1.1 Type statement.
 */
@Beta
public final class TypeStatementRfc7950Support extends TypeStatementImpl.Definition {
    private static final Map<String, StatementSupport<?, ?, ?>> ARGUMENT_SPECIFIC_SUPPORTS =
            ImmutableMap.<String, StatementSupport<?, ?, ?>>of(
                TypeUtils.LEAF_REF, new LeafrefSpecificationRfc7950Support(),
                TypeUtils.IDENTITY_REF, new IdentityrefSpecificationRfc7950Support());

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
