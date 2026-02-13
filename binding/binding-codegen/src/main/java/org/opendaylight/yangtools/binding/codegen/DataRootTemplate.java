/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Template for {@link DataRoot} specializations.
 */
final class DataRootTemplate extends InterfaceTemplate {
    private final @NonNullByDefault ParameterizedType metaType;

    DataRootTemplate(final GeneratedType genType) {
        super(genType);
        metaType = BindingTypes.rootMeta(genType);
    }

    @Override
    CharSequence generateConstants() {
        // extract metadata: these should be just DataRootArchetype's invariants
        if (consts.size() != 1) {
            throw new VerifyException("Unexpected constants " + consts);
        }
        final var constant = consts.getFirst();
        final var name = constant.getName();
        if (!name.equals(Naming.META_STATIC_FIELD_NAME)) {
            throw new VerifyException("Unexpected constant " + constant);
        }
        if (!(constant.getValue() instanceof JavaTypeName infoProviderName)) {
            throw new VerifyException("Malformed constant " + constant);
        }

        // pre-compute constants: split out for future isolation, as these contribute to the file header
        final var nonNullByDefault = importedName(NONNULL_BY_DEFAULT);
        final var meta = importedName(metaType.getRawType());
        final var metaT = importedName(metaType);
        final var type = importedName(type());
        final var infoProvider = importedName(infoProviderName);

        // build a block and format it: this is the best we can do
        return Block.builder().t("""
            /**
             * {@link """).s(meta).s(" associated with this class.").eol()
            .t("""
             */
            @""").s(nonNullByDefault).eol()
            .s(metaT).c(' ').w().s(name).s(" = new ").s(meta).s("<>(").s(type).s(".class, ")
            .s(infoProvider).s(".getInstance().getModuleInfo());")
            .build().appendTo(new StringBuilder()).toString();
    }
}
