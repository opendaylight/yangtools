/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.Constant;
import org.opendaylight.yangtools.binding.model.api.EnumTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;

/**
 * Java parts of {@link InterfaceTemplate}.
 */
abstract class AbstractInterfaceTemplate extends BaseTemplate {
    /**
     * List of constant instances which are generated as JAVA public static final attributes.
     */
    final List<Constant> consts;

    /**
     * List of method signatures which are generated as method declarations.
     */
    final List<MethodSignature> methods;

    /**
     * List of enumeration which are generated as JAVA enum type.
     */
    final List<EnumTypeObjectArchetype> enums;

    /**
     * List of generated types which are enclosed inside the generated type.
     */
    final List<GeneratedType> enclosedGeneratedTypes;

    @NonNullByDefault
    AbstractInterfaceTemplate(final GeneratedType type) {
        super(type);
        consts = type.getConstantDefinitions();
        methods = type.getMethodDefinitions();
        enums = type.getEnumerations();
        enclosedGeneratedTypes = type.getEnclosedTypes();
    }

    /**
     * {@return the code block containing this type's singleton constant declarations}
     */
    String generateConstants() {
        if (consts.isEmpty()) {
            return "";
        }

        final var sb = new StringBuilder();
        for (var constant : consts) {
            // Pattern constants are emitted separately
            if (!constant.getName().startsWith(TypeConstants.PATTERN_CONSTANT_NAME)) {
                sb.append(emitConstant(constant));
            }
        }
        return sb.toString();
    }

    final String generateDefaultImplementedInterface() {
        // Note: we cannot use importedName() or short name due to shadowing explained in MDSAL-365
        final var fqcn = type().name().canonicalName();

        return '@' + importedName(OVERRIDE) + '\n'
            +  "default " + importedName(CLASS) + '<' + fqcn + "> " + Naming.BINDING_CONTRACT_IMPLEMENTED_INTERFACE_NAME
                + "() {\n"
            +  "    return " + fqcn + ".class;\n"
            +  "}\n";
    }
}
