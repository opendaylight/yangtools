/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.Nullable;
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
     * Returns set of all child nodes defined within this DataNodeContainer. Although the return type is a collection,
     * each node is guaranteed to be present at most once.
     *
     * <p>
     * Note that the nodes returned are <strong>NOT</strong> {@code data nodes}, but rather {@link DataSchemaNode}s,
     * hence {@link ChoiceSchemaNode} and {@link CaseSchemaNode} are present instead of their children. This
     * is consistent with {@code schema tree}.
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
     * <p>
     * Note that the nodes searched are <strong>NOT</strong> {@code data nodes}, but rather {@link DataSchemaNode}s,
     * hence {@link ChoiceSchemaNode} and {@link CaseSchemaNode} are returned instead of their matching children. This
     * is consistent with {@code schema tree}.
     *
     * @param name QName of child
     * @return child node of this DataNodeContainer if child with given name is present, null otherwise
     * @deprecated Use {@link #findDataChildByName(QName)} instead.
     * @throws NullPointerException if {@code name} is null
     */
    @Deprecated
    default @Nullable DataSchemaNode getDataChildByName(final QName name) {
        return findDataChildByName(name).orElse(null);
    }

    /**
     * Returns the child node corresponding to the specified name.
     *
     * <p>
     * Note that the nodes searched are <strong>NOT</strong> {@code data nodes}, but rather {@link DataSchemaNode}s,
     * hence {@link ChoiceSchemaNode} and {@link CaseSchemaNode} are returned instead of their matching children.
     *
     * @param name QName of child
     * @return child node of this DataNodeContainer if child with given name is present, empty otherwise
     * @throws NullPointerException if {@code name} is null
     */
    Optional<DataSchemaNode> findDataChildByName(QName name);

    /**
     * Returns grouping nodes used ny this container.
     *
     * @return Set of all uses nodes defined within this DataNodeContainer
     */
    Set<UsesNode> getUses();

    /**
     * Returns a {@code data node} identified by a QName. This method is distinct from
     * {@link #findDataChildByName(QName)} in that it skips over {@link ChoiceSchemaNode}s and {@link CaseSchemaNode}s,
     * hence mirroring layout of the {@code data tree}, not {@code schema tree}.
     *
     * @param name QName identifier of the data node
     * @return Direct or indirect child of this DataNodeContainer which is a {@code data node}, empty otherwise
     * @throws NullPointerException if {@code name} is null
     */
    @Beta
    default Optional<DataSchemaNode> findDataTreeChild(final QName name) {
        // First we try to find a direct child and check if it is a data node (as per RFC7950)
        final Optional<DataSchemaNode> optDataChild = findDataChildByName(name);
        if (HelperMethods.isDataNode(optDataChild)) {
            return optDataChild;
        }

        // There either is no such node present, or there are Choice/CaseSchemaNodes with the same name involved,
        // hence we have to resort to a full search.
        for (DataSchemaNode child : getChildNodes()) {
            if (child instanceof ChoiceSchemaNode) {
                for (CaseSchemaNode choiceCase : ((ChoiceSchemaNode) child).getCases().values()) {
                    final Optional<DataSchemaNode> caseChild = choiceCase.findDataTreeChild(name);
                    if (caseChild.isPresent()) {
                        return caseChild;
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Returns a {@code data node} identified by a series of QNames. This is equivalent to incrementally calling
     * {@link #findDataTreeChild(QName)}.
     *
     * @param path Series of QNames towards identifying the requested data node
     * @return Direct or indirect child of this DataNodeContainer which is a {@code data node}, empty otherwise
     * @throws IllegalArgumentException if {@code path} is determined to go beyond a not-container-nor-list node.
     * @throws NoSuchElementException if {@code path} is empty
     * @throws NullPointerException if {@code path} is null or contains a null
     */
    @Beta
    default Optional<DataSchemaNode> findDataTreeChild(final QName... path) {
        return findDataTreeChild(Arrays.asList(path));
    }

    /**
     * Returns a {@code data node} identified by a series of QNames. This is equivalent to incrementally calling
     * {@link #findDataTreeChild(QName)}.
     *
     * @param path Series of QNames towards identifying the requested data node
     * @return Direct or indirect child of this DataNodeContainer which is a {@code data node}, empty otherwise
     * @throws IllegalArgumentException if {@code path} is determined to go beyond a not-container-nor-list node.
     * @throws NoSuchElementException if {@code path} is empty
     * @throws NullPointerException if {@code path} is null or contains a null
     */
    @Beta
    default Optional<DataSchemaNode> findDataTreeChild(final Iterable<QName> path) {
        final Iterator<QName> it = path.iterator();
        DataNodeContainer parent = this;
        do {
            final Optional<DataSchemaNode> optChild = parent.findDataTreeChild(requireNonNull(it.next()));
            if (!optChild.isPresent() || !it.hasNext()) {
                return optChild;
            }

            final DataSchemaNode child = optChild.get();
            checkArgument(child instanceof DataNodeContainer, "Path %s extends beyond terminal child %s", path, child);
            parent = (DataNodeContainer) child;
        } while (true);
    }
}
