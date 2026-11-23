package com.bibliouni.controlador;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarMaterialesRespondeOk() throws Exception {
        // Verifica que el endpoint de materiales responda (base para las pruebas de la Unidad 2)
        mockMvc.perform(get("/api/materiales"))
                .andExpect(status().isOk());
    }
}
