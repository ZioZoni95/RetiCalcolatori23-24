package uni.course.lecture2.race;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Account account = new Account(1000);
		System.out.printf("initial balance is %f\n", account.getAmount());
		Bancomat bancomat = new Bancomat(account,1000);
		Salary salary= new Salary(account, 1000);
		
		Thread bankThread= new Thread(bancomat);
		Thread companyThread= new Thread(salary);
	 
		long time1=System.currentTimeMillis();

		bankThread.start();
		companyThread.start();
		
		try {
			bankThread.join();
			companyThread.join();
			long time2=System.currentTimeMillis();
		    System.out.println("time " + (time2-time1));
		    System.out.printf("task terminated account is %f\n", account.getAmount());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
