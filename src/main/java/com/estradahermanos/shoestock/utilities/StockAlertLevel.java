package com.estradahermanos.shoestock.utilities;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/** Umbral de stock de una variante que dispara un correo a los administradores. */
public enum StockAlertLevel
{
    LOW(3, 4)
    {
        @Override
        public String headline()
        {
            return "Stock bajo";
        }

        @Override
        public String advice()
        {
            return "El stock es bajo y se recomienda realizar un pedido.";
        }
    },
    RUNNING_OUT(1, 2)
    {
        @Override
        public String headline()
        {
            return "Stock agotándose";
        }

        @Override
        public String advice()
        {
            return "El stock se está agotando y se recomienda hacer un pedido de ese estilo de zapato.";
        }
    },
    OUT_OF_STOCK(0, 0)
    {
        @Override
        public String headline()
        {
            return "Stock agotado";
        }

        @Override
        public String advice()
        {
            return "El stock de ese estilo de zapato quedó agotado y es necesario realizar un pedido de ese estilo de zapato.";
        }
    };

    private final int minimum;
    private final int maximum;

    StockAlertLevel(int minimum, int maximum)
    {
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public abstract String headline();

    public abstract String advice();

    /** Vacío cuando el stock no exige aviso (5 pares o más, o un dato ausente). */
    public static Optional<StockAlertLevel> fromStock(Integer stock)
    {
        if (Objects.isNull(stock))
        {
            return Optional.empty();
        }
        int pairs = stock;
        return Arrays.stream(values())
                .filter(level -> pairs >= level.minimum && pairs <= level.maximum)
                .findFirst();
    }

    public String subject(String shoeCode)
    {
        return headline() + ": " + shoeCode;
    }

    public String body(String shoeCode, String name, String color, Integer size, Integer pairs)
    {
        return "Estilo " + shoeCode
                + " (" + name + "), color " + color
                + ", talla " + size
                + ". Stock actual: " + pairs
                + " pares.\n"
                + advice();
    }
}
