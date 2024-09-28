import java.util.ArrayList;

public class Country {
	private String name;
	private int population;
	private final ArrayList<String> regions = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPopulation() {
		return population;
	}
	public void setPopulation(int population) {
		this.population = population;
	}
	public ArrayList<String> getRegions() {
		return regions;
	}
	public void addRegion(String region) {
		this.regions.add(region);
	}
}
