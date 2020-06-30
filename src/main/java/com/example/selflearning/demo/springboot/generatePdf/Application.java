package com.example.selflearning.demo.springboot.generatePdf;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SpringBootApplication
@RestController
public class Application {
	@GetMapping("/itex")
	public String hello(HttpServletResponse response) throws IOException, DocumentException, URISyntaxException {
		Path path = Paths.get(ClassLoader.getSystemResource("Java_logo.png").toURI());

		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream("iTextHelloWorld.pdf"));

		document.open();
//		add text
		Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);
		Chunk chunk = new Chunk("Hello World", font);
		document.add(chunk);

//		add Image
		Image img = Image.getInstance(path.toAbsolutePath().toString());
		document.add(img);


//		add table
		PdfPTable table = new PdfPTable(3);
		addTableHeader(table);
		addRows(table);
		addCustomRows(table);
		document.add(table);

		document.close();
		PdfReader pdfReader = new PdfReader("iTextHelloWorld.pdf");
		PdfStamper pdfStamper
				= new PdfStamper(pdfReader, new FileOutputStream("encryptedPdf.pdf"));

		pdfStamper.setEncryption(
				"userpass".getBytes(),
				"ownerpass".getBytes(),
				0,
				PdfWriter.ENCRYPTION_AES_256
		);

		pdfStamper.close();

		File file = new File("encryptedPdf.pdf");
		response.setContentType("application/zip");
		response.setHeader("Content-disposition", "attachment; filename=" + file.getName());
		response.setContentLength((int) file.length());

		OutputStream out = response.getOutputStream();
		FileInputStream in = new FileInputStream(file);
		IOUtils.copy(in,out);

		out.close();
		in.close();
		file.delete();
		return "hello from server";
	}
	@GetMapping("/pdfbox")
	public String hello1() throws IOException, URISyntaxException {
		Path path = Paths.get(ClassLoader.getSystemResource("Java_logo.png").toURI());
		PDDocument document = new PDDocument();
		PDPage page = new PDPage();
		document.addPage(page);

		PDPageContentStream contentStream = new PDPageContentStream(document, page);

//		add text
		contentStream.setFont(PDType1Font.COURIER, 12);
		contentStream.beginText();
		contentStream.showText("Hello World");
		contentStream.endText();
		contentStream.close();

//		add Image
		PDImageXObject image
				= PDImageXObject.createFromFile(path.toAbsolutePath().toString(), document);
		contentStream.drawImage(image, 0, 0);
//		contentStream.drawForm();
		contentStream.close();


		AccessPermission accessPermission = new AccessPermission();
		accessPermission.setCanPrint(false);
		accessPermission.setCanModify(false);

		StandardProtectionPolicy standardProtectionPolicy
				= new StandardProtectionPolicy("ownerpass", "userpass", accessPermission);
		document.protect(standardProtectionPolicy);
		document.save("pdfBoxHelloWorld.pdf");
		document.close();

//		encripted file

//		PDDocument document1 = new PDDocument();
//		PDPage page1 = new PDPage();
//		document1.addPage(page);
//
//		AccessPermission accessPermission = new AccessPermission();
//		accessPermission.setCanPrint(false);
//		accessPermission.setCanModify(false);
//
//		StandardProtectionPolicy standardProtectionPolicy
//				= new StandardProtectionPolicy("ownerpass", "userpass", accessPermission);
//		document1.protect(standardProtectionPolicy);
//		document1.save("pdfBoxEncryption.pdf");
//		document1.close();
		return "Hello1";
	}
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	private void addTableHeader(PdfPTable table) {
		Stream.of("column header 1", "column header 2", "column header 3")
				.forEach(columnTitle -> {
					PdfPCell header = new PdfPCell();
					header.setBackgroundColor(BaseColor.LIGHT_GRAY);
					header.setBorderWidth(2);
					header.setPhrase(new Phrase(columnTitle));
					table.addCell(header);
				});
	}
	private void addRows(PdfPTable table) {
		table.addCell("row 1, col 1");
		table.addCell("row 1, col 2");
		table.addCell("row 1, col 3");
	}

	private void addCustomRows(PdfPTable table)
			throws URISyntaxException, BadElementException, IOException {
		Path path = Paths.get(ClassLoader.getSystemResource("Java_logo.png").toURI());
		Image img = Image.getInstance(path.toAbsolutePath().toString());
		img.scalePercent(10);

		PdfPCell imageCell = new PdfPCell(img);
		table.addCell(imageCell);

		PdfPCell horizontalAlignCell = new PdfPCell(new Phrase("row 2, col 2"));
		horizontalAlignCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(horizontalAlignCell);

		PdfPCell verticalAlignCell = new PdfPCell(new Phrase("row 2, col 3"));
		verticalAlignCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
		table.addCell(verticalAlignCell);
	}
}
