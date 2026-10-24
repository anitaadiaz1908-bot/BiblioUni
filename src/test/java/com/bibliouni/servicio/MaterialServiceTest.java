package com.bibliouni.servicio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class MaterialServiceTest {

    @Autowired
    private MaterialService materialService;

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring cargue correctamente
        assertNotNull(materialService);
    }
}
