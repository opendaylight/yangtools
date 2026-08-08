/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Comparator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.slf4j.LoggerFactory;

/**
 * Generates a {@code serialVersionUID} as per
 * <a href="https://docs.oracle.com/en/java/javase/25/docs/specs/serialization/class.html#stream-unique-identifiers">
 * Java Object Serialization Specification, section 4.6</a>.
 *
 * @since 16.0.0
 */
@NonNullByDefault
final class SerialVersionHelper {
    private sealed interface DigestFactory {

        MessageDigest newMD();
    }

    private record CloneableFactory(MessageDigest prototype) implements DigestFactory {
        CloneableFactory {
            requireNonNull(prototype);
        }

        @Override
        public MessageDigest newMD() {
            try {
                return (MessageDigest) prototype.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private record FallbackFactory(String algorithm, Provider provider) implements DigestFactory {
        FallbackFactory {
            requireNonNull(algorithm);
            requireNonNull(provider);
        }

        @Override
        public MessageDigest newMD() {
            try {
                return MessageDigest.getInstance(algorithm, provider);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    static final TypeName SERIALIZABLE = TypeName.ofClass(Serializable.class);

    private static final Comparator<TypeName> IFACE_COMPARATOR = Comparator.comparing(TypeName::canonicalName);
    private static final DigestFactory DIGEST_FACTORY;

    static {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError(e);
        }
        DIGEST_FACTORY = chooseFactory(md);
    }

    private static DigestFactory chooseFactory(final MessageDigest md) {
        try {
            md.clone();
        } catch (CloneNotSupportedException e) {
            final var provider = md.getProvider();
            final var log = LoggerFactory.getLogger(SerialVersionHelper.class);
            if (log.isDebugEnabled()) {
                log.warn("SHA-1 provided by {} does not support clone()", provider.getName(), e);
            } else {
                log.warn("SHA-1 provided by {} does not support clone()", provider.getName());
            }
            return new FallbackFactory(md.getAlgorithm(), provider);
        }

        return new CloneableFactory(md);
    }

    private final ArrayList<TypeName> interfaces = new ArrayList<>();
    private final ArrayList<String> methods = new ArrayList<>();
    private final ArrayList<String> fields = new ArrayList<>();
    private final TypeName clazz;

    private boolean isAbstract = true;

    SerialVersionHelper(final TypeName clazz) {
        this.clazz = requireNonNull(clazz);
    }

    SerialVersionHelper setAbstract(final boolean newAbstract) {
        isAbstract = newAbstract;
        return this;
    }

    SerialVersionHelper addField(final String name) {
        fields.add(requireNonNull(name));
        return this;
    }

    SerialVersionHelper addInterface(final TypeName name) {
        interfaces.add(requireNonNull(name));
        return this;
    }

    SerialVersionHelper addMethod(final String name) {
        methods.add(requireNonNull(name));
        return this;
    }

    // At the end of the day, this should match
    // https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/io/ObjectStreamClass.java's
    // computeDefaultSUID(), but it does not. It is a part of the spec now, so
    //
    // Anyway, we want to keep things as compatible as possible, so this may never get rectified.
    long computeSerialVersion() {
        final var baos = new ByteArrayOutputStream();
        try (var dos = new DataOutputStream(baos)) {
            dos.writeUTF(clazz.simpleName());
            // Magic numbers, coming from the original implementation. This should This correspond to
            // "The class modifiers written as a 32-bit integer.", but the values are weird:
            //   3 == Modifier.PUBLIC | Modifier.PRIVATE
            //   7 == Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
            dos.writeInt(isAbstract ? 3 : 7);

            interfaces.sort(IFACE_COMPARATOR);
            for (var iface : interfaces) {
                dos.writeUTF(iface.canonicalName());
            }

            fields.sort(Comparator.naturalOrder());
            for (var field : fields) {
                dos.writeUTF(field);
            }

            methods.sort(Comparator.naturalOrder());
            for (var method : methods) {
                dos.writeUTF(method);
                // Note: the '2' here comes from our legacy AccessModifier being ordered
                // 'DEFAULT, PRIVATE, PUBLIC, PROTECTED' -- and now we only support public
                // Where does it really come from is hard to say, as originally 'PUBLIC' had ordinal() == 1, same as
                // Modifier.PUBLIC, but that was changed in I983cdad79c8779940e75937f97ba0575e753830e, when the ordinal
                // shifted to 2.
                // This code was introduced in commit ebe09fa86aeeb694c758a4f1ea6a152023fde3f7, with a single assertion
                // to compute 9028898643007565383L. That assertions was then changed multiple times:
                // - to -8829501012356283881L in  9943ce49d0b19d00872bd06a0cdc18b9daf4ecde, on or about 13.11.2013
                // - to -8290985055387641395L in I7e5b6e3a8f50daed6799164425a7f394b82fdef5, on or about 22.06.2023
                // - to  1508705866470220657L in I489acc8d0e6e8c126ae0e5eadfdf9acfc3c68f58, on or about 20.06.2024
                // - to -4330476182227230308L in I29a52002f64f595b752dad8dba15a93fd9e789f7, on or about 21.06.2024
                // Tracking these is problematic, as the code has been moved between controller/yangtools/mdsal multiple
                // times.
                dos.write(2);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        final var hashBytes = DIGEST_FACTORY.newMD().digest(baos.toByteArray());
        long hash = 0;
        for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
            hash = hash << 8 | hashBytes[i] & 0xFF;
        }
        return hash;
    }
}
