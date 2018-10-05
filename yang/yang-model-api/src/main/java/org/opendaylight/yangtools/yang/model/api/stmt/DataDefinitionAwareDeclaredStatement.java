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

@Beta
public interface DataDefinitionAwareDeclaredStatement<A> extends DeclaredStatement<A>, DataDefinitionContainer {
    @Override
    default @NonNull Collection<? extends DataDefinitionStatement> getDataDefinitions() {
        return declaredSubstatements(DataDefinitionStatement.class);
    }

    interface WithReusableDefinitions<A> extends DataDefinitionAwareDeclaredStatement<A>,
            DataDefinitionContainer.WithReusableDefinitions {
        @Override
        default @NonNull Collection<? extends TypedefStatement> getTypedefs() {
            return declaredSubstatements(TypedefStatement.class);
        }

        @Override
        default @NonNull Collection<? extends GroupingStatement> getGroupings() {
            return declaredSubstatements(GroupingStatement.class);
        }
    }

}
