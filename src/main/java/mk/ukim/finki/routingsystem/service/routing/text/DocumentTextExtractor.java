package mk.ukim.finki.routingsystem.service.routing.text;

import mk.ukim.finki.routingsystem.model.dto.Routing.TitleAndBody;


public interface DocumentTextExtractor {


    String extractAll(byte[] text);


    TitleAndBody extractTitleAndBody(byte[] text);

}
