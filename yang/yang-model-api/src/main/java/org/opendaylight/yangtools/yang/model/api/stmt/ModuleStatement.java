/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;
import javax.annotation.Nonnull;

public interface ModuleStatement extends MetaDeclaredStatement<String>, ModuleHeaderGroup, LinkageGroup, RevisionGroup,
        BodyGroup {
    default @Nonnull String getName() {
        return rawArgument();
    }

    @Override
    default YangVersionStatement getYangVersion() {
        final Optional<YangVersionStatement> opt = findFirstDeclaredSubstatement(YangVersionStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Nonnull
    @Override
    default NamespaceStatement getNamespace() {
        final Optional<NamespaceStatement> opt = findFirstDeclaredSubstatement(NamespaceStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default PrefixStatement getPrefix() {
        final Optional<PrefixStatement> opt = findFirstDeclaredSubstatement(PrefixStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
