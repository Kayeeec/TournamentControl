package cz.tournament.control.web.rest;

import cz.tournament.control.domain.Tournament;
import cz.tournament.control.domain.tournaments.AllVersusAll;
import cz.tournament.control.repository.AllVersusAllRepository;

import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.service.FileGeneratorService;
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
    private final FileGeneratorService fileGeneratorService;
    private final AllVersusAllRepository allVersusAllRepository;

    public FileGeneratorResource(FileGeneratorService fileGeneratorService, AllVersusAllRepository allVersusAllRepository) {
        this.fileGeneratorService = fileGeneratorService;
        this.allVersusAllRepository = allVersusAllRepository;
    }

    

    @GetMapping(value = "/allVersusAll/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public InputStreamResource getFile(@PathVariable Long id) throws FileNotFoundException, IOException {
        log.debug("REST request for generated file for allVersusAll tournament.id: {}", id);
        AllVersusAll tournament = allVersusAllRepository.findOne(id);
        response.setHeader("Content-Disposition", " inline; filename=\"tournament_"+id.toString()+".ods\"");
        return new InputStreamResource(new FileInputStream(fileGeneratorService.generateAllVersusAllODS(tournament)));
    }

}
