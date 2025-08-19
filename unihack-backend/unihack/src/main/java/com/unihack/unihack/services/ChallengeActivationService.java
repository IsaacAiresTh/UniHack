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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ChallengeActivationService {

    private static final Logger logger = LoggerFactory.getLogger(ChallengeActivationService.class);
    private final DockerClient dockerClient;

    @Value("${server.public.address:http://localhost}")
    private String serverPublicAddress;

    public ChallengeActivationService() {
        logger.info("A inicializar o ChallengeActivationService e a conectar-se ao Docker daemon.");
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();
        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }

    public Map<String, String> startChallengeContainer(String dockerImageName) {
        logger.info("A tentar iniciar o contentor para a imagem: {}", dockerImageName);

        ExposedPort internalPort = ExposedPort.tcp(80);
        Ports portBindings = new Ports();
        portBindings.bind(internalPort, Ports.Binding.empty());

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings)
                .withAutoRemove(true);

        CreateContainerResponse container;
        try {
            container = dockerClient.createContainerCmd(dockerImageName)
                    .withExposedPorts(internalPort)
                    .withHostConfig(hostConfig)
                    .withNetworkMode("unihack_unihack-net") // <-- ADICIONEI ESTA LINHA IMPORTANTE
                    .exec();
            logger.info("Contentor criado com sucesso. ID: {}", container.getId());
        } catch (Exception e) {
            logger.error("FALHA AO CRIAR O CONTENTOR para a imagem {}. A imagem existe localmente?", dockerImageName, e);
            throw new RuntimeException("Falha ao criar o contentor do desafio.", e);
        }

        String containerId = container.getId();
        dockerClient.startContainerCmd(containerId).exec();
        logger.info("Comando para iniciar o contentor {} enviado.", containerId);

        // --- LÓGICA DE VERIFICAÇÃO DE PORTA MAIS ROBUSTA COM TENTATIVAS ---
        String publicPort = null;
        int maxRetries = 5; // Tenta por até 2.5 segundos
        int retryCount = 0;

        while (publicPort == null && retryCount < maxRetries) {
            try {
                Thread.sleep(500); // Espera 500 milissegundos
                retryCount++;
                logger.info("Tentando obter a porta, tentativa {}...", retryCount);

                InspectContainerResponse inspectResponse = dockerClient.inspectContainerCmd(containerId).exec();
                Ports.Binding[] bindings = inspectResponse.getNetworkSettings().getPorts().getBindings().get(internalPort);

                if (bindings != null && bindings.length > 0 && bindings[0].getHostPortSpec() != null) {
                    publicPort = bindings[0].getHostPortSpec();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Thread interrompida enquanto esperava pela porta do contêiner.", e);
                break;
            } catch (Exception e) {
                logger.error("Erro ao inspecionar o contêiner na tentativa {}", retryCount, e);
            }
        }

        if (publicPort == null || publicPort.isEmpty()) {
            logger.error("FALHA: O contentor {} iniciou, mas não foi possível encontrar o mapeamento da porta após {} tentativas.", containerId, maxRetries);
            stopContainer(containerId); // Tenta parar o contêiner para não o deixar órfão
            throw new RuntimeException("Falha ao obter a porta de acesso do desafio.");
        }

        String accessUrl = serverPublicAddress + ":" + publicPort;
        logger.info("Contentor iniciado com sucesso: {} em {}", containerId, accessUrl);

        return Map.of(
                "containerId", containerId,
                "accessUrl", accessUrl
        );
    }

    public void stopContainer(String containerId) {
        if (containerId != null && !containerId.isEmpty()) {
            try {
                logger.info("A tentar parar o contentor: {}", containerId);
                dockerClient.stopContainerCmd(containerId).exec();
                logger.info("Comando para parar o contentor {} enviado.", containerId);
            } catch (Exception e) {
                logger.warn("Aviso ao parar o contentor {}. Ele pode já ter sido parado/removido. Mensagem: {}", containerId, e.getMessage());
            }
        }
    }
}