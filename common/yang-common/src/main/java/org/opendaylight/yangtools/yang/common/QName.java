/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serial;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The QName from XML consists of local name of element and XML namespace, but for our use, we added module revision to
 * it.
 *
 * <p>
 * In YANG context QName is full name of defined node, type, procedure or notification. QName consists of XML namespace,
 * YANG model revision and local name of defined type. It is used to prevent name clashes between nodes with same local
 * name, but from different schemas.
 *
 * <p>
 * The local name must conform to <a href="https://www.rfc-editor.org/rfc/rfc7950#section-6.2">RFC7950 Section 6.2</a>.
 *
 * <ul>
 * <li><b>XMLNamespace</b> - {@link #getNamespace()} - the namespace assigned to the YANG module which
 * defined element, type, procedure or notification.</li>
 * <li><b>Revision</b> - {@link #getRevision()} - the revision of the YANG module which describes the
 * element</li>
 * <li><b>LocalName</b> - {@link #getLocalName()} - the YANG schema identifier which were defined for this
 * node in the YANG module</li>
 * </ul>
 */
public final class QName extends AbstractQName implements Comparable<QName> {
    private static final Interner<QName> INTERNER = Interners.newWeakInterner();
    // Note: 5398411242927766414L is used for versions < 3.0.0 without writeReplace
    @Serial
    private static final long serialVersionUID = 1L;

    @Regex
    private static final String QNAME_STRING_FULL = "^\\((.+)\\?revision=(.+)\\)(.+)$";
    private static final Pattern QNAME_PATTERN_FULL = Pattern.compile(QNAME_STRING_FULL);

    @Regex
    private static final String QNAME_STRING_NO_REVISION = "^\\((.+)\\)(.+)$";
    private static final Pattern QNAME_PATTERN_NO_REVISION = Pattern.compile(QNAME_STRING_NO_REVISION);

    private final @NonNull QNameModule module;
    private transient int hash = 0;

    QName(final QNameModule module, final @NonNull String localName) {
        super(localName);
        this.module = requireNonNull(module);
    }

    /**
     * QName Constructor.
     *
     * @param namespace
     *            the namespace assigned to the YANG module
     * @param localName
     *            YANG schema identifier
     */
    private QName(final XMLNamespace namespace, final String localName) {
        this(QNameModule.of(namespace), checkLocalName(localName));
    }

    public static @NonNull QName create(final String input) {
        Matcher matcher = QNAME_PATTERN_FULL.matcher(input);
        if (matcher.matches()) {
            final String namespace = matcher.group(1);
            final String revision = matcher.group(2);
            final String localName = matcher.group(3);
            return create(namespace, revision, localName);
        }
        matcher = QNAME_PATTERN_NO_REVISION.matcher(input);
        if (matcher.matches()) {
            final XMLNamespace namespace = XMLNamespace.of(matcher.group(1));
            final String localName = matcher.group(2);
            return new QName(namespace, localName);
        }
        throw new IllegalArgumentException("Invalid input: " + input);
    }

    public static @NonNull QName create(final QName base, final String localName) {
        return create(base.getModule(), localName);
    }

    /**
     * Creates new QName.
     *
     * @param qnameModule Namespace and revision enclosed as a QNameModule
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     * @throws NullPointerException if any argument is null
     * @throws IllegalArgumentException if localName is not a valid YANG identifier
     */
    public static @NonNull QName create(final QNameModule qnameModule, final String localName) {
        return new QName(requireNonNull(qnameModule, "module may not be null"), checkLocalName(localName));
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName or null if namespace is undefined.
     * @param revision Revision of namespace or null if revision is unspecified.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     */
    public static @NonNull QName create(final XMLNamespace namespace, final @Nullable Revision revision,
            final String localName) {
        return create(QNameModule.ofRevision(namespace, revision), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName or null if namespace is undefined.
     * @param revision Revision of namespace.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     */
    public static @NonNull QName create(final XMLNamespace namespace, final Optional<Revision> revision,
            final String localName) {
        return create(QNameModule.ofRevision(namespace, revision.orElse(null)), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName or null if namespace is undefined.
     * @param revision Revision of namespace or null if revision is unspecified.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     */
    public static @NonNull QName create(final String namespace, final String localName, final Revision revision) {
        return create(QNameModule.of(XMLNamespace.of(namespace), revision), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName, MUST NOT BE Null.
     * @param revision Revision of namespace / YANG module. MUST NOT BE null, MUST BE in format {@code YYYY-mm-dd}.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return A new QName
     * @throws NullPointerException If any of parameters is null.
     * @throws IllegalArgumentException If {@code namespace} is not valid URI or {@code revision} does not conform
     *         to {@code YYYY-mm-dd}.
     */
    public static @NonNull QName create(final String namespace, final String revision, final String localName) {
        return create(QNameModule.ofRevision(namespace, revision), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName, MUST NOT BE Null.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return A new QName
     * @throws NullPointerException If any of parameters is null.
     * @throws IllegalArgumentException If {@code namespace} is not valid URI.
     */
    public static @NonNull QName create(final String namespace, final String localName) {
        return create(XMLNamespace.of(namespace), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName, MUST NOT BE null.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return A new QName
     * @throws NullPointerException If any of parameters is null.
     * @throws IllegalArgumentException If <code>namespace</code> is not valid URI.
     */
    public static @NonNull QName create(final XMLNamespace namespace, final String localName) {
        return new QName(namespace, localName);
    }

    /**
     * Read a QName from a DataInput. The format is expected to match the output format of {@link #writeTo(DataOutput)}.
     *
     * @param in DataInput to read
     * @return A QName instance
     * @throws IOException if I/O error occurs
     */
    public static @NonNull QName readFrom(final DataInput in) throws IOException {
        if (in instanceof QNameAwareDataInput aware) {
            return aware.readQName();
        }

        final var module = QNameModule.readFrom(in);
        return new QName(module, checkLocalName(in.readUTF()));
    }

    /**
     * Creates new QName composed of specified module and local name. This method does not perform lexical checking of
     * localName, and it is the caller's responsibility to performs these checks.
     *
     * <p>
     * When in doubt, use {@link #create(QNameModule, String)} instead.
     *
     * @param qnameModule Namespace and revision enclosed as a QNameModule
     * @param localName Local name part of QName, required to have been validated
     * @return Instance of QName
     * @throws NullPointerException if any of the arguments is null
     */
    @Beta
    public static @NonNull QName unsafeOf(final @NonNull QNameModule qnameModule, final @NonNull String localName) {
        return new QName(qnameModule, localName);
    }

    /**
     * Get the module component of the QName.
     *
     * @return Module component
     */
    public @NonNull QNameModule getModule() {
        return module;
    }

    /**
     * Returns XMLNamespace assigned to the YANG module.
     *
     * @return XMLNamespace assigned to the YANG module.
     */
    public @NonNull XMLNamespace getNamespace() {
        return module.namespace();
    }

    /**
     * Returns revision of the YANG module if the module has defined revision.
     *
     * @return revision of the YANG module if the module has defined revision.
     */
    public @NonNull Optional<Revision> getRevision() {
        return module.findRevision();
    }

    @Override
    public @NonNull QName intern() {
        // We also want to make sure we keep the QNameModule cached
        final QNameModule cacheMod = module.intern();

        // Identity comparison is here on purpose, as we are deciding whether to potentially store 'qname' into the
        // interner. It is important that it does not hold user-supplied reference (such a String instance from
        // parsing of an XML document).
        final QName template = cacheMod == module ? this : new QName(cacheMod, getLocalName().intern());

        return INTERNER.intern(template);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(module, getLocalName());
        }
        return hash;
    }

    /**
     * Compares the specified object with this list for equality.  Returns {@code true} if and only if the specified
     * object is also instance of {@link QName} and its {@link #getLocalName()}, {@link #getNamespace()} and
     * {@link #getRevision()} are equals to same properties of this instance.
     *
     * @param obj the object to be compared for equality with this QName
     * @return {@code true} if the specified object is equal to this QName
     */
    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof QName other && getLocalName().equals(other.getLocalName())
            && module.equals(other.module);
    }

    @Override
    public @NonNull String toString() {
        final StringBuilder sb = new StringBuilder().append('(').append(getNamespace());
        final Optional<Revision> rev = getRevision();
        if (rev.isPresent()) {
            sb.append("?revision=").append(rev.orElseThrow());
        }
        return sb.append(')').append(getLocalName()).toString();
    }

    @Override
    public @NonNull QName bindTo(final QNameModule namespace) {
        return module.equals(namespace) ? this : super.bindTo(namespace);
    }

    /**
     * Returns a QName with the same namespace and local name, but with no revision. If this QName does not have
     * a Revision, this object is returned.
     *
     * @return a QName with the same namespace and local name, but with no revision.
     */
    public @NonNull QName withoutRevision() {
        final QNameModule newModule;
        return (newModule = module.withoutRevision()) == module ? this : new QName(newModule, getLocalName());
    }

    /**
     * Formats {@link Revision} representing revision to format {@code YYYY-mm-dd}
     *
     * <p>
     * YANG Specification defines format for {@code revision<} as YYYY-mm-dd. This format for revision is reused across
     * multiple places such as capabilities URI, YANG modules, etc.
     *
     * @param revision Date object to format
     * @return String representation or null if the input was null.
     */
    public static @Nullable String formattedRevision(final Optional<Revision> revision) {
        return revision.map(Revision::toString).orElse(null);
    }

    /**
     * Compares this QName to other, without comparing revision.
     *
     * <p>
     * Compares instance of this to other instance of QName and returns true if both instances have equal
     * {@code localName} ({@link #getLocalName()}) and @{code namespace} ({@link #getNamespace()}).
     *
     * @param other Other QName. Must not be null.
     * @return true if this instance and other have equals localName and namespace.
     * @throws NullPointerException if {@code other} is null.
     */
    public boolean isEqualWithoutRevision(final QName other) {
        return getLocalName().equals(other.getLocalName()) && Objects.equals(getNamespace(), other.getNamespace());
    }

    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final QName o) {
        final int result = module.compareTo(o.module);
        return result != 0 ? result : getLocalName().compareTo(o.getLocalName());
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        if (out instanceof QNameAwareDataOutput aware) {
            aware.writeQName(this);
        } else {
            module.writeTo(out);
            out.writeUTF(getLocalName());
        }
    }

    @Override
    Object writeReplace() {
        return new QNv1(this);
    }
}
