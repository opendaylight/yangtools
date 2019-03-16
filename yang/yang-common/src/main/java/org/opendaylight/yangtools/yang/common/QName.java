/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.RegEx;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.WritableObject;

/**
 * The QName from XML consists of local name of element and XML namespace, but
 * for our use, we added module revision to it.
 *
 * <p>
 * In YANG context QName is full name of defined node, type, procedure or
 * notification. QName consists of XML namespace, YANG model revision and local
 * name of defined type. It is used to prevent name clashes between nodes with
 * same local name, but from different schemas.
 *
 * <ul>
 * <li><b>XMLNamespace</b> - {@link #getNamespace()} - the namespace assigned to the YANG module which
 * defined element, type, procedure or notification.</li>
 * <li><b>Revision</b> - {@link #getRevision()} - the revision of the YANG module which describes the
 * element</li>
 * <li><b>LocalName</b> - {@link #getLocalName()} - the YANG schema identifier which were defined for this
 * node in the YANG module</li>
 * </ul>
 *
 * <p>
 * QName may also have <code>prefix</code> assigned, but prefix does not
 * affect equality and identity of two QNames and carry only information
 * which may be useful for serializers / deserializers.
 */
public final class QName implements Immutable, Serializable, Comparable<QName>, Identifier, WritableObject {
    private static final Interner<QName> INTERNER = Interners.newWeakInterner();
    private static final long serialVersionUID = 5398411242927766414L;

    static final String QNAME_REVISION_DELIMITER = "?revision=";
    static final String QNAME_LEFT_PARENTHESIS = "(";
    static final String QNAME_RIGHT_PARENTHESIS = ")";

    @RegEx
    private static final String QNAME_STRING_FULL = "^\\((.+)\\?revision=(.+)\\)(.+)$";
    private static final Pattern QNAME_PATTERN_FULL = Pattern.compile(QNAME_STRING_FULL);

    @RegEx
    private static final String QNAME_STRING_NO_REVISION = "^\\((.+)\\)(.+)$";
    private static final Pattern QNAME_PATTERN_NO_REVISION = Pattern.compile(QNAME_STRING_NO_REVISION);

    private static final char[] ILLEGAL_CHARACTERS = { '?', '(', ')', '&', ':' };

    private final @NonNull QNameModule module;
    private final @NonNull String localName;
    private transient int hash = 0;

    private QName(final QNameModule module, final @NonNull String localName) {
        this.module = requireNonNull(module);
        this.localName = requireNonNull(localName);
    }

    /**
     * QName Constructor.
     *
     * @param namespace
     *            the namespace assigned to the YANG module
     * @param localName
     *            YANG schema identifier
     */
    private QName(final URI namespace, final String localName) {
        this(QNameModule.create(namespace), checkLocalName(localName));
    }

    private static @NonNull String checkLocalName(final String localName) {
        checkArgument(localName != null, "Parameter 'localName' may not be null.");
        checkArgument(!localName.isEmpty(), "Parameter 'localName' must be a non-empty string.");

        for (final char c : ILLEGAL_CHARACTERS) {
            if (localName.indexOf(c) != -1) {
                throw new IllegalArgumentException("Parameter 'localName':'" + localName
                    + "' contains illegal character '" + c + "'");
            }
        }
        return localName;
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
            final URI namespace = URI.create(matcher.group(1));
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
     * @param qnameModule
     *            Namespace and revision enclosed as a QNameModule
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
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
    public static @NonNull QName create(final URI namespace, final @Nullable Revision revision,
            final String localName) {
        return create(QNameModule.create(namespace, revision), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace Namespace of QName or null if namespace is undefined.
     * @param revision Revision of namespace.
     * @param localName Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     */
    public static @NonNull QName create(final URI namespace, final Optional<Revision> revision,
            final String localName) {
        return create(QNameModule.create(namespace, revision), localName);
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
        return create(QNameModule.create(parseNamespace(namespace), revision), localName);
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
        return create(parseNamespace(namespace), Revision.of(revision), localName);
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
        return create(parseNamespace(namespace), localName);
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
    public static @NonNull QName create(final URI namespace, final String localName) {
        return new QName(namespace, localName);
    }

    /**
     * Read a QName from a DataInput. The format is expected to match the output format of {@link #writeTo(DataOutput)}.
     *
     * @param in DataInput to read
     * @return A QName instance
     * @throws IOException if I/O error occurs
     */
    public static QName readFrom(final DataInput in) throws IOException {
        final QNameModule module = QNameModule.readFrom(in);
        return new QName(module, checkLocalName(in.readUTF()));
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
    public @NonNull URI getNamespace() {
        return module.getNamespace();
    }

    /**
     * Returns YANG schema identifier which were defined for this node in the
     * YANG module.
     *
     * @return YANG schema identifier which were defined for this node in the
     *         YANG module
     */
    public @NonNull String getLocalName() {
        return localName;
    }

    /**
     * Returns revision of the YANG module if the module has defined revision.
     *
     * @return revision of the YANG module if the module has defined revision.
     */
    public @NonNull Optional<Revision> getRevision() {
        return module.getRevision();
    }

    /**
     * Return an interned reference to a equivalent QName.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public @NonNull QName intern() {
        // We also want to make sure we keep the QNameModule cached
        final QNameModule cacheMod = module.intern();

        // Identity comparison is here on purpose, as we are deciding whether to potentially store 'qname' into the
        // interner. It is important that it does not hold user-supplied reference (such a String instance from
        // parsing of an XML document).
        final QName template = cacheMod == module ? this : QName.create(cacheMod, localName.intern());

        return INTERNER.intern(template);
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            hash = Objects.hash(module, localName);
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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QName)) {
            return false;
        }
        final QName other = (QName) obj;
        return Objects.equals(localName, other.localName) && module.equals(other.module);
    }

    private static @NonNull URI parseNamespace(final String namespace) {
        try {
            return new URI(namespace);
        } catch (final URISyntaxException ue) {
            throw new IllegalArgumentException("Namespace '" + namespace + "' is not a valid URI", ue);
        }
    }

    @Override
    public @NonNull String toString() {
        final StringBuilder sb = new StringBuilder();
        if (getNamespace() != null) {
            sb.append(QNAME_LEFT_PARENTHESIS).append(getNamespace());

            final Optional<Revision> rev = getRevision();
            if (rev.isPresent()) {
                sb.append(QNAME_REVISION_DELIMITER).append(rev.get());
            }
            sb.append(QNAME_RIGHT_PARENTHESIS);
        }
        sb.append(localName);
        return sb.toString();
    }

    /**
     * Returns a QName with the specified QNameModule and the same localname as this one.
     *
     * @param newModule New QNameModule to use
     * @return a QName with specified QNameModule and same local name as this one
     */
    public @NonNull QName withModule(final QNameModule newModule) {
        return new QName(newModule, localName);
    }

    /**
     * Returns a QName with the same namespace and local name, but with no revision. If this QName does not have
     * a Revision, this object is returned.
     *
     * @return a QName with the same namespace and local name, but with no revision.
     */
    public @NonNull QName withoutRevision() {
        final QNameModule newModule;
        return (newModule = module.withoutRevision()) == module ? this : new QName(newModule, localName);
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
        return localName.equals(other.getLocalName()) && Objects.equals(getNamespace(), other.getNamespace());
    }

    // FIXME: this comparison function looks odd. We are sorting first by local name and then by module? What is
    //        the impact on iteration order of SortedMap<QName, ?>?
    @Override
    @SuppressWarnings("checkstyle:parameterName")
    public int compareTo(final QName o) {
        // compare mandatory localName parameter
        int result = localName.compareTo(o.localName);
        if (result != 0) {
            return result;
        }
        return module.compareTo(o.module);
    }

    @Override
    public void writeTo(final DataOutput out) throws IOException {
        module.writeTo(out);
        out.writeUTF(localName);
    }
}
