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
 * <li><b>XMLNamespace</b> - the namespace assigned to the YANG module which
 * defined element, type, procedure or notification.</li>
 * <li><b>Revision</b> - the revision of the YANG module which describes the
 * element</li>
 * <li><b>LocalName</b> - the YANG schema identifier which were defined for this
 * node in the YANG module</li>
 * </ul>
 *
 *
 */
public final class QName implements Immutable, Serializable, Comparable<QName> {
    private static final long serialVersionUID = 5398411242927766414L;

    static final String QNAME_REVISION_DELIMITER = "?revision=";
    static final String QNAME_LEFT_PARENTHESIS = "(";
    static final String QNAME_RIGHT_PARENTHESIS = ")";

    private static final Pattern QNAME_PATTERN_FULL = Pattern.compile(
            "^\\((.+)\\" + QNAME_REVISION_DELIMITER + "(.+)\\)(.+)$");
    private static final Pattern QNAME_PATTERN_NO_REVISION = Pattern.compile(
           "^\\((.+)\\)(.+)$");
    private static final Pattern QNAME_PATTERN_NO_NAMESPACE_NO_REVISION = Pattern.compile(
            "^(.+)$");

    private static final char[] ILLEGAL_CHARACTERS = new char[] {'?', '(', ')', '&'};

    //Mandatory
    private final QNameModule module;
    //Mandatory
    private final String localName;
    //Nullable
    private final String prefix;

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
     */
    public QName(final URI namespace, final Date revision, final String prefix, final String localName) {
        this.localName = checkLocalName(localName);
        this.prefix = prefix;
        this.module = QNameModule.create(namespace, revision);
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

        for (char c: ILLEGAL_CHARACTERS) {
            if (localName.indexOf(c) != -1) {
                throw new IllegalArgumentException(String.format(
                        "Parameter 'localName':'%s' contains illegal character '%s'",
                        localName, c));
            }
        }
        return localName;
    }

    /**
     * QName Constructor.
     *
     * @param namespace
     *            the namespace assigned to the YANG module
     * @param revision
     *            the revision of the YANG module
     * @param localName
     *            YANG schema identifier
     */
    public QName(final URI namespace, final Date revision, final String localName) {
        this(namespace, revision, null, localName);
    }

    public QName(final QName base, final String localName) {
        this(base.getNamespace(), base.getRevision(), base.getPrefix(), localName);
    }

    /**
     * @deprecated Use {@link #create(String)} instead.
     * This implementation is broken.
     */
    @Deprecated
    public QName(final String input) throws ParseException {
        final String nsAndRev = input.substring(input.indexOf("(") + 1, input.indexOf(")"));
        final Date revision;
        final URI namespace;
        if (nsAndRev.contains("?")) {
            String[] splitted = nsAndRev.split("\\?");
            namespace = URI.create(splitted[0]);
            revision = getRevisionFormat().parse(splitted[1]);
        } else {
            namespace = URI.create(nsAndRev);
            revision = null;
        }

        this.localName = checkLocalName(input.substring(input.indexOf(")") + 1));
        this.prefix = null;
        this.module = QNameModule.create(namespace, revision);
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
            return new QName((URI)null, localName);
        }
        throw new IllegalArgumentException("Invalid input:" + input);
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
     */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((localName == null) ? 0 : localName.hashCode());
        result = prime * result + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
        result = prime * result + ((getFormattedRevision() == null) ? 0 : getFormattedRevision().hashCode());
        return result;
    }

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
        if (getNamespace() == null) {
            if (other.getNamespace() != null) {
                return false;
            }
        } else if (!getNamespace().equals(other.getNamespace())) {
            return false;
        }
        if (getFormattedRevision() == null) {
            if (other.getFormattedRevision() != null) {
                return false;
            }
        } else if (!getRevision().equals(other.getRevision())) {
            return false;
        }
        return true;
    }

    public static QName create(final QName base, final String localName){
        return new QName(base, localName);
    }

    public static QName create(final URI namespace, final Date revision, final String localName){
        return new QName(namespace, revision, localName);
    }


    public static QName create(final String namespace, final String revision, final String localName) throws IllegalArgumentException{
        final URI namespaceUri;
        try {
            namespaceUri = new URI(namespace);
        }  catch (URISyntaxException ue) {
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

    public String getFormattedRevision() {
        return module.getFormattedRevision();
    }

    public QName withoutRevision() {
        return QName.create(getNamespace(), null, localName);
    }

    public static Date parseRevision(final String formatedDate) {
        try {
            return getRevisionFormat().parse(formatedDate);
        } catch (ParseException| RuntimeException e) {
            throw new IllegalArgumentException(String.format("Revision '%s'is not in a supported format", formatedDate), e);
        }
    }

    public static String formattedRevision(final Date revision) {
        if(revision == null) {
            return null;
        }
        return getRevisionFormat().format(revision);
    }

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
