package uni.course.lecture2.race;

public class Bancomat implements Runnable {

	private Account account;
	private double amount;
	public Bancomat(Account account, double amount) {
		this.account=account;
		this.amount=amount;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (int i=1;i<=10; i++) {
			account.subtractAmount(amount);
		}
	}
}
