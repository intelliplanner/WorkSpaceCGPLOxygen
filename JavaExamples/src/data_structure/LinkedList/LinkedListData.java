package data_structure.LinkedList;

public class LinkedListData {

	Node head;

	static class Node {
		int data;
		Node next;

		Node(int data) {
			this.data = data;
			this.next = null;
		}
	}

	public static LinkedListData insertNodeAtPosition(LinkedListData list, int newData, int index) {
		Node currNode = list.head;
		Node prev = null;
		int count = 0;
		while(currNode != null) {
			if(index == count)
				break;
			prev = currNode;
			currNode = currNode.next;
			count++;
		}
		if(count==0) {
			Node newNode = new Node(newData);
			list.head = newNode;
			newNode.next = currNode;
		} else if(currNode!=null) {
			Node newNode = new Node(newData);	
			prev.next = newNode;
			newNode.next = currNode;
		}
		
		
		return list;
	}
	public LinkedListData insertNode(LinkedListData list, int newData) {
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

	static void printList(LinkedListData list) {
		
		if(list!=null) {
			Node node = list.head;
			
			while(node != null && node.next !=null)  {
				System.out.print(" "+node.data);
				node = node.next;
			}
			System.out.println("");
		}
	}
	
	public static LinkedListData delete(LinkedListData list , int key) {
			Node currNode = list.head; 
			Node prev = null;
			
			System.out.println("");
			// Case 1
			if(currNode != null && currNode.data == key )
			{
				list.head = currNode.next;
				System.out.println("found and deleted in First Case, key="+key); 
				return list;
			}
			
			// 2nd Case
			while(currNode != null && currNode.data != key) {
				prev = currNode;
				currNode = currNode.next; 
			}
			
			if(currNode != null) {
				prev.next = currNode.next;
				System.out.println("found and deleted in Second Case, Key="+key);
			}
			
			// 3rd Case
			
			if(currNode == null) {
				System.out.println("Not found 3rd Case, key="+key);
			}
			
			
			return list;	
	}
	
	public static LinkedListData deleteByIndex(LinkedListData list,int index) {
		int key  = getElementByIndex(list,index);
		list = delete(list, key);
		return list;
	}

	public static int getElementByIndex(LinkedListData list,int index) {
		System.out.println("");
		int key = -1; 
		int i = 0;
		Node currNode = list.head;
		while(currNode!=null) {
			
			if(index == i)
				key=currNode.data;
			
			currNode = currNode.next;	
			i++;
			
		}
		
		
		System.out.print("Key="+key+" on Index="+index);
		return key;
	}
	
	public static void main(String[] args) {
		LinkedListData list = new LinkedListData();
		list = list.insertNode(list, 3);
		list = list.insertNode(list, 2);
		list = list.insertNode(list, 1);
		list = list.insertNode(list, 4);
		list = list.insertNode(list, 4);
		
		System.out.println("----------- List -----------------------");
		printList(list);
		
//		System.out.println("----------- List After Delete By key-----------------------");
//		delete(list, 8);
//		printList(list);
		
//		System.out.println("----------- Get Value By Index-----------------------");
//		
//		getElementByIndex(list,2);
//		
//		System.out.println("----------- List After Delete By Index-----------------------");
//		deleteByIndex(list, 2);
//		printList(list);
		
		System.out.println("----------- List After Inser At Index-----------------------");
		list  = insertNodeAtPosition(list,5,2);
		
		printList(list);
		
	}
	
	
	
	

}
