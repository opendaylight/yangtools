/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataStatement;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument.WithSubstatements;

/**
 * Declared statement representation of 'yang-data' extension defined in
 * <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>.
 */
final class YangDataStatementImpl extends WithSubstatements<YangDataName> implements YangDataStatement {
    YangDataStatementImpl(final YangDataName argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(argument.name(), argument, substatements);
    }
}