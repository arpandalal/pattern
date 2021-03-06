/*
 * Copyright (c) 2007-2013 Concurrent, Inc. All Rights Reserved.
 *
 * Project and contact information: http://www.cascading.org/
 *
 * This file is part of the Cascading project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cascading.pattern.model.tree;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cascading.pattern.model.tree.decision.DecisionTree;
import cascading.pattern.model.tree.predicate.Predicate;
import cascading.tuple.Fields;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;

/**
 * Class Tree defines a decision tree.
 * <p/>
 * A Tree is literally a tree data structure where every possible branch is governed by a predicate, when
 * traversed, a branch is chosen if the predicate returns true against the current {@link cascading.tuple.Tuple}.
 * <p/>
 * When a leaf node is reached the "score" is returned, where the score can be a category or numeric value.
 * <p/>
 * When a set of Tree instances should be applied, see {@link cascading.pattern.ensemble.ParallelEnsembleAssembly}
 * for constructing a random forest implementation.
 */
public class Tree implements Serializable
  {
  private transient Map<String, Node> nodes = new HashMap<String, Node>();
  protected SimpleDirectedGraph<Node, Integer> graph = new SimpleDirectedGraph<Node, Integer>( Integer.class );
  protected Node root;

  private int count = 0;

  public Tree( String rootID )
    {
    root = new Node( rootID );
    nodes.put( root.getID(), root );
    graph.addVertex( root );
    }

  public Node getRoot()
    {
    return root;
    }

  public Set<Object> getScores()
    {
    Set<Object> set = new LinkedHashSet<Object>();

    for( Node node : getGraph().vertexSet() )
      {
      if( node.getScore() != null )
        set.add( node.getScore() );
      }

    return set;
    }

  public DirectedGraph<Node, Integer> getGraph()
    {
    return graph;
    }

  public void addPredicate( String fromID, String toID, Predicate predicate )
    {
    addPredicate( fromID, toID, predicate, null );
    }

  public void addPredicate( String fromID, String toID, Predicate predicate, Object score )
    {
    if( nodes.containsKey( toID ) )
      throw new IllegalArgumentException( "duplicate node name: " + toID );

    Node toNode = new Node( toID, predicate, score );

    nodes.put( toID, toNode );
    graph.addVertex( toNode );
    graph.addEdge( nodes.get( fromID ), toNode, count++ );
    }

  public DecisionTree createDecisionTree( Fields argumentFields )
    {
    return createDecisionTree( null, argumentFields );
    }

  public DecisionTree createDecisionTree( String[] categories, Fields argumentFields )
    {
    return new DecisionTree( categories, argumentFields, this, this.getRoot() );
    }

  @Override
  public String toString()
    {
    final StringBuilder sb = new StringBuilder( "Tree{" );
    sb.append( "nodes=" ).append( nodes );
    sb.append( ", graph=" ).append( graph );
    sb.append( ", root=" ).append( root );
    sb.append( '}' );
    return sb.toString();
    }
  }
