package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import com.estradahermanos.shoestock.repository.entities.Shoe;
import com.estradahermanos.shoestock.repository.entities.ShoeStock;
import com.estradahermanos.shoestock.repository.entities.Supplier;
import com.estradahermanos.shoestock.repository.repositories.ShoeRepository;
import com.estradahermanos.shoestock.repository.repositories.ShoeStockRepository;
import com.estradahermanos.shoestock.repository.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportCatalogLookup
{
    private final ShoeStockRepository shoeStockRepository;
    private final ShoeRepository      shoeRepository;
    private final SupplierRepository  supplierRepository;

    public Map<Integer, ShoeStock> variantsById(Collection<Integer> ids)
    {
        if (ids == null || ids.isEmpty())
        {
            return Map.of();
        }
        return shoeStockRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ShoeStock::getId, variant -> variant));
    }

    public Map<Integer, ShoeStock> allVariants()
    {
        return shoeStockRepository.findAll().stream()
                .collect(Collectors.toMap(ShoeStock::getId, variant -> variant));
    }

    public Map<String, Shoe> shoesByCode(Collection<String> codes)
    {
        if (codes == null || codes.isEmpty())
        {
            return Map.of();
        }
        return shoeRepository.findAllById(codes).stream()
                .collect(Collectors.toMap(Shoe::getCode, shoe -> shoe));
    }

    public Map<String, Shoe> allShoes()
    {
        return shoeRepository.findAll().stream()
                .collect(Collectors.toMap(Shoe::getCode, shoe -> shoe));
    }

    public Map<Integer, String> supplierNames()
    {
        return supplierRepository.findAll().stream()
                .collect(Collectors.toMap(Supplier::getId, Supplier::getFullName));
    }

    public void requireExistingSupplier(Integer supplierId)
    {
        if (supplierId == null)
        {
            return;
        }
        if (!supplierRepository.existsById(supplierId))
        {
            throw BusinessException.builder()
                    .code(HttpStatus.NOT_FOUND)
                    .message("Supplier not found")
                    .build();
        }
    }
}
