/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface RefineStatement extends ConfigStatementContainerDeclaredStatement<SchemaNodeIdentifier>, DocumentationGroup,
        ConditionalFeature, MandatoryStatementContainer, MustStatementContainer {
    String getTargetNode();

    @Nonnull
    Collection<? extends DefaultStatement> getDefaults();

    @Nullable
    PresenceStatement getPresence();

    @Nullable
    MinElementsStatement getMinElements();

    @Nullable
    MaxElementsStatement getMaxElements();
}
