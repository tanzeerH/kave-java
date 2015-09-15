package cc.kave.commons.model.groum.legacy;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cc.kave.commons.model.groum.Groum;
import cc.kave.commons.model.groum.Node;
import cc.kave.commons.model.groum.SubGroum;
import cc.kave.commons.model.groum.comparator.GroumComparator;
import cc.kave.commons.model.groum.nodes.ActionNode;

public class GroumComparatorTest {
	
	@Test
	public void NonemptyGroumIsGreaterThanEmptyGroum() {
		Groum empty = new Groum();
		Groum nonempty = Fixture.createConnectedGroumOfSize(2);
		GroumComparator uut = new GroumComparator();
		assertEquals(1, uut.compare(nonempty, empty));
	}
	
	@Test
	public void emptyGroumIsLessThanNonemptyGroum() {
		Groum empty = new Groum();
		Groum nonempty = Fixture.createConnectedGroumOfSize(2);
		GroumComparator uut = new GroumComparator();
		assertEquals(-1, uut.compare(empty, nonempty));
	}
	
	@Test
	public void emptyGroumIsEqualToEmptyGroum() {
		Groum empty = new Groum();
		Groum otherempty = new Groum();
		GroumComparator uut = new GroumComparator();
		assertEquals(0, uut.compare(empty, otherempty));
	}
	
	@Test
	public void findsEdgeCountDifference() {
		Groum groum = Fixture.createConnectedGroumOfSize(3);
		
		Groum groum_less_eges = new Groum();
		Node a = new ActionNode("1","1");
		Node b = new ActionNode("2","2");
		Node c = new ActionNode("3","3");
		groum_less_eges.addNode(a);
		groum_less_eges.addNode(b);
		groum_less_eges.addNode(c);
		groum_less_eges.addEdge(a, b);

		GroumComparator uut = new GroumComparator();
		assertEquals(1, uut.compare(groum, groum_less_eges));
	}
	
	@Test
	public void findsEqualGroumsWithDifferentOrder() {
		Node node1 = new ActionNode("1","1");
		Node node2 = new ActionNode("2","2");
		Node node3 = new ActionNode("3","3");
		
		Groum groum1 = new SubGroum();
		groum1.addNode(node1);
		groum1.addNode(node2);
		groum1.addNode(node3);
		groum1.addEdge(node1, node2);
		groum1.addEdge(node2, node3);
		
		Groum groum2 = new SubGroum();
		groum2.addNode(node2);
		groum2.addNode(node3);
		groum2.addEdge(node2, node3);
		groum2.addNode(node1);
		groum2.addEdge(node1, node2);
		
		GroumComparator uut = new GroumComparator();
		assertEquals(0, uut.compare(groum1, groum2));
	}
	
	@Test
	public void findsDifferenceInSuccessorCount() {
		Node node1 = new ActionNode("1","1");
		Node node2 = new ActionNode("2","2");
		Node node3 = new ActionNode("3","3");
		Node node4 = new ActionNode("4","4");
		
		SubGroum groum1 = new SubGroum();
		groum1.addNode(node1);
		groum1.addNode(node2);
		groum1.addNode(node3);
		groum1.addNode(node4);
		groum1.addEdge(node1, node2);
		groum1.addEdge(node2, node3);
		groum1.addEdge(node2, node4);
		
		SubGroum groum2 = new SubGroum();
		groum2.addNode(node1);
		groum2.addNode(node2);
		groum2.addNode(node3);
		groum2.addNode(node4);
		groum2.addEdge(node1, node2);
		groum2.addEdge(node2, node3);
		groum2.addEdge(node3, node4);
		
		GroumComparator uut = new GroumComparator();
		assertEquals(1, uut.compare(groum1, groum2));
	}
	
	@Test
	public void findsDifferenceInSuccessor() {
		Node node1 = new ActionNode("1","1");
		Node node2 = new ActionNode("2","2");
		Node node3 = new ActionNode("3","3");
		Node node4 = new ActionNode("4","4");
		
		SubGroum groum1 = new SubGroum();
		groum1.addNode(node1);
		groum1.addNode(node2);
		groum1.addNode(node3);
		groum1.addNode(node4);
		groum1.addEdge(node1, node2);
		groum1.addEdge(node1, node3);
		groum1.addEdge(node2, node4);
		
		SubGroum groum2 = new SubGroum();
		groum2.addNode(node4);
		groum2.addNode(node3);
		groum2.addNode(node2);
		groum2.addNode(node1);
		groum2.addEdge(node1, node2);
		groum2.addEdge(node1, node3);
		groum2.addEdge(node3, node4);
		
		GroumComparator uut = new GroumComparator();
		assertEquals(1, uut.compare(groum1, groum2));
	}
}