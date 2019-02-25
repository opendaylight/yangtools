/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

/**
 * Statement which can contain action statements.
 */
@Beta
public interface ActionStatementAwareDeclaredStatement<A> extends DeclaredStatement<A> {
    /**
     * Return actions in this container. Since actions were introduced in RFC7950, hence RFC6020 semantics always
     * returns an empty collection.
     *
     * @return collection of action statements
     */
    default @NonNull Collection<? extends ActionStatement> getActions() {
        return declaredSubstatements(ActionStatement.class);
    }
}
