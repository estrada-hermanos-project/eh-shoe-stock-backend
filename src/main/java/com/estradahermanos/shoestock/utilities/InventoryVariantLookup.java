package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InventoryVariantLookup
{
    private final ShoeStockRepository shoeStockRepository;

    public ShoeStock findVariant(String name, String color, Integer size)
    {
        List<ShoeStock> matches = shoeStockRepository.findByShoeNameAndColorAndSize(name, color, size);
        if (matches.isEmpty())
        {
            throw BusinessException.builder()
                    .code(HttpStatus.BAD_REQUEST)
                    .message("Some parameter is not being sent correctly: no matching stock")
                    .build();
        }
        // name is not unique; first match wins
        return matches.get(0);
    }
}
