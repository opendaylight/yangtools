/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

public final class BitsTypeBuilder extends AbstractRestrictedTypeBuilder<BitsTypeDefinition> {
    private final Builder<String, Bit> builder = ImmutableMap.builder();

    BitsTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    BitsTypeBuilder(final BitsTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public BitsTypeBuilder addBit(@Nonnull final Bit item) {
        // in case we are dealing with a restricted bits type, validate if the bit is a subset of its base type
        if (getBaseType() != null) {
            validateRestrictedBit(item);
        }

        builder.put(item.getName(), item);
        touch();
        return this;
    }

    private void validateRestrictedBit(@Nonnull final Bit item) {
        boolean isASubsetOfBaseBits = false;
        for (Bit baseTypeBit : getBaseType().getBits()) {
            if (item.getName().equals(baseTypeBit.getName())) {
                if (item.getPosition() != baseTypeBit.getPosition()) {
                    throw new InvalidBitDefinitionException(item, "Position of bit '%s' must be the same as the "
                            + "position of corresponding bit in the base bits type %s.", item.getName(),
                            getBaseType().getQName());
                }
                isASubsetOfBaseBits = true;
                break;
            }
        }

        if (!isASubsetOfBaseBits) {
            throw new InvalidBitDefinitionException(item, "Bit '%s' is not a subset of its base bits type %s.",
                    item.getName(), getBaseType().getQName());
        }
    }

    @Override
    public BitsTypeDefinition buildType() {
        final Map<String, Bit> map = builder.build();
        final Map<Long, Bit> positionMap = new TreeMap<>();

        for (Bit b : map.values()) {
            final Bit conflict = positionMap.put(b.getPosition(), b);
            if (conflict != null) {
                throw new InvalidBitDefinitionException(b, "Bit %s conflicts on position with bit ", conflict);
            }
        }

        if (getBaseType() == null) {
            return new BaseBitsType(getPath(), getUnknownSchemaNodes(), positionMap.values());
        } else {
            return new RestrictedBitsType(getBaseType(), getPath(), getUnknownSchemaNodes(), positionMap.values());
        }
    }
}
