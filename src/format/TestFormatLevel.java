package format;

import static org.junit.Assert.fail;

import org.junit.Test;

import data.EntryNumber;

public class TestFormatLevel
{
	
	@Test
	public void testMatch()
	{
		// Test FormatLevel
		String pattern = "Kapitel #R0.#a1:#R2";
		System.out.println("Pattern: " + pattern);
		FormatEntryNumber np = new FormatEntryNumber(pattern);

		boolean failed = false;
		
		System.out.println("------------------");
		String testStr = "Kapitel  II.60 asdfqwer";
		System.out.println("Test str:" + testStr);
		EntryNumber en = np.match(testStr, 0);
		if (en != null)
		{
			System.err.println(en.toString());
			System.err.println(en.toFormatedString());
			fail("should not match");
			failed = true;
		} else
		{
			System.out.println("Did not match");
		}

		System.out.println("------------------");
		testStr = "Kapitel II.60:IV asdfqwer";
		System.out.println("Test str:" + testStr);
		en = np.match(testStr, 0);
		if (en != null)
		{
			System.out.println(en.toString());
			System.out.println(en.toFormatedString());
			System.out.println(en.getOrig() + "|"
					+ testStr.substring(en.getOrig().length()).trim());
		} else
		{
			System.err.println("Did not match");
			fail("should match");
			failed = true;
		}

		System.out.println("------------------");
		testStr = "Kapitel  II.60:IV ";
		System.out.println("Test str:" + testStr);
		en = np.match(testStr, 0);
		if (en != null)
		{
			System.out.println(en.toString());
			System.out.println(en.toFormatedString());
		} else
		{
			System.err.println("Did not match");
			System.err.println(np.toRegExp());
			fail("should match");
			failed = true;
		}

		// empty pattern
		pattern = "";
		System.out.println("Pattern: " + pattern);
		np = new FormatEntryNumber(pattern);

		System.out.println("------------------");
		testStr = "Hallo";
		System.out.println("Test str:" + testStr);
		en = np.match(testStr, 0);
		if (en != null)
		{
			System.out.println(en.toString());
			System.out.println(en.toFormatedString());
			System.out.println(en.getOrig() + "|"
					+ testStr.substring(en.getOrig().length()).trim());
		} else
		{
			System.err.println("Did not match");
			fail("should match");
			failed = true;
		}

		// optional pattern
		pattern = "[AB]? #R0.#a1.#a2";
		System.out.println("Pattern: " + pattern);
		np = new FormatEntryNumber(pattern);

		System.out.println("------------------");
		testStr = "A I.1.1 Hallo";
		System.out.println("Test str:" + testStr);
		en = np.match(testStr, 0);
		if (en != null)
		{
			System.out.println(en.toString());
			System.out.println(en.toFormatedString());
			System.out.println(en.getOrig() + "|"
					+ testStr.substring(en.getOrig().length()).trim());
		} else
		{
			System.err.println("Did not match");
			fail("should match");
			failed = true;
		}

		System.out.println("------------------");
		testStr = "B     I.1.1   Hallo";
		System.out.println("Test str:" + testStr);
		en = np.match(testStr, 0);
		if (en != null)
		{
			System.out.println(en.toString());
			System.out.println(en.toFormatedString());
			System.out.println(en.getOrig() + "|"
					+ testStr.substring(en.getOrig().length()).trim());
		} else
		{
			System.err.println("Did not match");
			fail("should match");
			failed = true;
		}
		
		if(failed)
			fail("some test failed");
	}
}
