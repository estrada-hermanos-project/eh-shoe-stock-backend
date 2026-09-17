package com.estradahermanos.shoestock.service;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShoeDeleteServiceTest
{
    @Mock
    private ShoeRepository shoeRepository;

    private ShoeDeleteService service;

    @BeforeEach
    void setUp()
    {
        service = new ShoeDeleteService(shoeRepository);
    }

    @Test
    void deleteRemovesExistingShoe()
    {
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(true);

        service.deleteByCode("OXF-001");

        verify(shoeRepository).deleteByCode("OXF-001");
    }

    @Test
    void deleteRejectsMissingShoe()
    {
        when(shoeRepository.existsByCode("OXF-001")).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.deleteByCode("OXF-001"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getCode());
        verify(shoeRepository, never()).deleteByCode("OXF-001");
    }
}
