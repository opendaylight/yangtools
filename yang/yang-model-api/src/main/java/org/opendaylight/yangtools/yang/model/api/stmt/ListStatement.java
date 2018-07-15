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
import org.opendaylight.yangtools.yang.common.QName;

public interface ListStatement extends MultipleElementsDeclaredStatement,
        DataDefinitionContainer.WithReusableDefinitions, ConfigStatementContainerDeclaredStatement<QName>,
        ActionStatementContainer, MustStatementContainer, NotificationStatementContainer {
    default KeyStatement getKey() {
        final Optional<KeyStatement> opt = findFirstDeclaredSubstatement(KeyStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    default @Nonnull Collection<? extends UniqueStatement> getUnique() {
        return declaredSubstatements(UniqueStatement.class);
    }
}
