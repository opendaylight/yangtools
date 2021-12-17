/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public class ReactorDeclaredModel implements Immutable {
    private final ImmutableList<DeclaredStatement> rootStatements;

    public ReactorDeclaredModel(final List<DeclaredStatement> rootStatements) {
        this.rootStatements = ImmutableList.copyOf(rootStatements);
    }

    public ImmutableList<DeclaredStatement> getRootStatements() {
        return rootStatements;
    }
}
