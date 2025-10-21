package com.sofka.tagoKoder.backend.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sofka.tagoKoder.backend.client.controller.ClientController;
import com.sofka.tagoKoder.backend.client.model.dto.ClientDto;
import com.sofka.tagoKoder.backend.client.model.dto.ApiResponse;
import com.sofka.tagoKoder.backend.client.model.dto.ClientCreateDto;
import com.sofka.tagoKoder.backend.client.service.ClientService;

@SpringBootTest
public class sampleTest {

    private ClientService clientService = mock(ClientService.class);
    private ClientController clientController = new ClientController(clientService);

    @Test
    void createClientTest() {
        // Arrange
        ClientCreateDto newClient = new ClientCreateDto("Dni", "Name", "Password", "Gender", 1, "Address", "9999999999",
                true);
        ClientDto createdClient = new ClientDto(1L, "Dni", "Name", "Gender", 1, "Address", "9999999999", true);
        when(clientService.create(newClient)).thenReturn(createdClient);

        // Act
        ResponseEntity<ApiResponse<ClientDto>> response = clientController.create(newClient);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdClient, response.getBody().getData());
    }
}
