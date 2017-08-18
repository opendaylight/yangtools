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
import static org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil.getRevisionFormat;

import com.google.common.base.Strings;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.RegEx;
import org.opendaylight.yangtools.concepts.Immutable;

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
public final class QName implements Immutable, Serializable, Comparable<QName> {
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

    @RegEx
    private static final String QNAME_STRING_NO_NAMESPACE_NO_REVISION = "^(.+)$";
    private static final Pattern QNAME_PATTERN_NO_NAMESPACE_NO_REVISION =
        Pattern.compile(QNAME_STRING_NO_NAMESPACE_NO_REVISION);

    private static final char[] ILLEGAL_CHARACTERS = new char[] { '?', '(', ')', '&', ':' };

    // Non-null
    private final QNameModule module;
    // Non-null
    private final String localName;
    private transient int hash;

    private QName(final QNameModule module, final String localName) {
        this.localName = checkLocalName(localName);
        this.module = module;
    }

    /**
     * QName Constructor.
     *
     * @param namespace
     *            the namespace assigned to the YANG module
     * @param localName
     *            YANG schema identifier
     */
    public QName(final URI namespace, final String localName) {
        this(QNameModule.create(namespace, null), localName);
    }

    private static String checkLocalName(final String localName) {
        checkArgument(localName != null, "Parameter 'localName' may not be null.");
        checkArgument(!Strings.isNullOrEmpty(localName), "Parameter 'localName' must be a non-empty string.");

        for (final char c : ILLEGAL_CHARACTERS) {
            if (localName.indexOf(c) != -1) {
                throw new IllegalArgumentException(String.format(
                        "Parameter 'localName':'%s' contains illegal character '%s'", localName, c));
            }
        }
        return localName;
    }

    public static QName create(final String input) {
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
        matcher = QNAME_PATTERN_NO_NAMESPACE_NO_REVISION.matcher(input);
        if (matcher.matches()) {
            final String localName = matcher.group(1);
            return new QName((URI) null, localName);
        }
        throw new IllegalArgumentException("Invalid input:" + input);
    }

    public static QName create(final QName base, final String localName) {
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
    public static QName create(final QNameModule qnameModule, final String localName) {
        return new QName(requireNonNull(qnameModule,"module may not be null"), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace
     *            Namespace of QName or null if namespace is undefined.
     * @param revision
     *            Revision of namespace or null if revision is unspecified.
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     */
    public static QName create(final URI namespace, final Date revision, final String localName) {
        return create(QNameModule.create(namespace, revision), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace
     *            Namespace of QName or null if namespace is undefined.
     * @param revision
     *            Revision of namespace or null if revision is unspecified.
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     */
    public static QName create(final String namespace, final String localName, final Date revision) {
        final URI namespaceUri = parseNamespace(namespace);
        return create(QNameModule.create(namespaceUri, revision), localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace
     *            Namespace of QName, MUST NOT BE Null.
     * @param revision
     *            Revision of namespace / YANG module. MUST NOT BE null, MUST BE
     *            in format <code>YYYY-mm-dd</code>.
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return A new QName
     * @throws NullPointerException
     *             If any of parameters is null.
     * @throws IllegalArgumentException
     *             If <code>namespace</code> is not valid URI or
     *             <code>revision</code> is not according to format
     *             <code>YYYY-mm-dd</code>.
     */
    public static QName create(final String namespace, final String revision, final String localName) {
        final URI namespaceUri = parseNamespace(namespace);
        final Date revisionDate = parseRevision(revision);
        return create(namespaceUri, revisionDate, localName);
    }

    /**
     * Creates new QName.
     *
     * @param namespace
     *            Namespace of QName, MUST NOT BE Null.
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return A new QName
     * @throws NullPointerException
     *             If any of parameters is null.
     * @throws IllegalArgumentException
     *             If <code>namespace</code> is not valid URI.
     */
    public static QName create(final String namespace, final String localName) {
        return create(parseNamespace(namespace), null, localName);
    }

    /**
     * Get the module component of the QName.
     *
     * @return Module component
     */
    public QNameModule getModule() {
        return module;
    }

    /**
     * Returns XMLNamespace assigned to the YANG module.
     *
     * @return XMLNamespace assigned to the YANG module.
     */
    public URI getNamespace() {
        return module.getNamespace();
    }

    /**
     * Returns YANG schema identifier which were defined for this node in the
     * YANG module.
     *
     * @return YANG schema identifier which were defined for this node in the
     *         YANG module
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns revision of the YANG module if the module has defined revision,
     * otherwise returns <code>null</code>.
     *
     * @return revision of the YANG module if the module has defined revision,
     *         otherwise returns <code>null</code>
     */
    public Date getRevision() {
        return module.getRevision();
    }

    /**
     * Return an interned reference to a equivalent QName.
     *
     * @return Interned reference, or this object if it was interned.
     */
    public QName intern() {
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
     * Compares the specified object with this list for equality.  Returns
     * <tt>true</tt> if and only if the specified object is also instance of
     * {@link QName} and its {@link #getLocalName()}, {@link #getNamespace()} and
     * {@link #getRevision()} are equals to same properties of this instance.
     *
     * @param obj the object to be compared for equality with this QName
     * @return <tt>true</tt> if the specified object is equal to this QName
     *
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

    private static URI parseNamespace(final String namespace) {
        try {
            return new URI(namespace);
        } catch (final URISyntaxException ue) {
            throw new IllegalArgumentException(String.format("Namespace '%s' is not a valid URI", namespace), ue);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (getNamespace() != null) {
            sb.append(QNAME_LEFT_PARENTHESIS).append(getNamespace());

            if (getFormattedRevision() != null) {
                sb.append(QNAME_REVISION_DELIMITER).append(getFormattedRevision());
            }
            sb.append(QNAME_RIGHT_PARENTHESIS);
        }
        sb.append(localName);
        return sb.toString();
    }

    /**
     * Return string representation of revision in format <code>YYYY-mm-dd</code>
     *
     * <p>
     * YANG Specification defines format for <code>revision</code> as
     * YYYY-mm-dd. This format for revision is reused accross multiple places
     * such as capabilities URI, YANG modules, etc.
     *
     * @return String representation of revision or null, if revision is not
     *         set.
     */
    public String getFormattedRevision() {
        return module.getFormattedRevision();
    }

    /**
     * Creates copy of this with revision and prefix unset.
     *
     * @return copy of this QName with revision and prefix unset.
     */
    public QName withoutRevision() {
        return create(getNamespace(), null, localName);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    public static Date parseRevision(final String formatedDate) {
        try {
            return getRevisionFormat().parse(formatedDate);
        } catch (ParseException | RuntimeException e) {
            throw new IllegalArgumentException(
                    String.format("Revision '%s'is not in a supported format", formatedDate), e);
        }
    }

    /**
     * Formats {@link Date} representing revision to format
     * <code>YYYY-mm-dd</code>
     *
     * <p>
     * YANG Specification defines format for <code>revision</code> as
     * YYYY-mm-dd. This format for revision is reused accross multiple places
     * such as capabilities URI, YANG modules, etc.
     *
     * @param revision
     *            Date object to format or null
     * @return String representation or null if the input was null.
     */
    public static String formattedRevision(final Date revision) {
        if (revision == null) {
            return null;
        }
        return getRevisionFormat().format(revision);
    }

    /**
     * Compares this QName to other, without comparing revision.
     *
     * <p>
     * Compares instance of this to other instance of QName and returns true if
     * both instances have equal <code>localName</code> ({@link #getLocalName()}
     * ) and <code>namespace</code> ({@link #getNamespace()}).
     *
     * @param other
     *            Other QName. Must not be null.
     * @return true if this instance and other have equals localName and
     *         namespace.
     * @throws NullPointerException
     *             if <code>other</code> is null.
     */
    public boolean isEqualWithoutRevision(final QName other) {
        return localName.equals(other.getLocalName()) && Objects.equals(getNamespace(), other.getNamespace());
    }

    @Override
    public int compareTo(@Nonnull final QName other) {
        // compare mandatory localName parameter
        int result = localName.compareTo(other.localName);
        if (result != 0) {
            return result;
        }

        // compare nullable namespace parameter
        if (getNamespace() == null) {
            if (other.getNamespace() != null) {
                return -1;
            }
        } else {
            if (other.getNamespace() == null) {
                return 1;
            }
            result = getNamespace().compareTo(other.getNamespace());
            if (result != 0) {
                return result;
            }
        }

        // compare nullable revision parameter
        if (getRevision() == null) {
            if (other.getRevision() != null) {
                return -1;
            }
        } else {
            if (other.getRevision() == null) {
                return 1;
            }
            result = getRevision().compareTo(other.getRevision());
            if (result != 0) {
                return result;
            }
        }

        return result;
    }

}
