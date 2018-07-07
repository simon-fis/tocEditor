package format;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestFormatPart
{
	@Test
	public void testJson()
	{
		FormatPart fPart = new FormatPart(0, 0, FormatPageNumber.roman);
		
		fPart.addfLevel(new FormatLevel(0, "Kapitel #R0", FormatPageNumber.roman));
		fPart.addfLevel(new FormatLevel(1, "#R0.#a1", FormatPageNumber.roman));
		fPart.addfLevel(new FormatLevel(2, "#R0.#a1,#a2", FormatPageNumber.roman));
//		fail("Not yet implemented");
		
		ObjectMapper mapper = new ObjectMapper();
		
		try
		{
			String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(fPart);
			
			System.out.println(jsonStr);
			
			FormatPart fPart2 = mapper.readValue(jsonStr, FormatPart.class);
			
			System.out.println(fPart2);
			
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
