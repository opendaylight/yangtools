/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.MapEntry;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class AbstractMapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;

    AbstractMapModificationStrategy(final MapEntry<?> support, final ListSchemaNode schema,
        final DataTreeConfiguration treeConfig) {
        super(support, treeConfig);
        entryStrategy = Optional.of(ListEntryModificationStrategy.of(schema, treeConfig));
    }

    // FIXME: this is a hack, originally introduced in
    //        Change-Id: I9dc02a1917f38e8a0d62279843974b9869c48693. DataTreeRoot needs to be fixed up to properly
    //        handle the lookup of through maps.
    @Override
    public final Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof NodeIdentifierWithPredicates) {
            return entryStrategy;
        }
        // In case we already are in a MapEntry node(for example DataTree rooted at MapEntry)
        // try to retrieve the child that the identifier should be pointing to from our entryStrategy
        // if we have one. If the entryStrategy cannot find this child we just return the absent
        // we get from it.
        return entryStrategy.get().getChild(identifier);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("entry", entryStrategy.get()).toString();
    }
}
