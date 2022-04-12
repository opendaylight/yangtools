/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc8819.stmt;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.concepts.Tag;
import org.opendaylight.yangtools.rfc8819.model.api.ModuleTagStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString.WithSubstatements;


public class ModuleTagStatementImpl extends WithSubstatements<Tag> implements ModuleTagStatement {
    protected ModuleTagStatementImpl(Tag argument, ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(argument, substatements);
    }
}
