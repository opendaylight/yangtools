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

public final class EnumerationTypeBuilder extends TypeBuilder<EnumTypeDefinition> {
    private final Builder<String, EnumPair> builder = ImmutableMap.builder();

    EnumerationTypeBuilder(final SchemaPath path) {
        super(null, path);
    }

    public EnumerationTypeBuilder addEnum(@Nonnull final EnumPair item) {
        builder.put(item.getName(), item);
        return this;
    }

    @Override
    public EnumTypeDefinition build() {
        final Map<String, EnumPair> map = builder.build();
        final Map<Integer, EnumPair> positionMap = new HashMap<>();

        for (EnumPair p : map.values()) {
            final EnumPair conflict = positionMap.put(p.getValue(), p);
            if (conflict != null) {
                throw new InvalidEnumDefinitionException(p, "Bit %s conflicts on position with bit ", conflict);
            }
        }

        return new BaseEnumerationType(getPath(), getUnknownSchemaNodes(), map.values());
    }
}
