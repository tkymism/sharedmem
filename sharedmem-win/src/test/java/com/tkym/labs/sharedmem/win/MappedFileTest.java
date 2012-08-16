package com.tkym.labs.sharedmem.win;

import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_READ;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.FILE_MAP_WRITE;
import static com.tkym.labs.sharedmem.win.BaseNamedObjectsJni.PAGE_READWRITE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.tkym.labs.jvml.JavaLaunchErrorHandle;
import com.tkym.labs.jvml.JavaLaunchFactory;
import com.tkym.labs.sharedmem.win.BaseNamedObjectsJni;
import com.tkym.labs.sharedmem.win.FileMap;
import com.tkym.labs.sharedmem.win.BaseNamedObjectsException;
import com.tkym.labs.sharedmem.win.FileMapRepository;
import com.tkym.labs.sharedmem.win.FileMapView;
import com.tkym.labs.sharedmem.win.FileMapRepository.NamedMapNotExistsException;

public class MappedFileTest {
	@Test
	public void testMemoryMappedFile_Case001(){
		BaseNamedObjectsJni.getInstance();
	}
	
	@Test
	public void testMemoryMappedFile_Case002(){
		BaseNamedObjectsJni sm = BaseNamedObjectsJni.getInstance();
		int hC = sm.createFileMapping("memcache_Case002", PAGE_READWRITE, 0, 1024);
		int hO = sm.openFileMapping("memcache_Case002", FILE_MAP_READ);
		// access OK.
		int aC = sm.mapViewOfFile(hC, FILE_MAP_WRITE);
		assertTrue(aC > 0);
		sm.unmapViewOfFile(hO);
		sm.closeHandle(hC);
		
		// access fail.
		int aO = sm.mapViewOfFile(hO, FILE_MAP_WRITE);
		assertThat(aO, is(-5));
		sm.unmapViewOfFile(aO);
		sm.closeHandle(hO);
	}
	
	@Test
	public void testNameSharedMemoryProvider_Case003() throws BaseNamedObjectsException {
		FileMapRepository repo = FileMapRepository.getInstance();
		FileMap memory = repo.create("Local\\aaaMemory", 1024);
		FileMapView view = memory.mapAsWrite();
		assertThat(view.read(0), is((byte)0));
		byte[] array = "data hogehoge".getBytes();
		view.write(0, array);
		assertThat(view.read(0, array.length), is(array));
		view.unmap();
		memory.close();
	}
	
	@Test
	@Ignore
	public void testNameSharedMemoryProvider_Case004() throws BaseNamedObjectsException {
		String namespace = "testNameSharedMemoryProvider_Case004";
		FileMapRepository repo = FileMapRepository.getInstance();
		FileMap memory = repo.create(namespace, 1024);
		FileMapView view = memory.mapAsWrite();
		assertThat(view.read(0), is((byte)0));
		byte[] array = "data hogehoge".getBytes();
		view.write(0, array);
		assertThat(view.read(0, array.length), is(array));
		view.unmap();
		view = memory.mapAsRead();
		assertThat(view.read(0, array.length), is(array));
		view.unmap();
		memory.close();
	}
	
	@Test
	@Ignore
	public void testNameSharedMemoryProvider_Case005() throws BaseNamedObjectsException, InterruptedException {
		String namespace = "testNameSharedMemoryProvider_Case005";
		FileMapRepository repo = FileMapRepository.getInstance();
		
		FileMap mem1 = repo.create(namespace, 1024);
		FileMapView v1 = mem1.mapAsWrite();
		assertThat(v1.read(0), is((byte)0));
		byte[] array = "data hogehoge".getBytes();
		v1.write(0, array);
		assertThat(v1.read(0, array.length), is(array));
		
		FileMap mem2 = repo.openAsCopy(namespace);
		FileMapView v2 = mem2.mapOnCopy();
		assertThat(v2.read(0, array.length), is(array));
		
		v1.unmap();
		mem1.close();
		v2.unmap();
		mem2.close();
	}
	
	@Test
	@Ignore
	public void testNameSharedMemoryProvider_ErrorCase001() throws BaseNamedObjectsException{
		String namespace = "testNameSharedMemoryProvider_ErrorCase001";
		FileMapRepository repo = FileMapRepository.getInstance();
		FileMap mem1 = null;
		FileMap mem2 = null;
		
		mem1 = repo.create(namespace, 1024);
		try {
			mem2 = repo.create(namespace, 1024);
		} catch (BaseNamedObjectsException e) {
			assertThat(e.getCode(), is(183));
			assertThat(e.getMessage(), is("name[testNameSharedMemoryProvider_ErrorCase001 ] is already exists."));
		} finally {
			if (mem2 != null) mem2.close(); 
		}
		if (mem1 != null) mem1.close();
	}
	
	@Test
	@Ignore
	public void testMappedFile_Case001() throws BaseNamedObjectsException{
		String name = "testMappedFile_Case001";
		String str = "abcde";
		FileMap m1 = 
				FileMapRepository.
				getInstance().
				create(name, 1024);
		FileMapView v1 = m1.mapAsWrite();
		v1.createByteBuffer(0, 1024).put(str.getBytes());
		v1.unmap();
		FileMap m2 = 
				FileMapRepository.
				getInstance().
				openAsRead(name);
		FileMapView v2 = m2.mapAsRead();
		byte[] dst = new byte[str.getBytes().length];
		v2.createByteBuffer(0, 1024).get(dst);
		v2.unmap();
		String dstStr = new String(dst);
		System.out.println(dstStr);
		m2.close();
		m1.close();
		assertThat(dstStr, is(str));
	}
	
	@Test
	@Ignore
	public void testNameSharedMemory_Case005() throws BaseNamedObjectsException{
		String namespace = "testNameSharedMemory_Case005()";
		FileMapRepository repo = FileMapRepository.getInstance();
		String str = craeteStringData();
		byte[] data = str.getBytes();
		FileMap mem1 = repo.create(namespace, data.length);
		FileMapView view = mem1.mapAsWrite();
		view.write(0, data);
		byte[] byte2 = view.read(0, data.length);
		String str2 = new String(byte2);
		System.out.println(str2);
		assertThat(str2, is(str));
		JavaLaunchFactory.getInstance().
			java().
			main(BaseNamedObjectsMain.class, namespace, "90000").
			exec(new JavaLaunchErrorHandle() {
				@Override
				public void onFail(int error) {
					Assert.fail();
				}
		});
		view.unmap();
		mem1.close();
	}
	
	@Test
	@Ignore
	public void testNameSharedMemory_Case006() throws BaseNamedObjectsException{
		String namespace = "testNameSharedMemory_Case006()";
		FileMapRepository repo = FileMapRepository.getInstance();
		try {
			repo.openAsRead(namespace);
			Assert.fail();
		} catch (NamedMapNotExistsException e) {
			Assert.assertThat(e.getCode(), is(2));
		}
	}
	
	@Test
	@Ignore
	public void testSpeedCheckOfRead() throws BaseNamedObjectsException{
		String namespace = "testSpeedCheck()";
		FileMapRepository repo = FileMapRepository.getInstance();
		String str = craeteStringData();
		byte[] data = str.getBytes();
		assertThat(data.length, is(90000));
		FileMap mem1 = repo.create(namespace, data.length);
		FileMapView view = mem1.mapAsWrite();
		view.write(0, data);
		int count = 100;
		long start = System.nanoTime();
		for (int i=0; i<count; i++){
			view.read(0, data.length);
		}
		long end = System.nanoTime();
		String str2 = new String(view.read(0, data.length));
		assertThat(str2, is(str));
		System.out.println("lap time:"+(float)(end-start)/count/1000/1000+"[msec]");
		view.unmap();
		mem1.close();
	}
	
	@Test
	@Ignore
	public void testNameSharedMemory_Case008() throws BaseNamedObjectsException{
		String namespace = "testNameSharedMemory_Case008()";
		FileMapRepository repo = FileMapRepository.getInstance();
		FileMap obj = repo.create(namespace, 1000);
		FileMapView view = obj.mapAsWrite();
		ByteBuffer buffer = view.createByteBuffer(0, 100);
		buffer.
			putLong(Long.MAX_VALUE).
			putLong(Long.MIN_VALUE).
			putLong(0L);
		view.unmap();
		buffer = null;
		FileMapView view2 = obj.mapAsRead();
		ByteBuffer buffer2 = view2.createByteBuffer(0, 100);
		assertThat(buffer2.getLong(), is(Long.MAX_VALUE));
		assertThat(buffer2.position(), is(8));
		assertThat(buffer2.getLong(), is(Long.MIN_VALUE));
		assertThat(buffer2.position(), is(16));
		assertThat(buffer2.getLong(), is(0L));
		assertThat(buffer2.position(), is(24));
		view2.unmap();
		buffer2 = null;
		obj.close();
	}
	
	@Test
	public void testNameSharedMemory_Case009() throws BaseNamedObjectsException{
		String namespace = "testNameSharedMemory_Case009()";
		FileMapRepository repo = FileMapRepository.getInstance();
		FileMap obj = repo.create(namespace, 1000);
		int handleOfCreate = obj.getHandle();
		FileMap write = repo.open(namespace, BaseNamedObjectsJni.FILE_MAP_WRITE);
		int handleOfWrite = write.getHandle();
		assertTrue(handleOfCreate != handleOfWrite);
		write.close();
		obj.close();
	}
	
	static String craeteStringData(){
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<1000; i++)
			sb.append("あいうえお、かきくけこ、さしすせそ、たちつてと、なにぬねの、");
		return sb.toString();
	}
	
	static class BaseNamedObjectsMain{
		public static void main(String[] args) throws BaseNamedObjectsException {
			FileMapRepository repo = FileMapRepository.getInstance();
			FileMap mem1 = repo.openAsRead(args[0]);
			FileMapView view = mem1.mapAsRead();
			byte[] array = view.read(0, new Integer(args[1]).intValue());
			String str = new String(array, Charset.forName("UTF-8"));
			view.unmap();
			mem1.close();
			if (!str.equals(craeteStringData())){
				System.out.println(str);
				System.exit(-1);
			}
		}
	}
}