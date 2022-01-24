package collection.Comparator.ProductComparator;

public class Product {
	int id;
	String name;
	float price;

	public Product(int id, String name, float price) {
		this.id = id;
		this.name = name;
		this.price = price;
	}

//	@Override
//    public int hashCode() {
//		return this.id;
//	}

	@Override
	public boolean equals(Object obj) {
//		if (this == obj) {
//            return true;
//        }
//        if (!(this instanceof Product)) {
//            return false;
//        }
//        Product emp = (Product) obj;
//        return this.getId() == emp.getId();// || this.getName().equals(emp.getName());
//		  if (obj instanceof Product) {
//	            return ((Product) obj).id == id;
//	        }
	        return false;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getPrice() {
		return price;
	}

	public void setPrice(float price) {
		this.price = price;
	}
}
