package com.tkym.labs.sharedmem.win;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import com.tkym.labs.jvml.JavaLaunchException;
import com.tkym.labs.jvml.JavaLaunchFactory;
import com.tkym.labs.sharedmem.win.NamedMemoryAccesser.NamedMemoryDirect;

public class NamedMemoryFrameworkTest {
	private static final int WHERE = 0;
	private static final int LENGTH = 100;
	private static final int ALLOCATE = 1024;
	
	@Test
	public void testNamedMemoryApiCase001(){
		String name = "testNamedMemoryApiCase001";
		NamedMemoryProvider.
			getInstance().
			register(name, ALLOCATE).
			writable(name).
			lock().
			attach().on(WHERE, LENGTH).
			direct(new NamedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					for (int i=0; i<LENGTH; i++)
						buf.put((byte)(i%10));
				}
			}).
			detach().
			unlock();
		
		NamedMemoryProvider.
			getInstance().
			readonly(name).
			lock().
			attach().
			on(WHERE, LENGTH).
			direct(new NamedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					for (int i=0; i<LENGTH; i++)
						assertThat((int) buf.get(), is(i%10));
				}
			}).
			detach().
			unlock();
	}
	
	@Test
	public void testNamedMemoryFrameworkCase002(){}
	
	@Test
	public void testNamedMemoryApiCase002() throws JavaLaunchException, InterruptedException, ExecutionException{
		String name = "testNamedMemoryApiCase002";
		NamedMemoryProvider.
			getInstance().
			register(name, ALLOCATE).
			writable(name).
			lock().
			attach().on(WHERE, LENGTH).
			direct(new NamedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					for (int i=0; i<LENGTH; i++)
						buf.put((byte)0);
				}
			}).
			detach().
			unlock();
		
		int ret = JavaLaunchFactory.
			getInstance().
			java().
			main(NamedMemoryWriteMain.class, name, "1").asFuture().get();
		
		assertThat(ret, is(0));
		
		NamedMemoryProvider.
			getInstance().
			readonly(name).
			lock().
			attach().
			on(WHERE, LENGTH).
			direct(new NamedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					for (int i=0; i<LENGTH; i++)
						assertThat(buf.get(), is((byte)1));
				}
			}).
			detach().
			unlock();
		
		ret = JavaLaunchFactory.
				getInstance().
				java().
				main(NamedMemoryWriteMain.class, name, "2").asFuture().get();

		assertThat(ret, is(0));
		
		NamedMemoryProvider.
			getInstance().
			readonly(name).
			lock().
			attach().
			on(WHERE, LENGTH).
			direct(new NamedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					for (int i=0; i<LENGTH; i++)
						assertThat(buf.get(), is((byte) 2));
				}
			}).
			detach().
			unlock();
	}
	
	@Test
	public void testNamedMemoryApiCase003() throws JavaLaunchException, InterruptedException, ExecutionException{
		String name = "testNamedMemoryApiCase003";
		NamedMemoryProvider.
			getInstance().
			register(name, ALLOCATE).
			writable(name).
			lock().
			attach().on(WHERE, LENGTH).
			direct(new NamedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					for (int i=0; i<LENGTH; i++)
						buf.put((byte)0);
				}
			}).
			detach().
			unlock();
		
		NamedMemory mem = NamedMemoryProvider.getInstance().readonly(name);
		
		mem.lock();
		
		// Timeout = 10 msec
		int ret = JavaLaunchFactory.
			getInstance().
			java().
			main(NamedMemoryWriteMain.class, name, "1", "10").asFuture().get();
		
		assertThat(ret, is(1)); // timeout
		
		// this lock is wait infinite
		Future<Integer> future = JavaLaunchFactory.
			getInstance().
			java().
			main(NamedMemoryWriteMain.class, name, "1").asFuture();
		
		// sleep 
		TimeUnit.MILLISECONDS.sleep(100);
		
		// read with unlock.
		mem.attach().on(WHERE, LENGTH).direct(new NamedMemoryDirect() {
			@Override
			public void direct(ByteBuffer buf) throws Exception {
				for (int i=0; i<LENGTH; i++)
					assertThat(buf.get(), is((byte)0));
			}
		}).detach();
		
		mem.unlock();
		assertThat(future.get(), is(0)); // success
		
		// read with unlock.
		mem.attach().on(WHERE, LENGTH).direct(new NamedMemoryDirect() {
			@Override
			public void direct(ByteBuffer buf) throws Exception {
				for (int i=0; i<100; i++)
					assertThat(buf.get(), is((byte)1));
			}
		}).detach();
	}
	
	@Test
	public void testMemcache(){
		
		
		
		
	}
	
	static class NamedMemoryWriteMain{
		public static void main(String[] args){
			try {
				final String name = args[0];
				final byte value = new Byte(args[1]).byteValue();
				int timeout = -1;
				if(args.length>2)
					timeout = new Integer(args[2]).intValue();
				NamedMemory mem = NamedMemoryProvider.
					getInstance().
					writable(name);
				
				if (timeout > 0)
					mem.lock(timeout);
				else
					mem.lock();
				
				mem.attach().on(WHERE,LENGTH).direct(new NamedMemoryDirect() {
					@Override
					public void direct(ByteBuffer buf) throws Exception {
						for(int i=0; i<LENGTH; i++)
							buf.put(value);
					}
				}).detach().unlock();
				System.exit(0);
			} catch (TimeoutException e){
				System.exit(1);
			} catch (Exception e) {
				System.exit(-1);
			}
		}
	}
}