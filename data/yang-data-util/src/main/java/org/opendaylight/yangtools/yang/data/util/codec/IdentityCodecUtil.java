/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;

/**
 * Utility methods for implementing string-to-identity codecs.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public final class IdentityCodecUtil {
    private IdentityCodecUtil() {
        // Hidden on purpose
    }

    /**
     * Parse a string into a QName using specified prefix-to-QNameModule mapping function, interpreting the result
     * as an IdentitySchemaNode existing in specified SchemaContext.
     *
     * @param value string value to parse
     * @param schemaContext Parent schema context
     * @param prefixToModule prefix-to-QNameModule mapping function
     * @return Corresponding IdentitySchemaNode.
     * @throws IllegalArgumentException if the value is invalid or does not refer to an existing identity
     */
    public static IdentitySchemaNode parseIdentity(final String value, final EffectiveModelContext schemaContext,
            final Function<String, QNameModule> prefixToModule) {
        final var qname = QNameCodecUtil.decodeQName(value, prefixToModule);
        final var module = schemaContext.findModule(qname.getModule())
            .orElseThrow(() -> new IllegalStateException("Parsed QName " + qname + " refers to a non-existent module"));

        for (var identity : module.getIdentities()) {
            if (qname.equals(identity.getQName())) {
                return identity;
            }
        }

        throw new IllegalArgumentException("Parsed QName " + qname + " does not refer to a valid identity");
    }
}
