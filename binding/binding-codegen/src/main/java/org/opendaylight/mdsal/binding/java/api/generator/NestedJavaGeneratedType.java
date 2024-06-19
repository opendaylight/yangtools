/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;

/**
 * A type which is nested inside some other type. It defers import decisions to its enclosing type, eventually arriving
 * at a {@link TopLevelJavaGeneratedType}.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class NestedJavaGeneratedType extends AbstractJavaGeneratedType {
    private final AbstractJavaGeneratedType enclosingType;

    NestedJavaGeneratedType(final AbstractJavaGeneratedType enclosingType, final GeneratedType genType) {
        super(genType);
        this.enclosingType = requireNonNull(enclosingType);
    }

    @Override
    boolean importCheckedType(final JavaTypeName type) {
        // Defer to enclosing type, which needs to re-run its checks
        return enclosingType.checkAndImportType(type);
    }

    @Override
    String localTypeName(final JavaTypeName type) {
        // Check if the type is a reference to our immediately-enclosing type
        if (enclosingType.getName().equals(type)) {
            return enclosingType.getSimpleName();
        }

        final @Nullable List<String> descendant = findDescandantPath(type);
        if (descendant == null) {
            // The type is not present in our hierarchy, defer to our immediately-enclosing type, which may be able
            // to find the target.
            return enclosingType.localTypeName(type);
        }

        // Target type is a declared as a enclosed type of us and we have the path where it lurks.
        final Iterator<String> it = descendant.iterator();
        final StringBuilder sb = new StringBuilder().append(it.next());
        while (it.hasNext()) {
            sb.append('.').append(it.next());
        }
        return sb.toString();
    }

    @SuppressFBWarnings(value = "NP_NONNULL_RETURN_VIOLATION",
            justification = "SpotBugs confusion @Nullable vs @NonNullByDefault")
    private @Nullable List<String> findDescandantPath(final JavaTypeName type) {
        Optional<JavaTypeName> optEnclosing = type.immediatelyEnclosingClass();
        verify(optEnclosing.isPresent());

        final Deque<String> queue = new ArrayDeque<>();
        queue.addFirst(type.simpleName());
        while (optEnclosing.isPresent()) {
            final JavaTypeName enclosing = optEnclosing.orElseThrow();
            if (enclosing.equals(getName())) {
                return ImmutableList.copyOf(queue);
            }

            queue.addFirst(enclosing.simpleName());
            optEnclosing = enclosing.immediatelyEnclosingClass();
        }

        return null;
    }
}
