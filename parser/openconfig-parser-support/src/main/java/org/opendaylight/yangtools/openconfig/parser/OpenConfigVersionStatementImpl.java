/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.parser;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.openconfig.model.api.OpenConfigVersionStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString.WithSubstatements;

final class OpenConfigVersionStatementImpl extends WithSubstatements<SemVer>
        implements OpenConfigVersionStatement {
    OpenConfigVersionStatementImpl(final SemVer argument,
            final ImmutableList<DeclaredStatement<?>> substatements) {
        super(argument, substatements);
    }
}
