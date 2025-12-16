package mk.ukim.finki.routingsystem.service.implementations;

import mk.ukim.finki.routingsystem.model.dto.Document.TitleAndBody;
import mk.ukim.finki.routingsystem.service.text.DocumentTextExtractor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DocumentTextExtractorImpl implements DocumentTextExtractor {

    // extract all the pages -> 0 = all pages
    @Value("${app.text.extract.max-pages:0}")
    private int maxPages;

    // if false and PDF is encrypted - (return "")
    @Value("${app.text.extract.allow-encrypted:false}")
    private boolean allowEncrypted;


    @Override
    public String extractAll(byte[] text) {

        if (text == null || text.length == 0) {
            return "";
        }

        try (PDDocument document = Loader.loadPDF(text)) {

            if (document.isEncrypted() && !allowEncrypted) {
                return "";
            }

            PDFTextStripper pdfStripper = new PDFTextStripper();

            if (maxPages > 0) {
                pdfStripper.setStartPage(1);
                int lastPage = Math.min(document.getNumberOfPages(), maxPages);
                pdfStripper.setEndPage(lastPage);
            }

            return pdfStripper.getText(document);

        } catch (Exception e) {
            return "";
        }

    }

    @Override
    public TitleAndBody extractTitleAndBody(byte[] text) {

        String allText = extractAll(text);

        if (allText.isBlank()) {
            return new TitleAndBody("", "");
        }

        // splitting the text into lines (\\R matches any kind of break)
        String[] lines = allText.split("\\R+");

        int i = 0;
        String title = "";

        // find the first non-empty line - title
        while (i < lines.length && (title = lines[i].trim()).isBlank()) {
            i++;
        }

        String body;
        // everything after the title - body
        if (i + 1 < lines.length) {
            String[] rest = Arrays.copyOfRange(lines, i + 1, lines.length);
            body = String.join("\n", rest);
        } else {
            body = "";
        }

        return new TitleAndBody(title, body);
    }
}
