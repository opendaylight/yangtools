/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;

/**
 * Common interface capturing general layout of a top-level YANG declared statement -- either a {@link ModuleStatement}
 * or a {@link SubmoduleStatement}.
 */
@Beta
public sealed interface RootDeclaredStatement extends DataDefinitionStatement.MultipleIn<Unqualified>,
        AugmentStatement.MultipleIn<Unqualified>, ContactStatement.OptionalIn<Unqualified>,
        DescriptionStatement.OptionalIn<Unqualified>, DeviationStatement.MultipleIn<Unqualified>,
        ExtensionStatement.MultipleIn<Unqualified>, FeatureStatement.MultipleIn<Unqualified>,
        GroupingStatementMultipleIn<Unqualified>, IdentityStatement.MultipleIn<Unqualified>,
        ImportStatement.MultipleIn<Unqualified>, IncludeStatement.MultipleIn<Unqualified>,
        NotificationStatement.MultipleIn<Unqualified>, OrganizationStatement.OptionalIn<Unqualified>,
        ReferenceStatement.OptionalIn<Unqualified>, RevisionStatement.MultipleIn<Unqualified>,
        RpcStatement.MultipleIn<Unqualified>, TypedefStatement.MultipleIn<Unqualified>,
        YangVersionStatement.OptionalIn<Unqualified> permits ModuleStatement, SubmoduleStatement {
    @Override
    Unqualified argument();
}
