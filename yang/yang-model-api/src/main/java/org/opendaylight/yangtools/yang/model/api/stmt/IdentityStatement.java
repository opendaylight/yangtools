/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface IdentityStatement extends DeclaredStatement<QName>, DocumentationGroup.WithStatus, ConditionalFeature {

    @Nonnull QName getName();

    @Nullable BaseStatement getBase();

    @Nonnull default Collection<? extends BaseStatement> getBases() {
        final BaseStatement base = getBase();
        return base == null ? ImmutableList.of() : ImmutableList.of(base);
    }
}
