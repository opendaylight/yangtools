/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Collections2;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.slf4j.LoggerFactory;

/**
 * Generates a {@code serialVersionUID} as per
 * <a href="https://docs.oracle.com/en/java/javase/25/docs/specs/serialization/class.html#stream-unique-identifiers">
 * Java Object Serialization Specification, section 4.6</a>.
 *
 * @since 16.0.0
 */
// FIXME: this should live in binding-codegen
@Beta
@NonNullByDefault
public final class SerialVersionHelper {
    private record MethodDesc(String name, AccessModifier accessModifier) {
        MethodDesc {
            requireNonNull(name);
            requireNonNull(accessModifier);
        }
    }

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

    // Backward compatibility: these interfaces were retro-fitted into generated code and would affect the computation
    // FIXME: these should just not be carried in GeneratedType
    private static final Set<ConcreteType> IGNORED_INTERFACES =
        Set.of(BindingTypes.SCALAR_TYPE_OBJECT, BindingTypes.UNION_TYPE_OBJECT);
    private static final Comparator<JavaTypeName> IFACE_COMPARATOR = Comparator.comparing(JavaTypeName::canonicalName);
    private static final Comparator<MethodDesc> METHOD_COMPARATOR = Comparator.comparing(MethodDesc::name);
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

    private final ArrayList<JavaTypeName> interfaces = new ArrayList<>();
    private final ArrayList<MethodDesc> methods = new ArrayList<>();
    private final ArrayList<String> fields = new ArrayList<>();
    private final JavaTypeName clazz;

    private boolean isAbstract = true;

    public SerialVersionHelper(final JavaTypeName clazz) {
        this.clazz = requireNonNull(clazz);
    }

    public SerialVersionHelper setAbstract(final boolean newAbstract) {
        isAbstract = newAbstract;
        return this;
    }

    public SerialVersionHelper addField(final String name) {
        fields.add(requireNonNull(name));
        return this;
    }

    public SerialVersionHelper addInterface(final JavaTypeName name) {
        interfaces.add(requireNonNull(name));
        return this;
    }

    public SerialVersionHelper addMethod(final String name, final AccessModifier accessModifier) {
        if (accessModifier != AccessModifier.PRIVATE) {
            methods.add(new MethodDesc(name, accessModifier));
        }
        return this;
    }

    public long computeSerialVersion() {
        final var baos = new ByteArrayOutputStream();
        try (var dos = new DataOutputStream(baos)) {
            dos.writeUTF(clazz.simpleName());
            // FIXME: explain magic numbers
            dos.writeInt(isAbstract ? 3 : 7);

            interfaces.sort(IFACE_COMPARATOR);
            for (var iface : interfaces) {
                dos.writeUTF(iface.canonicalName());
            }

            fields.sort(Comparator.naturalOrder());
            for (var field : fields) {
                dos.writeUTF(field);
            }

            methods.sort(METHOD_COMPARATOR);
            for (var method : methods) {
                dos.writeUTF(method.name);
                dos.write(method.accessModifier.ordinal());
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

    public static long computeSerialVersion(final GeneratedType to) {
        final var svb = new SerialVersionHelper(to.name()).setAbstract(to.isAbstract());

        for (var iface : Collections2.filter(to.getImplements(), item -> !IGNORED_INTERFACES.contains(item))) {
            svb.addInterface(iface.name());
        }
        for (var property : to.getProperties()) {
            svb.addField(property.getName());
        }
        for (var method : to.getMethodDefinitions()) {
            svb.addMethod(method.getName(), method.getAccessModifier());
        }

        return svb.computeSerialVersion();
    }
}
