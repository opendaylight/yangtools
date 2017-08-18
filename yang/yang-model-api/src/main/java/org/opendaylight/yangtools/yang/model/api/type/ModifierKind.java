/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api.type;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Enum describing the effect of a YANG modifier statement.
 *
 * <p>
 * As of YANG 1.1 (RFC7950) there is only one modifier value available and that
 * is "invert-match". If there are more possible values added in the future,
 * this enum can be extended with more enum constants.
 */
public enum ModifierKind {
    INVERT_MATCH("invert-match");

    private static final Map<String, ModifierKind> MODIFIER_KIND_MAP = Maps.uniqueIndex(
        Arrays.asList(ModifierKind.values()), ModifierKind::getKeyword);

    private final String keyword;

    ModifierKind(final String keyword) {
        this.keyword = requireNonNull(keyword);
    }

    /**
     * YANG keyword of this modifier.
     *
     * @return String that corresponds to the YANG keyword.
     */
    public @Nonnull String getKeyword() {
        return keyword;
    }

    /**
     * Returns ModifierKind based on supplied Yang keyword.
     *
     * @param keyword
     *            Yang keyword in string form
     * @return ModifierKind based on supplied YANG keyword
     * @throws NullPointerException if keyword is null
     */
    public static Optional<ModifierKind> parse(final String keyword) {
        return Optional.ofNullable(MODIFIER_KIND_MAP.get(requireNonNull(keyword)));
    }
}
