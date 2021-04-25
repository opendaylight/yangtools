/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Abstract base class for objects which are string-equivalent to canonical string representation specified
 * in a YANG model. Note that each subclass of {@link DerivedString} defines its own {@link #hashCode()} and
 * {@link #equals(Object)} contracts based on implementation particulars.
 *
 * <p>
 * Given the following YANG snippet:
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
 *     <li>{@code public class FooDerivedString extends DerivedString<FooDerivedString>}, which implements all abstract
 *         methods of {@link DerivedString} as final methods. It will notably not override {@link #validator()} and
 *         must not be final.</li>
 *     <li>{@code public final class FooDerivedStringSupport extends DerivedStringSupport<FooDerivedString>}, which
 *         forms the baseline validator and instantiation for {@code FooDerivedString}. It should be a singleton class
 *         with a getInstance() method.</li>
 *     <li>{@code public class BarDerivedString extends FooDerivedString}, which overrides {@link #validator()} to
 *         indicate its contents have been validated to conform to bar -- it does that by returning the singleton
 *         instance of {@code BarDerivedStringValidator}.</li>
 *     <li>{@code public final class BarDerivedStringValidator extends DerivedStringValidator<FooDerivedString,
 *         BarDerivedString}. This method needs to notably implement
 *         {@link CanonicalValueValidator#validateRepresentation(CanonicalValue)} to hand out BarDerivedString
 *         instances. This class needs to be a singleton with a getInstance() method, too.</li>
 * </ul>
 * Since {@code baz} is not defining any new restrictions, all instances of FooDerivedString are valid for it and we
 * do not have to define any additional support.
 *
 * <p>
 * It is important for {@link DerivedString} subclasses not to be final because any YANG type can be further extended
 * and adding a final class in that hierarchy would prevent a proper class from being defined.
 *
 * @param <T> derived string representation
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public abstract class DerivedString<T extends DerivedString<T>> implements CanonicalValue<T> {
    private static final long serialVersionUID = 1L;

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(@Nullable Object obj);

    @Override
    public final String toString() {
        return toCanonicalString();
    }
}
