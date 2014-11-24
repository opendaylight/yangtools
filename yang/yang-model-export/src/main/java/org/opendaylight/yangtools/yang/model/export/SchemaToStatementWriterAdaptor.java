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
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;

@Beta
@NotThreadSafe
final class SchemaToStatementWriterAdaptor implements YangModuleWriter {

    final StatementWriter writer;

    private SchemaToStatementWriterAdaptor(final StatementWriter writer) {
        this.writer = Preconditions.checkNotNull(writer);
    }

    public static final YangModuleWriter from(final StatementWriter writer) {
        return new SchemaToStatementWriterAdaptor(writer);
    }

    @Override
    public void endNode() {
        writer.endStatement();
    }

    @Override
    public void startModuleNode(final String identifier) {
        writer.startStatement(YangStatement.Module);
        writer.writeArgument(identifier);
    }

    @Override
    public void startOrganizationNode(final String input) {
        writer.startStatement(YangStatement.Organization);
        writer.writeArgument(input);
    }

    @Override
    public void startContactNode(final String input) {
        writer.startStatement(YangStatement.Contact);
        writer.writeArgument(input);
    }

    @Override
    public void startDescriptionNode(final String input) {
        writer.startStatement(YangStatement.Description);
        writer.writeArgument(input);
    }

    @Override
    public void startReferenceNode(final String input) {
        writer.startStatement(YangStatement.Reference);
        writer.writeArgument(input);
    }

    @Override
    public void startUnitsNode(final String input) {
        writer.startStatement(YangStatement.Units);
        writer.writeArgument(input);
    }

    @Override
    public void startYangVersionNode(final String input) {
        writer.startStatement(YangStatement.YangVersion);
        writer.writeArgument(input);
    }

    @Override
    public void startNamespaceNode(final URI uri) {
        writer.startStatement(YangStatement.Namespace);
        writer.writeArgument(uri.toString());
    }

    @Override
    public void startKeyNode(final List<QName> keyList) {
        writer.startStatement(YangStatement.Key);
        final StringBuilder keyStr = new StringBuilder();
        for (final QName item : keyList) {
            keyStr.append(item.getLocalName());
        }
        writer.writeArgument(keyStr.toString());
    }

    @Override
    public void startPrefixNode(final String input) {
        writer.startStatement(YangStatement.Prefix);
        writer.writeArgument(input);
    }

    @Override
    public void startFeatureNode(final QName qName) {
        writer.startStatement(YangStatement.Feature);
        writer.writeArgument(qName);
    }

    @Override
    public void startExtensionNode(final QName qName) {
        writer.startStatement(YangStatement.Extension);
        writer.writeArgument(qName);
    }

    @Override
    public void startArgumentNode(final String input) {
        writer.startStatement(YangStatement.Argument);
        writer.writeArgument(input);
    }

    @Override
    public void startStatusNode(final Status status) {
        writer.startStatement(YangStatement.Status);
        writer.writeArgument(status.toString().toLowerCase());
    }

    @Override
    public void startTypeNode(final QName qName) {
        writer.startStatement(YangStatement.Type);
        writer.writeArgument(qName);
    }

    @Override
    public void startLeafNode(final QName qName) {
        writer.startStatement(YangStatement.Leaf);
        writer.writeArgument(qName);
    }

    @Override
    public void startContainerNode(final QName qName) {
        writer.startStatement(YangStatement.Container);
        writer.writeArgument(qName);
    }

    @Override
    public void startGroupingNode(final QName qName) {
        writer.startStatement(YangStatement.Grouping);
        writer.writeArgument(qName);
    }

    @Override
    public void startRpcNode(final QName qName) {
        writer.startStatement(YangStatement.Rpc);
        writer.writeArgument(qName);
    }

    @Override
    public void startInputNode() {
        writer.startStatement(YangStatement.Input);
    }

    @Override
    public void startOutputNode() {
        writer.startStatement(YangStatement.Output);
    }

    @Override
    public void startLeafListNode(final QName qName) {
        writer.startStatement(YangStatement.LeafList);
        writer.writeArgument(qName);
    }

    @Override
    public void startListNode(final QName qName) {
        writer.startStatement(YangStatement.List);
        writer.writeArgument(qName);
    }

    @Override
    public void startChoiceNode(final QName qName) {
        writer.startStatement(YangStatement.Choice);
        writer.writeArgument(qName);
    }

    @Override
    public void startCaseNode(final QName qName) {
        writer.startStatement(YangStatement.Case);
        writer.writeArgument(qName);
    }

    @Override
    public void startNotificationNode(final QName qName) {
        writer.startStatement(YangStatement.Notification);
        writer.writeArgument(qName);
    }

    @Override
    public void startIdentityNode(final QName qName) {
        writer.startStatement(YangStatement.Identity);
        writer.writeArgument(qName);
    }

    @Override
    public void startBaseNode(final QName qName) {
        writer.startStatement(YangStatement.Base);
        writer.writeArgument(qName);
    }

    @Override
    public void startTypedefNode(final QName qName) {
        writer.startStatement(YangStatement.Typedef);
        writer.writeArgument(qName);
    }

    @Override
    public void startRevisionNode(final Date date) {
        writer.startStatement(YangStatement.Revision);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startDefaultNode(final String string) {
        writer.startStatement(YangStatement.Default);
        writer.writeArgument(string);
    }

    @Override
    public void startMustNode(final RevisionAwareXPath xpath) {
        writer.startStatement(YangStatement.Must);
        writer.writeArgument(xpath);
    }

    @Override
    public void startErrorMessageNode(final String input) {
        writer.startStatement(YangStatement.ErrorMessage);
        writer.writeArgument(input);
    }

    @Override
    public void startErrorAppTagNode(final String input) {
        writer.startStatement(YangStatement.ErrorAppTag);
        writer.writeArgument(input);
    }

    @Override
    public void startPatternNode(final String regularExpression) {
        writer.startStatement(YangStatement.Pattern);
        writer.writeArgument(regularExpression);
    }

    @Override
    public void startValueNode(final Integer integer) {
        writer.startStatement(YangStatement.Value);
        writer.writeArgument(integer.toString());
    }

    @Override
    public void startEnumNode(final String name) {
        writer.startStatement(YangStatement.Enum);
        writer.writeArgument(name);
    }

    @Override
    public void startRequireInstanceNode(final boolean require) {
        writer.startStatement(YangStatement.RequireInstance);
        writer.writeArgument(Boolean.toString(require));
    }

    @Override
    public void startPathNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(YangStatement.Path);
        writer.writeArgument(revisionAwareXPath);
    }

    @Override
    public void startBitNode(final String name) {
        writer.startStatement(YangStatement.Bit);
        writer.writeArgument(name);
    }

    @Override
    public void startPositionNode(final UnsignedInteger position) {
        writer.startStatement(YangStatement.Position);
        writer.writeArgument(position.toString());
    }

    @Override
    public void startImportNode(final String moduleName) {
        writer.startStatement(YangStatement.Import);
        writer.writeArgument(moduleName);
    }

    @Override
    public void startRevisionDateNode(final Date date) {
        writer.startStatement(YangStatement.RevisionDate);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startUsesNode(final QName groupingName) {
        writer.startStatement(YangStatement.Uses);
        writer.writeArgument(groupingName);
    }

    @Override
    public void startAugmentNode(final SchemaPath targetPath) {
        writer.startStatement(YangStatement.Augment);
        writer.writeArgument(targetPath);
    }

    @Override
    public void startConfigNode(final boolean config) {
        writer.startStatement(YangStatement.Config);
        writer.writeArgument(Boolean.toString(config));
    }

    @Override
    public void startLengthNode(final String lengthString) {
        writer.startStatement(YangStatement.Length);
        writer.writeArgument(lengthString);
    }

    @Override
    public void startMaxElementsNode(final Integer max) {
        writer.startStatement(YangStatement.MaxElements);
        writer.writeArgument(max.toString());
    }

    @Override
    public void startMinElementsNode(final Integer min) {
        writer.startStatement(YangStatement.MinElements);
        writer.writeArgument(min.toString());
    }

    @Override
    public void startPresenceNode(final boolean presence) {
        writer.startStatement(YangStatement.Presence);
        writer.writeArgument(Boolean.toString(presence));
    }

    @Override
    public void startOrderedByNode(final String ordering) {
        writer.startStatement(YangStatement.OrderedBy);
        writer.writeArgument(ordering);
    }

    @Override
    public void startRangeNode(final String rangeString) {
        writer.startStatement(YangStatement.Range);
        writer.writeArgument(rangeString);
    }

    @Override
    public void startFractionDigitsNode(final Integer fractionDigits) {
        writer.startStatement(YangStatement.FractionDigits);
        writer.writeArgument(fractionDigits.toString());
    }

    @Override
    public void startRefineNode(final SchemaPath path) {
        writer.startStatement(YangStatement.Refine);
        writer.writeArgument(path);
    }

    @Override
    public void startMandatoryNode(final boolean mandatory) {
        writer.startStatement(YangStatement.Mandatory);
        writer.writeArgument(Boolean.toString(mandatory));
    }

    @Override
    public void startAnyxmlNode(final QName qName) {
        writer.startStatement(YangStatement.Anyxml);
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
        writer.startStatement(YangStatement.YinElement);
        writer.writeArgument(Boolean.toString(yinElement));
    }

    @Override
    public void startWhenNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(YangStatement.When);
        writer.writeArgument(revisionAwareXPath);
    }
}
