===============================================================================
TOC EDITOR
===============================================================================

This is a tool to help you creating bookmarks and page labels for an e-book in the pdf format. Maybe the easiest way to explain what this program does (helps you to do) can be seen at the example "testBook.pdf". The testBook.pdf do not contain any bookmarks and page labels, so working with this pdf is not very comfortable.

For example if you want to go to section 2.3 you have to scroll to the table of content on page 3, search entry "2.3 Test Section". There you see that this section starts at page 7. Than you can scroll to page 7. If you have bookmarks you just have to click on the "2.3 Test Section" in the bookmark panel (which is typically on the left hand side of most of the pdf viewers) and the viewer jumps to the right page.

Another example, if you want to find some special text in the book you go to the section "Index", look for your special text. Then you see, that your text of interest is on page 8, but page 8 does not coincide with page 8 of the pdf, so you have to scroll to page 8. If you have correctly set page labels you can type "8" into your pdf viewer and than he shows you the correct page.

After editing the "testBook.pdf" with this tool it can look like "testBook_result.pdf". Working with this pdf is much more comfortable.

Remark, if your e-book consist of just a view pages you can create bookmarks faster using a pdf viewer like FoxitReader. This tool is just preferable if your book has hundreds of pages and the toc has a lot of entries.

0. Requirements on you pdf file
===============================

The pdf file need a text layer (roughly speaking this is fulfilled if you can search in your pdf) and your book stored in this pdf need a table of content. Because this enables the program to extract the table of content into a text file. Optionally, you can write this text file also by hand or get it from some ocr software.

1. Requirements to compile/run this program
===================================

 1.1 Compling
     --------   
	 - You need Java JDK 8 (or higher) 
	   http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
	   
	(- You do not need eclipse but I explain how to compile the programm using eclipse)
	   https://www.eclipse.org/
	   
	 - Library: Apache PDFBox (I use version 2.0.8 but newer could also work) you need the files
	   pdfbox-2.0.8.jar
	   fontbox-2.0.8.jar
	   https://pdfbox.apache.org/download.cgi
	   
	 - Library: Jackson (I use version 2.9.2 but newer could also work) you need the files
	   jackson-annotations-2.9.2.jar
	   jackson-core-2.9.2.jar
	   jackson-databind-2.9.2.jar
	   http://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.9.2/
	   
	 - Library: Apache Commons Logging (I use version 1.2 but newer could also work) you need the file
	   commons-logging-1.2.jar
	   https://commons.apache.org/proper/commons-logging/download_logging.cgi
   
 1.2 Running
     -------
	 - You need Java JRE 8 (or higher) 
	   http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
	   
	 - Library: Xpdf tools (I use version 4.00 but newer could also work) Unfortunatelly, this is not a java library, so you need the binary for your operation system (I testet it on win 7/10 and debian ??). You need the file
	   pdftopng.exe (on Windows) resp. pdftopng (on Linux)
	   https://www.xpdfreader.com/download.html
   
2. Installation
===============

If you just want to run the programm you can skip section "2.1 Compiling".

 2.1 Compiling
     --------- 
     - Download the source code into some directory (I call it in the following "source")
	 - Open Eclipse
	 - Create a new java project (File -> New -> Java Project) and select this new project
	 - Go to Project->Properties, a new windows pops up, then select "Java Built Path"
	 - In the register "Source" use the button "Link Source..." to add your directory "source"
	 - In the register "Libraries" use the button "Add External JARs..." to successively add all the *.jar files from the libraries I mentioned in section 1.
	 - Also in the register "Libraries" use the button "Add Library..." to add "JUnit" (I use JUnit 4)
	 - Then click on "Apply" and close this window.
	 
	 - Use the "Package explorer" to open the file components/TocEditor.java
	 - Click on "Run->Run" to create a run configuration (called TocEditor). Remark, the application should crash with an FileNotFoundException.
	 
	 - Go to File -> Export.. and choose Java->Runable JAR file...
	 - On the next page select the Launch Configuration "TocEditor - <project name>" and export/compile the programm to a file "TocEditor.jar"
	 
	 Now you can close eclipse
 
 2.2 Installation
     ------------
	 - Download all files/folder execpt of the src folder and store the in a folder I call "tocEditor" in the following.
	 - Download Xpdf tools and store the file pdftopng.exe resp. pdftopng in the folder "home".
     - Open the file "TocEditor.bat" (on Windows) resp. TocEditor.sh" (on Linux) with a editor. In the following I restrict my self to the Windows case.
     - Adapt the paths in this *.bat file to our needs. If you use the suggested folder structure you just have to adapet the variable TEMP.
	   TEMP=absolute-path-to your "tocEditor" folder.
       JAR=path-to "TocEditor.jar"
       PROC=path-to-the-executable-pdftopng.exe 
	   WORK=path-to-some-empty-directory-which-will-be-used-to-store-some-temporary-files
 
4. Workflow
===========

 4.0 Starting the Program
     --------------------
     There are two possibilities: (i) dopple click the TocEditor.bat or (ii) click right on your pdf file, choose "Open With..." and select your Skript "TocEditor.bat".  Then, a console pops up printing some debug information and the TocEditor starts.

 4.1 Open PDF File
     -------------
     Open a pdf file (File->Open Pdf) respectively if you started the program via pdf->open with->tocEditor this pdf is already opened in the program. The pdf will be visible on right panel.
 
 4.2 Open TOC File
     -------------
     Open the toc file. There are two possibilities:
 
     (i) You can use this posibility, if your pdf has a text layer and there are some pages containing the table of content. Choose File->Extract TOC from PDF. A new window pops up showing the first page of the pdf. Use the buttons "Previous" and "Next" to swap to the first page of the toc in the pdf. Then press "Mark Page". If the toc consists of several pages, then swap to the last page of the toc, press "Mark Page" again. Finally, press "OK". Then the program uses the library PdfBox to extract the toc (the marked pages) into a text file (stored in the directory WORK) and open this toc file.
   
     (ii) You can use this possibility, if you already have a text file containing the toc. Use File->Open TOC and select your text file containing the toc.

     You also can combine this two methods. Creating a toc file using method (i). If you see that the toc if of bad quality, it is some times easier to do some preprocessing with a usual text editor. And afterward load the preprocessed toc file using method (ii). 
   
     Another thing I want to mention is, that the pdf viewer FoxitReader has a nice text extractor (Using save as .txt) also in the free version. Especially, if the quality of the text layer is low then FoxitReader produced often better toc files the the text extractor form PdfBox, which you can load using method (ii).
   
     Finally, you should see the toc in the centre panel.
 
 4.3 Editing Entries
     ---------------
     Now you can edit the entries of the toc as you want. To edit the title you have to press F2 to start editing.
   
 4.4 Setting the Level
     -----------------
     To successfully embed the bookmarks into the pdf you have to set for each entry on which level of the hierarchy it should appear. This can be done manually, or you can tell the program the structure of your book and the program tries to set the level automatically. Therefore, use the left panel. The structure of a book (for this program) is as follows:
     * A book have one or more parts. Within a part the book have the same page labels (romen or arabic page number)
     * A part of a book has a hierarchy of one or more levels (level 0 up to level 9). Like section, subsection, subsubsection,...and so on. 
     * Each level is identified by a pattern of the numbering.
     A typical structure of the main part of a book is
   
        level 0: 1. Section 1
        level 1:    1.1 Subsection 1 of Section 1
        level 1:    1.2 Subsection 2 of Section 1
        level 1:    1.3 Subsection 3 of Section 1
        level 0: 2. Section 2
        level 1:    2.1 Subsection 1 of Section 2
        level 1:    2.2 Subsection 2 of Section 2
        level 1:    2.3 Subsection 3 of Section 2
        level 0: 3. Section 3
        level 1:    3.1 Subsection 1 of Section 3
        level 1:    3.2 Subsection 2 of Section 3
        level 1:    3.3 Subsection 3 of Section 3
   
     To create a new part you have to choose the first entry which should belong to the new part and the click on Edit->New Part. For deleting parts and deleting/creating new levels use the buttons in the left panel.
	  
     The pattern can be some regular expression without groups. Additionally, you can use the special patterns "#a1" to tell the programm, there is a arabic number indicating the number of level 1. This enables the programm to do easy semantic checks. The # starts the special pattern. For the second character you can use
        
        a = for arabic number: 1,2,3,....
        r = for roman number (lower case): i,ii,iii,...
        R = for roman number (upper case): I,II,III,...
        L = for alphabetic number : A,B,C,...

   The third character indicates the level the numbering belongs to (0-9 is possible). For the above example one would use the patterns
   
        level 0: Pattern: "#a0\."
        level 1: Pattern: "#a0\.#a1"
   
   After editing all the parts, levels and patterns you should click on Edit->Reparse to apply the structure to the toc.
   
 4.5 Setting the Offset
     ------------------
     If all levels and page number are correctly set, you can start editing the offset. This step is needed because often the page numbers of the book do not coincide this the page numbers in the pdf. One possible reason is that sometimes with a new part in a book the page numbers start a gain with one. To easily set the offset you can click on View->Sync Preview. If you now click on an entry of the toc, the preview on the right jumps to the corresponding page (page+offset). If this is already the correct page in the pdf, then you do not have to edit the offset. If not, you can use the buttons "Previous" and "Next" in the preview panel to swap to the right page and click on Edit->Set Offset to automatically set the offset correctly.

     I usually set the offset for all level 0 entries first, then for all level 1 entries and so on. Remark, you can use View->Next Level Entry and View->Previous Level Entry, to jump in the toc to the next/previous entry with the same level.
 
 4.6 Saving
     ------
     As final step you can save your bookmarks clicking on File->Save PDF as ... (and embed the TOC and Page Labels).
   
4. Further Features and Remarks
===============================

 - There is no way to undo or redo any of your editing.
 - I usually prefer to use the short cuts for all the editing. But these short cuts are hard coded. So to change them you have to edit the source code (in the file components/TocEditor.bat the methods createFileMenu(), createEditMenu() and createViewMenu()). Remark some short cuts like Ctrl+C did not work for me, maybe because this is typically used for copy.
 - File menu:
   Save as ...(embed TOC and Page Labels): If there are already bookmarks stored in your pdf you will be asked, if you wish to over write them. If you press no, the bookmarkes are appended at the end of the bookmark list.

   The next three entries are deprecated:
   Save TOC for Pdftk: Saves the bookmarks in a text file in your WORK folder. Using a format readable by the tool pdftk.
   Save TOC as Html: Saves the bookmarks in a html file in your WORK folder.
   Save Page Labels: Saves the page labels in a text file in your WORK folder. Using the format of an uncompressed pdf.

 - Edit menu:
   Reparse: completely redo the parsing using the current structure indicated by the left panel.
   Ckeck: Just do some sanity checks. The result is indicated by the colours in the centre panel. This option is normally not needed, because the program should automatically recheck after each editing. The only case you need this option is after editing the "Allowed Titels" in the left panel. The "Allowed Titles" is a regular expression indicating which titles are allowed.
   Delte Entry, New Entry, Move Entry Up, Move Entry Down: There is nothing to say about
   Combine Entries: If an entry of the toc is split into two entries because of a line break. You can mark the first line and press Combine Entries to combine it with the second line.
   Delete Page Numbers: Deletes all page number.

5. Acknowledgement
==================
   Thank you for all these great libraries freely available.
   Thank you to you giving a try to my little tool.

   
If you have any Feedback for this programm please leave a message.



