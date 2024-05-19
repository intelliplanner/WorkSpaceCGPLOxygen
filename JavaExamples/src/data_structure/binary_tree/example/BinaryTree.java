package data_structure.binary_tree.example;

public class BinaryTree {

	static class Node {
		int value;
		Node left;
		Node right;

		Node(int value) {
			this.value = value;
			this.left = null;
			this.right = null;
		}
	}

	private static void insertNode(Node node, int val) {
		if (node.left != null) {
			insertNode(node, val);
		} else if (node.left != null) {

		}
		printBinaryTree(node);
	}

	private static void printBinaryTree(Node bTree) {

	}

	public static void main(String[] str) {

	}
}
