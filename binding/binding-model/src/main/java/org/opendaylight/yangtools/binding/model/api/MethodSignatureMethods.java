/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods for {@link MethodSignature} implementations.
 */
@NonNullByDefault
final class MethodSignatureMethods {
    private MethodSignatureMethods() {
        // hidden on purpose
    }

    public static int hashCode(final MethodSignature self) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(self.getName());
        result = prime * result + Objects.hashCode(self.getParameters());
        result = prime * result + Objects.hashCode(self.getReturnType());
        return result;
    }

    public static boolean equals(final MethodSignature self, final @Nullable Object obj) {
        return requireNonNull(self) == obj || obj instanceof MethodSignature other
            && self.getName().equals(other.getName()) && self.getParameters().equals(other.getParameters())
            && self.getReturnType().equals(other.getReturnType());
    }

    public static String toString(final MethodSignature self) {
        final var helper = MoreObjects.toStringHelper(MethodSignature.class).omitNullValues()
            .add("name", self.getName())
            .add("comment", self.getComment())
            .add("returnType", self.getReturnType());
        final var params = self.getParameters();
        if (!params.isEmpty()) {
            helper.add("params", params);
        }
        final var annotations = self.getAnnotations();
        if (!params.isEmpty()) {
            helper.add("annotations", annotations);
        }
        return helper.toString();
    }
}
