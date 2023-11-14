/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.contract;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;

/**
 * <a href="https://www.rfc-editor.org/rfc/rfc6020#section-6.2.1">YANG statement namespaces</a> which we process.
 */
public enum StatementNamespace {
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
     * The namespace for {@code augment} statements. These are specific to Java Binding.
     */
    AUGMENT("$AU", true),
    ACTION("$AC", true),
    ANYDATA("$AD", true),
    ANYXML("$AX", true),
    CASE("$CA", true),
    CHOICE("$CH", true),
    CONTAINER("$CO", true),
    INPUT("$IP", true),
    LEAF("$LE", true),
    LIST("$LI", true),
    LEAF_LIST("$LL", true),
    /**
     * The namespace for a {@code list}'s {@code key} statement. This typically does not conflict, but could in case of
     * <pre>{@code
     *   module foo {
     *     list foo { // results Foo
     *       key bar;  // triggers FooKey as a sibling to Foo
     *       leaf bar {
     *         type string;
     *       }
     *     }
     *
     *     container foo-key; // results in FooKey
     *   }
     * }</pre>
     * In this case the key-derived FooKey gets shifted to {@code $KE}.
     */
    KEY("$KE", true),
    NOTIFICATION("$NO", true),
    OUTPUT("$OU", true),
    RPC("$RP", true),
    /**
     * The namespace for a {@code module}'s data root interface. This typically does not conflict, but could in case of
     * <pre>{@code
     *     module foo { // results in FooData
     *       container foo-data; // results in FooData as well
     *     }
     * }</pre>
     * In this case the module-derived FooData gets shifted to {@code $D}.
     */
    DATA_ROOT("$D", true);

    private final @NonNull String suffix;
    private final boolean resistant;

    StatementNamespace(final @NonNull String suffix) {
        this(suffix, false);
    }

    StatementNamespace(final @NonNull String suffix, final boolean resistant) {
        verify(!suffix.isEmpty());
        this.suffix = requireNonNull(suffix);
        this.resistant = resistant;
    }

    public @NonNull String suffix() {
        return suffix;
    }

    public boolean resistant() {
        return resistant;
    }
}
