/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public class StringStringCodec extends TypeDefinitionAwareCodec<String, StringTypeDefinition>
        implements StringCodec<String> {

    private final RangeSet<Integer> lengths;

    StringStringCodec(final StringTypeDefinition typeDef) {
        super(Optional.of(typeDef), String.class);

        final Collection<LengthConstraint> constraints = typeDef.getLengthConstraints();
        if (!constraints.isEmpty()) {
            final RangeSet<Integer> tmp = TreeRangeSet.create();
            for (LengthConstraint c : constraints) {
                tmp.add(Range.closed(c.getMin().intValue(), c.getMax().intValue()));
            }

            lengths = ImmutableRangeSet.copyOf(tmp);
        } else {
            lengths = null;
        }
    }

    public static StringStringCodec from(final StringTypeDefinition normalizedType) {
        if (normalizedType.getPatternConstraints().isEmpty()) {
            return new StringStringCodec(normalizedType);
        }

        return new StringPatternCheckingCodec(normalizedType);
    }

    @Override
    public final String deserialize(final String stringRepresentation) {
        if (stringRepresentation == null) {
            // FIXME: These seems buggy, but someone may be using this behaviour
            return "";
        }
        validate(stringRepresentation);
        return stringRepresentation;
    }

    @Override
    public final String serialize(final String data) {
        return Objects.toString(data, "");
    }

    void validate(final String s) {
        if (lengths != null) {
            Preconditions.checkArgument(lengths.contains(s.length()), "String '%s' does not match allowed lengths %s",
                lengths);
        }
    }
}
