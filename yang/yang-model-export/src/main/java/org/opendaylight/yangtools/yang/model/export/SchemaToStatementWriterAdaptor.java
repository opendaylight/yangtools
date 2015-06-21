/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.primitives.UnsignedInteger;
import java.net.URI;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
@NotThreadSafe
final class SchemaToStatementWriterAdaptor implements Rfc6020ModuleWriter {

    private final StatementTextWriter writer;

    private SchemaToStatementWriterAdaptor(final StatementTextWriter writer) {
        this.writer = Preconditions.checkNotNull(writer);
    }

    public static Rfc6020ModuleWriter from(final StatementTextWriter writer) {
        return new SchemaToStatementWriterAdaptor(writer);
    }

    @Override
    public void endNode() {
        writer.endStatement();
    }

    @Override
    public void startModuleNode(final String identifier) {
        writer.startStatement(Rfc6020Mapping.MODULE);
        writer.writeArgument(identifier);
    }

    @Override
    public void startOrganizationNode(final String input) {
        writer.startStatement(Rfc6020Mapping.ORGANIZATION);
        writer.writeArgument(input);
    }

    @Override
    public void startContactNode(final String input) {
        writer.startStatement(Rfc6020Mapping.CONTACT);
        writer.writeArgument(input);
    }

    @Override
    public void startDescriptionNode(final String input) {
        writer.startStatement(Rfc6020Mapping.DESCRIPTION);
        writer.writeArgument(input);
    }

    @Override
    public void startReferenceNode(final String input) {
        writer.startStatement(Rfc6020Mapping.REFERENCE);
        writer.writeArgument(input);
    }

    @Override
    public void startUnitsNode(final String input) {
        writer.startStatement(Rfc6020Mapping.UNITS);
        writer.writeArgument(input);
    }

    @Override
    public void startYangVersionNode(final String input) {
        writer.startStatement(Rfc6020Mapping.YANG_VERSION);
        writer.writeArgument(input);
    }

    @Override
    public void startNamespaceNode(final URI uri) {
        writer.startStatement(Rfc6020Mapping.NAMESPACE);
        writer.writeArgument(uri.toString());
    }

    @Override
    public void startKeyNode(final List<QName> keyList) {
        writer.startStatement(Rfc6020Mapping.KEY);
        final StringBuilder keyStr = new StringBuilder();
        final Iterator<QName> iter = keyList.iterator();
        while (iter.hasNext()) {
            keyStr.append(iter.next().getLocalName());
            if (iter.hasNext()) {
                keyStr.append(' ');
            }
        }
        writer.writeArgument(keyStr.toString());
    }

    @Override
    public void startPrefixNode(final String input) {
        writer.startStatement(Rfc6020Mapping.PREFIX);
        writer.writeArgument(input);
    }

    @Override
    public void startFeatureNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.FEATURE);
        writer.writeArgument(qName);
    }

    @Override
    public void startExtensionNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.EXTENSION);
        writer.writeArgument(qName);
    }

    @Override
    public void startArgumentNode(final String input) {
        writer.startStatement(Rfc6020Mapping.ARGUMENT);
        writer.writeArgument(input);
    }

    @Override
    public void startStatusNode(final Status status) {
        writer.startStatement(Rfc6020Mapping.STATUS);
        writer.writeArgument(status.toString().toLowerCase());
    }

    @Override
    public void startTypeNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.TYPE);
        writer.writeArgument(qName);
    }

    @Override
    public void startLeafNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.LEAF);
        writer.writeArgument(qName);
    }

    @Override
    public void startContainerNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.CONTAINER);
        writer.writeArgument(qName);
    }

    @Override
    public void startGroupingNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.GROUPING);
        writer.writeArgument(qName);
    }

    @Override
    public void startRpcNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.RPC);
        writer.writeArgument(qName);
    }

    @Override
    public void startInputNode() {
        writer.startStatement(Rfc6020Mapping.INPUT);
    }

    @Override
    public void startOutputNode() {
        writer.startStatement(Rfc6020Mapping.OUTPUT);
    }

    @Override
    public void startLeafListNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.LEAF_LIST);
        writer.writeArgument(qName);
    }

    @Override
    public void startListNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.LIST);
        writer.writeArgument(qName);
    }

    @Override
    public void startChoiceNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.CHOICE);
        writer.writeArgument(qName);
    }

    @Override
    public void startCaseNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.CASE);
        writer.writeArgument(qName);
    }

    @Override
    public void startNotificationNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.NOTIFICATION);
        writer.writeArgument(qName);
    }

    @Override
    public void startIdentityNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.IDENTITY);
        writer.writeArgument(qName);
    }

    @Override
    public void startBaseNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.BASE);
        writer.writeArgument(qName);
    }

    @Override
    public void startTypedefNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.TYPEDEF);
        writer.writeArgument(qName);
    }

    @Override
    public void startRevisionNode(final Date date) {
        writer.startStatement(Rfc6020Mapping.REVISION);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startDefaultNode(final String string) {
        writer.startStatement(Rfc6020Mapping.DEFAULT);
        writer.writeArgument(string);
    }

    @Override
    public void startMustNode(final RevisionAwareXPath xpath) {
        writer.startStatement(Rfc6020Mapping.MUST);
        writer.writeArgument(xpath);
    }

    @Override
    public void startErrorMessageNode(final String input) {
        writer.startStatement(Rfc6020Mapping.ERROR_MESSAGE);
        writer.writeArgument(input);
    }

    @Override
    public void startErrorAppTagNode(final String input) {
        writer.startStatement(Rfc6020Mapping.ERROR_APP_TAG);
        writer.writeArgument(input);
    }

    @Override
    public void startPatternNode(final String regularExpression) {
        writer.startStatement(Rfc6020Mapping.PATTERN);
        writer.writeArgument(regularExpression);
    }

    @Override
    public void startValueNode(final Integer integer) {
        writer.startStatement(Rfc6020Mapping.VALUE);
        writer.writeArgument(integer.toString());
    }

    @Override
    public void startEnumNode(final String name) {
        writer.startStatement(Rfc6020Mapping.ENUM);
        writer.writeArgument(name);
    }

    @Override
    public void startRequireInstanceNode(final boolean require) {
        writer.startStatement(Rfc6020Mapping.REQUIRE_INSTANCE);
        writer.writeArgument(Boolean.toString(require));
    }

    @Override
    public void startPathNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(Rfc6020Mapping.PATH);
        writer.writeArgument(revisionAwareXPath);
    }

    @Override
    public void startBitNode(final String name) {
        writer.startStatement(Rfc6020Mapping.BIT);
        writer.writeArgument(name);
    }

    @Override
    public void startPositionNode(final UnsignedInteger position) {
        writer.startStatement(Rfc6020Mapping.POSITION);
        writer.writeArgument(position.toString());
    }

    @Override
    public void startImportNode(final String moduleName) {
        writer.startStatement(Rfc6020Mapping.IMPORT);
        writer.writeArgument(moduleName);
    }

    @Override
    public void startRevisionDateNode(final Date date) {
        writer.startStatement(Rfc6020Mapping.REVISION_DATE);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startUsesNode(final QName groupingName) {
        writer.startStatement(Rfc6020Mapping.USES);
        writer.writeArgument(groupingName);
    }

    @Override
    public void startAugmentNode(final SchemaPath targetPath) {
        writer.startStatement(Rfc6020Mapping.AUGMENT);
        writer.writeArgument(targetPath);
    }

    @Override
    public void startConfigNode(final boolean config) {
        writer.startStatement(Rfc6020Mapping.CONFIG);
        writer.writeArgument(Boolean.toString(config));
    }

    @Override
    public void startLengthNode(final String lengthString) {
        writer.startStatement(Rfc6020Mapping.LENGTH);
        writer.writeArgument(lengthString);
    }

    @Override
    public void startMaxElementsNode(final Integer max) {
        writer.startStatement(Rfc6020Mapping.MAX_ELEMENTS);
        writer.writeArgument(max.toString());
    }

    @Override
    public void startMinElementsNode(final Integer min) {
        writer.startStatement(Rfc6020Mapping.MIN_ELEMENTS);
        writer.writeArgument(min.toString());
    }

    @Override
    public void startPresenceNode(final boolean presence) {
        writer.startStatement(Rfc6020Mapping.PRESENCE);
        writer.writeArgument(Boolean.toString(presence));
    }

    @Override
    public void startOrderedByNode(final String ordering) {
        writer.startStatement(Rfc6020Mapping.ORDERED_BY);
        writer.writeArgument(ordering);
    }

    @Override
    public void startRangeNode(final String rangeString) {
        writer.startStatement(Rfc6020Mapping.RANGE);
        writer.writeArgument(rangeString);
    }

    @Override
    public void startFractionDigitsNode(final Integer fractionDigits) {
        writer.startStatement(Rfc6020Mapping.FRACTION_DIGITS);
        writer.writeArgument(fractionDigits.toString());
    }

    @Override
    public void startRefineNode(final SchemaPath path) {
        writer.startStatement(Rfc6020Mapping.REFINE);
        writer.writeArgument(path);
    }

    @Override
    public void startMandatoryNode(final boolean mandatory) {
        writer.startStatement(Rfc6020Mapping.MANDATORY);
        writer.writeArgument(Boolean.toString(mandatory));
    }

    @Override
    public void startAnyxmlNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.ANYXML);
        writer.writeArgument(qName);
    }

    @Override
    public void startUnknownNode(final StatementDefinition def) {
        writer.startStatement(def);
    }

    @Override
    public void startUnknownNode(final StatementDefinition def, final String nodeParameter) {
        writer.startStatement(def);
        writer.writeArgument(nodeParameter);
    }

    @Override
    public void startYinElementNode(final boolean yinElement) {
        writer.startStatement(Rfc6020Mapping.YIN_ELEMENT);
        writer.writeArgument(Boolean.toString(yinElement));
    }

    @Override
    public void startWhenNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(Rfc6020Mapping.WHEN);
        writer.writeArgument(revisionAwareXPath);
    }
}
