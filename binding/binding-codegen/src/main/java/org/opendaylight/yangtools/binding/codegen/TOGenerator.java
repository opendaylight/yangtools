/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
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
        final ClassTemplate template;
        if (type.isTypedef()) {
            template = new ClassTemplate(type);
        } else {
            final var featureDataRoot = BindingTypes.extractYangFeatureDataRoot(type);
            template = featureDataRoot == null ? new ListKeyTemplate(type) : new FeatureTemplate(type, featureDataRoot);
        }

        return template.generate();
    }
}
