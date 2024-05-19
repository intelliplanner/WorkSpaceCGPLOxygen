package InterviewQuestions.Persistent;

import java.util.Date;

final class ImmutableClass {
	private final String name;
	private final int id;
	private final Date date;
	private ImmutableClass(String name, int id,Date date) {
		this.name = name;
		this.id = id;
		this.date= new Date(date.getTime());
	}

	public ImmutableClass  getImmutableClassObject(String name, int id,Date date) {
		return new ImmutableClass( name,  id, date);
	}
	
	public Date getDate() {
		return new Date(date.getTime());
	}

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

}
