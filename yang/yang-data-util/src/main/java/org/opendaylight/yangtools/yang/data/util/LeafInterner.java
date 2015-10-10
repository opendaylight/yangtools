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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for sharing instances of {@link LeafNode}s which have low cardinality-- e.g. those which hold
 * boolean or enumeration values. Instances containing attributes are not interned.
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
public abstract class LeafInterner {
    private static final class Noop extends LeafInterner {
        @Override
        public <T extends LeafNode<?>> T intern(final T sample) {
            return sample;
        }
    }

    private static final class Weak extends LeafInterner {
        private static final Logger LOG = LoggerFactory.getLogger(Weak.class);
        private static final Interner<Object> INTERNER = Interners.newWeakInterner();

        @Override
        public <T extends LeafNode<?>> T intern(final T sample) {
            if (!((AttributesContainer) sample).getAttributes().isEmpty()) {
                // Non-empty attributes, do not intern
                return sample;
            }

            // All checks completed, intern the sample
            @SuppressWarnings("unchecked")
            final T ret = (T) INTERNER.intern(sample);
            LOG.trace("Interned object %s to %s", sample, ret);
            return ret;
        }
    }

    private static final LeafInterner NOOP = new Noop();
    private static final LeafInterner WEAK = new Weak();

    LeafInterner() {

    }

    /**
     * Return a {@link LeafInterner} for a particular schema. Interner instances must not be reused for leaves of
     * different types, otherwise they may produce unexpected results.
     *
     * @param schema The leaf node's schema
     * @return An interner instance
     */
    @Nonnull public static LeafInterner forSchema(@Nullable final LeafSchemaNode schema) {
        if (schema != null) {
            final TypeDefinition<?> type = schema.getType();
            if (type instanceof BooleanTypeDefinition || type instanceof EnumTypeDefinition) {
                return WEAK;
            }
        }

        return NOOP;
    }

    public abstract <T extends LeafNode<?>> T intern(final T sample);
}
