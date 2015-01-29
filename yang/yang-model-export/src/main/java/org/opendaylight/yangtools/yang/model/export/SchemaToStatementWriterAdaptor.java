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
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
@NotThreadSafe
final class SchemaToStatementWriterAdaptor implements Rfc6020ModuleWriter {

    final StatementTextWriter writer;

    private SchemaToStatementWriterAdaptor(final StatementTextWriter writer) {
        this.writer = Preconditions.checkNotNull(writer);
    }

    public static final Rfc6020ModuleWriter from(final StatementTextWriter writer) {
        return new SchemaToStatementWriterAdaptor(writer);
    }

    @Override
    public void endNode() {
        writer.endStatement();
    }

    @Override
    public void startModuleNode(final String identifier) {
        writer.startStatement(Rfc6020Mapping.Module);
        writer.writeArgument(identifier);
    }

    @Override
    public void startOrganizationNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Organization);
        writer.writeArgument(input);
    }

    @Override
    public void startContactNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Contact);
        writer.writeArgument(input);
    }

    @Override
    public void startDescriptionNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Description);
        writer.writeArgument(input);
    }

    @Override
    public void startReferenceNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Reference);
        writer.writeArgument(input);
    }

    @Override
    public void startUnitsNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Units);
        writer.writeArgument(input);
    }

    @Override
    public void startYangVersionNode(final String input) {
        writer.startStatement(Rfc6020Mapping.YangVersion);
        writer.writeArgument(input);
    }

    @Override
    public void startNamespaceNode(final URI uri) {
        writer.startStatement(Rfc6020Mapping.Namespace);
        writer.writeArgument(uri.toString());
    }

    @Override
    public void startKeyNode(final List<QName> keyList) {
        writer.startStatement(Rfc6020Mapping.Key);
        final StringBuilder keyStr = new StringBuilder();
        for (final QName item : keyList) {
            keyStr.append(item.getLocalName());
        }
        writer.writeArgument(keyStr.toString());
    }

    @Override
    public void startPrefixNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Prefix);
        writer.writeArgument(input);
    }

    @Override
    public void startFeatureNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Feature);
        writer.writeArgument(qName);
    }

    @Override
    public void startExtensionNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Extension);
        writer.writeArgument(qName);
    }

    @Override
    public void startArgumentNode(final String input) {
        writer.startStatement(Rfc6020Mapping.Argument);
        writer.writeArgument(input);
    }

    @Override
    public void startStatusNode(final Status status) {
        writer.startStatement(Rfc6020Mapping.Status);
        writer.writeArgument(status.toString().toLowerCase());
    }

    @Override
    public void startTypeNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Type);
        writer.writeArgument(qName);
    }

    @Override
    public void startLeafNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Leaf);
        writer.writeArgument(qName);
    }

    @Override
    public void startContainerNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Container);
        writer.writeArgument(qName);
    }

    @Override
    public void startGroupingNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Grouping);
        writer.writeArgument(qName);
    }

    @Override
    public void startRpcNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Rpc);
        writer.writeArgument(qName);
    }

    @Override
    public void startInputNode() {
        writer.startStatement(Rfc6020Mapping.Input);
    }

    @Override
    public void startOutputNode() {
        writer.startStatement(Rfc6020Mapping.Output);
    }

    @Override
    public void startLeafListNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.LeafList);
        writer.writeArgument(qName);
    }

    @Override
    public void startListNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.List);
        writer.writeArgument(qName);
    }

    @Override
    public void startChoiceNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Choice);
        writer.writeArgument(qName);
    }

    @Override
    public void startCaseNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Case);
        writer.writeArgument(qName);
    }

    @Override
    public void startNotificationNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Notification);
        writer.writeArgument(qName);
    }

    @Override
    public void startIdentityNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Identity);
        writer.writeArgument(qName);
    }

    @Override
    public void startBaseNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Base);
        writer.writeArgument(qName);
    }

    @Override
    public void startTypedefNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Typedef);
        writer.writeArgument(qName);
    }

    @Override
    public void startRevisionNode(final Date date) {
        writer.startStatement(Rfc6020Mapping.Revision);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startDefaultNode(final String string) {
        writer.startStatement(Rfc6020Mapping.Default);
        writer.writeArgument(string);
    }

    @Override
    public void startMustNode(final RevisionAwareXPath xpath) {
        writer.startStatement(Rfc6020Mapping.Must);
        writer.writeArgument(xpath);
    }

    @Override
    public void startErrorMessageNode(final String input) {
        writer.startStatement(Rfc6020Mapping.ErrorMessage);
        writer.writeArgument(input);
    }

    @Override
    public void startErrorAppTagNode(final String input) {
        writer.startStatement(Rfc6020Mapping.ErrorAppTag);
        writer.writeArgument(input);
    }

    @Override
    public void startPatternNode(final String regularExpression) {
        writer.startStatement(Rfc6020Mapping.Pattern);
        writer.writeArgument(regularExpression);
    }

    @Override
    public void startValueNode(final Integer integer) {
        writer.startStatement(Rfc6020Mapping.Value);
        writer.writeArgument(integer.toString());
    }

    @Override
    public void startEnumNode(final String name) {
        writer.startStatement(Rfc6020Mapping.Enum);
        writer.writeArgument(name);
    }

    @Override
    public void startRequireInstanceNode(final boolean require) {
        writer.startStatement(Rfc6020Mapping.RequireInstance);
        writer.writeArgument(Boolean.toString(require));
    }

    @Override
    public void startPathNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(Rfc6020Mapping.Path);
        writer.writeArgument(revisionAwareXPath);
    }

    @Override
    public void startBitNode(final String name) {
        writer.startStatement(Rfc6020Mapping.Bit);
        writer.writeArgument(name);
    }

    @Override
    public void startPositionNode(final UnsignedInteger position) {
        writer.startStatement(Rfc6020Mapping.Position);
        writer.writeArgument(position.toString());
    }

    @Override
    public void startImportNode(final String moduleName) {
        writer.startStatement(Rfc6020Mapping.Import);
        writer.writeArgument(moduleName);
    }

    @Override
    public void startRevisionDateNode(final Date date) {
        writer.startStatement(Rfc6020Mapping.RevisionDate);
        writer.writeArgument(SimpleDateFormatUtil.getRevisionFormat().format(date));
    }

    @Override
    public void startUsesNode(final QName groupingName) {
        writer.startStatement(Rfc6020Mapping.Uses);
        writer.writeArgument(groupingName);
    }

    @Override
    public void startAugmentNode(final SchemaPath targetPath) {
        writer.startStatement(Rfc6020Mapping.Augment);
        writer.writeArgument(targetPath);
    }

    @Override
    public void startConfigNode(final boolean config) {
        writer.startStatement(Rfc6020Mapping.Config);
        writer.writeArgument(Boolean.toString(config));
    }

    @Override
    public void startLengthNode(final String lengthString) {
        writer.startStatement(Rfc6020Mapping.Length);
        writer.writeArgument(lengthString);
    }

    @Override
    public void startMaxElementsNode(final Integer max) {
        writer.startStatement(Rfc6020Mapping.MaxElements);
        writer.writeArgument(max.toString());
    }

    @Override
    public void startMinElementsNode(final Integer min) {
        writer.startStatement(Rfc6020Mapping.MinElements);
        writer.writeArgument(min.toString());
    }

    @Override
    public void startPresenceNode(final boolean presence) {
        writer.startStatement(Rfc6020Mapping.Presence);
        writer.writeArgument(Boolean.toString(presence));
    }

    @Override
    public void startOrderedByNode(final String ordering) {
        writer.startStatement(Rfc6020Mapping.OrderedBy);
        writer.writeArgument(ordering);
    }

    @Override
    public void startRangeNode(final String rangeString) {
        writer.startStatement(Rfc6020Mapping.Range);
        writer.writeArgument(rangeString);
    }

    @Override
    public void startFractionDigitsNode(final Integer fractionDigits) {
        writer.startStatement(Rfc6020Mapping.FractionDigits);
        writer.writeArgument(fractionDigits.toString());
    }

    @Override
    public void startRefineNode(final SchemaPath path) {
        writer.startStatement(Rfc6020Mapping.Refine);
        writer.writeArgument(path);
    }

    @Override
    public void startMandatoryNode(final boolean mandatory) {
        writer.startStatement(Rfc6020Mapping.Mandatory);
        writer.writeArgument(Boolean.toString(mandatory));
    }

    @Override
    public void startAnyxmlNode(final QName qName) {
        writer.startStatement(Rfc6020Mapping.Anyxml);
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
        writer.startStatement(Rfc6020Mapping.YinElement);
        writer.writeArgument(Boolean.toString(yinElement));
    }

    @Override
    public void startWhenNode(final RevisionAwareXPath revisionAwareXPath) {
        writer.startStatement(Rfc6020Mapping.When);
        writer.writeArgument(revisionAwareXPath);
    }
}
