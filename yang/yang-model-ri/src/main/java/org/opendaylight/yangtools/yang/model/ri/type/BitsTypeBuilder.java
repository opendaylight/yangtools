/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.type;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

public final class BitsTypeBuilder extends AbstractRestrictedTypeBuilder<BitsTypeDefinition> {
    private final ImmutableMap.Builder<String, Bit> builder = ImmutableMap.builder();

    BitsTypeBuilder(final QName qname) {
        super(null, qname);
    }

    BitsTypeBuilder(final BitsTypeDefinition baseType, final QName qname) {
        super(baseType, qname);
    }

    public BitsTypeBuilder addBit(final @NonNull Bit item) {
        // in case we are dealing with a restricted bits type, validate if the bit is a subset of its base type
        if (getBaseType() != null) {
            validateRestrictedBit(item);
        }

        builder.put(item.getName(), item);
        touch();
        return this;
    }

    private void validateRestrictedBit(final @NonNull Bit item) {
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
        final Map<Uint32, Bit> positionMap = new TreeMap<>();

        for (Bit b : map.values()) {
            final Bit conflict = positionMap.put(b.getPosition(), b);
            if (conflict != null) {
                throw new InvalidBitDefinitionException(b, "Bit %s conflicts on position with bit ", conflict);
            }
        }

        return getBaseType() == null ? new BaseBitsType(getQName(), getUnknownSchemaNodes(), positionMap.values())
                : new RestrictedBitsType(getBaseType(), getQName(), getUnknownSchemaNodes(), positionMap.values());
    }
}
