package cz.tournament.control.web.rest;

import com.codahale.metrics.annotation.Timed;
import cz.tournament.control.domain.Tournament;

import cz.tournament.control.repository.TournamentRepository;
import cz.tournament.control.service.FileGeneratorService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * REST controller for managing Tournament.
 */
@RestController
@RequestMapping("/generateFile")
public class FileGeneratorResource {

    private final Logger log = LoggerFactory.getLogger(FileGeneratorResource.class);
    private final TournamentRepository tournamentRepository;
    private final FileGeneratorService fileGeneratorService;

    public FileGeneratorResource(TournamentRepository tournamentRepository, FileGeneratorService fileGeneratorService) {
        this.tournamentRepository = tournamentRepository;
        this.fileGeneratorService = fileGeneratorService;
    }

//    /**
//     * GET  /tournaments/:id : get the "id" tournament.
//     *
//     * @param id the id of the tournament to retrieve
//     * @return the ResponseEntity with status 200 (OK) and with body the tournament, or with status 404 (Not Found)
//     */
//    @GetMapping(value="/allVersusAll_file/{id}", produces = "application/vnd.oasis.opendocument.spreadsheet")
//    @Timed
//    public Response generateFile(@PathVariable Long id) {
//        log.debug("REST request to get Tournament : {}", id);
//        Tournament tournament = tournamentRepository.findOneWithEagerRelationships(id);
//        
//        File file = fileGeneratorService.getGeneratedFile(tournament);
//        
//        
//        return Response.ok(file, MediaType.parseMediaType("application/vnd.oasis.opendocument.spreadsheet").getType())
//      .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" ) //optional
//      .build();
//    }
    
    
    
    @RequestMapping(value = "/allVersusAll_file/{id}", method = RequestMethod.GET, produces = "application/vnd.oasis.opendocument.spreadsheet")
    public ResponseEntity<byte[]> generateFile(@PathVariable Long id) {
        log.debug("REST request to get Tournament : {}", id);
        Tournament tournament = tournamentRepository.findOneWithEagerRelationships(id);
        FileInputStream fileStream;
        try {
            fileStream = new FileInputStream(fileGeneratorService.getGeneratedFile(tournament));
            byte[] contents = IOUtils.toByteArray(fileStream);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/vnd.oasis.opendocument.spreadsheet"));
            String filename = "stahnuty.ods.";
            headers.setContentDispositionFormData(filename, filename);
            ResponseEntity<byte[]> response = new ResponseEntity<>(contents, headers, HttpStatus.OK);
            return response;
        } catch (FileNotFoundException e) {
           System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        }
        return null;
    }
    
    @RequestMapping(value = "/file/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public FileSystemResource getFile(@PathVariable Long id) {
        log.debug("REST request to get Tournament : {}", id);
        Tournament tournament = tournamentRepository.findOneWithEagerRelationships(id);
        return new FileSystemResource(fileGeneratorService.getGeneratedFile(tournament));
    }

    

}
