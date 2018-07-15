/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RefineStatement extends ConfigStatementContainerDeclaredStatement<SchemaNodeIdentifier>,
        DocumentedDeclaredStatement<SchemaNodeIdentifier>, ConditionalDeclaredStatement<SchemaNodeIdentifier>,
        MandatoryStatementContainerDeclaredStatement<SchemaNodeIdentifier>, MustStatementContainer {
    default @Nonnull String getTargetNode() {
        return rawArgument();
    }

    default @Nonnull Collection<? extends DefaultStatement> getDefaults() {
        return declaredSubstatements(DefaultStatement.class);
    }

    default @Nullable PresenceStatement getPresence() {
        final Optional<PresenceStatement> opt = findFirstDeclaredSubstatement(PresenceStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    default @Nullable MinElementsStatement getMinElements() {
        final Optional<MinElementsStatement> opt = findFirstDeclaredSubstatement(MinElementsStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    default @Nullable MaxElementsStatement getMaxElements() {
        final Optional<MaxElementsStatement> opt = findFirstDeclaredSubstatement(MaxElementsStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
