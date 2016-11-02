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
import org.opendaylight.yangtools.yang.common.QName;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

public interface OutputStatement extends DeclaredStatement<QName>, DataDefinitionContainer.WithReusableDefinitions {

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementation of
     * OutputStatement which does not support must statements.
     * YANG output statement has been changed in YANG 1.1 (RFC7950) and now allows must statements.
     *
     * @return collection of must statements
     */
    @Nonnull default Collection<? extends MustStatement> getMusts() {
        return ImmutableList.of();
    }
}
