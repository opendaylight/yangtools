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
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

public final class EnumerationBaseTypeBuilder extends BaseTypeBuilder<EnumTypeDefinition> {
    private final Builder<String, EnumPair> builder = ImmutableMap.builder();

    EnumerationBaseTypeBuilder(final SchemaPath path) {
        super(path);
    }

    public void addEnum(@Nonnull final EnumPair item) {
        builder.put(item.getName(), item);
    }

    @Override
    public EnumerationBaseType build() {
        final Map<String, EnumPair> map = builder.build();

        // FIXME: run null value checks and re-generate EnumPairs as appropriate

        return new EnumerationBaseType(getPath(), map.values());
    }
}
