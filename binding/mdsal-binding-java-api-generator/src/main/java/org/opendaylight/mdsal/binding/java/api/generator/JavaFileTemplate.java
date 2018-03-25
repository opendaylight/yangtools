/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.util.Types;

/**
 * Base Java file template. Contains a non-null type and imports which the generated code refers to.
 */
class JavaFileTemplate {
    // Hidden to well-define operations
    private final Map<String, JavaTypeName> importMap = new HashMap<>();

    protected final GeneratedType type;

    JavaFileTemplate(final GeneratedType type) {
        this.type = requireNonNull(type);
    }

    final GeneratedProperty findProperty(final GeneratedTransferObject gto, final String name) {
        final Optional<GeneratedProperty> optProp = gto.getProperties().stream()
                .filter(prop -> prop.getName().equals(name)).findFirst();
        if (optProp.isPresent()) {
            return optProp.get();
        }

        final GeneratedTransferObject parent = gto.getSuperType();
        return parent != null ? findProperty(parent, name) : null;
    }

    final String generateImportBlock() {
        return importMap.entrySet().stream()
                .filter(e -> isDefaultVisible(e.getValue()))
                .sorted((e1, e2) -> {
                    return e1.getValue().toString().compareTo(e2.getValue().toString());
                })
                .map(e -> "import " + e.getValue() + ";\n")
                .collect(Collectors.joining());
    }

    final String importedName(final Type intype) {
        GeneratorUtil.putTypeIntoImports(type, intype, importMap);
        return GeneratorUtil.getExplicitType(type, intype, importMap);
    }

    final String importedName(final Class<?> cls) {
        return importedName(Types.typeForClass(cls));
    }

    final void addImport(final Class<?> cls) {
        final JavaTypeName name = JavaTypeName.create(cls);
        importMap.put(name.simpleName(), name);
    }

    final void addImports(final JavaFileTemplate from) {
        importMap.putAll(from.importMap);
    }

    // Exposed for BuilderTemplate
    boolean isLocalInnerClass(final JavaTypeName name) {
        final Optional<JavaTypeName> optEnc = name.immediatelyEnclosingClass();
        return optEnc.isPresent() && type.getIdentifier().equals(optEnc.get());
    }

    private boolean isDefaultVisible(final JavaTypeName name) {
        return !hasSamePackage(name) || !isLocalInnerClass(name);
    }

    /**
     * Checks if packages of generated type and imported type is the same
     *
     * @param importedTypePackageName the package name of imported type
     * @return true if the packages are the same false otherwise
     */
    private boolean hasSamePackage(final JavaTypeName name) {
        return type.getPackageName().equals(name.packageName());
    }
}
