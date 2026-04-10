/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.lib;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.slf4j.LoggerFactory;

/**
 * Component parts that go together to provide a correct {@link UnsafeAccess} implementation. The operation of this
 * implementation can be adjusted on a JVM-global basis using the following properties:
 * <ul>
 *   <li>when {@value #VERIFY_STO_PROP} is set to {@code "true"}, all attempts to unsafely instantiate
 *       {@link ScalarTypeObject}s will use the usual safe instantiation</li>
 * </ul>
 *
 * @since 15.1.0
 */
@NonNullByDefault
public record UnsafeAccessSupport(UnsafeAccess access, ScalarTypeObjectRegistrar stoRegistrar) {
    // TODO: project into SpringBoot metadata
    private static final String VERIFY_STO_PROP = "odl.binding.spec.unsafe.verify-sto";

    static final UnsafeSTOFactoryFactory STO_FACTORY_FACTORY;

    static {
        final var log = LoggerFactory.getLogger(UnsafeAccessSupport.class);
        final var verifySTOProp = System.getProperty(VERIFY_STO_PROP);
        final var verifySTO = switch (verifySTOProp) {
            case null -> {
                log.debug("Unsafe ScalarTypeObject instantiation enabled");
                yield false;
            }
            case "false" -> {
                log.info("Unsafe ScalarTypeObject instantiation enabled");
                yield false;
            }
            case "true" -> {
                log.info("Unsafe ScalarTypeObject instantiation disabled");
                yield true;
            }
            default -> {
                log.warn("Unrecognized {} property value '{}', disabling unsafe ScalarTypeObject instantiation",
                    VERIFY_STO_PROP, verifySTOProp);
                yield true;
            }
        };

        STO_FACTORY_FACTORY = verifySTO
            ? SafeSTOFactory.FACTORY
            : UnsafeSTOFactory.FACTORY;
    }

    public UnsafeAccessSupport {
        requireNonNull(access);
        requireNonNull(stoRegistrar);
    }

    /**
     * Construct a new instance tied to a {@link Module} and a root package name of the generated code.
     *
     * @param rootPackageName the root package
     * @param definingModule the defining module
     */
    public static UnsafeAccessSupport of(final String rootPackageName, final Module definingModule) {
        final var state = new UnsafeAccessState(rootPackageName, definingModule);
        return new UnsafeAccessSupport(new DefaultUnsafeAccess(state), new DefaultSTORegistrar(state));
    }
}
