/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.VerifyException;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
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
    @NonNull Collection<? extends @NonNull TypeDefinition<?>> getTypeDefinitions();

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
    @NonNull Collection<? extends @NonNull DataSchemaNode> getChildNodes();

    /**
     * Returns set of all groupings defined within this DataNodeContainer.
     *
     * @return grouping statements in lexicographical order
     */
    @NonNull Collection<? extends @NonNull GroupingDefinition> getGroupings();

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
     * @throws NullPointerException if {@code name} is null
     */
    @Nullable DataSchemaNode dataChildByName(QName name);

    /**
     * Returns the child node corresponding to the specified name.
     *
     * <p>
     * Note that the nodes searched are <strong>NOT</strong> {@code data nodes}, but rather {@link DataSchemaNode}s,
     * hence {@link ChoiceSchemaNode} and {@link CaseSchemaNode} are returned instead of their matching children. This
     * is consistent with {@code schema tree}.
     *
     * @param name QName of child
     * @return child node of this DataNodeContainer
     * @throws NullPointerException if {@code name} is null
     * @throws VerifyException if the child does not exist
     */
    default @NonNull DataSchemaNode getDataChildByName(final QName name) {
        return verifyNotNull(dataChildByName(name), "No child matching %s found", name);
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
    default Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return Optional.ofNullable(dataChildByName(name));
    }

    /**
     * Returns the child node corresponding to the specified name.
     *
     * <p>
     * Note that the nodes searched are <strong>NOT</strong> {@code data nodes}, but rather {@link DataSchemaNode}s,
     * hence {@link ChoiceSchemaNode} and {@link CaseSchemaNode} are returned instead of their matching children.
     *
     * @param first QName of first child
     * @param others QNames of subsequent children
     * @return child node of this DataNodeContainer if child with given name is present, empty otherwise
     * @throws NullPointerException if any argument is null
     */
    default Optional<DataSchemaNode> findDataChildByName(final QName first, final QName... others) {
        var current = dataChildByName(first);
        for (var qname : others) {
            if (current instanceof DataNodeContainer container) {
                current = container.dataChildByName(qname);
            } else {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(current);
    }

    /**
     * Returns grouping nodes used ny this container.
     *
     * @return Set of all uses nodes defined within this DataNodeContainer
     */
    @NonNull Collection<? extends @NonNull UsesNode> getUses();

    /**
     * Returns a {@code data node} identified by a QName. This method is distinct from
     * {@link #findDataChildByName(QName)} in that it skips over {@link ChoiceSchemaNode}s and {@link CaseSchemaNode}s,
     * hence mirroring layout of the {@code data tree}, not {@code schema tree}.
     *
     * @param name QName identifier of the data node
     * @return Direct or indirect child of this DataNodeContainer which is a {@code data node}, empty otherwise
     * @throws NullPointerException if {@code name} is {@code null}
     */
    default Optional<DataSchemaNode> findDataTreeChild(final QName name) {
        // First we try to find a direct child and check if it is a data node (as per RFC7950)
        final var dataChild = dataChildByName(name);
        if (isDataNode(dataChild)) {
            return Optional.of(dataChild);
        }

        // There either is no such node present, or there are Choice/CaseSchemaNodes with the same name involved,
        // hence we have to resort to a full search.
        for (var child : getChildNodes()) {
            if (child instanceof ChoiceSchemaNode choice) {
                for (var choiceCase : choice.getCases()) {
                    final var caseChild = choiceCase.findDataTreeChild(name);
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
     * @throws NullPointerException if {@code path} is {@code null} or contains a {@code null} element
     */
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
     * @throws NullPointerException if {@code path} is {@code null} or contains a {@code null} element
     */
    default Optional<DataSchemaNode> findDataTreeChild(final Iterable<QName> path) {
        final var it = path.iterator();
        DataNodeContainer parent = this;
        do {
            final var optChild = parent.findDataTreeChild(requireNonNull(it.next()));
            if (optChild.isEmpty() || !it.hasNext()) {
                return optChild;
            }

            final var child = optChild.orElseThrow();
            if (!(child instanceof DataNodeContainer container)) {
                throw new IllegalArgumentException("Path " + path + " extends beyond terminal child " + child);
            }
            parent = container;
        } while (true);
    }

    private static boolean isDataNode(final DataSchemaNode node) {
        return node instanceof ContainerSchemaNode || node instanceof LeafSchemaNode
            || node instanceof LeafListSchemaNode || node instanceof ListSchemaNode
            || node instanceof AnydataSchemaNode || node instanceof AnyxmlSchemaNode;
    }
}
