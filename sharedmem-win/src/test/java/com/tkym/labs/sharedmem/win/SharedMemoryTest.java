package com.tkym.labs.sharedmem.win;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tkym.labs.jvml.JavaLaunchFactory;
import com.tkym.labs.sharedmem.win.BaseNamedObjectsException;
import com.tkym.labs.sharedmem.win.SharedMemoryLocator;
import com.tkym.labs.sharedmem.win.SharedMemoryRepository;
import com.tkym.labs.sharedmem.win.SharedMemoryAccesser.SharedMemoryDirectAttach;
import com.tkym.labs.sharedmem.win.SharedMemoryAccesser.SharedMemoryReader;
import com.tkym.labs.sharedmem.win.SharedMemoryAccesser.SharedMemoryWriter;

public class SharedMemoryTest {
	private static String TEST_STR;
	
	@BeforeClass
	public static void setupClass(){
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<100; i++)
			sb.append("abcde");
		TEST_STR = sb.toString();
	}
	
	static class CreateFileMapMain{
		private static String data(){
			StringBuilder sb = new StringBuilder();
			for(int i=0; i<100; i++)
				sb.append("abcde");
			return sb.toString();
		}
		public static void main(String[] args) throws InterruptedException, BaseNamedObjectsException, IOException, TimeoutException{
			String name = args[0];
			int sleepTime = 0;
			if (args.length>1) sleepTime = new Integer(args[1]).intValue();
			final byte[] data = data().getBytes();
			SharedMemoryRepository.
				getInstance().
				allocate(name, data.length+100).
				locate(0, 100).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer buf) {
						buf.putInt(0);
						buf.putLong(1L);
						buf.putLong(100L);
					}
				}).
				locate(100, data.length).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer buf) {
						buf.put(data);
					}
				}).release();
			TimeUnit.MILLISECONDS.sleep(sleepTime);
			SharedMemoryRepository.getInstance().deallocate(name);
			System.exit(0);
		}
	}

	@Test
	public void testSharedMemoryCase001() throws IOException, BaseNamedObjectsException, TimeoutException, InterruptedException, ExecutionException{
		String name = "testSharedMemoryCase001()";
		final byte[] data = TEST_STR.getBytes();
		SharedMemoryRepository.
				getInstance().
				allocate(name, data.length+100).
				locate(0,100).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer buf) {
						buf.putInt(0);
						buf.putLong(1L);
						buf.putLong(100L);
					}
				}).
				locate(100,data.length).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer buf) {
						buf.put(data);
					}
				}).
				release();
		boolean exists = 
				SharedMemoryRepository.
				getInstance().
				exists(name);
		assertTrue(exists);
		SharedMemoryRepository.
				getInstance().
				readonly(name).
				locate(0, 100).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer b1){
						assertThat(b1.getInt(), is(0));
						assertThat(b1.getLong(), is(1L));
						assertThat(b1.getLong(), is(100L));
					}
				}).
				locate(100, data.length).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer b2){
						byte[] dst = new byte[data.length];
						b2.get(dst);
						assertThat(dst, is(data));
					}
				}).
				release();
	}
	
	@Test
	public void testSharedMemoryCase002() throws IOException, BaseNamedObjectsException, TimeoutException, InterruptedException, ExecutionException{
		String name = "testSharedMemoryCase002()";
		byte[] data = TEST_STR.getBytes();
		Future<Integer> future = 
				JavaLaunchFactory.getInstance().
				java().
				main(CreateFileMapMain.class, name,"1000").
				asFuture();
		TimeUnit.MILLISECONDS.sleep(300);
		boolean exists = 
				SharedMemoryRepository.
				getInstance().
				exists(name);
		assertTrue(exists);
		System.out.println("exsits!!");
		SharedMemoryLocator v = 
				SharedMemoryRepository.
				getInstance().
				readonly(name);
		ByteBuffer b1 = v.bytebuffer(0, 100);
		assertThat(b1.getInt(), is(0));
		assertThat(b1.getLong(), is(1L));
		assertThat(b1.getLong(), is(100L));
		ByteBuffer b2 = v.bytebuffer(100, data.length);
		byte[] dst = new byte[data.length];
		b2.get(dst);
		assertThat(dst, is(data));
		v.release();
		assertThat(future.get(),is(0));
	}

	@Test
	public void testSharedMemoryCase003() throws IOException, BaseNamedObjectsException, TimeoutException, InterruptedException, ExecutionException{
		String name = "testSharedMemoryCase003";
		SharedMemoryRepository.
				getInstance().
				allocate(name, 1024).
				locate(0,1024).
				write(new SharedMemoryWriter(){
					@Override
					public void write(OutputStream os) throws Exception{
						ObjectOutputStream oos = new ObjectOutputStream(os);
						oos.writeObject(TEST_STR);
						oos.close();
					}
				}).
				release();
		
		boolean exists = 
				SharedMemoryRepository.
				getInstance().
				exists(name);
		assertTrue(exists);
		
		long start = System.nanoTime();
		SharedMemoryRepository.
				getInstance().
				readonly(name).
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
				release();
		long end = System.nanoTime();
		System.out.println((end-start)+"[nanosec]");
	}
	
	@Test
	public void testSharedMemoryCase004() throws IOException, BaseNamedObjectsException, TimeoutException, InterruptedException, ExecutionException{
		BufferedImage image = ImageIO.read(SharedMemoryTest.class.getResource("IMG_0106.JPG"));
		final ImageIcon icon = new ImageIcon(image);
		final byte[] data = serialize(icon);
		String name = "testSharedMemoryCase004";
		SharedMemoryRepository.
				getInstance().
				allocate(name, data.length).
				locate(0,data.length).
				direct(new SharedMemoryDirectAttach() {
					@Override
					public void attach(ByteBuffer buf) throws Exception {
						buf.put(data);
					}
				}).
				release();
		System.out.println("succeess write");
		boolean exists = 
				SharedMemoryRepository.
				getInstance().
				exists(name);
		assertTrue(exists);
		
		long start = System.currentTimeMillis();
		SharedMemoryRepository.
				getInstance().
				readonly(name).
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
				release();
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