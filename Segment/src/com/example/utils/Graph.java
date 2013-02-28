package com.example.utils;

import java.util.LinkedList;

public class Graph  {
public static enum termType{//Terminals
	SOURCE,
	SINK
}
// captype : double
//node_id int
double flow = 0;
public LinkedList nodeBlock = new LinkedList();
public LinkedList arcBlock = new LinkedList();
int size = 0;

public double add_node(){
	Node n = new Node();
	n.first = null;
	n.tr_cap = 0;
	nodeBlock.add(n);
	return n.tr_cap;
}
/* Adds a bidirectional edge between 'from' and 'to'
with the weights 'cap' and 'rev_cap' */
public void add_edge(int from, int to, double cap, double rev_cap){
	Arc a = new Arc();
	Arc a_rev = new Arc();

	a.sister = a_rev;
	a_rev.sister = a;
	a.next = ((Node)nodeBlock.get(from)).first;
	((Node)nodeBlock.get(from)).first = a;
	a_rev.next = ((Node)nodeBlock.get(to)).first;
	((Node)nodeBlock.get(to)).first = a_rev;
	a.head = (Node)nodeBlock.get(to);
	a_rev.head = (Node)nodeBlock.get(from);
	a.r_cap = cap;
	a_rev.r_cap = rev_cap;
}

/* Sets the weights of the edges 'SOURCE->i' and 'i->SINK'
Can be called at most once for each node before any call to 'add_tweights'.
Weights can be negative */
public void set_tweights(int i, double cap_source, double cap_sink){
	flow += (cap_source < cap_sink) ? cap_source : cap_sink;
	((Node)nodeBlock.get(i)).tr_cap = cap_source - cap_sink;
};

/* Adds new edges 'SOURCE->i' and 'i->SINK' with corresponding weights
Can be called multiple times for each node.
Weights can be negative */
public void add_tweights(int i, double cap_source, double cap_sink){
	double delta =((Node)nodeBlock.get(i)).tr_cap;
	if (delta > 0) cap_source += delta;
	else           cap_sink   -= delta;
	flow += (cap_source < cap_sink) ? cap_source : cap_sink;
	((Node)nodeBlock.get(i)).tr_cap = cap_source - cap_sink;	
};

/* After the maxflow is computed, this function returns to which
segment the node 'i' belongs (Graph::SOURCE or Graph::SINK) */
public termType what_segment(int i){
	if (((Node)nodeBlock.get(i)).parent!=null && !(((Node)nodeBlock.get(i)).is_sink)) return termType.SOURCE;
	return termType.SINK;
	
};

}
class Node{
	public int id = 0;
	public Arc			first;		/* first outcoming arc */

	public Arc			parent;	/* node's parent */
	public Node			next;		/* pointer to the next active node
						   (or to itself if it is the last node in the list) */
	public int				TS;			/* timestamp showing when DIST was computed */
	public int				DIST;		/* distance to the terminal */
	public boolean			is_sink;	/* flag showing whether the node is in the source or in the sink tree */

	public double			tr_cap;		/* if tr_cap > 0 then tr_cap is residual capacity of the arc SOURCE->node
						   otherwise         -tr_cap is residual capacity of the arc node->SINK */
	public Node(){
		id = 0;
		is_sink = false;
	}
    public Node(Node n){
    	id = n.id;
    	first = new Arc(n.first);
    	next = new Node(n.next);
    	is_sink = n.is_sink;
    	tr_cap = n.tr_cap;
    	DIST = n.DIST;
    }
}
class Arc{
	public Node head;/* node the arc points to */
	public Arc next,sister;/* reverse arc */
	public double r_cap;/* residual capacity */
	public Arc(){
		head = null;
		next = null;
		r_cap = 0;
		sister = null;
	}
	public Arc(Arc a){
		head = new Node(a.head);
		r_cap = a.r_cap;
		next = new Arc(a.next);
	}
}
