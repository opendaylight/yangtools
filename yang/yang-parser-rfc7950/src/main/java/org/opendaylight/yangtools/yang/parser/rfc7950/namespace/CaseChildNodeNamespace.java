/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.namespace;

import com.google.common.annotations.Beta;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 * Statement local namespace, which holds choice/case children schema node descendants.
 */
@Beta
public final class CaseChildNodeNamespace<D extends DeclaredStatement<QName>, E extends EffectiveStatement<QName, D>>
    extends NamespaceBehaviour<QName, StmtContext<?, D, E>, CaseChildNodeNamespace<D, E>>
    implements StatementNamespace<QName, D, E> {

    public CaseChildNodeNamespace() {
        super((Class) CaseChildNodeNamespace.class);
    }

    @Override
    public StmtContext<?, D, E> get(@Nonnull final QName key) {
        throw new UnsupportedOperationException("Direct access to namespace is not supported");
    }

    @Override
    public StmtContext<?, D, E> getFrom(final NamespaceStorageNode storage, final QName key) {
        if (isCaseStorageNode(storage)) {
            final NamespaceStorageNode ancestor = getCaseClosestAncestor(storage);
            if (!isAugmentStorageNode(ancestor)) {
                return getCaseClosestAncestor(storage).getFromLocalStorage(getIdentifier(), key);
            }
        }

        throw new UnsupportedOperationException("Not an effective case storage node.");
    }

    @Override
    public Map<QName, StmtContext<?, D, E>> getAllFrom(final NamespaceStorageNode storage) {
        if (isCaseStorageNode(storage)) {
            final NamespaceStorageNode ancestor = getCaseClosestAncestor(storage);
            if (!isAugmentStorageNode(ancestor)) {
                return getCaseClosestAncestor(storage).getAllFromLocalStorage(getIdentifier());
            }
        }

        throw new UnsupportedOperationException("Not an effective case storage node.");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTo(final NamespaceStorageNode storage, final QName key, final StmtContext<?, D, E> value) {

        // If storage is case which is not under augment then also put nodes into closest ancestor storage node
        // to detect collision, after that continue to add node to case to ensure that augment can find the target.
        if (isCaseStorageNode(storage)) {
            final NamespaceStorageNode ancestor = getCaseClosestAncestor(storage);
            if (!isAugmentStorageNode(ancestor)) {
                final StmtContext<?, D, E> prev = ancestor.putToLocalStorageIfAbsent(
                    CaseChildNodeNamespace.class, key, value);
                if (prev != null) {
                    throw new SourceException(value.getStatementSourceReference(),
                        "Error in module '%s': cannot add '%s'. Node name collision: '%s' already declared at %s",
                        value.getRoot().getStatementArgument(), key, prev.getStatementArgument(),
                        prev.getStatementSourceReference());
                }
            }
        }
    }

    private static NamespaceStorageNode getCaseClosestAncestor(final NamespaceStorageNode storage) {
        NamespaceStorageNode current = storage;

        while (!isLocalOrRootStmtLocal(current.getStorageNodeType())
                || isCaseStorageNode(current)
                || isChoiceStorageNode(current)) {
            current = current.getParentNamespaceStorage();
        }
        return current;
    }

    private static boolean isLocalOrRootStmtLocal(final StorageNodeType type) {
        return type == StorageNodeType.STATEMENT_LOCAL
                || type == StorageNodeType.ROOT_STATEMENT_LOCAL;
    }

    private static boolean isCaseStorageNode(final NamespaceStorageNode storage) {
        return (storage instanceof StmtContext)
            && (((StmtContext) storage).getPublicDefinition() == YangStmtMapping.CASE);
    }

    private static boolean isChoiceStorageNode(final NamespaceStorageNode storage) {
        return (storage instanceof StmtContext)
                && (((StmtContext) storage).getPublicDefinition() == YangStmtMapping.CHOICE);
    }

    private static boolean isAugmentStorageNode(final NamespaceStorageNode storage) {
        return ((storage instanceof StmtContext)
            && (((StmtContext) storage).getPublicDefinition() == YangStmtMapping.AUGMENT));
    }
}
