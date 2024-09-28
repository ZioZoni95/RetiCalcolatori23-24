package uni.course.lecture2.race;

import java.util.concurrent.locks.*;

public class AccountLocked extends Account{
	
	private Lock accountLock= new ReentrantLock();
	
	public AccountLocked() {
		super();
	}

	public AccountLocked(double initial) {
		super(initial);
	}
	
	@Override
	public void addAmount(double amount) {
		try {
			accountLock.lock();
			double tmp =balance;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tmp += amount;
			this.balance=tmp;
		}
		finally {
			accountLock.unlock();
		}
	}
	
	@Override
	public void subtractAmount(double amount) {
		try {
			accountLock.lock();
			double tmp =balance;
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			tmp -= amount;
			this.balance=tmp;
		}
		finally {
			accountLock.unlock();
		}
	}

}


