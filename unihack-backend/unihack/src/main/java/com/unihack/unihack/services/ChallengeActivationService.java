package com.unihack.unihack.services;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.springframework.stereotype.Service;

@Service
public class ChallengeActivationService {

    private final DockerClient dockerClient;

    public ChallengeActivationService() {
        // Conecta-se ao daemon Docker no host do servidor
        // Geralmente via /var/run/docker.sock
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())
            .sslConfig(config.getSSLConfig())
            .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public String startChallengeContainer(String challengeImageName) {
        // Nome da imagem do desafio, ex: "unihack/desafio-sqli:latest"

        // Define a porta interna do contêiner
        ExposedPort exposedPort = ExposedPort.tcp(80); // A porta que o PHP/Apache escuta DENTRO do contêiner

        // Mapeia para uma porta aleatória no host
        Ports portBindings = new Ports();
        portBindings.bind(exposedPort, Ports.Binding.empty()); 

        HostConfig hostConfig = HostConfig.newHostConfig().withPortBindings(portBindings);

        // Cria o contêiner
        CreateContainerResponse container = dockerClient.createContainerCmd(challengeImageName)
                .withExposedPorts(exposedPort)
                .withHostConfig(hostConfig)
                .exec();

        // Inicia o contêiner
        dockerClient.startContainerCmd(container.getId()).exec();

        // ATENÇÃO: A lógica para obter a porta pública exata aqui é um pouco mais complexa.
        // Você precisa inspecionar o contêiner após iniciá-lo para descobrir qual porta foi atribuída.
        // Esta é uma simplificação.
        System.out.println("Contêiner iniciado: " + container.getId());

        // TODO: Inspecionar o contêiner para pegar a porta e retorná-la.
        // Guardar o container.getId() no banco de dados associado ao usuário.

        return container.getId(); // Retorne o ID para gerenciar depois
    }

    public void stopAndRemoveContainer(String containerId) {
        if (containerId != null && !containerId.isEmpty()) {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("Contêiner parado e removido: " + containerId);
        }
    }
}
