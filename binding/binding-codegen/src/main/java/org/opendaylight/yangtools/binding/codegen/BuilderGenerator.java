/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;

/**
 * Transformator of the data from the virtual form to JAVA programming language. The result source code represent java
 * class. For generation of the source code is used the template written in XTEND language.
 */
@NonNullByDefault
record BuilderGenerator(GeneratedType type) implements Generator {
    BuilderGenerator {
        requireNonNull(type);
    }

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code is generated according to the template
     * source code template which is written in XTEND language.
     */
    @Override
    public String generate() {
        return templateForType(type).generate();
    }

    @Override
    public String getUnitName() {
        return type.simpleName() + Naming.BUILDER_SUFFIX;
    }

    @VisibleForTesting
    static BuilderTemplate templateForType(final GeneratedType type) {
        final var origName = type.name();
        final var builderName = origName.createSibling(origName.simpleName() + Naming.BUILDER_SUFFIX);

        return new BuilderTemplate(new CodegenGeneratedTypeBuilder(builderName)
            .addEnclosingTransferObject(new CodegenGeneratedTOBuilder(
                builderName.createEnclosed(origName.simpleName() + "Impl"))
                .addImplementsType(type)
                .build())
            .build(), type, getKey(type));
    }

    private static GeneratedTransferObject getKey(final GeneratedType type) {
        for (var method : type.getMethodDefinitions()) {
            if (Naming.KEY_AWARE_KEY_NAME.equals(method.getName())) {
                final var keyType = method.getReturnType();
                if (keyType instanceof GeneratedTransferObject gto) {
                    return gto;
                }
                throw new VerifyException("Unexpected key type " + keyType);
            }
        }
        return null;
    }
}
