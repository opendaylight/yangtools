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
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public final class EnumerationTypeBuilder extends AbstractRestrictedTypeBuilder<EnumTypeDefinition> {
    private final Builder<String, EnumPair> builder = ImmutableMap.builder();

    EnumerationTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    EnumerationTypeBuilder(final EnumTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    public EnumerationTypeBuilder addEnum(@Nonnull final EnumPair item) {
        // in case we are dealing with a restricted enumeration type, validate if the enum is a subset of its base type
        if (getBaseType() != null) {
            validateRestrictedEnum(item);
        }

        builder.put(item.getName(), item);
        touch();
        return this;
    }

    private void validateRestrictedEnum(@Nonnull final EnumPair item) {
        boolean isASubsetOfBaseEnums = false;
        for (EnumPair baseTypeEnumPair : getBaseType().getValues()) {
            if (item.getName().equals(baseTypeEnumPair.getName())) {
                if (item.getValue() != baseTypeEnumPair.getValue()) {
                    throw new InvalidEnumDefinitionException(item, "Value of enum '%s' must be the same as the value"
                            + " of corresponding enum in the base enumeration type %s.", item.getName(),
                            getBaseType().getQName());
                }
                isASubsetOfBaseEnums = true;
                break;
            }
        }

        if (!isASubsetOfBaseEnums) {
            throw new InvalidEnumDefinitionException(item, "Enum '%s' is not a subset of its base enumeration type %s.",
                    item.getName(), getBaseType().getQName());
        }
    }

    @Override
    public EnumTypeDefinition buildType() {
        final Map<String, EnumPair> map = builder.build();
        final Map<Integer, EnumPair> positionMap = new HashMap<>();

        for (EnumPair p : map.values()) {
            final EnumPair conflict = positionMap.put(p.getValue(), p);
            if (conflict != null) {
                throw new InvalidEnumDefinitionException(p, "Bit %s conflicts on position with bit ", conflict);
            }
        }

        if (getBaseType() == null) {
            return new BaseEnumerationType(getPath(), getUnknownSchemaNodes(), map.values());
        } else {
            return new RestrictedEnumerationType(getBaseType(), getPath(), getUnknownSchemaNodes(), map.values());
        }
    }
}
