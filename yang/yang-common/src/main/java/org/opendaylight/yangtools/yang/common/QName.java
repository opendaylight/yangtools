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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(QName.class);

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

    //Nullable
    private final URI namespace;
    //Mandatory
    private final String localName;
    //Nullable
    private final String prefix;
    //Nullable
    private final String formattedRevision;
    //Nullable
    private final Date revision;

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
        this.namespace = namespace;
        this.revision = revision;
        this.prefix = prefix;
        if(revision != null) {
            this.formattedRevision = getRevisionFormat().format(revision);
        } else {
            this.formattedRevision = null;
        }
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
        Date revision = null;
        String nsAndRev = input.substring(input.indexOf("(") + 1, input.indexOf(")"));
        if (nsAndRev.contains("?")) {
            String[] splitted = nsAndRev.split("\\?");
            this.namespace = URI.create(splitted[0]);
            revision = getRevisionFormat().parse(splitted[1]);
        } else {
            this.namespace = URI.create(nsAndRev);
        }

        this.localName = checkLocalName(input.substring(input.indexOf(")") + 1));
        this.revision = revision;
        this.prefix = null;
        if (revision != null) {
            this.formattedRevision = getRevisionFormat().format(revision);
        } else {
            this.formattedRevision = null;
        }
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
        return namespace;
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
        return revision;
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
        result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
        result = prime * result + ((formattedRevision == null) ? 0 : formattedRevision.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        QName other = (QName) obj;
        if (localName == null) {
            if (other.localName != null) {
                return false;
            }
        } else if (!localName.equals(other.localName)) {
            return false;
        }
        if (namespace == null) {
            if (other.namespace != null) {
                return false;
            }
        } else if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (formattedRevision == null) {
            if (other.formattedRevision != null) {
                return false;
            }
        } else if (!revision.equals(other.revision)) {
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
        try {
            URI namespaceUri = new URI(namespace);
            Date revisionDate = parseRevision(revision);
            return create(namespaceUri, revisionDate, localName);
        }  catch (URISyntaxException ue) {
            throw new IllegalArgumentException("Namespace is is not valid URI", ue);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (namespace != null) {
            sb.append(QNAME_LEFT_PARENTHESIS + namespace);

            if (formattedRevision != null) {
                sb.append(QNAME_REVISION_DELIMITER + formattedRevision);
            }
            sb.append(QNAME_RIGHT_PARENTHESIS);
        }
        sb.append(localName);
        return sb.toString();
    }

    /**
     * Returns a namespace in form defined by section 5.6.4. of {@link https
     * ://tools.ietf.org/html/rfc6020}, if namespace is not correctly defined,
     * the method will return <code>null</code> <br>
     * example "http://example.acme.com/system?revision=2008-04-01"
     *
     * @return namespace in form defined by section 5.6.4. of {@link https
     *         ://tools.ietf.org/html/rfc6020}, if namespace is not correctly
     *         defined, the method will return <code>null</code>
     *
     */
    URI getRevisionNamespace() {

        if (namespace == null) {
            return null;
        }

        String query = "";
        if (revision != null) {
            query = "revision=" + formattedRevision;
        }

        URI compositeURI = null;
        try {
            compositeURI = new URI(namespace.getScheme(), namespace.getUserInfo(), namespace.getHost(),
                    namespace.getPort(), namespace.getPath(), query, namespace.getFragment());
        } catch (URISyntaxException e) {
            LOGGER.error("", e);
        }
        return compositeURI;
    }

    public String getFormattedRevision() {
        return formattedRevision;
    }

    public QName withoutRevision() {
        return QName.create(namespace, null, localName);
    }

    public static Date parseRevision(final String formatedDate) {
        try {
            return getRevisionFormat().parse(formatedDate);
        } catch (ParseException| RuntimeException e) {
            throw new IllegalArgumentException("Revision is not in supported format:" + formatedDate,e);
        }
    }

    public static String formattedRevision(final Date revision) {
        if(revision == null) {
            return null;
        }
        return getRevisionFormat().format(revision);
    }

    public boolean isEqualWithoutRevision(final QName other) {
        return localName.equals(other.getLocalName()) && Objects.equals(namespace, other.getNamespace());
    }

    @Override
    public int compareTo(final QName other) {
        // compare mandatory localName parameter
        int result = localName.compareTo(other.localName);
        if (result != 0) {
            return result;
        }

        // compare nullable namespace parameter
        if (namespace == null) {
            if (other.namespace != null) {
                return -1;
            }
        } else {
            if (other.namespace == null) {
                return 1;
            }
            result = namespace.compareTo(other.namespace);
            if (result != 0) {
                return result;
            }
        }

        // compare nullable revision parameter
        if (revision == null) {
            if (other.revision != null) {
                return -1;
            }
        } else {
            if (other.revision == null) {
                return 1;
            }
            result = revision.compareTo(other.revision);
            if (result != 0) {
                return result;
            }
        }

        return result;
    }

}
