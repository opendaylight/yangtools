/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Verify;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.jaxen.ContextSupport;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.FunctionContext;
import org.jaxen.JaxenRuntimeException;
import org.jaxen.UnresolvableException;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.XPathFunctionContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.RegexUtils;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

/**
 * A {@link FunctionContext} which contains also YANG-specific functions current(), re-match(), deref(),
 * derived-from(), derived-from-or-self(), enum-value() and bit-is-set().
 */
final class YangFunctionContext implements FunctionContext {
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final Double DOUBLE_NAN = Double.NaN;

    // Core XPath functions, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static final FunctionContext XPATH_FUNCTION_CONTEXT = new XPathFunctionContext(false);
    // current() function, as per http://tools.ietf.org/html/rfc6020#section-6.4.1
    private static final Function CURRENT_FUNCTION = (context, args) -> {
        if (!args.isEmpty()) {
            throw new FunctionCallException("current() takes no arguments.");
        }

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());
        return (NormalizedNodeContext) context;
    };

    // re-match(string subject, string pattern) function as per https://tools.ietf.org/html/rfc7950#section-10.2.1
    private static final Function REMATCH_FUNCTION = (context, args) -> {
        if (args == null || args.size() != 2) {
            throw new FunctionCallException("re-match() takes two arguments: string subject, string pattern.");
        }

        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException("First argument of re-match() should be a String.");
        }

        if (!(args.get(1) instanceof String)) {
            throw new FunctionCallException("Second argument of re-match() should be a String.");
        }

        final String subject = (String) args.get(0);
        final String rawPattern = (String) args.get(1);

        final String pattern = RegexUtils.getJavaRegexFromXSD(rawPattern);

        return (Boolean) subject.matches(pattern);
    };

    // deref(node-set nodes) function as per https://tools.ietf.org/html/rfc7950#section-10.3.1
    private static final Function DEREF_FUNCTION = (context, args) -> {
        if (!args.isEmpty()) {
            throw new FunctionCallException("deref() takes only one argument: node-set nodes.");
        }

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final SchemaContext schemaContext = getSchemaContext(currentNodeContext);
        final TypedSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(schemaContext,
                currentNodeContext);

        final Object nodeValue = currentNodeContext.getNode().getValue();

        if (correspondingSchemaNode.getType() instanceof InstanceIdentifierTypeDefinition
                && nodeValue instanceof YangInstanceIdentifier) {
            return getNodeReferencedByInstanceIdentifier((YangInstanceIdentifier) nodeValue, currentNodeContext);
        }

        if (correspondingSchemaNode.getType() instanceof LeafrefTypeDefinition) {
            final LeafrefTypeDefinition leafrefType = (LeafrefTypeDefinition) correspondingSchemaNode.getType();
            final RevisionAwareXPath xpath = leafrefType.getPathStatement();
            return getNodeReferencedByLeafref(xpath, currentNodeContext, schemaContext, correspondingSchemaNode,
                    nodeValue);
        }

        return null;
    };

    private static NormalizedNode<?, ?> getNodeReferencedByInstanceIdentifier(final YangInstanceIdentifier path,
            final NormalizedNodeContext currentNodeContext) {
        final NormalizedNodeNavigator navigator = (NormalizedNodeNavigator) currentNodeContext.getNavigator();
        final NormalizedNode<?, ?> rootNode = navigator.getRootNode();
        final List<PathArgument> pathArguments = path.getPathArguments();
        if (pathArguments.get(0).getNodeType().equals(rootNode.getNodeType())) {
            final List<PathArgument> relPath = pathArguments.subList(1, pathArguments.size());
            final Optional<NormalizedNode<?, ?>> possibleNode = NormalizedNodes.findNode(rootNode, relPath);
            if (possibleNode.isPresent()) {
                return possibleNode.get();
            }
        }

        return null;
    }

    private static NormalizedNode<?, ?> getNodeReferencedByLeafref(final RevisionAwareXPath xpath,
            final NormalizedNodeContext currentNodeContext, final SchemaContext schemaContext,
            final TypedSchemaNode correspondingSchemaNode, final Object nodeValue) {
        final NormalizedNode<?, ?> referencedNode = xpath.isAbsolute() ? getNodeReferencedByAbsoluteLeafref(xpath,
                currentNodeContext, schemaContext, correspondingSchemaNode) : getNodeReferencedByRelativeLeafref(xpath,
                currentNodeContext, schemaContext, correspondingSchemaNode);

        if (referencedNode instanceof LeafSetNode) {
            return getReferencedLeafSetEntryNode((LeafSetNode<?>) referencedNode, nodeValue);
        }

        if (referencedNode instanceof LeafNode && referencedNode.getValue().equals(nodeValue)) {
            return referencedNode;
        }

        return null;
    }

    private static NormalizedNode<?, ?> getNodeReferencedByAbsoluteLeafref(final RevisionAwareXPath xpath,
            final NormalizedNodeContext currentNodeContext, final SchemaContext schemaContext,
            final TypedSchemaNode correspondingSchemaNode) {
        final LeafrefXPathStringParsingPathArgumentBuilder builder = new LeafrefXPathStringParsingPathArgumentBuilder(
                xpath.toString(), schemaContext, correspondingSchemaNode, currentNodeContext);
        final List<PathArgument> pathArguments = builder.build();
        final NormalizedNodeNavigator navigator = (NormalizedNodeNavigator) currentNodeContext.getNavigator();
        final NormalizedNode<?, ?> rootNode = navigator.getRootNode();
        if (pathArguments.get(0).getNodeType().equals(rootNode.getNodeType())) {
            final List<PathArgument> relPath = pathArguments.subList(1, pathArguments.size());
            final Optional<NormalizedNode<?, ?>> possibleNode = NormalizedNodes.findNode(rootNode, relPath);
            if (possibleNode.isPresent()) {
                return possibleNode.get();
            }
        }

        return null;
    }

    private static NormalizedNode<?, ?> getNodeReferencedByRelativeLeafref(final RevisionAwareXPath xpath,
            final NormalizedNodeContext currentNodeContext, final SchemaContext schemaContext,
            final TypedSchemaNode correspondingSchemaNode) {
        NormalizedNodeContext relativeNodeContext = currentNodeContext;
        final StringBuilder xPathStringBuilder = new StringBuilder(xpath.toString());
        // strip the relative path of all ../ at the beginning
        while (xPathStringBuilder.indexOf("../") == 0) {
            xPathStringBuilder.delete(0, 3);
            relativeNodeContext = relativeNodeContext.getParent();
        }

        // add / to the beginning of the path so that it can be processed the same way as an absolute path
        xPathStringBuilder.insert(0, '/');
        final LeafrefXPathStringParsingPathArgumentBuilder builder = new LeafrefXPathStringParsingPathArgumentBuilder(
                xPathStringBuilder.toString(), schemaContext, correspondingSchemaNode, currentNodeContext);
        final List<PathArgument> pathArguments = builder.build();
        final NormalizedNode<?, ?> relativeNode = relativeNodeContext.getNode();
        final Optional<NormalizedNode<?, ?>> possibleNode = NormalizedNodes.findNode(relativeNode, pathArguments);
        if (possibleNode.isPresent()) {
            return possibleNode.get();
        }

        return null;
    }

    private static LeafSetEntryNode<?> getReferencedLeafSetEntryNode(final LeafSetNode<?> referencedNode,
            final Object currentNodeValue) {
        for (final LeafSetEntryNode<?> entryNode : referencedNode.getValue()) {
            if (currentNodeValue.equals(entryNode.getValue())) {
                return entryNode;
            }
        }

        return null;
    }

    // derived-from(node-set nodes, string identity) function as per https://tools.ietf.org/html/rfc7950#section-10.4.1
    private static final Function DERIVED_FROM_FUNCTION = (context, args) -> {
        if (args == null || args.size() != 1) {
            throw new FunctionCallException("derived-from() takes two arguments: node-set nodes, string identity.");
        }

        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException("Argument 'identity' of derived-from() function should be a String.");
        }

        final String identityArg = (String) args.get(0);

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final SchemaContext schemaContext = getSchemaContext(currentNodeContext);
        final TypedSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(schemaContext,
            currentNodeContext);

        if (!(correspondingSchemaNode.getType() instanceof IdentityrefTypeDefinition)) {
            return Boolean.FALSE;
        }

        if (!(currentNodeContext.getNode().getValue() instanceof QName)) {
            return Boolean.FALSE;
        }

        final QName currentNodeValue = (QName) currentNodeContext.getNode().getValue();

        final IdentitySchemaNode identityArgSchemaNode = getIdentitySchemaNodeFromString(identityArg, schemaContext,
                correspondingSchemaNode);
        final IdentitySchemaNode currentNodeIdentitySchemaNode = getIdentitySchemaNodeFromQName(currentNodeValue,
                schemaContext);

        final Set<IdentitySchemaNode> ancestorIdentities = new HashSet<>();
        collectAncestorIdentities(currentNodeIdentitySchemaNode, ancestorIdentities);

        return Boolean.valueOf(ancestorIdentities.contains(identityArgSchemaNode));
    };

    // derived-from-or-self(node-set nodes, string identity) function as per
    // https://tools.ietf.org/html/rfc7950#section-10.4.2
    private static final Function DERIVED_FROM_OR_SELF_FUNCTION = (context, args) -> {
        if (args == null || args.size() != 1) {
            throw new FunctionCallException(
                "derived-from-or-self() takes two arguments: node-set nodes, string identity");
        }

        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException(
                "Argument 'identity' of derived-from-or-self() function should be a String.");
        }

        final String identityArg = (String) args.get(0);

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final SchemaContext schemaContext = getSchemaContext(currentNodeContext);
        final TypedSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(schemaContext,
            currentNodeContext);

        if (!(correspondingSchemaNode.getType() instanceof IdentityrefTypeDefinition)) {
            return Boolean.FALSE;
        }

        if (!(currentNodeContext.getNode().getValue() instanceof QName)) {
            return Boolean.FALSE;
        }

        final QName currentNodeValue = (QName) currentNodeContext.getNode().getValue();

        final IdentitySchemaNode identityArgSchemaNode = getIdentitySchemaNodeFromString(identityArg, schemaContext,
                correspondingSchemaNode);
        final IdentitySchemaNode currentNodeIdentitySchemaNode = getIdentitySchemaNodeFromQName(currentNodeValue,
                schemaContext);
        if (currentNodeIdentitySchemaNode.equals(identityArgSchemaNode)) {
            return Boolean.TRUE;
        }

        final Set<IdentitySchemaNode> ancestorIdentities = new HashSet<>();
        collectAncestorIdentities(currentNodeIdentitySchemaNode, ancestorIdentities);

        return Boolean.valueOf(ancestorIdentities.contains(identityArgSchemaNode));
    };

    private static void collectAncestorIdentities(final IdentitySchemaNode identity,
            final Set<IdentitySchemaNode> ancestorIdentities) {
        for (final IdentitySchemaNode id : identity.getBaseIdentities()) {
            collectAncestorIdentities(id, ancestorIdentities);
            ancestorIdentities.add(id);
        }
    }

    private static IdentitySchemaNode getIdentitySchemaNodeFromQName(final QName identityQName,
            final SchemaContext schemaContext) {
        final Module module = schemaContext.findModuleByNamespaceAndRevision(identityQName.getNamespace(),
                identityQName.getRevision());
        return findIdentitySchemaNodeInModule(module, identityQName);
    }

    private static IdentitySchemaNode getIdentitySchemaNodeFromString(final String identity,
            final SchemaContext schemaContext, final TypedSchemaNode correspondingSchemaNode) {
        final List<String> identityPrefixAndName = COLON_SPLITTER.splitToList(identity);
        final Module module = schemaContext.findModuleByNamespaceAndRevision(
                correspondingSchemaNode.getQName().getNamespace(), correspondingSchemaNode.getQName().getRevision());
        if (identityPrefixAndName.size() == 2) {
            // prefix of local module
            if (identityPrefixAndName.get(0).equals(module.getPrefix())) {
                return findIdentitySchemaNodeInModule(module, QName.create(module.getQNameModule(),
                        identityPrefixAndName.get(1)));
            }

            // prefix of imported module
            for (final ModuleImport moduleImport : module.getImports()) {
                if (identityPrefixAndName.get(0).equals(moduleImport.getPrefix())) {
                    final Module importedModule = schemaContext.findModuleByName(moduleImport.getModuleName(),
                        moduleImport.getRevision());
                    return findIdentitySchemaNodeInModule(importedModule, QName.create(
                        importedModule.getQNameModule(), identityPrefixAndName.get(1)));
                }
            }

            throw new IllegalArgumentException(String.format("Cannot resolve prefix '%s' from identity '%s'.",
                    identityPrefixAndName.get(0), identity));
        }

        if (identityPrefixAndName.size() == 1) { // without prefix
            return findIdentitySchemaNodeInModule(module, QName.create(module.getQNameModule(),
                    identityPrefixAndName.get(0)));
        }

        throw new IllegalArgumentException(String.format("Malformed identity argument: %s.", identity));
    }

    private static IdentitySchemaNode findIdentitySchemaNodeInModule(final Module module, final QName identityQName) {
        for (final IdentitySchemaNode id : module.getIdentities()) {
            if (identityQName.equals(id.getQName())) {
                return id;
            }
        }

        throw new IllegalArgumentException(String.format("Identity %s does not have a corresponding"
                    + " identity schema node in the module %s.", identityQName, module));
    }

    // enum-value(node-set nodes) function as per https://tools.ietf.org/html/rfc7950#section-10.5.1
    private static final Function ENUM_VALUE_FUNCTION = (context, args) -> {
        if (!args.isEmpty()) {
            throw new FunctionCallException("enum-value() takes one argument: node-set nodes.");
        }

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final SchemaContext schemaContext = getSchemaContext(currentNodeContext);
        final TypedSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(schemaContext,
            currentNodeContext);

        final TypeDefinition<?> nodeType = correspondingSchemaNode.getType();
        if (!(nodeType instanceof EnumTypeDefinition)) {
            return DOUBLE_NAN;
        }

        final Object nodeValue = currentNodeContext.getNode().getValue();
        if (!(nodeValue instanceof String)) {
            return DOUBLE_NAN;
        }

        final EnumTypeDefinition enumerationType = (EnumTypeDefinition) nodeType;
        final String enumName = (String) nodeValue;

        return getEnumValue(enumerationType, enumName);
    };

    private static int getEnumValue(final EnumTypeDefinition enumerationType, final String enumName) {
        for (final EnumTypeDefinition.EnumPair enumPair : enumerationType.getValues()) {
            if (enumName.equals(enumPair.getName())) {
                return enumPair.getValue();
            }
        }

        throw new IllegalStateException(String.format("Enum %s does not belong to enumeration %s.",
                enumName, enumerationType));
    }

    // bit-is-set(node-set nodes, string bit-name) function as per
    // https://tools.ietf.org/html/rfc7950#section-10.6.1
    private static final Function BIT_IS_SET_FUNCTION = (context, args) -> {
        if (args == null || args.size() != 1) {
            throw new FunctionCallException("bit-is-set() takes two arguments: node-set nodes, string bit-name");
        }

        if (!(args.get(0) instanceof String)) {
            throw new FunctionCallException("Argument bit-name of bit-is-set() function should be a String");
        }

        final String bitName = (String) args.get(0);

        Verify.verify(context instanceof NormalizedNodeContext, "Unhandled context %s", context.getClass());

        final NormalizedNodeContext currentNodeContext = (NormalizedNodeContext) context;
        final SchemaContext schemaContext = getSchemaContext(currentNodeContext);
        final TypedSchemaNode correspondingSchemaNode = getCorrespondingTypedSchemaNode(schemaContext,
            currentNodeContext);

        final TypeDefinition<?> nodeType = correspondingSchemaNode.getType();
        if (!(nodeType instanceof BitsTypeDefinition)) {
            return Boolean.FALSE;
        }

        final Object nodeValue = currentNodeContext.getNode().getValue();
        if (!(nodeValue instanceof Set)) {
            return Boolean.FALSE;
        }

        final BitsTypeDefinition bitsType = (BitsTypeDefinition) nodeType;
        Preconditions.checkState(containsBit(bitsType, bitName), "Bit %s does not belong to bits %s.", bitName,
            bitsType);
        return Boolean.valueOf(((Set<?>)nodeValue).contains(bitName));
    };

    private static boolean containsBit(final BitsTypeDefinition bitsType, final String bitName) {
        for (BitsTypeDefinition.Bit bit : bitsType.getBits()) {
            if (bitName.equals(bit.getName())) {
                return true;
            }
        }

        return false;
    }

    private static SchemaContext getSchemaContext(final NormalizedNodeContext normalizedNodeContext) {
        final ContextSupport contextSupport = normalizedNodeContext.getContextSupport();
        Verify.verify(contextSupport instanceof NormalizedNodeContextSupport, "Unhandled context support %s",
                contextSupport.getClass());
        return ((NormalizedNodeContextSupport) contextSupport).getSchemaContext();
    }

    private static TypedSchemaNode getCorrespondingTypedSchemaNode(final SchemaContext schemaContext,
            final NormalizedNodeContext currentNodeContext) {
        Iterator<NormalizedNodeContext> ancestorOrSelfAxisIterator;
        try {
            ancestorOrSelfAxisIterator = currentNodeContext.getContextSupport().getNavigator()
                    .getAncestorOrSelfAxisIterator(currentNodeContext);
        } catch (UnsupportedAxisException ex) {
            throw new JaxenRuntimeException(ex);
        }

        final Deque<QName> schemaPathToCurrentNode = new ArrayDeque<>();
        while (ancestorOrSelfAxisIterator.hasNext()) {
            final NormalizedNode<?, ?> nextNode = ancestorOrSelfAxisIterator.next().getNode();
            if (!(nextNode instanceof MapNode) && !(nextNode instanceof LeafSetNode)
                    && !(nextNode instanceof AugmentationNode)) {
                schemaPathToCurrentNode.addFirst(nextNode.getNodeType());
            }
        }

        final SchemaNode schemaNode = SchemaContextUtil.findNodeInSchemaContext(schemaContext, schemaPathToCurrentNode);

        Preconditions.checkNotNull(schemaNode, "Node %s does not have a corresponding SchemaNode in the SchemaContext.",
                currentNodeContext.getNode());
        Preconditions.checkState(schemaNode instanceof TypedSchemaNode, "Node %s must be a leaf or a leaf-list.",
                currentNodeContext.getNode());
        return (TypedSchemaNode) schemaNode;
    }

    // Singleton instance of reuse
    private static final YangFunctionContext INSTANCE = new YangFunctionContext();

    private YangFunctionContext() {
    }

    static YangFunctionContext getInstance() {
        return INSTANCE;
    }

    @Override
    public Function getFunction(final String namespaceURI, final String prefix, final String localName)
            throws UnresolvableException {
        if (prefix == null) {
            switch (localName) {
                case "bit-is-set":
                    return BIT_IS_SET_FUNCTION;
                case "current":
                    return CURRENT_FUNCTION;
                case "deref":
                    return DEREF_FUNCTION;
                case "derived-from":
                    return DERIVED_FROM_FUNCTION;
                case "derived-from-or-self":
                    return DERIVED_FROM_OR_SELF_FUNCTION;
                case "enum-value":
                    return ENUM_VALUE_FUNCTION;
                case "re-match":
                    return REMATCH_FUNCTION;
                default:
                    break;
            }
        }

        return XPATH_FUNCTION_CONTEXT.getFunction(namespaceURI, prefix, localName);
    }
}
