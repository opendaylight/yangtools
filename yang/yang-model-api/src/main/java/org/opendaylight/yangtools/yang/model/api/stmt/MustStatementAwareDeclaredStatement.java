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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

@Beta
public interface MustStatementAwareDeclaredStatement<A> extends DeclaredStatement<A>,
        MustStatementContainer {
    @Override
    default @Nonnull Collection<? extends MustStatement> getMusts() {
        return declaredSubstatements(MustStatement.class);
    }
}
