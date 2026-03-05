package mk.ukim.finki.routingsystem.model.dto.Routing;

import java.util.List;
import java.util.regex.Pattern;

public record CompiledRules(

        List<Pattern> strong,
        List<Pattern> weak,
        List<Pattern> negative
) { }
