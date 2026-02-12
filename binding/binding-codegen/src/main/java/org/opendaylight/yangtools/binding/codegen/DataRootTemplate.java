/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for {@link DataRoot} specializations.
 */
final class DataRootTemplate extends InterfaceTemplate {
    DataRootTemplate(final GeneratedType genType) {
        super(genType);
    }

    @Override
    CharSequence generateConstants() {
        if (consts.size() != 1) {
            throw new VerifyException("Unexpected constants " + consts);
        }

        final var meta = consts.getFirst();
        if (!meta.getName().equals(Naming.META_STATIC_FIELD_NAME)) {
            throw new VerifyException("Unexpected constant " + meta);
        }
        if (!(meta.getValue() instanceof JavaTypeName infoProviderName)) {
            throw new VerifyException("Malformed constant " + meta);
        }

        // pre-compute constants: split out for future isolation
        final var nonNullByDefault = importedName(NONNULL_BY_DEFAULT);
        final var rootMetaType = BindingTypes.rootMeta(type());
        final var rootMetaRaw = importedName(rootMetaType.getRawType());
        final var rootMeta = importedName(rootMetaType);
        final var infoProvider = importedName(infoProviderName);
        final var type = importedName(type());

        return new StringBuilder("""
            /**
             * YANG identifier of the statement represented by this class.
             */
            """).append(nonNullByDefault).append('\n')
            .append(rootMeta).append(" META = new ").append(rootMetaRaw).append("<>(")
            .append(type).append(".class, ").append(infoProvider).append(");\n")
            .toString();
    }
}
