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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sharing instances of {@link LeafSetEntryNode}s which have low cardinality -- e.g. those which hold
 * boolean or enumeration values. Instances containing attributes are not interned.
 *
 * <p>
 * Such objects have cardinality which is capped at the product of QNAMES * TYPE_CARDINALITY, where QNAMES is the total
 * number of different QNames where the type is used and TYPE_CARDINALITY is the number of possible values for the type.
 * Boolean has cardinality of 2, enumerations have cardinality equal to the number of enum statements.
 *
 * <p>
 * The theory here is that we tend to have a large number (100K+) of entries in a few places, which could end up hogging
 * the heap retained via the DataTree with duplicate objects (same QName, same value, different object). Using this
 * utility, such objects will end up reusing the same object, preventing this overhead.
 */
@Beta
public final class LeafsetEntryInterner {
    private static final Logger LOG = LoggerFactory.getLogger(LeafsetEntryInterner.class);
    private static final LeafsetEntryInterner INSTANCE = new LeafsetEntryInterner();
    private static final Interner<Object> INTERNER = Interners.newWeakInterner();

    private LeafsetEntryInterner() {

    }

    @SuppressWarnings("static-method")
    public <T extends LeafSetEntryNode<?>> @NonNull T intern(final @NonNull T sample) {
        if (!sample.getAttributes().isEmpty()) {
            // Non-empty attributes, do not intern
            return sample;
        }

        /*
         * We do not perform type checks here as they are implied by #forSchema(LeafListSchemaNode). Any misuse can
         * result in inappropriate candidates being interned, but the alternative would be quite a bit slower.
         */
        @SuppressWarnings("unchecked")
        final T ret = (T) INTERNER.intern(sample);
        LOG.trace("Interned object {} to {}", sample, ret);
        return ret;
    }

    /**
     * Return a {@link LeafsetEntryInterner} for a particular schema. Interner instances must be used only for leafset
     * entries for that particular schema, otherwise they may produce unexpected results.
     *
     * @param schema Schema of the parent leaf set
     * @return An interner instance, or null if the leafset's type should not be interned.
     */
    public static @Nullable LeafsetEntryInterner forSchema(final @Nullable LeafListSchemaNode schema) {
        if (schema != null) {
            final TypeDefinition<?> type = schema.getType();
            if (type instanceof BooleanTypeDefinition || type instanceof EnumTypeDefinition
                    || type instanceof IdentityrefTypeDefinition) {
                return INSTANCE;
            }
        }
        return null;
    }
}
