package org.bouncycastle.gpg.keybox;

import java.io.IOException;
import java.security.Security;
import java.util.Arrays;

import junit.framework.TestCase;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.test.SimpleTest;

public class KeyBoxByteBufferTest
    extends SimpleTest
{

    public static void main(
        String[] args)
    {
        Security.addProvider(new BouncyCastleProvider());
        runTest(new KeyBoxByteBufferTest());
    }

    public void testReadPastEnd()
        throws Exception
    {
        byte[] expected = new byte[]{1, 2, 3};

        KeyBoxByteBuffer buf = KeyBoxByteBuffer.wrap(expected);
        try
        {
            buf.bN(4);
            fail("Would be past end.");
        }
        catch (IllegalArgumentException ilex)
        {

        }

        try
        {
            buf.bN(-1);
            TestCase.fail("Would be past end.");
        }
        catch (IllegalArgumentException ilex)
        {

        }

        isEquals(0, buf.bN(0).length);
        byte[] b = buf.bN(3);
        isEquals("Length", b.length, 3);
        isTrue("Wrong values", Arrays.equals(b, expected));
    }


    public void testRangeReadPastEnd()
        throws Exception
    {
        byte[] expected = new byte[]{1, 2, 3, 4, 5};

        KeyBoxByteBuffer buf = KeyBoxByteBuffer.wrap(expected);
        try
        {
            buf.rangeOf(3, 6);
            fail("Would be past end.");
        }
        catch (IllegalArgumentException ilex)
        {

        }

        try
        {
            buf.rangeOf(1, -3);
            fail("End is negative");
        }
        catch (IllegalArgumentException ilex)
        {

        }

        try
        {
            buf.rangeOf(4, 3);
            fail("End is less than start");
        }
        catch (IllegalArgumentException ilex)
        {

        }

        isEquals(0, buf.rangeOf(1, 1).length);
        byte[] b = buf.rangeOf(1, 3);
        isEquals("wrong length", 2, b.length);
        isTrue("Wrong values", Arrays.equals(b, new byte[]{2, 3}));
    }

    public void testConsumeReadPastEnd()
        throws Exception
    {
        KeyBoxByteBuffer buf = KeyBoxByteBuffer.wrap(new byte[4]);
        buf.consume(3);
        isEquals(-16, buf.size());
        try
        {
            buf.consume(2);
            TestCase.fail("consume past end of buffer");
        }
        catch (IllegalArgumentException ilex)
        {

        }
        buf.position(0);
    }

    public void testExceptions()
        throws IOException
    {
        testException("Could not convert ", "IllegalStateException", () -> KeyBoxByteBuffer.wrap(new Object()));
        final KeyBoxByteBuffer buf = KeyBoxByteBuffer.wrap(new byte[4]);
        testException("invalid range ", "IllegalArgumentException", () -> buf.rangeOf(-1, 2));

        testException("range exceeds buffer remaining", "IllegalArgumentException", () -> buf.rangeOf(0, 24));

        testException("size exceeds buffer remaining", "IllegalArgumentException", () -> buf.consume(buf.remaining() + 1));

        testException("size less than 0", "IllegalArgumentException", () -> buf.consume(buf.size()));

        testException("size exceeds buffer remaining", "IllegalArgumentException", () -> {
            KeyBoxByteBuffer buf1 = KeyBoxByteBuffer.wrap(new byte[21]);
            buf1.consume(buf1.getBuffer().remaining() + 1);
        });

    }

    @Override
    public String getName()
    {
        return "KeyBoxBuffer";
    }

    @Override
    public void performTest()
        throws Exception
    {
        testConsumeReadPastEnd();
        testRangeReadPastEnd();
        testReadPastEnd();
        testExceptions();
    }
}
