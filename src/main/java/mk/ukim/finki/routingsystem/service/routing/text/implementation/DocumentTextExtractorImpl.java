package mk.ukim.finki.routingsystem.service.routing.text.implementation;

import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;
import mk.ukim.finki.routingsystem.model.exceptions.UnreadableDocumentException;
import mk.ukim.finki.routingsystem.service.routing.text.DocumentTextExtractor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

/**
 * Implementation of {@link DocumentTextExtractor} using Apache PDFBox.
 */

@Service
public class DocumentTextExtractorImpl implements DocumentTextExtractor {

  // extract all the pages -> 0 = all pages
  @Value("${app.text.extract.max-pages:0}")
  private int maxPages;

  // if false and PDF is encrypted - throw UnreadableDocumentException
  @Value("${app.text.extract.allow-encrypted:false}")
  private boolean allowEncrypted;


  @Override
  public String extractAll(byte[] pdfBytes) {

    if (pdfBytes == null || pdfBytes.length == 0) {
      throw new UnreadableDocumentException("The document has no file.");
    }

    try (PDDocument document = Loader.loadPDF(pdfBytes)) {

      if (document.isEncrypted() && !allowEncrypted) {
        throw new UnreadableDocumentException("The PDF is password-protected.");
      }

      PDFTextStripper pdfStripper = new PDFTextStripper();

      if (maxPages > 0) {
        pdfStripper.setStartPage(1);
        int lastPage = Math.min(document.getNumberOfPages(), maxPages);
        pdfStripper.setEndPage(lastPage);
      }

      // "" here means only that the PDF has no text layer (e.g. a scan)
      return pdfStripper.getText(document);

    } catch (InvalidPasswordException e) {
      // a PDF with an open password fails already in Loader.loadPDF
      throw new UnreadableDocumentException("The PDF is password-protected.", e);
    } catch (IOException e) {
      throw new UnreadableDocumentException("The PDF file is damaged.", e);
    }

  }

  @Override
  public TitleAndBody extractTitleAndBody(byte[] pdfBytes) {

    String allText = extractAll(pdfBytes);

    if (allText.isBlank()) {
      return new TitleAndBody("", "");
    }

    String[] lines = allText.split("\\R+");

    int i = 0;
    String title = "";

    while (i < lines.length && (title = lines[i].trim()).isBlank()) {
      i++;
    }

    String body;

    if (i + 1 < lines.length) {
      String[] rest = Arrays.copyOfRange(lines, i + 1, lines.length);
      body = String.join("\n", rest);
    } else {
      body = "";
    }

    return new TitleAndBody(title, body);
  }
}
