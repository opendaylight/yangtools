/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

@Beta
public abstract class ArgumentDefinition implements Immutable {
    private static final class YinAttribute extends ArgumentDefinition {
        YinAttribute(final QName argumentName) {
            super(argumentName);
        }

        @Override
        public boolean isYinElement() {
            return false;
        }
    }

    private static final class YinElement extends ArgumentDefinition {
        YinElement(final QName argumentName) {
            super(argumentName);
        }

        @Override
        public boolean isYinElement() {
            return true;
        }
    }

    private final @NonNull QName argumentName;

    ArgumentDefinition(final QName argumentName) {
        this.argumentName = requireNonNull(argumentName);
    }

    public static @NonNull ArgumentDefinition of(final QName argumentName, final boolean yinElement) {
        return yinElement ? new YinElement(argumentName) : new YinAttribute(argumentName);
    }

    public static @NonNull Optional<ArgumentDefinition> ofNullable(final @Nullable QName argumentName,
            final boolean yinElement) {
        return argumentName == null ? Optional.empty() : Optional.of(of(argumentName, yinElement));
    }


    public final @NonNull QName getArgumentName() {
        return argumentName;
    }

    /**
     * Returns true, if argument of statement is represented as value of YIN element. If argument of statement is
     * represented as argument of YIN element, returns false.
     *
     * @return returns true, if statement argument is represented as value of YIN element, otherwise returns false.
     */
    public abstract boolean isYinElement();

    @Override
    public final int hashCode() {
        return Boolean.hashCode(isYinElement()) * 31 + argumentName.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ArgumentDefinition)) {
            return false;
        }
        final ArgumentDefinition other = (ArgumentDefinition) obj;
        return isYinElement() == other.isYinElement() && argumentName.equals(other.argumentName);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ArgumentDefinition.class).add("argumentName", argumentName)
                .add("yinElement", isYinElement()).toString();
    }
}
