/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;

public interface ModuleStatement extends MetaDeclaredStatement<String>, ModuleHeaderGroup, LinkageDeclaredStatement,
        RevisionAwareDeclaredStatement, BodyDeclaredStatement {
    @Override
    @NonNull String rawArgument();

    default @NonNull String getName() {
        return rawArgument();
    }

    @Override
    default YangVersionStatement getYangVersion() {
        final Optional<YangVersionStatement> opt = findFirstDeclaredSubstatement(YangVersionStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default NamespaceStatement getNamespace() {
        return findFirstDeclaredSubstatement(NamespaceStatement.class).get();
    }

    @Override
    default PrefixStatement getPrefix() {
        return findFirstDeclaredSubstatement(PrefixStatement.class).get();
    }
}
