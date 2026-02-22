/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.YangSourceDefinition;

@NonNullByDefault
final class RuntimeEnumTypeObjectArchetype extends AbstractEnumTypeObjectArchetype {
    RuntimeEnumTypeObjectArchetype(final JavaTypeName name, final List<Pair> values,
            final List<AnnotationType> annotations) {
        super(name, values, annotations);
    }

    @Override
    public String getDescription() {
        throw uoe();
    }

    @Override
    public String getReference() {
        throw uoe();
    }

    @Override
    public String getModuleName() {
        throw uoe();
    }

    @Override
    public Optional<YangSourceDefinition> getYangSourceDefinition() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Not available at runtime");
    }
}