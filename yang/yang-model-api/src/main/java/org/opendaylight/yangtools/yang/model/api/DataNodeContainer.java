/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Node which can contains other nodes.
 */
public interface DataNodeContainer {

    /**
     * Returns set of all newly defined types within this DataNodeContainer.
     *
     * @return typedef statements in lexicographical order
     */
    Set<TypeDefinition<?>> getTypeDefinitions();

    /**
     * Returns set of all child nodes defined within this DataNodeContainer.
     * Although the return type is a collection, each node is guaranteed to
     * be present at most once.
     *
     * @return child nodes in lexicographical order
     */
    Collection<DataSchemaNode> getChildNodes();

    /**
     * Returns set of all groupings defined within this DataNodeContainer.
     *
     * @return grouping statements in lexicographical order
     */
    Set<GroupingDefinition> getGroupings();

    /**
     * Returns the child node corresponding to the specified name.
     *
     * @param name
     *            QName of child
     * @return child node of this DataNodeContainer if child with given name is present, null otherwise
     *
     * @deprecated Use {@link #findDataChildByName(QName)} instead.
     */
    @Deprecated
    @Nullable default DataSchemaNode getDataChildByName(final QName name) {
        return findDataChildByName(name).orElse(null);
    }

    /**
     * Returns the child node corresponding to the specified name.
     *
     * @param name
     *            QName of child
     * @return child node of this DataNodeContainer if child with given name is present, empty otherwise
     * @throws NullPointerException if name is null
     */
    Optional<DataSchemaNode> findDataChildByName(QName name);

    /**
     * Returns grouping nodes used ny this container.
     *
     * @return Set of all uses nodes defined within this DataNodeContainer
     */
    Set<UsesNode> getUses();
}
