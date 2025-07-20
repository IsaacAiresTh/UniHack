package com.unihack.unihack.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class ChallengeActivationService {

    private final DockerClient dockerClient;

    public ChallengeActivationService() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public Map<String, String> startChallengeContainer(String dockerImageName) {
        ExposedPort internalPort = ExposedPort.tcp(80);
        Ports portBindings = new Ports();
        portBindings.bind(internalPort, Ports.Binding.empty());

        // Adiciona a remoção automática do contêiner quando ele é parado
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withAutoRemove(true);

        CreateContainerResponse container = dockerClient.createContainerCmd(dockerImageName)
                .withExposedPorts(internalPort)
                .withHostConfig(hostConfig)
                .exec();

        String containerId = container.getId();
        dockerClient.startContainerCmd(containerId).exec();

        // Inspeciona o contêiner para pegar a porta pública
        InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
        String publicPort = Objects.requireNonNull(inspectResponse.getNetworkSettings().getPorts().getBindings().get(internalPort))[0].getHostPortSpec();
        String accessUrl = "http://localhost:" + publicPort; // Em produção, usar o IP do servidor

        System.out.println("Contêiner iniciado: " + containerId + " em " + accessUrl);

        return Map.of(
                "containerId", containerId,
                "accessUrl", accessUrl
        );
    }

    public void stopContainer(String containerId) {
        if (containerId != null && !containerId.isEmpty()) {
            try {
                System.out.println("Parando contêiner: " + containerId);
                dockerClient.stopContainerCmd(containerId).exec();
                // A remoção será automática se withAutoRemove(true) foi usado
            } catch (Exception e) {
                System.err.println("Erro ao parar contêiner " + containerId + ". Ele pode já ter sido parado/removido. " + e.getMessage());
            }
        }
    }
}