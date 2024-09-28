package uni.course.lecture2.race;

public class Salary implements Runnable {

	private Account account;
	private double amount;
	public Salary(Account account, double amount) {
		this.account=account;
		this.amount=amount;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (int i=1;i<=10; i++) {
			account.addAmount(amount);
		}
	}
}
