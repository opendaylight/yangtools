/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

@Beta
public interface TypeAwareDeclaredStatement<A> extends DeclaredStatement<A> {
    default @NonNull TypeStatement getType() {
        return findFirstDeclaredSubstatement(TypeStatement.class).orElseThrow();
    }

    default @NonNull Optional<UnitsStatement> getUnits() {
        return findFirstDeclaredSubstatement(UnitsStatement.class);
    }
}
