package data_structure.binary_tree.example1;

public class BinaryTreeEx1 {
	Node root;
	class Node {
		int key;
		Node left;
		Node right;

		public Node(int key) {
			this.key = key;
			left = null;
			right = null;
		}
	}



	BinaryTreeEx1() {
		root = null;
	}

	void insert(int key) {
		root = insert(root, key);
	}

	Node insert(Node root, int key) {
		if (root == null) {
			root = new Node(key);
			return root;
		}
		if (root.key > key) {
			root.left = insert(root.left, key);
		} else {
			root.right =insert(root.right, key);
		}
		return root;
	}

	void inOrder() {
		System.out.println("---------------In-Order-----------");
		inOrderRec(root);
		System.out.println("");
	}

	// A utility function to do inorder traversal of BST
	void inOrderRec(Node root) {
		if (root != null) {
			inOrderRec(root.left);
			System.out.print(root.key +" ");
			inOrderRec(root.right);
		}
	}

	void preOrder() {
		System.out.println("---------------Pre Order-----------");
		preOrder(root);
		System.out.println("");
	}

	void preOrder(Node root) {
		if(root!=null) {
			System.out.print(root.key+" ");
			preOrder(root.left);
			preOrder(root.right);
		}
	}

	void postOrder() {
		System.out.println("---------------Post Order-----------");
		postOrder(root);
		System.out.println("");
	}
	void postOrder(Node root) {
		if(root!=null) {
			postOrder(root.left);
			postOrder(root.right);
			System.out.print(root.key + " ");
		}
	}

	boolean searchKeyinBST(int key) {
		boolean s =  searchKeyinBST(root,key);
		return s;
	}
	
	boolean searchKeyinBST(Node root,int key) {
		if(root == null) {
			return false;
		}
		
		if(root.key == key) 
			return true;
		else if(root.key > key) 
			return searchKeyinBST(root.left,key);
		else 
			return	searchKeyinBST(root.right,key);
		
	}
	
	
	
	public static void main(String args[]) {
		BinaryTreeEx1 tree = new BinaryTreeEx1();

		/*
		 * Let us create following BST 50 / \ 30 70 / \ / \ 20 40 60 80
		 */
		tree.insert(50);
		tree.insert(30);
		tree.insert(20);
		tree.insert(40);
		tree.insert(70);
		tree.insert(60);
		tree.insert(80);

		// print inorder traversal of the BST
		tree.inOrder();
		tree.preOrder();
		tree.postOrder();
		
		
	System.out.println("Key Exist: "+tree.searchKeyinBST(90));
	}

}
