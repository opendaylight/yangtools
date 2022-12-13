/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import com.google.common.annotations.VisibleForTesting;
import org.opendaylight.mdsal.binding.model.api.CodeGenerator;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.YangData;

/**
 * Transformator of the data from the virtual form to JAVA programming language. The result source code represent java
 * class. For generation of the source code is used the template written in XTEND language.
 */
public final class BuilderGenerator implements CodeGenerator {
    private static final JavaTypeName AUGMENTABLE = JavaTypeName.create(Augmentable.class);
    private static final JavaTypeName AUGMENTATION = JavaTypeName.create(Augmentation.class);
    private static final JavaTypeName YANG_DATA = JavaTypeName.create(YangData.class);

    /**
     * Passes via list of implemented types in <code>type</code>.
     *
     * @param type JAVA <code>Type</code>
     * @return boolean value which is true if any of implemented types is of the type <code>Augmentable</code>.
     */
    @Override
    public boolean isAcceptable(final Type type) {
        if (type instanceof GeneratedType generated && !(type instanceof GeneratedTransferObject)) {
            for (Type t : generated.getImplements()) {
                // "rpc" and "grouping" elements do not implement Augmentable
                final JavaTypeName name = t.getIdentifier();
                if (name.equals(AUGMENTABLE) || name.equals(AUGMENTATION) || name.equals(YANG_DATA)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Generates JAVA source code for generated type <code>Type</code>. The code is generated according to the template
     * source code template which is written in XTEND language.
     */
    @Override
    public String generate(final Type type) {
        if (type instanceof GeneratedType generated && !(type instanceof GeneratedTransferObject)) {
            return templateForType(generated).generate();
        }
        return "";
    }

    @Override
    public String getUnitName(final Type type) {
        return type.getName() + BindingMapping.BUILDER_SUFFIX;
    }

    @VisibleForTesting
    static BuilderTemplate templateForType(final GeneratedType type) {
        final JavaTypeName origName = type.getIdentifier();
        final JavaTypeName builderName = origName.createSibling(origName.simpleName() + BindingMapping.BUILDER_SUFFIX);

        return new BuilderTemplate(new CodegenGeneratedTypeBuilder(builderName)
            .addEnclosingTransferObject(new CodegenGeneratedTOBuilder(
                builderName.createEnclosed(origName.simpleName() + "Impl"))
                .addImplementsType(type)
                .build())
            .build(), type, getKey(type));
    }

    private static Type getKey(final GeneratedType type) {
        for (MethodSignature m : type.getMethodDefinitions()) {
            if (BindingMapping.IDENTIFIABLE_KEY_NAME.equals(m.getName())) {
                return m.getReturnType();
            }
        }
        return null;
    }
}
