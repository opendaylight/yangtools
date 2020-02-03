/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.data.api.codec.StringCodec;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public class StringStringCodec extends TypeDefinitionAwareCodec<String, StringTypeDefinition>
        implements StringCodec<String> {
    private final LengthConstraint lengthConstraint;

    StringStringCodec(final StringTypeDefinition typeDef) {
        super(requireNonNull(typeDef), String.class);
        lengthConstraint = typeDef.getLengthConstraint().orElse(null);
    }

    public static StringStringCodec from(final StringTypeDefinition normalizedType) {
        return normalizedType.getPatternConstraints().isEmpty() ? new StringStringCodec(normalizedType)
                : new StringPatternCheckingCodec(normalizedType);
    }

    @Override
    protected final String deserializeImpl(final String stringRepresentation) {
        validate(stringRepresentation);
        return stringRepresentation;
    }

    @Override
    protected final String serializeImpl(final String data) {
        return data;
    }

    /**
     * Defines allowed chars and ranges for chars according to RFC7950, Section 14.
     * String can contain characters
     * 9(Horizontal tab, %x09), 10(Line feed, %x0A), 13(Carriage return, %x0D)
     * and allowed range is from 32(%x20) to 55295(%xD7FF).
     * Every other character in range 0 to 1114111(%x10FFFF) is disallowed.
     */
    protected boolean isCharAllowedRFC7950(final int ch) {
        if (ch == 9 || ch == 10 || ch == 13 || (ch >= 32 && ch <= 55295)) {
            return true;
        } else {
            return !(ch <= 1114111);
        }
    }

    /**
     * Method checking, whether all characters in string are valid according to RFC7950, Section 14,
     * by calling method isCharAllowedRFC7950, where allowed chars and char ranges are checked.
     */
    protected void checkForbiddenChars(final String str) {
        for (int i = 0; i < str.length(); i++) {
            checkArgument(isCharAllowedRFC7950(str.charAt(i)),
                "String '%s' contains forbidden characters defined in RFC7950(Section 14)", str);
        }
    }

    void validate(final String str) {
        if (lengthConstraint != null) {
            checkArgument(lengthConstraint.getAllowedRanges().contains(str.length()),
                "String '%s' does not match allowed length constraint %s", lengthConstraint);
        }
        checkForbiddenChars(str);
    }
}
