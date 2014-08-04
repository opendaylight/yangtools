/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil.getRevisionFormat;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.concepts.Immutable;

/**
 * The QName from XML consists of local name of element and XML namespace, but
 * for our use, we added module revision to it.
 *
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
 * QName may also have <code>prefix</code> assigned, but prefix does not
 * affect equality and identity of two QNames and carry only information
 * which may be useful for serializers / deserializers.
 *
 *
 */
public final class QName implements Immutable, Serializable, Comparable<QName> {
    private static final long serialVersionUID = 5398411242927766414L;

    static final String QNAME_REVISION_DELIMITER = "?revision=";
    static final String QNAME_LEFT_PARENTHESIS = "(";
    static final String QNAME_RIGHT_PARENTHESIS = ")";

    private static final Pattern QNAME_PATTERN_FULL = Pattern.compile("^\\((.+)\\" + QNAME_REVISION_DELIMITER
            + "(.+)\\)(.+)$");
    private static final Pattern QNAME_PATTERN_NO_REVISION = Pattern.compile("^\\((.+)\\)(.+)$");
    private static final Pattern QNAME_PATTERN_NO_NAMESPACE_NO_REVISION = Pattern.compile("^(.+)$");

    private static final char[] ILLEGAL_CHARACTERS = new char[] { '?', '(', ')', '&' };

    // Mandatory
    private final QNameModule module;
    // Mandatory
    private final String localName;
    // Nullable
    private final String prefix;

    private QName(final QNameModule module, final String prefix, final String localName) {
        this.localName = checkLocalName(localName);
        this.prefix = prefix;
        this.module = module;
    }

    /**
     * QName Constructor.
     *
     * @param namespace
     *            the namespace assigned to the YANG module
     * @param revision
     *            the revision of the YANG module
     * @param prefix
     *            locally defined prefix assigned to local name
     * @param localName
     *            YANG schema identifier
     *
     * @deprecated Prefix storage in QNames is deprecated.
     */
    @Deprecated
    public QName(final URI namespace, final Date revision, final String prefix, final String localName) {
        this(QNameModule.create(namespace, revision), prefix, localName);
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
        this(namespace, null, "", localName);
    }

    private static String checkLocalName(final String localName) {
        if (localName == null) {
            throw new IllegalArgumentException("Parameter 'localName' may not be null.");
        }
        if (localName.length() == 0) {
            throw new IllegalArgumentException("Parameter 'localName' must be a non-empty string.");
        }

        for (char c : ILLEGAL_CHARACTERS) {
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
            String namespace = matcher.group(1);
            String revision = matcher.group(2);
            String localName = matcher.group(3);
            return create(namespace, revision, localName);
        }
        matcher = QNAME_PATTERN_NO_REVISION.matcher(input);
        if (matcher.matches()) {
            URI namespace = URI.create(matcher.group(1));
            String localName = matcher.group(2);
            return new QName(namespace, localName);
        }
        matcher = QNAME_PATTERN_NO_NAMESPACE_NO_REVISION.matcher(input);
        if (matcher.matches()) {
            String localName = matcher.group(1);
            return new QName((URI) null, localName);
        }
        throw new IllegalArgumentException("Invalid input:" + input);
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
     * YANG module
     *
     * @return YANG schema identifier which were defined for this node in the
     *         YANG module
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Returns revision of the YANG module if the module has defined revision,
     * otherwise returns <code>null</code>
     *
     * @return revision of the YANG module if the module has defined revision,
     *         otherwise returns <code>null</code>
     */
    public Date getRevision() {
        return module.getRevision();
    }

    /**
     * Returns locally defined prefix assigned to local name
     *
     * @return locally defined prefix assigned to local name
     *
     * @deprecated Prefix storage in QNames is deprecated.
     */
    @Deprecated
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localName == null) ? 0 : localName.hashCode());
        result = prime * result + module.hashCode();
        return result;
    }

    /**
     *
     * Compares the specified object with this list for equality.  Returns
     * <tt>true</tt> if and only if the specified object is also instance of
     * {@link QName} and its {@link #getLocalName()}, {@link #getNamespace()} and
     * {@link #getRevision()} are equals to same properties of this instance.
     *
     * @param o the object to be compared for equality with this QName
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
        if (localName == null) {
            if (other.localName != null) {
                return false;
            }
        } else if (!localName.equals(other.localName)) {
            return false;
        }
        return module.equals(other.module);
    }

    public static QName create(final QName base, final String localName) {
        return new QName(base.getModule(), base.getPrefix(), localName);
    }

    /**
     * Creates new QName.
     *
     * @param qnameModule
     *            Namespace and revision enclosed as a QNameModule
     * @param prefix
     *            Namespace prefix
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return Instance of QName
     *
     * @deprecated Prefix storage in QNames is deprecated.
     */
    @Deprecated
    public static QName create(final QNameModule module, final String prefix, final String localName) {
        if (module == null) {
            throw new NullPointerException("module may not be null");
        }
        return new QName(module, prefix, localName);
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
        return new QName(qnameModule, null, localName);
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
        return new QName(QNameModule.create(namespace, revision), null, localName);
    }

    /**
     *
     * Creates new QName.
     *
     * @param namespace
     *            Namespace of QName, MUST NOT BE Null.
     * @param revision
     *            Revision of namespace / YANG module. MUST NOT BE null, MUST BE
     *            in format <code>YYYY-mm-dd</code>.
     * @param localName
     *            Local name part of QName. MUST NOT BE null.
     * @return
     * @throws NullPointerException
     *             If any of paramaters is null.
     * @throws IllegalArgumentException
     *             If <code>namespace</code> is not valid URI or
     *             <code>revision</code> is not according to format
     *             <code>YYYY-mm-dd</code>.
     */
    public static QName create(final String namespace, final String revision, final String localName)
            throws IllegalArgumentException {
        final URI namespaceUri;
        try {
            namespaceUri = new URI(namespace);
        } catch (URISyntaxException ue) {
            throw new IllegalArgumentException(String.format("Namespace '%s' is not a valid URI", namespace), ue);
        }

        Date revisionDate = parseRevision(revision);
        return create(namespaceUri, revisionDate, localName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (getNamespace() != null) {
            sb.append(QNAME_LEFT_PARENTHESIS + getNamespace());

            if (getFormattedRevision() != null) {
                sb.append(QNAME_REVISION_DELIMITER + getFormattedRevision());
            }
            sb.append(QNAME_RIGHT_PARENTHESIS);
        }
        sb.append(localName);
        return sb.toString();
    }

    /**
     * Return string representation of revision in format
     * <code>YYYY-mm-dd</code>
     *
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
        return QName.create(getNamespace(), null, localName);
    }

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
     *
     * Compares this QName to other, without comparing revision.
     *
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
    public int compareTo(final QName other) {
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
