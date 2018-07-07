package util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

public class Annotation
{

	public static void main(String[] args) throws IOException
	{
		if(args.length < 1){
			System.out.println("outDir PDF1 [PDF2 ...]");
			return;
		}
		File outDir = new File(args[0]);
		
		System.out.println("===Remove Annotations=============================================");
		for(int i=1; i<args.length; ++i){
			
			File pdfFile =  new File(args[i]);
			
			System.out.println(pdfFile.getName());
			
			File outFile = new File(outDir, pdfFile.getName());
			
			if(outFile.exists()){
				System.out.println("output file " + outFile.getAbsolutePath() + " already exists");
				continue;
			}
			
			PDDocument document = PDDocument.load(pdfFile);
			
			// remove annotations
			PDPageTree pages = document.getPages();
			
			Iterator<PDPage> iter = pages.iterator();
			while(iter.hasNext()){
				PDPage page = iter.next();
				
				page.setAnnotations(null);
			}
			
			// remove embedded files
			document.getDocumentCatalog().setNames(null);
			
			// save
			document.save(outFile);
			
			document.close();
		}
	}
}
