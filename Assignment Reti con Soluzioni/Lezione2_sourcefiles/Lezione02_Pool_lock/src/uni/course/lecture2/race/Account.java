package uni.course.lecture2.race;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {
	protected double balance;
	
	
	public Account() {
		this.balance=0;
	}

	public Account(double initial) {
		this.balance=initial;
	}
	public double getAmount() {
		return this.balance;
	}

	public void addAmount(double amount) {	
		double tmp =balance;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tmp += amount;
		this.balance=tmp;	
	}

	public void subtractAmount(double amount) {	
		double tmp =balance;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		tmp -= amount;
		this.balance=tmp;
	
	}

}


