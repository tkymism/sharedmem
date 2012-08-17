package com.tkym.labs.sharedmem.tool;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tkym.labs.jvml.JavaLaunchFactory;
import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryAccesser.SharedMemoryDirect;
import com.tkym.labs.sharedmem.api.SharedMemoryException;
import com.tkym.labs.sharedmem.api.SharedMemoryRepository;
import com.tkym.labs.sharedmem.win.SharedMemoryFactoryWin;

public class SharedMemoryNotifierTest {
	@BeforeClass
	public static void setupClass(){
		SharedMemoryRepository.registerFactory(SharedMemoryFactoryWin.class);
	}
	
	@Test
	public void testNotifierCase001() throws InterruptedException, ExecutionException{
		String name = "testNotifierCase001";
		// allocate
		SharedMemory sm = 
				SharedMemoryRepository.
				getInstance().
				get(name);

		// allocate
		SharedMemoryExistsWatchTask watch = 
				new SharedMemoryExistsWatchTask(
						sm, 100, TimeUnit.MILLISECONDS); 
		
		// watch thread start.
		Future<SharedMemory> watched = 
				Executors.newSingleThreadExecutor().
				submit(watch);
		
		// shared memory write.
		sm.lock().infinity().
			allocate(4).
			locate(0,4).
			direct(new SharedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					buf.putInt(1);
				}
			}).
			detach().
			unlock();
		
		// watched shared memory
		SharedMemory sm2 = watched.get();
		
		// is locked
		assertTrue(sm2.lock().isLocked());
		
		// read shared memory.
		sm2.readonly().locate(0,4).direct(new SharedMemoryDirect() {
			@Override
			public void direct(ByteBuffer buf) throws Exception {
				assertThat(buf.getInt(), is(1));
			}
		}).detach().unlock();
	}
	
	class SharedMemoryExistsWatchTask implements Callable<SharedMemory>{
		private final SharedMemory sharedMemory;
		private final long delay;
		private final TimeUnit unit;
		private int waitTimeout = 1000;
		
		SharedMemoryExistsWatchTask(SharedMemory sharedMemory, long delay, TimeUnit unit){
			this.sharedMemory = sharedMemory;
			this.delay = delay;
			this.unit = unit;
		}
		
		@Override
		public SharedMemory call() throws Exception {
			while(true){
				
				boolean exist = 
						sharedMemory.
							lock().until(waitTimeout).
							exists();
				
				if (exist)
					return sharedMemory;
				
				sharedMemory.unlock();
				
				unit.sleep(delay);
			}
		}
	}
	
	static class ChildProcessMain{
		public static void main(String[] args) {
			SharedMemoryRepository.registerFactory(SharedMemoryFactoryWin.class);
			String name = args[0];
			
			SharedMemory sm = 
					SharedMemoryRepository.
					getInstance().
					get(name);
			
		}
	}
}
