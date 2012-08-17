package com.tkym.labs.sharedmem;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tkym.labs.sharedmem.api.SharedMemory;
import com.tkym.labs.sharedmem.api.SharedMemoryAccesser.SharedMemoryDirect;
import com.tkym.labs.sharedmem.api.SharedMemoryAccesser.SharedMemoryReader;
import com.tkym.labs.sharedmem.api.SharedMemoryAccesser.SharedMemoryWriter;
import com.tkym.labs.sharedmem.api.SharedMemoryRepository;
import com.tkym.labs.sharedmem.win.SharedMemoryFactoryWin;

public class SharedMemoryTest {
	private static String TEST_STR;
	
	@BeforeClass
	public static void setupClass(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<100; i++)
			sb.append("abcde");
		TEST_STR = sb.toString();
		SharedMemoryRepository.
			registerFactory(SharedMemoryFactoryWin.class);
	}
	
	static class CreateFileMapMain{
		private static String data(){
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<100; i++)
				sb.append("abcde");
			return sb.toString();
		}
		public static void main(String[] args) throws InterruptedException, IOException, TimeoutException{
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

	@Test
	public void testSharedMemoryCase001() throws IOException, TimeoutException, InterruptedException, ExecutionException{
		String name = "testSharedMemoryCase001()";
		final byte[] data = TEST_STR.getBytes();
		
		SharedMemoryRepository.
				getInstance().
				get(name).
				lock().infinity().
				allocate(data.length+100).
				locate(0,100).
				direct(new SharedMemoryDirect() {
					@Override
					public void direct(ByteBuffer buf) {
						buf.putInt(0);
						buf.putLong(1L);
						buf.putLong(100L);
					}
				}).
				locate(100,data.length).
				direct(new SharedMemoryDirect() {
					@Override
					public void direct(ByteBuffer buf) {
						buf.put(data);
					}
				}).
				detach().
				unlock();
		
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
		
		SharedMemoryRepository.
				getInstance().
				get(name).readonly().
				locate(0, 100).
				direct(new SharedMemoryDirect() {
					@Override
					public void direct(ByteBuffer b1){
						assertThat(b1.getInt(), is(0));
						assertThat(b1.getLong(), is(1L));
						assertThat(b1.getLong(), is(100L));
					}
				}).
				locate(100, data.length).
				direct(new SharedMemoryDirect() {
					@Override
					public void direct(ByteBuffer b2){
						byte[] dst = new byte[data.length];
						b2.get(dst);
						assertThat(dst, is(data));
					}
				}).
				detach().unlock();
	}

	@Test
	public void testSharedMemoryCase003() throws IOException, TimeoutException, InterruptedException, ExecutionException{
		String name = "testSharedMemoryCase003";
		SharedMemoryRepository.
				getInstance().
				get(name).
				lock().until(1000).
				allocate(1024).
				locate(0,1024).
				write(new SharedMemoryWriter(){
					@Override
					public void write(OutputStream os) throws Exception{
						ObjectOutputStream oos = new ObjectOutputStream(os);
						oos.writeObject(TEST_STR);
						oos.close();
					}
				}).
				detach().
				unlock();
		
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
			
		long start = System.nanoTime();
		
		SharedMemoryRepository.
				getInstance().
				get(name).
				readonly().
				locate(0, 1024).
				read(new SharedMemoryReader() {
					@Override
					public void read(InputStream is) throws Exception{
						ObjectInputStream ois = new ObjectInputStream(is);
						Object obj = ois.readObject();
						String data2 = (String) obj;
						ois.close();
						Assert.assertThat(data2, is(TEST_STR));
					}
				}).
				detach().
				unlock();

		long end = System.nanoTime();
		System.out.println((end-start)+"[nanosec]");
	}
	
	@Test
	public void testSharedMemoryCase004() throws IOException, TimeoutException, InterruptedException, ExecutionException{
		BufferedImage image = ImageIO.read(SharedMemoryTest.class.getResource("IMG_0106.JPG"));
		final ImageIcon icon = new ImageIcon(image);
		final byte[] data = serialize(icon);
		String name = "testSharedMemoryCase004";
		SharedMemory sm = 
				SharedMemoryRepository.
				getInstance().
				get(name);
		
		sm.lock().infinity().
			allocate(data.length).
			locate(0,data.length).
			direct(new SharedMemoryDirect() {
				@Override
				public void direct(ByteBuffer buf) throws Exception {
					buf.put(data);
				}
			}).
			detach().
			unlock();
		
		if (!sm.lock().
				infinity().
				exists()){
			sm.unlock();
			fail();
		}

		long start = System.currentTimeMillis();
				
		sm.readonly().
			locate(0, data.length).
			read(new SharedMemoryReader() {
				@Override
				public void read(InputStream is) throws Exception{
					ObjectInputStream ois = new ObjectInputStream(is);
					Object obj = ois.readObject();
					assertTrue(obj instanceof ImageIcon);
					ois.close();
				}
			}).
			detach().
			unlock();
		long end = System.currentTimeMillis();
		System.out.println((end-start)+"[msec]");
	}
	
	static byte[] serialize(Object obj) throws IOException{
		ByteArrayOutputStream bis = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bis);
		oos.writeObject(obj);
		byte[] ret = bis.toByteArray();
		oos.close();
		bis.close();
		return ret;
	}
}