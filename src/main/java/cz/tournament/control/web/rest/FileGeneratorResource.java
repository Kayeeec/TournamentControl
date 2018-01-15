package cz.tournament.control.web.rest;

import cz.tournament.control.domain.AllVersusAll;
import cz.tournament.control.domain.Combined;
import cz.tournament.control.domain.Elimination;
import cz.tournament.control.domain.Swiss;
import cz.tournament.control.service.AllVersusAllService;
import cz.tournament.control.service.CombinedService;
import cz.tournament.control.service.EliminationService;
import cz.tournament.control.service.SwissService;
import cz.tournament.control.service.fileGenerator.AllVersusAllFileGeneratorService;
import cz.tournament.control.service.fileGenerator.CombinedFileGeneratorService;
import cz.tournament.control.service.fileGenerator.EliminationFileGeneratorService;
import cz.tournament.control.service.fileGenerator.SwissFileGeneratorService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    
    private final SwissService swissService;
    private final SwissFileGeneratorService swissFileGeneratorService;
    
    private final EliminationService eliminationService;
    private final EliminationFileGeneratorService eliminationFileGeneratorService;
    
    private final CombinedService combinedService;
    private final CombinedFileGeneratorService combinedFileGeneratorService;

    public FileGeneratorResource(AllVersusAllFileGeneratorService avaFileGeneratorService, AllVersusAllService allVersusAllService, SwissService swissService, SwissFileGeneratorService swissFileGeneratorService, EliminationService eliminationService, EliminationFileGeneratorService eliminationFileGeneratorService, CombinedService combinedService, CombinedFileGeneratorService combinedFileGeneratorService) {
        this.avaFileGeneratorService = avaFileGeneratorService;
        this.allVersusAllService = allVersusAllService;
        this.swissService = swissService;
        this.swissFileGeneratorService = swissFileGeneratorService;
        this.eliminationService = eliminationService;
        this.eliminationFileGeneratorService = eliminationFileGeneratorService;
        this.combinedService = combinedService;
        this.combinedFileGeneratorService = combinedFileGeneratorService;
    }

    @GetMapping(value = "/allVersusAll/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public InputStreamResource getAllVersusAllFile(@PathVariable Long id) throws FileNotFoundException, IOException {
        log.debug("REST request for generated file for allVersusAll tournament.id: {}", id);
        
        AllVersusAll tournament = allVersusAllService.findOne(id);
        String filename = "allVersusAll-tournament_"+tournament.getName()+".ods";
        File file = avaFileGeneratorService.generateSpreadSheet(tournament);
        
        response.setHeader("Content-Disposition", " inline; filename=\""+filename+"\"");
        return new InputStreamResource(new FileInputStream(file));
    }
    
    @GetMapping(value = "/swiss/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public InputStreamResource getSwissFile(@PathVariable Long id) throws FileNotFoundException, IOException {
        log.debug("REST request for generated file for swiss tournament.id: {}", id);
        
        Swiss tournament = swissService.findOne(id);
        String filename = "swiss-tournament_"+tournament.getName()+".ods";
        File file = swissFileGeneratorService.generateSpreadSheet(tournament);
        
        response.setHeader("Content-Disposition", " inline; filename=\""+filename+"\"");
        return new InputStreamResource(new FileInputStream(file));
    }
    
    @GetMapping(value = "/elimination/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public InputStreamResource getEliminationFile(@PathVariable Long id) throws FileNotFoundException, IOException {
        log.debug("REST request for generated file for elimination tournament.id: {}", id);
        
        Elimination tournament = eliminationService.findOne(id);
        String filename = "elimination-tournament_"+tournament.getName()+".ods";
        File file = eliminationFileGeneratorService.generateSpreadSheet(tournament);
        
        response.setHeader("Content-Disposition", " inline; filename=\""+filename+"\"");
        return new InputStreamResource(new FileInputStream(file));
    }
    
    @GetMapping(value = "/combined/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public InputStreamResource getCombinedFile(@PathVariable Long id) throws FileNotFoundException, IOException {
        log.debug("REST request for generated file for combined tournament.id: {}", id);
        
        Combined tournament = combinedService.findOne(id);
        String filename = "combined-tournament_"+tournament.getName()+".ods";
        File file = combinedFileGeneratorService.generateSpreadSheet(tournament);
        
        response.setHeader("Content-Disposition", " inline; filename=\""+filename+"\"");
        return new InputStreamResource(new FileInputStream(file));
    }

}
