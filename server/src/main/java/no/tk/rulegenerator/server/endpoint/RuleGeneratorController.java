package no.tk.rulegenerator.server.endpoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNProposition;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingRequest;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingResponse;
import no.tk.rulegenerator.server.endpoint.dtos.ModelCheckingRequest;
import no.tk.rulegenerator.server.endpoint.dtos.ModelCheckingResponse;
import no.tk.rulegenerator.server.endpoint.verification.BPMNModelChecker;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class RuleGeneratorController {

  // Needed for custom form data deserialization
  ObjectMapper jsonMapper = new ObjectMapper();
  CollectionType setTypeJackson =
      jsonMapper.getTypeFactory().constructCollectionType(Set.class, BPMNProposition.class);

  /**
   * Generate a graph grammar for a given BPMN file.
   *
   * @param file BPMN file defining a collaboration.
   * @param response will be a ZIP of the graph grammar for the BPMN file.
   */
  @PostMapping(value = "/generateGGAndZip", produces = "application/zip")
  public void generateGGAndZip(
      @RequestParam("file") MultipartFile file,
      @RequestParam(value = "propositions", defaultValue = "[]") String propositions,
      HttpServletResponse response)
      throws IOException {
    RuleGeneratorControllerHelper.deleteGGsAndStateSpacesOlderThanOneHour();

    Path ggDir = RuleGeneratorControllerHelper.generateGGForBPMNFile(file, true).getLeft();

    RuleGeneratorControllerHelper.generatePropositions(
        ggDir, readPropositionsFromJSON(propositions), true);

    // Zip all files
    try (DirectoryStream<Path> graphGrammarFiles = Files.newDirectoryStream(ggDir)) {
      zipAndReturnFiles(response, graphGrammarFiles);
    }
  }

  private void zipAndReturnFiles(HttpServletResponse response, DirectoryStream<Path> files)
      throws IOException {
    // Setting headers
    response.setStatus(HttpServletResponse.SC_OK);
    response.addHeader("Content-Disposition", "attachment; filename=\"graphGrammar.gps.zip\"");

    // Zipping files
    try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
      for (Path file : files) {
        zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));

        Files.copy(file, zipOutputStream);

        zipOutputStream.closeEntry();
      }
    }
  }

  /**
   * Run model-checking of certain BPMN-specific properties for a BPMN collaboration.
   *
   * @param request contains the BPMN file and properties to be checked.
   * @return model-checking results for the requested properties.
   */
  @PostMapping(value = "/checkBPMNSpecificProperties")
  public BPMNSpecificPropertyCheckingResponse checkBPMNSpecificProperties(
      @ModelAttribute BPMNSpecificPropertyCheckingRequest request)
      throws IOException, InterruptedException {
    RuleGeneratorControllerHelper.deleteGGsAndStateSpacesOlderThanOneHour();

    Pair<Path, BPMNCollaboration> result =
        RuleGeneratorControllerHelper.generateGGForBPMNFile(request.file(), false);

    return new BPMNModelChecker(result.getLeft(), result.getRight())
        .checkBPMNProperties(request.propertiesToBeChecked());
  }

  /**
   * Run model-checking for a temporal logic query for a BPMN collaboration and propositions.
   *
   * @param request contains the BPMN file and properties to be checked.
   * @return model-checking results for the requested properties.
   */
  @PostMapping(value = "/checkTemporalLogic")
  public ModelCheckingResponse checkTemporalLogicProperty(
      @ModelAttribute ModelCheckingRequest request) throws IOException, InterruptedException {
    RuleGeneratorControllerHelper.deleteGGsAndStateSpacesOlderThanOneHour();

    Pair<Path, BPMNCollaboration> dirAndCollaboration =
        RuleGeneratorControllerHelper.generateGGForBPMNFile(request.file(), false);

    RuleGeneratorControllerHelper.generatePropositions(
        dirAndCollaboration.getLeft(), readPropositionsFromJSON(request.propositions()), false);

    return new BPMNModelChecker(dirAndCollaboration.getLeft(), dirAndCollaboration.getRight())
        .checkTemporalLogicProperty(request.logic(), request.property());
  }

  private Set<BPMNProposition> readPropositionsFromJSON(String propositions)
      throws JsonProcessingException {
    if (propositions == null) {
      return Collections.emptySet();
    }
    return jsonMapper.readValue(propositions, setTypeJackson);
  }
}
