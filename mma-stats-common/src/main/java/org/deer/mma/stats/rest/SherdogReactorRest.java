package org.deer.mma.stats.rest;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.deer.mma.stats.reactor.service.SherdogDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reactor")
public class SherdogReactorRest {

  @Autowired
  private SherdogDiscoveryService sherdogDiscoveryService;

  @RequestMapping(value = "/trigger-sherdog-attribute-discovery", method = POST, consumes = "application/json")
  public ResponseEntity<String> triggerSherdogDiscovery() {
    sherdogDiscoveryService.triggerSherdogDiscovery();
    return ResponseEntity.ok("Discovery triggered");
  }
}
