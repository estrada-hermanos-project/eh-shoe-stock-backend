package com.estradahermanos.shoestock.repository.cruds;

import com.estradahermanos.shoestock.repository.entities.Sale;
import com.estradahermanos.shoestock.repository.projections.LastSaleDateView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySizeView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByStockView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountBySupplierView;
import com.estradahermanos.shoestock.repository.projections.SaleAmountByTypeView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SaleCrud extends JpaRepository<Sale, Integer>
{
    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);

    List<Sale> findBySaleDate(LocalDate saleDate);

    @Query("""
           SELECT s.shoeStockId AS shoeStockId, SUM(s.amount) AS totalAmount
             FROM Sale s
            WHERE (:startDate IS NULL OR s.saleDate >= :startDate)
              AND (:endDate   IS NULL OR s.saleDate <= :endDate)
            GROUP BY s.shoeStockId
           """)
    List<SaleAmountByStockView> sumAmountGroupedByStock(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Query("""
           SELECT s.shoeStockId AS shoeStockId, MAX(s.saleDate) AS lastSaleDate
             FROM Sale s
            GROUP BY s.shoeStockId
           """)
    List<LastSaleDateView> findLastSaleDateGrouped();

    @Query("""
           SELECT sh.type AS type, SUM(s.amount) AS totalAmount, COUNT(s.id) AS saleCount
             FROM Sale s, ShoeStock ss, Shoe sh
            WHERE s.shoeStockId = ss.id AND ss.shoeId = sh.code
              AND (:startDate IS NULL OR s.saleDate >= :startDate)
              AND (:endDate   IS NULL OR s.saleDate <= :endDate)
            GROUP BY sh.type
           """)
    List<SaleAmountByTypeView> sumAmountGroupedByType(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("""
           SELECT ss.size AS size, sh.type AS type, SUM(s.amount) AS totalAmount
             FROM Sale s, ShoeStock ss, Shoe sh
            WHERE s.shoeStockId = ss.id AND ss.shoeId = sh.code
              AND (:startDate IS NULL OR s.saleDate >= :startDate)
              AND (:endDate   IS NULL OR s.saleDate <= :endDate)
              AND (:type IS NULL OR sh.type = :type)
              AND (:shoeCode IS NULL OR sh.code = :shoeCode)
            GROUP BY ss.size, sh.type
           """)
    List<SaleAmountBySizeView> sumAmountGroupedBySize(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate,
                                                      @Param("type") String type,
                                                      @Param("shoeCode") String shoeCode);

    @Query("""
           SELECT sh.supplier AS supplierId, SUM(s.amount) AS totalAmount, COUNT(DISTINCT sh.code) AS stylesSold
             FROM Sale s, ShoeStock ss, Shoe sh
            WHERE s.shoeStockId = ss.id AND ss.shoeId = sh.code
              AND (:startDate IS NULL OR s.saleDate >= :startDate)
              AND (:endDate   IS NULL OR s.saleDate <= :endDate)
            GROUP BY sh.supplier
           """)
    List<SaleAmountBySupplierView> sumAmountGroupedBySupplier(@Param("startDate") LocalDate startDate,
                                                              @Param("endDate") LocalDate endDate);
}
