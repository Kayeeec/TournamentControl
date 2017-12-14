package cz.tournament.control.web.rest;

import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.service.AllVersusAllService;
import cz.tournament.control.service.fileGenerator.AllVersusAllFileGeneratorService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;

/**
 * REST controller for managing Tournament.
 */
@RestController
@RequestMapping("/generateFile")
public class FileGeneratorResource {

    @Autowired
    private HttpServletResponse response;

    private final Logger log = LoggerFactory.getLogger(FileGeneratorResource.class);
    
    private final AllVersusAllFileGeneratorService avaFileGeneratorService;
    private final AllVersusAllService allVersusAllService;

    public FileGeneratorResource(AllVersusAllFileGeneratorService avaFileGeneratorService, AllVersusAllService allVersusAllService) {
        this.avaFileGeneratorService = avaFileGeneratorService;
        this.allVersusAllService = allVersusAllService;
    }

    @GetMapping(value = "/allVersusAll/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public InputStreamResource getFile(@PathVariable Long id) throws FileNotFoundException, IOException {
        log.debug("REST request for generated file for allVersusAll tournament.id: {}", id);
        
        AllVersusAll tournament = allVersusAllService.findOne(id);
        String filename = "allVersusAll-tournament_"+tournament.getName()+".ods";
        File file = avaFileGeneratorService.generateAllVersusAllSpreadSheet(tournament);
        
        response.setHeader("Content-Disposition", " inline; filename=\""+filename+"\"");
        return new InputStreamResource(new FileInputStream(file));
    }

}
