package com.tkym.labs.sharedmem.win;

import java.nio.ByteBuffer;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class BaseNamedObjectJniTest {
	@Test
	public void testLongSeparate001(){
		ByteBuffer buffer = ByteBuffer.allocate(100);
		buffer.putLong(Long.MAX_VALUE);
		Assert.assertThat(buffer.position(), CoreMatchers.is(8));
		buffer.position(0);
		int upper = buffer.getInt();
		int lower = buffer.getInt();
		System.out.println(upper+":"+lower);
	}
	
	static void toByteArray(long value){
		
	}
}
