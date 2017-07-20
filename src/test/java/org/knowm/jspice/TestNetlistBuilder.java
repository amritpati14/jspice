package org.knowm.jspice;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.knowm.jspice.component.element.memristor.MMSSMemristor;
import org.knowm.jspice.netlist.Netlist;
import org.knowm.jspice.netlist.NetlistBuilder;
import org.knowm.jspice.simulate.transientanalysis.TransientConfig;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;

public class TestNetlistBuilder {

  @Test
  public void test1() throws IOException {

    List<String> netlistLines = new ArrayList<>();

    // create netlist from traditional SPICE netlist file.
    ConfigurationSourceProvider provider = new ResourceConfigurationSourceProvider();
    try (Scanner scanner = new Scanner(provider.open("knowm_mr_netlist.cir"))) {
      while (scanner.hasNext()) {
        netlistLines.add(scanner.nextLine());
      }
    }

    Netlist netlist = NetlistBuilder.buildFromSPICENetlist(netlistLines);

    System.out.println("netlist " + netlist);

    // DC voltage sources
    assertThat(netlist.getNetListDCVoltageSources()).hasSize(2);

    // transient
    assertThat(netlist.getSimulationConfig()).isInstanceOf(TransientConfig.class);
    TransientConfig config = (TransientConfig) netlist.getSimulationConfig();
    assertThat(config.getStopTime()).isEqualTo(0.5);
    assertThat(config.getTimeStep()).isEqualTo(0.0049505);
    assertThat(config.getDrivers()[0].getAmplitude()).isEqualTo(0.5);
    assertThat(config.getDrivers()[0].getFrequency()).isEqualTo(10);

    // memristor
    assertThat(netlist.getNetListMemristors()).hasSize(1);
    MMSSMemristor memristor = (MMSSMemristor) netlist.getNetListMemristors().get(0).getComponent();
    assertThat(memristor.getTau()).isEqualTo(0.0001);
  }

}