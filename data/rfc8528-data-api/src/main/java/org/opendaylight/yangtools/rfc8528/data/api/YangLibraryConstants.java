/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.data.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants related to {@code ietf-yang-library.yang}. As schema-mount works in concert with yang-library, we need
 * these constants to interpret correctly categorize incoming data and present them to schema resolution process.
 *
 * <p>
 * While RFC7895 and RFC8525 are not strictly required by YANG, RFC7950 contains a weak reference to it when dealing
 * with capability negotiation on protocol layers. Moreover RFC8528 makes it explicit that an instance of yang-library
 * is mounted underneath both {@code inline} and {@code shared-schema} types of mount.
 *
 * <p>
 * While we could mandate use of either RFC7895 or RFC8525 across the board, this is not feasible, as mount points may
 * be nested and point to external systems -- hence it is completely possible to encounter both old and new information
 * in a single mount point tree.
 */
@Beta
@NonNullByDefault
public final class YangLibraryConstants {
    /**
     * The namespace assigned to {@code ietf-yang-library}. This constant is required for XML-like parsers, using
     * XML namespaces to reference modules.
     */
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-library");
    /**
     * The module name assigned to {@code ietf-yang-library}. This constant is required for JSON-like parsers, using
     * module names to reference modules.
     */
    public static final String MODULE_NAME = "ietf-yang-library";

    /**
     * Top-level containers which hold YANG Library information, ordered by descending preference, with more modern
     * and/or preferred entries first.
     */
    public enum ContainerName {
        // Note: order this enum from most-preferred to least-preferred name
        /**
         * Container in RFC8525 (NMDA) YANG Library.
         */
        RFC8525("yang-library"),
        /**
         * Container in RFC7895 (pre-NMDA) YANG Library.
         */
        RFC7895("modules-state");

        private final String localName;

        ContainerName(final String localName) {
            this.localName = requireNonNull(localName);
        }

        public String getLocalName() {
            return localName;
        }

        public static ContainerName ofLocalName(final String localName) {
            final var ret = forLocalName(localName);
            if (ret == null) {
                throw new IllegalArgumentException("Unrecognized container name '" + localName + "'");
            }
            return ret;
        }

        public static @Nullable ContainerName forLocalName(final String localName) {
            return switch (localName) {
                case "yang-library" -> RFC8525;
                case "modules-state" -> RFC7895;
                default -> null;
            };
        }
    }

    private YangLibraryConstants() {
        // Hidden
    }
}
