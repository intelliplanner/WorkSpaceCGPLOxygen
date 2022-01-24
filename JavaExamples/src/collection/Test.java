package collection;

import org.json.JSONException;
import org.json.JSONObject;

public class Test {
	String name;
	String subject;
	int age;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public static void main(String s[]) {
		Test ob = new Test();
		ob.setAge(11);
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.append("age", ob);
			//System.out.println(jsonObj.get);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
