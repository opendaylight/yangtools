/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentSyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

/**
 *
 */
public abstract sealed class ArgumentSupport<A> implements Immutable {

    public abstract static sealed class WithDeclared<A> extends ArgumentSupport<A> {
        private final @NonNull ArgumentDefinition<A> definition;

        WithDeclared(final ArgumentDefinition<A> definition) {
            this.definition = requireNonNull(definition);
        }

        public final @NonNull ArgumentDefinition<A> definition() {
            return definition;
        }

        /**
         * Given a raw string representation of an argument, try to use a shared representation. Default implementation
         * does nothing.
         *
         * @param rawArgument Argument string
         * @return A potentially-shard instance
         */
        @NonNullByDefault
        public String internRawArgument(final String rawArgument) {
            return rawArgument;
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper
                .add("argumentName", definition.humanName())
                .add("representation", definition.declaredRepresentation());
        }
    }

    public abstract static sealed class WithoutDeclared<A> extends ArgumentSupport<A> {
        // Nothing else
    }

    public abstract static non-sealed class Explicit<A> extends WithDeclared<A> {
        protected Explicit(final ArgumentDefinition<A> definition) {
            super(definition);
        }

        @NonNullByDefault
        public abstract A parseArgument(CommonStmtCtx stmt, String rawArgument);
    }

    public abstract static non-sealed class ExplicitInNamespace<A> extends WithDeclared<A> implements InNamespace<A> {
        protected ExplicitInNamespace(final ArgumentDefinition<A> definition) {
            super(definition);
        }

        /**
         * Parses textual representation of argument in object representation.
         *
         * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
         * @param value String representation of value, as was present in text source.
         * @return Parsed value
         * @throws SourceException when an inconsistency is detected.
         */
        @NonNullByDefault
        public abstract A parseArgument(CommonStmtCtx stmt, IdentifierBinding identifierBinding, String rawArgument);
    }

    public abstract static non-sealed class Implicit<A> extends WithoutDeclared<A> {

        @NonNullByDefault
        public abstract A provideArgument(CommonStmtCtx stmt);
    }

    public abstract static non-sealed class ImplicitInNamespace<A> extends WithoutDeclared<A>
            implements InNamespace<A> {

        @NonNullByDefault
        public abstract A provideArgument(CommonStmtCtx stmt, IdentifierBinding identifierBinding);
    }

    public sealed interface InNamespace<A> permits ExplicitInNamespace, ImplicitInNamespace, QNameInNamespace {
        /**
         * Adapts the argument value to match a new module. Default implementation returns original value stored in context,
         * which is appropriate for most implementations.
         *
         * @param ctx Context, which may be used to access source-specific namespaces required for parsing.
         * @param targetModule Target module, may not be null.
         * @return Adapted argument value.
         */
        @NonNullByDefault
        default A adaptArgument(final IdentifierBinding newIdentifierBinding, final A argument) {
            return argument;
        }
    }

    private sealed interface QNameInNamespace extends InNamespace<QName> {
        @Override
        default QName adaptArgument(final IdentifierBinding newIdentifierBinding, final QName argument) {
            return argument.bindTo(newIdentifierBinding.currentModule()).intern();
        }
    }

    private static final class NoArgument extends Implicit<Empty> {
        static final @NonNull NoArgument INSTANCE = new NoArgument();

        private NoArgument() {
            // Hidden on purpose
        }

        @Override
        public Empty provideArgument(final CommonStmtCtx stmt) {
            return Empty.value();
        }
    }

    private static final class StringAsBoolean extends Explicit<Boolean> {
        StringAsBoolean(final ArgumentDefinition<Boolean> definition) {
            super(definition);
        }

        @Override
        public String internRawArgument(final String rawArgument) {
            return switch (rawArgument) {
                case "false" -> "false";
                case "true" -> "true";
                default -> rawArgument;
            };
        }

        @Override
        public Boolean parseArgument(final CommonStmtCtx stmt, final String rawArgument) {
            return switch (rawArgument) {
                case "false" -> Boolean.FALSE;
                case "true" -> Boolean.TRUE;
                default -> throw new SourceException(stmt,
                    "Invalid '%s' statement %s '%s', it can be either 'true' or 'false'",
                    stmt.publicDefinition().humanName(), definition().humanName(), rawArgument);
            };
        }
    }

    private static final class StringAsString extends Explicit<String> {
        StringAsString(final ArgumentDefinition<String> definition) {
            super(definition);
        }

        @Override
        public String parseArgument(final CommonStmtCtx stmt, final String rawArgument) {
            return rawArgument;
        }
    }

    private static final class IdentifierArgAsQName extends ExplicitInNamespace<QName> implements QNameInNamespace {
        IdentifierArgAsQName(final ArgumentDefinition<QName> definition) {
            super(definition);
        }

        @Override
        public QName parseArgument(final CommonStmtCtx stmt, final IdentifierBinding identifierBinding,
                final String rawArgument) {
            try {
                return identifierBinding.identifier.parseArgument(rawArgument);
            } catch (ArgumentSyntaxException e) {
                throw new SourceException(
                    IdentifierBinding.formatMessage(stmt.publicDefinition().humanName(), definition(), rawArgument, e),
                    stmt, e);
            }
        }
    }

    private static final class UnqualifiedAsQName extends ImplicitInNamespace<QName> implements QNameInNamespace {
        private final @NonNull Unqualified unqualified;

        UnqualifiedAsQName(final Unqualified unqualified) {
            this.unqualified = requireNonNull(unqualified);
        }

        @Override
        public QName provideArgument(final CommonStmtCtx stmt, final IdentifierBinding identifierBinding) {
            return unqualified.bindTo(identifierBinding.currentModule()).intern();
        }

        @Override
        protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
            return helper.add("unqualified", unqualified);
        }
    }

    public static @NonNull Implicit<Empty> noArgument() {
        return NoArgument.INSTANCE;
    }

    public static @NonNull ImplicitInNamespace<QName> implicitIdentifierArg(final Unqualified unqualified) {
        return new UnqualifiedAsQName(unqualified);
    }

    public static @NonNull ExplicitInNamespace<QName> explicitIdentifierArg(
            final ArgumentDefinition<QName> definition) {
        return new IdentifierArgAsQName(definition);
    }

    public static @NonNull Explicit<String> explicitString(final @NonNull ArgumentDefinition<String> definition) {
        return new StringAsString(definition);
    }

    public static @NonNull Explicit<Boolean> explicitBoolean(final @NonNull ArgumentDefinition<Boolean> definition) {
        return new StringAsBoolean(definition);
    }

    @Override
    public String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    @NonNullByDefault
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper;
    }
}
