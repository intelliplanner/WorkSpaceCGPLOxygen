package data_structure.LinkedList;


public class LinkedListReverse {

	Node head;

	static class Node {
		int data;
		Node next;

		Node(int data) {
			this.data = data;
			this.next = null;
		}
	}
	public LinkedListReverse insertNode(LinkedListReverse list, int newData) {
		Node newNode = new Node(newData);
		newNode.next = null; 		
		
		if (list.head == null) {
			list.head = newNode;
		} else {
			Node last = list.head;
			while (last.next != null) {
				last = last.next;
			}
			last.next = newNode;
			
		}
	
		return list;
	}

	static void printList(LinkedListReverse list) {
		
		if(list!=null) {
			Node node = list.head;
			
			while(node != null)  {
				System.out.print(" "+node.data);
				node = node.next;
			}
			System.out.println("");
		}
	}
	

	static void printReverseList(LinkedListReverse list) {
		if(list!=null) {
			printReverseList(list.head);
		}
	}
	
	private static void printReverseList(Node node) {
		if(node !=null) {
			printReverseList(node.next);
			System.out.print(" "+node.data);
		}
	}

	public static void main(String[] args) {
		LinkedListReverse list = new LinkedListReverse();
		list = list.insertNode(list, 3);
		list = list.insertNode(list, 2);
		list = list.insertNode(list, 1);
		list = list.insertNode(list, 4);
		list = list.insertNode(list, 5);
		list = list.insertNode(list, 6);
		
		System.out.println("----------- List -----------------------");
		printList(list);
		
		System.out.println("----------- List Reverse-----------------------");
		printReverseList(list);
		
//		System.out.println("----------- List Reverse-----------------------");
//		printReverseListNew(list, 3);
		
		System.out.println("----------- List Chunks Reverse -----------------------");
		reverseLinkedListIntoChunks(list);
	}
	
	
	static void printReverseListNew(LinkedListReverse list, int countIndex) {
		if(list!=null) {
			Node node = printReverseListNew(list.head, countIndex);
			System.out.println(" ");
			while(node !=null) {
				System.out.print(" "+node.data);
				node = node.next;
			}
		}
	}
	
	static int index=1;
	private static Node printReverseListNew(Node node,int countIndex) {
		if(node !=null) {
			if(index!=countIndex) {
				index++;
				printReverseListNew(node.next,countIndex);
				System.out.print(" "+node.data);
			}
		}
		return node;
	}
	
	
	private static Node reverseLinkedListIntoChunks(Node head, int k) {
		Node current = head; 
	       Node next = null; 
	       Node prev = null; 
	         
	       int count = 0; 
	  
	       /* Reverse first k nodes of linked list */
	       while (count < k && current != null)  
	       { 
	           next = current.next; 
	           current.next = prev; 
	           prev = current; 
	           current = next; 
	           count++; 
	       } 
	  
	       /* next is now a pointer to (k+1)th node  
	          Recursively call for the list starting from current. 
	          And make rest of the list as next of first node */
	       if (next != null)  
	          head.next = reverseLinkedListIntoChunks(next, k); 
	       
	       return prev; 

	}
	private static void reverseLinkedListIntoChunks(LinkedListReverse list) {
		Node node = list.head; 
		reverseLinkedListIntoChunks(node,2);
	}
	
	
	
	
}
