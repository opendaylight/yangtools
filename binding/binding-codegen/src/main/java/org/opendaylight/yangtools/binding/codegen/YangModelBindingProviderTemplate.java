/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.io.IOException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.meta.YangModelBindingProvider;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * A template for {@link YangModelBindingProvider}.
 */
@NonNullByDefault
final class YangModelBindingProviderTemplate extends Template {
    private final JavaTypeName typeName;

    YangModelBindingProviderTemplate(final DataRootArchetype root) {
        typeName = JavaTypeName.create(YangModuleInfoTemplate.servicePackageName(root.statement().localQNameModule()),
            YangModuleInfoTemplate.MODEL_BINDING_PROVIDER_CLASS_NAME);
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
                     """);
//    +  " * The {@link YangModelBindingProvider} for {@code " + module.getName()
//        + "} module. This class should not be used\n"
//    +  " * directly, but rather through {@link ServiceLoader}.\n"
//    +  " */\n"
//    +  '@' + JavaFileTemplate.GENERATED + "(\"mdsal-binding-generator\")\n"
//    +  "public final class " + MODEL_BINDING_PROVIDER_CLASS_NAME + " implements YangModelBindingProvider {\n"
//    +  """
//            /**
//             * Construct a new provider.
//             */
//        """
//    +  "    public " + MODEL_BINDING_PROVIDER_CLASS_NAME + "() {\n"
//    +  "        // Nothing else\n"
//    +  "    }\n"
//    +  '\n'
//    +  "    @Override\n"
//    +  "    public YangModuleInfo getModuleInfo() {\n"
//    +  "        return " + CLASS_NAME + '.' + INSTANCE_FIELD_NAME + ";\n"
//    +  "    }\n"
//    +  "}\n";
    }
}
