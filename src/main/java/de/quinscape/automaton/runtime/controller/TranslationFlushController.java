package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TranslationFlushController
{
    private final TranslationService translationService;

    public static final ResponseEntity<String> OK = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);


    public TranslationFlushController(
        @Autowired(required = false) TranslationService translationService
    )
    {
        this.translationService = translationService;
    }

    @RequestMapping(value = "/_dev/flush-translations", method = RequestMethod.GET)
    public ResponseEntity<String> flushTranslations()
    {
        if (translationService != null)
        {
            translationService.flush();
        }
        return OK;
    }
}
