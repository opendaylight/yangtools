/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6020_YANG_MODULE;
import static org.opendaylight.yangtools.yang.common.YangConstants.RFC6241_YANG_MODULE;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-B">RFC6241, Appendix B</a>.
 *
 * @param <T> Value type
 */
@Beta
@NonNullByDefault
public abstract class ErrorInfo<T> {
    /**
     * {@code bad-attribute}, {@link #value()} is the name of the attribute.
     */
    public static final QName BAD_ATTRIBUTE_QNAME = QName.create(RFC6241_YANG_MODULE, "bad-attribute").intern();
    /**
     * {@code bad-element}, {@link #value()} is the name of the element.
     */
    public static final QName BAD_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "bad-element").intern();
    /**
     * {@code bad-namespace}, {@link #value()} is the name of the namespace.
     */
    public static final QName BAD_NAMESPACE_QNAME = QName.create(RFC6241_YANG_MODULE, "bad-namespace").intern();
    /**
     * {@code session-id}, {@link #value()} the session identifier, as modeled in {@code SessionIdOrZero}.
     */
    public static final QName SESSION_ID_QNAME = QName.create(RFC6241_YANG_MODULE, "session-id").intern();
    @Deprecated(since = "RFC6241")
    public static final QName ERR_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "err-element").intern();
    @Deprecated(since = "RFC6241")
    public static final QName NOOP_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "noop-element").intern();
    @Deprecated(since = "RFC6241")
    public static final QName OK_ELEMENT_QNAME = QName.create(RFC6241_YANG_MODULE, "ok-element").intern();
    /**
     * {@code missing-choice} as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc6020#section-13.7">RFC6020, section 13.7</a>.
     */
    public static final QName MISSING_CHOICE_QNAME = QName.create(RFC6020_YANG_MODULE, "missing-choice").intern();
    /**
     * {@code non-unique} as defined in
     * <a href="https://datatracker.ietf.org/doc/html/rfc6020#section-13.1">RFC6020, section 13.1</a>.
     */
    public static final QName NON_UNIQUE_QNAME = QName.create(RFC6020_YANG_MODULE, "non-unique").intern();

    private final QName name;
    private final T value;

    protected ErrorInfo(final QName name, final T value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    /**
     * The name of this {@code error-info} element.
     *
     * @return Element name.
     */
    final QName name() {
        return name;
    }

    /**
     * The value of this {@code error-info} element. This may be a simple or a complex type.
     *
     * @return Element value.
     */
    final T value() {
        return value;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this)).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("name", name).add("value", value);
    }
}