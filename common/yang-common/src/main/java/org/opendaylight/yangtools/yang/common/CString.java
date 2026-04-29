/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link VString} that can produce a the canonical representation from some internal representation. This is useful
 * to hold YANG-modeled data in the following scenario:
 * <pre>
 *     typedef foo {
 *         type string;
 *         pattern "[1-9]?[0-9]";
 *     }
 *
 *     typedef bar {
 *         type foo;
 *         patter "[1-9][0-9]";
 *     }
 *
 *     typedef baz {
 *         type foo;
 *     }
 * </pre>
 * it is obvious we could use a storage class with 'int' as the internal representation of all three types and define
 * operations on top of it. In this case we would define:
 * <ul>
 *     <li>{@code public class FooCString implements CString}, which implements all abstract methods of {@link CString}
 *          as final methods. It will notably not override {@link #validator()} and must not be final.</li>
 *     <li>{@code public final class FooCStringSupport extends CanonicalValueSupport<FooCString>}, which forms
 *         the baseline validator and instantiation for {@code FooDerivedString}. It should be a singleton class
 *         with a getInstance() method.</li>
 *     <li>{@code public class BarCString extends FooCString}, which overrides {@link #validator()} to indicate its
 *         contents have been validated to conform to bar -- it does that by returning the singleton instance of
 *         {@code BarCStringValidator}.</li>
 *     <li>{@code public final class BarCStringValidator extends CanonicalValueValidator<FooCString, BarCString}. This
 *         method needs to notably implement {@link CanonicalValueValidator#validateRepresentation(CanonicalValue)}
 *         to hand out BarDerivedString instances. This class needs to be a singleton with a getInstance() method, too.
 *     </li>
 * </ul>
 * Since {@code baz} is not defining any new restrictions, all instances of FooCString are valid for it and we do not
 * have to define any additional support.
 *
 * <p>It is important for {@link CString} subclasses not to be final because any YANG type can be further extended
 * and adding a final class in that hierarchy would prevent a proper class from being defined.
 *
 * <p>Also note that {@link #hashCode()} needs to be consistent with {@code toCanonicalString().hashCode()} and
 * {@link #equals(Object)} needs to treat all {@link Stringly} types as equal.
 */
@Beta
@NonNullByDefault
public non-sealed interface CString extends CanonicalValue<CString>, VString {
    @Override
    default String toValidString() {
        return toCanonicalString();
    }

    @Override
    default int compareTo(final CString o) {
        return toCanonicalString().compareTo(toCanonicalString());
    }
}
