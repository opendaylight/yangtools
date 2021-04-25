/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.checkerframework.checker.regex.qual.Regex;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * An OID, or ObjectIdentifier, as defined by ITU and ISO/IEC.
 */
// TODO: this class could also be Comparable<ObjectIdentifier>
@Beta
public final class ObjectIdentifier implements Identifier, WritableObject {
    private static final long serialVersionUID = 1L;
    @Regex
    private static final String CHECK_OID_REGEX = "^\\d+(\\.\\d+)*$";
    private static final Pattern CHECK_OID_PATTERN = Pattern.compile(CHECK_OID_REGEX);
    private static final Pattern SPLIT_PATTERN = Pattern.compile(".", Pattern.LITERAL);

    private final int[] subIdentifiers;

    private ObjectIdentifier(final int[] subIdentifiers) {
        this.subIdentifiers = subIdentifiers;
    }

    /**
     * Create an {@link ObjectIdentifier} from its integer components. Each sub-identifier is interpreted as an unsigned
     * integer.
     *
     * @param subIdentifiers OID sub-identifiers
     * @return An ObjectIdentifier.
     */
    public static ObjectIdentifier of(final int... subIdentifiers) {
        return new ObjectIdentifier(subIdentifiers.clone());
    }

    /**
     * Create an {@link ObjectIdentifier} from its string representation.
     *
     * @param str String OID representation.
     * @return An ObjectIdentifier.
     */
    public static ObjectIdentifier forString(final String str) {
        return new ObjectIdentifier(parseObjectId(str));
    }

    public int[] getSubIdentifiers() {
        // Always make a defensive copy
        return subIdentifiers.clone();
    }

    public IntStream streamSubIdentifiers() {
        return Arrays.stream(subIdentifiers);
    }

    /**
     * Read an {@link ObjectIdentifier} from a DataInput, performing the inverse of {@link #writeTo(DataOutput)}. For
     * details see {@link WritableObject}.
     *
     * @param in Data input
     * @return Object identifier
     * @throws IOException If an I/O error is reported
     */
    public static ObjectIdentifier readFrom(final DataInput in) throws IOException {
        final int count = in.readUnsignedByte();
        checkArgument(count >= 0 && count <= 128, "Illegal item count %s", count);

        final int[] oid = new int[count];
        for (int index = 0; index < count; ++index) {
            oid[index] = in.readInt();
        }

        return new ObjectIdentifier(oid);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        out.writeByte(subIdentifiers.length);
        for (int i : subIdentifiers) {
            out.writeInt(i);
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(subIdentifiers);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof ObjectIdentifier
                && Arrays.equals(subIdentifiers, ((ObjectIdentifier) obj).subIdentifiers);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(subIdentifiers[0]);
        for (int index = 1; index < subIdentifiers.length; index++) {
            stringBuilder.append('.').append(subIdentifiers[index]);
        }
        return stringBuilder.toString();
    }

    private static int[] parseObjectId(final String objectId) {
        checkArgument(CHECK_OID_PATTERN.matcher(objectId).matches(), "Wrong format for OID: '%s'", objectId);

        final String[] splitOid = SPLIT_PATTERN.split(objectId);
        checkArgument(splitOid.length <= 128, "Object Identifier can have at most 128 sub-identifiers");

        final int[] oid = new int[splitOid.length];
        for (int index = 0; index < splitOid.length; index ++) {
            oid[index] = Uint32.valueOf(splitOid[index]).intValue();
        }
        return oid;
    }
}
