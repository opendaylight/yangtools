/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Objects;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * The Schema Context Util contains support methods for searching through Schema Context modules for specified schema
 * nodes via Schema Path or Revision Aware XPath. The Schema Context Util is designed as mixin,
 * so it is not instantiable.
 * 
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
@SuppressWarnings("all")
public class SchemaContextUtil {
  private SchemaContextUtil() {
  }
  
  /**
   * Method attempts to find DataSchemaNode in Schema Context via specified Schema Path. The returned
   * DataSchemaNode from method will be the node at the end of the SchemaPath. If the DataSchemaNode is not present
   * in the Schema Context the method will return <code>null</code>.
   * <br>
   * In case that Schema Context or Schema Path are not specified correctly (i.e. contains <code>null</code>
   * values) the method will return IllegalArgumentException.
   * 
   * @throws IllegalArgumentException
   * 
   * @param context
   *            Schema Context
   * @param schemaPath
   *            Schema Path to search for
   * @return DataSchemaNode from the end of the Schema Path or
   *         <code>null</code> if the Node is not present.
   */
  public static SchemaNode findDataSchemaNode(final SchemaContext context, final SchemaPath schemaPath) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (schemaPath == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Schema Path reference cannot be NULL");
      throw _illegalArgumentException_1;
    }
    final List<QName> prefixedPath = schemaPath.getPath();
    boolean _notEquals = (!Objects.equal(prefixedPath, null));
    if (_notEquals) {
      return SchemaContextUtil.findNodeInSchemaContext(context, prefixedPath);
    }
    return null;
  }
  
  /**
   * Method attempts to find DataSchemaNode inside of provided Schema Context and Yang Module accordingly to
   * Non-conditional Revision Aware XPath. The specified Module MUST be present in Schema Context otherwise the
   * operation would fail and return <code>null</code>.
   * <br>
   * The Revision Aware XPath MUST be specified WITHOUT the conditional statement (i.e. without [cond]) in path,
   * because in this state the Schema Context is completely unaware of data state and will be not able to properly
   * resolve XPath. If the XPath contains condition the method will return IllegalArgumentException.
   * <br>
   * In case that Schema Context or Module or Revision Aware XPath contains <code>null</code> references the method
   * will throw IllegalArgumentException
   * <br>
   * If the Revision Aware XPath is correct and desired Data Schema Node is present in Yang module or in depending
   * module in Schema Context the method will return specified Data Schema Node, otherwise the operation will fail
   * and method will return <code>null</code>.
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param module Yang Module
   * @param nonCondXPath Non Conditional Revision Aware XPath
   * @return Returns Data Schema Node for specified Schema Context for given Non-conditional Revision Aware XPath,
   * or <code>null</code> if the DataSchemaNode is not present in Schema Context.
   */
  public static SchemaNode findDataSchemaNode(final SchemaContext context, final Module module, final RevisionAwareXPath nonCondXPath) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (module == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    boolean _tripleEquals_2 = (nonCondXPath == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("Non Conditional Revision Aware XPath cannot be NULL!");
      throw _illegalArgumentException_2;
    }
    final String strXPath = nonCondXPath.toString();
    boolean _notEquals = (!Objects.equal(strXPath, null));
    if (_notEquals) {
      boolean _contains = strXPath.contains("[");
      if (_contains) {
        IllegalArgumentException _illegalArgumentException_3 = new IllegalArgumentException("Revision Aware XPath cannot contains condition!");
        throw _illegalArgumentException_3;
      }
      boolean _isAbsolute = nonCondXPath.isAbsolute();
      if (_isAbsolute) {
        final LinkedList<QName> qnamedPath = SchemaContextUtil.xpathToQNamePath(context, module, strXPath);
        boolean _notEquals_1 = (!Objects.equal(qnamedPath, null));
        if (_notEquals_1) {
          return SchemaContextUtil.findNodeInSchemaContext(context, qnamedPath);
        }
      }
    }
    return null;
  }
  
  /**
   * Method attempts to find DataSchemaNode inside of provided Schema Context and Yang Module accordingly to
   * Non-conditional relative Revision Aware XPath. The specified Module MUST be present in Schema Context otherwise
   * the operation would fail and return <code>null</code>.
   * <br>
   * The relative Revision Aware XPath MUST be specified WITHOUT the conditional statement (i.e. without [cond]) in
   * path, because in this state the Schema Context is completely unaware of data state and will be not able to
   * properly resolve XPath. If the XPath contains condition the method will return IllegalArgumentException.
   * <br>
   * The Actual Schema Node MUST be specified correctly because from this Schema Node will search starts. If the
   * Actual Schema Node is not correct the operation will simply fail, because it will be unable to find desired
   * DataSchemaNode.
   * <br>
   * In case that Schema Context or Module or Actual Schema Node or relative Revision Aware XPath contains
   * <code>null</code> references the method will throw IllegalArgumentException
   * <br>
   * If the Revision Aware XPath doesn't have flag <code>isAbsolute == false</code> the method will
   * throw IllegalArgumentException.
   * <br>
   * If the relative Revision Aware XPath is correct and desired Data Schema Node is present in Yang module or in
   * depending module in Schema Context the method will return specified Data Schema Node,
   * otherwise the operation will fail
   * and method will return <code>null</code>.
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param module Yang Module
   * @param actualSchemaNode Actual Schema Node
   * @param relativeXPath Relative Non Conditional Revision Aware XPath
   * @return DataSchemaNode if is present in specified Schema Context for given relative Revision Aware XPath,
   * otherwise will return <code>null</code>.
   */
  public static SchemaNode findDataSchemaNodeForRelativeXPath(final SchemaContext context, final Module module, final SchemaNode actualSchemaNode, final RevisionAwareXPath relativeXPath) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (module == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    boolean _tripleEquals_2 = (actualSchemaNode == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("Actual Schema Node reference cannot be NULL!");
      throw _illegalArgumentException_2;
    }
    boolean _tripleEquals_3 = (relativeXPath == null);
    if (_tripleEquals_3) {
      IllegalArgumentException _illegalArgumentException_3 = new IllegalArgumentException("Non Conditional Revision Aware XPath cannot be NULL!");
      throw _illegalArgumentException_3;
    }
    boolean _isAbsolute = relativeXPath.isAbsolute();
    if (_isAbsolute) {
      String _plus = ("Revision Aware XPath MUST be relative i.e. MUST contains ../, " + "for non relative Revision Aware XPath use findDataSchemaNode method!");
      IllegalArgumentException _illegalArgumentException_4 = new IllegalArgumentException(_plus);
      throw _illegalArgumentException_4;
    }
    final SchemaPath actualNodePath = actualSchemaNode.getPath();
    boolean _notEquals = (!Objects.equal(actualNodePath, null));
    if (_notEquals) {
      final LinkedList<QName> qnamePath = SchemaContextUtil.resolveRelativeXPath(context, module, relativeXPath, actualSchemaNode);
      boolean _notEquals_1 = (!Objects.equal(qnamePath, null));
      if (_notEquals_1) {
        return SchemaContextUtil.findNodeInSchemaContext(context, qnamePath);
      }
    }
    return null;
  }
  
  /**
   * Returns parent Yang Module for specified Schema Context in which Schema Node is declared. If the Schema Node
   * is not present in Schema Context the operation will return <code>null</code>.
   * <br>
   * If Schema Context or Schema Node contains <code>null</code> references the method will throw IllegalArgumentException
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param schemaNode Schema Node
   * @return Yang Module for specified Schema Context and Schema Node, if Schema Node is NOT present,
   * the method will returns <code>null</code>
   */
  public static Module findParentModule(final SchemaContext context, final SchemaNode schemaNode) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (schemaNode == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Schema Node cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    final SchemaPath schemaPath = schemaNode.getPath();
    boolean _tripleEquals_2 = (schemaPath == null);
    if (_tripleEquals_2) {
      String _plus = ("Schema Path for Schema Node is not " + "set properly (Schema Path is NULL)");
      IllegalStateException _illegalStateException = new IllegalStateException(_plus);
      throw _illegalStateException;
    }
    final List<QName> qnamedPath = schemaPath.getPath();
    boolean _or = false;
    boolean _tripleEquals_3 = (qnamedPath == null);
    if (_tripleEquals_3) {
      _or = true;
    } else {
      boolean _isEmpty = qnamedPath.isEmpty();
      _or = (_tripleEquals_3 || _isEmpty);
    }
    if (_or) {
      String _plus_1 = ("Schema Path contains invalid state of path parts." + "The Schema Path MUST contain at least ONE QName which defines namespace and Local name");
      String _plus_2 = (_plus_1 + "of path.");
      IllegalStateException _illegalStateException_1 = new IllegalStateException(_plus_2);
      throw _illegalStateException_1;
    }
    int _size = qnamedPath.size();
    int _minus = (_size - 1);
    final QName qname = qnamedPath.get(_minus);
    URI _namespace = qname.getNamespace();
    Date _revision = qname.getRevision();
    return context.findModuleByNamespaceAndRevision(_namespace, _revision);
  }
  
  /**
   * Method will attempt to find DataSchemaNode from specified Module and Queue of QNames through the Schema
   * Context. The QNamed path could be defined across multiple modules in Schema Context so the method is called
   * recursively. If the QNamed path contains QNames that are not part of any Module or Schema Context Path the
   * operation will fail and returns <code>null</code>
   * <br>
   * If Schema Context, Module or Queue of QNames refers to <code>null</code> values,
   * the method will throws IllegalArgumentException
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param module Yang Module
   * @param qnamedPath Queue of QNames
   * @return DataSchemaNode if is present in Module(s) for specified Schema Context and given QNamed Path,
   * otherwise will return <code>null</code>.
   */
  private static SchemaNode findSchemaNodeForGivenPath(final SchemaContext context, final Module module, final Queue<QName> qnamedPath) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (module == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    URI _namespace = module.getNamespace();
    boolean _tripleEquals_2 = (_namespace == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("Namespace for Module cannot contains NULL reference!");
      throw _illegalArgumentException_2;
    }
    boolean _or = false;
    boolean _tripleEquals_3 = (qnamedPath == null);
    if (_tripleEquals_3) {
      _or = true;
    } else {
      boolean _isEmpty = qnamedPath.isEmpty();
      _or = (_tripleEquals_3 || _isEmpty);
    }
    if (_or) {
      String _plus = ("Schema Path contains invalid state of path parts." + "The Schema Path MUST contain at least ONE QName which defines namespace and Local name");
      String _plus_1 = (_plus + "of path.");
      IllegalStateException _illegalStateException = new IllegalStateException(_plus_1);
      throw _illegalStateException;
    }
    DataNodeContainer nextNode = module;
    final URI moduleNamespace = module.getNamespace();
    QName childNodeQName = null;
    SchemaNode schemaNode = null;
    boolean _and = false;
    boolean _notEquals = (!Objects.equal(nextNode, null));
    if (!_notEquals) {
      _and = false;
    } else {
      boolean _isEmpty_1 = qnamedPath.isEmpty();
      boolean _not = (!_isEmpty_1);
      _and = (_notEquals && _not);
    }
    boolean _while = _and;
    while (_while) {
      {
        QName _peek = qnamedPath.peek();
        childNodeQName = _peek;
        boolean _notEquals_1 = (!Objects.equal(childNodeQName, null));
        if (_notEquals_1) {
          String _localName = childNodeQName.getLocalName();
          DataSchemaNode _dataChildByName = nextNode.getDataChildByName(_localName);
          schemaNode = _dataChildByName;
          boolean _and_1 = false;
          boolean _tripleEquals_4 = (schemaNode == null);
          if (!_tripleEquals_4) {
            _and_1 = false;
          } else {
            _and_1 = (_tripleEquals_4 && (nextNode instanceof Module));
          }
          if (_and_1) {
            NotificationDefinition _notificationByName = SchemaContextUtil.getNotificationByName(((Module) nextNode), childNodeQName);
            schemaNode = _notificationByName;
          }
          boolean _and_2 = false;
          boolean _tripleEquals_5 = (schemaNode == null);
          if (!_tripleEquals_5) {
            _and_2 = false;
          } else {
            _and_2 = (_tripleEquals_5 && (nextNode instanceof Module));
          }
          if (_and_2) {
          }
          final URI childNamespace = childNodeQName.getNamespace();
          final Date childRevision = childNodeQName.getRevision();
          boolean _notEquals_2 = (!Objects.equal(schemaNode, null));
          if (_notEquals_2) {
            if ((schemaNode instanceof ContainerSchemaNode)) {
              nextNode = ((ContainerSchemaNode) schemaNode);
            } else {
              if ((schemaNode instanceof ListSchemaNode)) {
                nextNode = ((ListSchemaNode) schemaNode);
              } else {
                if ((schemaNode instanceof ChoiceNode)) {
                  final ChoiceNode choice = ((ChoiceNode) schemaNode);
                  qnamedPath.poll();
                  boolean _isEmpty_2 = qnamedPath.isEmpty();
                  boolean _not_1 = (!_isEmpty_2);
                  if (_not_1) {
                    QName _peek_1 = qnamedPath.peek();
                    childNodeQName = _peek_1;
                    ChoiceCaseNode _caseNodeByName = choice.getCaseNodeByName(childNodeQName);
                    nextNode = _caseNodeByName;
                    schemaNode = ((DataSchemaNode) nextNode);
                  }
                } else {
                  nextNode = null;
                }
              }
            }
          } else {
            boolean _equals = childNamespace.equals(moduleNamespace);
            boolean _not_2 = (!_equals);
            if (_not_2) {
              final Module nextModule = context.findModuleByNamespaceAndRevision(childNamespace, childRevision);
              SchemaNode _findSchemaNodeForGivenPath = SchemaContextUtil.findSchemaNodeForGivenPath(context, nextModule, qnamedPath);
              schemaNode = _findSchemaNodeForGivenPath;
              return schemaNode;
            }
          }
          qnamedPath.poll();
        }
      }
      boolean _and_1 = false;
      boolean _notEquals_1 = (!Objects.equal(nextNode, null));
      if (!_notEquals_1) {
        _and_1 = false;
      } else {
        boolean _isEmpty_2 = qnamedPath.isEmpty();
        boolean _not_1 = (!_isEmpty_2);
        _and_1 = (_notEquals_1 && _not_1);
      }
      _while = _and_1;
    }
    return schemaNode;
  }
  
  private static SchemaNode findNodeInSchemaContext(final SchemaContext context, final List<QName> path) {
    final QName current = path.get(0);
    URI _namespace = current.getNamespace();
    Date _revision = current.getRevision();
    final Module module = context.findModuleByNamespaceAndRevision(_namespace, _revision);
    boolean _tripleEquals = (module == null);
    if (_tripleEquals) {
      return null;
    }
    return SchemaContextUtil.findNodeInModule(module, path);
  }
  
  private static SchemaNode findNodeInModule(final Module module, final List<QName> path) {
    final QName current = path.get(0);
    SchemaNode node = module.getDataChildByName(current);
    boolean _notEquals = (!Objects.equal(node, null));
    if (_notEquals) {
      List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNode(((DataSchemaNode) node), _nextLevel);
    }
    RpcDefinition _rpcByName = SchemaContextUtil.getRpcByName(module, current);
    node = _rpcByName;
    boolean _notEquals_1 = (!Objects.equal(node, null));
    if (_notEquals_1) {
      List<QName> _nextLevel_1 = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNodeInRpc(((RpcDefinition) node), _nextLevel_1);
    }
    NotificationDefinition _notificationByName = SchemaContextUtil.getNotificationByName(module, current);
    node = _notificationByName;
    boolean _notEquals_2 = (!Objects.equal(node, null));
    if (_notEquals_2) {
      List<QName> _nextLevel_2 = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNodeInNotification(((NotificationDefinition) node), _nextLevel_2);
    }
    return null;
  }
  
  private static SchemaNode findNodeInRpc(final RpcDefinition rpc, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return rpc;
    }
    final QName current = path.get(0);
    String _localName = current.getLocalName();
    final String _switchValue = _localName;
    boolean _matched = false;
    if (!_matched) {
      if (Objects.equal(_switchValue,"input")) {
        _matched=true;
        ContainerSchemaNode _input = rpc.getInput();
        List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
        return SchemaContextUtil.findNode(_input, _nextLevel);
      }
    }
    if (!_matched) {
      if (Objects.equal(_switchValue,"output")) {
        _matched=true;
        ContainerSchemaNode _output = rpc.getOutput();
        List<QName> _nextLevel_1 = SchemaContextUtil.nextLevel(path);
        return SchemaContextUtil.findNode(_output, _nextLevel_1);
      }
    }
    return null;
  }
  
  private static SchemaNode findNodeInNotification(final NotificationDefinition rpc, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return rpc;
    }
    final QName current = path.get(0);
    final DataSchemaNode node = rpc.getDataChildByName(current);
    boolean _notEquals = (!Objects.equal(node, null));
    if (_notEquals) {
      List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNode(node, _nextLevel);
    }
    return null;
  }
  
  private static SchemaNode _findNode(final ChoiceNode parent, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return parent;
    }
    final QName current = path.get(0);
    final ChoiceCaseNode node = parent.getCaseNodeByName(current);
    boolean _notEquals = (!Objects.equal(node, null));
    if (_notEquals) {
      List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNodeInCase(node, _nextLevel);
    }
    return null;
  }
  
  private static SchemaNode _findNode(final ContainerSchemaNode parent, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return parent;
    }
    final QName current = path.get(0);
    final DataSchemaNode node = parent.getDataChildByName(current);
    boolean _notEquals = (!Objects.equal(node, null));
    if (_notEquals) {
      List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNode(node, _nextLevel);
    }
    return null;
  }
  
  private static SchemaNode _findNode(final ListSchemaNode parent, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return parent;
    }
    final QName current = path.get(0);
    final DataSchemaNode node = parent.getDataChildByName(current);
    boolean _notEquals = (!Objects.equal(node, null));
    if (_notEquals) {
      List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNode(node, _nextLevel);
    }
    return null;
  }
  
  private static SchemaNode _findNode(final DataSchemaNode parent, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return parent;
    } else {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Path nesting violation");
      throw _illegalArgumentException;
    }
  }
  
  public static SchemaNode findNodeInCase(final ChoiceCaseNode parent, final List<QName> path) {
    boolean _isEmpty = path.isEmpty();
    if (_isEmpty) {
      return parent;
    }
    final QName current = path.get(0);
    final DataSchemaNode node = parent.getDataChildByName(current);
    boolean _notEquals = (!Objects.equal(node, null));
    if (_notEquals) {
      List<QName> _nextLevel = SchemaContextUtil.nextLevel(path);
      return SchemaContextUtil.findNode(node, _nextLevel);
    }
    return null;
  }
  
  public static RpcDefinition getRpcByName(final Module module, final QName name) {
    Set<RpcDefinition> _rpcs = module.getRpcs();
    for (final RpcDefinition notification : _rpcs) {
      QName _qName = notification.getQName();
      boolean _equals = Objects.equal(_qName, name);
      if (_equals) {
        return notification;
      }
    }
    return null;
  }
  
  private static List<QName> nextLevel(final List<QName> path) {
    int _size = path.size();
    return path.subList(1, _size);
  }
  
  public static NotificationDefinition getNotificationByName(final Module module, final QName name) {
    Set<NotificationDefinition> _notifications = module.getNotifications();
    for (final NotificationDefinition notification : _notifications) {
      QName _qName = notification.getQName();
      boolean _equals = Objects.equal(_qName, name);
      if (_equals) {
        return notification;
      }
    }
    return null;
  }
  
  /**
   * Transforms string representation of XPath to Queue of QNames. The XPath is split by "/" and for each part of
   * XPath is assigned correct module in Schema Path.
   * <br>
   * If Schema Context, Parent Module or XPath string contains <code>null</code> values,
   * the method will throws IllegalArgumentException
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param parentModule Parent Module
   * @param xpath XPath String
   * @return
   */
  private static LinkedList<QName> xpathToQNamePath(final SchemaContext context, final Module parentModule, final String xpath) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (parentModule == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Parent Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    boolean _tripleEquals_2 = (xpath == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("XPath string reference cannot be NULL!");
      throw _illegalArgumentException_2;
    }
    LinkedList<QName> _linkedList = new LinkedList<QName>();
    final LinkedList<QName> path = _linkedList;
    final String[] prefixedPath = xpath.split("/");
    for (final String pathComponent : prefixedPath) {
      boolean _isEmpty = pathComponent.isEmpty();
      boolean _not = (!_isEmpty);
      if (_not) {
        QName _stringPathPartToQName = SchemaContextUtil.stringPathPartToQName(context, parentModule, pathComponent);
        path.add(_stringPathPartToQName);
      }
    }
    return path;
  }
  
  /**
   * Transforms part of Prefixed Path as java String to QName.
   * <br>
   * If the string contains module prefix separated by ":" (i.e. mod:container) this module is provided from from
   * Parent Module list of imports. If the Prefixed module is present in Schema Context the QName can be
   * constructed.
   * <br>
   * If the Prefixed Path Part does not contains prefix the Parent's Module namespace is taken for construction of
   * QName.
   * <br>
   * If Schema Context, Parent Module or Prefixed Path Part refers to <code>null</code> the method will throw
   * IllegalArgumentException
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param parentModule Parent Module
   * @param prefixedPathPart Prefixed Path Part string
   * @return QName from prefixed Path Part String.
   */
  private static QName stringPathPartToQName(final SchemaContext context, final Module parentModule, final String prefixedPathPart) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (parentModule == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Parent Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    boolean _tripleEquals_2 = (prefixedPathPart == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("Prefixed Path Part cannot be NULL!");
      throw _illegalArgumentException_2;
    }
    boolean _contains = prefixedPathPart.contains(":");
    if (_contains) {
      final String[] prefixedName = prefixedPathPart.split(":");
      String _get = prefixedName[0];
      final Module module = SchemaContextUtil.resolveModuleForPrefix(context, parentModule, _get);
      boolean _notEquals = (!Objects.equal(module, null));
      if (_notEquals) {
        URI _namespace = module.getNamespace();
        Date _revision = module.getRevision();
        String _get_1 = prefixedName[1];
        QName _qName = new QName(_namespace, _revision, _get_1);
        return _qName;
      }
    } else {
      URI _namespace_1 = parentModule.getNamespace();
      Date _revision_1 = parentModule.getRevision();
      QName _qName_1 = new QName(_namespace_1, _revision_1, prefixedPathPart);
      return _qName_1;
    }
    return null;
  }
  
  /**
   * Method will attempt to resolve and provide Module reference for specified module prefix. Each Yang module
   * could contains multiple imports which MUST be associated with corresponding module prefix. The method simply
   * looks into module imports and returns the module that is bounded with specified prefix. If the prefix is not
   * present in module or the prefixed module is not present in specified Schema Context,
   * the method will return <code>null</code>.
   * <br>
   * If String prefix is the same as prefix of the specified Module the reference to this module is returned.
   * <br>
   * If Schema Context, Module or Prefix are referring to <code>null</code> the method will return
   * IllegalArgumentException
   * 
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param module Yang Module
   * @param prefix Module Prefix
   * @return Module for given prefix in specified Schema Context if is present, otherwise returns <code>null</code>
   */
  private static Module resolveModuleForPrefix(final SchemaContext context, final Module module, final String prefix) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (module == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    boolean _tripleEquals_2 = (prefix == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("Prefix string cannot be NULL!");
      throw _illegalArgumentException_2;
    }
    String _prefix = module.getPrefix();
    boolean _equals = prefix.equals(_prefix);
    if (_equals) {
      return module;
    }
    final Set<ModuleImport> imports = module.getImports();
    for (final ModuleImport mi : imports) {
      String _prefix_1 = mi.getPrefix();
      boolean _equals_1 = prefix.equals(_prefix_1);
      if (_equals_1) {
        String _moduleName = mi.getModuleName();
        Date _revision = mi.getRevision();
        return context.findModuleByName(_moduleName, _revision);
      }
    }
    return null;
  }
  
  /**
   * @throws IllegalArgumentException
   * 
   * @param context Schema Context
   * @param module Yang Module
   * @param relativeXPath Non conditional Revision Aware Relative XPath
   * @param leafrefSchemaPath Schema Path for Leafref
   * @return
   */
  private static LinkedList<QName> resolveRelativeXPath(final SchemaContext context, final Module module, final RevisionAwareXPath relativeXPath, final SchemaNode leafrefParentNode) {
    boolean _tripleEquals = (context == null);
    if (_tripleEquals) {
      IllegalArgumentException _illegalArgumentException = new IllegalArgumentException("Schema Context reference cannot be NULL!");
      throw _illegalArgumentException;
    }
    boolean _tripleEquals_1 = (module == null);
    if (_tripleEquals_1) {
      IllegalArgumentException _illegalArgumentException_1 = new IllegalArgumentException("Module reference cannot be NULL!");
      throw _illegalArgumentException_1;
    }
    boolean _tripleEquals_2 = (relativeXPath == null);
    if (_tripleEquals_2) {
      IllegalArgumentException _illegalArgumentException_2 = new IllegalArgumentException("Non Conditional Revision Aware XPath cannot be NULL!");
      throw _illegalArgumentException_2;
    }
    boolean _isAbsolute = relativeXPath.isAbsolute();
    if (_isAbsolute) {
      String _plus = ("Revision Aware XPath MUST be relative i.e. MUST contains ../, " + "for non relative Revision Aware XPath use findDataSchemaNode method!");
      IllegalArgumentException _illegalArgumentException_3 = new IllegalArgumentException(_plus);
      throw _illegalArgumentException_3;
    }
    SchemaPath _path = leafrefParentNode.getPath();
    boolean _tripleEquals_3 = (_path == null);
    if (_tripleEquals_3) {
      IllegalArgumentException _illegalArgumentException_4 = new IllegalArgumentException("Schema Path reference for Leafref cannot be NULL!");
      throw _illegalArgumentException_4;
    }
    LinkedList<QName> _linkedList = new LinkedList<QName>();
    final LinkedList<QName> absolutePath = _linkedList;
    final String strXPath = relativeXPath.toString();
    boolean _notEquals = (!Objects.equal(strXPath, null));
    if (_notEquals) {
      final String[] xpaths = strXPath.split("/");
      boolean _notEquals_1 = (!Objects.equal(xpaths, null));
      if (_notEquals_1) {
        int colCount = 0;
        String _get = xpaths[colCount];
        boolean _contains = _get.contains("..");
        boolean _while = _contains;
        while (_while) {
          int _plus_1 = (colCount + 1);
          colCount = _plus_1;
          String _get_1 = xpaths[colCount];
          boolean _contains_1 = _get_1.contains("..");
          _while = _contains_1;
        }
        SchemaPath _path_1 = leafrefParentNode.getPath();
        final List<QName> path = _path_1.getPath();
        boolean _notEquals_2 = (!Objects.equal(path, null));
        if (_notEquals_2) {
          int _size = path.size();
          final int lenght = (_size - colCount);
          List<QName> _subList = path.subList(0, lenght);
          absolutePath.addAll(_subList);
          int _length = xpaths.length;
          List<String> _subList_1 = ((List<String>)Conversions.doWrapArray(xpaths)).subList(colCount, _length);
          final Function1<String,QName> _function = new Function1<String,QName>() {
              public QName apply(final String it) {
                QName _stringPathPartToQName = SchemaContextUtil.stringPathPartToQName(context, module, it);
                return _stringPathPartToQName;
              }
            };
          List<QName> _map = ListExtensions.<String, QName>map(_subList_1, _function);
          absolutePath.addAll(_map);
        }
      }
    }
    return absolutePath;
  }
  
  private static SchemaNode findNode(final DataSchemaNode parent, final List<QName> path) {
    if (parent instanceof ChoiceNode) {
      return _findNode((ChoiceNode)parent, path);
    } else if (parent instanceof ContainerSchemaNode) {
      return _findNode((ContainerSchemaNode)parent, path);
    } else if (parent instanceof ListSchemaNode) {
      return _findNode((ListSchemaNode)parent, path);
    } else if (parent != null) {
      return _findNode(parent, path);
    } else {
      throw new IllegalArgumentException("Unhandled parameter types: " +
        Arrays.<Object>asList(parent, path).toString());
    }
  }
}
