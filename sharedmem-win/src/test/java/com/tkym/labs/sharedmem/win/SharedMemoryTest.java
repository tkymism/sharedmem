package com.tkym.labs.sharedmem.win;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.tkym.labs.jvml.JavaLaunchFactory;
import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryAccesser.SharedMemoryDirect;
import com.tkym.labs.sharedmem.api.SharedMemoryRepository;

public class SharedMemoryTest {
	private static String TEST_STR;
	
	@BeforeClass
	public static void setupClass() throws InstantiationException, IllegalAccessException{
		SharedMemoryRepository.registerFactory(SharedMemoryFactoryWin.class);
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<100; i++)
			sb.append("abcde");
		TEST_STR = sb.toString();
	}
	
	@Test
	public void testShareMemoryLock_Case001(){
		SharedMemory sm = 
				SharedMemoryRepository.
				getInstance().
				get("testShareMemoryLock_Case001");
		sm.lock().infinity();
		assertTrue(sm.lock().isLocked());
		sm.lock().release();
		assertFalse(sm.lock().isLocked());
	}
	
	@Test
	public void testShareMemoryLock_Case002() throws InterruptedException, ExecutionException{
		Callable<Integer> locktask = new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				System.out.println("["+Thread.currentThread().getId()+" start]");
				try {
					SharedMemory sm = 
							SharedMemoryRepository.
							getInstance().
							get("testShareMemoryLock_Case002");
					sm.lock().infinity();
					sm.lock().release();
					System.out.println("["+Thread.currentThread().getId()+" end]");
					return 0;
				} catch (Exception e) {
					e.printStackTrace();
					return -1;
				}
			}
		};
		ExecutorService service = Executors.newCachedThreadPool();
		List<Future<Integer>> futures = new ArrayList<Future<Integer>>();
		for (int i=0; i<100; i++)
			futures.add(service.submit(locktask));
		for (Future<Integer> f: futures)
			assertThat(f.get(), is(0));
	}
	
	@Test
	public void testSharedMemoryCase002() throws IOException, TimeoutException, InterruptedException, ExecutionException{
		String name = "testSharedMemoryCase002()";
		byte[] data = TEST_STR.getBytes();

		Future<Integer> future = 
				JavaLaunchFactory.getInstance().
				java().
				main(CreateFileMapMain.class, name,"1000").
				asFuture();
		
		TimeUnit.MILLISECONDS.sleep(500);
		
		boolean exists = 
				SharedMemoryRepository.
				getInstance().
				get(name).
				lock().infinity().
				exists();
		
		if (!exists){
			SharedMemoryRepository.
				getInstance().
				get(name).
				unlock();
			fail();
		}
		
		SharedMemoryLocatorImpl v = 
				(SharedMemoryLocatorImpl) 
				SharedMemoryRepository.
				getInstance().
				get(name).
				readonly();
		
		ByteBuffer b1 = v.bytebuffer(0, 100);
		assertThat(b1.getInt(), is(0));
		assertThat(b1.getLong(), is(1L));
		assertThat(b1.getLong(), is(100L));
		ByteBuffer b2 = v.bytebuffer(100, data.length);
		byte[] dst = new byte[data.length];
		b2.get(dst);
		assertThat(dst, is(data));
		v.detach();
		assertThat(future.get(),is(0));
	}
	
	static class CreateFileMapMain{
		private static String data(){
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<100; i++)
				sb.append("abcde");
			return sb.toString();
		}
		public static void main(String[] args) throws InterruptedException, IOException, TimeoutException, InstantiationException, IllegalAccessException{
			SharedMemoryRepository.
				registerFactory(SharedMemoryFactoryWin.class);
			String name = args[0];
			int sleepTime = 0;
			if (args.length>1) sleepTime = new Integer(args[1]).intValue();
			final byte[] data = data().getBytes();
			SharedMemoryRepository.
				getInstance().
				get(name).
				lock().until(1000).
				allocate(data.length+100).
				locate(0, 100).
				direct(new SharedMemoryDirect() {
					@Override
					public void direct(ByteBuffer buf) {
						buf.putInt(0);
						buf.putLong(1L);
						buf.putLong(100L);
					}
				}).
				locate(100, data.length).
				direct(new SharedMemoryDirect() {
					@Override
					public void direct(ByteBuffer buf) {
						buf.put(data);
					}
				}).
				detach().
				lock().release();
			TimeUnit.MILLISECONDS.sleep(sleepTime);
			System.exit(0);
		}
	}

}
