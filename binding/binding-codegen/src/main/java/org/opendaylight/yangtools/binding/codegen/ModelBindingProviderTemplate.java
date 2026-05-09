/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.INSTANCE_FIELD_NAME;
import static org.opendaylight.yangtools.binding.codegen.ModuleSupportTemplate.servicePackageName;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 *
 */
@NonNullByDefault
final class ModelBindingProviderTemplate extends Template {
    /**
     * The name of the {@link YangModelBindingProvider} implementation class.
     */
    static final String CLASS_NAME = "YangModelBindingProviderImpl";

    private final ModuleEffectiveStatement module;
    private final JavaTypeName typeName;

    ModelBindingProviderTemplate(final ModuleEffectiveStatement module) {
        this.module = requireNonNull(module);
        typeName = JavaTypeName.create(servicePackageName(module.localQNameModule()), CLASS_NAME);
    }

    @Override
    JavaTypeName typeName() {
        return typeName;
    }

    @Override
    void generateTo(final Appendable out) throws IOException {
        out
            .append("package ").append(typeName.packageName()).append(";\n")
            .append("""

                     import java.lang.Override;
                     import java.util.ServiceLoader;
                     import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
                     import org.opendaylight.yangtools.binding.meta.YangModuleInfo;

                     /**
                     """)
            .append(" * The {@link YangModelBindingProvider} for {@code ").append(module.argument().getLocalName())
                .append("} module. This class should not be used\n")
            .append("""
                      * directly, but rather through {@link ServiceLoader}.
                      */
                     @javax.annotation.processing.Generated("mdsal-binding-generator")
                     """)
            .append("public final class " + CLASS_NAME + " implements YangModelBindingProvider {\n")
            .append("""
                         /**
                          * Construct a new provider.
                          */
                     """)
            .append("    public " + CLASS_NAME + "() {\n")
            .append("""
                             // Nothing else
                         }

                         @Override
                         public YangModuleInfo getModuleInfo() {
                     """)
            .append("        return " + CLASS_NAME + '.' + INSTANCE_FIELD_NAME + ";\n")
            .append("    }\n")
            .append("}\n");
    }
}
