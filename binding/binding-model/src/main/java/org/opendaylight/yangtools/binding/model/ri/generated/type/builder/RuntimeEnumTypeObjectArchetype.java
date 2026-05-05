/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.AnnotationType;
import org.opendaylight.yangtools.binding.model.api.EnumTypeValue;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

@NonNullByDefault
final class RuntimeEnumTypeObjectArchetype extends AbstractEnumTypeObjectArchetype {
    RuntimeEnumTypeObjectArchetype(final JavaTypeName name, final List<EnumTypeValue> values,
            final List<AnnotationType> annotations) {
        super(name, values, annotations);
    }
}
