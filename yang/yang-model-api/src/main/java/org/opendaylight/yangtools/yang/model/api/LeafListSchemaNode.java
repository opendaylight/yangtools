/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * Interface describing YANG 'leaf-list' statement.
 */
public interface LeafListSchemaNode extends TypedSchemaNode {
    /**
     * YANG 'ordered-by' statement. It defines whether the order of entries
     * within this leaf-list are determined by the user or the system. If not
     * present, default is false.
     *
     * @return true if ordered-by argument is "user", false otherwise
     */
    boolean isUserOrdered();

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementation of
     * LeafListSchemaNode which does not support default statements.
     * YANG leaf-list statement has been changed in YANG 1.1 (RFC7950) and now allows default statements.
     *
     * @return collection of Strings which specify the default values of this leaf-list
     */
    @Nonnull default Collection<String> getDefaults() {
        return ImmutableList.of();
    }
}
