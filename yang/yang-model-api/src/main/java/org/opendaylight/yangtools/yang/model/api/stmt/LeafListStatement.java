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

public interface LeafListStatement extends DataDefinitionStatement, MultipleElementsGroup, TypeGroup {

    @Nullable Collection<? extends MustStatement> getMusts();

    @Nullable ConfigStatement getConfig();

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementation of
     * LeafListStatement which does not support default statements.
     * YANG leaf-list statement has been changed in YANG 1.1 (RFC7950) and now allows default statements.
     *
     * @return collection of default statements
     */
     // FIXME: version 2.0.0: make this method non-default
    @Nonnull default Collection<? extends DefaultStatement> getDefaults() {
        return ImmutableList.of();
    }
}
