package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.WAIT_TIMEOUT;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.tkym.labs.jvml.JavaLaunchException;
import com.tkym.labs.jvml.JavaLaunchFactory;
import com.tkym.labs.sharedmem.win.BaseNamedObjectsJni;
import com.tkym.labs.sharedmem.win.Mutex;
import com.tkym.labs.sharedmem.win.BaseNamedObjectsException;
import com.tkym.labs.sharedmem.win.MutexRepository;

public class MutexTest {
	
	@Test
	public void testMutexObjects_Case001() throws BaseNamedObjectsException, TimeoutException{
		MutexRepository repo = 
				MutexRepository.getInstance();
		String mutexName = "testMutexObjects_Case001";
		Mutex mutex = repo.create(mutexName);
		mutex.waitFor(10);
		mutex.release();
		mutex.close();
	}
	
	@Test
	public void testMutexObjects_Case002() throws BaseNamedObjectsException, JavaLaunchException, InterruptedException, ExecutionException, TimeoutException{
		MutexRepository repo = 
				MutexRepository.getInstance();
		String mutexName = "testMutexObjects_Case002";
		repo.create(mutexName);
		Mutex mutex = repo.open(mutexName);
		mutex.waitFor(10);
		mutex.release();
		mutex.close();
	}
	
	@Test
	public void testMutexObjects_Case003() throws BaseNamedObjectsException, JavaLaunchException, InterruptedException, ExecutionException, TimeoutException{
		MutexRepository repo = 
				MutexRepository.getInstance();
		String mutexName = "testMutexObjects_Case003";
		repo.create(mutexName);
		Mutex mutex = repo.open(mutexName);
		mutex.waitFor();
		mutex.release();
		mutex.close();
		int ret2 = 
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName+"1").
				asFuture().get();
		assertThat(ret2, is(0));
	}
	
	@Test
	public void testMutexObjects_Case004() throws BaseNamedObjectsException, JavaLaunchException, InterruptedException, ExecutionException, TimeoutException{
		String mutexName = "testMutexObjects_Case004";
		Future<Integer> f1 =
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName,"f1","1000").
				asFuture();
		Future<Integer> f2 =
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName,"f2","1","100").
				asFuture();
		assertThat(f2.get(), is(WAIT_TIMEOUT));
		assertThat(f1.get(), is(0));
	}

	@Test
	public void testMutexObjects_Case005() throws BaseNamedObjectsException, JavaLaunchException, InterruptedException, ExecutionException, TimeoutException{
		String mutexName = "testMutexObjects_Case005";
		System.out.println(mutexName+"start--------");
		Future<Integer> f1 =
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName,"f1","1000").
				asFuture();
		assertThat(f1.get(), is(0));
		Future<Integer> f2 =
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName,"f2","1","100").
				asFuture();
		assertThat(f2.get(), is(0));
	}
	
	@Test
	public void testMutexObjects_Case006() throws BaseNamedObjectsException, JavaLaunchException, InterruptedException, ExecutionException, TimeoutException{
		String mutexName = "testMutexObjects_Case006";
		System.out.println(mutexName+"start--------");
		Future<Integer> f1 =
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName,"f1","200").
				asFuture();
		Future<Integer> f2 =
				JavaLaunchFactory.
				getInstance().
				java().
				main(WaitForMutexTestMain.class, mutexName,"f2","1","1000").
				asFuture();
		assertThat(f1.get(), is(0));
		assertThat(f2.get(), is(0));
	}
	
	static class WaitForMutexTestMain{
		public static void main(String[] args) throws InterruptedException{
			String mutexName = args[0];
			int sleepTime = 0;
			String processName = "";
			if (args.length>1)
				processName = args[1]+":";
			if (args.length>2) sleepTime = new Integer(args[2]).intValue();
			int waitTime = 100;
			if (args.length>3) waitTime = new Integer(args[3]).intValue();
			MutexRepository repo = MutexRepository.getInstance();
			try {
				Mutex mutex = 
						repo.create(mutexName);
				System.out.println(processName+"open:"+mutexName);
				int ret = mutex.waitFor(waitTime);
				System.out.println(processName+"waitFor:"+mutexName+" ret="+ret);
				TimeUnit.MILLISECONDS.sleep(sleepTime);
				System.out.println(processName+"resume:"+mutexName);
				mutex.release();
				System.out.println(processName+"release:"+mutexName);
				mutex.close();
				System.out.println(processName+"close:"+mutexName);
			}catch (BaseNamedObjectsException e){
				e.printStackTrace();
				System.exit(e.getCode());
			}catch (TimeoutException e){
				e.printStackTrace();
				System.exit(BaseNamedObjectsJni.WAIT_TIMEOUT);
			}
			System.exit(0);
		}
	}
}