package format;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestFormat
{

	@Test
	public void testJson()
	{
		Format format = new Format();

		FormatPart fPart = new FormatPart(0, 0, FormatPageNumber.arabic);

		FormatLevel fLevel = new FormatLevel(0, "Kapitel #R0", FormatPageNumber.roman);
		fPart.addfLevel(fLevel);
		fPart.addfLevel(new FormatLevel(1, "#R0.#a1", FormatPageNumber.roman));
		fPart.addfLevel(new FormatLevel(2, "#R0.#a1,#a2", FormatPageNumber.roman));

		format.fParts.add(fPart);

		FormatPart fPart2 = new FormatPart(1, 15, FormatPageNumber.roman);

		fPart2.addfLevel(new FormatLevel(0, "Bla #a0", FormatPageNumber.roman));

		format.fParts.add(fPart2);

		// FormatLevel
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(fLevel);

			System.out.println(jsonStr);

			FormatLevel fLevel2 = mapper.readValue(jsonStr, FormatLevel.class);

			System.out.println(fLevel2.toFormatString());

		} catch (IOException e)
		{
			e.printStackTrace();
		}

		// FormatPart
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(fPart);

			System.out.println(jsonStr);

			FormatPart fPart3 = mapper.readValue(jsonStr,
					FormatPart.class);

			System.out.println(fPart3.toFormatString());

		} catch (IOException e)
		{
			e.printStackTrace();
		}

		// Format
		try
		{
			ObjectMapper mapper = new ObjectMapper();
			String jsonStr = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(format);

			System.out.println(jsonStr);

			Format format3 = mapper.readValue(jsonStr, Format.class);

			System.out.println(format3.toFormatString());

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

}
