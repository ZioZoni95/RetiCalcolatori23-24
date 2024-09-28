public class Persona {
	private String name;
	private int age;
	private String city;
	public Persona (String name, int age, String city)
	{
		this.name=name;
		this.age=age;
		this.city=city;
	}
	public Persona ()
	{
		
	}
	public String toString() {
		return name+ String.valueOf(age)+city;
	}
	
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name=name;
	}
}

