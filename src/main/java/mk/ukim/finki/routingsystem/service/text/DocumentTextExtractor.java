package mk.ukim.finki.routingsystem.service.text;

import mk.ukim.finki.routingsystem.model.dto.TitleAndBody;


public interface DocumentTextExtractor {

    // extracts all the text from the document bytes
    String extractAll(byte[] text);

    // returns the first non-empty line as the title and the rest as a body
    TitleAndBody extractTitleAndBody(byte[] text);

}
