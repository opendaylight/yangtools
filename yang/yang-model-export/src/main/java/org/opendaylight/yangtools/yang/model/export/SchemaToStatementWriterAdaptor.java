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
import com.google.common.collect.Iterables;
import com.google.common.primitives.UnsignedInteger;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;

@Beta
@NotThreadSafe
final class SchemaToStatementWriterAdaptor implements YangModuleWriter {

    private final StatementTextWriter writer;

    private SchemaToStatementWriterAdaptor(final StatementTextWriter writer) {
        this.writer = Preconditions.checkNotNull(writer);
    }

    public static YangModuleWriter from(final StatementTextWriter writer) {
        return new SchemaToStatementWriterAdaptor(writer);
    }

    @Override
    public void endNode() {
        writer.endStatement();
    }

    @Override
    public void startModuleNode(final String identifier) {
        writer.startStatement(YangStmtMapping.MODULE);
        writer.writeArgument(identifier);
    }

    @Override
    public void startOrganizationNode(final String input) {
        writer.startStatement(YangStmtMapping.ORGANIZATION);
        writer.writeArgument(input);
    }

    @Override
    public void startContactNode(final String input) {
        writer.startStatement(YangStmtMapping.CONTACT);
        writer.writeArgument(input);
    }

    @Override
    public void startDescriptionNode(final String input) {
        writer.startStatement(YangStmtMapping.DESCRIPTION);
        writer.writeArgument(input);
    }

    @Override
    public void startReferenceNode(final String input) {
        writer.startStatement(YangStmtMapping.REFERENCE);
        writer.writeArgument(input);
    }

    @Override
    public void startUnitsNode(final String input) {
        writer.startStatement(YangStmtMapping.UNITS);
        writer.writeArgument(input);
    }

    @Override
    public void startYangVersionNode(final String input) {
        writer.startStatement(YangStmtMapping.YANG_VERSION);
        writer.writeArgument(input);
    }

    @Override
    public void startNamespaceNode(final URI uri) {
        writer.startStatement(YangStmtMapping.NAMESPACE);
        writer.writeArgument(uri.toString());
    }

    @Override
    public void startKeyNode(final List<QName> keyList) {
        writer.startStatement(YangStmtMapping.KEY);
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
        writer.startStatement(YangStmtMapping.PREFIX);
        writer.writeArgument(input);
    }

    @Override
    public void startFeatureNode(final QName qName) {
        writer.startStatement(YangStmtMapping.FEATURE);
        writer.writeArgument(qName);
    }

    @Override
    public void startExtensionNode(final QName qName) {
        writer.startStatement(YangStmtMapping.EXTENSION);
        writer.writeArgument(qName);
    }

    @Override
    public void startArgumentNode(final String input) {
        writer.startStatement(YangStmtMapping.ARGUMENT);
        writer.writeArgument(input);
    }

    @Override
    public void startStatusNode(final Status status) {
        writer.startStatement(YangStmtMapping.STATUS);
        writer.writeArgument(status.toString().toLowerCase());
    }

    @Override
    public void startTypeNode(final QName qName) {
        writer.startStatement(YangStmtMapping.TYPE);
        writer.writeArgument(qName);
    }

    @Override
    public void startLeafNode(final QName qName) {
        writer.startStatement(YangStmtMapping.LEAF);
        writer.writeArgument(qName);
    }

    @Override
    public void startContainerNode(final QName qName) {
        writer.startStatement(YangStmtMapping.CONTAINER);
        writer.writeArgument(qName);
    }

    @Override
    public void startGroupingNode(final QName qName) {
        writer.startStatement(YangStmtMapping.GROUPING);
        writer.writeArgument(qName);
    }

    @Override
    public void startRpcNode(final QName qName) {
        writer.startStatement(YangStmtMapping.RPC);
        writer.writeArgument(qName);
    }

    @Override
    public void startInputNode() {
        writer.startStatement(YangStmtMapping.INPUT);
    }

    @Override
    public void startOutputNode() {
        writer.startStatement(YangStmtMapping.OUTPUT);
    }

    @Override
    public void startLeafListNode(final QName qName) {
        writer.startStatement(YangStmtMapping.LEAF_LIST);
        writer.writeArgument(qName);
    }

    @Override
    public void startListNode(final QName qName) {
        writer.startStatement(YangStmtMapping.LIST);
        writer.writeArgument(qName);
    }

    @Override
    public void startChoiceNode(final QName qName) {
        writer.startStatement(YangStmtMapping.CHOICE);
        writer.writeArgument(qName);
    }

    @Override
    public void startCaseNode(final QName qName) {
        writer.startStatement(YangStmtMapping.CASE);
        writer.writeArgument(qName);
    }

    @Override
    public void startNotificationNode(final QName qName) {
        writer.startStatement(YangStmtMapping.NOTIFICATION);
        writer.writeArgument(qName);
    }

    @Override
    public void startIdentityNode(final QName qName) {
        writer.startStatement(YangStmtMapping.IDENTITY);
        writer.writeArgument(qName);
    }

    @Override
    public void startBaseNode(final QName qName) {
        writer.startStatement(YangStmtMapping.BASE);
        writer.writeArgument(qName);
    }

    @Override
    public void startTypedefNode(final QName qName) {
        writer.startStatement(YangStmtMapping.TYPEDEF);
        writer.writeArgument(qName);
    }

    @Override
    public void startRevisionNode(final Date date) {
        writer.startStatement(YangStmtMapping.REVISION);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startDefaultNode(final String string) {
        writer.startStatement(YangStmtMapping.DEFAULT);
        writer.writeArgument(string);
    }

    @Override
    public void startMustNode(final RevisionAwareXPath xpath) {
        writer.startStatement(YangStmtMapping.MUST);
        writer.writeArgument(xpath);
    }

    @Override
    public void startErrorMessageNode(final String input) {
        writer.startStatement(YangStmtMapping.ERROR_MESSAGE);
        writer.writeArgument(input);
    }

    @Override
    public void startErrorAppTagNode(final String input) {
        writer.startStatement(YangStmtMapping.ERROR_APP_TAG);
        writer.writeArgument(input);
    }

    @Override
    public void startPatternNode(final String regularExpression) {
        writer.startStatement(YangStmtMapping.PATTERN);
        writer.writeArgument(regularExpression);
    }

    @Override
    public void startValueNode(final Integer integer) {
        writer.startStatement(YangStmtMapping.VALUE);
        writer.writeArgument(integer.toString());
    }

    @Override
    public void startEnumNode(final String name) {
        writer.startStatement(YangStmtMapping.ENUM);
        writer.writeArgument(name);
    }

    @Override
    public void startRequireInstanceNode(final boolean require) {
        writer.startStatement(YangStmtMapping.REQUIRE_INSTANCE);
        writer.writeArgument(Boolean.toString(require));
    }

    @Override
    public void startPathNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(YangStmtMapping.PATH);
        writer.writeArgument(revisionAwareXPath);
    }

    @Override
    public void startBitNode(final String name) {
        writer.startStatement(YangStmtMapping.BIT);
        writer.writeArgument(name);
    }

    @Override
    public void startPositionNode(final UnsignedInteger position) {
        writer.startStatement(YangStmtMapping.POSITION);
        writer.writeArgument(position.toString());
    }

    @Override
    public void startImportNode(final String moduleName) {
        writer.startStatement(YangStmtMapping.IMPORT);
        writer.writeArgument(moduleName);
    }

    @Override
    public void startRevisionDateNode(final Date date) {
        writer.startStatement(YangStmtMapping.REVISION_DATE);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startUsesNode(final QName groupingName) {
        writer.startStatement(YangStmtMapping.USES);
        writer.writeArgument(groupingName);
    }

    @Override
    public void startAugmentNode(final SchemaPath targetPath) {
        writer.startStatement(YangStmtMapping.AUGMENT);
        writer.writeArgument(targetPath);
    }

    @Override
    public void startConfigNode(final boolean config) {
        writer.startStatement(YangStmtMapping.CONFIG);
        writer.writeArgument(Boolean.toString(config));
    }

    @Override
    public void startLengthNode(final String lengthString) {
        writer.startStatement(YangStmtMapping.LENGTH);
        writer.writeArgument(lengthString);
    }

    @Override
    public void startMaxElementsNode(final Integer max) {
        writer.startStatement(YangStmtMapping.MAX_ELEMENTS);
        writer.writeArgument(max.toString());
    }

    @Override
    public void startMinElementsNode(final Integer min) {
        writer.startStatement(YangStmtMapping.MIN_ELEMENTS);
        writer.writeArgument(min.toString());
    }

    @Override
    public void startPresenceNode(final boolean presence) {
        writer.startStatement(YangStmtMapping.PRESENCE);
        writer.writeArgument(Boolean.toString(presence));
    }

    @Override
    public void startOrderedByNode(final String ordering) {
        writer.startStatement(YangStmtMapping.ORDERED_BY);
        writer.writeArgument(ordering);
    }

    @Override
    public void startRangeNode(final String rangeString) {
        writer.startStatement(YangStmtMapping.RANGE);
        writer.writeArgument(rangeString);
    }

    @Override
    public void startFractionDigitsNode(final Integer fractionDigits) {
        writer.startStatement(YangStmtMapping.FRACTION_DIGITS);
        writer.writeArgument(fractionDigits.toString());
    }

    @Override
    public void startRefineNode(final SchemaPath path) {
        writer.startStatement(YangStmtMapping.REFINE);
        writer.writeArgument(path);
    }

    @Override
    public void startMandatoryNode(final boolean mandatory) {
        writer.startStatement(YangStmtMapping.MANDATORY);
        writer.writeArgument(Boolean.toString(mandatory));
    }

    @Override
    public void startAnyxmlNode(final QName qName) {
        writer.startStatement(YangStmtMapping.ANYXML);
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
        writer.startStatement(YangStmtMapping.YIN_ELEMENT);
        writer.writeArgument(Boolean.toString(yinElement));
    }

    @Override
    public void startWhenNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(YangStmtMapping.WHEN);
        writer.writeArgument(revisionAwareXPath);
    }

    @Override
    public void startAnydataNode(final QName qName) {
        writer.startStatement(YangStmtMapping.ANYDATA);
        writer.writeArgument(qName);
    }

    @Override
    public void startActionNode(final QName qName) {
        writer.startStatement(YangStmtMapping.ACTION);
        writer.writeArgument(qName);
    }

    @Override
    public void startModifierNode(final ModifierKind modifier) {
        writer.startStatement(YangStmtMapping.MODIFIER);
        writer.writeArgument(modifier.getKeyword());
    }

    @Override
    public void startUniqueNode(final UniqueConstraint uniqueConstraint) {
        writer.startStatement(YangStmtMapping.UNIQUE);
        final StringBuilder uniqueStr = new StringBuilder();
        final Collection<Relative> tag = uniqueConstraint.getTag();

        final Iterator<Relative> iter = tag.iterator();
        while (iter.hasNext()) {
            uniqueStr.append(
                    String.join("/", Iterables.transform(iter.next().getPathFromRoot(), qn -> qn.getLocalName())));
            if (iter.hasNext()) {
                uniqueStr.append(' ');
            }
        }
        writer.writeArgument(uniqueStr.toString());
    }

    @Override
    public void startRevisionNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.REVISION);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startRevisionDateNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.REVISION_DATE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startExtensionNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.EXTENSION);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startBaseNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.BASE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startFeatureNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.FEATURE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startYinElementNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.YIN_ELEMENT);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startIdentityNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.IDENTITY);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startTypedefNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.TYPEDEF);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startRpcNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.RPC);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startTypeNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.TYPE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startContainerNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.CONTAINER);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startPresenceNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.PRESENCE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startStatusNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.STATUS);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startConfigNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.CONFIG);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startLeafNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.LEAF);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startWhenNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.WHEN);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startLeafListNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.LEAF_LIST);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startMustNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.MUST);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startMinElementsNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.MIN_ELEMENTS);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startMaxElementsNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.MAX_ELEMENTS);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startListNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.LIST);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startKeyNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.KEY);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startUniqueNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.UNIQUE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startActionNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.ACTION);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startChoiceNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.CHOICE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startMandatoryNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.MANDATORY);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startAnyxmlNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.ANYXML);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startCaseNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.CASE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startAnydataNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.ANYDATA);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startGroupingNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.GROUPING);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startUsesNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.USES);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startRefineNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.REFINE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startAugmentNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.AUGMENT);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startNotificationNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.NOTIFICATION);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startValueNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.VALUE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startModifierNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.MODIFIER);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startFractionDigitsNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.FRACTION_DIGITS);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startPathNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.PATH);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startRequireInstanceNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.REQUIRE_INSTANCE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startPositionNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.POSITION);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startBelongsToNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.BELONGS_TO);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startIfFeatureNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.IF_FEATURE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startSubmoduleNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.SUBMODULE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startIncludeNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.INCLUDE);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startDeviationNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.DEVIATION);
        writer.writeArgument(rawArgument);
    }

    @Override
    public void startDeviateNode(final String rawArgument) {
        writer.startStatement(YangStmtMapping.DEVIATE);
        writer.writeArgument(rawArgument);
    }
}
