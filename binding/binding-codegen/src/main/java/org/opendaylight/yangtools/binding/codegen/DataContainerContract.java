/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.CLASS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BindingContract;
import org.opendaylight.yangtools.binding.lib.JavaContract;

/**
 * Enumeration of how a template interacts with {@link BindingContract#implementedInterface()}.
 */
@NonNullByDefault
enum DataContainerContract {
    /**
     * Do not generate a method, like {@link GroupingTemplate}.
     */
    NONE {
        @Override
        BlockFragment implementationIn(final InterfaceTemplate<?> template) {
            return bb -> {
                // no-op
            };
        }
    },
    /**
     * Generate an abstract method with narrowed return type, like {@link NotificationBodyTemplate}.
     */
    NARROW {
        @Override
        BlockFragment implementationIn(final InterfaceTemplate<?> template) {
            return bb -> bb
                .nl()
                .at().eol(template.importedName(OVERRIDE))
                // FIXME: use selfRef instead of canonical name
                .str(template.importedName(CLASS)).str("<? extends ").str(template.archetype.canonicalName())
                    .eol("> implementedInterface();");
        }
    },
    /**
     * Generate a default method narrowed to exactly the generated type and return its class, like
     * {@link DataRootTemplate}.
     */
    // TODO: reconsider usefulness of this
    BINDING {
        @Override
        BlockFragment implementationIn(final InterfaceTemplate<?> template) {
            return bb -> bb
                .nl()
                .frg(new ImplementedInterfaceMethod.Canonical(template));
        }
    },
    /**
     * Same as {@value #BINDING}, but also generate default methods implementing the {@link JavaContract}, like
     * {@link ContainerObjectTemplate}.
     */
    JAVA {
        @Override
        BlockFragment implementationIn(final InterfaceTemplate<?> template) {
            return bb -> bb
                .nl()
                .frg(new ImplementedInterfaceMethod.Canonical(template))
                .nl()
                .frg(new JavaDataContainerMethods(template.javaType(), template.getters, template.augmentable));
        }
    };

    abstract BlockFragment implementationIn(InterfaceTemplate<?> template);
}
