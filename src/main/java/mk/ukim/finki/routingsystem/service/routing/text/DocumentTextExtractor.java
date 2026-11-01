package mk.ukim.finki.routingsystem.service.routing.text;

import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;

/**
 * Service interface for extracting the text from the uploaded PDF document.
 */

public interface DocumentTextExtractor {

  /**
   * Extracts all text from a PDF document.
   *
   * @param pdfBytes the raw bytes of the PDF document
   * @return a string containing the full text of the document, empty if the PDF has no text (e.g. a scan)
   * @throws mk.ukim.finki.routingsystem.model.exceptions.UnreadableDocumentException if the file is missing, password-protected or damaged
   */
  String extractAll(byte[] pdfBytes);

  /**
   * Extracts the text from a PDF document in a title and body.
   *
   * @param pdfBytes the raw bytes of the PDF document
   * @return a {@link TitleAndBody} containing the title and body of the document
   * @throws mk.ukim.finki.routingsystem.model.exceptions.UnreadableDocumentException if the file is missing, password-protected or damaged
   */
  TitleAndBody extractTitleAndBody(byte[] pdfBytes);

}
