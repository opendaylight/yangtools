/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sharing instances of {@link NormalizedNode}s which have low cardinality. We currently implement
 * this for LeafNode and LeafSetEntryNode objects, whose value type is either boolean or enumeration. A further
 * restriction is that attributes must not be present.
 *
 * Such objects have cardinality which is capped at the product of QNAMES * TYPE_CARDINALITY, where QNAMES is the total
 * number of different QNames where the type is used and TYPE_CARDINALITY is the number of possible values for the type.
 * Boolean has cardinality of 2, enumerations have cardinality equal to the number of enum statements.
 *
 * The theory here is that we tend to have a large number (100K+) of entries in a few places, which could end up hogging
 * the heap retained via the DataTree with duplicate objects (same QName, same value, different object). Using this
 * utility, such objects will end up reusing the same object, preventing this overhead.
 */
@Beta
public final class NormalizedNodeInterners {
    private static final Logger LOG = LoggerFactory.getLogger(NormalizedNodeInterners.class);
    private static final Interner<Object> LEAF_INTERNER = Interners.newWeakInterner();
    private static final Interner<Object> LEAFSET_ENTRY_INTERNER = Interners.newWeakInterner();

    private NormalizedNodeInterners() {
        throw new UnsupportedOperationException();
    }

    private static <T extends NormalizedNode<?, ?>> T intern(final TypeDefinition<?> type, final Interner<Object> where, final T what) {
        if (what instanceof AttributesContainer && !((AttributesContainer) what).getAttributes().isEmpty()) {
            // Non-empty attributes, do not intern
            return what;
        }

        // Take advantage of boolean types mapping to a java.lang.Boolean
        if (!(what.getValue() instanceof Boolean)) {
            // FIXME: BUG-4268: we really just need the semantic portion (enum or boolean), this recursion just shows
            //        how broken the TypeDefinition<?>
            TypeDefinition<?> real = type;
            while (real.getBaseType() != null) {
                real = real.getBaseType();
            }

            if (!(real instanceof EnumTypeDefinition)) {
                return what;
            }
        }

        // All checks completed, intern the sample
        @SuppressWarnings("unchecked")
        final T ret = (T) where.intern(what);
        LOG.trace("Interned object %s to %s", what, ret);
        return ret;
    }

    public static <T extends LeafNode<?>> T internLeaf(final LeafSchemaNode node, final T sample) {
        return intern(node.getType(), LEAF_INTERNER, sample);
    }

    public static <T extends LeafSetEntryNode<?>> T internLeafEntry(final LeafListSchemaNode node, final T sample) {
        return intern(node.getType(), LEAFSET_ENTRY_INTERNER, sample);
    }
}
