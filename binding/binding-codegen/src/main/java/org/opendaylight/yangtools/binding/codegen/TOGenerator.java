/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Transformator of the data from the virtual form to JAVA source code. The result source code represents JAVA class.
 * For generating of the source code is used the template written in XTEND language.
 */
@NonNullByDefault
record TOGenerator(GeneratedTransferObject type) implements Generator {
    TOGenerator {
        requireNonNull(type);
    }

    @Override
    public GeneratedTransferObject type() {
        return type;
    }

    @Override
    public String generate() {
        return switch (type) {
            // FIXME: split out into separate generator
            case UnionTypeObjectArchetype union -> new UnionTypeObjectTemplate(union).generate();
            default -> {
                if (type.isTypedef()) {
                    yield new ClassTemplate(type).generate();
                }
                final var featureDataRoot = BindingTypes.extractYangFeatureDataRoot(type);
                yield featureDataRoot == null ? new ListKeyTemplate(type).generate()
                    : new FeatureTemplate(type, featureDataRoot).generate();
            }
        };
    }


    @Override
    public String getUnitName() {
        return type.name().simpleName();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("type", type).toString();
    }
}
