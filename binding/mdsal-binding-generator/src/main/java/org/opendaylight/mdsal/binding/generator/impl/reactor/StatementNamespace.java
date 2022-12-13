/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

/**
 * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.2.1">YANG statement namespaces</a> which we process.
 */
// FIXME: move this to 'BindingNamespace' in binding-spec-util
enum StatementNamespace {
    /**
     * The namespace of all {@code feature} statements, bullet 3.
     */
    FEATURE("$F"),
    /**
     * The namespace of all {@code identity} statements, bullet 4.
     */
    IDENTITY("$I"),
    /**
     * The namespace of all {@code typedef} statements, bullet 5.
     */
    TYPEDEF("$T"),
    /**
     * The namespace of all {@code grouping} statements, bullet 6.
     */
    GROUPING("$G"),
    /**
     * The namespace of all RFC8040 {@code ietf-restconf:yang-data} statements. These sit outside of the usual YANG
     * statement namespaces, but may overlap in the usual case when template names conform to YANG {@code identifier}
     * rules.
     */
    YANG_DATA("$YD"),
    /**
     * All other processed statements. Includes {@code augment}, and {@code schema tree} statements.
     */
    // FIXME: peel augment into "$A", which our own thing
    // FIXME: add "$D" to disambiguate <module-name>Data
    // FIXME: add "$L" to disambiguate <module-name>Listener
    // FIXME: add "$S" to disambiguate <module-name>Service
    DEFAULT("");

    private final @NonNull String suffix;

    StatementNamespace(final @NonNull String suffix) {
        this.suffix = requireNonNull(suffix);
    }

    @NonNull String appendSuffix(final String str) {
        return suffix.isEmpty() ? verifyNotNull(str) : str + suffix;
    }
}
