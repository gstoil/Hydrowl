/* Copyright 2013, 2014 by the National Technical University of Athens.

   This file is part of Hydrowl.

   Hydrowl is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Hydrowl is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with Hydrowl. If not, see <http://www.gnu.org/licenses/>.
 */

package org.semanticweb.hydrowl.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class LabeledGraph<Vertice,NodeLabel,EdgeLabel> {

	public class Edge {
		protected Vertice toElement;
		protected EdgeLabel edgeLabel;
		
		public Edge( EdgeLabel label, Vertice to ){
	        edgeLabel = label;
	        toElement = to;
		}
		public Vertice getToElement(){
			return toElement;
		}
		public EdgeLabel getEdgeLabel(){
			return edgeLabel;
		}
		public String toString(){
			return edgeLabel + " " + toElement;
		}
	}
	
    protected final Set<Vertice> elements;
    protected final Map<Vertice,Set<NodeLabel>> labelsByNodes;
    protected final Map<Vertice,Set<EdgeLabel>> edgeLabelsByNodes;
    protected final Map<Vertice,Set<Edge>> successorsByNodes;
    protected final Map<Vertice,Set<Edge>> predecessorsByNodes;

    public LabeledGraph() {
        elements = new HashSet<Vertice>();
        labelsByNodes = new HashMap<Vertice,Set<NodeLabel>>();
        edgeLabelsByNodes = new HashMap<Vertice,Set<EdgeLabel>>();
        successorsByNodes = new HashMap<Vertice,Set<Edge>>();
        predecessorsByNodes = new HashMap<Vertice,Set<Edge>>();
        
    }
    public void addLabel(Vertice node, NodeLabel nodeLabel){
    	Set<NodeLabel> nodeLabels = labelsByNodes.get( node );
		if( nodeLabels == null ){
			nodeLabels = new HashSet<NodeLabel>();
			labelsByNodes.put(node , nodeLabels);
		}
		nodeLabels.add(nodeLabel);
		elements.add(node);
    }
    public Set<NodeLabel> getLabelsOfNode(Vertice node){
    	return labelsByNodes.get( node );
    }
    public Set<EdgeLabel> getAllLabelsOfOutgoingEdges(Vertice node){
    	return edgeLabelsByNodes.get( node );
    }
    public void addEdge(Vertice from, Vertice to, EdgeLabel label) {
        Set<Edge> successorEdges = successorsByNodes.get(from);
        if (successorEdges == null) {
        	successorEdges = new HashSet<Edge>();
            successorsByNodes.put(from, successorEdges);
        }
        Edge newEdge = new Edge(label, to);
        successorEdges.add(newEdge);
        
        Set<Edge> predecessorEdges = predecessorsByNodes.get(to);
        if (predecessorEdges == null) {
        	predecessorEdges = new HashSet<Edge>();
        	predecessorsByNodes.put(to, predecessorEdges);
        }
        Edge newEdgeInverse = new Edge(label, from);
        predecessorEdges.add(newEdgeInverse);
        
        Set<EdgeLabel> edgeLabels = edgeLabelsByNodes.get(from);
        if (edgeLabels == null) {
        	edgeLabels = new HashSet<EdgeLabel>();
        	edgeLabelsByNodes.put(from, edgeLabels);
        }
        edgeLabels.add(label);
        
        elements.add(from);
        elements.add(to);
    }
    public Set<Vertice> getElements() {
        return elements;
    }
    public boolean hasCycles(){
		for (Vertice var : elements)
			if (isReachableSuccessor(var,var))
				return true;
		return false;
	}
    public void invertEdge(Vertice from,Edge edge,EdgeLabel invertedEdgeLabel ){
    	successorsByNodes.get(from).remove(edge);
    	addEdge(edge.getToElement(), from, invertedEdgeLabel);
    }
    public boolean isReachableSuccessor(Vertice fromNode,Vertice toNode) {
//	    	if( fromNode.equals( toNode ))
//	    		return true;
        Set<Vertice> result = new HashSet<Vertice>();
        Queue<Vertice> toVisit=new LinkedList<Vertice>();
        toVisit.add(fromNode);
        while (!toVisit.isEmpty()) {
        	Vertice current=toVisit.poll();
        	Set<Edge> successorEdges = getSuccessors( current );
            if (containsToNode(successorEdges,toNode))
            	return true;
            if (result.add(current))
                toVisit.addAll( getAllToNodes( successorEdges ) );
        }
        return false;
    }
    private Set<Vertice> getAllToNodes(Set<Edge> successorEdges) {
    	Set<Vertice> allToNodes = new HashSet<Vertice>();
    	for (Edge edge : successorEdges)
    		allToNodes.add( edge.getToElement() );
		return allToNodes;
	}
	private boolean containsToNode(Set<Edge> successorEdges, Vertice toNode) {
		for (Edge edge : successorEdges)
			if (edge.getToElement().equals( toNode ))
				return true;
		return false;
	}
	public Set<Edge> getSuccessors(Vertice node) {
        Set<Edge> result = successorsByNodes.get(node);
        if (result==null)
            result=Collections.emptySet();
        return result;
    }
	public Set<Edge> getPredecessors(Vertice node) {
        Set<Edge> result = predecessorsByNodes.get(node);
        if (result==null)
            result=Collections.emptySet();
        return result;
    }
    public LabeledGraph<Vertice,NodeLabel,EdgeLabel> clone() {
    	LabeledGraph<Vertice,NodeLabel,EdgeLabel> result=new LabeledGraph<Vertice,NodeLabel,EdgeLabel>();
        for (Map.Entry<Vertice,Set<Edge>> entry : successorsByNodes.entrySet()) {
            Vertice from=entry.getKey();
            for (Edge successor : entry.getValue())
                result.addEdge(from,successor.getToElement(),successor.getEdgeLabel());
        }
        return result;
    }
	public void printGraph(){
		for (Vertice var : getElements()) {
			System.out.println( "current var is: " + var  );
			for( Edge edge : getSuccessors( var ) ) 
				System.out.println( "successor var is " + edge.getToElement() + " with edge label: " + edge.getEdgeLabel() );
			for( Edge edge : getPredecessors( var ) ) 
				System.out.println( "predecessor var is " + edge.getToElement() + " with edge label: " + edge.getEdgeLabel() );
			
			System.out.println( );
		}			
	}
}