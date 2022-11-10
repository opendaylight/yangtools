/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import com.google.common.collect.Collections2;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.AccessModifier;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.mdsal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.mdsal.binding.model.api.type.builder.TypeMemberBuilder;
import org.opendaylight.mdsal.binding.model.ri.BindingTypes;

public final class SerialVersionHelper {
    private static final Set<ConcreteType> IGNORED_INTERFACES =
        Set.of(BindingTypes.TYPE_OBJECT, BindingTypes.SCALAR_TYPE_OBJECT);
    private static final Comparator<TypeMemberBuilder<?>> SUID_MEMBER_COMPARATOR =
        Comparator.comparing(TypeMemberBuilder::getName);
    private static final Comparator<Type> SUID_NAME_COMPARATOR = Comparator.comparing(Type::getFullyQualifiedName);
    private static final ThreadLocal<MessageDigest> SHA1_MD = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA");
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to get a SHA digest provider", e);
        }
    });

    private SerialVersionHelper() {
        // Hidden on purpose
    }

    public static long computeDefaultSUID(final GeneratedTypeBuilderBase<?> to) {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try (DataOutputStream dout = new DataOutputStream(bout)) {
            dout.writeUTF(to.getName());
            dout.writeInt(to.isAbstract() ? 3 : 7);

            for (final Type ifc : sortedCollection(SUID_NAME_COMPARATOR, filteredImplementsTypes(to))) {
                dout.writeUTF(ifc.getFullyQualifiedName());
            }

            for (final GeneratedPropertyBuilder gp : sortedCollection(SUID_MEMBER_COMPARATOR, to.getProperties())) {
                dout.writeUTF(gp.getName());
            }

            for (final MethodSignatureBuilder m : sortedCollection(SUID_MEMBER_COMPARATOR, to.getMethodDefinitions())) {
                if (!m.getAccessModifier().equals(AccessModifier.PRIVATE)) {
                    dout.writeUTF(m.getName());
                    dout.write(m.getAccessModifier().ordinal());
                }
            }

            dout.flush();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to hash object " + to, e);
        }

        final byte[] hashBytes = SHA1_MD.get().digest(bout.toByteArray());
        long hash = 0;
        for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
            hash = hash << 8 | hashBytes[i] & 0xFF;
        }
        return hash;
    }

    private static Collection<Type> filteredImplementsTypes(final GeneratedTypeBuilderBase<?> to) {
        return Collections2.filter(to.getImplementsTypes(), item -> !IGNORED_INTERFACES.contains(item));
    }

    private static <T> Iterable<T> sortedCollection(final Comparator<? super T> comparator, final Collection<T> input) {
        if (input.size() <= 1) {
            return input;
        }

        final var ret = new ArrayList<>(input);
        ret.sort(comparator);
        return ret;
    }
}
